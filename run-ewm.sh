#!/bin/bash

# ============================================
# Script: run-ewm.sh
# Description: Step-by-step launching with separate logs
# ============================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Create logs directory
LOGS_DIR="$SCRIPT_DIR/logs"
mkdir -p "$LOGS_DIR"

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to wait for service
wait_for_service() {
    local url=$1
    local name=$2
    local max_attempts=${3:-20}
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        echo -e "${YELLOW}Waiting for $name... (attempt $attempt of $max_attempts)${NC}"

        if curl -s "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}[OK] $name is UP${NC}"
            return 0
        fi

        sleep 3
        attempt=$((attempt + 1))
    done

    echo -e "${RED}[ERROR] $name failed to start after $max_attempts attempts${NC}"
    return 1
}

# Function to check if Docker is running
check_docker() {
    echo "Checking Docker status..."

    if ! command -v docker &> /dev/null; then
        echo -e "${RED}[ERROR] Docker is not installed${NC}"
        return 1
    fi

    if ! docker info &> /dev/null; then
        echo -e "${RED}[ERROR] Docker is not running. Please start Docker first.${NC}"
        echo -e "${YELLOW}Start Docker and try again.${NC}"
        return 1
    fi

    echo -e "${GREEN}[OK] Docker is running${NC}"
    return 0
}

# Function to start Docker Compose services
start_docker_services() {
    echo "Starting Docker Compose services..."
    cd "$SCRIPT_DIR"

    if [ ! -f "docker-compose.yml" ]; then
        echo -e "${YELLOW}[WARN] docker-compose.yml not found. Skipping Docker services.${NC}"
        return 0
    fi

    docker-compose up -d
    echo -e "${GREEN}[OK] Docker Compose services started${NC}"

    # Wait for databases to be ready
    echo "Waiting for databases to be ready..."
    sleep 10
}

# Function to stop Docker Compose services (for cleanup)
stop_docker_services() {
    cd "$SCRIPT_DIR"
    if [ -f "docker-compose.yml" ]; then
        docker-compose down
        echo "Docker Compose services stopped"
    fi
}

# ============================================
# MAIN SCRIPT
# ============================================

echo "============================================"
echo "Starting Microservices in sequence..."
echo "============================================"
echo

# Check Docker
if ! check_docker; then
    exit 1
fi
echo

# Start Docker services (PostgreSQL for all services)
start_docker_services
echo

# ============================================
# Step 1: Discovery Server (Eureka)
# ============================================
echo "[1/4] Starting Discovery Server..."
cd "$SCRIPT_DIR/infra/discovery-server"
nohup java -jar target/discovery-server-0.0.1-SNAPSHOT.jar > "$LOGS_DIR/discovery-server.log" 2>&1 &
echo "Discovery Server PID: $!"
echo "Log: $LOGS_DIR/discovery-server.log"

wait_for_service "http://localhost:8761/actuator/health" "Discovery Server" 20
if [ $? -ne 0 ]; then exit 1; fi
echo

# ============================================
# Step 2: Config Server
# ============================================
echo "[2/4] Starting Config Server..."
cd "$SCRIPT_DIR/infra/config-server"
nohup java -jar target/config-server-0.0.1-SNAPSHOT.jar > "$LOGS_DIR/config-server.log" 2>&1 &
echo "Config Server PID: $!"
echo "Log: $LOGS_DIR/config-server.log"

wait_for_service "http://localhost:8888/actuator/health" "Config Server" 20
if [ $? -ne 0 ]; then exit 1; fi
echo

# ============================================
# Step 3: Gateway Server
# ============================================
echo "[3/4] Starting Gateway Server..."
cd "$SCRIPT_DIR/infra/gateway-server"
nohup java -jar target/gateway-server-0.0.1-SNAPSHOT.jar > "$LOGS_DIR/gateway-server.log" 2>&1 &
echo "Gateway Server PID: $!"
echo "Log: $LOGS_DIR/gateway-server.log"

wait_for_service "http://localhost:8080/actuator/health" "Gateway Server" 20
if [ $? -ne 0 ]; then exit 1; fi
echo

# ============================================
# Summary
# ============================================
echo "============================================"
echo -e "${GREEN}ALL SERVICES ARE RUNNING${NC}"
echo "============================================"
echo "Docker Services (PostgreSQL):"
echo "  - event-service-db:   localhost:5435"
echo "  - request-service-db: localhost:5436"
echo "  - user-service-db:    localhost:5434"
echo "  - stats-server-db:    localhost:5432"
echo ""
echo "Java Services:"
echo "  Discovery Server: http://localhost:8761"
echo "  Config Server:    http://localhost:8888"
echo "  Gateway Server:   http://localhost:8080"
echo ""
echo "Logs directory: $LOGS_DIR"
echo "  - $LOGS_DIR/discovery-server.log"
echo "  - $LOGS_DIR/config-server.log"
echo "  - $LOGS_DIR/gateway-server.log"
echo ""
echo "View logs in real-time:"
echo "  tail -f $LOGS_DIR/discovery-server.log"
echo "  tail -f $LOGS_DIR/config-server.log"
echo "  tail -f $LOGS_DIR/gateway-server.log"
echo ""
echo "Docker logs: docker-compose logs -f"
echo ""
echo "To stop all:"
echo "  pkill -f 'discovery-server|config-server|gateway-server'"
echo "  docker-compose down"
echo "============================================"