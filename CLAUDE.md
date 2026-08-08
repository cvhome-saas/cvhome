# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Orientation

`cvhome` is a multi-tenant e-commerce SaaS: Java 25 / Spring Boot microservices plus Angular and Next.js
frontends, in one Gradle composite build. Java package root `com.asrevo.cvhome.*`.

**Read the `project-structure` skill (`.claude/skills/project-structure/`) before navigating the repo or
deciding where new code belongs.** It is the authoritative map: the three trees (`store-commons/`,
`store-core/`, `store-pod/`), every service with its port and purpose, the tenancy model (orgs / stores /
pods), the `-commons`/`-core`/`-external-api`/`-service` module pattern, API and permission conventions,
config slices, gateways and local domains, service discovery, events/outbox, and per-topic reference files.
Don't re-derive that from source; don't duplicate it here.

**Layering of the rules:** the `project-structure` skill is the *rulebook* (why the architecture is shaped
this way); this file is the *enforcement layer* (what every change must satisfy). When they overlap they
agree — and if either drifts, the source wins: `settings.gradle` for modules, `common-config.yml` for
ports/hosts, `CustomPermissionEvaluator` for permission tokens, `schema.sql` for DDL, `BaseException`'s javadoc
for the error rules.

## House style & working modes

These shape **solution size and prose only** — never correctness, tenancy, security, or the gates below.

1. **Reuse before writing.** Climb the ladder before adding code: does it need to exist? → already in this
   repo (a value object in `commons/domain/`, a helper in `store-commons:autoconfigure`, an existing
   `-external-api` client, a populator/mapper)? → Spring/JDK built-in? → a dependency already in
   `libs.versions.toml`? → only then new code. Fix bugs at the root (the shared service/facade), not with a
   patch per caller. **Never** simplify away tenant scoping, `@PreAuthorize`, secret encryption, DDL updates,
   or anything in the Feature Checklist.
2. **Match the repo over the "objectively best" pattern.** A pod follows the
   `-commons`/`-core`/`-external-api`/`-service` split, endpoints take `StoreMerchantId` + `LanguageCode`,
   config lives in the shared slices. Do it the way the neighbouring service does it, even when a shorter
   path exists.
3. **Terse prose.** Explanations stay short; fragments fine. Code, commit messages and PR-review comments
   stay normal English. Never compress a security caveat or a multi-step order into ambiguity.

**Precedence when these clash:** repo workflow > reuse > brevity. Tenant isolation, permission gates and
secret encryption never yield to a smaller diff.

## Build & test

Gradle wrapper (9.2.0) drives Java *and* the npm apps. All commands from the repo root.

```bash
./gradlew build -x test -x check                 # full build, what CI's build job runs
./gradlew :store-pod:catalog:catalog-service:build
./gradlew test                                    # all tests
./gradlew :store-pod:catalog:catalog-service:test --tests '*ProductApiTest*'   # single test
./gradlew unitTest            # only @Tag("unit-test")
./gradlew integrationTest     # only @Tag("integration-test")
./gradlew checkstyleMain checkstyleTest           # CI quality job; maxWarnings = 0
./gradlew :store-core:seller-ui:bootBuildImage    # docker image (Spring apps and -ui apps alike)
```

Toolchain, the checkstyle rule set and the test-tag split: `references/build-system.md` in the skill. What
binds every change:

- **Checkstyle failures block CI** — warnings = errors, and a `TODO` comment fails the build.
- **Docker must be running** for `./gradlew test` (Testcontainers).
- Module paths come from `settings.gradle`; dependency versions from `gradle/libs.versions.toml`. Never
  hardcode a version in a `build.gradle`, and never add a module without `settings.gradle`.

## Frontends

The three delivery patterns (`-ui` npm modules, uaa's embedded SPA, Thymeleaf) and their build wiring:
`references/frontends.md`; landing-ui's workspace and themes: `references/landing-ui.md`.

```bash
cd store-core/seller-ui  && npm start          # Angular 20, ng serve
cd store-pod/landing-ui  && npm run dev        # Next.js 16, npm workspaces (app, libs/*, templates/*)
```

- **Never hand-edit `store-core/seller-ui/angular.json` to get hot reload** — SSR lives in the `production`
  configuration by design and `ng serve` already runs CSR. An uncommitted diff there gets reverted.
- **Build landing-ui from the root** (`npm run build` chains libs → templates → app); `app` alone compiles
  against stale types.
- Angular work: the `angular-developer` skill. Next.js/React: `vercel-react-best-practices`.
- To *exercise* a frontend (QA, reproducing a UI bug, anything needing real data) start the backend too —
  see **Running locally**; `npm start` alone is not enough.

## Running locally

```bash
sudo ./extra/scripts/configure-domain.sh   # once — /etc/hosts entries for gateway.com, pods, demo stores
./extra/scripts/run-lcl.sh                 # the whole stack: infra + every Java service + both frontends
```

`run-lcl.sh` is the way to bring the stack up. It starts the compose infra (postgres, spg, monitoring), then
each Java service under `--spring.profiles.active=lcl,test-stores` in dependency order (uaa first — it issues
the tokens), then `seller-ui` (:8010) and `landing-ui` (:8110), pre-building landing-ui's workspace libs.
It **blocks in the foreground** tailing `build/lcl-logs/*.log`; Ctrl-C (or SIGTERM) tears everything down,
containers included, and it also brings the rest down if any one service dies. Ports come from
`common-config.yml` — change one there and change it in the script's `JAVA_SERVICES`/`NODE_SERVICES` tables too.

```bash
./extra/scripts/run-lcl.sh --list             # what would start, then exit — safe dry run
./extra/scripts/run-lcl.sh uaa catalog        # only those services, plus infra
./extra/scripts/run-lcl.sh --no-infra         # compose already up; also leaves it up on exit
./extra/scripts/run-lcl.sh --keep-infra       # stop the services, leave the containers running
./extra/scripts/run-lcl.sh --build            # ./gradlew build -x test -x check first
```

**Testing UI / reproducing a frontend bug — including QA and browser-driven work — starts here.** A UI bug
usually needs the backend behind it (gateway, uaa, the pod service serving the data), so start the stack
rather than `npm start` alone. Entry points, with the demo logins the `test-stores` profile seeds:

| what | url | login |
|---|---|---|
| seller console | `http://gateway.com:8000/` | `org1-admin` / `admin` |
| demo storefront | `http://org1-store1.spg-507f1f77.gateway.com` | `user` / `revo` |
| grafana | `http://localhost:3000` | — |

Those credentials are local seed data only (`store-core/uaa/.../init-sql/data-test-stores.sql` and the pods'
`init-sql/stores/*`) and exist solely because the `test-stores` profile is active — they are not secrets and
never appear outside `lcl`. If a login fails, the stack almost certainly came up without `test-stores`. The
storefront account is scoped per store (`cua.users.client_id`), so it only authenticates through the store
host — posting to `localhost:8124/login` directly always fails, and that is not a bug.

**Known local gap:** `docker-compose-lcl.yml` has no MinIO, but the seeded media urls point at
`http://localhost:9000/...`, so every logo, slider and product image on the storefront is broken locally.
Expected — don't file it as a UI bug.

Because it blocks, run it in the background (`run_in_background`) and watch `build/lcl-logs/` — and
**check whether it is already running before starting it again**: the script skips any service whose port is
already bound, so a second run against a live stack starts nothing useful and its exit tears the containers
down. `./extra/scripts/run-lcl.sh --list` plus a port probe (`lsof -i :8000`, `:8010`, `:8110`) is the cheap
check. To iterate on one frontend against an already-running backend, don't restart everything — leave the
stack up and run `npm start` in that `-ui` module, or start a narrowed set (`run-lcl.sh --no-infra seller-ui`).

To stop a backgrounded run, send it **`SIGTERM`** (`pkill -TERM -f "bash ./extra/scripts/run-lcl.sh"`), not
`SIGINT` — a shell that starts the script in the background inherits SIGINT as ignored and no trap can
override that, so Ctrl-C/`kill -INT` is silently a no-op there. Then verify: ports free and
`docker compose -f docker-compose-lcl.yml ps` empty. Ctrl-C only works when it is in the foreground.

Java services run **on the host**, not in Docker; `spg`'s `extra_hosts` map service hostnames back to the
host. Profiles: `lcl`, `fargate`, `test-stores`. Ports, hosts and namespaces are declared once in
`store-commons/autoconfigure/src/main/resources/common-config.yml` — change them there, never inline. To run a
single service by hand:
`./gradlew :store-pod:catalog:catalog-service:bootRun --args='--spring.profiles.active=lcl,test-stores'`.

## Working conventions

- **Never commit to `develop` (or `main`) directly.** Any change starts with a fresh branch cut from an
  up-to-date `develop` (`git fetch && git switch -c <type>/<short-name> origin/develop`) and lands via PR
  into `develop`; `main` is the release branch. CI runs on both. If you find yourself already on `develop`
  with edits, branch first, then commit.
- **`/go` ships the working tree** (branch if needed → commit → push → PR into `develop`, template filled,
  changelog label) and **`/reset` returns to a clean `develop`** without losing work. Both live in
  `.claude/commands/`; prefer them over doing the sequence by hand.
- **PR body follows `.github/PULL_REQUEST_TEMPLATE.md`**: title `<type|area>: <what changed>`, then
  *Why* → *What* → *The parts that are not obvious* → *Deviations* → *Verification*, then the checklist with
  the untouched sections deleted. Label it before merge — `.github/release.yml` builds the changelog from
  `type/*` and `warn/*` labels, and an unlabelled PR lands in "Other Changes".
- Every new endpoint takes `StoreMerchantId merchantStore` + `LanguageCode language` (supplied by argument
  resolvers from the `store`/`lang` query params) and carries
  `@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','LAYER.DOMAIN.ACTION')")`.
- **Every endpoint ships a runnable request.** Adding or changing one means adding or changing its block in
  `<service>/http/<api-class>.http` (IntelliJ HTTP Client format, one file per `*Api` class, named after the
  class kebab-cased). Address it through the gateway — `{{SELLER_UI_URL}}/spg/catalog/…`, never the service's
  own port — with `?store={{STORE_ID}}&lang={{LANG}}`. Shared vars live in the repo-root
  `http-client.env.json`; session ids in the gitignored `http-client.private.env.json` (copy the `.example`).
  Reference: `store-pod/catalog/catalog-service/http/product-api.http`; rules:
  `references/http-request-files.md` in the skill.
- Use the value objects in `store-commons/commons/.../domain/` instead of raw `String`/`Long` ids.
- `schema.sql` (`src/main/resources/schema.sql` for control-plane's Spring Data JDBC, `init-sql/schema.sql`
  for the JPA pod services) is the source of truth for DDL — `ddl-auto: update` is only a safety net.
- Tenant secrets are encrypted in the mapper layer via `secret-crypto`; never add a plaintext credential
  column.
- A new service needs entries in **all three** of `common-config.yml`, `lcl-config.yml`,
  `fargate-config.yml` to be resolvable via `lb://`.

## Error handling

Rules, hierarchy and the corrections behind them: `references/error-handling.md` in the skill — read it before
touching errors. What binds every change:

- **Throw and declare condition-named classes only** (`DuplicateSkuException`), never `BaseException` or a
  category base. Throwing one is a compile error (bases are abstract); *declaring* one still compiles, so it is
  a review/grep gate. Exceptions are checked by design — let the new failure mode break callers.
- **Category names the parent, condition names the class**, one class per condition with a static `of(...)`
  factory over that context's `ErrorCode` enum. Catch narrowly, never `switch` on `category()`.
- **Get *who failed* right:** us → our code and status; a peer cvhome service → `RemoteServiceException`, the
  remote's code re-emitted; a third party → `ExternalProviderException`, our code with theirs as `providerCode`.
  Inside a provider call also split *decided* (refused, 422) from *undecided* (no answer, 502) — collapsing them
  cancels orders that were charged.
- **Bodies come only from `ProblemDetailFactory`**, via the single `@ControllerAdvice` with no `basePackages`.
  No root-cause text in `detail`; internal detail stays in the log, joined by `traceId`.
- **A nicer `ErrorCode` on a legacy exception is not a migration** — the signature still says nothing. `LEGACY.*`
  marks un-migrated throw sites; `payment` is the reference implementation.

## Review policy

**The rows live in `.github/PULL_REQUEST_TEMPLATE.md`** — Placement / API / Persistence / Secrets / Errors /
Integration / Configuration / Frontend, one gate per row, each with a repo mechanism behind it that fails
silently when skipped. Every PR that adds or extends a feature is checked against them: delete the sections
the change does not touch, and treat a section you keep as mandatory. The `project-structure` skill explains
*why* each row exists; this file and that template are the enforcement.

**Reject or fix on sight** — the violations that recur, each bypassing a mechanism:

- A `TODO` comment (checkstyle `TodoComment` fails the build), a star import, a 140+ char line
- A hardcoded host, port, or service URL instead of `common-config.yml` + `lb://<service>`
- A hardcoded dependency version in a `build.gradle` instead of `libs.versions.toml`
- A raw `String`/`Long` where a `commons/domain/` value object exists
- A controller method missing `@PreAuthorize`, or authorization done with an inline role/authority check
- A new permission token with no `case` in `CustomPermissionEvaluator` — it denies by default, so it 403s silently
- An endpoint added or changed with no matching `.http` block, a `.http` request aimed at a service's own port
  instead of the gateway, or a session id / secret committed to `http-client.env.json`
- A plaintext secret column, or a credential written to a log
- An entity/column change with no matching `schema.sql` edit, or a new enum value not added to the `CHECK`
  constraint
- A consumer depending on another pod's `-core`/`-service`, or reaching into another service's schema
- A non-idempotent `@OutboxHandler`
- A new endpoint that ignores `StoreMerchantId`, or a repository query not scoped by store
- An i18n key added to `en` only
- `throws BaseException` (or a category base), a new `LEGACY.*` throw site, or `catch (BaseException)` +
  `switch (category())`
- A hand-built `ProblemDetail`, a second `@ControllerAdvice`, or root-cause text in `detail`
- A provider's code/status re-emitted as ours, or a rejection and a transport failure sharing one `catch`
- `buildClient(...)` with no catalog argument, or caller-side types on the server interface
- A `catch` that swallows a typed failure and returns a success shape (200 on a rejection kills the typed path)
- A hand-edited `store-core/seller-ui/angular.json`

**Verification gates — all mandatory before saying done:**

- [ ] `./gradlew checkstyleMain checkstyleTest` clean (warnings = errors)
- [ ] `./gradlew build -x test -x check` clean
- [ ] `./gradlew test` (or the touched module's `:test`) clean, Docker running for Testcontainers
- [ ] Touched frontend builds: `npm run build` in that `-ui` module
- [ ] User-visible change exercised against a running stack, not just unit-tested

## Plans

Implementation plans live in `.claude/plans/<kebab-case-name>.md`, in-repo so they travel with the branch and
any tool can read them. Use a descriptive name (`stripe-refund-flow.md`, not an auto-generated one). When
asked to "create a plan" or entering plan mode, write the file there.
