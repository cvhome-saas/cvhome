# QA testing — exercising the real stack

Automated tests (`./gradlew test` / `integrationTest`) prove a unit behaves; **QA proves the feature works
end to end through the same path a user takes** — browser → gateway → uaa → pod service → database. This file
is the procedure for that: bring the stack up, drive it, read the evidence, tear it down.

Do QA whenever a change is user-visible: a new/changed endpoint, anything in a `-ui` module, a login or
routing change, a permission token, a new store/pod. "It compiles and the unit test passes" is not QA.

---

## 1. Bring the whole stack up

The runner is the public `lcl` CLI — install it once, then work from the repo root; the stack itself is
described by `lcl.yml` there.

```bash
npm install -g @cvhome-saas/lcl            # once per machine
sudo ./extra/scripts/configure-domain.sh   # once per machine — /etc/hosts for gateway.com, pods, demo stores
lcl start -d                               # infra + every Java service + both frontends; returns when healthy
```

`lcl` is *the* way to start it. It brings up the compose infra (postgres, MinIO, `spg`), then each Java service
under `--spring.profiles.active=lcl,test-stores` in dependency order (**uaa first — it issues the tokens**),
then `console-ui` (:8011) and `landing-ui` (:8110), pre-building landing-ui's workspace libs in the service's
`prepare` step. Java services run **on the host**, not in Docker; `spg`'s `extra_hosts` map service hostnames
back to the host. The monitoring containers (otel-collector, loki, tempo, prometheus, grafana) are not in the
default set — add them with `--infra all`.

**Several named stacks run side by side.** `--stack xxx` (default `default`) selects the stack every command
acts on; each has its own supervisor, services, checkout-scoped Compose project, and `.lcl/<stack>/` state and
logs. Ports come from `common-config.yml` and are declared per service in `lcl.yml`; when any of them is already
taken (another stack, a stray process) the **whole stack shifts by +1000·k** to the first free sequence —
gateway 9000, catalog 9122, postgres 6432, spg 1080 … — and `lcl urls --stack xxx` / `lcl ports --stack xxx`
tell you where that stack lives. Hostnames never change. Everything that has to follow a shifted port does:
the Spring services through a generated `SPRING_APPLICATION_JSON`, spg's Caddyfile through `{$LCL_PORT_*}`,
landing-ui through `INTERNAL_SPG`, and uaa's seeded `web-app` redirect URIs through an `after-up` hook.

**One stack per worktree.** Each feature lives in its own git worktree (AGENTS.md, Working conventions), and
each worktree runs its own stack: from inside the worktree, `lcl start -d --stack <short-name>` — lcl resolves
`lcl.yml` upward from the cwd, so the stack builds and serves *that* worktree's code with its own `.lcl/<stack>/`
state. The dynamic port shifting above is what lets any number of worktrees QA in parallel without conflicts;
always pass `--stack` and read live ports from `lcl urls --stack <short-name>` rather than assuming the
configured ones. Stop the stack before removing the worktree.

| command | what it does |
|---|---|
| `lcl start [svc…] [-d] [--build] [--stack xxx] [--parallel N] [--infra core\|all]` | start the stack (or just those services plus their dependencies); `-d` returns once everything is healthy |
| `lcl status [--json]` | services, state, ports, pids, uptime, health |
| `lcl urls` / `lcl ports [--env]` | that stack's console, storefront, minio, postgres URLs / port map (`--env` for shells and `.http` files) |
| `lcl restart catalog` / `lcl stop catalog` | one service; the rest keep running (data untouched) |
| `lcl why catalog` | exit code, health reason, who holds the port, exact command + env, last errors |
| `lcl logs [svc…] [-f] [--errors] [--grep RE] [--since 10m]` | service logs (`.lcl/<stack>/logs/`) |
| `lcl events [-f]` | the audit trail: every start/stop/crash/health transition (`.lcl/<stack>/events.jsonl`) |
| `lcl validate` / `lcl doctor` | check `lcl.yml` against the schema / check Docker, `/etc/hosts`, working dirs, ports |
| `lcl list` | every running stack |
| `lcl stop [--hard]` | that stack only; `--hard` also deletes the compose volumes |

**`lcl restart` of the whole stack is not reliable.** The services each run their own `./gradlew … bootRun`,
and they compete for the shared `~/.gradle/caches/journal-1` lock — a restart has the previous run's daemons
still alive while the new clients start, and the pod services, which have no ordering between them, lose the
race and exit 1. The give-away is `Timeout waiting to lock journal cache` and a `landing-ui` stuck on
`blocked by dependency`, so the storefronts are down while the platform services look healthy. Use
`lcl stop` then `lcl start -d`; a cold start has no leftover daemons. Full write-up: `qa/lcl-qa.md` §05.

**Check `lcl status` before starting another one.** A crashed service no longer takes the stack down: it is
marked `crashed`, `status` shows it and `why` explains it. Stop with `lcl stop`, never with a manual `kill` of
a supervised process. A foreground `lcl start` blocks and Ctrl-C shuts that stack down; `-d` is the usual form.

Iterating on one frontend against an already-running backend? Don't restart everything — leave the stack up and
`lcl restart console-ui`, or run `npm start` in that `-ui` module against the running gateway. One service by
hand: `./gradlew :store-pod:catalog:catalog-service:bootRun --args='--spring.profiles.active=lcl,test-stores'`.

---

## 2. Entry points and demo logins

Seeded by the `test-stores` profile:

| what | url | login |
|---|---|---|
| seller console | `http://gateway.com:8000/` | `org1-admin` / `admin` |
| demo storefront | `http://org1-store1.spg-507f1f77.gateway.com` | `user` / `revo` |
| other demo stores | `org1-store2.`, `org2-store1.`, `org2-store2.spg-507f1f77.gateway.com` | same |
| uaa directly | `http://uaa.gateway.com:8001` | |
| grafana | `http://localhost:3000` | — |

Those credentials are local seed data only (`store-core/uaa/.../init-sql/data-test-stores.sql` and the pods'
`init-sql/stores/*`) and exist solely because `test-stores` is active — not secrets, never present outside
`lcl`. **If a login fails, the stack almost certainly came up without `test-stores`.**

The storefront account is scoped per store (`cua.users.client_id`), so it only authenticates *through the
store host* — posting to `localhost:8124/login` directly always fails, and that is not a bug.


### The seeded tenants

| | org | store 1 | store 2 |
|---|---|---|---|
| **ORG1** | `21f023932bc66470c104b76f` | `65f023632bc46470c104b76f` | `65f023632bc46470c104b75f` |
| **ORG2** | `352023632b046970c104b76f` | `65f020632bc46470c104b76f` | `65f023632bc26470c104b75f` |

The shared pod is `507f1f77bcf86cd799439011`, seeded from `store-core-lcl-config.yml` by `PodSeedInitializer`;
it is the only one, so placement cases have to insert a second. org1-store1 and org1-store2 are the **same
org** (use store 2 for "another store"); org2-store1 is "another org". Locales differ on purpose:
org1-store1 is `en, ar` — the RTL cases need it — and org2-store2 is Arabic-first.

### The seeded accounts

From `store-core/uaa/src/main/resources/init-sql/data-test-stores.sql`, all with password `admin`:

| Username | Role | Store metadata |
|---|---|---|
| `super-admin` | SUPER_ADMIN | the platform screens |
| `org1-admin` | ORG_ADMIN | **none** — an org admin is in no store's user list |
| `org1-store1-admin` | STORE_ADMIN | ORG1-STORE1 |
| `org1-store1-moderator` | STORE_MODERATOR | ORG1-STORE1 — the read-only case |
| `org1-store2-admin` | STORE_ADMIN | ORG1-STORE2 |
| `org2-admin` | ORG_ADMIN | another org — the isolation cases |

**If you change a password, change it back**; the seed only runs on a clean database.

### A service-to-service token

Placement, capacity and the pods' s2s endpoints need client credentials, **not** a session:

```bash
TOK=$(curl -s -u 'store-core@service.store-core.internal:<the shared lcl secret>' \
  -d 'grant_type=client_credentials&scope=store_core' \
  http://uaa.gateway.com:8001/oauth2/token | jq -r .access_token)
```

### Billing's quota will stop you after a few stores

Every test store is provisioned unpaid and `max-pending-stores` is 3, after which creation answers 422
`BILLING.QUOTA.STORE_EXCEEDED`. Between runs:

```sql
delete from billing.store_subscription where id in (select id from tenancy.manager_store where name like 'TEST%');
delete from pod_registry.pod_store_placement;
delete from tenancy.manager_store where name like 'TEST%';
```

The gateway holds sessions **in memory**: restarting it logs you out, and the symptom is a 401 where you
expected a 403.

---

## 3. Driving the UI in a browser

Use the `claude-in-chrome` skill (invoke it before touching any `mcp__claude-in-chrome__*` tool); the
`chrome-devtools-mcp:*` skills cover performance, a11y and network debugging.

- Start from `tabs_context_mcp`, then open a **new tab** rather than hijacking one of the user's.
- Never trigger `alert`/`confirm`/`prompt` — a modal blocks the extension for the rest of the session. Use
  `console.log` + `read_console_messages` instead.
- `read_console_messages` and `read_network_requests` are the fast path for "the page is blank" / "the save
  button does nothing" — a 401/403 there usually means a missing `@PreAuthorize` token or a token that never
  got relayed, not a frontend bug.
- Record multi-step flows with `gif_creator` when the user will want to review or share the result; name the
  file for the flow (`checkout_guest_order.gif`).
- Two or three failed tool calls, a page that will not load, or exploration drifting off the feature → stop
  and report, don't keep poking.

---

## 4. QA-ing an API without a browser

Every endpoint ships a runnable block in `<service>/http/<api-class>.http` — that *is* the API QA script.
Run it in IntelliJ's HTTP Client against the `lcl` environment. Rules and file layout:
`references/http-request-files.md`.

- Address the **gateway** (`{{SELLER_UI_URL}}/spg/catalog/…`), never the service's own port — that is the path
  a real client takes, and it is what catches a missing route.
- Always `?store={{STORE_ID}}&lang={{LANG}}`. Swap to `{{STORE_ID_2}}` to prove tenant scoping: the second
  store must **not** see the first store's row.
- Shared vars in the repo-root `http-client.env.json`; session ids in the gitignored
  `http-client.private.env.json` (copy the `.example`) — never commit a session id.
- Exercise the failure modes too, not just the happy path: the 4xx blocks are where the typed-exception
  contract is actually verified (`references/error-handling.md`).


**Two addressing forms, and the `pod` predicate.** Both go through a gateway; neither uses a service port:

```
http://gateway.com:8000/spg/<service>/api/v1/...?store=<id>&pod=507f1f77bcf86cd799439011&lang=en   # seller path
http://<store>.spg-507f1f77.gateway.com/<service>/api/v1/...?store=<id>&lang=en                    # pod path
```

**The platform gateway route predicates on `pod` as well as `store`** — a `/spg/**` URL carrying only `store`
is a 404 that looks like missing data, and it is the single most common false alarm in this repo.

---

## 5. Reading the evidence

| signal | where |
|---|---|
| service stdout | `.lcl/<stack>/logs/<service>.log`, or `lcl logs <service> -f` |
| logs / traces / metrics | grafana `http://localhost:3000` (Loki, Tempo, Prometheus) |
| a failing request's internals | the `traceId` on the ProblemDetail response, then that trace in Tempo |
| "the event never arrived" | `select * from outbox_record where status='FAILED'` — read `failure_reason` |
| "no instances available for X" | that service's entry in `lcl-config.yml` |

Response bodies never carry root-cause text; the detail is in the log, joined by `traceId`. Don't guess from
the HTTP body alone.


Reading a database directly, the idiom every QA document uses (`...` continues the same command):

```bash
docker exec cvhome-postgres-1 psql -U postgres -d cvhome -c "select ...;"
... "select ...;"
```

---

## 6. Known local gaps — expected, don't file them

- **Storefront login only works through the store host** (see §2).
- A store or pod you add locally needs its hostname added to `configure-domain.sh` too, or the browser gets
  "host not found".

---

## 7. Writing the QA document — `<service>/qa/<module>-qa.md`

The QA that a human tester runs lives in **one markdown file per service**, beside that service's `http/`
folder: `store-core/tenancy/tenancy-service/qa/tenancy-qa.md`,
`store-pod/catalog/catalog-service/qa/catalog-qa.md`, `store-core/console-ui/qa/console-ui-qa.md`. The name
drops the `-service` suffix. Every runnable app has one — the thirteen Java services plus `console-ui` and
`landing-ui`. Library modules (`-commons`, `-core`, `-external-api`, `store-commons/*`) do not: QA is end to
end and they have no running surface. The one file that is not beside a service is **`qa/lcl-qa.md`** at the
repo root, which covers the stack itself.

Markdown so it reviews in the PR and travels with the branch.

**One file per service, not one per plan.** A plan that ships in ten PRs and touches four services appends its
cases to **those four files**, into the section that theme belongs to. It does not get a document of its own.
Organising by plan is what this replaced, and it failed in a specific way: `content` cases ended up spread
across eight files named after changes nobody remembers, the same setup was repeated in each and drifted, later
plans silently invalidated earlier files, and a tester opening a service directory had no way to know which
document applied. Fold each plan into the service files instead, and note the plan in the section's provenance
line.

**A case lives with the service that owns the flow's entry point.** A console screen backed by tenancy is a
console case if the assertion is what the screen does, and a tenancy case if the assertion is what the endpoint
answers. Cross-reference the other side by path (`- **Also touches** — ../../..`), and **never duplicate the
case text**: two copies drift, and then one of them is wrong.

**`store-core/billing/billing-service/qa/billing-qa.md` is the reference implementation.** Match its structure:

1. **Title and a short intro** — `# QA — <service> (<module path>)`, then what this service owns and what a
   tester is actually proving here.
2. **A scope block** — `Scope` (the endpoints and screens covered) · `Runs on` (`lcl start -d --stack <name>`,
   and to address it through the gateway, never its own port) · `Cases` (the count, with the tag split) ·
   `Also see` (the service files holding the other half of any cross-service flow) · anything a tester must
   know before touching it (a test-mode key, a migration order).
3. **The tag legend.** Every case is tagged, and the tags are the point of the document:
   - **[verified]** — run against a running stack and passed.
   - **[unit only]** — the branch is covered by a named test, but nobody drove it through the stack. Name the
     test so the reader can judge.
   - **[not verified]** — never run end to end by anyone.

   A case nobody has executed is where the bugs are. **Marking it is more useful than implying otherwise** —
   never quietly present an unrun case as passing, and never drop it to keep the document tidy. A case that
   moves between files keeps the tag it earned; a case rewritten from scratch starts at **[not verified]**.
4. **`## 00 — Before you start`** — open by pointing at §§1–5 of this file for the shared prerequisites (stack
   startup, demo logins, seeded ids, addressing, the `psql` idiom), then list **only what is specific to this
   service**: its fixture SQL, its config overrides, its provider keys, and the queries that show the truth
   underneath it. Do not copy the shared blocks in: fifteen copies of the seeded-ids table is the drift this
   layout exists to prevent.
5. **Sections with a short prefix**, each opening with the design points a tester needs in order to judge what
   they see — especially anything deliberate that *looks* like a bug (fail-open here, fail-closed there; 404
   instead of 403). One section per theme, not one per phase. A section holding cases that came from a plan
   carries a one-line **provenance** (`_From qa/<plan>.md §PFX._`, naming the plan-shaped file it came from — those live on in git history) so the history stays reachable.
   **A prefix must be unique within its file**: when two sources collide, renumber and say so
   (`_Was CAT-01…08; renumbered because catalog's own `CAT` is the category tree._`).
6. **Cases as `### PFX-NN — what it proves · severity · [tag]`**, numbered within their section, with
   `- **Setup**` / `- **Steps**` / `- **Expect**` bullets. Mark the ones that matter `· critical` or `· high`.
   Add `- **Seen**` with the concrete evidence when the observation is worth more than "it passed" (the actual
   status codes, the log line, the timing). State plainly what is *expected to fail* so a tester does not spend
   a morning re-finding a known gap.
7. **`## MIG`** when there is a migration — the order, and what breaks silently if a step is skipped.
8. **`## REG — Regression watchlist`** — a table of every defect that actually happened in this service: what
   broke, how it looked, and which case catches it again. Highest-value section in the file, because each row
   has already proven it can happen.
9. **`## 99 — Known gaps`** — behaviour that is expected today, so nobody re-raises it. Lead with the largest.
10. **A closing line** — where to raise findings, and which log to attach.

Write it for a person who did not do the work. Explain *why* a case exists, not just its steps — a case whose
purpose is unclear gets skipped or "fixed" back. This is for humans; the machine-checkable path is `.http`
blocks and tests, and it does not replace them.

**Never a rendered page or an attachment.** Those cannot be diffed, and a test plan nobody can review is a test
plan nobody trusts.

---

## 8. QA checklist for a user-visible change

- [ ] Exercised through the **gateway**, not the service port — both edges if the feature is seller *and*
      storefront (`gateway.com:8000/spg/...` and `org1-store1.spg-507f1f77.gateway.com`)
- [ ] **Tenant isolation shown, not assumed**: repeat the action as a second store (`STORE_ID_2` /
      `org2-store1`) and confirm it cannot see or mutate the first store's data
- [ ] **Permission actually gates**: a principal without the token gets 403, not a 200 with empty data
- [ ] Failure modes hit on purpose — the typed error surfaces the right status and `code`, no root-cause leak
- [ ] i18n: switched locale; the new keys resolve in **every locale the app ships** (console-ui `en,ar`; landing-ui and cua `en,ar,es,fr,ru`), no raw key on screen
- [ ] **AR checked as RTL** — layout and icon direction, not just the strings
- [ ] Console clean and no failed requests in the network panel for the flow
- [ ] For a new service/route: it resolves via `lb://`, and the edge (Caddyfile / `GatewayRouteLocatorImpl`)
      returns your API rather than console-ui's HTML

---

## 9. Where automated tests fit

```bash
./gradlew test                 # src/test — unit + architecture tests, no Docker
./gradlew integrationTest      # src/integrationTest — Testcontainers, Docker required
./gradlew perServiceCoverage   # coverage report per micro service
```

Unit and integration tests are separate **source sets** (`*Test` vs `*IntegrationTest`), not JUnit tags. Integration
tests use **Testcontainers (Postgres, MinIO), so Docker must be running** — a failure at container startup is an
environment problem, not a test failure. Every store-scoped integration test owes the same two cases this checklist
demands: tenant isolation and the permission gate.

These gate the merge; the browser/`.http` pass above is what proves the feature. **How to write them, the naming
standard, the shared `test-support` helpers and the coverage ratchet: `references/testing.md`.**
