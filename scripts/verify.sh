#!/usr/bin/env bash
# Org-standard name for cvhome's pipeline-before-push script; the receipt and the hooks are unchanged.
exec "$(git rev-parse --show-toplevel)/extra/scripts/verify-before-push.sh" "$@"
