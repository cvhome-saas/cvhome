# QA — local stack lifecycle (`lcl`)

`extra/scripts/lcl` (source `extra/lcl`) owns local stack startup, shutdown, per-service recovery, health, logs,
audit and isolation between named stacks. These cases prove it manages the full stack, single services, and
several stacks at once (`--stack xxx`) without touching each other. `extra/scripts/run-lcl.sh` is a shim over the same tool.

- **Scope** — `extra/scripts/lcl`, `extra/lcl/` (engine), `lcl.yml` + `extra/lcl-config/` (project), `docker-compose-lcl.yml`, `store-pod/spg/Caddyfile`
  (`{$LCL_PORT_*}`), local Docker infra, Java services, frontends
- **Change** — rewrite of the bash supervisor as a TypeScript multi-stack runner with dynamic port sequences
- **Cases** — 14

Each case is tagged **[verified]** (run on this branch, passed) or **[not verified]**.

---

## 00 — Before you start

Run from the repository root. Docker must be running; hosts file configured. Every command below takes
`--stack <name>`; without it the `default` stack is meant.

```bash
sudo ./extra/scripts/configure-domain.sh
./extra/scripts/lcl doctor            # every line ✓ (a "!" about ports in use is fine)
./extra/scripts/lcl ports             # configured ports from lcl.yml
```

Probes:

```bash
./extra/scripts/lcl status
./extra/scripts/lcl list
./extra/scripts/lcl events | tail
docker compose ls                     # one project per stack: lcl-<stack>
lsof -nP -iTCP:8122 -sTCP:LISTEN
```

Stop through the tool (`lcl stop`), never with a manual `kill`.

---

## 01 — Ports and services come from `lcl.yml` [verified]

- **Steps** — `lcl ports`; temporarily change `catalog`'s `port:` in `lcl.yml`; `lcl ports` again; revert.
- **Expect** — the table lists 14 services + spg + infra with the configured ports; the changed port shows up
  without touching the tool. `lcl doctor` validates `lcl.yml` (runnable services, `after` references, templates, hooks).

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

## 06 — Stop / start / restart one service [verified]

- **Steps** — `lcl stop payment`; `lcl status`; `lcl start payment`; `lcl restart payment`.
- **Expect** — only payment changes state (`stopped` → `up`), new pid each time, other services keep their
  pids and uptime; `events` shows `service.stopping/stopped/starting/up` for payment only; infra untouched.

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
  is skipped when the default stack's minio 9000 collides with a +1000 gateway), compose project `lcl-xxx`,
  `docker compose ls` shows both projects, `lcl list` shows both stacks with their gateway ports. Gradle runs with
  `--project-cache-dir build/lcl/xxx/gradle` and landing-ui with `.next-xxx`, so the same checkout serves both. Login
  redirect from `http://gateway.com:<gw-b>/oauth2/authorization/uaa` targets `uaa.gateway.com:<uaa-b>` with
  `redirect_uri=http://gateway.com:<gw-b>/…` (the seeded `web-app` client was patched — `events` has
  `uaa.redirects.patched`). `curl -sL http://org1-store1.spg-507f1f77.gateway.com:<spg-b>/` → 200 with the
  store's title (Caddy dials landing-ui on the shifted port; domain lookup works with a port in `Host`).

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
- **Expect** — lines from `build/lcl/logs/payment.log`; only `ERROR|Exception|Caused by` lines across services;
  live tail; the payment event history.

## 13 — Orphan recovery [verified]

- **Steps** — `kill -9 <supervisor pid>` from `lcl status`; `lcl status`; `lcl stop`.
- **Expect** — `status` reports the supervisor as not answering; `stop` kills the recorded service processes,
  sweeps only this stack's ports, brings the compose project down and clears the registry.

## 14 — Compatibility shim [verified: --list, pid; not verified: start/stop through the shim]

- **Steps** — `./extra/scripts/run-lcl.sh --list`, `./extra/scripts/run-lcl.sh start -d uaa --no-infra`,
  `./extra/scripts/run-lcl.sh pid`, `./extra/scripts/run-lcl.sh stop`.
- **Expect** — each prints the `lcl` command it forwards to and behaves like it (`--list` → `ports`, `pid` → `status`).
