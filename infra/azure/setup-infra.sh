#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────
# Azure Infrastructure Setup for Travel Plan Platform
# Creates: Resource Group, ACR, Container Apps Environment,
#          Managed Eureka, Azure Event Hubs (Kafka)
# ──────────────────────────────────────────────────────────
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Load environment variables
if [ -f "$SCRIPT_DIR/.env" ]; then
  set -a; source "$SCRIPT_DIR/.env"; set +a
else
  echo "ERROR: $SCRIPT_DIR/.env not found. Copy env.template to .env and fill in values."
  exit 1
fi

echo "==> Setting Azure subscription..."
az account set --subscription "$AZURE_SUBSCRIPTION_ID"

# ── 1. Resource Group ────────────────────────────────────
echo "==> Creating resource group: $AZURE_RESOURCE_GROUP"
az group create \
  --name "$AZURE_RESOURCE_GROUP" \
  --location "$AZURE_LOCATION"

# ── 2. Azure Container Registry ─────────────────────────
echo "==> Creating container registry: $ACR_NAME"
az acr create \
  --resource-group "$AZURE_RESOURCE_GROUP" \
  --name "$ACR_NAME" \
  --sku Basic \
  --admin-enabled true

# ── 3. Log Analytics Workspace ───────────────────────────
echo "==> Creating Log Analytics workspace: $LOG_ANALYTICS_WORKSPACE"
az monitor log-analytics workspace create \
  --resource-group "$AZURE_RESOURCE_GROUP" \
  --workspace-name "$LOG_ANALYTICS_WORKSPACE"

LOG_ANALYTICS_ID=$(az monitor log-analytics workspace show \
  --resource-group "$AZURE_RESOURCE_GROUP" \
  --workspace-name "$LOG_ANALYTICS_WORKSPACE" \
  --query customerId -o tsv)

LOG_ANALYTICS_KEY=$(az monitor log-analytics workspace get-shared-keys \
  --resource-group "$AZURE_RESOURCE_GROUP" \
  --workspace-name "$LOG_ANALYTICS_WORKSPACE" \
  --query primarySharedKey -o tsv)

# ── 4. Container Apps Environment ────────────────────────
echo "==> Creating Container Apps environment: $CONTAINER_ENV_NAME"
az containerapp env create \
  --resource-group "$AZURE_RESOURCE_GROUP" \
  --name "$CONTAINER_ENV_NAME" \
  --location "$AZURE_LOCATION" \
  --logs-workspace-id "$LOG_ANALYTICS_ID" \
  --logs-workspace-key "$LOG_ANALYTICS_KEY"

# ── 5. Managed Eureka Server for Spring ──────────────────
echo "==> Creating managed Eureka Server component..."
az containerapp env java-component eureka-server-for-spring create \
  --resource-group "$AZURE_RESOURCE_GROUP" \
  --environment "$CONTAINER_ENV_NAME" \
  --name eureka

# ── 6. Azure Event Hubs Namespace (Kafka protocol) ──────
echo "==> Creating Event Hubs namespace: $EVENTHUB_NAMESPACE"
az eventhubs namespace create \
  --resource-group "$AZURE_RESOURCE_GROUP" \
  --name "$EVENTHUB_NAMESPACE" \
  --sku "$EVENTHUB_SKU" \
  --enable-kafka true \
  --location "$AZURE_LOCATION"

# Create Kafka topics as Event Hub instances
TOPICS=("booking-events" "booking-notifications" "trip-completion-events" "rating-update-events")
for TOPIC in "${TOPICS[@]}"; do
  echo "    Creating topic: $TOPIC"
  az eventhubs eventhub create \
    --resource-group "$AZURE_RESOURCE_GROUP" \
    --namespace-name "$EVENTHUB_NAMESPACE" \
    --name "$TOPIC" \
    --partition-count 3 \
    --message-retention 1

  # Dead letter topic
  echo "    Creating DLT: ${TOPIC}.DLT"
  az eventhubs eventhub create \
    --resource-group "$AZURE_RESOURCE_GROUP" \
    --namespace-name "$EVENTHUB_NAMESPACE" \
    --name "${TOPIC}.DLT" \
    --partition-count 1 \
    --message-retention 1
done

# Get Event Hubs connection string for Kafka bootstrap
EVENTHUB_CONNECTION=$(az eventhubs namespace authorization-rule keys list \
  --resource-group "$AZURE_RESOURCE_GROUP" \
  --namespace-name "$EVENTHUB_NAMESPACE" \
  --name RootManageSharedAccessKey \
  --query primaryConnectionString -o tsv)

KAFKA_BOOTSTRAP="${EVENTHUB_NAMESPACE}.servicebus.windows.net:9093"

echo ""
echo "=============================================="
echo " Infrastructure setup complete!"
echo "=============================================="
echo ""
echo " ACR Login Server:    ${ACR_NAME}.azurecr.io"
echo " Container Env:       $CONTAINER_ENV_NAME"
echo " Eureka:              managed (bind with --bind eureka)"
echo " Kafka Bootstrap:     $KAFKA_BOOTSTRAP"
echo ""
echo " Save these for deploy-services.sh:"
echo "   KAFKA_BOOTSTRAP_SERVERS=$KAFKA_BOOTSTRAP"
echo "   EVENTHUB_CONNECTION_STRING=$EVENTHUB_CONNECTION"
echo ""
echo " Next: Run deploy-services.sh to build and deploy all services."
