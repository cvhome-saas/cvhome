# Plan: `lcl` — TypeScript multi-stack local runner replacing `run-lcl.sh`

## Context

`extra/scripts/run-lcl.sh` (bash, single instance, ports hardcoded in four places) could not run two stacks at
once, had no health model, no audit trail, and `stop` swept canonical ports (killing any other stack). The user
asked for a mature tool in a real language that runs 2–3 stacks in parallel, injects ports at runtime, knows which
services are up/down, and helps troubleshoot. Three refinements came during the work:

1. `common-config.yml` is the single source of truth; configured ports are the default and the stack shifts to a
   new sequence only when one is taken.
2. No git worktrees: stacks are **named** (`--stack xxx`, default `default`) inside one checkout.
3. Nothing hand-listed: compose services/ports, Java modules and node apps are **discovered at runtime**.

## Status: implemented and verified — nothing left to build

All milestones are done in the working tree (uncommitted). Remaining work is the user's review; optional follow-ups
are listed at the end.

## What exists

```
extra/lcl/                 zero-dependency TypeScript, Node ≥ 23.6 runs it directly (no build step)
  src/main.ts              CLI (parseArgs): start stop restart status urls ports logs events why doctor list clean
  src/catalog.ts           runtime discovery (see below)
  src/instance.ts          stack name, build/lcl/<stack>/ paths, state.json, registry ~/.cvhome/lcl/instances
  src/ports.ts             wildcard bind + one lsof scan; configured ports first, else next free +1000·k sequence
  src/render.ts            lcl-instance.yml (Spring overrides), compose.env, compose.override.yml (`ports: !override`), URLs
  src/supervisor.ts        per-stack daemon: start order, health loop (/actuator/health), crash policy, control socket
  src/proc.ts compose.ts control.ts health.ts logs.ts events.ts ui.ts yaml.ts
extra/scripts/lcl          entry shim;  extra/scripts/run-lcl.sh  compat shim (--list→ports, pid→status, --volumes→--hard)
extra/lcl/README.md        design + command reference
```

**Discovery (`catalog.ts`)** — service names/ports from `common-config.yml`; Java module = directory of the
`src/main/resources/application.yml` whose `spring.application.name` matches (`:store-pod:catalog:catalog-service`);
node app = `package.json` directory named like the service, framework from dependencies (Angular → `ng serve --port`,
Next workspace → `build:libs-*` then `next dev -p`); containers + every published port from `docker-compose-lcl.yml`;
a configured service that is also a compose service is container-served (`spg`); infra roles by image
(postgres/minio/otel). `lcl doctor` reports a configured service with none of the sources.

**Port injection** — Java: generated `build/lcl/<stack>/lcl-instance.yml` via `--spring.config.additional-location`
(service ports, discovery URIs incl. the namespaced gateway alias, datasource, MinIO, full pod entries — Spring
replaces lists, so entries are complete copies with the endpoint changed). Compose: generated override with
`ports: !override` per service + `compose.env` for the `${LCL_PORT_*:-default}` placeholders the compose file uses in
spg's URLs/env. Caddyfile upstreams `{$LCL_PORT_<SVC>:default}`. uaa's seeded `web-app` redirect URIs patched via
psql on a shifted stack. Gradle `--project-cache-dir build/lcl/<stack>/gradle` and `NEXT_DIST_DIR=.next-<stack>`
(read in `storefront/next.config.ts`) let two stacks run the same module from one checkout.

**Repo-side edits** — `docker-compose-lcl.yml` (`${LCL_PORT_*:-default}` ports + spg env), `store-pod/spg/Caddyfile`
(env upstreams), `store-pod/landing-ui/storefront/next.config.ts` (`distDir` from env), docs (`AGENTS.md`,
`.agents/.claude` project-structure skill refs, `qa/run-lcl-lifecycle.md`), memory `lcl-stack-runner`.

## Verified

- Full stack 14/14 `up` in ~90 s (`--parallel 3`), gateway + storefront 200.
- Two stacks from one checkout at once (`default` +0, `xxx` shifted); login redirect targets the shifted gateway;
  storefront renders through spg on the shifted port; stopping one leaves the other untouched.
- Runtime discovery run: `xxx --ports offset=1` moved every container via the generated override (postgres 6432,
  minio 10000/10001, spg 1080/1443/3019), services on 9001/9120/9110, spg lookup 200, clean stop.
- Single-service stop/start/restart, `why`, crash isolation (`kill -9` a JVM), orphan recovery (`kill -9` the
  supervisor → `lcl stop`), foreground Ctrl-C, shim `--list`/`pid`, precedence of the override file confirmed
  through `/actuator/env`.

## Optional follow-ups (not started)

- `--ports configured` and the shim's start/stop path are tagged *not verified* in `qa/run-lcl-lifecycle.md`.
- The `${LCL_PORT_*}` placeholders in `docker-compose-lcl.yml` port mappings are now redundant (the override
  handles ports); they could be reverted to plain `"5432:5432"`, keeping only the ones used inside spg's env URLs.
- Commit: suggested message `feat(lcl): TypeScript multi-stack local runner replacing run-lcl.sh`.
