# GNSS-Flow — Context & Handoff (for next agent)

This document is a **handoff snapshot** for continuing development of GNSS-Flow (an Emlid Flow–like Android app + Raspberry Pi gateway), including what works today, how to run everything locally, what to validate, and what to build next.

## Repo structure

- **Android app**: `android/`
  - Kotlin + Jetpack Compose
  - MVVM-ish with `ViewModel`s and `StateFlow`
  - Local persistence via **Room**
- **Pi gateway**: `pi/`
  - Python + **FastAPI** + **Uvicorn**
  - Provides REST endpoints + a WebSocket telemetry stream
  - Uses mock telemetry generation (circular path); NTRIP is mocked for now
- **Shared API docs**:
  - OpenAPI: `shared/openapi/openapi.yaml`
  - WebSocket telemetry schema: `shared/openapi/ws_schemas/telemetry.schema.json`
- **Parity / staged plan**: `docs/parity/checklist.md`

## Current product behavior (high level)

The system is intended to run like this:

- Start Pi gateway locally (simulating a GNSS receiver + NTRIP state).
- Android app connects to Pi gateway over REST + WebSocket:
  - REST for `/health`, NTRIP config/connect/disconnect, and IMU enable flag
  - WebSocket for continuous telemetry (`/ws/telemetry`)
- Survey features use the latest telemetry to:
  - Collect points (manual and auto-collect)
  - Navigate to a saved point (“Stakeout”, renamed to “Navigate to point”)
  - Compute lines & offsets live (station/offset on line AB)
  - Compute polygons area/perimeter live

## What is implemented (as of latest commits)

### Connectivity & telemetry

- Android **Connect** screen:
  - Base URL input
  - Connect to telemetry WebSocket
  - Shows telemetry fields (fix quality, sats, accuracies, etc.)
- Global telemetry caching:
  - `android/app/src/main/java/com/gnssflow/app/telemetry/TelemetryStore.kt` holds the latest telemetry for app-wide use.

### NTRIP (mocked)

- Pi gateway exposes:
  - `GET /ntrip/config`, `POST /ntrip/config`
  - `POST /ntrip/connect`, `POST /ntrip/disconnect`
- Android can save/toggle NTRIP and shows status.
- Pi NTRIP behavior is mocked (connected/bytes-per-sec) but endpoint wiring works.

### IMU enable + storing roll/pitch/yaw on points

- Pi gateway exposes:
  - `GET /imu/enabled`
  - `POST /imu/enabled` with `{ "enabled": true|false }`
- WebSocket telemetry optionally includes:
  - `imu: { rollDeg, pitchDeg, yawDeg }` when IMU is enabled
- Android:
  - Connect screen has IMU toggle + status
  - Telemetry card shows roll/pitch/yaw if present
  - Point collection stores IMU fields into Room when present:
    - `PointEntity.imuRollDeg`, `.imuPitchDeg`, `.imuYawDeg`

### Projects & point collection

- Projects list/create/open
- Project detail:
  - Manual collect button
  - Auto-collect controls (time + distance thresholds)
  - Scroll behavior: only point list scrolls; controls remain visible
- Points stored in Room with location/accuracy + optional IMU fields

### Navigate to point (“Stakeout”)

- Renamed to **“Navigate to point”** in UI.
- Shows:
  - Distance
  - Bearing
  - Local deltas \(ΔN, ΔE, ΔU\)
  - Horizontal accuracy

### Lines & offsets (P1-LINES)

- Room entities:
  - `LineEntity` + `LineDao`
- Screen:
  - Pick A and B from saved points
  - Live station/offset using current telemetry (relative to A)
  - Save a line from A→B
  - List saved lines + **Use** to select A/B
- Recent UX fixes:
  - Saved lines now get **unique, descriptive names** (include A/B point codes + index)
  - “Use” now visibly affects the screen by showing A/B **point codes** in the “Live station/offset” card

### Polygons (P1-POLYGONS)

- Room entities:
  - `PolygonEntity` + `PolygonDao`
- Screen:
  - Tap points to add/remove vertices
  - Live area/perimeter
  - Save polygon
- Recent UX fixes:
  - Saved polygons now show a list (name + vertex count) with a **Use** action
  - Jitter/flicker fixed by **caching per-project state** in `PolygonsViewModel` (no selection resets on recomposition)

### Tests

Android unit tests exist for core math and collection logic, including:
- `GeoStakeoutTest`
- `LineMathTest`
- `PolygonMathTest`
- `CollectRulesEngineTest`
- Repository / point collect tests updated for IMU fields

Pi pytest suite exists and should pass locally.

## How to run (local dev)

### Start Pi gateway

**Linux / macOS** — from repo root:

```bash
cd pi
source .venv/bin/activate
PYTHONPATH=src python -m uvicorn gnss_gateway.main:app --reload --port 8000
```

**Windows (PowerShell)** — from repo root:

```powershell
cd pi
# First time only: create venv and install deps
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -e ".[dev]"

# Start gateway (PYTHONPATH so uvicorn finds gnss_gateway)
$env:PYTHONPATH = "src"
.\.venv\Scripts\python.exe -m uvicorn gnss_gateway.main:app --reload --port 8000
```

Health check:

```bash
curl -sS http://127.0.0.1:8000/health
```

Expected:

```json
{"status":"ok"}
```

### Run Android app

**macOS** — from repo root:

```bash
cd android
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew clean installDebug
```

**Windows** — use Android Studio (recommended):

1. Open the `android` folder in Android Studio.
2. Wait for Gradle sync; start an emulator (AVD Manager).
3. Run **Run → Run 'app'** to build and install on the emulator.

Or from PowerShell (requires Android Studio’s JBR and working plugin resolution):

```powershell
cd android
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat installDebug
```

Run unit tests:

```bash
cd android
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest
```

If KSP ever errors with corrupted caches, nuke the cache:

```bash
rm -rf android/app/build/kspCaches
```

### Connect Android → Pi

- Use base URL: **`http://10.0.2.2:8000`** (Android emulator → host loopback; same on Windows and macOS)
- Tap **Connect**
- You should see telemetry values update periodically

## Validation checklist (manual QA)

### Connect + telemetry

- Set base URL to `http://10.0.2.2:8000`
- Tap **Connect**
- Confirm telemetry updates and no buttons are clipped/hidden

### IMU toggle

- Toggle **IMU** on the Connect screen
- Confirm:
  - IMU status text updates
  - Telemetry shows roll/pitch/yaw

### Point collection with IMU stored

- Create a project
- Collect a point while IMU is enabled
- Confirm the point row shows IMU values (R/P/Y)

### Navigate to point

- From project point list, tap **Navigate**
- Confirm distance/bearing/deltas update as telemetry changes

### Lines & offsets

- Open **Lines & offsets**
- Select A and B from points
- Save line; confirm it appears in “Saved lines” with a non-confusing name
- Tap “Use” on a saved line:
  - Confirm A/B labels show the chosen point codes
  - Live station/offset updates with telemetry

### Polygons

- Open **Polygons**
- Add 3+ vertices; confirm live area/perimeter updates
- Save; confirm it appears under “Saved polygons”
- Clear selection; use a saved polygon; confirm vertices are restored
- Confirm no UI jitter/flicker while interacting

## Known constraints / tech debt

- Pi gateway telemetry and NTRIP are **mocked** (good for wiring and UI; needs real receiver integration later).
- Geometry uses approximations:
  - Haversine distance + bearing
  - Local tangent-plane deltas for short distances
  - Good enough for mock telemetry and UX, but later should be validated against higher-accuracy geodesy if needed.
- Room DB version has been bumped multiple times during rapid iteration; verify migrations are stable before productionizing.

## “Next phase” suggestions (what to build next)

Use `docs/parity/checklist.md` as the authoritative staged plan. Candidates to proceed:

- **Stage 5: COGO tools**
  - More advanced line/offset workflows (perpendicular offset staking, stationing UI, etc.)
  - Export/import points and features (CSV/GeoJSON)
- **Receiver integration**
  - Replace mock telemetry with real GNSS receiver parsing on Pi
  - NMEA parsing & quality metrics, RTCM handling, robust connection state
- **Map view**
  - Lightweight map with live position, points, lines, polygons overlay
- **Polish & reliability**
  - Better naming/editing for saved lines/polygons (rename/delete)
  - Persist selected line/polygon selections
  - Improve UX for point selection (search, filters, newest first)

## Git / GitHub notes

- The repo remote is typically `origin` pointing to GitHub.
- On a new Mac, SSH pushing requires:
  - `~/.ssh/id_ed25519` key
  - Add `~/.ssh/id_ed25519.pub` to GitHub SSH keys
  - Test: `ssh -T git@github.com`

