#!/usr/bin/env bash
set -euo pipefail

# -------------------------------------------------------
# Install gnss-gateway as a systemd service on the Pi.
# Run this ON the Pi itself (or via ssh).
# -------------------------------------------------------

APP_DIR="/opt/gnss-gateway"
SERVICE_NAME="gnss-gateway"
SERVICE_FILE="/etc/systemd/system/${SERVICE_NAME}.service"

echo "Installing $SERVICE_NAME to $APP_DIR ..."

sudo mkdir -p "$APP_DIR"
sudo cp -r pi/src pi/pyproject.toml "$APP_DIR/"

if [[ ! -d "$APP_DIR/.venv" ]]; then
  echo "Creating virtualenv..."
  sudo python3 -m venv "$APP_DIR/.venv"
fi

echo "Installing Python dependencies..."
sudo "$APP_DIR/.venv/bin/pip" install -e "$APP_DIR[dev]" -q

echo "Writing systemd unit file..."
sudo tee "$SERVICE_FILE" > /dev/null <<UNIT
[Unit]
Description=GNSS Gateway Service
After=network.target

[Service]
Type=simple
WorkingDirectory=$APP_DIR
Environment=PYTHONPATH=$APP_DIR/src
ExecStart=$APP_DIR/.venv/bin/python -m uvicorn gnss_gateway.main:app --host 0.0.0.0 --port 8000
Restart=on-failure
RestartSec=5
User=pi

[Install]
WantedBy=multi-user.target
UNIT

echo "Enabling and starting service..."
sudo systemctl daemon-reload
sudo systemctl enable "$SERVICE_NAME"
sudo systemctl restart "$SERVICE_NAME"

echo ""
echo "Done. Check status with:  systemctl status $SERVICE_NAME"
