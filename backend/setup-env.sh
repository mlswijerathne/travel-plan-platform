#!/bin/bash
# ============================================================
# Travel Plan Platform - Environment Setup Wizard
# ============================================================
# Interactive script to create/update the .env file
#
# Usage:
#   ./setup-env.sh          # Interactive setup
#   ./setup-env.sh --check  # Validate existing .env
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

ENV_FILE="$SCRIPT_DIR/.env"
EXAMPLE_FILE="$SCRIPT_DIR/.env.example"

# ── Check mode ──
if [ "$1" = "--check" ]; then
    echo -e "${BOLD}${CYAN}Validating .env file...${NC}"
    echo ""

    if [ ! -f "$ENV_FILE" ]; then
        echo -e "${RED}[FAIL] .env file not found!${NC}"
        echo "  Run: ./setup-env.sh"
        exit 1
    fi

    set -a
    source "$ENV_FILE"
    set +a

    ERRORS=0
    WARNINGS=0

    # Required vars
    REQUIRED_VARS=(
        "SUPABASE_JWT_SECRET:Supabase JWT Secret"
        "TOURIST_DB_URL:Tourist DB URL"
        "TOURIST_DB_USERNAME:Tourist DB Username"
        "TOURIST_DB_PASSWORD:Tourist DB Password"
        "HOTEL_DB_URL:Hotel DB URL"
        "HOTEL_DB_USERNAME:Hotel DB Username"
        "HOTEL_DB_PASSWORD:Hotel DB Password"
        "TOUR_GUIDE_DB_URL:Tour Guide DB URL"
        "TOUR_GUIDE_DB_USERNAME:Tour Guide DB Username"
        "TOUR_GUIDE_DB_PASSWORD:Tour Guide DB Password"
        "VEHICLE_DB_URL:Vehicle DB URL"
        "VEHICLE_DB_USERNAME:Vehicle DB Username"
        "VEHICLE_DB_PASSWORD:Vehicle DB Password"
        "BOOKING_DB_URL:Booking DB URL"
        "BOOKING_DB_USERNAME:Booking DB Username"
        "BOOKING_DB_PASSWORD:Booking DB Password"
        "ITINERARY_DB_URL:Itinerary DB URL"
        "ITINERARY_DB_USERNAME:Itinerary DB Username"
        "ITINERARY_DB_PASSWORD:Itinerary DB Password"
        "REVIEW_DB_URL:Review DB URL"
        "REVIEW_DB_USERNAME:Review DB Username"
        "REVIEW_DB_PASSWORD:Review DB Password"
        "TRIP_PLAN_DB_URL:Trip Plan DB URL"
        "TRIP_PLAN_DB_USERNAME:Trip Plan DB Username"
        "TRIP_PLAN_DB_PASSWORD:Trip Plan DB Password"
        "ECOMMERCE_DB_URL:Ecommerce DB URL"
        "ECOMMERCE_DB_USERNAME:Ecommerce DB Username"
        "ECOMMERCE_DB_PASSWORD:Ecommerce DB Password"
        "EVENT_DB_URL:Event DB URL"
        "EVENT_DB_USERNAME:Event DB Username"
        "EVENT_DB_PASSWORD:Event DB Password"
    )

    OPTIONAL_VARS=(
        "GEMINI_API_KEY:Gemini API Key (for AI Agent)"
        "CORS_ALLOWED_ORIGINS:CORS Origins"
    )

    for entry in "${REQUIRED_VARS[@]}"; do
        IFS=: read -r var desc <<< "$entry"
        val="${!var}"
        if [ -z "$val" ] || [ "$val" = "your-password" ] || [ "$val" = "your-jwt-secret" ]; then
            echo -e "  ${RED}[MISSING]${NC} $desc ($var)"
            ERRORS=$((ERRORS + 1))
        else
            echo -e "  ${GREEN}[OK]${NC}      $desc"
        fi
    done

    echo ""
    for entry in "${OPTIONAL_VARS[@]}"; do
        IFS=: read -r var desc <<< "$entry"
        val="${!var}"
        if [ -z "$val" ] || echo "$val" | grep -q "your-"; then
            echo -e "  ${YELLOW}[WARN]${NC}    $desc ($var) - not set"
            WARNINGS=$((WARNINGS + 1))
        else
            echo -e "  ${GREEN}[OK]${NC}      $desc"
        fi
    done

    echo ""
    if [ $ERRORS -gt 0 ]; then
        echo -e "${RED}$ERRORS required variable(s) missing. Run ./setup-env.sh to fix.${NC}"
        exit 1
    else
        echo -e "${GREEN}All required variables configured!${NC}"
        if [ $WARNINGS -gt 0 ]; then
            echo -e "${YELLOW}$WARNINGS optional variable(s) not set.${NC}"
        fi
    fi
    exit 0
fi

# ============================================================
# Interactive Setup
# ============================================================

echo -e "${BOLD}${CYAN}============================================${NC}"
echo -e "${BOLD}${CYAN} Travel Plan Platform - Environment Setup${NC}"
echo -e "${BOLD}${CYAN}============================================${NC}"
echo ""

# Load existing values if .env exists
if [ -f "$ENV_FILE" ]; then
    echo -e "${GREEN}Existing .env found. Current values will be shown as defaults.${NC}"
    echo -e "${YELLOW}Press Enter to keep existing value, or type a new one.${NC}"
    set -a
    source "$ENV_FILE"
    set +a
    EXISTING=true
else
    echo -e "${YELLOW}No .env file found. Creating a new one.${NC}"
    EXISTING=false
fi
echo ""

# Helper: prompt with default
prompt_value() {
    local var_name=$1
    local description=$2
    local default_val=$3
    local current_val="${!var_name}"
    local use_default="${current_val:-$default_val}"

    if [ -n "$use_default" ] && ! echo "$use_default" | grep -q "your-"; then
        # Mask passwords
        if echo "$var_name" | grep -qi "password\|secret\|key"; then
            local masked="${use_default:0:4}****"
            echo -ne "  ${description} [${masked}]: "
        else
            echo -ne "  ${description} [${use_default}]: "
        fi
        read -r input
        if [ -z "$input" ]; then
            eval "$var_name=\"$use_default\""
        else
            eval "$var_name=\"$input\""
        fi
    else
        echo -ne "  ${description}: "
        read -r input
        if [ -n "$input" ]; then
            eval "$var_name=\"$input\""
        fi
    fi
}

# ── Supabase ──
echo -e "${BOLD}1. Supabase Configuration${NC}"
prompt_value "SUPABASE_URL" "Supabase URL" "https://your-project.supabase.co"
prompt_value "SUPABASE_JWT_SECRET" "JWT Secret" ""
echo ""

# ── General ──
echo -e "${BOLD}2. General Configuration${NC}"
prompt_value "CORS_ALLOWED_ORIGINS" "CORS Origins" "http://localhost:3000"
prompt_value "EUREKA_URL" "Eureka URL" "http://localhost:8761/eureka/"
prompt_value "KAFKA_BOOTSTRAP_SERVERS" "Kafka Bootstrap" "localhost:29092"
echo ""

# ── Database credentials ──
echo -e "${BOLD}3. Database Credentials${NC}"
echo -e "  ${YELLOW}Each service uses a separate Neon PostgreSQL database.${NC}"
echo -e "  ${YELLOW}If all services share the same DB credentials, enter them once:${NC}"
echo ""
echo -ne "  Use same credentials for all services? (y/n) [n]: "
read -r SAME_DB
echo ""

DB_SERVICES=("TOURIST" "HOTEL" "TOUR_GUIDE" "VEHICLE" "BOOKING" "ITINERARY" "REVIEW" "TRIP_PLAN" "ECOMMERCE" "EVENT")
DB_NAMES=("Tourist" "Hotel" "Tour Guide" "Vehicle" "Booking" "Itinerary" "Review" "Trip Plan" "Ecommerce" "Event")

if [ "$SAME_DB" = "y" ] || [ "$SAME_DB" = "Y" ]; then
    echo -e "  ${BLUE}Shared Database Credentials:${NC}"
    prompt_value "SHARED_DB_URL" "Database URL" "jdbc:postgresql://your-neon-host/neondb?sslmode=require"
    prompt_value "SHARED_DB_USERNAME" "Username" "neondb_owner"
    prompt_value "SHARED_DB_PASSWORD" "Password" ""
    echo ""

    for svc in "${DB_SERVICES[@]}"; do
        eval "${svc}_DB_URL=\"$SHARED_DB_URL\""
        eval "${svc}_DB_USERNAME=\"$SHARED_DB_USERNAME\""
        eval "${svc}_DB_PASSWORD=\"$SHARED_DB_PASSWORD\""
    done
else
    for i in "${!DB_SERVICES[@]}"; do
        svc=${DB_SERVICES[$i]}
        name=${DB_NAMES[$i]}
        echo -e "  ${BLUE}${name} Service:${NC}"
        prompt_value "${svc}_DB_URL" "  DB URL" "jdbc:postgresql://your-neon-host/neondb?sslmode=require"
        prompt_value "${svc}_DB_USERNAME" "  Username" "neondb_owner"
        prompt_value "${svc}_DB_PASSWORD" "  Password" ""
        echo ""
    done
fi

# ── AI Agent ──
echo -e "${BOLD}4. AI Agent Service (optional)${NC}"
prompt_value "GEMINI_API_KEY" "Gemini API Key" ""
prompt_value "OPENAI_API_KEY" "OpenAI API Key (optional)" ""
prompt_value "GOOGLE_MAPS_API_KEY" "Google Maps API Key (optional)" ""
echo ""

# ── Write .env file ──
echo -e "${BLUE}Writing .env file...${NC}"

cat > "$ENV_FILE" << ENVEOF
# ============================================================
# Travel Plan Platform - Environment Configuration
# Generated by setup-env.sh on $(date '+%Y-%m-%d %H:%M:%S')
# ============================================================

# Supabase Configuration
SUPABASE_URL=${SUPABASE_URL:-https://your-project.supabase.co}
SUPABASE_JWT_SECRET=${SUPABASE_JWT_SECRET:-your-jwt-secret}

# CORS Configuration
CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS:-http://localhost:3000}

# Eureka Configuration
EUREKA_URL=${EUREKA_URL:-http://localhost:8761/eureka/}

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=${KAFKA_BOOTSTRAP_SERVERS:-localhost:29092}

# Database Credentials (per service)
# Tourist Service
TOURIST_DB_URL=${TOURIST_DB_URL}
TOURIST_DB_USERNAME=${TOURIST_DB_USERNAME}
TOURIST_DB_PASSWORD=${TOURIST_DB_PASSWORD}

# Hotel Service
HOTEL_DB_URL=${HOTEL_DB_URL}
HOTEL_DB_USERNAME=${HOTEL_DB_USERNAME}
HOTEL_DB_PASSWORD=${HOTEL_DB_PASSWORD}

# Tour Guide Service
TOUR_GUIDE_DB_URL=${TOUR_GUIDE_DB_URL}
TOUR_GUIDE_DB_USERNAME=${TOUR_GUIDE_DB_USERNAME}
TOUR_GUIDE_DB_PASSWORD=${TOUR_GUIDE_DB_PASSWORD}

# Vehicle Service
VEHICLE_DB_URL=${VEHICLE_DB_URL}
VEHICLE_DB_USERNAME=${VEHICLE_DB_USERNAME}
VEHICLE_DB_PASSWORD=${VEHICLE_DB_PASSWORD}

# Booking Service
BOOKING_DB_URL=${BOOKING_DB_URL}
BOOKING_DB_USERNAME=${BOOKING_DB_USERNAME}
BOOKING_DB_PASSWORD=${BOOKING_DB_PASSWORD}

# Itinerary Service
ITINERARY_DB_URL=${ITINERARY_DB_URL}
ITINERARY_DB_USERNAME=${ITINERARY_DB_USERNAME}
ITINERARY_DB_PASSWORD=${ITINERARY_DB_PASSWORD}

# Review Service
REVIEW_DB_URL=${REVIEW_DB_URL}
REVIEW_DB_USERNAME=${REVIEW_DB_USERNAME}
REVIEW_DB_PASSWORD=${REVIEW_DB_PASSWORD}

# Trip Plan Service
TRIP_PLAN_DB_URL=${TRIP_PLAN_DB_URL}
TRIP_PLAN_DB_USERNAME=${TRIP_PLAN_DB_USERNAME}
TRIP_PLAN_DB_PASSWORD=${TRIP_PLAN_DB_PASSWORD}

# Ecommerce Service
ECOMMERCE_DB_URL=${ECOMMERCE_DB_URL}
ECOMMERCE_DB_USERNAME=${ECOMMERCE_DB_USERNAME}
ECOMMERCE_DB_PASSWORD=${ECOMMERCE_DB_PASSWORD}

# Event Service
EVENT_DB_URL=${EVENT_DB_URL}
EVENT_DB_USERNAME=${EVENT_DB_USERNAME}
EVENT_DB_PASSWORD=${EVENT_DB_PASSWORD}

# AI Agent Service
GEMINI_API_KEY=${GEMINI_API_KEY:-your-gemini-api-key}
OPENAI_API_KEY=${OPENAI_API_KEY:-your-openai-api-key}
GOOGLE_MAPS_API_KEY=${GOOGLE_MAPS_API_KEY:-your-google-maps-api-key}
ENVEOF

echo -e "${GREEN}.env file written to: ${ENV_FILE}${NC}"
echo ""

# Validate
echo -e "${BLUE}Validating...${NC}"
bash "$SCRIPT_DIR/setup-env.sh" --check
