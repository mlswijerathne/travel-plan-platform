#!/bin/bash
# ============================================================
# Travel Plan Platform - Stop Everything
# ============================================================
# Stops all running services + Kafka infrastructure
#
# Usage:
#   ./stop-all.sh              # Stop everything
#   ./stop-all.sh --keep-kafka # Stop services but keep Kafka running
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Docker PATH fix for Windows
export PATH="$PATH:/c/Program Files/Docker/Docker/resources/bin"

KEEP_KAFKA=false
for arg in "$@"; do
    case $arg in
        --keep-kafka) KEEP_KAFKA=true ;;
    esac
done

echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW} Stopping Travel Plan Platform${NC}"
echo -e "${YELLOW}============================================${NC}"
echo ""

STOPPED=0

# ── Method 1: Kill from PID file ──
PID_FILE="$SCRIPT_DIR/.running-pids"
if [ -f "$PID_FILE" ]; then
    echo -e "${BLUE}Stopping tracked services from PID file...${NC}"
    while IFS=: read -r pid name port; do
        if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
            echo -e "  Stopping ${name} (PID: ${pid}, port: ${port})..."
            kill "$pid" 2>/dev/null
            STOPPED=$((STOPPED + 1))
        fi
    done < "$PID_FILE"
    rm -f "$PID_FILE"
fi

# ── Method 2: Kill any Maven spring-boot:run processes ──
echo -e "${BLUE}Checking for Maven Spring Boot processes...${NC}"
MVN_PIDS=$(ps aux 2>/dev/null | grep "[s]pring-boot:run" | awk '{print $2}' || true)
if [ -n "$MVN_PIDS" ]; then
    for pid in $MVN_PIDS; do
        echo -e "  Killing Maven process PID: $pid"
        kill "$pid" 2>/dev/null
        STOPPED=$((STOPPED + 1))
    done
fi

# ── Method 3: Kill Java processes on known service ports ──
echo -e "${BLUE}Checking service ports...${NC}"
PORTS=(8761 8060 8082 8083 8084 8085 8086 8087 8088 8089 8090 8091 8093)
PORT_NAMES=("discovery-server" "api-gateway" "tourist" "hotel" "tour-guide" "vehicle" "booking" "itinerary" "review" "trip-plan" "event" "ecommerce" "ai-agent")

for i in "${!PORTS[@]}"; do
    port=${PORTS[$i]}
    name=${PORT_NAMES[$i]}

    # Try to find process using the port (works on Windows Git Bash with netstat)
    PID=$(netstat -ano 2>/dev/null | grep ":${port} " | grep "LISTENING" | awk '{print $5}' | head -1 || true)

    if [ -n "$PID" ] && [ "$PID" != "0" ]; then
        echo -e "  ${name} (port ${port}) - killing PID ${PID}"
        taskkill //PID "$PID" //F 2>/dev/null || kill "$PID" 2>/dev/null || true
        STOPPED=$((STOPPED + 1))
    else
        # Try lsof for non-Windows systems
        PID=$(lsof -ti ":${port}" 2>/dev/null || true)
        if [ -n "$PID" ]; then
            echo -e "  ${name} (port ${port}) - killing PID ${PID}"
            kill "$PID" 2>/dev/null
            STOPPED=$((STOPPED + 1))
        fi
    fi
done

# ── Stop Kafka ──
echo ""
if [ "$KEEP_KAFKA" = false ]; then
    echo -e "${BLUE}Stopping Kafka infrastructure...${NC}"
    if command -v docker &>/dev/null && docker info &>/dev/null 2>&1; then
        if docker compose -f docker-compose-infra.yml ps --quiet 2>/dev/null | grep -q .; then
            docker compose -f docker-compose-infra.yml down
            echo -e "  ${GREEN}Kafka stopped.${NC}"
        else
            echo -e "  ${YELLOW}Kafka was not running.${NC}"
        fi
    else
        echo -e "  ${YELLOW}Docker not available, skipping Kafka shutdown.${NC}"
    fi
else
    echo -e "${YELLOW}Keeping Kafka running (--keep-kafka).${NC}"
fi

# ── Clean up logs (optional) ──
echo ""
if [ $STOPPED -gt 0 ]; then
    echo -e "${GREEN}Stopped $STOPPED service(s).${NC}"
else
    echo -e "${YELLOW}No running services found.${NC}"
fi

echo -e "${GREEN}Done.${NC}"
