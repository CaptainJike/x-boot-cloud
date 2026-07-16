#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUNTIME_DIR="${ROOT_DIR}/.run/local-services"
PID_DIR="${RUNTIME_DIR}/pids"
SERVICE_START_WAIT_SECONDS="${SERVICE_START_WAIT_SECONDS:-5}"
SERVICE_READY_TIMEOUT_SECONDS="${SERVICE_READY_TIMEOUT_SECONDS:-30}"
SERVICE_STOP_WAIT_SECONDS="${SERVICE_STOP_WAIT_SECONDS:-20}"
JAVA_BIN="${JAVA_BIN:-java}"
MAVEN_BIN="${MAVEN_BIN:-mvn}"

NACOS_JVM_ARGS=(
  "-DNACOS_CONFIG_GROUP=DEFAULT_GROUP"
  "-DNACOS_CONFIG_NAMESPACE=4b94624a-f3a9-4e13-b186-2ff573a632b3"
  "-DNACOS_CONFIG_SERVER_ADDR=127.0.0.1:8848"
  "-DNACOS_DISCOVERY_GROUP=DISCOVERY_GROUP"
  "-DNACOS_DISCOVERY_NAMESPACE=4b94624a-f3a9-4e13-b186-2ff573a632b3"
  "-DNACOS_DISCOVERY_SERVER_ADDR=127.0.0.1:8848"
)

# Backend services must be started before the API applications that depend on
# them.
BACKEND_SERVICES=(
  "sys-service:x-boot-modules/sys/sys-service"
  "oss-service:x-boot-modules/oss/oss-service"
  "ai-service:x-boot-modules/ai/ai-service"
  "learning-service:x-boot-modules/learning/learning-service"
)

API_SERVICES=(
  "admin-api:x-boot-api/admin-api"
  "app-api:x-boot-api/app-api"
)

SERVICES=(
  "${BACKEND_SERVICES[@]}"
  "${API_SERVICES[@]}"
)

ensure_runtime_dirs() {
  mkdir -p "${PID_DIR}"
}

resolve_jar_file() {
  local module="$1"
  local target_dir="${ROOT_DIR}/${module}/target"

  find "${target_dir}" -maxdepth 1 -type f -name "*.jar" \
    ! -name "original-*.jar" \
    ! -name "*-sources.jar" \
    ! -name "*-javadoc.jar" | sort | head -n 1
}

is_pid_running() {
  local pid="${1:-}"

  [[ -n "${pid}" ]] && kill -0 "${pid}" 2>/dev/null
}

find_service_pid_by_jar() {
  local module="$1"
  local jar_file
  local jar_name

  jar_file="$(resolve_jar_file "${module}")"
  if [[ -z "${jar_file}" ]]; then
    return 1
  fi

  jar_name="$(basename "${jar_file}")"
  ps -eo pid=,command= | awk -v jar_name="${jar_name}" '
    index($0, " -jar ") && index($0, jar_name) { print $1; exit }
  '
}

process_has_listening_port() {
  local pid="$1"

  lsof -Pan -a -p "${pid}" -iTCP -sTCP:LISTEN >/dev/null 2>&1
}
