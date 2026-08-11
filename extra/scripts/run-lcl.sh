#!/usr/bin/env bash
#
# Start the whole local stack — infra containers, every Java service, both frontends — and shut all of it down
# again on exit. Ctrl-C, SIGTERM or a fatal error all take the same path: nothing is left running.
#
#   ./extra/scripts/run-lcl.sh                    everything
#   ./extra/scripts/run-lcl.sh uaa catalog        only those, plus infra
#   ./extra/scripts/run-lcl.sh --no-infra         assume docker compose is already up
#   ./extra/scripts/run-lcl.sh --keep-infra       leave the containers running on exit
#   ./extra/scripts/run-lcl.sh --build            ./gradlew build -x test -x check first
#   ./extra/scripts/run-lcl.sh --list             show what would start, then exit
#
# Prerequisites: Docker running, and `sudo ./extra/scripts/configure-domain.sh` run once for the /etc/hosts
# entries. Ports come from common-config.yml — if you change one there, change it here too.

set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
LOG_DIR="$ROOT/build/lcl-logs"
COMPOSE_FILE="$ROOT/docker-compose-lcl.yml"
# Both profiles, always: `lcl` is the environment slice (ports, discovery, datasource) and
# `test-stores` is what seeds the demo orgs/stores and test users the local hostnames point at.
# Without the second one, uaa has no test users and the pod services have no demo store to serve.
PROFILES="lcl,test-stores"

# name | gradle module | port. Order matters: uaa issues the tokens everything else validates.
JAVA_SERVICES=(
    "uaa|:store-core:uaa|8001"
    "tenancy|:store-core:tenancy:tenancy-service|8020"
    "billing|:store-core:billing:billing-service|8021"
    "gateway|:store-core:gateway:gateway-service|8000"
    "merchant|:store-pod:merchant:merchant-service|8120"
    "catalog|:store-pod:catalog:catalog-service|8122"
    "checkout|:store-pod:checkout:checkout-service|8123"
    "payment|:store-pod:payment:payment-service|8125"
    "cua|:store-pod:cua|8124"
)

# name | directory | npm script | port | npm scripts to run first (space separated, may be empty)
#
# landing-ui's libs are consumed as built output (`main: dist/index.js`), so `npm run dev` on the app alone fails
# with "Can't resolve '@store-front/hooks/...'" until they exist. They are also what goes stale first: the app
# would happily compile against yesterday's types. `tsc -b` is incremental, so rebuilding every run is cheap and
# removes the trap entirely. Templates need no prebuild — TemplateManager loads each Next app in dev mode and
# compiles it on demand.
NODE_SERVICES=(
    "seller-ui|store-core/seller-ui|start|8010|"
    "landing-ui|store-pod/landing-ui|dev|8110|build:libs-types build:libs-services build:libs-hooks"
)

WANTED=()
START_INFRA=true
STOP_INFRA=true
DO_BUILD=false
LIST_ONLY=false

while [[ $# -gt 0 ]]; do
    case "$1" in
        --no-infra)   START_INFRA=false; STOP_INFRA=false ;;
        --keep-infra) STOP_INFRA=false ;;
        --build)      DO_BUILD=true ;;
        --list)       LIST_ONLY=true ;;
        -h|--help)    sed -n '3,14p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'; exit 0 ;;
        -*)           echo "unknown option: $1" >&2; exit 2 ;;
        *)            WANTED+=("$1") ;;
    esac
    shift
done

# --- output -------------------------------------------------------------------------------------------------

if [[ -t 1 ]]; then
    C_DIM=$'\033[2m'; C_RED=$'\033[31m'; C_GREEN=$'\033[32m'; C_YELLOW=$'\033[33m'; C_OFF=$'\033[0m'
else
    C_DIM=""; C_RED=""; C_GREEN=""; C_YELLOW=""; C_OFF=""
fi

say()  { printf '%s==>%s %s\n' "$C_GREEN" "$C_OFF" "$*"; }
warn() { printf '%s==>%s %s\n' "$C_YELLOW" "$C_OFF" "$*"; }
die()  { printf '%s==>%s %s\n' "$C_RED" "$C_OFF" "$*" >&2; exit 1; }

# --- lifecycle ----------------------------------------------------------------------------------------------

PIDS=()
NAMES=()
SHUTTING_DOWN=false
INFRA_UP=false

# A gradle launcher forks the JVM that actually holds the port, and `npm run` forks its own child, so killing
# the pid we started is not enough — the grandchild would keep the port and the next run would fail to bind.
kill_tree() {
    local pid=$1 child
    for child in $(pgrep -P "$pid" 2>/dev/null); do
        kill_tree "$child"
    done
    kill -TERM "$pid" 2>/dev/null
}

shutdown() {
    $SHUTTING_DOWN && return
    SHUTTING_DOWN=true

    if (( ${#PIDS[@]} == 0 )); then
        stop_infra
        return
    fi

    echo
    say "shutting down"

    local i pid
    for (( i=${#PIDS[@]} - 1; i >= 0; i-- )); do
        if kill -0 "${PIDS[i]}" 2>/dev/null; then
            printf '    %sstopping %s%s\n' "$C_DIM" "${NAMES[i]}" "$C_OFF"
            kill_tree "${PIDS[i]}"
        fi
    done

    # Give them a moment to close their ports, then insist.
    local deadline=$((SECONDS + 20))
    while (( SECONDS < deadline )); do
        local alive=false
        for pid in "${PIDS[@]}"; do
            kill -0 "$pid" 2>/dev/null && alive=true && break
        done
        $alive || break
        sleep 1
    done
    for pid in "${PIDS[@]}"; do
        kill -0 "$pid" 2>/dev/null && kill_tree_kill9 "$pid"
    done

    stop_infra

    say "all stopped. logs kept in $LOG_DIR"
}

# Only tear down what this run brought up: a `docker compose down` after `--no-infra`, or after dying before the
# containers ever started, would kill a stack somebody else is using.
stop_infra() {
    $STOP_INFRA || return 0
    $INFRA_UP || return 0
    say "stopping infra containers"
    docker compose -f "$COMPOSE_FILE" down --remove-orphans >/dev/null 2>&1
}

kill_tree_kill9() {
    local pid=$1 child
    for child in $(pgrep -P "$pid" 2>/dev/null); do
        kill_tree_kill9 "$child"
    done
    kill -KILL "$pid" 2>/dev/null
}

# EXIT is the one that matters — every path out, including a `die`, goes through it. INT/TERM only convert the
# signal into an exit. Note that a shell starting this script in the *background* inherits SIGINT as ignored, and
# no trap can override that; run it in the foreground, where Ctrl-C works, or send it a TERM.
trap shutdown EXIT
trap 'exit 130' INT TERM

# --- helpers ------------------------------------------------------------------------------------------------

selected() {
    (( ${#WANTED[@]} == 0 )) && return 0
    local want
    for want in "${WANTED[@]}"; do
        [[ "$want" == "$1" ]] && return 0
    done
    return 1
}

port_open() {
    (exec 3<>"/dev/tcp/127.0.0.1/$1") 2>/dev/null && exec 3>&- && return 0
    return 1
}

# Wait for a port, but give up the moment the process dies — otherwise a service that failed at startup would
# hold the script for the full timeout with nothing to show for it.
wait_for_port() {
    local name=$1 port=$2 pid=$3 timeout=${4:-180}
    local deadline=$((SECONDS + timeout))
    while (( SECONDS < deadline )); do
        if port_open "$port"; then
            printf '    %s%-14s%s up on :%s\n' "$C_GREEN" "$name" "$C_OFF" "$port"
            return 0
        fi
        if ! kill -0 "$pid" 2>/dev/null; then
            warn "$name died during startup — see $LOG_DIR/$name.log"
            tail -n 20 "$LOG_DIR/$name.log" | sed 's/^/      /'
            return 1
        fi
        sleep 2
    done
    warn "$name did not open :$port within ${timeout}s — carrying on, see $LOG_DIR/$name.log"
    return 1
}

track() {
    PIDS+=("$1")
    NAMES+=("$2")
}

# --- listing ------------------------------------------------------------------------------------------------

if $LIST_ONLY; then
    echo "infra:  $( $START_INFRA && echo "docker compose -f $(basename "$COMPOSE_FILE")" || echo "(skipped)" )"
    echo "java:"
    for entry in "${JAVA_SERVICES[@]}"; do
        IFS='|' read -r name module port <<<"$entry"
        selected "$name" && printf '    %-14s %-52s :%s\n' "$name" "$module" "$port"
    done
    echo "node:"
    for entry in "${NODE_SERVICES[@]}"; do
        IFS='|' read -r name dir script port prep <<<"$entry"
        selected "$name" || continue
        printf '    %-14s %-52s :%s\n' "$name" "$dir (npm run $script)" "$port"
        [[ -n "$prep" ]] && printf '    %s%-14s prep: npm run %s%s\n' "$C_DIM" "" "$prep" "$C_OFF"
    done
    trap - EXIT
    exit 0
fi

# --- go -----------------------------------------------------------------------------------------------------

cd "$ROOT" || die "cannot cd to $ROOT"
mkdir -p "$LOG_DIR"

command -v docker >/dev/null || die "docker not found on PATH"
docker info >/dev/null 2>&1 || die "docker is not running"

if $DO_BUILD; then
    say "building (this is the slow part)"
    ./gradlew build -x test -x check --console=plain || die "build failed"
fi

if $START_INFRA; then
    say "starting infra (postgres, spg, monitoring)"
    docker compose -f "$COMPOSE_FILE" up -d || die "docker compose failed"
    INFRA_UP=true
    printf '    %swaiting for postgres%s\n' "$C_DIM" "$C_OFF"
    for _ in $(seq 1 60); do
        port_open 5432 && break
        sleep 1
    done
    port_open 5432 || warn "postgres is not answering on :5432 — services will fail to start"
fi

say "starting java services (profiles: $PROFILES)"
for entry in "${JAVA_SERVICES[@]}"; do
    IFS='|' read -r name module port <<<"$entry"
    selected "$name" || continue

    if port_open "$port"; then
        warn "$name: :$port already in use — skipping, something else is serving it"
        continue
    fi

    ./gradlew "$module:bootRun" --args="--spring.profiles.active=$PROFILES" --console=plain \
        >"$LOG_DIR/$name.log" 2>&1 &
    track $! "$name"
    wait_for_port "$name" "$port" $!
done

say "starting frontends"
for entry in "${NODE_SERVICES[@]}"; do
    IFS='|' read -r name dir script port prep <<<"$entry"
    selected "$name" || continue

    if [[ ! -d "$ROOT/$dir/node_modules" ]]; then
        warn "$name: node_modules missing — running npm install first"
        (cd "$ROOT/$dir" && npm install) >"$LOG_DIR/$name-install.log" 2>&1 \
            || { warn "$name: npm install failed, see $LOG_DIR/$name-install.log"; continue; }
    fi

    if port_open "$port"; then
        warn "$name: :$port already in use — skipping"
        continue
    fi

    if [[ -n "$prep" ]]; then
        printf '    %sbuilding %s workspace libs%s\n' "$C_DIM" "$name" "$C_OFF"
        : >"$LOG_DIR/$name-prep.log"
        prep_failed=false
        for target in $prep; do
            (cd "$ROOT/$dir" && npm run "$target") >>"$LOG_DIR/$name-prep.log" 2>&1 || {
                warn "$name: 'npm run $target' failed — see $LOG_DIR/$name-prep.log"
                tail -n 20 "$LOG_DIR/$name-prep.log" | sed 's/^/      /'
                prep_failed=true
                break
            }
        done
        $prep_failed && continue
    fi

    (cd "$ROOT/$dir" && npm run "$script") >"$LOG_DIR/$name.log" 2>&1 &
    track $! "$name"
    wait_for_port "$name" "$port" $! 240
done

if (( ${#PIDS[@]} == 0 )); then
    warn "nothing was started"
    exit 1
fi

cat <<BANNER

$C_GREEN==>$C_OFF ${#PIDS[@]} process(es) running. Ctrl-C stops everything, containers included.

    seller console   http://gateway.com:8000
    storefront       http://org1-store1.spg-507f1f77.gateway.com
    grafana          http://localhost:3000
    logs             $LOG_DIR

BANNER

# Hold the foreground and follow the logs. If any service dies on its own, stop and let the trap clean up the
# rest — a half-running stack is worse than none, because the failure is easy to miss.
tail -n 0 -F "$LOG_DIR"/*.log 2>/dev/null &
track $! "log-tail"

while true; do
    for (( i = 0; i < ${#PIDS[@]}; i++ )); do
        [[ "${NAMES[i]}" == "log-tail" ]] && continue
        if ! kill -0 "${PIDS[i]}" 2>/dev/null; then
            warn "${NAMES[i]} exited — bringing the rest down"
            exit 1
        fi
    done
    sleep 3
done
