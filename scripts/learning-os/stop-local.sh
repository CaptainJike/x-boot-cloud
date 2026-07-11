#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PID_DIR="$ROOT_DIR/.logs/learning-os/pids"

stop_service() {
  local name="$1"
  local pid_file="$PID_DIR/${name}.pid"

  if [[ ! -f "$pid_file" ]]; then
    echo "[learning-os] ${name} pid file not found"
    return
  fi

  local pid
  pid="$(cat "$pid_file")"
  if kill -0 "$pid" 2>/dev/null; then
    echo "[learning-os] stopping ${name} pid=${pid}"
    kill "$pid"
  else
    echo "[learning-os] ${name} is not running"
  fi
  rm -f "$pid_file"
}

stop_service "app-api"
stop_service "learning-service"
stop_service "ai-service"
stop_service "sys-service"
