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
- **Cases** — 17
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

## 15 — Telemetry, dashboards and the platform-as-images stack live in load-testing [moved 2026-09-08]

The dev stack has no telemetry backend and this repo ships no monitoring configuration. The collector, Loki,
Tempo, Prometheus (rules and tests), Grafana (dashboards and their generators) and the compose stack that runs
the platform as its built images are `../load-testing/stack/`; the cases that used to be 15–17 here are
`../load-testing/qa/load-testing-qa.md` § 05 and the reading guide is `../load-testing/docs/monitoring/`.
Images for that stack are a pre-step: `./gradlew bootBuildImage` here (tags `latest`).

- **Expected here** — `OTEL_SDK_DISABLED=false lcl start -d` now has nothing to export to; the Java services
  retry the exporter and count errors in `lcl status`. Leave the SDK disabled on the dev stack.

## 16 — heapdump and env through the gateway are 404 [not verified]

The shared `common-config.yml` maps `health`, `info` and `prometheus` under `/actuator` and nothing else
(`MANAGEMENT_ENDPOINTS_EXPOSURE`, default `health,info,prometheus`), and the health body carries no
`components` for an anonymous caller (`show-details: when-authorized`). Every service but uaa serves its
actuator anonymously and the gateway forwards `/tenancy/actuator/...`, so before this the heap dump — with the
signing keys in it — was one GET away from the public edge (authorization audit, A1). Unit-tested against the
shipped file in `store-commons/autoconfigure` (`ActuatorExposureTest`); this case is the live check.

- **Setup** — the default stack up, `MANAGEMENT_ENDPOINTS_EXPOSURE` unset in the shell that started it.
- **Steps** — through the gateway: `curl -si http://gateway.com:8000/tenancy/actuator/heapdump | head -1`,
  the same for `/tenancy/actuator/env` and `/tenancy/actuator/configprops`; then
  `curl -s http://gateway.com:8000/tenancy/actuator/health`; then directly on a pod service,
  `curl -si localhost:8122/actuator/env | head -1` and `curl -s localhost:8122/actuator/health`.
- **Expect** — every heapdump / env / configprops request is `404`; each health body is `{"status":"UP"}` with no
  `components`; `lcl status` still shows every service `up` (the runner's own probe reads only `"status"`).
- **The debugging lever** — `MANAGEMENT_ENDPOINTS_EXPOSURE='*' lcl restart tenancy` (or any one service) and
  `/tenancy/actuator/env` answers 200 for that process only; restart it without the variable to close it again.
- **Expected to fail until the gateway PR lands** — the gateway's own `/actuator/env` and
  `/actuator/gateway/routes` are unmapped by this change as well, but its `/actuator/**` stays `permitAll` until
  the gateway hardening PR puts a super-admin gate in front of it; see
  [gateway](../store-core/gateway/gateway-service/qa/gateway-qa.md) § 99.

## 17 — Every Spring service as a native executable, against the same stack [verified 2026-09-10, feat/graalvm-native]

The twelve Spring services build as GraalVM native executables (`references/build-system.md` → *Native images*;
`.agents/plans/graalvm-native-images.md`). This case proves a native build behaves like the JVM one: each service's
native executable replaces its JVM process on a running stack, with the same environment and profiles
(`lcl,test-stores`), and every `http/*.http` file runs against both.

- **Setup** — GraalVM 25 on the host (`sdk install java 25.0.2-graalce`; CE is what the buildpack's Liberica NIK is
  built from), `GRAALVM_HOME` pointing at it; the stack up (`lcl start -d --stack <name>`) and green.
- **Steps**
  1. `./gradlew nativeCompile --parallel --max-workers=3` — twelve executables under
     `<module>/build/native/nativeCompile/` (~17 min on a 14-core laptop; one service peaks ~9.5–12 GB, cap it with
     `NATIVE_IMAGE_OPTIONS=-J-Xmx12g`).
  2. Run every `.http` file under the twelve services' directories against the JVM stack and keep the pass/fail per
     request (the IntelliJ HTTP client, or `npx httpyac send <file> --all --env lcl`, with the session ids of
     `super-admin`, `org1-admin` and `org1-store1-admin` in `http-client.private.env.json`).
  3. For each service: `lcl stop <svc>`, then start its executable from the module directory with the JVM's
     environment and `--spring.profiles.active=lcl,test-stores`; wait for `/actuator/health` UP.
  4. Sign in to the console through the gateway (native gateway, native uaa) and run step 2 again.
- **Expect** — every service healthy; the console sign-in works; **the same requests pass as on the JVM** (2026-09-10:
  392 of 592 on `main`'s JVM, 392 on this branch's JVM, 392 native — the 200 that fail do so identically on both:
  `.http` blocks still sending `Cookie: SESSION=` instead of the gateway's cookie, variables the files never set,
  state from earlier runs).
- **Measured on the host, same workload** (macOS, indicative — the load-testing stack is where containers are measured):

  | service | JVM start | native start | JVM memory | native memory |
  |---|---|---|---|---|
  | uaa | 3.9 s | 0.58 s | 552 MB | 118 MB |
  | cua | 4.1 s | 0.57 s | 448 MB | 129 MB |
  | store-core-gateway | 1.2 s | 0.12 s | 442 MB | 139 MB |
  | tenancy | 2.2 s | 0.35 s | 415 MB | 131 MB |
  | billing | 2.3 s | 0.35 s | 415 MB | 133 MB |
  | pod-registry | 1.8 s | 0.27 s | 329 MB | 103 MB |
  | merchant | 2.9 s | 0.36 s | 410 MB | 141 MB |
  | content | 3.8 s | 0.75 s | 521 MB | 137 MB |
  | catalog | 4.3 s | 1.07 s | 457 MB | 138 MB |
  | checkout | 3.2 s | 0.40 s | 461 MB | 140 MB |
  | payment | 3.2 s | 0.47 s | 474 MB | 137 MB |
  | inventory | 2.9 s | 0.52 s | 393 MB | 128 MB |

  "Start" is Spring's own `Started … in` line; "memory" is the process's physical footprint (`vmmap --summary`) after
  the `.http` run. Every native service sits well inside a 512 MB task.
- **Expected to fail** — nothing that passes on the JVM. A pod registered with an `INTERNAL` endpoint is called as
  `lb://spg.<namespace>`, a name no build can know, and fails natively (every deployment registers pods
  `EXTERNAL`). The `bootBuildImage -Pnative` path (Paketo, Liberica NIK) was not built locally for this case: the JVM
  image with the plugin applied was (`./gradlew :…:bootBuildImage` stays a `java … JarLauncher` image), and the buildpack
  resolved Liberica NIK 25.0.4 for it; the first native image is CodeBuild's (`cvhome-platform` `qa/platform-qa.md`
  § 02.2b).

