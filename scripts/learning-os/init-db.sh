#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ENV_FILE="$ROOT_DIR/scripts/learning-os/env.local"

if [[ -f "$ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi

MYSQL_HOST="${LEARNING_DB_HOST:-127.0.0.1}"
MYSQL_PORT="${LEARNING_DB_PORT:-3306}"
MYSQL_DB="${LEARNING_DB_NAME:-x_boot_learning_os}"
MYSQL_USER="${LEARNING_DB_USERNAME:-root}"
MYSQL_PASSWORD="${LEARNING_DB_PASSWORD:-root}"

echo "[learning-os] initializing database ${MYSQL_DB} on ${MYSQL_HOST}:${MYSQL_PORT}"

cd "$ROOT_DIR"

if [[ -n "$MYSQL_PASSWORD" ]]; then
  MYSQL_PWD="${MYSQL_PASSWORD}" mysql \
    -h"${MYSQL_HOST}" \
    -P"${MYSQL_PORT}" \
    -u"${MYSQL_USER}" \
    < "scripts/learning-os/sql/init_learning_os_v1.sql"
else
  mysql \
    -h"${MYSQL_HOST}" \
    -P"${MYSQL_PORT}" \
    -u"${MYSQL_USER}" \
    < "scripts/learning-os/sql/init_learning_os_v1.sql"
fi

echo "[learning-os] database init finished"
