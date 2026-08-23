#!/bin/bash
set -euo pipefail
npm ci
npm run build
docker build -t store-front-app .
docker run --rm -p 8110:8110 -e FALLBACK_STORE_ID="${FALLBACK_STORE_ID:-}" store-front-app
