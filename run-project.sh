#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

require_command() {
  local command_name="$1"
  local error_message="$2"

  if ! command -v "$command_name" >/dev/null 2>&1; then
    echo "[ERROR] $error_message"
    exit 1
  fi
}

require_command java "Java 17 is not installed or not available in PATH."
require_command mvn "Maven 3.9+ is not installed or not available in PATH."
require_command node "Node.js 18+ is not installed or not available in PATH."
require_command npm "npm is not installed or not available in PATH."

if [ ! -d "$ROOT_DIR/frontend/node_modules" ]; then
  echo "Frontend dependencies not found. Running npm install..."
  (
    cd "$ROOT_DIR/frontend"
    npm install
  )
fi

cleanup() {
  if [ -n "${BACKEND_PID:-}" ] && kill -0 "$BACKEND_PID" >/dev/null 2>&1; then
    kill "$BACKEND_PID" >/dev/null 2>&1 || true
  fi

  if [ -n "${FRONTEND_PID:-}" ] && kill -0 "$FRONTEND_PID" >/dev/null 2>&1; then
    kill "$FRONTEND_PID" >/dev/null 2>&1 || true
  fi
}

trap cleanup EXIT INT TERM

echo "Starting backend on http://localhost:8080 ..."
(
  cd "$ROOT_DIR/backend"
  mvn spring-boot:run
) &
BACKEND_PID=$!

echo "Starting frontend on http://localhost:3000 ..."
(
  cd "$ROOT_DIR/frontend"
  npm run dev
) &
FRONTEND_PID=$!

echo
echo "StegaCrypt is starting."
echo "Frontend: http://localhost:3000"
echo "Backend health: http://localhost:8080/api/health"
echo "Press Ctrl+C in this terminal to stop both processes."

wait
