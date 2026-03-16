#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/../../pi"
uvicorn gnss_gateway.main:app --reload --port 8000
