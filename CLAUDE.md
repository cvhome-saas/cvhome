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
ports/hosts, `CustomPermissionEvaluator` for permission tokens, `schema.sql` for DDL.

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

`landing-ui`'s build order matters — `npm run build` chains libs → templates → app; building `app` alone
against stale libs will use old types. `uaa`'s Angular SPA (`store-core/uaa/src/main/resources/uaa-fe`) is
**not** a Gradle module; it is built by the node plugin and copied into `static/` during `processResources`.

Angular work: use the `angular-developer` skill. Next.js/React work: `vercel-react-best-practices`.

## Running locally

```bash
sudo ./extra/scripts/configure-domain.sh   # once — /etc/hosts entries for gateway.com, pods, demo stores
docker compose -f docker-compose-lcl.yml up -d   # postgres, spg (Caddy), otel/monitoring only
./gradlew :store-pod:catalog:catalog-service:bootRun --args='--spring.profiles.active=lcl'
```

Java services run **on the host**, not in Docker; `spg`'s `extra_hosts` map service hostnames back to the
host. Profiles: `lcl`, `fargate`, `test-stores`. Ports, hosts and namespaces are declared once in
`store-commons/autoconfigure/src/main/resources/common-config.yml` — change them there, never inline.

## Working conventions

- Branch off and PR into **`develop`** (`main` is the release branch); CI runs on both.
- Every new endpoint takes `StoreMerchantId merchantStore` + `LanguageCode language` (supplied by argument
  resolvers from the `store`/`lang` query params) and carries
  `@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','LAYER.DOMAIN.ACTION')")`.
- Use the value objects in `store-commons/commons/.../domain/` instead of raw `String`/`Long` ids.
- `schema.sql` (`src/main/resources/schema.sql` for control-plane's Spring Data JDBC, `init-sql/schema.sql`
  for the JPA pod services) is the source of truth for DDL — `ddl-auto: update` is only a safety net.
- Tenant secrets are encrypted in the mapper layer via `secret-crypto`; never add a plaintext credential
  column.
- A new service needs entries in **all three** of `common-config.yml`, `lcl-config.yml`,
  `fargate-config.yml` to be resolvable via `lb://`.

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

**Persistence**
- [ ] Table/column added to the service's DDL (`schema.sql` for control-plane's Spring Data JDBC,
      `init-sql/schema.sql` for the JPA pod services) — an entity change alone is not a schema change
- [ ] New enum value also added to the `varchar` `CHECK` constraint
- [ ] Query is tenant-scoped by store; no cross-service foreign key, no cross-schema join

**Secrets**
- [ ] Tenant-supplied credentials encrypted in the mapper (`toEntity` encrypt / `toDTO` decrypt, guarded by
      `EncryptedValue.isEncrypted`) via `secret-crypto` — never a plaintext column, never logged

**Integration**
- [ ] Synchronous cross-service call declared as a `@HttpExchange` interface in the provider's
      `-external-api`, implemented by its `External*Api` controller, consumed via `RestClientBuilder`
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

## Flag in review

Reject or fix on sight — each of these has a repo mechanism that is being bypassed:

- A `TODO` comment (checkstyle `TodoComment` fails the build), a star import, a 140+ char line
- A hardcoded host, port, or service URL instead of `common-config.yml` + `lb://<service>`
- A hardcoded dependency version in a `build.gradle` instead of `libs.versions.toml`
- A raw `String`/`Long` where a `commons/domain/` value object exists
- A controller method missing `@PreAuthorize`, or authorization done with an inline role/authority check
- A plaintext secret column, or a credential written to a log
- An entity/column change with no matching `schema.sql` edit
- A consumer depending on another pod's `-core`/`-service`, or reaching into another service's schema
- A non-idempotent `@OutboxHandler`
- A new endpoint that ignores `StoreMerchantId`, or a repository query not scoped by store
- An i18n key added to `en` only

## Plans

Implementation plans live in `.claude/plans/<kebab-case-name>.md`, in-repo so they travel with the branch and
any tool can read them. Use a descriptive name (`stripe-refund-flow.md`, not an auto-generated one). When
asked to "create a plan" or entering plan mode, write the file there.
