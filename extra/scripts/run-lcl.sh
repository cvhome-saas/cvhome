#!/usr/bin/env bash
#
# Start, stop and inspect the whole local stack — infra containers, every Java service, both frontends.
# A no-argument run is still `start`: it blocks in the foreground and shuts everything down on exit.
#
#   ./extra/scripts/run-lcl.sh [start]             everything
#   ./extra/scripts/run-lcl.sh start uaa catalog   only those, plus infra
#   ./extra/scripts/run-lcl.sh start --no-infra    assume docker compose is already up
#   ./extra/scripts/run-lcl.sh start --keep-infra  leave the containers running on exit
#   ./extra/scripts/run-lcl.sh start --build       ./gradlew build -x test -x check first
#   ./extra/scripts/run-lcl.sh start -d            start in the background and return after ports open
#   ./extra/scripts/run-lcl.sh start --list        show configured services with current running/pid status
#   ./extra/scripts/run-lcl.sh stop                stop the recorded stack, keeping compose volumes (data survives)
#   ./extra/scripts/run-lcl.sh stop --hard         stop and also delete compose volumes (full wipe)
#   ./extra/scripts/run-lcl.sh stop payment        stop only payment in the recorded stack
#   ./extra/scripts/run-lcl.sh restart [options]   stop, then start again
#   ./extra/scripts/run-lcl.sh restart -d          restart the stack in the background and return after ports open
#   ./extra/scripts/run-lcl.sh restart catalog     restart only catalog in the recorded stack
#   ./extra/scripts/run-lcl.sh logs [service...]   tail all logs, or selected service logs
#   ./extra/scripts/run-lcl.sh pid [service...]    print recorded supervisor/service pids
#
# Prerequisites: Docker running, and `sudo ./extra/scripts/configure-domain.sh` run once for the /etc/hosts
# entries. Ports come from common-config.yml — if you change one there, change it here too.

set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
LOG_DIR="$ROOT/build/lcl-logs"
RUNTIME_DIR="$ROOT/build/lcl-runtime"
SERVICE_PID_DIR="$RUNTIME_DIR/services"
RESTARTING_DIR="$RUNTIME_DIR/restarting"
STOPPED_DIR="$RUNTIME_DIR/stopped"
START_REQUEST_DIR="$RUNTIME_DIR/start-requests"
SUPERVISOR_PID_FILE="$RUNTIME_DIR/supervisor.pid"
DETACHED_LOG="$ROOT/build/lcl-stack.log"
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
    "pod-registry|:store-core:pod-registry:pod-registry-service|8022"
    "gateway|:store-core:gateway:gateway-service|8000"
    "merchant|:store-pod:merchant:merchant-service|8120"
    "content|:store-pod:content:content-service|8121"
    "catalog|:store-pod:catalog:catalog-service|8122"
    "checkout|:store-pod:checkout:checkout-service|8123"
    "payment|:store-pod:payment:payment-service|8125"
    "inventory|:store-pod:inventory:inventory-service|8126"
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
    "console-ui|store-core/console-ui|start|8011|"
    "landing-ui|store-pod/landing-ui|dev|8110|build:libs-types build:libs-services build:libs-hooks"
)

WANTED=()
START_INFRA=true
STOP_INFRA=true
DO_BUILD=false
LIST_ONLY=false
DELETE_VOLUMES=false
DETACH=false

COMMAND=start
if [[ $# -gt 0 ]]; then
    case "$1" in
        start|stop|restart|logs|pid)
            COMMAND="$1"
            shift
            ;;
    esac
fi

while [[ $# -gt 0 ]]; do
    case "$1" in
        --no-infra)   START_INFRA=false; STOP_INFRA=false ;;
        --keep-infra) STOP_INFRA=false ;;
        --build)      DO_BUILD=true ;;
        --list)       LIST_ONLY=true ;;
        -d|--detach)  DETACH=true ;;
        --hard|--volumes)
            case "$COMMAND" in
                stop|restart) DELETE_VOLUMES=true ;;
                *) echo "$1 is only valid with stop or restart" >&2; exit 2 ;;
            esac
            ;;
        -h|--help)    sed -n '3,21p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'; exit 0 ;;
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
        cleanup_runtime
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

    local name port listener
    for name in "${NAMES[@]}"; do
        [[ "$name" == "log-tail" ]] && continue
        port="$(service_port "$name" || true)"
        [[ -n "$port" ]] || continue
        for listener in $(listener_pids_on_port "$port"); do
            kill_tree_kill9 "$listener"
        done
    done

    stop_infra
    cleanup_runtime

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

wait_for_java_service() {
    local name=$1 port=$2 pid=$3 timeout=${4:-180}
    local deadline=$((SECONDS + timeout))
    while (( SECONDS < deadline )); do
        if port_open "$port"; then
            printf '    %s%-14s%s up on :%s\n' "$C_GREEN" "$name" "$C_OFF" "$port"
            return 0
        fi
        if grep -q "Started .*Application" "$LOG_DIR/$name.log" 2>/dev/null; then
            printf '    %s%-14s%s started; :%s not reachable from this shell\n' "$C_GREEN" "$name" "$C_OFF" "$port"
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

wait_for_exit() {
    local pid=$1 timeout=${2:-20}
    local deadline=$((SECONDS + timeout))
    while (( SECONDS < deadline )); do
        active_pid "$pid" || return 0
        sleep 1
    done
    return 1
}

wait_for_port_closed() {
    local name=$1 port=$2 timeout=${3:-30}
    local deadline=$((SECONDS + timeout))
    while (( SECONDS < deadline )); do
        if ! port_open "$port"; then
            printf '    %s%-14s%s closed :%s\n' "$C_DIM" "$name" "$C_OFF" "$port"
            return 0
        fi
        sleep 1
    done
    return 1
}

listener_pids_on_port() {
    local port=$1
    command -v lsof >/dev/null || return 0
    lsof -nP -t -iTCP:"$port" -sTCP:LISTEN 2>/dev/null | sort -u
}

record_listener_pid() {
    local name=$1 port=$2 listener
    listener="$(listener_pids_on_port "$port" | head -1)"
    [[ -n "$listener" ]] || return 0
    printf '%s\n' "$listener" >"$SERVICE_PID_DIR/$name.pid"
}

track() {
    PIDS+=("$1")
    NAMES+=("$2")
    [[ "$2" == "log-tail" ]] && return
    mkdir -p "$SERVICE_PID_DIR"
    printf '%s\n' "$1" >"$SERVICE_PID_DIR/$2.pid"
}

active_pid() {
    [[ "$1" =~ ^[0-9]+$ ]] && kill -0 "$1" 2>/dev/null
}

supervisor_pid() {
    local pid
    [[ -f "$SUPERVISOR_PID_FILE" ]] || return 1
    read -r pid <"$SUPERVISOR_PID_FILE" || return 1
    active_pid "$pid" || return 1
    printf '%s\n' "$pid"
}

cleanup_runtime() {
    rm -f "$SUPERVISOR_PID_FILE"
    rm -rf "$SERVICE_PID_DIR"
    rm -rf "$RESTARTING_DIR"
    rm -rf "$STOPPED_DIR"
    rm -rf "$START_REQUEST_DIR"
}

cleanup_logs() {
    rm -rf "$LOG_DIR"
}

cleanup_stale_runtime() {
    local pid file
    [[ -d "$SERVICE_PID_DIR" ]] || return 0
    for file in "$SERVICE_PID_DIR"/*.pid; do
        [[ -e "$file" ]] || continue
        read -r pid <"$file" || pid=""
        [[ "$pid" =~ ^[0-9]+$ ]] || rm -f "$file"
    done
    rmdir "$SERVICE_PID_DIR" 2>/dev/null || true
}

compose_down() {
    command -v docker >/dev/null || { warn "docker not found on PATH; skipping infra cleanup"; return 0; }
    docker info >/dev/null 2>&1 || { warn "docker is not running; skipping infra cleanup"; return 0; }
    # Volumes survive a plain stop so postgres/minio data persists across runs; `stop --hard` wipes them.
    if $DELETE_VOLUMES; then
        say "stopping infra containers and deleting volumes"
        docker compose -f "$COMPOSE_FILE" down --remove-orphans -v
    else
        say "stopping infra containers (volumes kept)"
        docker compose -f "$COMPOSE_FILE" down --remove-orphans
    fi
}

# Services can outlive their supervisor (crash, closed terminal, killed pid file) and keep their ports. A stop
# that only tears down the supervisor would leave them orphaned, and the next start would skip every busy port —
# so always sweep the known service ports for leftover listeners.
kill_stray_service_listeners() {
    local entry name port listener swept=false
    for entry in "${JAVA_SERVICES[@]}" "${NODE_SERVICES[@]}"; do
        IFS='|' read -r name _ <<<"$entry"
        port="$(service_port "$name")" || continue
        for listener in $(listener_pids_on_port "$port"); do
            $swept || say "stopping stray service processes from a previous run"
            swept=true
            printf '    %sstopping %s (pid %s) on :%s%s\n' "$C_DIM" "$name" "$listener" "$port" "$C_OFF"
            kill_tree "$listener"
        done
    done
    $swept || return 0

    for entry in "${JAVA_SERVICES[@]}" "${NODE_SERVICES[@]}"; do
        IFS='|' read -r name _ <<<"$entry"
        port="$(service_port "$name")" || continue
        wait_for_port_closed "$name" "$port" 20 && continue
        for listener in $(listener_pids_on_port "$port"); do
            kill_tree_kill9 "$listener"
        done
        wait_for_port_closed "$name" "$port" 10 || warn "$name is still holding :$port"
    done
}

stop_recorded_stack() {
    cleanup_stale_runtime

    local pid=""
    if pid="$(supervisor_pid)"; then
        say "stopping local stack supervisor ($pid)"
        kill -TERM "$pid" 2>/dev/null || true

        local deadline=$((SECONDS + 60))
        while (( SECONDS < deadline )); do
            active_pid "$pid" || break
            sleep 1
        done

        if active_pid "$pid"; then
            warn "supervisor $pid did not stop within 60s"
            return 1
        fi
        cleanup_runtime
    else
        warn "no recorded local stack supervisor is running"
        cleanup_stale_runtime
    fi

    kill_stray_service_listeners
    compose_down
    cleanup_runtime
    cleanup_logs
    return 0
}

print_pid_status() {
    cleanup_stale_runtime

    if (( ${#WANTED[@]} == 0 )); then
        local pid
        if pid="$(supervisor_pid)"; then
            printf 'supervisor %s running\n' "$pid"
        else
            printf 'supervisor - stopped\n'
        fi
    fi

    local found=false entry name file pid
    for entry in "${JAVA_SERVICES[@]}" "${NODE_SERVICES[@]}"; do
        IFS='|' read -r name _ <<<"$entry"
        selected "$name" || continue
        found=true
        file="$SERVICE_PID_DIR/$name.pid"
        if [[ -f "$file" ]]; then
            read -r pid <"$file" || pid=""
            if active_pid "$pid"; then
                printf '%-14s %s running\n' "$name" "$pid"
            else
                printf '%-14s - stopped\n' "$name"
            fi
        else
            printf '%-14s - stopped\n' "$name"
        fi
    done

    $found || (( ${#WANTED[@]} == 0 )) || die "no matching service names: ${WANTED[*]}"
}

service_runtime_status() {
    local name=$1 port=$2 pid listener
    if pid="$(recorded_service_pid "$name")" && active_pid "$pid"; then
        printf 'running %s' "$pid"
        return 0
    fi

    listener="$(listener_pids_on_port "$port" | head -1)"
    if [[ -n "$listener" ]]; then
        printf 'port-used %s' "$listener"
        return 0
    fi

    printf 'stopped'
}

print_list() {
    cleanup_stale_runtime
    validate_wanted_services

    local pid status entry name module port dir script prep
    if pid="$(supervisor_pid)"; then
        printf 'supervisor: running %s\n' "$pid"
    else
        printf 'supervisor: stopped\n'
    fi

    echo "infra:      $( $START_INFRA && echo "docker compose -f $(basename "$COMPOSE_FILE")" || echo "(skipped)" )"
    echo "java:"
    for entry in "${JAVA_SERVICES[@]}"; do
        IFS='|' read -r name module port <<<"$entry"
        selected "$name" || continue
        status="$(service_runtime_status "$name" "$port")"
        printf '    %-14s %-18s %-52s :%s\n' "$name" "$status" "$module" "$port"
    done
    echo "node:"
    for entry in "${NODE_SERVICES[@]}"; do
        IFS='|' read -r name dir script port prep <<<"$entry"
        selected "$name" || continue
        status="$(service_runtime_status "$name" "$port")"
        printf '    %-14s %-18s %-52s :%s\n' "$name" "$status" "$dir (npm run $script)" "$port"
        [[ -n "$prep" ]] && printf '    %s%-14s %-18s prep: npm run %s%s\n' "$C_DIM" "" "" "$prep" "$C_OFF"
    done
}

tail_logs() {
    local files=() name
    if (( ${#WANTED[@]} == 0 )); then
        files=("$LOG_DIR"/*.log)
    else
        for name in "${WANTED[@]}"; do
            files+=("$LOG_DIR/$name.log")
        done
    fi

    local existing=() file
    for file in "${files[@]}"; do
        [[ -e "$file" ]] && existing+=("$file")
    done
    (( ${#existing[@]} > 0 )) || die "no matching logs in $LOG_DIR"

    tail -F "${existing[@]}"
}

known_service() {
    local expected=$1 entry name
    for entry in "${JAVA_SERVICES[@]}" "${NODE_SERVICES[@]}"; do
        name=""
        IFS='|' read -r name _ <<<"$entry"
        [[ "$name" == "$expected" ]] && return 0
    done
    return 1
}

validate_wanted_services() {
    local name
    (( ${#WANTED[@]} == 0 )) && return 0
    for name in "${WANTED[@]}"; do
        known_service "$name" || die "unknown service: $name"
    done
}

service_port() {
    local expected=$1 entry name port
    for entry in "${JAVA_SERVICES[@]}"; do
        name=""; port=""
        IFS='|' read -r name _ port <<<"$entry"
        [[ "$name" == "$expected" ]] && { printf '%s\n' "$port"; return 0; }
    done
    for entry in "${NODE_SERVICES[@]}"; do
        name=""; port=""
        IFS='|' read -r name _ _ port _ <<<"$entry"
        [[ "$name" == "$expected" ]] && { printf '%s\n' "$port"; return 0; }
    done
    return 1
}

recorded_service_pid() {
    local name=${1:-} file pid
    [[ -n "$name" ]] || return 1
    file="$SERVICE_PID_DIR/$name.pid"
    [[ -f "$file" ]] || return 1
    read -r pid <"$file" || return 1
    [[ "$pid" =~ ^[0-9]+$ ]] || return 1
    printf '%s\n' "$pid"
}

recorded_service_pid_raw() {
    local name=${1:-} file pid
    [[ -n "$name" ]] || return 1
    file="$SERVICE_PID_DIR/$name.pid"
    [[ -f "$file" ]] || return 1
    read -r pid <"$file" || return 1
    [[ "$pid" =~ ^[0-9]+$ ]] || return 1
    printf '%s\n' "$pid"
}

stop_recorded_service() {
    local name=$1 port=$2 pid listener
    mkdir -p "$RESTARTING_DIR"
    : >"$RESTARTING_DIR/$name"
    if pid="$(recorded_service_pid_raw "$name")"; then
        say "stopping $name ($pid)"
        kill_tree "$pid"
    else
        warn "$name is not recorded as running"
    fi

    if ! wait_for_port_closed "$name" "$port" 20; then
        for listener in $(listener_pids_on_port "$port"); do
            warn "$name: :$port still held by pid $listener — stopping listener"
            kill_tree "$listener"
        done
        wait_for_port_closed "$name" "$port" 20 || {
            for listener in $(listener_pids_on_port "$port"); do
                kill_tree_kill9 "$listener"
            done
            wait_for_port_closed "$name" "$port" 10
        } || warn "$name did not close :$port before restart"
    fi

    rm -f "$SERVICE_PID_DIR/$name.pid"
}

start_java_service() {
    local name=$1 module=$2 port=$3
    local pid

    if port_open "$port"; then
        warn "$name: :$port already in use — skipping, something else is serving it"
        return 0
    fi

    nohup bash -c 'trap "" HUP; exec "$@"' _ \
        ./gradlew "$module:bootRun" --args="--spring.profiles.active=$PROFILES" --console=plain \
        >"$LOG_DIR/$name.log" 2>&1 </dev/null &
    pid=$!
    track "$pid" "$name"
    disown "$pid" 2>/dev/null || true
    if wait_for_java_service "$name" "$port" "$pid"; then
        record_listener_pid "$name" "$port"
        rm -f "$STOPPED_DIR/$name"
    fi
}

start_node_service() {
    local name=$1 dir=$2 script=$3 port=$4 prep=$5
    local pid

    if [[ ! -d "$ROOT/$dir/node_modules" ]]; then
        warn "$name: node_modules missing — running npm install first"
        (cd "$ROOT/$dir" && npm install) >"$LOG_DIR/$name-install.log" 2>&1 \
            || { warn "$name: npm install failed, see $LOG_DIR/$name-install.log"; return 0; }
    fi

    if port_open "$port"; then
        warn "$name: :$port already in use — skipping"
        return 0
    fi

    if [[ -n "$prep" ]]; then
        printf '    %sbuilding %s workspace libs%s\n' "$C_DIM" "$name" "$C_OFF"
        : >"$LOG_DIR/$name-prep.log"
        local target prep_failed=false
        for target in $prep; do
            (cd "$ROOT/$dir" && npm run "$target") >>"$LOG_DIR/$name-prep.log" 2>&1 || {
                warn "$name: 'npm run $target' failed — see $LOG_DIR/$name-prep.log"
                tail -n 20 "$LOG_DIR/$name-prep.log" | sed 's/^/      /'
                prep_failed=true
                break
            }
        done
        $prep_failed && return 0
    fi

    (cd "$ROOT/$dir" && nohup bash -c 'trap "" HUP; exec "$@"' _ \
        npm run "$script" >"$LOG_DIR/$name.log" 2>&1 </dev/null) &
    pid=$!
    track "$pid" "$name"
    disown "$pid" 2>/dev/null || true
    wait_for_port "$name" "$port" "$pid" 240 && rm -f "$STOPPED_DIR/$name"
}

start_selected_services() {
    local entry name module port dir script prep
    local failed=false

    say "starting java services (profiles: $PROFILES)"
    for entry in "${JAVA_SERVICES[@]}"; do
        IFS='|' read -r name module port <<<"$entry"
        selected "$name" || continue
        start_java_service "$name" "$module" "$port" || failed=true
    done

    say "starting frontends"
    for entry in "${NODE_SERVICES[@]}"; do
        IFS='|' read -r name dir script port prep <<<"$entry"
        selected "$name" || continue
        start_node_service "$name" "$dir" "$script" "$port" "$prep" || failed=true
    done

    ! $failed
}

start_services_in_recorded_stack() {
    validate_wanted_services
    [[ -f "$SUPERVISOR_PID_FILE" ]] || die "no recorded local stack supervisor is running"

    cd "$ROOT" || die "cannot cd to $ROOT"
    mkdir -p "$START_REQUEST_DIR"

    local name
    for name in "${WANTED[@]}"; do
        : >"$START_REQUEST_DIR/$name"
    done
    say "requested start for: ${WANTED[*]}"

    local deadline=$((SECONDS + 300))
    while (( SECONDS < deadline )); do
        if selected_ports_open; then
            say "ready: ${WANTED[*]}"
            return 0
        fi
        supervisor_pid >/dev/null 2>&1 || die "recorded local stack supervisor stopped before services were ready"
        sleep 1
    done

    warn "selected services did not open their ports within 300s"
    print_list
    return 1
}

restart_selected_services() {
    validate_wanted_services
    $DELETE_VOLUMES && die "--hard cannot be used when restarting selected services"
    [[ -f "$SUPERVISOR_PID_FILE" ]] || die "no recorded local stack supervisor is running"

    cd "$ROOT" || die "cannot cd to $ROOT"
    mkdir -p "$LOG_DIR" "$SERVICE_PID_DIR"
    port_open 5432 || warn "postgres is not answering on :5432 — selected services may fail during startup"

    if $DO_BUILD; then
        say "building (this is the slow part)"
        ./gradlew build -x test -x check --console=plain || die "build failed"
    fi

    local name
    for name in "${WANTED[@]}"; do
        rm -f "$STOPPED_DIR/$name"
        stop_recorded_service "$name" "$(service_port "$name")"
    done

    start_services_in_recorded_stack
}

stop_selected_services() {
    validate_wanted_services
    $DELETE_VOLUMES && die "--hard cannot be used when stopping selected services"
    [[ -f "$SUPERVISOR_PID_FILE" ]] || die "no recorded local stack supervisor is running"

    cd "$ROOT" || die "cannot cd to $ROOT"
    mkdir -p "$SERVICE_PID_DIR" "$STOPPED_DIR"

    local name
    for name in "${WANTED[@]}"; do
        stop_recorded_service "$name" "$(service_port "$name")"
        rm -f "$RESTARTING_DIR/$name"
        : >"$STOPPED_DIR/$name"
    done
}

start_service_by_name() {
    local expected=$1 entry name module port dir script prep
    for entry in "${JAVA_SERVICES[@]}"; do
        IFS='|' read -r name module port <<<"$entry"
        if [[ "$name" == "$expected" ]]; then
            rm -f "$STOPPED_DIR/$name"
            : >"$RESTARTING_DIR/$name"
            start_java_service "$name" "$module" "$port"
            rm -f "$RESTARTING_DIR/$name"
            return 0
        fi
    done
    for entry in "${NODE_SERVICES[@]}"; do
        IFS='|' read -r name dir script port prep <<<"$entry"
        if [[ "$name" == "$expected" ]]; then
            rm -f "$STOPPED_DIR/$name"
            : >"$RESTARTING_DIR/$name"
            start_node_service "$name" "$dir" "$script" "$port" "$prep"
            rm -f "$RESTARTING_DIR/$name"
            return 0
        fi
    done
    warn "unknown service start request: $expected"
    return 1
}

process_start_requests() {
    local request name
    [[ -d "$START_REQUEST_DIR" ]] || return 0
    for request in "$START_REQUEST_DIR"/*; do
        [[ -e "$request" ]] || continue
        name="$(basename "$request")"
        rm -f "$request"
        start_service_by_name "$name" || true
    done
    rmdir "$START_REQUEST_DIR" 2>/dev/null || true
}

selected_ports_open() {
    local entry name port
    for entry in "${JAVA_SERVICES[@]}"; do
        name=""; port=""
        IFS='|' read -r name _ port <<<"$entry"
        selected "$name" || continue
        port_open "$port" || return 1
    done
    for entry in "${NODE_SERVICES[@]}"; do
        name=""; port=""
        IFS='|' read -r name _ _ port _ <<<"$entry"
        selected "$name" || continue
        port_open "$port" || return 1
    done
    return 0
}

detached_child_args() {
    local args=("$COMMAND")
    if ! $START_INFRA; then
        args+=(--no-infra)
    elif ! $STOP_INFRA; then
        args+=(--keep-infra)
    fi
    $DO_BUILD && args+=(--build)
    $DELETE_VOLUMES && args+=(--hard)
    if (( ${#WANTED[@]} > 0 )); then
        args+=("${WANTED[@]}")
    fi
    printf '%s\0' "${args[@]}"
}

start_detached() {
    validate_wanted_services
    case "$COMMAND" in
        start)
            if (( ${#WANTED[@]} > 0 )) && supervisor_pid >/dev/null 2>&1; then
                start_services_in_recorded_stack
                return $?
            fi
            ;;
        restart)
            (( ${#WANTED[@]} == 0 )) || return 1
            ;;
        *) return 1 ;;
    esac

    local args=() child_pid supervisor deadline
    while IFS= read -r -d '' arg; do
        args+=("$arg")
    done < <(detached_child_args)

    mkdir -p "$(dirname "$DETACHED_LOG")"
    : >"$DETACHED_LOG"
    RUN_LCL_DETACHED_CHILD=1 nohup bash -c 'trap "" HUP; exec "$@"' _ \
        "${BASH_SOURCE[0]}" "${args[@]+"${args[@]}"}" >"$DETACHED_LOG" 2>&1 </dev/null &
    child_pid=$!
    disown "$child_pid" 2>/dev/null || true

    say "starting in background ($child_pid); log: $DETACHED_LOG"
    deadline=$((SECONDS + 900))
    while (( SECONDS < deadline )); do
        if supervisor="$(supervisor_pid 2>/dev/null)"; then
            if selected_ports_open; then
                say "ready. supervisor $supervisor"
                return 0
            fi
        fi
        if ! active_pid "$child_pid" && ! supervisor_pid >/dev/null 2>&1; then
            warn "background start exited before services were ready — see $DETACHED_LOG"
            tail -n 40 "$DETACHED_LOG" | sed 's/^/      /'
            return 1
        fi
        sleep 2
    done

    warn "background start did not finish within 900s — see $DETACHED_LOG"
    print_list
    return 1
}

# --- listing ------------------------------------------------------------------------------------------------

if $DETACH && [[ "${RUN_LCL_DETACHED_CHILD:-}" != "1" ]] && ! $LIST_ONLY; then
    if start_detached; then
        exit 0
    fi
    if [[ "$COMMAND" == "restart" ]] && (( ${#WANTED[@]} > 0 )); then
        warn "-d is not needed for selected service restart; running it in the foreground"
    else
        warn "-d is only supported with start or full restart"
        exit 1
    fi
fi

case "$COMMAND" in
    start)
        if ! $LIST_ONLY && (( ${#WANTED[@]} > 0 )) && supervisor_pid >/dev/null 2>&1; then
            start_services_in_recorded_stack || exit 1
            exit 0
        fi
        ;;
    stop)
        if (( ${#WANTED[@]} > 0 )); then
            stop_selected_services || exit 1
            exit 0
        fi
        stop_recorded_stack
        exit 0
        ;;
    restart)
        if (( ${#WANTED[@]} > 0 )) && ! $LIST_ONLY; then
            restart_selected_services || exit 1
            exit 0
        fi
        if ! $LIST_ONLY; then
            stop_recorded_stack || die "cannot restart until the existing stack stops"
        fi
        DELETE_VOLUMES=false
        ;;
    logs)
        tail_logs
        exit 0
        ;;
    pid)
        print_pid_status
        exit 0
        ;;
esac

if $LIST_ONLY; then
    print_list
    exit 0
fi

# --- go -----------------------------------------------------------------------------------------------------

# EXIT is the one that matters — every path out of the supervisor, including a `die`, goes through it. INT/TERM
# only convert the signal into an exit. `run-lcl.sh stop` sends TERM to the recorded supervisor.
trap shutdown EXIT
trap 'exit 130' INT TERM

cd "$ROOT" || die "cannot cd to $ROOT"

if pid="$(supervisor_pid)"; then
    warn "local stack already recorded under supervisor $pid — stopping it first"
    stop_recorded_stack || die "existing local stack did not stop"
else
    cleanup_runtime
    cleanup_logs
    if $START_INFRA; then
        compose_down
    fi
fi

mkdir -p "$LOG_DIR" "$SERVICE_PID_DIR"

printf '%s\n' "$$" >"$SUPERVISOR_PID_FILE"

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

start_selected_services

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
    process_start_requests
    for (( i = 0; i < ${#PIDS[@]}; i++ )); do
        name="${NAMES[i]:-}"
        [[ "$name" == "log-tail" ]] && continue
        [[ -n "$name" ]] || continue
        if ! kill -0 "${PIDS[i]}" 2>/dev/null; then
            port="$(service_port "$name" || true)"
            adopt_deadline=$((SECONDS + 45))
            while (( SECONDS < adopt_deadline )); do
                if [[ -f "$STOPPED_DIR/$name" ]]; then
                    continue 2
                fi
                if [[ -n "$port" ]] && port_open "$port"; then
                    record_listener_pid "$name" "$port"
                    if refreshed_pid="$(recorded_service_pid "$name")"; then
                        PIDS[i]="$refreshed_pid"
                    fi
                    rm -f "$RESTARTING_DIR/$name"
                    continue 2
                fi
                if refreshed_pid="$(recorded_service_pid "$name")" && [[ "$refreshed_pid" != "${PIDS[i]}" ]]; then
                    PIDS[i]="$refreshed_pid"
                    rm -f "$RESTARTING_DIR/$name"
                    continue 2
                fi
                sleep 1
            done
            warn "$name exited — bringing the rest down"
            exit 1
        fi
    done
    sleep 3
done
