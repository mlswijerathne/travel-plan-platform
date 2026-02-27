#!/bin/bash
# ============================================================
# Travel Plan Platform - Local Services Startup (Maven)
# ============================================================
# Starts Spring Boot services locally using Maven.
# For Kafka, run start-infra.sh first (or use run-all.sh).
#
# Usage:
#   ./start-local.sh          # Start all services
#   ./start-local.sh core     # Core services only (no Kafka needed)
#   ./start-local.sh infra    # Only Eureka + Gateway
#   ./start-local.sh <name>   # Start a single service (e.g., tourist-service)
# ============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# Load .env file
if [ -f .env ]; then
    echo -e "${GREEN}Loading .env file...${NC}"
    set -a
    source .env
    set +a
else
    echo -e "${RED}ERROR: backend/.env file not found!${NC}"
    echo "Run: ./setup-env.sh  or  cp .env.example .env"
    exit 1
fi

# Validate critical env vars
if [ -z "$SUPABASE_JWT_SECRET" ] || [ "$SUPABASE_JWT_SECRET" = "your-jwt-secret" ]; then
    echo -e "${RED}ERROR: SUPABASE_JWT_SECRET not set in .env${NC}"
    exit 1
fi

# PID tracking
PIDS=()
PID_FILE="$SCRIPT_DIR/.running-pids"
LOG_DIR="$SCRIPT_DIR/logs"
mkdir -p "$LOG_DIR"

cleanup() {
    echo -e "\n${YELLOW}Shutting down all services...${NC}"
    for pid in "${PIDS[@]}"; do
        if kill -0 "$pid" 2>/dev/null; then
            kill "$pid" 2>/dev/null
        fi
    done
    wait 2>/dev/null
    rm -f "$PID_FILE"
    echo -e "${GREEN}All services stopped.${NC}"
}
trap cleanup EXIT INT TERM

# Build common-lib first
build_common() {
    echo -e "${BLUE}Building common-lib...${NC}"
    mvn install -N -q
    mvn -f common-lib/pom.xml install -q -DskipTests
    echo -e "${GREEN}common-lib built.${NC}"
}

# Start a service in background
start_service() {
    local name=$1
    local port=$2
    local db_url_var=$3
    local db_user_var=$4
    local db_pass_var=$5

    echo -e "${BLUE}Starting ${name} on port ${port}...${NC}"

    # Set DB env vars if provided
    if [ -n "$db_url_var" ]; then
        export DATABASE_URL="${!db_url_var}"
        export DATABASE_USERNAME="${!db_user_var}"
        export DATABASE_PASSWORD="${!db_pass_var}"
    fi

    mvn -f "${name}/pom.xml" spring-boot:run -DskipTests \
        -Dspring-boot.run.jvmArguments="-Xmx256m" \
        > "$LOG_DIR/${name}.log" 2>&1 &

    local pid=$!
    PIDS+=($pid)
    echo "$pid:$name:$port" >> "$PID_FILE"
    echo -e "${GREEN}  ${name} started (PID: ${pid}, log: logs/${name}.log)${NC}"

    # Unset per-service vars
    unset DATABASE_URL DATABASE_USERNAME DATABASE_PASSWORD
}

# Wait for a service to be healthy
wait_for_service() {
    local name=$1
    local port=$2
    local max_wait=${3:-120}
    local elapsed=0

    echo -n "  Waiting for ${name} (port ${port})..."
    while [ $elapsed -lt $max_wait ]; do
        if curl -s "http://localhost:${port}/actuator/health" > /dev/null 2>&1; then
            echo -e " ${GREEN}UP${NC}"
            return 0
        fi
        sleep 2
        elapsed=$((elapsed + 2))
        echo -n "."
    done
    echo -e " ${RED}TIMEOUT (check logs/${name}.log)${NC}"
    return 1
}

# ============================================================
# MAIN
# ============================================================

MODE="${1:-all}"

# ── Single service mode ──
KNOWN_SERVICES="discovery-server api-gateway tourist-service hotel-service tour-guide-service vehicle-service booking-service itinerary-service review-service trip-plan-service event-service ecommerce-service ai-agent-service"
if echo "$KNOWN_SERVICES" | grep -qw "$MODE"; then
    echo -e "${GREEN}Starting single service: ${MODE}${NC}"
    build_common

    # Map service to port + DB vars
    case "$MODE" in
        discovery-server)   start_service "$MODE" 8761 ;;
        api-gateway)        start_service "$MODE" 8060 ;;
        tourist-service)    start_service "$MODE" 8082 "TOURIST_DB_URL" "TOURIST_DB_USERNAME" "TOURIST_DB_PASSWORD" ;;
        hotel-service)      start_service "$MODE" 8083 "HOTEL_DB_URL" "HOTEL_DB_USERNAME" "HOTEL_DB_PASSWORD" ;;
        tour-guide-service) start_service "$MODE" 8084 "TOUR_GUIDE_DB_URL" "TOUR_GUIDE_DB_USERNAME" "TOUR_GUIDE_DB_PASSWORD" ;;
        vehicle-service)    start_service "$MODE" 8085 "VEHICLE_DB_URL" "VEHICLE_DB_USERNAME" "VEHICLE_DB_PASSWORD" ;;
        booking-service)    start_service "$MODE" 8086 "BOOKING_DB_URL" "BOOKING_DB_USERNAME" "BOOKING_DB_PASSWORD" ;;
        itinerary-service)  start_service "$MODE" 8087 "ITINERARY_DB_URL" "ITINERARY_DB_USERNAME" "ITINERARY_DB_PASSWORD" ;;
        review-service)     start_service "$MODE" 8088 "REVIEW_DB_URL" "REVIEW_DB_USERNAME" "REVIEW_DB_PASSWORD" ;;
        trip-plan-service)  start_service "$MODE" 8089 "TRIP_PLAN_DB_URL" "TRIP_PLAN_DB_USERNAME" "TRIP_PLAN_DB_PASSWORD" ;;
        event-service)      start_service "$MODE" 8090 "EVENT_DB_URL" "EVENT_DB_USERNAME" "EVENT_DB_PASSWORD" ;;
        ecommerce-service)  start_service "$MODE" 8091 "ECOMMERCE_DB_URL" "ECOMMERCE_DB_USERNAME" "ECOMMERCE_DB_PASSWORD" ;;
        ai-agent-service)   start_service "$MODE" 8093 ;;
    esac

    echo ""
    echo "Press Ctrl+C to stop."
    wait
    exit 0
fi

echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN} Travel Plan Platform - Local Startup${NC}"
echo -e "${GREEN} Mode: ${MODE}${NC}"
echo -e "${GREEN}============================================${NC}"

# Step 1: Build
build_common

# Step 2: Start Discovery Server (Eureka)
echo -e "\n${YELLOW}=== Phase 1: Service Discovery ===${NC}"
start_service "discovery-server" 8761
wait_for_service "discovery-server" 8761 90

if [ "$MODE" = "infra" ]; then
    echo -e "\n${YELLOW}=== Phase 2: API Gateway ===${NC}"
    start_service "api-gateway" 8060
    wait_for_service "api-gateway" 8060 90

    echo -e "\n${GREEN}Infrastructure ready.${NC}"
    echo -e "  Eureka:  http://localhost:8761"
    echo -e "  Gateway: http://localhost:8060"
    echo ""
    echo "Press Ctrl+C to stop."
    wait
    exit 0
fi

# Step 3: Start API Gateway
echo -e "\n${YELLOW}=== Phase 2: API Gateway ===${NC}"
start_service "api-gateway" 8060
wait_for_service "api-gateway" 8060 90

# Step 4: Core services (no Kafka dependency)
echo -e "\n${YELLOW}=== Phase 3: Core Services ===${NC}"

start_service "tourist-service" 8082 "TOURIST_DB_URL" "TOURIST_DB_USERNAME" "TOURIST_DB_PASSWORD"
start_service "vehicle-service" 8085 "VEHICLE_DB_URL" "VEHICLE_DB_USERNAME" "VEHICLE_DB_PASSWORD"
start_service "trip-plan-service" 8089 "TRIP_PLAN_DB_URL" "TRIP_PLAN_DB_USERNAME" "TRIP_PLAN_DB_PASSWORD"
start_service "event-service" 8090 "EVENT_DB_URL" "EVENT_DB_USERNAME" "EVENT_DB_PASSWORD"
start_service "ecommerce-service" 8091 "ECOMMERCE_DB_URL" "ECOMMERCE_DB_USERNAME" "ECOMMERCE_DB_PASSWORD"
start_service "ai-agent-service" 8093

if [ "$MODE" = "core" ]; then
    echo -e "\n${GREEN}============================================${NC}"
    echo -e "${GREEN} Core services started (no Kafka needed)${NC}"
    echo -e "${GREEN}============================================${NC}"
    echo -e "  Eureka:     http://localhost:8761"
    echo -e "  Gateway:    http://localhost:8060"
    echo -e "  Tourist:    http://localhost:8082"
    echo -e "  Vehicle:    http://localhost:8085"
    echo -e "  Trip Plan:  http://localhost:8089"
    echo -e "  Event:      http://localhost:8090"
    echo -e "  Ecommerce:  http://localhost:8091"
    echo -e "  AI Agent:   http://localhost:8093"
    echo ""
    echo "Press Ctrl+C to stop all services."
    wait
    exit 0
fi

# Step 5: Kafka-dependent services
echo -e "\n${YELLOW}=== Phase 4: Kafka-dependent Services ===${NC}"
echo -e "${YELLOW}(Kafka must be running - use ./start-infra.sh)${NC}"

start_service "hotel-service" 8083 "HOTEL_DB_URL" "HOTEL_DB_USERNAME" "HOTEL_DB_PASSWORD"
start_service "tour-guide-service" 8084 "TOUR_GUIDE_DB_URL" "TOUR_GUIDE_DB_USERNAME" "TOUR_GUIDE_DB_PASSWORD"
start_service "booking-service" 8086 "BOOKING_DB_URL" "BOOKING_DB_USERNAME" "BOOKING_DB_PASSWORD"
start_service "itinerary-service" 8087 "ITINERARY_DB_URL" "ITINERARY_DB_USERNAME" "ITINERARY_DB_PASSWORD"
start_service "review-service" 8088 "REVIEW_DB_URL" "REVIEW_DB_USERNAME" "REVIEW_DB_PASSWORD"

echo -e "\n${GREEN}============================================${NC}"
echo -e "${GREEN} ALL SERVICES STARTED${NC}"
echo -e "${GREEN}============================================${NC}"
echo -e "  Eureka Dashboard: http://localhost:8761"
echo -e "  API Gateway:      http://localhost:8060"
echo -e "  Kafka UI:         http://localhost:8080 (if Docker infra running)"
echo ""
echo -e "  Services:"
echo -e "    Tourist:      http://localhost:8082"
echo -e "    Hotel:        http://localhost:8083"
echo -e "    Tour Guide:   http://localhost:8084"
echo -e "    Vehicle:      http://localhost:8085"
echo -e "    Booking:      http://localhost:8086"
echo -e "    Itinerary:    http://localhost:8087"
echo -e "    Review:       http://localhost:8088"
echo -e "    Trip Plan:    http://localhost:8089"
echo -e "    Event:        http://localhost:8090"
echo -e "    Ecommerce:    http://localhost:8091"
echo -e "    AI Agent:     http://localhost:8093"
echo ""
echo -e "  Logs:   ${LOG_DIR}/"
echo -e "  Health: ./health-check.sh"
echo ""
echo "Press Ctrl+C to stop all services."
wait
