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

Module paths come from `settings.gradle` — it is the source of truth for what is a Gradle module versus a
grouping folder. Only that file lists modules; there is no root `build.gradle`.

- **Checkstyle failures block CI** (`config/checkstyle/checkstyle.xml`, warnings = errors). Notable rules:
  140-char lines, no star imports, no unused imports, `DeclarationOrder`, `MissingSwitchDefault`,
  `MultipleStringLiterals`, and **`TodoComment` — a `TODO` comment fails the build**. Reports land in
  `build/reports/checkstyle/`.
- **Toolchain is Java 25** (`java-common-conventions`), and CI sets up Corretto 25 — the README's "JDK 21" is
  stale. Gradle needs a JDK it can run on; toolchains are auto-provisioned via foojay.
- Integration tests use Testcontainers (Postgres, MinIO), so **Docker must be running** for `./gradlew test`.
- Dependency versions live in `gradle/libs.versions.toml`; convention plugins in `build-logic/`. Never
  hardcode a version in a `build.gradle`.

## Frontends

`-ui` modules are npm apps wrapped by the `ui-conventions` plugin: Gradle `build` → `npm run build`, Gradle
`bootRun` → `npm run dev`. Node/npm are downloaded by the node plugin, so Gradle tasks work without a local
Node install; running npm directly in the module also works.

```bash
cd store-core/seller-ui  && npm start          # Angular 20 SSR, ng serve
cd store-pod/landing-ui  && npm run dev        # Next.js 16, npm workspaces (app, libs/*, templates/*)
```

**seller-ui SSR vs. hot reload is already solved in `angular.json` — don't hand-edit it.** SSR (`server`,
`outputMode: server`, `ssr.entry`) lives in the `production` build configuration only; the `development`
configuration — what `ng serve` / `npm start` / `run-lcl.sh` use by default — is plain CSR, so HMR works.
`ng build` still defaults to production and emits `dist/seller-ui/server/`. So: never strip the SSR block to
get hot reload (that breaks the real build), and never commit a local `angular.json` diff — if you see one
uncommitted, it predates this setup and should be reverted (`git checkout -- store-core/seller-ui/angular.json`).

`landing-ui`'s build order matters — `npm run build` chains libs → templates → app; building `app` alone
against stale libs will use old types. `uaa`'s Angular SPA (`store-core/uaa/src/main/resources/uaa-fe`) is
**not** a Gradle module; it is built by the node plugin and copied into `static/` during `processResources`.

Angular work: use the `angular-developer` skill. Next.js/React work: `vercel-react-best-practices`. To
*exercise* a frontend (QA, reproducing a UI bug, anything needing real data) start the backend too — see
**Running locally & QA**; `npm start` alone is not enough.

## Running locally & QA

```bash
sudo ./extra/scripts/configure-domain.sh   # once — /etc/hosts entries for gateway.com, pods, demo stores
./extra/scripts/run-lcl.sh                 # the whole stack: infra + every Java service + both frontends
```

**Read `references/qa-testing.md` in the `project-structure` skill before running the stack or QA-ing a
change.** It has the flags, the demo logins, browser-driven QA, `.http` API QA, where the logs and traces
are, the known local gaps, and the QA checklist. Don't re-derive it here.

What binds every change:

- **A user-visible change is not done until it has been exercised end to end** — through the gateway, in the
  browser or via its `.http` blocks. Passing unit tests is not QA.
- `run-lcl.sh` **blocks and tears the whole stack down on exit** — run it in the background, check it isn't
  already running first (`--list` + `lsof -i :8000`), and stop it with **`SIGTERM`**, never `SIGINT`.
- QA proves **tenant isolation and the permission gate**, not just the happy path: repeat the action as a
  second store, and confirm a principal without the token gets 403.
- Java services run **on the host**, not in Docker. Profiles: `lcl`, `fargate`, `test-stores`. Ports, hosts
  and namespaces come from `common-config.yml` — never inline one.

## Working conventions

- **Never commit to `develop` (or `main`) directly.** Any change starts with a fresh branch cut from an
  up-to-date `develop` (`git fetch && git switch -c <type>/<short-name> origin/develop`) and lands via PR
  into `develop`; `main` is the release branch. CI runs on both. If you find yourself already on `develop`
  with edits, branch first, then commit.
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

## Feature checklist (review policy)

Apply to every PR that adds or extends a feature. The `project-structure` skill explains *why*; this is the
enforcement layer. Tick only the rows the change actually touches — but a touched row is mandatory.

**Placement**
- [ ] Entities + `Readable*`/`Persistable*` DTOs in `<domain>-commons`; business logic (services, facades,
      populators, repositories) in `<domain>-core`; controllers/`SecurityConfig` in `<domain>-service`
- [ ] New Gradle module added to `settings.gradle` and applying a `build-logic` convention plugin
- [ ] No dependency on another pod's `-core` or `-service` — cross-service calls go through that pod's
      `-external-api`

**API**
- [ ] Endpoint signature takes `StoreMerchantId merchantStore` and `LanguageCode language` (unannotated)
- [ ] `@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.<DOMAIN>.*')")` present —
      `STORE-CORE.<DOMAIN>.<ACTION>` for platform endpoints. No inline role/authority checks
- [ ] A genuinely new permission token has a `case` in `CustomPermissionEvaluator` **and** a method on
      `PermissionAccessChecker` (`store-commons/autoconfigure`, `com.asrevo.cvhome.s2s`) — the evaluator
      denies by default, so a token with no case silently 403s
- [ ] Ids/codes use value objects from `store-commons/commons/.../domain/`, not raw `String`/`Long`
- [ ] Request DTOs validated (`@Valid` + bean-validation annotations)
- [ ] Endpoint has a runnable block in `<service>/http/<api-class>.http` — gateway path form, not the service
      port; `?store={{STORE_ID}}&lang={{LANG}}`; a new url/id added to `http-client.env.json` rather than
      inlined; at least one non-2xx block where the endpoint declares a failure mode

**Persistence**
- [ ] Table/column added to the service's DDL (`schema.sql` for control-plane's Spring Data JDBC,
      `init-sql/schema.sql` for the JPA pod services) — an entity change alone is not a schema change
- [ ] New enum value also added to the `varchar` `CHECK` constraint
- [ ] Query is tenant-scoped by store; no cross-service foreign key, no cross-schema join

**Secrets**
- [ ] Tenant-supplied credentials encrypted in the mapper (`toEntity` encrypt / `toDTO` decrypt, guarded by
      `EncryptedValue.isEncrypted`) via `secret-crypto` — never a plaintext column, never logged

**Errors** (details: `references/error-handling.md`)
- [ ] New failure mode = `ErrorCode` constant + one condition-named exception in that `-commons`, declared by
      name on the throwing method and on `I<Domain>Service` if exposed over HTTP
- [ ] No new `LEGACY.*`; a touched legacy throw site is migrated, not just re-coded
- [ ] Ran the skill's grep gates: generic `throws` over the touched module, and old type names (comments
      included) after a rename

**Integration**
- [ ] Synchronous cross-service call declared as a `@HttpExchange` interface in the provider's
      `-external-api`, implemented by its `External*Api` controller, consumed via `RestClientBuilder`
- [ ] `buildClient(...)` gets an explicit error contract — `*ApiErrors.CATALOG`, or `RemoteErrorCatalog.none()`
      when the API names no failures
- [ ] Caller-side exception types on the **`@HttpExchange` interface's** `throws` clause (not the server one) —
      that is what delivers them narrowed; a `.map(...)` entry alone does not. Reactive callers use `onErrorMap`
- [ ] Asynchronous work published as a domain event from an aggregate root, event type in an `-events`
      module, `@OutboxHandler` **idempotent** (delivery is at-least-once)
- [ ] uaa user management goes through `UserAccountService` (`uaa-client`), stamping `org` + `store`

**Configuration**
- [ ] Ports/hosts/namespaces changed in `common-config.yml` only; a new service registered in
      `common-config.yml` + `lcl-config.yml` + `fargate-config.yml`
- [ ] New route reflected in `store-pod/spg/Caddyfile` (pod edge) or `GatewayRouteLocatorImpl`/`PodClient`
      (platform edge), and in `configure-domain.sh` if a new local hostname is involved
- [ ] Dependency versions added to `gradle/libs.versions.toml`, referenced as `libs.*`

**Frontend**
- [ ] i18n keys added to **all five locales** — no orphans: seller-ui `public/assets/i18n/{en,ar,es,fr,ru}.json`,
      landing-ui `locales/{en,ar,es,fr,ru}.json`, cua `messages_{en,ar,es,fr,ru}.properties` (+ the default
      `messages.properties`)
- [ ] Angular: standalone components, `OnPush` change detection, `inject()`, signals, HTTP through a service
      never inside a component (see the `angular-developer` skill)
- [ ] landing-ui: a change to `libs/*` or `templates/*` is built through the root `npm run build` chain, and
      a new theme follows `references/new-landing-ui-template.md`
- [ ] AR is an RTL locale — check layout, not just the strings

**Verification gates (all mandatory before saying done)**
- [ ] `./gradlew checkstyleMain checkstyleTest` clean (warnings = errors)
- [ ] `./gradlew build -x test -x check` clean
- [ ] `./gradlew test` (or the touched module's `:test`) clean, Docker running for Testcontainers
- [ ] Touched frontend builds: `npm run build` in that `-ui` module
- [ ] User-visible change QA'd against the local stack — the checklist in the skill's `references/qa-testing.md`

## Flag in review

Reject or fix on sight — each of these has a repo mechanism that is being bypassed:

- A `TODO` comment (checkstyle `TodoComment` fails the build), a star import, a 140+ char line
- A hardcoded host, port, or service URL instead of `common-config.yml` + `lb://<service>`
- A hardcoded dependency version in a `build.gradle` instead of `libs.versions.toml`
- A raw `String`/`Long` where a `commons/domain/` value object exists
- A controller method missing `@PreAuthorize`, or authorization done with an inline role/authority check
- An endpoint added or changed with no matching `.http` block, a `.http` request aimed at a service's own port
  instead of the gateway, or a session id / secret committed to `http-client.env.json`
- A plaintext secret column, or a credential written to a log
- An entity/column change with no matching `schema.sql` edit
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

## Plans

Implementation plans live in `.claude/plans/<kebab-case-name>.md`, in-repo so they travel with the branch and
any tool can read them. Use a descriptive name (`stripe-refund-flow.md`, not an auto-generated one). When
asked to "create a plan" or entering plan mode, write the file there.
