#!/usr/bin/env bash
set -euo pipefail

if ! command -v spectral >/dev/null 2>&1; then
  echo "spectral not installed; skipping lint." >&2
  exit 0
fi

spectral lint shared/openapi/openapi.yaml
