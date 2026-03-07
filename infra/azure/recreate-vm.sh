#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────────
# Recreate Azure VM with Upgraded Resources
# Deletes the previous VM and creates a new one with:
#   - RAM: up to 16 GB
#   - CPU: 4 vCPUs (Standard_D4s_v3)
# ──────────────────────────────────────────────────────────────────
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Load environment variables
if [ -f "$SCRIPT_DIR/.env" ]; then
  set -a; source "$SCRIPT_DIR/.env"; set +a
else
  echo "ERROR: $SCRIPT_DIR/.env not found. Copy env.template to .env and fill in values."
  exit 1
fi

# ── Configuration ─────────────────────────────────────────────────
VM_NAME="${VM_NAME:-travel-plan-vm}"
VM_SIZE="Standard_D4s_v3"                  # 4 vCPUs, 16 GB RAM
VM_IMAGE="Canonical:ubuntu-24_04-lts:server:latest"
VM_OS_DISK_SIZE=64                          # GB
VM_ADMIN_USER="azureuser"
NSG_NAME="${VM_NAME}-nsg"
SSH_KEY_PATH="${SSH_KEY_PATH:-~/.ssh/id_rsa.pub}"

echo "=============================================="
echo " Azure VM Upgrade Script"
echo "=============================================="
echo " Resource Group:  $AZURE_RESOURCE_GROUP"
echo " VM Name:         $VM_NAME"
echo " New Size:        $VM_SIZE (4 vCPUs, 16 GB RAM)"
echo " Location:        $AZURE_LOCATION"
echo "=============================================="
echo ""

# ── Safety: confirm before deletion ──────────────────────────────
read -r -p "This will DELETE the existing VM '$VM_NAME' and create a new one. Continue? [y/N] " confirm
if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
  echo "Aborted."
  exit 0
fi

# ── Set subscription ──────────────────────────────────────────────
echo "==> Setting Azure subscription..."
az account set --subscription "$AZURE_SUBSCRIPTION_ID"

# ── Step 1: Get current VM public IP (if allocated) ──────────────
echo "==> Checking for existing VM..."
EXISTING_IP=""
if az vm show --resource-group "$AZURE_RESOURCE_GROUP" --name "$VM_NAME" &>/dev/null; then
  EXISTING_IP=$(az vm list-ip-addresses \
    --resource-group "$AZURE_RESOURCE_GROUP" \
    --name "$VM_NAME" \
    --query '[0].virtualMachine.network.publicIpAddresses[0].ipAddress' \
    -o tsv 2>/dev/null || true)
  echo "    Current VM IP: ${EXISTING_IP:-none}"

  # ── Step 2: Delete the old VM and associated resources ─────────
  echo "==> Deleting existing VM: $VM_NAME ..."
  az vm delete \
    --resource-group "$AZURE_RESOURCE_GROUP" \
    --name "$VM_NAME" \
    --yes \
    --force-deletion true

  echo "==> Cleaning up orphaned resources (NIC, OS disk, public IP)..."
  # Delete NIC
  NIC_NAME="${VM_NAME}VMNic"
  az network nic delete \
    --resource-group "$AZURE_RESOURCE_GROUP" \
    --name "$NIC_NAME" 2>/dev/null || true

  # Delete OS disk
  OS_DISK=$(az disk list \
    --resource-group "$AZURE_RESOURCE_GROUP" \
    --query "[?starts_with(name, '${VM_NAME}')].name" -o tsv 2>/dev/null || true)
  if [ -n "$OS_DISK" ]; then
    az disk delete --resource-group "$AZURE_RESOURCE_GROUP" --name "$OS_DISK" --yes 2>/dev/null || true
  fi

  # Delete old public IP
  PIP_NAME="${VM_NAME}PublicIP"
  az network public-ip delete \
    --resource-group "$AZURE_RESOURCE_GROUP" \
    --name "$PIP_NAME" 2>/dev/null || true

  echo "    Old VM and resources deleted."
else
  echo "    No existing VM found. Creating fresh."
fi

# ── Step 3: Create NSG with required ports ────────────────────────
echo "==> Creating/updating NSG: $NSG_NAME ..."
az network nsg create \
  --resource-group "$AZURE_RESOURCE_GROUP" \
  --name "$NSG_NAME" \
  --location "$AZURE_LOCATION" 2>/dev/null || true

# SSH
az network nsg rule create \
  --resource-group "$AZURE_RESOURCE_GROUP" \
  --nsg-name "$NSG_NAME" \
  --name AllowSSH \
  --priority 1000 \
  --destination-port-ranges 22 \
  --access Allow --protocol Tcp --direction Inbound 2>/dev/null || true

# HTTP/HTTPS (Caddy)
az network nsg rule create \
  --resource-group "$AZURE_RESOURCE_GROUP" \
  --nsg-name "$NSG_NAME" \
  --name AllowHTTP \
  --priority 1100 \
  --destination-port-ranges 80 443 \
  --access Allow --protocol Tcp --direction Inbound 2>/dev/null || true

# ── Step 4: Create the new VM ─────────────────────────────────────
echo "==> Creating new VM: $VM_NAME ($VM_SIZE — 4 vCPUs, 16 GB RAM)..."
az vm create \
  --resource-group "$AZURE_RESOURCE_GROUP" \
  --name "$VM_NAME" \
  --location "$AZURE_LOCATION" \
  --size "$VM_SIZE" \
  --image "$VM_IMAGE" \
  --admin-username "$VM_ADMIN_USER" \
  --ssh-key-values "$SSH_KEY_PATH" \
  --os-disk-size-gb "$VM_OS_DISK_SIZE" \
  --nsg "$NSG_NAME" \
  --custom-data "$SCRIPT_DIR/cloud-init.yml" \
  --public-ip-sku Standard \
  --output table

# ── Step 5: Get new IP ────────────────────────────────────────────
NEW_IP=$(az vm list-ip-addresses \
  --resource-group "$AZURE_RESOURCE_GROUP" \
  --name "$VM_NAME" \
  --query '[0].virtualMachine.network.publicIpAddresses[0].ipAddress' \
  -o tsv)

echo ""
echo "=============================================="
echo " VM Recreation Complete!"
echo "=============================================="
echo ""
echo " New VM:          $VM_NAME"
echo " Size:            $VM_SIZE (4 vCPUs, 16 GB RAM)"
echo " Public IP:       $NEW_IP"
echo " SSH:             ssh $VM_ADMIN_USER@$NEW_IP"
echo ""
echo " IMPORTANT — Next Steps:"
echo " 1. Wait ~2 min for cloud-init to finish (Docker install)"
echo " 2. SSH in and clone the repo:"
echo "      ssh $VM_ADMIN_USER@$NEW_IP"
echo "      cd /opt/travel-plan"
echo "      git clone https://github.com/<your-org>/travel-plan-platform.git ."
echo " 3. Copy .env file to /opt/travel-plan/backend/.env"
echo " 4. Deploy:"
echo "      cd backend"
echo "      docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d"
echo " 5. Update GitHub Secrets:"
echo "      VM_HOST=$NEW_IP"
if [ -n "$EXISTING_IP" ] && [ "$EXISTING_IP" != "$NEW_IP" ]; then
  echo ""
  echo " ⚠ IP changed: $EXISTING_IP → $NEW_IP"
  echo "   Update DNS records and GitHub Secrets (VM_HOST)!"
fi
echo ""
