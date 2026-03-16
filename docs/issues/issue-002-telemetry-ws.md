# Issue 002: Telemetry WebSocket

## Acceptance criteria
- `/ws/telemetry` accepts WebSocket connections.
- Payload matches telemetry schema and updates at least 1 Hz in mock.

## Test plan
- Contract validation with schema and unit test stub.
# Issue 002: Telemetry WebSocket

## Summary
Provide a WebSocket telemetry stream with schema compliance.

## Checklist reference
- `P0-WS-TELEMETRY` in `docs/parity/checklist.md`

## Acceptance criteria
- WebSocket upgrade succeeds at `/ws/telemetry`.
- Telemetry payloads validate against `shared/openapi/ws_schemas/telemetry.schema.json`.
- Updates stream at least 1 Hz in mock mode.

## Notes
- Add contract tests when schema validation harness is ready.
