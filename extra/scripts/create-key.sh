#!/bin/sh
set -eu

FORCE=0
ARGS=""
for arg in "$@"; do
    if [ "$arg" = "--force" ]; then
        FORCE=1
    else
        ARGS="$ARGS $arg"
    fi
done
set -- $ARGS

KEY_ID="${1:-default-key}"
KEYS_DIR="${2:-$HOME/.cvhome/secret-crypto/keys}"
KEY_FILE="$KEYS_DIR/$KEY_ID"

if [ -e "$KEY_FILE" ] && [ "$FORCE" -ne 1 ]; then
    echo "Key file already exists: $KEY_FILE" >&2
    echo "Regenerating it will make any data encrypted under this key permanently undecryptable." >&2
    echo "Re-run with --force to overwrite anyway." >&2
    exit 1
fi

mkdir -p "$KEYS_DIR"
chmod 700 "$KEYS_DIR"

if command -v openssl >/dev/null 2>&1; then
    openssl rand -out "$KEY_FILE" 32
else
    dd if=/dev/urandom of="$KEY_FILE" bs=32 count=1 2>/dev/null
fi

chmod 600 "$KEY_FILE"

echo "Generated AES-256 key at: $KEY_FILE"
echo
echo "Set in common-config.yml (or a local override):"
echo "  com.asrevo.cvhome.crypto.local:"
echo "    key-provider-type: FILE"
echo "    active-key-id: $KEY_ID"
echo "    file-path: \"$KEYS_DIR\""
