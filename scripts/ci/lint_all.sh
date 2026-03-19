#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
ERRORS=0

echo "=== Lint: Python (ruff) ==="
cd "$ROOT_DIR/pi"
if command -v ruff >/dev/null 2>&1; then
  ruff check . || ERRORS=$((ERRORS + 1))
elif [[ -f .venv/bin/ruff ]]; then
  .venv/bin/ruff check . || ERRORS=$((ERRORS + 1))
else
  echo "  ruff not found — skipping Python lint"
fi

echo ""
echo "=== Lint: OpenAPI ==="
cd "$ROOT_DIR"
if command -v spectral >/dev/null 2>&1; then
  spectral lint shared/openapi/openapi.yaml || ERRORS=$((ERRORS + 1))
else
  echo "  spectral not found — validating YAML only"
  python3 -c "import yaml; yaml.safe_load(open('shared/openapi/openapi.yaml')); print('  OpenAPI YAML OK')"
fi

echo ""
echo "=== Lint: WebSocket schemas ==="
for f in shared/openapi/ws_schemas/*.json; do
  python3 -c "import json; json.load(open('$f')); print('  OK: $f')" || ERRORS=$((ERRORS + 1))
done

echo ""
echo "=== Lint: Android ==="
cd "$ROOT_DIR/android"
./gradlew lint --no-daemon || ERRORS=$((ERRORS + 1))

echo ""
if [[ $ERRORS -gt 0 ]]; then
  echo "=== LINT FAILED ($ERRORS issues) ==="
  exit 1
else
  echo "=== ALL LINTS PASSED ==="
fi
