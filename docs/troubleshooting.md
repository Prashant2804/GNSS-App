# Troubleshooting

## Pi service not reachable
- Verify Pi is on the same network.
- Check `systemctl status gnss-gateway`.
- Confirm firewall allows port 8000.

## No telemetry updates
- Verify GNSS receiver connection and serial port.
- Check logs in `/var/log/gnss-gateway`.
