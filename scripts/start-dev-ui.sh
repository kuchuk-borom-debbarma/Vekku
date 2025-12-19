#!/bin/bash
# Starts the Vekku Dev Client (and Docker if needed)

# Resolve script directory and project root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "🚀 Starting Vekku Dev Client..."

# 1. Start Docker (Prerequisite)
echo "🐳 Checking Docker..."
docker compose -f "$PROJECT_ROOT/docker-compose.yaml" up -d

# 2. Start Dev Tool
echo "💻 Launching Dev Dashboard..."
cd "$PROJECT_ROOT/vekku-dev-client"
npm run dev
