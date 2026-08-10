#!/bin/bash

# Canvas App - Start Script (Linux / macOS)
# Mirrors start.bat - starts backend then frontend, tails logs, cleans up on Ctrl+C

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

BACKEND_PORT=8080
FRONTEND_PORT=5174

# -- Colours -------------------------------------------------------
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# -- Cleanup: kill both processes on Ctrl+C / script exit ----------
cleanup() {
    status=$?
    echo -e "\n${YELLOW}Shutting down Canvas App...${NC}"
    [ -n "$BACKEND_PID" ]  && kill "$BACKEND_PID"  2>/dev/null || true
    [ -n "$FRONTEND_PID" ] && kill "$FRONTEND_PID" 2>/dev/null || true
    exit "$status"
}
trap cleanup EXIT INT TERM

# -- Banner --------------------------------------------------------
echo ""
echo -e "${BLUE}=====================================================================${NC}"
echo -e "${BLUE}           Canvas Drawing Application - Startup                     ${NC}"
echo -e "${BLUE}=====================================================================${NC}"
echo ""

# ------------------------------------------------------------------
# BACKEND
# ------------------------------------------------------------------
echo -e "${BLUE}=====================================================================${NC}"
echo -e "${YELLOW}Starting Backend (Spring Boot on port ${BACKEND_PORT})...${NC}"
echo -e "${BLUE}=====================================================================${NC}"

cd "$SCRIPT_DIR/canvas-backend"

if [ ! -f "gradlew" ]; then
    echo -e "${RED}[ERROR] gradlew not found in canvas-backend${NC}"
    exit 1
fi

chmod +x gradlew

echo -e "${YELLOW}[INFO] Running: ./gradlew clean build -x test (this may take a while)...${NC}"
if ! ./gradlew clean build -x test > "$SCRIPT_DIR/backend-build.log" 2>&1; then
    echo -e "${RED}[ERROR] Backend build failed. Check $SCRIPT_DIR/backend-build.log${NC}"
    exit 1
fi
echo -e "${GREEN}[OK] Backend build succeeded.${NC}"

# Start bootRun in background, log to file
./gradlew bootRun > "$SCRIPT_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
echo -e "${GREEN}[OK] Backend started (PID: $BACKEND_PID)${NC}"
echo -e "     Logs: $SCRIPT_DIR/backend.log"
echo ""

# Wait for backend to be ready (health check - up to 60 seconds)
echo -e "${YELLOW}[INFO] Waiting for backend to be fully ready...${NC}"
BACKEND_READY=0
for i in $(seq 1 60); do
    if ! kill -0 "$BACKEND_PID" 2>/dev/null; then
        echo -e "\n${RED}[ERROR] Backend process exited early. Check $SCRIPT_DIR/backend.log${NC}"
        exit 1
    fi
    if curl -fsS "http://localhost:${BACKEND_PORT}/actuator/health" > /dev/null 2>&1; then
        echo -e "\n${GREEN}[OK] Backend is fully ready!${NC}"
        BACKEND_READY=1
        break
    fi
    echo -n "."
    sleep 1
done

if [ $BACKEND_READY -eq 0 ]; then
    echo -e "\n${YELLOW}[WARNING] Backend may still be starting. Continuing with frontend startup...${NC}"
fi
echo ""

# ------------------------------------------------------------------
# FRONTEND
# ------------------------------------------------------------------
echo -e "${BLUE}=====================================================================${NC}"
echo -e "${YELLOW}Starting Frontend (React / Vite on port ${FRONTEND_PORT})...${NC}"
echo -e "${BLUE}=====================================================================${NC}"

cd "$SCRIPT_DIR/canvas-frontend"

# Install dependencies only when node_modules is absent
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}[INFO] Installing npm dependencies (this may take a minute)...${NC}"
    if ! npm install > "$SCRIPT_DIR/frontend-install.log" 2>&1; then
        echo -e "${RED}[ERROR] Frontend dependency install failed. Check $SCRIPT_DIR/frontend-install.log${NC}"
        exit 1
    fi
    echo -e "${GREEN}[OK] Dependencies installed.${NC}"
else
    echo -e "${GREEN}[OK] Dependencies already installed.${NC}"
fi
echo ""

# Start Vite dev server in background
npm start > "$SCRIPT_DIR/frontend.log" 2>&1 &
FRONTEND_PID=$!
echo -e "${GREEN}[OK] Frontend started (PID: $FRONTEND_PID)${NC}"
echo -e "     Logs: $SCRIPT_DIR/frontend.log"
echo ""

# Wait for frontend to be ready (up to 60 seconds)
echo -e "${YELLOW}[INFO] Waiting for frontend to start...${NC}"
FRONTEND_READY=0
for i in $(seq 1 60); do
    if ! kill -0 "$FRONTEND_PID" 2>/dev/null; then
        echo -e "\n${RED}[ERROR] Frontend process exited early. Check $SCRIPT_DIR/frontend.log${NC}"
        exit 1
    fi
    if curl -fsS "http://localhost:${FRONTEND_PORT}" > /dev/null 2>&1; then
        echo -e "\n${GREEN}[OK] Frontend is ready!${NC}"
        FRONTEND_READY=1
        break
    fi
    echo -n "."
    sleep 1
done

if [ $FRONTEND_READY -eq 0 ]; then
    echo -e "\n${YELLOW}[WARNING] Frontend may still be starting. Check frontend.log for details.${NC}"
fi
echo ""

# ------------------------------------------------------------------
# SUCCESS
# ------------------------------------------------------------------
echo -e "${GREEN}=====================================================================${NC}"
echo -e "${GREEN}                  Canvas App Successfully Started!                   ${NC}"
echo -e "${GREEN}=====================================================================${NC}"
echo ""
echo -e "  Frontend (React):  ${BLUE}http://localhost:${FRONTEND_PORT}${NC}"
echo -e "  Backend  (API):    ${BLUE}http://localhost:${BACKEND_PORT}${NC}"
echo -e "  API Docs:          ${BLUE}http://localhost:${BACKEND_PORT}/swagger-ui/index.html${NC}"
echo -e "  Test coverage:     npm run test:coverage  (run inside canvas-frontend)"
echo ""
echo -e "  Backend  PID: $BACKEND_PID"
echo -e "  Frontend PID: $FRONTEND_PID"
echo ""
echo -e "  Press Ctrl+C to stop all services."
echo ""
echo -e "${GREEN}=====================================================================${NC}"
echo ""

# Keep script alive; cleanup trap fires on Ctrl+C
wait $BACKEND_PID $FRONTEND_PID