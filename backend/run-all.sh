#!/bin/bash
# ============================================================
# Travel Plan Platform - MASTER Startup Script
# ============================================================
# One command to start EVERYTHING:
#   1. Checks prerequisites (Java, Maven, Docker, .env)
#   2. Builds common-lib
#   3. Starts Kafka via Docker
#   4. Starts all Spring Boot services via Maven
#   5. Runs health checks
#
# Usage:
#   ./run-all.sh              # Start everything
#   ./run-all.sh --no-kafka   # Skip Kafka (core services only)
#   ./run-all.sh --build-only # Only build, don't start
# ============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# ── Colors ──
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# ── Docker PATH fix for Windows ──
export PATH="$PATH:/c/Program Files/Docker/Docker/resources/bin"

# ── Args ──
NO_KAFKA=false
BUILD_ONLY=false
for arg in "$@"; do
    case $arg in
        --no-kafka)  NO_KAFKA=true ;;
        --build-only) BUILD_ONLY=true ;;
    esac
done

# ── PID / State tracking ──
PIDS=()
PID_FILE="$SCRIPT_DIR/.running-pids"
LOG_DIR="$SCRIPT_DIR/logs"
mkdir -p "$LOG_DIR"

# ── Health tracking ──
FAILED_SERVICES=()
SERVICE_STATUS=()   # "name:port:UP|DOWN"

# ── Cleanup handler ──
cleanup() {
    echo ""
    echo -e "${YELLOW}============================================${NC}"
    echo -e "${YELLOW} Shutting down Travel Plan Platform...${NC}"
    echo -e "${YELLOW}============================================${NC}"

    # Kill all tracked Java/Maven processes
    for pid in "${PIDS[@]}"; do
        if kill -0 "$pid" 2>/dev/null; then
            echo -e "  Stopping PID $pid..."
            kill "$pid" 2>/dev/null
        fi
    done
    wait 2>/dev/null

    # Stop Kafka if we started it
    if [ "$NO_KAFKA" = false ] && command -v docker &>/dev/null; then
        echo -e "  Stopping Kafka infrastructure..."
        docker compose -f docker-compose-infra.yml down 2>/dev/null
    fi

    # Cleanup PID file
    rm -f "$PID_FILE"

    echo -e "${GREEN}All services stopped.${NC}"
}
trap cleanup EXIT INT TERM

# ============================================================
# STEP 1: Prerequisites Check
# ============================================================
echo -e "${BOLD}${CYAN}============================================${NC}"
echo -e "${BOLD}${CYAN} Travel Plan Platform - Full System Start${NC}"
echo -e "${BOLD}${CYAN}============================================${NC}"
echo ""

echo -e "${BLUE}[1/6] Checking prerequisites...${NC}"

ERRORS=0

# Java
if command -v java &>/dev/null; then
    JAVA_VER=$(java -version 2>&1 | head -1 | awk -F '"' '{print $2}' | cut -d. -f1)
    if [ "$JAVA_VER" -ge 21 ] 2>/dev/null; then
        echo -e "  ${GREEN}[OK]${NC} Java $JAVA_VER"
    else
        echo -e "  ${RED}[FAIL]${NC} Java 21+ required (found: $JAVA_VER)"
        ERRORS=$((ERRORS + 1))
    fi
else
    echo -e "  ${RED}[FAIL]${NC} Java not found"
    ERRORS=$((ERRORS + 1))
fi

# Maven
if command -v mvn &>/dev/null; then
    MVN_VER=$(mvn -version 2>&1 | head -1 | awk '{print $3}')
    echo -e "  ${GREEN}[OK]${NC} Maven $MVN_VER"
else
    echo -e "  ${RED}[FAIL]${NC} Maven not found"
    ERRORS=$((ERRORS + 1))
fi

# Docker (only required if Kafka is needed)
if [ "$NO_KAFKA" = false ]; then
    if command -v docker &>/dev/null; then
        if docker info &>/dev/null 2>&1; then
            DOCKER_VER=$(docker --version | awk '{print $3}' | tr -d ',')
            echo -e "  ${GREEN}[OK]${NC} Docker $DOCKER_VER (daemon running)"
        else
            echo -e "  ${RED}[FAIL]${NC} Docker installed but daemon not running"
            echo -e "        ${YELLOW}Start Docker Desktop or use --no-kafka flag${NC}"
            ERRORS=$((ERRORS + 1))
        fi
    else
        echo -e "  ${RED}[FAIL]${NC} Docker not found (needed for Kafka)"
        echo -e "        ${YELLOW}Install Docker Desktop or use --no-kafka flag${NC}"
        ERRORS=$((ERRORS + 1))
    fi
else
    echo -e "  ${YELLOW}[SKIP]${NC} Docker (--no-kafka mode)"
fi

# curl (for health checks)
if command -v curl &>/dev/null; then
    echo -e "  ${GREEN}[OK]${NC} curl"
else
    echo -e "  ${YELLOW}[WARN]${NC} curl not found (health checks will be skipped)"
fi

# .env file
if [ -f .env ]; then
    echo -e "  ${GREEN}[OK]${NC} .env file found"
    set -a
    source .env
    set +a
else
    echo -e "  ${RED}[FAIL]${NC} backend/.env file not found!"
    echo -e "        Run: ${CYAN}./setup-env.sh${NC} or ${CYAN}cp .env.example .env${NC}"
    ERRORS=$((ERRORS + 1))
fi

# Validate critical env vars
if [ -z "$SUPABASE_JWT_SECRET" ] || [ "$SUPABASE_JWT_SECRET" = "your-jwt-secret" ]; then
    echo -e "  ${RED}[FAIL]${NC} SUPABASE_JWT_SECRET not configured in .env"
    ERRORS=$((ERRORS + 1))
else
    echo -e "  ${GREEN}[OK]${NC} SUPABASE_JWT_SECRET configured"
fi

if [ $ERRORS -gt 0 ]; then
    echo ""
    echo -e "${RED}$ERRORS prerequisite(s) failed. Fix the issues above and retry.${NC}"
    exit 1
fi

echo -e "  ${GREEN}All prerequisites passed!${NC}"

if [ "$BUILD_ONLY" = true ]; then
    echo ""
    echo -e "${BLUE}[2/6] Building all modules...${NC}"
    mvn install -N -q
    mvn -f common-lib/pom.xml install -q -DskipTests
    echo -e "  ${GREEN}common-lib built.${NC}"
    mvn clean package -DskipTests -q
    echo -e "  ${GREEN}All modules built successfully.${NC}"
    exit 0
fi

# ============================================================
# STEP 2: Build common-lib
# ============================================================
echo ""
echo -e "${BLUE}[2/6] Building common-lib...${NC}"
mvn install -N -q
mvn -f common-lib/pom.xml install -q -DskipTests
echo -e "  ${GREEN}common-lib built.${NC}"

# ============================================================
# STEP 3: Start Kafka Infrastructure
# ============================================================
echo ""
if [ "$NO_KAFKA" = false ]; then
    echo -e "${BLUE}[3/6] Starting Kafka infrastructure (Docker)...${NC}"
    docker compose -f docker-compose-infra.yml up -d

    # Wait for Kafka to be healthy
    echo -n "  Waiting for Kafka to be ready..."
    KAFKA_WAIT=0
    while [ $KAFKA_WAIT -lt 60 ]; do
        if docker compose -f docker-compose-infra.yml ps kafka 2>/dev/null | grep -q "healthy"; then
            echo -e " ${GREEN}READY${NC}"
            break
        fi
        sleep 2
        KAFKA_WAIT=$((KAFKA_WAIT + 2))
        echo -n "."
    done
    if [ $KAFKA_WAIT -ge 60 ]; then
        echo -e " ${YELLOW}TIMEOUT (continuing anyway)${NC}"
    fi
    echo -e "  ${GREEN}Kafka UI: http://localhost:8080${NC}"
else
    echo -e "${YELLOW}[3/6] Skipping Kafka (--no-kafka mode)${NC}"
    echo -e "  ${YELLOW}Kafka-dependent services (hotel, tour-guide, booking, itinerary, review) may fail to start${NC}"
fi

# ============================================================
# Helper Functions
# ============================================================
start_service() {
    local name=$1
    local port=$2
    local db_url_var=$3
    local db_user_var=$4
    local db_pass_var=$5

    echo -e "  ${BLUE}Starting ${name} (port ${port})...${NC}"

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
    echo -e "  ${GREEN}${name} started (PID: ${pid})${NC}"

    # Unset per-service vars
    unset DATABASE_URL DATABASE_USERNAME DATABASE_PASSWORD
}

wait_for_service() {
    local name=$1
    local port=$2
    local max_wait=${3:-120}
    local elapsed=0
    local pid=""

    # Find the PID for this service so we can detect early crashes
    if [ -f "$PID_FILE" ]; then
        pid=$(grep "^[0-9]*:${name}:" "$PID_FILE" 2>/dev/null | tail -1 | cut -d: -f1)
    fi

    echo -n "  Waiting for ${name} (port ${port})..."
    while [ $elapsed -lt $max_wait ]; do
        # Check if process already died
        if [ -n "$pid" ] && ! kill -0 "$pid" 2>/dev/null; then
            echo -e " ${RED}CRASHED${NC} (check logs/${name}.log)"
            FAILED_SERVICES+=("${name}:${port}")
            SERVICE_STATUS+=("${name}:${port}:CRASHED")
            return 1
        fi
        if curl -s --max-time 2 "http://localhost:${port}/actuator/health" > /dev/null 2>&1; then
            echo -e " ${GREEN}UP${NC}"
            SERVICE_STATUS+=("${name}:${port}:UP")
            return 0
        fi
        sleep 3
        elapsed=$((elapsed + 3))
        echo -n "."
    done
    echo -e " ${RED}TIMEOUT${NC} (check logs/${name}.log)"
    FAILED_SERVICES+=("${name}:${port}")
    SERVICE_STATUS+=("${name}:${port}:TIMEOUT")
    return 1
}

# Verify a service that should already be running (for final health table)
check_service_health() {
    local name=$1
    local port=$2
    local response
    response=$(curl -s --max-time 3 "http://localhost:${port}/actuator/health" 2>/dev/null)
    if echo "$response" | grep -q '"status":"UP"'; then
        echo "UP"
    elif [ -n "$response" ]; then
        echo "DEGRADED"
    else
        echo "DOWN"
    fi
}

# ============================================================
# STEP 4: Start Infrastructure Services (Eureka + Gateway)
# ============================================================
echo ""
echo -e "${BLUE}[4/6] Starting infrastructure services...${NC}"

start_service "discovery-server" 8761
wait_for_service "discovery-server" 8761 90 || true

start_service "api-gateway" 8060
wait_for_service "api-gateway" 8060 90 || true

# ============================================================
# STEP 5: Start Core Services (no Kafka dependency)
# ============================================================
echo ""
echo -e "${BLUE}[5/6] Starting core services...${NC}"

start_service "tourist-service"   8082 "TOURIST_DB_URL"    "TOURIST_DB_USERNAME"    "TOURIST_DB_PASSWORD"
start_service "vehicle-service"   8085 "VEHICLE_DB_URL"    "VEHICLE_DB_USERNAME"    "VEHICLE_DB_PASSWORD"
start_service "trip-plan-service" 8089 "TRIP_PLAN_DB_URL"  "TRIP_PLAN_DB_USERNAME"  "TRIP_PLAN_DB_PASSWORD"
start_service "event-service"     8090 "EVENT_DB_URL"      "EVENT_DB_USERNAME"      "EVENT_DB_PASSWORD"
start_service "ecommerce-service" 8091 "ECOMMERCE_DB_URL"  "ECOMMERCE_DB_USERNAME"  "ECOMMERCE_DB_PASSWORD"
start_service "ai-agent-service"  8093

echo ""
echo -e "${BLUE}  Waiting for core services to be ready...${NC}"
wait_for_service "tourist-service"   8082 150 || true
wait_for_service "vehicle-service"   8085 150 || true
wait_for_service "trip-plan-service" 8089 150 || true
wait_for_service "event-service"     8090 150 || true
wait_for_service "ecommerce-service" 8091 150 || true
wait_for_service "ai-agent-service"  8093 150 || true

# ============================================================
# STEP 6: Start Kafka-dependent Services
# ============================================================
echo ""
if [ "$NO_KAFKA" = false ]; then
    echo -e "${BLUE}[6/6] Starting Kafka-dependent services...${NC}"
    start_service "hotel-service"      8083 "HOTEL_DB_URL"      "HOTEL_DB_USERNAME"      "HOTEL_DB_PASSWORD"
    start_service "tour-guide-service" 8084 "TOUR_GUIDE_DB_URL" "TOUR_GUIDE_DB_USERNAME" "TOUR_GUIDE_DB_PASSWORD"
    start_service "booking-service"    8086 "BOOKING_DB_URL"    "BOOKING_DB_USERNAME"    "BOOKING_DB_PASSWORD"
    start_service "itinerary-service" 8087 "ITINERARY_DB_URL"  "ITINERARY_DB_USERNAME"  "ITINERARY_DB_PASSWORD"
    start_service "review-service"    8088 "REVIEW_DB_URL"     "REVIEW_DB_USERNAME"     "REVIEW_DB_PASSWORD"

    echo ""
    echo -e "${BLUE}  Waiting for Kafka-dependent services to be ready...${NC}"
    wait_for_service "hotel-service"      8083 180 || true
    wait_for_service "tour-guide-service" 8084 180 || true
    wait_for_service "booking-service"    8086 180 || true
    wait_for_service "itinerary-service"  8087 180 || true
    wait_for_service "review-service"     8088 180 || true
else
    echo -e "${YELLOW}[6/6] Skipped Kafka-dependent services${NC}"
fi

# ============================================================
# FINAL HEALTH CHECK TABLE
# ============================================================
echo ""
echo -e "${BOLD}${CYAN}============================================================${NC}"
echo -e "${BOLD}${CYAN}   TRAVEL PLAN PLATFORM - STARTUP COMPLETE${NC}"
echo -e "${BOLD}${CYAN}============================================================${NC}"
echo ""
echo -e "${BOLD}  Verifying all services via /actuator/health...${NC}"
echo ""

print_status() {
    local label=$1
    local port=$2
    local url=$3
    local status
    status=$(check_service_health "$label" "$port")
    local color
    case "$status" in
        UP)       color="$GREEN" ;;
        DEGRADED) color="$YELLOW" ;;
        *)        color="$RED" ;;
    esac
    printf "  %-22s ${color}%-8s${NC} %s\n" "$label" "[$status]" "$url"
}

echo -e "  ${BOLD}Infrastructure:${NC}"
print_status "Eureka Dashboard"  8761 "http://localhost:8761"
print_status "API Gateway"       8060 "http://localhost:8060"
if [ "$NO_KAFKA" = false ]; then
    # Kafka UI uses root path, not actuator
    if curl -s --max-time 3 "http://localhost:8080" > /dev/null 2>&1; then
        printf "  %-22s ${GREEN}%-8s${NC} %s\n" "Kafka UI" "[UP]" "http://localhost:8080"
    else
        printf "  %-22s ${RED}%-8s${NC} %s\n" "Kafka UI" "[DOWN]" "http://localhost:8080"
    fi
fi

echo ""
echo -e "  ${BOLD}Core Services:${NC}"
print_status "tourist-service"   8082 "http://localhost:8082"
print_status "vehicle-service"   8085 "http://localhost:8085"
print_status "trip-plan-service" 8089 "http://localhost:8089"
print_status "event-service"     8090 "http://localhost:8090"
print_status "ecommerce-service" 8091 "http://localhost:8091"
print_status "ai-agent-service"  8093 "http://localhost:8093"

if [ "$NO_KAFKA" = false ]; then
    echo ""
    echo -e "  ${BOLD}Kafka-dependent Services:${NC}"
    print_status "hotel-service"      8083 "http://localhost:8083"
    print_status "tour-guide-service" 8084 "http://localhost:8084"
    print_status "booking-service"    8086 "http://localhost:8086"
    print_status "itinerary-service" 8087 "http://localhost:8087"
    print_status "review-service"    8088 "http://localhost:8088"
fi

# ── Report failures ──
echo ""
if [ ${#FAILED_SERVICES[@]} -eq 0 ]; then
    echo -e "  ${BOLD}${GREEN}All services started successfully!${NC}"
else
    echo -e "  ${BOLD}${RED}${#FAILED_SERVICES[@]} service(s) failed to start:${NC}"
    for svc in "${FAILED_SERVICES[@]}"; do
        svc_name=$(echo "$svc" | cut -d: -f1)
        echo -e "    ${RED}✗${NC} $svc_name  → check ${LOG_DIR}/${svc_name}.log"
        # Show last 5 error lines from log
        if [ -f "${LOG_DIR}/${svc_name}.log" ]; then
            echo -e "      ${YELLOW}Last errors:${NC}"
            grep -i "error\|exception\|failed" "${LOG_DIR}/${svc_name}.log" 2>/dev/null | tail -3 | sed 's/^/        /'
        fi
    done
fi

echo ""
echo -e "  ${BOLD}Logs:${NC}   ${LOG_DIR}/"
echo -e "  ${BOLD}Health:${NC} Run ${CYAN}./health-check.sh${NC} in another terminal"
echo -e "  ${BOLD}Stop:${NC}   Press ${BOLD}Ctrl+C${NC} or run ${CYAN}./stop-all.sh${NC}"
echo ""
echo -e "Press Ctrl+C to stop all services."
wait
