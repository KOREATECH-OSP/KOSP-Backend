#!/bin/bash

# KOSP MSA Services Shutdown Script
# Usage: ./scripts/stop-all-services.sh

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}Stopping KOSP MSA services...${NC}"
echo ""

# Function to stop service by PID file
stop_service() {
    local service=$1
    local pidfile="logs/${service}.pid"
    
    if [ -f "$pidfile" ]; then
        local pid=$(cat "$pidfile")
        if kill -0 "$pid" 2>/dev/null; then
            echo -e "${YELLOW}Stopping $service (PID: $pid)...${NC}"
            kill "$pid"
            rm "$pidfile"
            echo -e "${GREEN}  $service stopped${NC}"
        else
            echo -e "${YELLOW}  $service (PID: $pid) not running${NC}"
            rm "$pidfile"
        fi
    else
        echo -e "${YELLOW}  No PID file for $service${NC}"
    fi
}

stop_service "api-service"
stop_service "challenge-service"
stop_service "notification-service"
stop_service "harvester"

echo ""
echo -e "${GREEN}All services stopped${NC}"

# Also kill any remaining Gradle daemons for these services
echo ""
echo -e "${YELLOW}Cleaning up Gradle daemon processes...${NC}"
pkill -f "api-service:bootRun" 2>/dev/null && echo "  Killed api-service daemon" || true
pkill -f "challenge-service:bootRun" 2>/dev/null && echo "  Killed challenge-service daemon" || true
pkill -f "notification-service:bootRun" 2>/dev/null && echo "  Killed notification-service daemon" || true
pkill -f "harvester:bootRun" 2>/dev/null && echo "  Killed harvester daemon" || true

echo ""
echo -e "${GREEN}Cleanup complete${NC}"
