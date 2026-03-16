# GNSS Flow-like App

## What it is
Android app (Kotlin + Jetpack Compose) that connects over Wi-Fi to a Raspberry Pi
Zero 2 W gateway, which connects to a u-blox ZED-X20P GNSS receiver. The Pi
provides REST + WebSocket telemetry and project data; Android is the primary UI.

## Prereqs
- Java 17
- Android Studio (latest stable)
- Python 3.11+
- Raspberry Pi OS (for Pi deployment)

## Quick start (no hardware)
1. `make bootstrap`
2. `make run-pi-local` (starts API + telemetry using replay file)
3. `make run-android` (connect to http://10.0.2.2:8000 from emulator)

## Raspberry Pi install
1. Copy config: `pi/config/gnss-gateway.example.yaml` -> `/etc/gnss-gateway.yaml`
2. Deploy: `make deploy-pi HOST=pi.local`
3. Verify: open `http://pi.local:8000/health`

## Configure GNSS
- Set serial port and baud in config
- Verify telemetry shows updates

## Configure NTRIP
- Add caster and mount point in app or Pi config
- Confirm RTCM bytes/sec > 0 and "Age of diff" stable

## Export and demo
- Use `shared/samples/demo_project.bundle.zip`
- Follow `docs/demo/demo_script.md`
