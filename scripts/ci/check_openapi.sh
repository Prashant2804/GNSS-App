#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "=== Validating OpenAPI spec ==="
python3 -c "
import yaml, sys
spec = yaml.safe_load(open('$ROOT_DIR/shared/openapi/openapi.yaml'))
assert 'openapi' in spec, 'Missing openapi version key'
assert 'paths' in spec, 'Missing paths key'
paths = list(spec['paths'].keys())
assert len(paths) > 0, 'No paths defined'
print(f'  OpenAPI OK — {len(paths)} paths defined')
for p in sorted(paths):
    print(f'    {p}')
"

echo ""
echo "=== Validating WS schemas ==="
for f in "$ROOT_DIR"/shared/openapi/ws_schemas/*.json; do
  python3 -c "import json; json.load(open('$f')); print('  OK: $(basename "$f")')"
done

echo ""
echo "=== Contract checks passed ==="
