#!/bin/bash
# ============================================================
# Travel Plan Platform - Log Viewer
# ============================================================
# View service logs in real-time
#
# Usage:
#   ./view-logs.sh                    # List available logs
#   ./view-logs.sh tourist-service    # Tail a specific service log
#   ./view-logs.sh all                # Tail all logs combined
#   ./view-logs.sh errors             # Show only ERROR lines from all logs
#   ./view-logs.sh clean              # Delete all log files
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$SCRIPT_DIR/logs"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

if [ ! -d "$LOG_DIR" ]; then
    echo -e "${YELLOW}No logs directory found. Start services first.${NC}"
    exit 1
fi

SERVICE="$1"

case "$SERVICE" in
    ""|"list")
        echo -e "${BOLD}${CYAN}Available Logs:${NC}"
        echo ""
        for logfile in "$LOG_DIR"/*.log; do
            if [ -f "$logfile" ]; then
                name=$(basename "$logfile" .log)
                size=$(du -h "$logfile" 2>/dev/null | awk '{print $1}')
                lines=$(wc -l < "$logfile" 2>/dev/null)
                # Check for errors
                errors=$(grep -c "ERROR" "$logfile" 2>/dev/null || echo "0")
                if [ "$errors" -gt 0 ]; then
                    echo -e "  ${name}  (${size}, ${lines} lines, ${RED}${errors} errors${NC})"
                else
                    echo -e "  ${name}  (${size}, ${lines} lines, ${GREEN}0 errors${NC})"
                fi
            fi
        done
        echo ""
        echo -e "Usage: ${CYAN}./view-logs.sh <service-name>${NC}"
        echo -e "       ${CYAN}./view-logs.sh all${NC}       - tail all logs"
        echo -e "       ${CYAN}./view-logs.sh errors${NC}    - show errors only"
        echo -e "       ${CYAN}./view-logs.sh clean${NC}     - delete all logs"
        ;;

    "all")
        echo -e "${BLUE}Tailing all service logs (Ctrl+C to stop)...${NC}"
        echo ""
        tail -f "$LOG_DIR"/*.log 2>/dev/null
        ;;

    "errors")
        echo -e "${BOLD}${RED}Errors across all services:${NC}"
        echo ""
        for logfile in "$LOG_DIR"/*.log; do
            if [ -f "$logfile" ]; then
                name=$(basename "$logfile" .log)
                errors=$(grep "ERROR" "$logfile" 2>/dev/null)
                if [ -n "$errors" ]; then
                    echo -e "${BOLD}${YELLOW}── $name ──${NC}"
                    echo "$errors" | tail -20
                    echo ""
                fi
            fi
        done
        ;;

    "clean")
        echo -ne "Delete all log files? (y/n): "
        read -r confirm
        if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
            rm -f "$LOG_DIR"/*.log
            echo -e "${GREEN}All logs deleted.${NC}"
        else
            echo "Cancelled."
        fi
        ;;

    *)
        LOGFILE="$LOG_DIR/${SERVICE}.log"
        if [ -f "$LOGFILE" ]; then
            echo -e "${BLUE}Tailing ${SERVICE} log (Ctrl+C to stop)...${NC}"
            echo -e "${YELLOW}Last 50 lines + live tail:${NC}"
            echo ""
            tail -50f "$LOGFILE"
        else
            echo -e "${RED}Log file not found: ${LOGFILE}${NC}"
            echo ""
            echo "Available services:"
            for logfile in "$LOG_DIR"/*.log; do
                [ -f "$logfile" ] && echo "  $(basename "$logfile" .log)"
            done
        fi
        ;;
esac
