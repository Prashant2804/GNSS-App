#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${HOST:-}" ]]; then
  echo "HOST is required. Example: make deploy-pi HOST=pi.local"
  exit 1
fi

echo "Update service on ${HOST} (placeholder)."
