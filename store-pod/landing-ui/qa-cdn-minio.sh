#!/bin/bash
###############################################################################
# QA: static-assets CDN mode against the local MinIO from docker-compose-lcl.
#
# Prereqs:
#   - MinIO running (docker compose -f docker-compose-lcl.yml up -d minio)
#   - a production build: npm ci && npm run build   (run from store-pod/landing-ui)
#
# What it does:
#   1. creates a public-read bucket in MinIO (idempotent)
#   2. copies storefront/.next/standalone into a temp dir exactly like the
#      Dockerfile lays it out (fresh copy = untouched sentinel, like a new container)
#   3. starts `node storefront/start.mjs` with the STATIC_ASSETS_* env
#   4. checks: upload log, page renders, no origin-relative /_next refs remain,
#      every CDN asset URL referenced by the page returns 200 from MinIO
#
# Run it twice: the second run must log "already synced … skipping upload".
# STOREFRONT_THEME_OVERRIDE=true keeps the ?theme= / ?color= QA overrides working in
# this production build (they are off by default outside dev).
# Inspect the bucket at http://localhost:9001 (minioadmin/minioadmin)
# or: docker exec cvhome-minio-1 mc ls -r lcl/storefront-assets/storefront | head
###############################################################################
set -euo pipefail
cd "$(dirname "$0")"

MINIO_CONTAINER="${MINIO_CONTAINER:-cvhome-minio-1}"
BUCKET="${STATIC_ASSETS_S3_BUCKET:-storefront-assets}"
PREFIX="${STATIC_ASSETS_S3_PREFIX:-storefront}"
PORT="${PORT:-8110}"
BASE_URL="http://localhost:9000/${BUCKET}/${PREFIX}"

[ -f storefront/.next/standalone/storefront/server.js ] || { echo "no build output — run: npm ci && npm run build"; exit 1; }
! lsof -nP -iTCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1 || { echo "port ${PORT} already in use (dev server?) — stop it or re-run with PORT=<free port>"; exit 1; }
curl -sf http://localhost:9000/minio/health/live >/dev/null || { echo "MinIO not reachable on :9000 — start docker-compose-lcl minio"; exit 1; }

echo "==> 1/4 bucket ${BUCKET} (public read)"
docker exec "$MINIO_CONTAINER" sh -c \
  "mc alias set lcl http://localhost:9000 minioadmin minioadmin >/dev/null && mc mb -p lcl/${BUCKET} >/dev/null 2>&1 || true; mc anonymous set download lcl/${BUCKET} >/dev/null"

echo "==> 2/4 fresh container-like layout (Dockerfile COPY steps)"
RUN_DIR="$(mktemp -d /tmp/landing-ui-cdn-qa.XXXXXX)"
cp -R storefront/.next/standalone/ "$RUN_DIR"/
cp -R storefront/.next/static "$RUN_DIR"/storefront/.next/static
cp -R storefront/public "$RUN_DIR"/storefront/public
cp storefront/start.mjs "$RUN_DIR"/storefront/start.mjs
mkdir -p "$RUN_DIR"/storefront/scripts
cp -R storefront/scripts/static-assets "$RUN_DIR"/storefront/scripts/static-assets

echo "==> 3/4 starting on :${PORT} with sync enabled (dir: $RUN_DIR)"
LOG="$RUN_DIR/server.log"
(
  cd "$RUN_DIR"
  STOREFRONT_THEME_OVERRIDE=true \
  STATIC_ASSETS_SYNC_ENABLED=true \
  STATIC_ASSETS_S3_BUCKET="$BUCKET" \
  STATIC_ASSETS_S3_PREFIX="$PREFIX" \
  STATIC_ASSETS_BASE_URL="$BASE_URL" \
  STATIC_ASSETS_S3_ENDPOINT=http://localhost:9000 \
  STATIC_ASSETS_S3_FORCE_PATH_STYLE=true \
  AWS_REGION=eu-central-1 \
  AWS_ACCESS_KEY_ID=minioadmin \
  AWS_SECRET_ACCESS_KEY=minioadmin \
  PORT="$PORT" node storefront/start.mjs >"$LOG" 2>&1
) &
SERVER_PID=$!
trap 'kill $SERVER_PID 2>/dev/null || true' EXIT

for _ in $(seq 1 60); do
  curl -sf -o /dev/null "http://localhost:${PORT}/store-not-found" && break
  kill -0 $SERVER_PID 2>/dev/null || { echo "server died:"; cat "$LOG"; exit 1; }
  sleep 1
done

echo "---- static-assets log:"
grep static-assets "$LOG" || { echo "no static-assets output:"; cat "$LOG"; exit 1; }

echo "==> 4/4 checks"
PAGE="$RUN_DIR/page.html"
CODE=$(curl -s -o "$PAGE" -w "%{http_code}" "http://localhost:${PORT}/store-not-found")
[ "$CODE" = 200 ] || { echo "FAIL: page returned $CODE"; exit 1; }
echo "page: 200"

REL=$(grep -c '"/_next/static' "$PAGE" || true)
[ "$REL" = 0 ] || { echo "FAIL: $REL origin-relative /_next/static refs left in HTML"; exit 1; }
echo "origin-relative /_next refs in HTML: 0"

grep -o "${BASE_URL}/[^\"<> \\\\]*" "$PAGE" | sed 's/\\u0026.*//' | sort -u > "$RUN_DIR/urls.txt"
COUNT=$(wc -l < "$RUN_DIR/urls.txt" | tr -d ' ')
[ "$COUNT" -gt 0 ] || { echo "FAIL: page references no CDN URLs"; exit 1; }
FAIL=0
while read -r u; do
  c=$(curl -s -o /dev/null -w "%{http_code}" "$u")
  [ "$c" = 200 ] || { echo "FAIL: $c $u"; FAIL=1; }
done < "$RUN_DIR/urls.txt"
[ "$FAIL" = 0 ] || exit 1
echo "all $COUNT CDN asset URLs referenced by the page return 200 from MinIO"

echo
echo "OK. Bucket contents:  docker exec $MINIO_CONTAINER mc ls -r lcl/${BUCKET}/${PREFIX} | head -20"
echo "Build marker:         docker exec $MINIO_CONTAINER mc ls lcl/${BUCKET}/${PREFIX}/_builds/"
echo "Browse in console:    http://localhost:9001 (minioadmin/minioadmin)"
echo "Open the page:        http://localhost:${PORT}/store-not-found  (server still running, pid $SERVER_PID)"
echo "Stop with Ctrl+C. Re-run this script to see the 'already synced — skipping upload' path."
wait $SERVER_PID
