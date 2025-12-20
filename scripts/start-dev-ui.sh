#!/bin/bash
# Starts the Vekku Dev Client (and Docker if needed)

# Resolve script directory and project root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "🚀 Starting Vekku Dev Client..."

# Helper to kill port
kill_port() {
  PORT=$1
  PID=$(lsof -ti:$PORT)
  if [ -n "$PID" ]; then
    echo "Killing process on port $PORT (PID: $PID)..."
    kill -9 $PID
  fi
}

# Kill conflicting ports
echo "🧹 Cleaning up ports (3000, 3001, 3002, 5173, 8080)..."
kill_port 3000
kill_port 3001
kill_port 3002
kill_port 5173
kill_port 8080

# 1. Start Docker (Prerequisite)
echo "🐳 Checking Docker..."
docker compose -f "$PROJECT_ROOT/docker-compose.yaml" up -d

# 2. Start Dev Tool
echo "💻 Launching Dev Dashboard..."
cd "$PROJECT_ROOT/vekku-dev-client"
npm run dev
