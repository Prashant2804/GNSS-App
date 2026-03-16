# Issue 001: Health endpoint

## Summary
Implement and verify the Pi REST health endpoint.

## Checklist reference
- `P0-HEALTH` in `docs/parity/checklist.md`

## Acceptance criteria
- `GET /health` returns 200 JSON with `{ "status": "ok" }`.
- `pi/tests/test_health.py` passes.

## Notes
- Validate on Pi and local dev server.
