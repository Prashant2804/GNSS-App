# Field Install Guide

1. Flash Raspberry Pi OS and connect to network.
2. Copy `pi/config/gnss-gateway.example.yaml` to `/etc/gnss-gateway.yaml`.
3. Run `make deploy-pi HOST=<pi-host>`.
4. Verify `http://<pi-host>:8000/health`.
