# GNSS Flow-like App — Copilot Master Plan

## 0. Goal
Build an Android app (simple UX) that connects over Wi-Fi to a Raspberry Pi Zero 2 W gateway, which connects to a u-blox ZED-X20P GNSS receiver.
The system must implement all features in `/docs/parity/checklist.md` with acceptance criteria and tests.

## 1. Non-negotiables
- Single source of truth: this file + `/shared/openapi/openapi.yaml` + `/shared/schemas/*.json`
- Any new endpoint must be added to OpenAPI first.
- Any new WebSocket message must be added to `/shared/openapi/ws_schemas/*.json`
- Any new feature must add:
  - unit tests
  - a parity checklist line item (if not already present)
  - documentation (short)

## 2. Repo structure (do not deviate)
Use /android, /pi, /shared, /docs, /scripts, and /.github per README.

## 3. Build contract
The following commands MUST work on a fresh clone:
- `make bootstrap`
- `make lint`
- `make test`
- `make run-pi-local`
- `make run-android`
- `make deploy-pi HOST=<pi-host>`
If a command cannot be supported in CI, document why.

## 4. Architecture decisions
### 4.1 Pi service
- Language: Python (FastAPI).
- Provides REST + WebSocket telemetry.
- Reads GNSS from serial/UART/USB and can replay mocked GNSS logs for dev.

### 4.2 Android app
- Kotlin + Jetpack Compose + MVVM
- Room DB (offline-first)
- Retrofit for REST, OkHttp WebSocket for telemetry

### 4.3 Data ownership
- Canonical source of truth: Android is canonical; Pi is gateway + logging.
- Conflict strategy: last-write-wins with audit trail on Android.

## 5. Features (must implement all)
All features are enumerated in `/docs/parity/checklist.md`.
Implementation must track those items with GitHub issues.

## 6. Delivery stages
### Stage 1: Skeleton
- Repo scaffolding, OpenAPI, simple Connect screen, Pi health endpoint
- CI green

### Stage 2: Telemetry + corrections
- WebSocket telemetry streaming
- NTRIP configuration and routing
- Display fix quality and correction status in Android

### Stage 3: Projects + collect
- Project CRUD
- Store points with codes + attributes
- Auto-collection rules

### Stage 4: Stakeout + lines + polygons
- Stakeout guidance
- Lines and intervals/offsets
- Polygon area/perimeter

### Stage 5: COGO tools
- Inverse
- Traverse
- Intersection

### Stage 6: Import/export + demo kit
- CSV, custom CSV, DXF, KML, Shapefile exports
- Demo bundle and GNSS replay demo
- Showcase script

## 7. Definition of Done
A feature is done when:
- It is implemented
- It has tests
- It is documented
- It passes parity checklist acceptance criteria
- It works in demo script

## 8. Copilot instructions
When implementing:
1) Read this file first
2) Update OpenAPI + schemas
3) Implement Pi endpoints
4) Implement Android client + UI
5) Add tests
6) Update docs and parity checklist
7) Ensure `make test` passes
