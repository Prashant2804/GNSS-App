# Issue 004: OpenAPI + WS Schemas

## Acceptance criteria
- OpenAPI includes /health and /ws/telemetry.
- Telemetry schema exists and is linted in CI.

## Test plan
- `scripts/ci/check_openapi.sh` passes.
# Issue 004: OpenAPI + WS schemas

## Summary
Ensure OpenAPI and WebSocket schemas represent the current contract.

## Checklist reference
- `P1-OPENAPI-SCHEMAS` in `docs/parity/checklist.md`

## Acceptance criteria
- OpenAPI includes `/health` and `/ws/telemetry` entries.
- `scripts/ci/check_openapi.sh` passes.
- WebSocket telemetry schema is valid JSON Schema Draft 2020-12.

## Notes
- Add additional schemas as new events are defined.
