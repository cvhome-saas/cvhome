# QA — the local stack (`lcl`)

The public `@cvhome-saas/lcl` package owns local stack startup, shutdown, per-service recovery, health, logs,
audit and isolation between named stacks. These cases prove it manages the full stack, single services, and
several stacks at once (`--stack xxx`) without touching each other.

**This is the one QA document that does not live beside a service**, because it belongs to no service: it
covers the CLI, `lcl.yml`, the Docker infra and the port sequences that every other QA document depends on.
Every other file is `<service>/qa/<module>-qa.md` — see
[`references/qa-testing.md`](../.claude/skills/project-structure/references/qa-testing.md) §7.

- **Scope** — public `cvhome-saas/lcl` engine, `lcl.yml` (project), `docker-compose-lcl.yml`,
  `store-pod/spg/Caddyfile` (`{$LCL_PORT_*}`), local Docker infra, Java services, frontends
- **Change** — rewrite of the bash supervisor as a TypeScript multi-stack runner with dynamic port sequences
- **Cases** — 15
- **Also see** — [spg](../store-pod/spg/qa/spg-qa.md) (case 09's `X-Forwarded-Port` observation is asserted
  there as HDR-01), [uaa](../store-core/uaa/qa/uaa-qa.md) (case 09's redirect patching is AUT-08),
  [inventory](../store-pod/inventory/inventory-service/qa/inventory-qa.md) (case 06 is the fix for a
  regression in its REG table)

Each case is tagged **[verified]** (run against this runner and this `lcl.yml`, passed) or **[not verified]**.
The verified runs were done on `feat/external-lcl-cli`, which carries the same runner and an equivalent
`lcl.yml`; the copy of `lcl.yml` on this branch has been validated (`lcl validate`, `lcl doctor`) but not yet
started, so treat the tags as "known to work for this configuration", not as a run of this branch.

---

## 00 — Before you start

Run from the repository root. Docker must be running; hosts file configured. Every command below takes
`--stack <name>`; without it the `default` stack is meant.

```bash
sudo ./extra/scripts/configure-domain.sh
lcl doctor            # every line ✓ (a "!" about ports in use is fine)
lcl ports             # configured ports from lcl.yml
```

Probes:

```bash
lcl status
lcl list
lcl events | tail
docker compose ls                     # one project per stack: lcl-<stack>
lsof -nP -iTCP:8122 -sTCP:LISTEN
```

Stop through the tool (`lcl stop`), never with a manual `kill`.

---

## 01 — Ports and services come from `lcl.yml` [verified]

- **Steps** — `lcl ports`; temporarily change `catalog`'s `ports.http` in `lcl.yml`; `lcl ports` again; revert.
- **Expect** — the table lists 14 services + spg + infra with the configured ports; the changed port shows up
  without touching the tool. `lcl validate` checks `lcl.yml` against schema v1 and `lcl doctor` checks the machine (Docker, `/etc/hosts`, working directories, ports).

## 02 — Full start in the background, health, urls [verified]

- **Steps** — `lcl start -d --parallel 3`.
- **Expect** — infra `minio postgres spg` up under project `lcl-default`; every service reported `up on :<port>`
  as it becomes healthy; the final `status` table shows 14 × `up` with `UP` health for Java services;
  `lcl urls` prints `http://gateway.com:8000`, the storefront, minio, postgres. `curl -sI http://gateway.com:8000/`
  → 200 and `curl -sL http://org1-store1.spg-507f1f77.gateway.com/` → 200.

## 03 — Foreground start and Ctrl-C [verified]

- **Steps** — `lcl stop`; `lcl start uaa --infra postgres` in a terminal; wait for `uaa up`; press Ctrl-C.
- **Expect** — the supervisor prints transitions live, then `shutting down` … `all stopped`; `lcl status` says
  `supervisor stopped`; `docker compose ls` has no `lcl-default` project; `:8001` free.

## 04 — Stop whole stack [verified]

- **Steps** — with a running stack, `lcl stop`.
- **Expect** — services stopped in reverse order, containers down (volumes kept), registry entry removed,
  `events` ends with `instance.stopped`. `lcl stop --hard` additionally runs `compose down -v`.

## 05 — Restart whole stack [verified]

- **Steps** — `lcl restart -d`.
- **Expect** — old supervisor gone, new supervisor pid, all services `up`, same ports.

> ### Known gap — `lcl restart` of the whole stack can leave half of it `crashed`
>
> **Symptom.** Six or so services exit 1 within a minute of each other, all with the same Gradle failure:
>
> ```
> Could not create service of type FileAccessTimeJournal …
>   Timeout waiting to lock journal cache (~/.gradle/caches/journal-1).
>   It is currently in use by another process. Owner PID: …
> ```
>
> `lcl status` shows them `crashed`, and everything downstream reads `blocked by dependency` — `landing-ui` waits
> on content/catalog/checkout/inventory, so **the storefronts 502 while the platform services look fine**.
>
> **Cause.** Every service runs its own `./gradlew … bootRun`, and each Gradle client needs the *shared*
> `~/.gradle/caches/journal-1` lock at startup. `--project-cache-dir` is per stack; the Gradle user home is not.
> A restart stops the services but the Gradle daemons they spawned outlive the build, so a restart briefly has the
> old daemons and the new clients competing. The pod services have no ordering between them — they all depend only
> on uaa and postgres — so six start at once and lose the race. The lock timeout is
> `DefaultFileLockManager.DEFAULT_LOCK_TIMEOUT`, a hard-coded 60 s constant with no system property behind it, so
> it cannot be raised.
>
> **What works.**
>
> ```bash
> lcl stop --stack <name>
> ./gradlew --stop          # only if no other stack is running: this stops every daemon in the shared user home
> lcl start -d --stack <name>
> ```
>
> A cold `lcl start` is reliable because there are no leftover daemons. `lcl start --parallel 1` does **not**
> recover an already-crashed service — `start` skips services in a terminal state, and `lcl restart <svc>` on one
> does not retry either; the events log still shows the original crash. Stop and start the stack.
>
> **Do not** run `./gradlew --stop` while another worktree's stack is up: daemons are shared across stacks, and a
> `bootRun` daemon never finishes on its own, so stopping them takes that stack's services down too.
>
> **Not fixed in `lcl.yml`.** The available levers are all worse than the problem: a per-stack `GRADLE_USER_HOME`
> gives every stack its own multi-gigabyte dependency cache, a daemon-stopping `before-start`/`after-stop` hook
> would kill other worktrees' stacks, and a sleep-based stagger is a guess dressed as configuration. Recorded here
> instead.

## 05b — Rebuilding a shared library under a running stack [verified]

Gradle writes `store-commons/*/build/libs/*.jar` **in place**, and every Java service opened that jar when it
started. Replacing it under a live JVM does not reload it: classes already loaded keep working, and the first
class loaded *lazily* afterwards fails with a `NoClassDefFoundError` for a class that is plainly there —

```
java.lang.IllegalArgumentException: Failed to evaluate expression 'hasPermission(...)'
  Caused by: java.lang.NoClassDefFoundError: com/asrevo/cvhome/s2s/utils/SecurityUtils
    at StoreRoleAccessChecker.wrongRealm(...)  [autoconfigure-1.0.16.jar]
  Caused by: java.lang.ClassNotFoundException: com.asrevo.cvhome.s2s.utils.SecurityUtils
```

- **How it presents** — a 500 `COMMON.INTERNAL_ERROR` on one page, with the rest of the console working.
  It was the catalogue, because `wrongRealm` is on the org-admin path and nothing had reached it since the
  rebuild. It reads like a permission bug and is not one: the class is in the source *and* in the jar. Compare
  the jar's mtime with the service's uptime (`lcl status`) and the answer is immediate.
- **Steps** — start the stack, edit anything in `store-commons/autoconfigure`, `./gradlew :…:build`, then use a
  page that calls a pod service.
- **Expect** — the failure above until the services are restarted.
- **The fix** — restart every service that was running when the jar changed:
  `lcl restart tenancy billing pod-registry merchant content catalog checkout cua payment inventory --stack <name>`.
  Which library was rebuilt decides the blast radius: `sso-core` is uaa and cua (see `cua-qa.md` §00),
  **`autoconfigure` is every Java service**, because they all depend on it.
- **Not a defect** — nothing here needs fixing in the code; it is what rebuilding a jar under a running JVM
  does. Worth knowing before spending an afternoon on a `hasPermission` expression that is correct.

## 06 — Stop / start / restart one service [verified]

- **Steps** — `lcl stop payment`; `lcl status`; `lcl start payment`; `lcl restart payment`.
- **Expect** — only payment changes state (`stopped` → `up`), new pid each time, other services keep their
  pids and uptime; `events` shows `service.stopping/stopped/starting/up` for payment only; infra untouched.

> **Restarting an `-ui` service after changing `ui-kit` needs its Vite cache cleared.** console-ui links the
> shared library as a `file:` dependency and Vite pre-bundles it into
> `store-core/console-ui/.angular/cache/…/vite/deps/`. That copy is not invalidated when the library is rebuilt,
> so the app loads the old one and fails at the first new export — the symptom is a route that silently refuses
> to navigate, with `does not provide an export named …` in the browser console and nothing in the server log.
> `rm -rf store-core/console-ui/.angular/cache` before `lcl restart console-ui`. It cost half an hour once.

## 07 — Crash isolation and `why` [verified]

- **Steps** — `kill -9 $(lsof -t -iTCP:8125 -sTCP:LISTEN)`; wait 5 s; `lcl status`; `lcl why payment`.
- **Expect** — payment `crashed` with the exit reason, every other service still `up` (the stack does **not**
  come down); `why` shows exit code/signal, `port :8125 is free`, the exact command, `LCL_*` env and the last
  error lines. `lcl start payment` brings it back.

## 08 — `--fail-fast` and `--restart` [not verified]

- **Steps** — `lcl start -d uaa tenancy --fail-fast --infra postgres`; kill tenancy's JVM. Then
  `lcl start -d uaa --restart on-failure:2 --infra postgres`; kill uaa's JVM.
- **Expect** — first: the whole stack shuts down (old behaviour). Second: `service.restart-scheduled` then
  `service.up` again; after the 2nd crash no further restart.

## 09 — Second stack runs concurrently on a shifted sequence [verified]

- **Steps** — with the default stack running: `lcl start -d --parallel 4 --stack xxx`.
- **Expect** — the start warns which ports are in use and shifts: `offset +1000` (or the next free one — +1000
  is skipped when the default stack's minio 9000 collides with a +1000 gateway), a distinct checkout-scoped Compose project,
  `docker compose ls` shows both projects, `lcl list` shows both stacks with their gateway ports. Gradle runs with
  `--project-cache-dir .lcl/xxx/gradle` and landing-ui with `.next-xxx`, so the same checkout serves both. Login
  redirect from `http://gateway.com:<gw-b>/oauth2/authorization/uaa` targets `uaa.gateway.com:<uaa-b>` with
  `redirect_uri=http://gateway.com:<gw-b>/…` (the seeded `web-app` client was patched — `events` has
  `uaa.redirects.patched`). `curl -sL http://org1-store1.spg-507f1f77.gateway.com:<spg-b>/` → 200 with the
  store's title (Caddy dials landing-ui on the shifted port; domain lookup works with a port in `Host`). Shopper login
  through cua should keep the port too — spg now sets `X-Forwarded-Port`, so `DynamicRegisteredClientRepository`
  derives `redirect_uri=http://org1-store1.spg-507f1f77.gateway.com:<spg-b>/callback` instead of dropping to :80
  **[not verified]**: the header was added after this case was last run.

## 10 — Stopping one stack leaves the other alone [verified]

- **Steps** — `lcl stop --stack xxx`; `lcl status`.
- **Expect** — xxx's containers and processes gone, xxx removed from `lcl list`; the default stack still 14 × `up`
  with unchanged pids.

## 11 — Port policy flags [verified: offset=1; not verified: configured]

- **Steps** — with the default stack running: `lcl start -d uaa --ports configured --infra postgres --stack yyy`
  and `lcl start -d uaa --ports offset=1 --infra postgres --stack yyy`.
- **Expect** — `configured` fails fast listing the busy ports; `offset=1` forces uaa on 9001 / postgres on 6432
  (or fails listing what holds them).

## 12 — Logs and events [verified]

- **Steps** — `lcl logs payment -n 20`; `lcl logs --errors`; `lcl logs payment -f` (Ctrl-C); `lcl events --service payment`.
- **Expect** — lines from `.lcl/default/logs/payment.log`; only `ERROR|Exception|Caused by` lines across services;
  live tail; the payment event history.

## 13 — Orphan recovery [verified]

- **Steps** — `kill -9 <supervisor pid>` from `lcl status`; `lcl status`; `lcl stop`.
- **Expect** — `status` reports the supervisor as not answering; `stop` kills only processes whose identity still
  matches the recorded process, brings the compose project down and clears the registry. A foreign process on an old
  port is reported and remains untouched.

## 14 — Global package and schema contract [not verified]

- **Steps** — `npm install -g @cvhome-saas/lcl@0.1.0`; `lcl --version`; `lcl validate`; `lcl start -d uaa --no-infra`;
  `lcl status`; `lcl stop`.
- **Expect** — version is `0.1.0`; the repo-root schema-v1 configuration validates; the globally installed binary
  starts and stops the service without any engine or wrapper under `extra/`.

## 15 — Telemetry reaches the collector from every service [verified]

- **Setup** — Docker running; nothing on the configured ports (`lcl doctor`).
- **Steps** — `OTEL_SDK_DISABLED=false lcl start -d --infra all`; open the storefront
  (`http://org1-store1.spg-507f1f77.gateway.com/`) and the seller console once; then in Grafana
  (`http://localhost:3000`, anonymous admin) run, per data source:
  Loki `sum by (service_name) (count_over_time({service_name=~".+"}[30m]))`,
  Tempo `{ resource.service.name="landing-ui" }` and open one trace,
  Prometheus `count by (service_name) (group by (__name__, service_name) ({service_name=~".+"}))`.
  Also `lcl logs --errors --grep 'Failed to export'`, and at the collector
  `curl -s localhost:8889/metrics > /tmp/m` then
  `grep -c http_server_requests_milliseconds /tmp/m` (duplicate export path),
  `grep http_server_requests_seconds_bucket /tmp/m | grep -o 'le="[^"]*"' | sort -u` (latency buckets),
  `grep -o 'service_version="[^"]*"' /tmp/m | sort -u`,
  `grep traces_span_metrics_calls_total /tmp/m | grep -cE 'span_name="(http|task) '` (Micrometer-tracing spans),
  `grep -c '^tomcat_threads_busy' /tmp/m`, `grep traces_span_metrics_calls_total /tmp/m | grep -c 'fetch GET http'`.
  Then `curl -s 'http://localhost:8122/api/v2/products/does-not-exist?store=65f023632bc46470c104b75f&lang=en'`
  and take the `traceId` from the body: `grep <traceId> .lcl/<stack>/logs/catalog.log`, Loki
  `{service_name="catalog"} | trace_id="<traceId>"`, Tempo `curl localhost:3200/api/traces/<traceId>`.
  Finally `grep -c ObservationThreadLocalAccessor .lcl/<stack>/logs/store-core-gateway.log` after 5 minutes.
- **Expect** — all twelve Java services appear in each of the three queries; landing-ui appears in Tempo and
  Prometheus (its Node SDK exports no logs); the storefront trace contains spans from landing-ui, spg and the
  pod services it called (merchant, content, catalog, inventory); Loki lines carry `trace_id` and `span_id`;
  the export grep prints nothing. At the collector: the milliseconds count is 0; the buckets are the thirteen SLO
  boundaries (0.05 … 10) plus `+Inf`; `service_version` is the build version, never `${version}`; the
  Micrometer-tracing span count is 0 (one server span per route, named `GET /api/...`); Tomcat threads are
  present; no landing-ui span name contains a URL. The 404 body's `traceId` is a 32-hex trace id that appears
  in the catalog log line (`[<traceId>-<spanId>]`), in Loki and in Tempo. The gateway WARN count is 0.
  **Expected to fail if** `otel.exporter.otlp.protocol` is dropped from `SPRING_APPLICATION_JSON` in `lcl.yml`:
  the starter defaults to http/protobuf against the gRPC port, every Java service logs
  `Failed to export spans/logs … unexpected end of stream on http://localhost:4317` once a second, and only
  metrics arrive.
- **Note** — `lcl restart <svc>` re-uses the supervisor's environment, so a changed `OTEL_*` variable needs
  `lcl stop` + `lcl start`, not a restart. The `--infra all` set is not part of `lcl start -d`; without it the
  SDK-enabled services retry the export forever and count it as errors in `lcl status`. A whole-stack
  `lcl restart` also comes back with the default infra only — the monitoring containers are gone until the next
  `lcl start -d --infra all`.

## 16 — SLO rules evaluate and every dashboard has data [verified; not verified: the two in-UI link clicks (*View trace*, *Slow traces*) — checked through the Loki and Tempo APIs instead]

- **Setup** — case 15 passed; `cd ../load-testing && make smoke` has run once against the stack (fills every
  route, journey and the k6 series).
- **Steps** — `curl -s localhost:9090/api/v1/rules | jq '[.data.groups[].rules[] | select(.health!="ok")]'`;
  `curl -s 'localhost:9090/api/v1/query?query=count(cvhome:http_server_errors:ratio_rate5m)'` and the same for
  `cvhome:span_server:p95_5m`, `cvhome:sql:p95_5m`, `cvhome:hikari_pool:utilisation`,
  `cvhome:tomcat_threads:utilisation`, `cvhome:s2s:rate5m`;
  `curl -s 'localhost:3000/api/search?type=dash-db' | jq length`; open Grafana → the home page is *Platform
  Overview*; walk the folders `platform`, `data`, `edge`, `observability`, `load-testing` and open every
  dashboard; on *Service RED* pick `catalog`, on *Load test vs app* pick the smoke run's `testid`; on *Logs &
  Errors* expand an error line and click *View trace*; on *Service RED* → *Latency* click *Slow traces*.
  Trigger a 401 (`curl -s -o /dev/null -w '%{http_code}' 'http://localhost:8122/api/v1/private/product/unique?store=65f023632bc46470c104b75f&lang=en&name=x'`)
  and, two minutes later, `curl -s localhost:8889/metrics | grep cvhome_auth_rejections_total`.
  From the repo root: `node extra/monitoring/scripts/build-dashboards.mjs --check && node extra/monitoring/scripts/dashboard-docs.mjs --check`
  and `docker run --rm -v "$PWD/extra/monitoring/prometheus-rules:/r:ro" --entrypoint promtool prom/prometheus:v3.11.2 test rules /r/tests/cvhome.test.yml`.
- **Expect** — the unhealthy-rules list is empty; each `count(...)` query returns a number (12 services for the
  HTTP and JVM ones, the service-graph edges for `cvhome:s2s:rate5m`); the search returns 12 dashboards, all
  marked provisioned and not editable in the UI; every dashboard shows data in its panels (the *Auth rejections
  by reason*, *Cache*, *Outbox records* and *Gateway sessions* panels once the smoke run and the 401 have
  happened); *View trace* opens the trace in Tempo with the same id; *Slow traces* opens Tempo search filtered
  on the service; `cvhome_auth_rejections_total{reason="missing_token",status="401"}` is present; both scripts
  print "valid" / "up to date" and promtool prints SUCCESS. **Expected to fail if** a dashboard JSON is edited
  by hand without regenerating `docs/dashboards.md` — the check script fails on the diff, which is the point.

## 17 — The load stack: images, one container each, 1 GB per container [verified; not verified: LOAD_MEM other than 1g, a registry-tagged LOAD_TAG]

The numbers a load test produces against `lcl start` are development numbers (gradle `bootRun`, no memory limit,
`next dev`). This stack runs the platform as its images with the memory a deployed task gets.

- **Setup** — Docker running, `/etc/hosts` from `configure-domain.sh`, no lcl stack on the default ports
  (`lcl list` shows none — the script refuses otherwise).
- **Steps** —
  1. `extra/scripts/load-stack.sh build` — expect `store-core/{uaa,store-core-gateway,tenancy,billing,pod-registry,console-ui}:latest`
     and `store-pod/{merchant,content,catalog,checkout,cua,payment,inventory,landing-ui}:latest` in `docker images`
     (a Docker Hub timeout on the buildpack builder fails one image; rerun the same command).
  2. `extra/scripts/load-stack.sh up` — expect "every Java service is UP" within ten minutes, then
     `landing-ui 307`, `console-ui 200`, `spg 307`.
  3. `extra/scripts/load-stack.sh stats` — every container `/ 1GiB`; a JVM at rest sits at 250–400 MiB.
  4. `curl -s http://localhost:9090/api/v1/query --data-urlencode 'query=count by (service_version) (process_uptime_seconds)'`
     — `1.0.16 12`: every JVM exports to the collector, from inside the network.
  5. `cd ../load-testing && make smoke` — 308 requests, 0 failed, 2 orders (the `spg:domain-lookup` check that
     fails once is the same on lcl).
  6. `extra/scripts/load-stack.sh down --hard` — no `cvhome-load-*` containers or volumes remain.
- **Expect** — the storefront reaches spg by its in-network alias and spg reaches the pods by theirs (no
  `host-gateway` entries: `docker inspect cvhome-load-spg-1 | grep -c host-gateway` is 0); an lcl stack started
  afterwards on the same ports works unchanged.
- **Expected to fail** — `up` while an lcl stack holds the ports (refused by the guard, or port collisions if the
  guard is bypassed); `up` before `build` (`pull access denied` for `store-pod/catalog`).
