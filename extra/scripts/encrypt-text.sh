#!/bin/sh
set -eu

# Encrypts text with AES-256-GCM using a key file created by create-key.sh,
# producing output compatible with LocalAesCryptoProvider / EncryptedValue
# (com.asrevo.cvhome.crypto), i.e. "ENC:<version>:<keyId>:<algorithm>:<ivB64>:<ciphertextB64>"
# where ciphertextB64 is the GCM ciphertext with the 16-byte auth tag appended.

usage() {
    echo "Usage: $0 <text> [keys-dir]" >&2
    echo "Prints an EncryptedValue-serialized string (ENC:...) to stdout." >&2
    exit 1
}

[ $# -ge 1 ] || usage

TEXT="$1"
KEY_ID="default-key"
KEYS_DIR="${2:-$HOME/.cvhome/secret-crypto/keys}"
KEY_FILE="$KEYS_DIR/$KEY_ID"

[ -f "$KEY_FILE" ] || { echo "Key file not found: $KEY_FILE" >&2; echo "Generate one first with create-key.sh" >&2; exit 1; }

command -v node >/dev/null 2>&1 || { echo "node is required but was not found" >&2; exit 1; }

VERSION=1
ALGORITHM="AES-256-GCM"

TEXT="$TEXT" KEY_FILE="$KEY_FILE" KEY_ID="$KEY_ID" VERSION="$VERSION" ALGORITHM="$ALGORITHM" node -e '
const crypto = require("crypto");
const fs = require("fs");

const key = fs.readFileSync(process.env.KEY_FILE);
const plaintext = Buffer.from(process.env.TEXT, "utf8");

const iv = crypto.randomBytes(12);
const cipher = crypto.createCipheriv("aes-256-gcm", key, iv);
const encrypted = Buffer.concat([cipher.update(plaintext), cipher.final()]);
const tag = cipher.getAuthTag();
const ciphertext = Buffer.concat([encrypted, tag]);

const serialized = "ENC:" + [
    process.env.VERSION,
    process.env.KEY_ID,
    process.env.ALGORITHM,
    iv.toString("base64"),
    ciphertext.toString("base64"),
].join(":");

process.stdout.write(serialized + "\n");
'
