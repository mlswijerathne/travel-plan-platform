#!/bin/bash
# ============================================================
# Travel Plan Platform - Infrastructure Only (Docker)
# ============================================================
# Starts ONLY Kafka + Kafka UI via Docker Compose
#
# Usage:
#   ./start-infra.sh          # Start Kafka via Docker
#   ./start-infra.sh stop     # Stop Kafka
#   ./start-infra.sh status   # Show Kafka status
#   ./start-infra.sh logs     # Tail Kafka logs
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

COMPOSE_FILE="docker-compose-infra.yml"

# Check Docker
if ! command -v docker &>/dev/null; then
    echo -e "${RED}ERROR: Docker not found.${NC}"
    echo "Install Docker Desktop: https://www.docker.com/products/docker-desktop/"
    exit 1
fi

if ! docker info &>/dev/null 2>&1; then
    echo -e "${RED}ERROR: Docker daemon is not running.${NC}"
    echo "Start Docker Desktop and try again."
    exit 1
fi

case "${1:-start}" in
    stop)
        echo -e "${YELLOW}Stopping Kafka infrastructure...${NC}"
        docker compose -f "$COMPOSE_FILE" down
        echo -e "${GREEN}Kafka stopped.${NC}"
        ;;

    status)
        echo -e "${BLUE}Kafka Infrastructure Status:${NC}"
        echo ""
        docker compose -f "$COMPOSE_FILE" ps
        echo ""
        # Show topics if Kafka is running
        if docker compose -f "$COMPOSE_FILE" ps kafka 2>/dev/null | grep -q "running"; then
            echo -e "${BLUE}Kafka Topics:${NC}"
            docker compose -f "$COMPOSE_FILE" exec kafka kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null
        fi
        ;;

    logs)
        echo -e "${BLUE}Tailing Kafka logs (Ctrl+C to stop)...${NC}"
        docker compose -f "$COMPOSE_FILE" logs -f kafka
        ;;

    start|"")
        echo -e "${BLUE}Starting Kafka infrastructure via Docker...${NC}"
        docker compose -f "$COMPOSE_FILE" up -d

        # Wait for Kafka health
        echo -n "  Waiting for Kafka to be ready..."
        WAIT=0
        while [ $WAIT -lt 60 ]; do
            if docker compose -f "$COMPOSE_FILE" ps kafka 2>/dev/null | grep -q "healthy"; then
                echo -e " ${GREEN}READY${NC}"
                break
            fi
            sleep 2
            WAIT=$((WAIT + 2))
            echo -n "."
        done
        if [ $WAIT -ge 60 ]; then
            echo -e " ${YELLOW}TIMEOUT${NC} (Kafka may still be starting)"
        fi

        echo ""
        echo -e "${GREEN}Infrastructure Status:${NC}"
        docker compose -f "$COMPOSE_FILE" ps
        echo ""
        echo -e "  Kafka:    ${GREEN}localhost:29092${NC} (external), localhost:9092 (internal)"
        echo -e "  Kafka UI: ${GREEN}http://localhost:8080${NC}"
        echo ""
        echo -e "  Status:  ${BLUE}./start-infra.sh status${NC}"
        echo -e "  Logs:    ${BLUE}./start-infra.sh logs${NC}"
        echo -e "  Stop:    ${BLUE}./start-infra.sh stop${NC}"
        ;;

    *)
        echo "Usage: ./start-infra.sh [start|stop|status|logs]"
        exit 1
        ;;
esac
