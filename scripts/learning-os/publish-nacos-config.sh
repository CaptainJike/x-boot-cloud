#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ENV_FILE="$ROOT_DIR/scripts/learning-os/env.local"
CONFIG_DIR="$ROOT_DIR/scripts/learning-os/nacos"

if [[ -f "$ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi

NACOS_BASE_URL="${NACOS_BASE_URL:-http://127.0.0.1:8848}"
NACOS_GROUP="${NACOS_GROUP:-DEFAULT_GROUP}"
NACOS_NAMESPACE="${NACOS_NAMESPACE:-}"
NACOS_USERNAME="${NACOS_USERNAME:-nacos}"
NACOS_PASSWORD="${NACOS_PASSWORD:-nacos}"

resolve_access_token() {
  if [[ -z "$NACOS_USERNAME" ]]; then
    return 0
  fi

  local response
  response="$(curl \
    --fail \
    --silent \
    --show-error \
    -X POST \
    "${NACOS_BASE_URL}/nacos/v1/auth/login" \
    --data-urlencode "username=${NACOS_USERNAME}" \
    --data-urlencode "password=${NACOS_PASSWORD}")"

  printf '%s' "$response" | sed -n 's/.*"accessToken"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p'
}

NACOS_ACCESS_TOKEN="${NACOS_ACCESS_TOKEN:-$(resolve_access_token)}"

publish_file() {
  local file_path="$1"
  local data_id
  data_id="$(basename "$file_path")"

  echo "[learning-os] publishing ${data_id}"

  local -a curl_args=(
    --fail
    --silent
    --show-error
    -X POST
    "${NACOS_BASE_URL}/nacos/v1/cs/configs"
    --data-urlencode "dataId=${data_id}"
    --data-urlencode "group=${NACOS_GROUP}"
    --data-urlencode "type=yaml"
    --data-urlencode "content@${file_path}"
  )

  if [[ -n "$NACOS_NAMESPACE" ]]; then
    curl_args+=(--data-urlencode "tenant=${NACOS_NAMESPACE}")
  fi

  if [[ -n "$NACOS_ACCESS_TOKEN" ]]; then
    curl_args+=(--data-urlencode "accessToken=${NACOS_ACCESS_TOKEN}")
  elif [[ -n "$NACOS_USERNAME" ]]; then
    curl_args+=(--data-urlencode "username=${NACOS_USERNAME}")
    curl_args+=(--data-urlencode "password=${NACOS_PASSWORD}")
  fi

  curl "${curl_args[@]}" > /dev/null
}

for file_path in \
  "$CONFIG_DIR/COMMON.yml" \
  "$CONFIG_DIR/DATASOURCE.yml" \
  "$CONFIG_DIR/REDIS.yml" \
  "$CONFIG_DIR/SA-TOKEN.yml" \
  "$CONFIG_DIR/DUBBO.yml" \
  "$CONFIG_DIR/sys-service.yml" \
  "$CONFIG_DIR/ai-service.yml" \
  "$CONFIG_DIR/learning-service.yml" \
  "$CONFIG_DIR/app-api.yml"; do
  publish_file "$file_path"
done

echo "[learning-os] nacos config publish finished"
