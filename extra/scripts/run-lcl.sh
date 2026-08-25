#!/usr/bin/env bash
#
# Compatibility shim. The local stack runner is now `extra/scripts/lcl` (source in extra/lcl, TypeScript on Node):
# several named stacks side by side (--stack xxx), ports shifted automatically when taken, health, audit and
# troubleshooting commands. Run `./extra/scripts/lcl --help`.
#
# Old spellings still work: start|stop|restart|logs|pid, --no-infra, --keep-infra, --build, -d, --list, --hard.

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
LCL="$ROOT/extra/scripts/lcl"

args=()
for arg in "$@"; do
    case "$arg" in
        --list) args+=(ports) ;;              # `run-lcl.sh --list` → `lcl ports`
        --volumes) args+=(--hard) ;;
        pid) args+=(status) ;;
        gateway) args+=(store-core-gateway) ;;
        *) args+=("$arg") ;;
    esac
done
[[ ${#args[@]} -gt 0 ]] || args=(start)
# `start --list` used to mean "show the table": drop the command when the table was asked for
if [[ " ${args[*]} " == *" ports "* ]]; then
    filtered=()
    for a in "${args[@]}"; do [[ "$a" == "start" ]] || filtered+=("$a"); done
    args=("${filtered[@]}")
fi

printf '\033[2mrun-lcl.sh is a shim; use ./extra/scripts/lcl %s\033[0m\n' "${args[*]}" >&2
exec "$LCL" "${args[@]}"
