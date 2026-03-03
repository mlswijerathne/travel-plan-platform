#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────
# Deploy all Travel Plan services to Azure Container Apps
# Prerequisites: setup-infra.sh has been run
# ──────────────────────────────────────────────────────────
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend"

# Load environment variables
if [ -f "$SCRIPT_DIR/.env" ]; then
  set -a; source "$SCRIPT_DIR/.env"; set +a
else
  echo "ERROR: .env not found. Copy env.template to .env first."
  exit 1
fi

ACR_LOGIN_SERVER="${ACR_NAME}.azurecr.io"
IMAGE_TAG="${IMAGE_TAG:-latest}"

# Event Hubs Kafka config
KAFKA_BOOTSTRAP="${EVENTHUB_NAMESPACE}.servicebus.windows.net:9093"
EVENTHUB_CONNECTION="${EVENTHUB_CONNECTION_STRING}"
KAFKA_SASL_JAAS="org.apache.kafka.common.security.plain.PlainLoginModule required username=\"\$ConnectionString\" password=\"${EVENTHUB_CONNECTION}\";"

# ── ACR Login ────────────────────────────────────────────
echo "==> Logging in to ACR: $ACR_LOGIN_SERVER"
az acr login --name "$ACR_NAME"

# ── Build & Push Images ─────────────────────────────────
# All Dockerfiles use backend/ as build context
SERVICES=(
  "api-gateway"
  "tourist-service"
  "hotel-service"
  "tour-guide-service"
  "vehicle-service"
  "booking-service"
  "itinerary-service"
  "review-service"
  "trip-plan-service"
  "event-service"
  "ecommerce-service"
  "ai-agent-service"
)

for SERVICE in "${SERVICES[@]}"; do
  IMAGE="${ACR_LOGIN_SERVER}/${SERVICE}:${IMAGE_TAG}"
  echo "==> Building $SERVICE -> $IMAGE"
  docker build \
    -f "$BACKEND_DIR/$SERVICE/Dockerfile" \
    -t "$IMAGE" \
    "$BACKEND_DIR"
  echo "==> Pushing $IMAGE"
  docker push "$IMAGE"
done

# ── Helper: deploy a service ────────────────────────────
deploy_service() {
  local NAME=$1
  local PORT=$2
  local INGRESS=$3  # "external" or "internal"
  local DB_URL=$4
  local DB_USER=$5
  local DB_PASS=$6
  shift 6
  local EXTRA_ENV=("$@")

  local IMAGE="${ACR_LOGIN_SERVER}/${NAME}:${IMAGE_TAG}"

  echo "==> Deploying $NAME (port $PORT, $INGRESS ingress)"

  # Build env vars array
  local ENV_VARS=(
    "SPRING_PROFILES_ACTIVE=prod"
    "SUPABASE_URL=$SUPABASE_URL"
    "SUPABASE_JWT_SECRET=secretref:supabase-jwt-secret"
    "CORS_ALLOWED_ORIGINS=$CORS_ALLOWED_ORIGINS"
  )

  # Add database env vars if provided
  if [ -n "$DB_URL" ]; then
    ENV_VARS+=(
      "DATABASE_URL=$DB_URL"
      "DATABASE_USERNAME=$DB_USER"
      "DATABASE_PASSWORD=secretref:db-pass-${NAME}"
    )
  fi

  # Add extra env vars
  for EV in "${EXTRA_ENV[@]}"; do
    ENV_VARS+=("$EV")
  done

  # Build secrets array
  local SECRETS="supabase-jwt-secret=$SUPABASE_JWT_SECRET"
  if [ -n "$DB_PASS" ]; then
    SECRETS="$SECRETS db-pass-${NAME}=$DB_PASS"
  fi

  az containerapp create \
    --resource-group "$AZURE_RESOURCE_GROUP" \
    --environment "$CONTAINER_ENV_NAME" \
    --name "$NAME" \
    --image "$IMAGE" \
    --registry-server "$ACR_LOGIN_SERVER" \
    --target-port "$PORT" \
    --ingress "$INGRESS" \
    --min-replicas 0 \
    --max-replicas 3 \
    --cpu 0.5 \
    --memory 1Gi \
    --secrets $SECRETS \
    --env-vars "${ENV_VARS[@]}" \
    --bind eureka
}

# ── Kafka env vars (for services that use Kafka) ────────
KAFKA_ENV=(
  "KAFKA_BOOTSTRAP_SERVERS=$KAFKA_BOOTSTRAP"
  "SPRING_KAFKA_PROPERTIES_SECURITY_PROTOCOL=SASL_SSL"
  "SPRING_KAFKA_PROPERTIES_SASL_MECHANISM=PLAIN"
  "SPRING_KAFKA_PROPERTIES_SASL_JAAS_CONFIG=$KAFKA_SASL_JAAS"
)

# ── Deploy API Gateway (external ingress, no DB) ────────
echo "==> Deploying api-gateway (external ingress)"
az containerapp create \
  --resource-group "$AZURE_RESOURCE_GROUP" \
  --environment "$CONTAINER_ENV_NAME" \
  --name "api-gateway" \
  --image "${ACR_LOGIN_SERVER}/api-gateway:${IMAGE_TAG}" \
  --registry-server "$ACR_LOGIN_SERVER" \
  --target-port 8060 \
  --ingress external \
  --min-replicas 1 \
  --max-replicas 5 \
  --cpu 0.5 \
  --memory 1Gi \
  --secrets "supabase-jwt-secret=$SUPABASE_JWT_SECRET" \
  --env-vars \
    "SPRING_PROFILES_ACTIVE=prod" \
    "SUPABASE_URL=$SUPABASE_URL" \
    "SUPABASE_JWT_SECRET=secretref:supabase-jwt-secret" \
    "CORS_ALLOWED_ORIGINS=$CORS_ALLOWED_ORIGINS" \
  --bind eureka

# ── Deploy DB services (internal ingress) ────────────────

# Services WITHOUT Kafka
deploy_service "trip-plan-service" 8089 internal \
  "$TRIP_PLAN_DB_URL" "$TRIP_PLAN_DB_USERNAME" "$TRIP_PLAN_DB_PASSWORD"

deploy_service "ecommerce-service" 8091 internal \
  "$ECOMMERCE_DB_URL" "$ECOMMERCE_DB_USERNAME" "$ECOMMERCE_DB_PASSWORD"

# Services WITH Kafka
deploy_service "tourist-service" 8082 internal \
  "$TOURIST_DB_URL" "$TOURIST_DB_USERNAME" "$TOURIST_DB_PASSWORD" \
  "${KAFKA_ENV[@]}"

deploy_service "hotel-service" 8083 internal \
  "$HOTEL_DB_URL" "$HOTEL_DB_USERNAME" "$HOTEL_DB_PASSWORD" \
  "${KAFKA_ENV[@]}"

deploy_service "tour-guide-service" 8084 internal \
  "$TOUR_GUIDE_DB_URL" "$TOUR_GUIDE_DB_USERNAME" "$TOUR_GUIDE_DB_PASSWORD" \
  "${KAFKA_ENV[@]}"

deploy_service "vehicle-service" 8085 internal \
  "$VEHICLE_DB_URL" "$VEHICLE_DB_USERNAME" "$VEHICLE_DB_PASSWORD" \
  "${KAFKA_ENV[@]}"

deploy_service "booking-service" 8086 internal \
  "$BOOKING_DB_URL" "$BOOKING_DB_USERNAME" "$BOOKING_DB_PASSWORD" \
  "${KAFKA_ENV[@]}"

deploy_service "itinerary-service" 8087 internal \
  "$ITINERARY_DB_URL" "$ITINERARY_DB_USERNAME" "$ITINERARY_DB_PASSWORD" \
  "${KAFKA_ENV[@]}"

deploy_service "review-service" 8088 internal \
  "$REVIEW_DB_URL" "$REVIEW_DB_USERNAME" "$REVIEW_DB_PASSWORD" \
  "${KAFKA_ENV[@]}"

deploy_service "event-service" 8090 internal \
  "$EVENT_DB_URL" "$EVENT_DB_USERNAME" "$EVENT_DB_PASSWORD" \
  "${KAFKA_ENV[@]}"

# AI Agent Service (DB + extra API keys, no Kafka)
deploy_service "ai-agent-service" 8093 internal \
  "$AI_AGENT_DB_URL" "$AI_AGENT_DB_USERNAME" "$AI_AGENT_DB_PASSWORD" \
  "OPENAI_API_KEY=$OPENAI_API_KEY" \
  "GOOGLE_MAPS_API_KEY=$GOOGLE_MAPS_API_KEY"

# ── Print Results ────────────────────────────────────────
echo ""
echo "=============================================="
echo " All services deployed!"
echo "=============================================="
echo ""

GATEWAY_FQDN=$(az containerapp show \
  --resource-group "$AZURE_RESOURCE_GROUP" \
  --name api-gateway \
  --query properties.configuration.ingress.fqdn -o tsv)

echo " API Gateway URL: https://$GATEWAY_FQDN"
echo ""
echo " Set this as NEXT_PUBLIC_API_BASE_URL in your frontend:"
echo "   NEXT_PUBLIC_API_BASE_URL=https://$GATEWAY_FQDN"
echo ""
echo " Update CORS_ALLOWED_ORIGINS if your frontend URL changed."
