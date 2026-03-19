#!/usr/bin/env bash
set -euo pipefail

# -------------------------------------------------------
# E2E Smoke Test
# Starts the Pi gateway, hits /health, runs Android tests,
# then tears down. Exit 0 = all green.
# -------------------------------------------------------

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
PI_DIR="$ROOT_DIR/pi"
ANDROID_DIR="$ROOT_DIR/android"
PI_PID=""

cleanup() {
  if [[ -n "$PI_PID" ]]; then
    echo "Stopping Pi gateway (PID $PI_PID)..."
    kill "$PI_PID" 2>/dev/null || true
    wait "$PI_PID" 2>/dev/null || true
  fi
}
trap cleanup EXIT

echo "=== 1/4  Starting Pi gateway ==="
cd "$PI_DIR"
if [[ ! -d ".venv" ]]; then
  python3 -m venv .venv
  .venv/bin/pip install -e ".[dev]" -q
fi
PYTHONPATH=src .venv/bin/python -m uvicorn gnss_gateway.main:app --port 8000 &
PI_PID=$!
sleep 3

echo "=== 2/4  Checking /health ==="
HEALTH=$(curl -sf http://127.0.0.1:8000/health || echo "FAIL")
if echo "$HEALTH" | grep -q '"ok"'; then
  echo "  /health OK"
else
  echo "  /health FAILED: $HEALTH"
  exit 1
fi

echo "=== 3/4  Pi pytest ==="
cd "$PI_DIR"
PYTHONPATH=src .venv/bin/pytest --tb=short -q

echo "=== 4/4  Android unit tests ==="
cd "$ANDROID_DIR"
./gradlew testDebugUnitTest --no-daemon -q

echo ""
echo "=== ALL SMOKE TESTS PASSED ==="
