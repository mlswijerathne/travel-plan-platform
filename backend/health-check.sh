#!/bin/bash
# ============================================================
# Travel Plan Platform - Health Check Dashboard
# ============================================================
# Checks the health status of all services
#
# Usage:
#   ./health-check.sh          # Check all services once
#   ./health-check.sh --watch  # Continuous monitoring (refresh every 10s)
#   ./health-check.sh --json   # Output JSON health details
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# Docker PATH fix for Windows
export PATH="$PATH:/c/Program Files/Docker/Docker/resources/bin"

# Args
WATCH=false
JSON_MODE=false
for arg in "$@"; do
    case $arg in
        --watch) WATCH=true ;;
        --json)  JSON_MODE=true ;;
    esac
done

# ── Service definitions ──
declare -a SERVICES=(
    "discovery-server:8761:Infrastructure"
    "api-gateway:8060:Infrastructure"
    "tourist-service:8082:Core"
    "vehicle-service:8085:Core"
    "trip-plan-service:8089:Core"
    "event-service:8090:Core"
    "ecommerce-service:8091:Core"
    "ai-agent-service:8093:Core"
    "hotel-service:8083:Kafka"
    "tour-guide-service:8084:Kafka"
    "booking-service:8086:Kafka"
    "itinerary-service:8087:Kafka"
    "review-service:8088:Kafka"
)

check_service() {
    local name=$1
    local port=$2
    local timeout=3

    local response
    response=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout $timeout "http://localhost:${port}/actuator/health" 2>/dev/null)

    if [ "$response" = "200" ]; then
        echo "UP"
    elif [ "$response" = "503" ]; then
        echo "DEGRADED"
    elif [ "$response" = "000" ]; then
        echo "DOWN"
    else
        echo "ERROR:$response"
    fi
}

check_kafka() {
    if command -v docker &>/dev/null && docker info &>/dev/null 2>&1; then
        local kafka_status
        kafka_status=$(docker compose -f docker-compose-infra.yml ps kafka --format "{{.Health}}" 2>/dev/null || echo "")
        if [ "$kafka_status" = "healthy" ]; then
            echo "UP"
        elif [ -n "$kafka_status" ]; then
            echo "STARTING"
        else
            echo "DOWN"
        fi
    else
        echo "N/A"
    fi
}

check_kafka_ui() {
    local response
    response=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 3 "http://localhost:8080" 2>/dev/null)
    if [ "$response" = "200" ]; then
        echo "UP"
    else
        echo "DOWN"
    fi
}

run_health_check() {
    local total=0
    local up=0
    local down=0

    if [ "$JSON_MODE" = true ]; then
        echo "{"
        echo '  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'",'
        echo '  "services": {'
    else
        echo -e "${BOLD}${CYAN}============================================${NC}"
        echo -e "${BOLD}${CYAN} Travel Plan Platform - Health Check${NC}"
        echo -e "${BOLD}${CYAN} $(date '+%Y-%m-%d %H:%M:%S')${NC}"
        echo -e "${BOLD}${CYAN}============================================${NC}"
        echo ""
    fi

    # Check Kafka
    local kafka_status
    kafka_status=$(check_kafka)
    local kafka_ui_status
    kafka_ui_status=$(check_kafka_ui)

    if [ "$JSON_MODE" = false ]; then
        echo -e "  ${BOLD}Docker Infrastructure:${NC}"
        case $kafka_status in
            UP)      echo -e "    Kafka (9092/29092)    ${GREEN}[UP]${NC}" ;;
            STARTING) echo -e "    Kafka (9092/29092)    ${YELLOW}[STARTING]${NC}" ;;
            DOWN)    echo -e "    Kafka (9092/29092)    ${RED}[DOWN]${NC}" ;;
            N/A)     echo -e "    Kafka (9092/29092)    ${YELLOW}[N/A - Docker unavailable]${NC}" ;;
        esac
        case $kafka_ui_status in
            UP)   echo -e "    Kafka UI (8080)       ${GREEN}[UP]${NC}" ;;
            DOWN) echo -e "    Kafka UI (8080)       ${RED}[DOWN]${NC}" ;;
        esac
        echo ""
    fi

    local current_group=""
    local first_json=true

    for entry in "${SERVICES[@]}"; do
        IFS=: read -r name port group <<< "$entry"
        total=$((total + 1))

        local status
        status=$(check_service "$name" "$port")

        if [ "$JSON_MODE" = true ]; then
            if [ "$first_json" = true ]; then
                first_json=false
            else
                echo ","
            fi
            echo -n "    \"$name\": {\"port\": $port, \"group\": \"$group\", \"status\": \"$status\"}"
        else
            # Print group header
            if [ "$group" != "$current_group" ]; then
                current_group=$group
                echo -e "  ${BOLD}${group} Services:${NC}"
            fi

            # Format status display
            local display_name
            display_name=$(printf "%-24s" "$name ($port)")
            case $status in
                UP)
                    echo -e "    ${display_name} ${GREEN}[UP]${NC}"
                    up=$((up + 1))
                    ;;
                DEGRADED)
                    echo -e "    ${display_name} ${YELLOW}[DEGRADED]${NC}"
                    ;;
                DOWN)
                    echo -e "    ${display_name} ${RED}[DOWN]${NC}"
                    down=$((down + 1))
                    ;;
                ERROR:*)
                    local code=${status#ERROR:}
                    echo -e "    ${display_name} ${RED}[ERROR: HTTP $code]${NC}"
                    down=$((down + 1))
                    ;;
            esac
        fi
    done

    if [ "$JSON_MODE" = true ]; then
        echo ""
        echo "  },"
        echo "  \"kafka\": \"$kafka_status\","
        echo "  \"kafka_ui\": \"$kafka_ui_status\","
        echo "  \"summary\": {\"total\": $total, \"up\": $up, \"down\": $down}"
        echo "}"
    else
        echo ""
        echo -e "  ${BOLD}Summary:${NC} ${GREEN}${up}/${total} UP${NC}"
        if [ $down -gt 0 ]; then
            echo -e "           ${RED}${down} DOWN${NC} - check logs/ for details"
        fi
        echo ""
    fi
}

# ── Main ──
if [ "$WATCH" = true ]; then
    echo -e "${YELLOW}Monitoring mode - refreshing every 10 seconds (Ctrl+C to stop)${NC}"
    echo ""
    while true; do
        clear
        run_health_check
        echo -e "${YELLOW}Refreshing in 10 seconds... (Ctrl+C to stop)${NC}"
        sleep 10
    done
else
    run_health_check
fi
