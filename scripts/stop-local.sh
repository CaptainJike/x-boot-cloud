#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/common.sh"

stop_service() {
  local name="$1"
  local module="$2"
  local pid_file="${PID_DIR}/${name}.pid"
  local snapshot_file="${RUNTIME_DIR}/${name}.offsets"
  local pid=""

  if [[ -f "${pid_file}" ]]; then
    pid="$(cat "${pid_file}" 2>/dev/null || true)"
  fi
  if ! is_pid_running "${pid}"; then
    pid="$(find_service_pid_by_jar "${module}" || true)"
  fi
  if ! is_pid_running "${pid}"; then
    echo "[local-services] ${name} is not running"
    rm -f "${pid_file}"
    rm -f "${snapshot_file}"
    return
  fi

  printf '%s\n' "${pid}" > "${pid_file}"
  echo "[local-services] stopping ${name} pid=${pid}"
  kill "${pid}"

  local waited=0
  while is_pid_running "${pid}"; do
    if (( waited >= SERVICE_STOP_WAIT_SECONDS )); then
      echo "[local-services] force stopping ${name} pid=${pid}"
      kill -9 "${pid}" 2>/dev/null || true
      break
    fi
    sleep 1
    waited=$((waited + 1))
  done

  rm -f "${pid_file}"
  rm -f "${snapshot_file}"
}

ensure_runtime_dirs

for (( idx=${#SERVICES[@]} - 1; idx>=0; idx-- )); do
  IFS=":" read -r name module <<< "${SERVICES[idx]}"
  stop_service "${name}" "${module}"
done
