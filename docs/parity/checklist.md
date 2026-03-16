# Parity Checklist

Each item includes priority, acceptance criteria, manual test steps, and automated test expectation.

## Receiver Control
- **Receiver setup (base/rover)** — Priority: P0  
  Acceptance: set receiver role, persist config, reflect in /device/info.  
  Manual: set role, restart service, verify role retained.  
  Automated: unit + contract.
- **RTK configuration (NTRIP)** — Priority: P0  
  Acceptance: store NTRIP profile; telemetry reports connected + bytes/sec.  
  Manual: add profile, connect, verify corrections fields.  
  Automated: unit + contract.
- **PPK raw logging** — Priority: P1  
  Acceptance: start/stop creates session bundle and manifest.  
  Manual: start/stop log and download bundle.  
  Automated: integration.

## Projects & Data Model
- **Create projects** — Priority: P0  
  Acceptance: create project with units and metadata.  
  Manual: create project and see in list.  
  Automated: unit + contract.
- **Edit/duplicate projects** — Priority: P1  
  Acceptance: rename/duplicate with history.  
  Manual: edit/duplicate and verify outputs.  
  Automated: unit.
- **Change units after creation** — Priority: P1  
  Acceptance: unit change re-computes values.  
  Manual: change units and verify.  
  Automated: unit.

## Collect (Points)
- **Store points** — Priority: P0  
  Acceptance: save point with code, attributes, author, timestamp.  
  Manual: store point and view detail.  
  Automated: unit.
- **Averaging** — Priority: P1  
  Acceptance: N-epoch/N-second averaging works.  
  Manual: enable averaging and compare stored values.  
  Automated: unit.
- **Auto-collection rules** — Priority: P1  
  Acceptance: auto-collect by time and/or distance.  
  Manual: enable rules and confirm auto points.  
  Automated: unit.

## Stakeout (Points)
- **Stakeout guidance** — Priority: P0  
  Acceptance: shows distance, bearing, N/E/Z deltas.  
  Manual: select target point and verify guidance updates.  
  Automated: unit + UI.
- **Stakeout reports** — Priority: P1  
  Acceptance: report includes target vs measured, deltas, accuracy.  
  Manual: run stakeout and export report.  
  Automated: unit.

## Lines & Polygons
- **Collect lines** — Priority: P1  
  Acceptance: create lines from vertices or points.  
  Manual: collect line and verify geometry.  
  Automated: unit.
- **Line stakeout (intervals/offsets)** — Priority: P1  
  Acceptance: chainage and offsets computed along full line.  
  Manual: stakeout along line with intervals.  
  Automated: unit.
- **Polygons (area/perimeter)** — Priority: P1  
  Acceptance: compute area/perimeter for closed polygon.  
  Manual: create polygon and verify metrics.  
  Automated: unit.

## Codes
- **Code libraries** — Priority: P1  
  Acceptance: import/export code list and assign codes to objects.  
  Manual: import codes, assign to point/line.  
  Automated: unit.

## Layers & Maps
- **Base maps** — Priority: P1  
  Acceptance: switch between vector and satellite.  
  Manual: toggle base maps.  
  Automated: UI.
- **WMS layers** — Priority: P2  
  Acceptance: add WMS, toggle, opacity, cache.  
  Manual: add layer and verify.  
  Automated: UI.

## CAD (DXF)
- **DXF import** — Priority: P2  
  Acceptance: DXF geometry and attributes loaded.  
  Manual: import sample DXF and view.  
  Automated: unit.
- **CAD stakeout** — Priority: P2  
  Acceptance: select CAD vertex/segment for stakeout.  
  Manual: select CAD and start stakeout.  
  Automated: unit.

## Surfaces (LandXML)
- **LandXML import** — Priority: P2  
  Acceptance: surface imported and rendered.  
  Manual: import LandXML and view surface.  
  Automated: unit.
- **Cut/fill** — Priority: P2  
  Acceptance: cut/fill displayed during stakeout.  
  Manual: stakeout with surface and verify.  
  Automated: unit.

## COGO Tools
- **Inverse** — Priority: P1  
  Acceptance: compute distance/azimuth/deltas.  
  Manual: select two points.  
  Automated: unit.
- **Traverse** — Priority: P1  
  Acceptance: create new point from distance/azimuth/elevation.  
  Manual: enter values, verify point.  
  Automated: unit.
- **Intersection** — Priority: P2  
  Acceptance: compute intersection (distance-distance, direction-direction, distance-direction).  
  Manual: choose mode and verify results.  
  Automated: unit.

## Coordinate Systems / Base Shift / Units
- **Coordinate system library** — Priority: P1  
  Acceptance: choose from catalog or create custom.  
  Manual: select system, verify transform.  
  Automated: unit.
- **Geoid/vertical datum** — Priority: P2  
  Acceptance: geoid selection affects elevation.  
  Manual: toggle geoid and verify.  
  Automated: unit.
- **Base shift/localization** — Priority: P1  
  Acceptance: translation/rotation/scale applied to project.  
  Manual: enable base shift and verify coords.  
  Automated: unit.

## Import/Export
- **CSV + Custom CSV** — Priority: P1  
  Acceptance: export with configurable fields.  
  Manual: export and inspect file.  
  Automated: unit.
- **DXF/KML/Shapefile** — Priority: P2  
  Acceptance: export to required formats.  
  Manual: export and open in viewer.  
  Automated: unit.
- **PNEZD** — Priority: P2  
  Acceptance: import/export PNEZD.  
  Manual: import and verify points.  
  Automated: unit.

## Reports
- **Stakeout report** — Priority: P1  
  Acceptance: report includes timestamp, operator, deltas, accuracy.  
  Manual: export report and verify fields.  
  Automated: unit.

## Auto-collection
- **Collect rules** — Priority: P1  
  Acceptance: rules trigger based on time/distance thresholds.  
  Manual: enable and verify collection.  
  Automated: unit.

## Collaboration / Sync (Future)
- **Cloud sync** — Priority: P2  
  Acceptance: placeholder for future workflow.  
  Manual: n/a.  
  Automated: n/a.
# Parity Checklist

This checklist enumerates all required features with priority, acceptance
criteria, and test expectations. Update status as work progresses.

## Stage 1: Skeleton

### P0-HEALTH — Health endpoint
- Priority: P0
- Scope: Pi REST `/health`
- Acceptance criteria:
  - `GET /health` returns 200 JSON with `{ "status": "ok" }`.
- Manual steps:
  - Hit `http://<pi>:8000/health` in browser or curl.
- Automated tests:
  - `pi/tests/test_health.py` asserts 200 and status payload.
- Status: In progress

### P0-WS-TELEMETRY — Telemetry WebSocket
- Priority: P0
- Scope: Pi WebSocket `/ws/telemetry`
- Acceptance criteria:
  - WebSocket upgrade succeeds.
  - Telemetry JSON matches `shared/openapi/ws_schemas/telemetry.schema.json`.
  - Updates occur at least 1 Hz in dev mock mode.
- Manual steps:
  - Connect with WebSocket client and verify JSON payloads stream.
- Automated tests:
  - Contract validation via schema lint (manual until tests added).
- Status: In progress

### P1-ANDROID-CONNECT — Android Connect screen
- Priority: P1
- Scope: Android UI
- Acceptance criteria:
  - Connect screen renders with status placeholders and connect action.
  - Shows base URL or connection state when configured.
- Manual steps:
  - Launch app and verify Connect screen layout.
- Automated tests:
  - Compose UI test verifies "Connect" view renders.
- Status: Not started

### P1-OPENAPI-SCHEMAS — OpenAPI + WS schemas
- Priority: P1
- Scope: `/shared/openapi` and `/shared/openapi/ws_schemas`
- Acceptance criteria:
  - OpenAPI includes `/health` and `/ws/telemetry`.
  - WebSocket telemetry schema exists and is lintable.
- Manual steps:
  - Run `make test` and inspect schema files.
- Automated tests:
  - `scripts/ci/check_openapi.sh` passes.
- Status: In progress

## Stage 2: Telemetry + Corrections

### P0-NTRIP-CONFIG — NTRIP configuration
- Priority: P0
- Scope: Pi config + Android UI
- Acceptance criteria:
  - Configure caster, mount point, and credentials.
  - Pi reports correction status in telemetry.
- Manual steps:
  - Configure NTRIP details and verify correction age decreases.
- Automated tests:
  - Unit test for config parsing and NTRIP client connect.
- Status: Not started

### P0-FIX-QUALITY — Fix quality indicators
- Priority: P0
- Scope: Telemetry + Android UI
- Acceptance criteria:
  - Telemetry includes fix quality, satellites, accuracy, age of diff.
  - Android displays fix and correction state.
- Manual steps:
  - Observe telemetry values updating in UI.
- Automated tests:
  - ViewModel unit test maps telemetry to UI state.
- Status: Not started

## Stage 3: Projects + Collect

### P0-PROJECTS-CRUD — Project management
- Priority: P0
- Scope: Android DB + Pi sync
- Acceptance criteria:
  - Create, edit, delete projects locally.
  - Sync project metadata to Pi.
- Manual steps:
  - Create a project, edit name, delete, verify list updates.
- Automated tests:
  - Room DAO tests for CRUD.
- Status: Not started

### P0-POINT-COLLECT — Point collection
- Priority: P0
- Scope: Android collection + Pi logging
- Acceptance criteria:
  - Store points with code, attributes, and GNSS position.
  - Exportable via bundle.
- Manual steps:
  - Collect a point and verify in project detail.
- Automated tests:
  - ViewModel test for adding point to project.
- Status: Not started

### P1-AUTO-COLLECT — Auto-collection rules
- Priority: P1
- Scope: Android rules engine
- Acceptance criteria:
  - Rule-based auto-collection triggers on fix + duration.
- Manual steps:
  - Enable rule and observe auto-collected points.
- Automated tests:
  - Unit tests for rule evaluation.
- Status: Not started

## Stage 4: Stakeout + Lines + Polygons

### P1-STAKEOUT — Stakeout guidance
- Priority: P1
- Scope: Android guidance UI
- Acceptance criteria:
  - Guidance displays delta/heading to target point.
- Manual steps:
  - Select target and verify guidance updates as telemetry changes.
- Automated tests:
  - Unit tests for offset calculations.
- Status: Not started

### P1-LINES — Lines and offsets
- Priority: P1
- Scope: Android geometry tools
- Acceptance criteria:
  - Create line from points and compute station/offset.
- Manual steps:
  - Build a line, select a point, check offset output.
- Automated tests:
  - Geometry unit tests for line calculations.
- Status: Not started

### P1-POLYGONS — Polygon area/perimeter
- Priority: P1
- Scope: Android geometry tools
- Acceptance criteria:
  - Create polygon and compute area/perimeter.
- Manual steps:
  - Add polygon points and verify metrics.
- Automated tests:
  - Geometry unit tests for polygon area.
- Status: Not started

## Stage 5: COGO Tools

### P1-COGO-INVERSE — Inverse tool
- Priority: P1
- Scope: Android COGO
- Acceptance criteria:
  - Compute bearing/distance between two points.
- Manual steps:
  - Select two points and verify output.
- Automated tests:
  - Unit tests for inverse computations.
- Status: Not started

### P1-COGO-TRAVERSE — Traverse tool
- Priority: P1
- Scope: Android COGO
- Acceptance criteria:
  - Compute traverse closure and adjust if needed.
- Manual steps:
  - Enter traverse legs and check closure.
- Automated tests:
  - Unit tests for traverse calculations.
- Status: Not started

### P2-COGO-INTERSECTION — Intersection tool
- Priority: P2
- Scope: Android COGO
- Acceptance criteria:
  - Compute intersection of two bearings/lines.
- Manual steps:
  - Provide lines and verify intersection point.
- Automated tests:
  - Unit tests for intersection math.
- Status: Not started

## Stage 6: Import/Export + Demo Kit

### P0-EXPORTS — Export formats
- Priority: P0
- Scope: Android export
- Acceptance criteria:
  - Export CSV, custom CSV, DXF, KML, Shapefile.
- Manual steps:
  - Export a project and open files.
- Automated tests:
  - Unit tests for export adapters.
- Status: Not started

### P1-DEMO-BUNDLE — Demo bundle + replay
- Priority: P1
- Scope: `shared/samples` + scripts
- Acceptance criteria:
  - Demo bundle exists and is usable in app.
  - GNSS replay script works with demo data.
- Manual steps:
  - Run replay script and follow demo script.
- Automated tests:
  - CI smoke test for replay script exit code.
- Status: Not started

## Stage 7: Mapping + CAD + Surfaces

### P1-MAPPING-WMS — Mapping/WMS overlays
- Priority: P1
- Scope: Android map view
- Acceptance criteria:
  - Map screen supports base tiles and WMS overlay.
- Manual steps:
  - Enable overlay and verify tiles render.
- Automated tests:
  - ViewModel/unit tests for layer selection.
- Status: Not started

### P1-CAD-DXF — CAD DXF support
- Priority: P1
- Scope: Import/export
- Acceptance criteria:
  - Import DXF to project layers.
- Manual steps:
  - Import `shared/samples/sample.dxf` and verify entities.
- Automated tests:
  - Unit tests for DXF parser.
- Status: Not started

### P2-LANDXML — LandXML surfaces
- Priority: P2
- Scope: Import/export
- Acceptance criteria:
  - Import LandXML surfaces and compute elevations.
- Manual steps:
  - Import `shared/samples/sample.landxml` and verify surface list.
- Automated tests:
  - Unit tests for LandXML parser.
- Status: Not started

## Stage 8: Survey Tools + Reports

### P1-SURVEY-TOOLS — Survey utilities
- Priority: P1
- Scope: Android tools
- Acceptance criteria:
  - Provide at least: resection, offsets, and point averaging.
- Manual steps:
  - Run each tool and verify output values.
- Automated tests:
  - Unit tests for each tool.
- Status: Not started

### P2-STAKEOUT-REPORTS — Stakeout reports
- Priority: P2
- Scope: Export/reporting
- Acceptance criteria:
  - Stakeout report export includes deltas and timestamps.
- Manual steps:
  - Generate a report and verify fields.
- Automated tests:
  - Unit tests for report formatting.
- Status: Not started

## Stage 9: Test Harness + CI/CD

### P0-TEST-HARNESS — Automated test harness
- Priority: P0
- Scope: repo-wide
- Acceptance criteria:
  - End-to-end smoke tests for Pi + Android.
- Manual steps:
  - Run `make e2e` and verify passes.
- Automated tests:
  - CI workflow runs e2e on PRs.
- Status: Not started

### P0-CICD — CI/CD pipelines
- Priority: P0
- Scope: `.github/workflows`
- Acceptance criteria:
  - CI workflows for Android, Pi, contracts.
- Manual steps:
  - Review workflow status in GitHub.
- Automated tests:
  - CI required checks green.
- Status: Not started

### P1-ANDROID-RELEASE — Android release pipeline
- Priority: P1
- Scope: Android CI/CD
- Acceptance criteria:
  - Release build produces signed APK/AAB (with placeholders).
- Manual steps:
  - Trigger release workflow with test keystore.
- Automated tests:
  - CI artifact upload step succeeds.
- Status: Not started

### P1-DEPLOY-PI — Pi deployment scripts
- Priority: P1
- Scope: `scripts/deploy`
- Acceptance criteria:
  - Deploy script installs systemd service and config.
- Manual steps:
  - Run `make deploy-pi HOST=...` and verify service status.
- Automated tests:
  - CI lint checks scripts.
- Status: Not started
