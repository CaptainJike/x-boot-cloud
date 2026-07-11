#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ENV_FILE="$ROOT_DIR/scripts/learning-os/env.local"
LOG_DIR="$ROOT_DIR/.logs/learning-os"
PID_DIR="$LOG_DIR/pids"

if [[ -f "$ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi

mkdir -p "$LOG_DIR" "$PID_DIR"

export NACOS_DISCOVERY_SERVER_ADDR="${NACOS_DISCOVERY_SERVER_ADDR:-127.0.0.1:8848}"
export NACOS_CONFIG_SERVER_ADDR="${NACOS_CONFIG_SERVER_ADDR:-127.0.0.1:8848}"
export NACOS_DISCOVERY_NAMESPACE="${NACOS_DISCOVERY_NAMESPACE:-${NACOS_NAMESPACE:-}}"
export NACOS_CONFIG_NAMESPACE="${NACOS_CONFIG_NAMESPACE:-${NACOS_NAMESPACE:-}}"
export NACOS_DISCOVERY_GROUP="${NACOS_DISCOVERY_GROUP:-${NACOS_GROUP:-DEFAULT_GROUP}}"
export NACOS_CONFIG_GROUP="${NACOS_CONFIG_GROUP:-${NACOS_GROUP:-DEFAULT_GROUP}}"

export LEARNING_GITHUB_REDIRECT_URI="${LEARNING_GITHUB_REDIRECT_URI:-http://localhost:5173/auth/callback}"

start_service() {
  local name="$1"
  local module="$2"
  local log_file="$LOG_DIR/${name}.log"
  local pid_file="$PID_DIR/${name}.pid"

  if [[ -f "$pid_file" ]] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
    echo "[learning-os] ${name} is already running with pid $(cat "$pid_file")"
    return
  fi

  echo "[learning-os] starting ${name}"
  nohup mvn -pl "${module}" -am spring-boot:run >"${log_file}" 2>&1 &
  echo $! > "$pid_file"
  echo "[learning-os] ${name} pid=$(cat "$pid_file") log=${log_file}"
}

start_service "sys-service" "x-boot-modules/sys/sys-service"
start_service "ai-service" "x-boot-modules/ai/ai-service"
start_service "learning-service" "x-boot-modules/learning/learning-service"
start_service "app-api" "x-boot-api/app-api"

cat <<EOF
[learning-os] local services started
  app-api:          http://127.0.0.1:8080
  sys-service:      http://127.0.0.1:7101
  ai-service:       http://127.0.0.1:7102
  learning-service: http://127.0.0.1:7103

Log directory:
  ${LOG_DIR}

Useful commands:
  tail -f ${LOG_DIR}/app-api.log
  bash scripts/learning-os/stop-local.sh
EOF
