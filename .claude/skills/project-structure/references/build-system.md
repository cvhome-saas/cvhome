# Build system, versioning, and configuration

## Root layout

The root `build.gradle` is one line of plugin: it aggregates coverage and nothing else. Everything about a module
lives in that module's own `build.gradle` plus the convention plugins. The root also holds:

- `settings.gradle` — `rootProject.name = 'cvhome'`, `pluginManagement { includeBuild('build-logic') }`, and an
  explicit `include(...)` list of every module (~44 paths). **This is the authoritative list of build units.**
- `gradle/libs.versions.toml` — the version catalog.
- `gradlew` / `gradlew.bat`, `gradle.properties`, `lombok.config`, `config/checkstyle/`.
- `docker-compose-lcl.yml` — local infrastructure.

## `gradle/libs.versions.toml` — the version catalog

**Every dependency version lives here. Never hardcode a version in a `build.gradle`.**

Three sections:

- **`[versions]`** — the version numbers, referenced by alias.
- **`[libraries]`** — `name = { module = "group:artifact", version.ref = "someVersion" }`.
- **`[plugins]`** — plugin ids, including the in-repo convention plugins.

In a `build.gradle` you reference them as `libs.<name>` with dots replacing dashes:

```groovy
implementation libs.spring.boot.starter.web      // spring-boot-starter-web
implementation libs.mapstruct                     // mapstruct
plugins { alias(libs.plugins.spring.boot) }       // spring-boot
```

### Key pinned versions

| Area | Version(s) |
|---|---|
| Spring Boot | `springBoot = 4.0.1`, `springDataCommons = 4.0.1`, `springWeb = 7.0.2`, `springCloudCommons = 5.0.0` |
| Persistence | `hibernate = 7.2.0.Final`, `postgresql = 42.7.8`, `jakartaPersistenceApi = 3.2.0` |
| Mapping / boilerplate | `lombok = 1.18.42`, `mapstruct = 1.6.3`, `lombokMapstructBinding = 0.2.0` |
| JSON | `jackson = 3.0.3` (**note: `tools.jackson.core` — Jackson 3**), `fasterxml = 2.19.0` (annotations only, still `com.fasterxml`) |
| Docs | `springdoc = 3.0.1`, `swagger = 2.2.8` |
| AWS / integrations | `awsSdk = 2.41.5` (s3, kms, servicediscovery), `stripe = 31.1.0`, `mongodbBson = 5.6.2`, `dnsjava = 3.6.3` |
| Messaging | `namastack-outbox = 1.7.1` (transactional outbox: `-starter-jdbc`, `-starter-jpa`, `-api`) |
| Testing | `testcontainers = 1.19.7`, `testcontainersjupiter = 2.0.3`, `hamcrestRecord = 1.0.0` |
| Frontend tooling | `nodeGradle = 7.0.2` (the Gradle node plugin) |

**Jackson 3 is a real trap:** databind/core come from `tools.jackson.core`, but `jackson-annotations` is still
`com.fasterxml.jackson.core` on the 2.x line. Use the catalog aliases rather than writing coordinates by hand.

Two BOMs are imported directly in service `build.gradle` files rather than through the catalog:
`org.springframework.cloud:spring-cloud-dependencies:2025.1.0` and
`io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.27.0`.

## `build-logic/` — convention plugins

A composite build (`includeBuild('build-logic')`), so its plugins are available to every module but it is not
itself in the `include(...)` list. Plugins in
`build-logic/src/main/groovy/`:

| Plugin id | Applied by | Provides |
|---|---|---|
| `com.asrevo.java-common-conventions` | (base) | Java toolchain, checkstyle, **unit-test wiring (`src/test`, JUnit/AssertJ/Mockito/ArchUnit) and JaCoCo** |
| `com.asrevo.java-library-conventions` | every `-commons`, `-core`, `-external-api`, `-events` | `java-common-conventions` + `java-library` |
| `com.asrevo.java-integration-test-conventions` | (applied by application conventions) | the `src/integrationTest` source set, its Testcontainers classpath, `store-commons:test-support`, and the module's integration coverage report |
| `com.asrevo.java-application-conventions` | every `-service`, `uaa`, `cua` | integration-test conventions + `application` + image helpers |
| `com.asrevo.jacoco-aggregate-conventions` | the **root** project only | `coverageReport` (whole monorepo), `domainCoverage` / `printDomainCoverage` (unit, integration and merged reports per domain) and the three ratcheted gates `domainCoverageVerification` reads from `domainCoverageMinimum` in the root `build.gradle` |
| `com.asrevo.docker-conventions` | services and UIs | `bootBuildImage` helpers `createImageName()` / `createImageTags()`, ECR publish wiring |
| `com.asrevo.ui-conventions` | `console-ui`, `landing-ui` | node plugin + npm build/dev/clean wiring (see `frontends.md`) |

A typical `-service` `build.gradle` therefore reads:

```groovy
plugins {
    alias(libs.plugins.java.application.conventions)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}
group = 'com.asrevo.cvhome'
springBoot { buildInfo() }
bootBuildImage {
    imageName = createImageName("store-pod/<name>", project.version)
    tags      = createImageTags("store-pod/<name>", project.version)
    // JLink-slimmed JVM via BP_JVM_JLINK_ENABLED
}
```

## Toolchain, checkstyle and the test tasks

**Java 25.** `java-common-conventions` sets the toolchain and CI installs Corretto 25. Toolchains are
auto-provisioned via foojay, so a missing JDK downloads itself — but Gradle still needs a JDK it can *run* on.

**Checkstyle failures block CI** — `config/checkstyle/checkstyle.xml`, `maxWarnings = 0`, so a warning is an
error. Reports land in `build/reports/checkstyle/`. The rules that bite in practice:

| Rule | Effect |
|---|---|
| `TodoComment` | **a `TODO` comment fails the build** — finish it or leave it out |
| `LineLength` | 140 characters |
| `AvoidStarImport`, `UnusedImports` | no `import x.*`, no unused imports |
| `DeclarationOrder` | fields → constructors → methods, statics first |
| `MissingSwitchDefault` | every `switch` needs a `default` |
| `MultipleStringLiterals` | the same literal twice in one file → extract a constant |

Tests are split by **source set**, not by tag (`@Tag` is gone — do not reintroduce it): `src/test` holds `*Test`
(no Spring, no Docker), `src/integrationTest` holds `*IntegrationTest` (full context + Testcontainers).

```bash
./gradlew test                 # unit + architecture tests, no Docker
./gradlew integrationTest      # Testcontainers; Docker MUST be running
./gradlew check                # both + checkstyle + verifyTestNaming + the per-domain coverage gates
./gradlew check -x integrationTest             # laptop without Docker
./gradlew checkstyleMain checkstyleTest checkstyleIntegrationTest   # what CI's quality job runs
./gradlew domainCoverage printDomainCoverage   # build/reports/coverage/<domain>/ + a domain × {unit, integration, merged} table
./gradlew :store-pod:catalog:catalog-service:test --tests '*PagesTest*'
```

A container failing to start is an environment problem, not a test failure. **Full rules, naming standard, the
`test-support` catalogue and the coverage ratchet: `references/testing.md`.**

## Configuration

Shared configuration ships **inside** the `store-commons:autoconfigure` jar, and each service imports slices
from the classpath: `common-config.yml` (always) + an environment slice (`lcl` / `fargate`) + a layer slice
(`store-core-*` / `store-pod-*`). Profiles are `lcl`, `fargate`, `test-stores`.

**To change a port, hostname, or namespace, edit `common-config.yml`**, not the individual service.

Full detail — including the service registry, the composition rule per layer, and what each slice sets —
is in `configuration.md`.

## Common commands

```bash
./gradlew clean build -x test          # build everything, skip tests
./gradlew :store-pod:catalog:catalog-service:bootRun    # run one service
./gradlew :store-core:console-ui:bootRun                 # npm run dev, via ui-conventions
./gradlew :store-core:uaa:build                         # also builds + embeds uaa-fe
docker compose -f docker-compose-lcl.yml up             # postgres, spg, otel-collector, loki, tempo, prometheus, grafana
sudo ./extra/scripts/configure-domain.sh                # one-off: /etc/hosts entries for *.gateway.com
```

Run `configure-domain.sh` **before** the first local run — services address each other by hostname
(`merchant.gateway.com`, `spg-507f1f77.gateway.com`, `org1-store1.spg-507f1f77.gateway.com`), not `localhost`,
and nothing resolves without those entries. See `gateways-and-local-domains.md`.

Local infra in `docker-compose-lcl.yml`: `postgres:15-alpine`, `saas-gateway` (spg image),
`otel/opentelemetry-collector-contrib`, `grafana/loki`, `grafana/tempo`, `prom/prometheus`, `grafana/grafana` —
i.e. the full OpenTelemetry logs/traces/metrics stack, matching the OTel starters wired into every service.
