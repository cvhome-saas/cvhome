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
| `com.asrevo.java-application-conventions` | every `-service`, `uaa`, `cua` | integration-test conventions + `application` + image helpers, and the image itself: ECR registry, jlink, the JVM/native switch (`-Pnative`, see *Native images*) |
| `com.asrevo.jacoco-aggregate-conventions` | the **root** project only | `coverageReport` (whole monorepo), `domainCoverage` / `printDomainCoverage` (unit, integration and merged reports per domain) and the three ratcheted gates `domainCoverageVerification` reads from `domainCoverageMinimum` in the root `build.gradle` |
| `com.asrevo.docker-conventions` | services and UIs | `bootBuildImage` helpers `createImageName()` / `createImageTags()`, ECR publish wiring |
| `com.asrevo.ui-conventions` | `console-ui`, `landing-ui` | node plugin + npm build/dev/clean wiring (see `frontends.md`) |

A typical `-service` `build.gradle` therefore reads:

```groovy
plugins {
    alias(libs.plugins.java.application.conventions)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.graalvm.native)          // processAot in every build; native on -Pnative
}
group = 'com.asrevo.cvhome'
springBoot { buildInfo() }
bootBuildImage {
    imageName = createImageName("store-pod/<name>", project.version)   // cvhome-platform's drift check reads
    tags      = createImageTags("store-pod/<name>", project.version)   // these two lines; keep them here
    // everything else (registry, jlink, the native switch) is java-application-conventions
}
```

## Native images (GraalVM)

Every Spring service can ship as a GraalVM native executable instead of a JVM: it starts in a fraction of a second
and needs no code cache, metaspace or JIT, so it fits a 512 MB Fargate task where a JVM cannot start (the 1024 MB
floor in `cvhome-platform/flavours.yaml` is the JVM's). The plan, the measurements and every decision are in
`.agents/plans/graalvm-native-images.md`.

```bash
./gradlew bootBuildImage                           # JVM images, built as always (the default)
./gradlew bootBuildImage -Pnative                  # native images: same names, same tags, same run image
./gradlew bootBuildImage -Pnative -PnativeImageArgs='-J-Xmx12g'  # cap each compiler when several share a builder
./gradlew :store-core:pod-registry:pod-registry-service:nativeCompile   # a host executable (needs GraalVM 25)
```

- **`processAot` runs in every build.** Spring decides the bean graph ahead of time and a native image keeps only
  that. It is part of `bootJar`, so `./gradlew build` (and CI's build job) fails on an AOT error in the PR that
  caused it, not in a release. `lcl` and `bootRun` still run the JVM; nothing about local development changed.
- **No bean exists or not because of configuration** (*Toolchain, checkstyle and the test tasks* below): that
  rule is what lets one image serve Fargate, the load-testing stack (`lcl,test-stores`) and every flavour. A
  library's auto-configuration switched by an environment's property gets the same treatment at build time:
  `OpenTelemetryAotEnvironmentPostProcessor` builds the real OpenTelemetry SDK (the SDK honours
  `otel.sdk.disabled` itself at run time); without it every native service ran with telemetry frozen off.
- **Reflection, proxies and resources are hints, as code.** A native image contains only what the build could see
  being used. The shared registrars in `store-commons/autoconfigure/.../aot/` (listed in its
  `META-INF/spring/aot.factories`) cover the patterns this codebase has: the root YAML and `init-sql/**`, every
  `-external-api` client's two JDK proxies, the value objects, Stripe's Gson-bound models and params, Hibernate
  `UserType`s and id arrays, the outbox's scheduled work, handlers and events, the uaa SDK's DTOs, validators and the
  error contract, and every type in a `model` package — the JSON shapes, including the ones no controller names (a
  meta kept as a JSON column, a generic list's element); sso-core's covers what uaa and cua persist about an
  authentication. Each finds its targets by scanning, so a new client, param, type, handler or validator is covered
  by existing. A new kind of reflection gets a registrar beside the code that needs it, with a
  `RuntimeHintsPredicates` test — never a hand-kept `reflect-config.json`. A file the code reads itself is a
  resource hint next to that code (`content/aot/LayoutCopyRuntimeHints`); read it as a classpath resource, not through
  `java.util.ResourceBundle`, whose native support covers only the locales the image was built with.
- **Spring Data JDBC runs its runtime repositories natively** (`spring.aot.jdbc.repositories.enabled: false` in
  `common-config.yml`, read by `processAot`): 4.0.1's generated JDBC repositories map a DTO-returning `@Query`
  onto the entity and fail. JPA's generated repositories stay on. Its generated property accessors are off too
  (`spring.aot.data.accessors.enabled`).
- **Lazy to-one associations need enhanced entities — in the native image only.** A native image cannot generate
  Hibernate's proxy subclasses, so a native build (`-Pnative`, or a `native*` task such as `nativeCompile`) enhances
  every module that applies the Hibernate plugin, lazy loading only (`java-common-conventions`). A JVM build compiles
  plain entities: enhanced bytecode differs from one machine to the next, and CI's coverage job matches JaCoCo's data
  to recompiled classes by checksum. A new module with lazy to-one associations applies the plugin
  (`alias(libs.plugins.hibernate.orm)`, as catalog-core does) and nothing else. An enhanced class implements
  Hibernate's interfaces, so a module whose entities others compile against exports Hibernate as `compileOnlyApi`, as
  `store-pod/commons/store-commons` does.
- **A `@Bean` declares the class it returns** when that class carries `@Cacheable`, `@Transactional` or another
  proxy-driving annotation (catalog's `CachedExternalMerchantStoreService`, not `ExternalMerchantStoreService`):
  Spring builds AOP proxies ahead of time from the declared type, and a bean declared as an interface is simply not
  proxied in the native image — no error, just no cache.
- **No `@Bean` method with parameters is called directly** inside its configuration class: take the bean as a method
  parameter instead. An inter-bean call with arguments bypasses the generated bean and fails only in the native
  image.
- **Tests run on the JVM.** Only the application is native. `processTestAot` is off in
  `java-application-conventions`: with both plugins applied, `test` would first boot every Spring test context at
  build time, which costs minutes, and fails in a module whose tests are not Spring tests. `nativeTest` is not used.
- **Finding a missing hint.** It fails at run time, on the path that needs it, usually as `ClassNotFoundException`,
  `NoSuchMethodException` or "... is not registered for reflection / proxy". Reproduce on a native executable
  (`nativeCompile`, then run it with the service's `lcl` environment); for a stubborn one, the GraalVM tracing
  agent (`-agentlib:native-image-agent=config-output-dir=…` on the JVM) records what was touched — turn its finding
  into a registrar, not a committed JSON file.
- **Build cost.** On a 14-core laptop one service compiles in 52 s to 1m37s and the largest (payment) peaks at
  11.9 GB of compiler heap (at 7 GB it runs out); the twelve take 17 minutes three at a time. The buildpack uses Liberica NIK (GraalVM CE: Serial GC, no PGO) and
  `-march=compatibility`, so an x86-64 image runs on any Fargate host generation.

## Versioning

**The version is the git tag; no file carries it.** `gradle.properties` says `version=0.0.0-SNAPSHOT`
permanently and is never bumped. A release is a tag `vX.Y.Z` on `main`, cut by the orchestrator repo
(`cvhome-saas/orchestrator`, its `Release product` workflow) — never by hand and never from this repo.
Images are built by cvhome-platform's CodeBuild `2-images` project, which checks this repo out at that
tag and runs `bootBuildImage -Pversion=X.Y.Z` (a `-P` on the command line overrides `gradle.properties`),
pushing to the environment's ECR under the platform's project prefix. This repo has no publish workflow.

Image tags come from `createImageTags` in build-logic, identical in `docker-conventions` (npm apps) and
`java-application-conventions` (Spring services):

| `project.version` | image tags |
|---|---|
| `X.Y.Z` (CI, from the tag) | `X.Y.Z`, `X.Y`, `latest` |
| `*-SNAPSHOT` (every local build) | `latest` only |

So a local `./gradlew bootBuildImage` produces `<image>:0.0.0-SNAPSHOT` + `latest` and can never claim a
release number; `-Pversion=2.0.0` reproduces exactly what CI does. `project.version` also reaches the running
services as `service_version` (`@version@` in `common-config.yml`; how it is read: `load-testing/docs/monitoring/signals.md`).

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

**No bean exists or not because of configuration.** `verifyNoConfigurationSwitchedBeans` (in every module's `check`)
fails on `@Profile`, `@ConditionalOnProperty`, `@ConditionalOnBooleanProperty` or `@ConditionalOnExpression` in main
code. Anything that fixes the bean graph ahead of time — Spring AOT, a GraalVM native image — decides those once, at
build, for Fargate, every flavour and the load-testing stack alike. Read a switch when the code runs instead: an
initializer that returns early (the sso-core seeders, billing's catalog seeder), a client that knows no services when
off (the ECS discovery client), an indicator that says where it runs (the ECS task health). `@ConditionalOnClass`,
`@ConditionalOnMissingBean` and `@ConditionalOnWebApplication` are the same in every deployment and stay. Every module
also compiles with `-parameters`, so Spring Data binds a query method's parameters by name in the `-core` libraries
too.

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
docker compose -f docker-compose-lcl.yml up             # postgres, minio, spg — dev infra only
sudo ./extra/scripts/configure-domain.sh                # one-off: /etc/hosts entries for *.gateway.com
```

Run `configure-domain.sh` **before** the first local run — services address each other by hostname
(`merchant.gateway.com`, `spg-507f1f77.gateway.com`, `org1-store1.spg-507f1f77.gateway.com`), not `localhost`,
and nothing resolves without those entries. See `gateways-and-local-domains.md`.

Local infra in `docker-compose-lcl.yml`: `postgres:15-alpine`, `minio`, `saas-gateway` (spg image). No telemetry
backend runs locally (`otel.sdk.disabled: true`); the OTel starters wired into every service export to the
collector of the **load-testing** repo's compose stack (`make stack-up` there), which also carries Loki, Tempo,
Prometheus with the recording rules, and Grafana with the dashboards. This repo ships no monitoring configuration.
