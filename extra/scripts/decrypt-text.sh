#!/bin/sh
set -eu

# Decrypts an EncryptedValue-serialized string produced by encrypt-text.sh,
# compatible with LocalAesCryptoProvider / EncryptedValue (com.asrevo.cvhome.crypto):
# "ENC:<version>:<keyId>:<algorithm>:<ivB64>:<ciphertextB64>" where ciphertextB64
# is the GCM ciphertext with the 16-byte auth tag appended. The key-id embedded in
# the serialized value is used to locate the key file under keys-dir.

usage() {
    echo "Usage: $0 <serialized> [keys-dir]" >&2
    echo "Prints the decrypted plaintext to stdout." >&2
    exit 1
}

[ $# -ge 1 ] || usage

SERIALIZED="$1"
KEYS_DIR="${2:-$HOME/.cvhome/secret-crypto/keys}"

case "$SERIALIZED" in
    ENC:*) ;;
    *) echo "Value is not encrypted or missing prefix: $SERIALIZED" >&2; exit 1 ;;
esac

command -v node >/dev/null 2>&1 || { echo "node is required but was not found" >&2; exit 1; }

SERIALIZED="$SERIALIZED" KEYS_DIR="$KEYS_DIR" node -e '
const crypto = require("crypto");
const fs = require("fs");
const path = require("path");

const serialized = process.env.SERIALIZED;
const content = serialized.substring("ENC:".length);
const parts = content.split(":");
if (parts.length !== 5) {
    console.error("Invalid serialized EncryptedValue (expected 5 parts): " + serialized);
    process.exit(1);
}
const [version, keyId, algorithm, ivB64, ctB64] = parts;

if (algorithm !== "AES-256-GCM") {
    console.error("Unsupported algorithm: " + algorithm);
    process.exit(1);
}

const keyFile = path.join(process.env.KEYS_DIR, keyId);
if (!fs.existsSync(keyFile)) {
    console.error("Key file not found: " + keyFile);
    process.exit(1);
}
const key = fs.readFileSync(keyFile);

const iv = Buffer.from(ivB64, "base64");
const ct = Buffer.from(ctB64, "base64");
const tag = ct.subarray(ct.length - 16);
const ciphertext = ct.subarray(0, ct.length - 16);

const decipher = crypto.createDecipheriv("aes-256-gcm", key, iv);
decipher.setAuthTag(tag);
const plaintext = Buffer.concat([decipher.update(ciphertext), decipher.final()]);

process.stdout.write(plaintext.toString("utf8") + "\n");
'
