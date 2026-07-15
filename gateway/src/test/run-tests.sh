#!/usr/bin/env sh
set -euo pipefail

ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)

if [ ! -f "$ROOT_DIR/pom.xml" ]; then
  echo "pom.xml not found in $ROOT_DIR. Configure Maven before running tests."
  exit 1
fi

cd "$ROOT_DIR"
mvn -q test
