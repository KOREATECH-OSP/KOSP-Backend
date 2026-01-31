#!/bin/bash

# KOSP MSA Services Startup Script
# Usage: ./scripts/start-all-services.sh
# This script starts all 4 microservices in the background

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}KOSP MSA Services Startup${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Check if .env.local exists
if [ ! -f .env.local ]; then
    echo -e "${RED}ERROR: .env.local not found${NC}"
    echo "Please create .env.local with required environment variables"
    exit 1
fi

# Load environment variables
echo -e "${YELLOW}Loading environment variables from .env.local...${NC}"
export $(cat .env.local | grep -v '^#' | xargs)

# Create logs directory
mkdir -p logs

# Check if ports are already in use
check_port() {
    local port=$1
    local service=$2
    if lsof -ti:$port > /dev/null 2>&1; then
        echo -e "${RED}ERROR: Port $port is already in use (needed for $service)${NC}"
        echo "Please stop the process using port $port first"
        return 1
    fi
    return 0
}

echo -e "${YELLOW}Checking ports...${NC}"
check_port 8080 "api-service" || exit 1
check_port 8081 "notification-service" || exit 1

# Start services in background
echo ""
echo -e "${GREEN}Starting services...${NC}"

echo -e "${YELLOW}1/4 Starting api-service (port 8080)...${NC}"
nohup ./gradlew :api-service:bootRun > logs/api-service.log 2>&1 &
API_PID=$!
echo "  PID: $API_PID"

echo -e "${YELLOW}2/4 Starting challenge-service (background worker)...${NC}"
nohup ./gradlew :challenge-service:bootRun > logs/challenge-service.log 2>&1 &
CHALLENGE_PID=$!
echo "  PID: $CHALLENGE_PID"

echo -e "${YELLOW}3/4 Starting notification-service (port 8081)...${NC}"
nohup ./gradlew :notification-service:bootRun > logs/notification-service.log 2>&1 &
NOTIFICATION_PID=$!
echo "  PID: $NOTIFICATION_PID"

echo -e "${YELLOW}4/4 Starting harvester (Spring Batch)...${NC}"
nohup ./gradlew :harvester:bootRun > logs/harvester.log 2>&1 &
HARVESTER_PID=$!
echo "  PID: $HARVESTER_PID"

# Save PIDs to file
echo "$API_PID" > logs/api-service.pid
echo "$CHALLENGE_PID" > logs/challenge-service.pid
echo "$NOTIFICATION_PID" > logs/notification-service.pid
echo "$HARVESTER_PID" > logs/harvester.pid

echo ""
echo -e "${GREEN}All services started!${NC}"
echo ""
echo "Process IDs saved to logs/*.pid"
echo "Logs available in logs/*.log"
echo ""
echo -e "${YELLOW}Waiting for services to start (this may take 30-60 seconds)...${NC}"
echo "You can monitor startup progress:"
echo "  tail -f logs/api-service.log"
echo "  tail -f logs/notification-service.log"
echo ""
echo "To check if services are ready:"
echo "  curl http://localhost:8080/actuator/health  # api-service"
echo "  curl http://localhost:8081/actuator/health  # notification-service"
echo ""
echo "To stop all services:"
echo "  ./scripts/stop-all-services.sh"
echo ""
echo -e "${GREEN}========================================${NC}"
