#!/usr/bin/env bash
# Regenerates the local stack's demo images. The local MinIO runs without a volume, so every Docker
# restart empties the bucket and the storefront renders with broken images (see
# qa/content-owns-appearance-and-media.md, "Populating MinIO"). This script rebuilds one placeholder
# per seeded storage_key straight from the database and syncs them up — about a minute, ~940 objects.
#
# Needs: docker (the lcl postgres container), python3, the aws CLI. macOS/Linux, no image libraries.
#
#   ./extra/scripts/seed-minio-images.sh                 # default lcl stack (localhost:9000)
#   MINIO_URL=http://localhost:11000 ./extra/scripts/seed-minio-images.sh   # an offset stack
set -euo pipefail

MINIO_URL="${MINIO_URL:-http://localhost:9000}"
BUCKET="${BUCKET:-d0dd4299-963a-4458-b31f-8efe31c35e8e}"
PG_CONTAINER="${PG_CONTAINER:-$(docker ps --format '{{.Names}}' | grep 'postgres' | head -1)}"
STAGE="$(mktemp -d)"
trap 'rm -rf "$STAGE"' EXIT

echo "keys from ${PG_CONTAINER}, objects to ${MINIO_URL}/${BUCKET}"
docker exec "$PG_CONTAINER" psql -U postgres -d cvhome -tAc \
  'select storage_key from content.media_asset order by 1' > "$STAGE/keys.txt"

python3 - "$STAGE" <<'PY'
# One solid-colour JPEG-sized PNG per (store, kind), copied to every key. Pure stdlib: a placeholder
# needs to exist, not to be pretty. Browsers render the PNG bytes fine whatever the extension says.
import sys, struct, zlib, hashlib, pathlib

stage = pathlib.Path(sys.argv[1])

def png(w, h, rgb):
    raw = b''.join(b'\x00' + bytes(rgb) * w for _ in range(h))
    def chunk(t, d):
        c = t + d
        return struct.pack('>I', len(d)) + c + struct.pack('>I', zlib.crc32(c))
    return (b'\x89PNG\r\n\x1a\n'
            + chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 2, 0, 0, 0))
            + chunk(b'IDAT', zlib.compress(raw, 6))
            + chunk(b'IEND', b''))

def color(seed):
    hv = hashlib.md5(seed.encode()).digest()
    return [hv[0] % 96 + 120, hv[1] % 96 + 120, hv[2] % 96 + 120]

SIZES = {'LOGO': (320, 120), 'BANNER': (1440, 640), 'SLIDER': (1440, 640)}
bases, keys = {}, [k.strip() for k in open(stage / 'keys.txt') if k.strip()]
for key in keys:
    parts = key.split('/')
    store = parts[1] if len(parts) > 1 else 'x'
    kind = parts[2] if key.startswith('files/') and len(parts) > 2 else 'PRODUCT'
    w, h = SIZES.get(kind, (800, 800))
    base = (store, kind)
    if base not in bases:
        bases[base] = png(w, h, color(store + kind))
    target = stage / 'tree' / key
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(bases[base])
print(f'staged {len(keys)} objects from {len(bases)} base images')
PY

AWS_ACCESS_KEY_ID="${MINIO_USER:-minioadmin}" AWS_SECRET_ACCESS_KEY="${MINIO_PASSWORD:-minioadmin}" \
  aws --endpoint-url "$MINIO_URL" s3 cp --recursive --content-type image/jpeg --only-show-errors \
  "$STAGE/tree/" "s3://${BUCKET}/"
echo "done — the storefront's images render again"
