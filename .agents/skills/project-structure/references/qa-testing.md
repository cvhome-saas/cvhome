# QA testing — exercising the real stack

Automated tests (`./gradlew unitTest` / `integrationTest`) prove a unit behaves; **QA proves the feature works
end to end through the same path a user takes** — browser → gateway → uaa → pod service → database. This file
is the procedure for that: bring the stack up, drive it, read the evidence, tear it down.

Do QA whenever a change is user-visible: a new/changed endpoint, anything in a `-ui` module, a login or
routing change, a permission token, a new store/pod. "It compiles and the unit test passes" is not QA.

---

## 1. Bring the whole stack up

```bash
sudo ./extra/scripts/configure-domain.sh   # once per machine — /etc/hosts for gateway.com, pods, demo stores
./extra/scripts/run-lcl.sh start           # infra + every Java service + both frontends
```

`run-lcl.sh` is *the* way to start it. It starts the compose infra (postgres, MinIO at `:9000`, `spg`,
monitoring), then each Java service under `--spring.profiles.active=lcl,test-stores` in dependency order
(**uaa first — it issues the tokens**), then `seller-ui` (:8010) and `landing-ui` (:8110), pre-building
landing-ui's workspace libs. Java services run **on the host**, not in Docker; `spg`'s `extra_hosts` map
service hostnames back to the host. A no-argument `./extra/scripts/run-lcl.sh` still means `start`.

| command / flag | effect |
|---|---|
| `start --list` | print configured services with current `running <pid>` / `stopped` / `port-used <pid>` status |
| `start uaa catalog` | only those services, plus infra |
| `start --no-infra` | compose is already up; also leaves it up on exit |
| `start --keep-infra` | stop the services, leave the containers running |
| `start --build` | `./gradlew build -x test -x check` first |
| `start -d` | start the stack in the background, wait for requested ports, then return |
| `start -d catalog` | start only the selected stopped service in the recorded stack, then return |
| `stop` | stop the recorded stack and delete compose volumes, logs, and pids |
| `stop catalog` | stop only the selected recorded service |
| `stop --volumes` | same cleanup as `stop`; kept for compatibility |
| `restart` | stop the recorded stack with cleanup, then start it again |
| `restart -d` | full stack restart in the background, wait for ports, then return |
| `restart catalog` | restart only the selected recorded service |
| `restart --volumes` | same cleanup as `restart`; rejected with selected service restart |
| `logs [service...]` | tail all logs, or selected logs such as `logs gateway catalog` |
| `pid [service...]` | print the recorded supervisor and service pids |

Ports come from `common-config.yml` — change one there and change it in the script's
`JAVA_SERVICES` / `NODE_SERVICES` tables too.

`start` removes old logs/runtime files and resets compose volumes before booting infra. It records its
supervisor and service pids under `build/lcl-runtime/`. Starting again first stops any recorded live stack, so a
stale run does not silently share ports with the new one. `stop <service...>` and `restart <service...>` use
those pid files to stop or replace only selected services while the supervisor keeps the rest of the stack
running. A selected stop leaves that service down until `restart <service>` or `start -d <service>`.
Selected service restart requires the stack infra to still be up; if Postgres is not answering on `:5432`, it
warns before starting the service. The supervisor blocks in the foreground tailing `build/lcl-logs/*.log`, and
brings everything down if any non-stopped/non-restarting service dies. Run it in the background when a test
needs to keep the stack available, inspect it with `pid`, and stop it with `stop`. Then verify: ports free and
`docker compose -f docker-compose-lcl.yml ps` empty. Internally, shutdown still uses `SIGTERM`; use the command
rather than manual `pkill`.

Use `start -d` or full `restart -d` when the terminal should return after startup; the background supervisor
continues writing startup output to `build/lcl-stack.log`.

Iterating on one frontend against an already-running backend? Don't restart everything — leave the stack up
and run `npm start` in that `-ui` module, or start a narrowed set
(`run-lcl.sh start --no-infra seller-ui`). One service by hand:
`./gradlew :store-pod:catalog:catalog-service:bootRun --args='--spring.profiles.active=lcl,test-stores'`.

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

---

## 5. Reading the evidence

| signal | where |
|---|---|
| service stdout | `build/lcl-logs/<service>.log` |
| logs / traces / metrics | grafana `http://localhost:3000` (Loki, Tempo, Prometheus) |
| a failing request's internals | the `traceId` on the ProblemDetail response, then that trace in Tempo |
| "the event never arrived" | `select * from outbox_record where status='FAILED'` — read `failure_reason` |
| "no instances available for X" | that service's entry in `lcl-config.yml` |

Response bodies never carry root-cause text; the detail is in the log, joined by `traceId`. Don't guess from
the HTTP body alone.

---

## 6. Known local gaps — expected, don't file them

- **No MinIO in `docker-compose-lcl.yml`**, but seeded media urls point at `http://localhost:9000/...` — so
  every logo, slider and product image on the storefront is broken locally. Expected.
- **Storefront login only works through the store host** (see §2).
- A store or pod you add locally needs its hostname added to `configure-domain.sh` too, or the browser gets
  "host not found".

---

## 7. Writing the QA document — `qa/<plan>.md`

The QA that a human tester runs lives in **one markdown file per plan or feature**, at the repo root under
`qa/`, named after the plan it belongs to (`qa/billing-per-store-subscriptions.md` for
`.claude/plans/billing-subscription-service.md`). Markdown so it reviews in the PR and travels with the branch.

**One file per plan, not one per phase.** A plan that ships in ten PRs still produces one QA document, appended
to as each phase lands. Ten files covering one feature is the failure mode: a tester has no idea which to run,
the same setup is repeated ten times and drifts, later phases silently invalidate earlier files, and cases end
up recorded as blocked in one file and passing in another with nothing connecting them. Fold each phase into
the existing document instead.

**`qa/billing-per-store-subscriptions.md` is the reference implementation.** Match its structure:

1. **Title and a short intro** — what changed, and why anyone should read this rather than the PR.
2. **A scope block** — `Scope` (the services touched) · `Change` (PR number, branch, plan path) · `Cases` (the
   count) · anything a tester must know before touching it (a test-mode key, a migration order).
3. **The tag legend.** Every case is tagged, and the tags are the point of the document:
   - **[verified]** — run against a running stack and passed.
   - **[unit only]** — the branch is covered by a named test, but nobody drove it through the stack. Name the
     test so the reader can judge.
   - **[not verified]** — never run end to end by anyone.

   A case nobody has executed is where the bugs are. **Marking it is more useful than implying otherwise** —
   never quietly present an unrun case as passing, and never drop it to keep the document tidy.
4. **`## 00 — Before you start`** — setup, logins, seeded ids, fixture SQL, and the queries that show the truth
   underneath. Everything a tester needs before case 1, in one place, so no case repeats it.
5. **Sections with a short prefix**, each opening with the design points a tester needs in order to judge what
   they see — especially anything deliberate that *looks* like a bug (fail-open here, fail-closed there;
   404 instead of 403). One section per theme, not one per phase.
6. **Cases as `### PFX-NN — what it proves · severity · [tag]`**, numbered within their section, with
   `- **Setup**` / `- **Steps**` / `- **Expect**` bullets. Mark the ones that matter `· critical` or `· high`.
   Add `- **Seen**` with the concrete evidence when the observation is worth more than "it passed" (the actual
   status codes, the log line, the timing). State plainly what is *expected to fail* so a tester does not spend
   a morning re-finding a known gap.
7. **`## MIG`** when there is a migration — the order, and what breaks silently if a step is skipped.
8. **`## REG — Regression watchlist`** — a table of every defect that actually happened during the work: what
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
- [ ] i18n: switched locale; the new keys resolve in **all five** (`en,ar,es,fr,ru`), no raw key on screen
- [ ] **AR checked as RTL** — layout and icon direction, not just the strings
- [ ] Console clean and no failed requests in the network panel for the flow
- [ ] For a new service/route: it resolves via `lb://`, and the edge (Caddyfile / `GatewayRouteLocatorImpl`)
      returns your API rather than seller-ui's HTML

---

## 9. Where automated tests fit

```bash
./gradlew test                 # everything
./gradlew unitTest             # only @Tag("unit-test")
./gradlew integrationTest      # only @Tag("integration-test")
./gradlew :store-pod:catalog:catalog-service:test --tests '*ProductApiTest*'
```

Both tasks come from `com.asrevo.java-application-conventions`. Integration tests use **Testcontainers
(Postgres, MinIO), so Docker must be running** — a `./gradlew test` failing at container startup is an
environment problem, not a test failure. These gate the merge; the browser/`.http` pass above is what proves
the feature.
