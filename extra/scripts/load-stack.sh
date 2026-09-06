#!/usr/bin/env bash
# The load stack: every service as its built image, one container each, LOAD_MEM (1g) per container.
# See docker-compose-load.yml for what it is and why.
#
#   extra/scripts/load-stack.sh build            build the fourteen images (./gradlew bootBuildImage)
#   extra/scripts/load-stack.sh up               start everything, wait until every Java service answers /actuator/health
#   extra/scripts/load-stack.sh down [--hard]    stop; --hard also removes the volumes (fresh database next time)
#   extra/scripts/load-stack.sh ps | logs [svc]  what is running / its logs
#   extra/scripts/load-stack.sh stats            memory and CPU per container, once
#
# Knobs (environment): LOAD_MEM=1g  LOAD_POOL_SIZE=10  LOAD_TAG=latest  LOAD_REGISTRY=  OTEL_SDK_DISABLED=false
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$root"
compose=(docker compose -p cvhome-load -f docker-compose-lcl.yml -f docker-compose-load.yml)

java_services=(uaa store-core-gateway tenancy billing pod-registry merchant content catalog checkout cua payment inventory)
# A function, not an associative array: macOS ships bash 3.2.
port_of() {
  case "$1" in
    uaa) echo 8001 ;; store-core-gateway) echo 8000 ;; tenancy) echo 8020 ;; billing) echo 8021 ;; pod-registry) echo 8022 ;;
    merchant) echo 8120 ;; content) echo 8121 ;; catalog) echo 8122 ;; checkout) echo 8123 ;; cua) echo 8124 ;;
    payment) echo 8125 ;; inventory) echo 8126 ;; landing-ui) echo 8110 ;; console-ui) echo 8011 ;; spg) echo 80 ;;
  esac
}

wait_healthy() {
  local deadline=$((SECONDS + ${LOAD_WAIT:-600})) pending
  while :; do
    pending=()
    for s in "${java_services[@]}"; do
      curl -sf "http://localhost:$(port_of "$s")/actuator/health" 2>/dev/null | grep -q '"status":"UP"' || pending+=("$s")
    done
    if [ ${#pending[@]} -eq 0 ]; then echo "==> every Java service is UP"; return 0; fi
    if [ $SECONDS -ge $deadline ]; then echo "!! still not UP after ${LOAD_WAIT:-600}s: ${pending[*]}" >&2; return 1; fi
    printf '    waiting for %s\n' "${pending[*]}"
    sleep 10
  done
}

case "${1:-}" in
  build)
    ./gradlew bootBuildImage -x test -x check --console=plain
    ;;
  up)
    if lcl list 2>/dev/null | grep -Eq 'running [0-9]+'; then
      echo "!! an lcl stack is running on these ports; stop it first (lcl stop --stack <name>)" >&2; exit 1
    fi
    "${compose[@]}" up -d
    wait_healthy
    for s in landing-ui console-ui spg; do
      curl -s -o /dev/null -w "    $s %{http_code}\n" "http://localhost:$(port_of "$s")/" || true
    done
    echo "==> http://gateway.com:8000  http://spg-507f1f77.gateway.com  http://localhost:3000 (Grafana)"
    ;;
  down)
    if [ "${2:-}" = "--hard" ]; then "${compose[@]}" down -v; else "${compose[@]}" down; fi
    ;;
  ps)
    "${compose[@]}" ps
    ;;
  logs)
    shift; "${compose[@]}" logs --tail=200 "$@"
    ;;
  stats)
    docker stats --no-stream --format 'table {{.Name}}\t{{.MemUsage}}\t{{.MemPerc}}\t{{.CPUPerc}}' | grep -E 'NAME|cvhome-load' | sed 's/cvhome-load-//'
    ;;
  *)
    sed -n '2,12p' "$0"; exit 2
    ;;
esac
