#!/usr/bin/env bash
set -euo pipefail

echo "Linting Python..."
cd "$(dirname "$0")/../../pi"
python -m ruff check . || true

echo "Linting Android..."
cd "$(dirname "$0")/../../android"
./gradlew lint
