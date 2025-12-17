#!/bin/bash

# ==============================================================================
# VEKKU DASHBOARD CONTROL CENTER
# ==============================================================================
# This script manages the Vekku stack (Docker, Brain, Server, Client) in a single
# terminal window, running services in the background and redirecting logs to files.
# It provides an interactive menu to restart specific components.

# --- Configuration ---
# --- Configuration ---
# Resolve script directory and project root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

LOG_DIR="$PROJECT_ROOT/logs"
PID_DIR="$PROJECT_ROOT/pids"
BRAIN_DIR="$PROJECT_ROOT/vekku-brain-service"
SERVER_DIR="$PROJECT_ROOT/vekku-server"
CLIENT_DIR="$PROJECT_ROOT/vekku-client"

# --- Colors ---
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# --- Initialization ---
mkdir -p "$LOG_DIR"
mkdir -p "$PID_DIR"

cleanup() {
    echo -e "\n${RED}🛑 Shutting down all services...${NC}"
    kill_process "brain"
    kill_process "server"
    kill_process "client"
    # Docker stays up unless explicitly stopped
    echo -e "${GREEN}✅ Shutdown complete. Bye!${NC}"
    exit 0
}

trap cleanup SIGINT SIGTERM

# --- Helper Functions ---

get_pid() {
    local name="$1"
    if [ -f "$PID_DIR/$name.pid" ]; then
        cat "$PID_DIR/$name.pid"
    fi
}

is_running() {
    local name="$1"
    local pid=$(get_pid "$name")
    if [ -n "$pid" ] && ps -p "$pid" > /dev/null; then
        return 0 # True
    else
        return 1 # False
    fi
}

kill_process() {
    local name="$1"
    local pid=$(get_pid "$name")
    if [ -n "$pid" ]; then
        if ps -p "$pid" > /dev/null; then
            echo -e "${YELLOW}Killing $name (PID: $pid)...${NC}"
            kill "$pid" > /dev/null 2>&1
            wait "$pid" 2>/dev/null
        fi
        rm -f "$PID_DIR/$name.pid"
    fi
}

# --- Service Launchers ---

start_docker() {
    echo -e "${BLUE}🐳 Checking Docker...${NC}"
    docker compose -f "$SERVER_DIR/docker-compose.yaml" up -d
    
    # Wait loop
    # Using /readyz endpoint for Qdrant and forcing GET (no --head) to ensure correct status
    echo -n "   Waiting for Qdrant..."
    until curl --output /dev/null --silent --fail "http://localhost:6333/readyz"; do
        echo -n "."
        sleep 1
    done
    echo -e " ${GREEN}Qdrant Ready.${NC}"
}

start_brain() {
    if is_running "brain"; then
        echo -e "${YELLOW}Brain Service already running.${NC}"
        return
    fi
    echo -e "${CYAN}🧠 Starting Brain Service...${NC}"
    (cd "$BRAIN_DIR" && npm run dev > "../$LOG_DIR/brain.log" 2>&1) &
    echo $! > "$PID_DIR/brain.pid"
}

start_server() {
    if is_running "server"; then
        echo -e "${YELLOW}Server already running.${NC}"
        return
    fi
    echo -e "${CYAN}☕ Starting Main Server...${NC}"
    (cd "$SERVER_DIR" && ./mvnw spring-boot:run > "../$LOG_DIR/server.log" 2>&1) &
    echo $! > "$PID_DIR/server.pid"
}

start_client() {
    if is_running "client"; then
        echo -e "${YELLOW}Client already running.${NC}"
        return
    fi
    echo -e "${CYAN}⚛️ Starting Client...${NC}"
    (cd "$CLIENT_DIR" && npm run dev > "../$LOG_DIR/client.log" 2>&1) &
    echo $! > "$PID_DIR/client.pid"
}

restart_service() {
    local name="$1"
    kill_process "$name"
    sleep 1
    case $name in
        "brain") start_brain ;;
        "server") start_server ;;
        "client") start_client ;;
    esac
    echo -e "${GREEN}✅ Restarted $name.${NC}"
}

monitor_logs() {
    clear
    echo -e "${BLUE}--- Live Logs (Ctrl+C to return) ---${NC}"
    # Check if logs exist
    touch "$LOG_DIR/brain.log" "$LOG_DIR/server.log" "$LOG_DIR/client.log"
    
    # Tail all logs
    # Using a subshell trap to handle Ctrl+C gracefully and return to menu
    (trap 'exit 0' INT; tail -f "$LOG_DIR"/*.log)
}

# --- Main Dashboard Loop ---

show_status() {
    clear
    echo -e "${BLUE}=======================================${NC}"
    echo -e "${BLUE}   VEKKU CONTROL CENTER                ${NC}"
    echo -e "${BLUE}=======================================${NC}"
    
    printf "%-10s %-10s %-20s\n" "SERVICE" "STATUS" "LOG FILE"
    echo "---------------------------------------"
    
    for svc in "brain" "server" "client"; do
        if is_running "$svc"; then
            status="${GREEN}RUNNING${NC}"
        else
            status="${RED}STOPPED${NC}"
        fi
        printf "%-10s %-15b %-20s\n" "$svc" "$status" "$LOG_DIR/$svc.log"
    done
    
    echo "---------------------------------------"
    echo "---------------------------------------"
    echo -e "Press key to restart:"
    echo -e " [${YELLOW}b${NC}] Brain  [${YELLOW}s${NC}] Server  [${YELLOW}c${NC}] Client"
    echo -e " [${CYAN}l${NC}] Live Logs (All)"
    echo -e " [${RED}q${NC}] Quit all"
    echo "---------------------------------------"
}

# Initial Start
start_docker
start_brain
start_server
start_client

while true; do
    show_status
    read -rsn1 input
    case $input in
        b) restart_service "brain" ;;
        s) restart_service "server" ;;
        c) restart_service "client" ;;
        l) monitor_logs ;;
        q) cleanup ;;
    esac
done
