#!/bin/bash

# Configuration
BACKEND_URL="https://github.com/kuchuk-borom-debbarma/Vekku-Bun.git"
WEB_URL="https://github.com/kuchuk-borom-debbarma/Vekku-Reactjs.git"

echo "------------------------------------------"
echo "🚀 Updating Vekku Subtrees to Latest"
echo "------------------------------------------"

# Update Backend
echo "📡 Pulling latest from Backend ($BACKEND_URL)..."
git subtree pull --prefix=backend "$BACKEND_URL" main -m "chore: sync backend subtree with latest changes"

# Update Web
echo ""
echo "📡 Pulling latest from Web ($WEB_URL)..."
git subtree pull --prefix=web "$WEB_URL" main -m "chore: sync web subtree with latest changes"

echo ""
echo "📤 Pushing synchronized history to Vekku Meta..."
git push origin main

echo ""
echo "✅ Successfully synchronized all repositories."
echo "------------------------------------------"
