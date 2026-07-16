#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/common.sh"

build_all_services() {
  local -a modules=()
  local service
  local name
  local module

  for service in "${SERVICES[@]}"; do
    IFS=":" read -r name module <<< "${service}"
    modules+=("${module}")
  done

  local module_list
  module_list="$(IFS=,; echo "${modules[*]}")"

  echo "[local-services] packaging services"
  if ! "${MAVEN_BIN}" -pl "${module_list}" -am -DskipTests package; then
    echo "[local-services] package failed" >&2
    return 1
  fi
}

capture_log_snapshot() {
  local name="$1"
  local snapshot_file="$2"
  local log_dir="${ROOT_DIR}/logs/${name}"

  : > "${snapshot_file}"
  if [[ ! -d "${log_dir}" ]]; then
    return
  fi

  find "${log_dir}" -maxdepth 1 -type f -name "*.log" | sort | while read -r file; do
    printf '%s\t%s\n' "${file}" "$(wc -c < "${file}")" >> "${snapshot_file}"
  done
}

read_appended_log_content() {
  local snapshot_file="$1"
  local file="$2"
  local previous_size="0"

  if [[ -f "${snapshot_file}" ]]; then
    previous_size="$(
      awk -F '\t' -v target="${file}" '$1 == target { print $2; exit }' "${snapshot_file}"
    )"
    previous_size="${previous_size:-0}"
  fi

  local current_size
  current_size="$(wc -c < "${file}")"
  if (( current_size <= previous_size )); then
    return
  fi

  tail -c "+$((previous_size + 1))" "${file}" 2>/dev/null || true
}

log_contains_since_snapshot() {
  local name="$1"
  local snapshot_file="$2"
  local pattern="$3"
  local log_dir="${ROOT_DIR}/logs/${name}"

  if [[ ! -d "${log_dir}" ]]; then
    return 1
  fi

  local file
  while read -r file; do
    if read_appended_log_content "${snapshot_file}" "${file}" | rg -q "${pattern}"; then
      return 0
    fi
  done < <(find "${log_dir}" -maxdepth 1 -type f -name "*.log" | sort)

  return 1
}

service_has_log_files() {
  local name="$1"
  local log_dir="${ROOT_DIR}/logs/${name}"

  [[ -n "$(find "${log_dir}" -maxdepth 1 -type f -name "*.log" -print -quit 2>/dev/null)" ]]
}

tail_service_logs() {
  local name="$1"
  local log_dir="${ROOT_DIR}/logs/${name}"

  if [[ ! -d "${log_dir}" ]]; then
    return
  fi

  local file
  while read -r file; do
    echo "[local-services] tail ${file}" >&2
    tail -n 20 "${file}" >&2 || true
  done < <(find "${log_dir}" -maxdepth 1 -type f -name "*.log" | sort)
}

wait_for_service_ready() {
  local name="$1"
  local pid="$2"
  local snapshot_file="$3"
  local waited=0
  local log_dir="${ROOT_DIR}/logs/${name}"

  while (( waited < SERVICE_READY_TIMEOUT_SECONDS )); do
    if service_has_log_files "${name}"; then
      if log_contains_since_snapshot "${name}" "${snapshot_file}" "APPLICATION FAILED TO START|Application run failed"; then
        echo "[local-services] ${name} failed to start, check ${log_dir}" >&2
        tail_service_logs "${name}"
        return 1
      fi

      if log_contains_since_snapshot "${name}" "${snapshot_file}" "Started .*Application|Application '.*' is running!"; then
        return 0
      fi
    elif is_pid_running "${pid}" && process_has_listening_port "${pid}"; then
      echo "[local-services] ${name} is running and already listening on TCP ports"
      return 0
    fi

    if ! is_pid_running "${pid}"; then
      if service_has_log_files "${name}"; then
        echo "[local-services] ${name} exited before becoming ready, check ${log_dir}" >&2
        tail_service_logs "${name}"
      else
        echo "[local-services] ${name} exited before becoming ready and did not produce file logs" >&2
      fi
      return 1
    fi

    sleep 1
    waited=$((waited + 1))
  done

  if is_pid_running "${pid}" && ! service_has_log_files "${name}"; then
    echo "[local-services] ${name} is running, no file logs detected, readiness judged by process survival"
    return 0
  fi

  echo "[local-services] ${name} did not report ready within ${SERVICE_READY_TIMEOUT_SECONDS}s, check ${log_dir}" >&2
  tail_service_logs "${name}"
  return 1
}

start_service() {
  local name="$1"
  local module="$2"
  local pid_file="${PID_DIR}/${name}.pid"
  local snapshot_file="${RUNTIME_DIR}/${name}.offsets"
  local pid=""
  local jar_file

  if [[ -f "${pid_file}" ]]; then
    pid="$(cat "${pid_file}" 2>/dev/null || true)"
  fi
  if ! is_pid_running "${pid}"; then
    pid="$(find_service_pid_by_jar "${module}" || true)"
  fi
  if is_pid_running "${pid}"; then
      printf '%s\n' "${pid}" > "${pid_file}"
      echo "[local-services] ${name} is already running with pid ${pid}"
      return
  fi
  rm -f "${pid_file}"

  jar_file="$(resolve_jar_file "${module}")"
  if [[ -z "${jar_file}" ]]; then
    echo "[local-services] jar not found for ${name} under ${ROOT_DIR}/${module}/target" >&2
    return 1
  fi

  rm -f "${snapshot_file}"
  capture_log_snapshot "${name}" "${snapshot_file}"

  echo "[local-services] starting ${name}"
  nohup "${JAVA_BIN}" "${NACOS_JVM_ARGS[@]}" -jar "${jar_file}" </dev/null >/dev/null 2>&1 &
  pid="$!"
  printf '%s\n' "${pid}" > "${pid_file}"
  echo "[local-services] ${name} pid=${pid} jar=${jar_file} logs=${ROOT_DIR}/logs/${name}"

  sleep "${SERVICE_START_WAIT_SECONDS}"
  if ! wait_for_service_ready "${name}" "${pid}" "${snapshot_file}"; then
    if ! is_pid_running "${pid}"; then
      rm -f "${pid_file}"
    fi
    rm -f "${snapshot_file}"
    return 1
  fi

  printf '%s\n' "${pid}" > "${pid_file}"
  rm -f "${snapshot_file}"
}

ensure_runtime_dirs
build_all_services

started_services=()
failed_services=()
skipped_services=()

start_service_group() {
  local service
  local name
  local module

  for service in "$@"; do
    IFS=":" read -r name module <<< "${service}"
    if start_service "${name}" "${module}"; then
      started_services+=("${name}")
    else
      failed_services+=("${name}")
    fi
  done
}

print_failure_summary() {
  local reason="$1"

  cat <<EOF
[local-services] startup finished with failures

reason:
  ${reason}

started services:
  ${started_services[*]:-none}

failed services:
  ${failed_services[*]:-none}
EOF

  if (( ${#skipped_services[@]} > 0 )); then
    cat <<EOF

skipped services:
  ${skipped_services[*]}
EOF
  fi

  cat <<EOF

log directory:
  ${ROOT_DIR}/logs

stop command:
  bash scripts/stop-local.sh
EOF
}

start_service_group "${BACKEND_SERVICES[@]}"

if (( ${#failed_services[@]} > 0 )); then
  for service in "${API_SERVICES[@]}"; do
    IFS=":" read -r name module <<< "${service}"
    skipped_services+=("${name}")
  done
  print_failure_summary "backend service startup failed, API startup skipped"
  exit 1
fi

start_service_group "${API_SERVICES[@]}"

if (( ${#failed_services[@]} > 0 )); then
  print_failure_summary "some API services failed to start"
  exit 1
fi

cat <<EOF
[local-services] all configured services have been started

services:
  sys-service
  oss-service
  ai-service
  learning-service
  admin-api
  app-api

log directory:
  ${ROOT_DIR}/logs

stop command:
  bash scripts/stop-local.sh
EOF
