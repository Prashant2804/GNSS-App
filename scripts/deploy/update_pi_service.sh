#!/usr/bin/env bash
set -euo pipefail

# -------------------------------------------------------
# Update the gnss-gateway service on a remote Pi via SSH.
# Usage:  make deploy-pi HOST=pi.local
#   or:   HOST=pi.local ./scripts/deploy/update_pi_service.sh
# -------------------------------------------------------

if [[ -z "${HOST:-}" ]]; then
  echo "Usage: HOST=<pi-hostname> $0"
  echo "  e.g. HOST=pi.local $0"
  exit 1
fi

USER="${PI_USER:-pi}"
APP_DIR="/opt/gnss-gateway"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "=== Deploying to $USER@$HOST ==="

echo "1/3  Syncing source files..."
rsync -avz --delete \
  --exclude '.venv' \
  --exclude '__pycache__' \
  --exclude '*.pyc' \
  "$ROOT_DIR/pi/" "$USER@$HOST:$APP_DIR/"

echo "2/3  Installing dependencies on Pi..."
ssh "$USER@$HOST" "cd $APP_DIR && .venv/bin/pip install -e '.[dev]' -q"

echo "3/3  Restarting service..."
ssh "$USER@$HOST" "sudo systemctl restart gnss-gateway"

echo ""
echo "=== Deploy complete. Service status: ==="
ssh "$USER@$HOST" "systemctl status gnss-gateway --no-pager" || true
