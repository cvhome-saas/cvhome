# Testing architecture: unit / integration split, shared test libs, per-service coverage reports

## Context

Backend testing today is uneven and un-wired:

- Only 13 of ~60 modules have `src/test`; every `-core` (business logic) module has **zero** tests and no test
  classpath, because `java-library-conventions` applies no JUnit/jacoco at all.
- `java-application-conventions` splits tests by JUnit `@Tag`, but `unitTest`/`integrationTest` are not part of
  `check`, 6 test classes carry no tag and are silently skipped by them, and `jacocoTestReport { dependsOn test }`
  makes `unitTest` drag the whole suite in (the live `@TODO` in that file).
- Test infrastructure is copy-pasted: `TestcontainersConfiguration` ×10, `MinioS3Config` ×5, `Test*Application` ×10.
  The one shared helper (`ServletTestCustomSecurityConfig`, JWT signer) ships in **production** `autoconfigure`
  behind the `signer` profile. `ApiTestSupport` (the good HTTP/JWT helper) exists only in content-service.
- JaCoCo is applied to services only, no XML, no aggregate, nothing uploaded in CI, no verification task.
- Catalog + merchant services declare `implementation libs.spring.boot.starter.restclient.test` (test starter on
  the runtime classpath).

Decisions taken with the user: **separate source sets** (`test` = unit, `integrationTest` = Testcontainers);
naming `*Test` / `*IntegrationTest` with behaviour-sentence method names; **report + aggregate + soft gate**
coverage; **backend only** this round (frontend later).

## Target architecture

```
                 src/test/java              src/integrationTest/java
 -commons        value objects, mappers      —
 -core           services/facades (Mockito) —  (optionally @DataJpaTest slices if a core owns repositories)
 -service        controllers slice tests     @SpringBootTest + Testcontainers, real HTTP, JWT via test-support
 (ArchUnit)      one <Domain>ArchitectureTest per service in src/test — layering rules, no test deps in main
```

### Test types and where they live

| Type | Source set | Suffix | Deps available | Docker |
|---|---|---|---|---|
| Unit | `src/test` | `*Test` | JUnit 5, AssertJ, Mockito, Spring test (no context) | no |
| Architecture | `src/test` | `*ArchitectureTest` | ArchUnit | no |
| Integration | `src/integrationTest` | `*IntegrationTest` | + Boot test, Testcontainers, `test-support` | yes |

Gradle enforces the suffix per source set (`include '**/*Test.class'` vs `'**/*IntegrationTest.class'`); a
misnamed class simply does not run and `check` fails a sanity task (`verifyTestNaming`) that greps for
`@Test` classes not matching the pattern. `@Tag` is retired.

### Naming standard (goes in the skill)

- Class: `<ClassUnderTest>Test`, `<Feature>IntegrationTest`, `<Domain>ArchitectureTest`. Mirror the package of
  the class under test.
- Method: lowerCamel behaviour sentence — `failedRefreshKeepsLastKnownGood()`, `secondStoreCannotSeeFirstStoresRow()`
  — matches existing style and checkstyle `MethodName`; `@DisplayName` optional for longer prose.
- Use `@Nested` classes for one-method-many-cases (`class Publish { … }`).
- AssertJ only (no Hamcrest/JUnit `assertEquals`); `@ParameterizedTest` for tables.
- Every integration test of a store-scoped endpoint has at least one tenant-isolation case and one 403 case
  (mirrors `qa-testing.md` §8).

## Implementation

### 1. Version catalog — `gradle/libs.versions.toml`

Add versions/libraries: `archunit` (`com.tngtech.archunit:archunit-junit5`), `assertj-core`, `mockito-core` +
`mockito-junit-jupiter`, `junit-jupiter` (all versionless where the Boot BOM provides them; explicit for
ArchUnit), `jacoco = "0.8.14"` (or current). Add `[bundles]`: `test-unit` (junit, assertj, mockito, spring-test),
`test-integration` (spring-boot-starter-test, spring-boot-testcontainers, testcontainers-junit-jupiter,
testcontainers-postgresql, spring-security-test, junit-platform-launcher). Reconcile `testcontainers` 1.19.7 vs
`testcontainersjupiter` 2.0.3 to one 2.x line.

### 2. build-logic — new/changed convention plugins (`build-logic/src/main/groovy/`)

**`com.asrevo.java-common-conventions.gradle`** (base; now owns all test wiring so libraries get it too):
- apply `jacoco`; `jacoco { toolVersion = libs.versions.jacoco.get() }`.
- `testImplementation platform(spring-boot-dependencies:${libs.versions.springBoot})` — resolves the versionless
  aliases for library modules (lifts the hand-rolled block in `store-commons/autoconfigure/build.gradle`).
- `testImplementation libs.bundles.test.unit`, `testRuntimeOnly libs.junit.platform.launcher`.
- `tasks.named('test') { useJUnitPlatform(); include '**/*Test.class'; exclude '**/*IntegrationTest.class' }`.
- `jacocoTestReport { reports { xml.required = true; html.required = true } }`; `test finalizedBy jacocoTestReport`.
- `jacocoTestCoverageVerification` with `minimum = 0.0` placeholders and a per-project override hook
  (`ext.coverageMinimum`) so modules can ratchet; wired into `check`.
- Register `verifyTestNaming` (fails on `src/test` classes ending in `IntegrationTest` or `src/integrationTest`
  classes not ending in `IntegrationTest`).

**New `com.asrevo.java-integration-test-conventions.gradle`** (applied by application conventions; opt-in for
libraries):
- `sourceSets { integrationTest { compileClasspath += main.output + test.output; runtimeClasspath += … } }`,
  `integrationTestImplementation.extendsFrom testImplementation`.
- `integrationTestImplementation libs.bundles.test.integration` + `project(':store-commons:test-support')`.
- `tasks.register('integrationTest', Test) { testClassesDirs = sourceSets.integrationTest.output.classesDirs;
  useJUnitPlatform(); include '**/*IntegrationTest.class'; shouldRunAfter test; systemProperty
  'spring.profiles.active','signer,test-stores' }`.
- `jacocoIntegrationTestReport` (JacocoReport over the integrationTest exec) and a merged
  `jacocoMergedReport` — replaces the broken `jacocoTestReport { dependsOn test }` coupling.
- `check.dependsOn integrationTest` (CI runs it; `-x integrationTest` locally without Docker).
- `checkstyleIntegrationTest` gets the same report redirection as main/test.

**`com.asrevo.java-application-conventions.gradle`**: drop the tag-based `unitTest`/`integrationTest` tasks and
the `@TODO` block; apply the new integration-test conventions. Keep `createImageName/Tags`.

**`com.asrevo.java-library-conventions.gradle`**: unchanged shape (inherits test wiring from common).

**New `com.asrevo.jacoco-aggregate-conventions.gradle`** + **new root `build.gradle`** (the skill says "no root
build.gradle"; we introduce a minimal one that only applies this plugin — update the skill accordingly):
- uses Gradle's built-in `jacoco-report-aggregation` plugin, `dependencies { jacocoAggregation project(...) }` over
  every Java subproject; `testCodeCoverageReport` → `build/reports/jacoco/aggregate/` (xml + html).
- registers `perServiceCoverage` task that copies each `*-service`/`uaa`/`cua` merged report to
  `build/reports/coverage/<service>/` so "report per micro service" is a single artifact tree.

Register new plugin ids in `[plugins]` of the catalog (`integration-test-conventions`, `jacoco-aggregate-conventions`).

### 3. Shared test library — new module `store-commons/test-support`

`settings.gradle` include `:store-commons:test-support`; `java-library-conventions`; **`api`** on the integration
bundle so consumers only add the project. Contents (moved/generalised from content-service and autoconfigure):

```
store-commons/test-support/src/main/java/com/asrevo/cvhome/testsupport/
  containers/PostgresTestConfiguration.java   @TestConfiguration + @ServiceConnection PostgreSQLContainer (postgres:15-alpine)
  containers/MinioTestConfiguration.java      MinIOContainer + DynamicPropertyRegistrar for com.asrevo.cvhome.cdn.*  (from MinioS3Config)
  security/TestJwtSigner.java + TestSecurityConfiguration.java   moved out of autoconfigure's ServletTestCustomSecurityConfig
  security/Tokens.java                        token(role, store, org) helpers; constants for demo stores/orgs of test-stores
  http/ApiClient.java                         generalised ApiTestSupport (RestClient, scoped(), path(), expect(), json())
  time/MutableClock.java + TestClockConfiguration.java   from content-service
  annotations/@ServiceIntegrationTest         meta-annotation: @SpringBootTest(RANDOM_PORT) @ActiveProfiles({"signer","test-stores"})
                                              @Import({PostgresTestConfiguration, TestSecurityConfiguration}) @Tag-free
  annotations/@StorageIntegrationTest         = above + MinioTestConfiguration
  arch/CvhomeArchitectureRules.java           reusable ArchUnit rules: api→core→commons layering, no *-service class
                                              referenced from -core, no test classes in main, controllers take StoreMerchantId
```

Then: delete `ServletTestCustomSecurityConfig` from `autoconfigure` **main** (production jar loses the
signer endpoint — the `signer` profile stays as the activation switch, now only present on the integration
classpath); replace the 10 `TestcontainersConfiguration` / 5 `MinioS3Config` / 10 `Test*Application` copies
with `@ServiceIntegrationTest` + one `Test<Name>Application` per service using
`.with(PostgresTestConfiguration.class)`. Verify `run-lcl.sh` / any `.http` file does not rely on
`api/v1/test/sign` (grep first; if it does, keep a tiny `signer` controller in test-support and document it).

### 4. Migrate existing tests

- Move every `@Tag("integration-test")` class and every `*IntegrationTest`/`*ApplicationTests`
  (`contextLoads`) into `src/integrationTest/java`; rename `XApplicationTests` → `XContextIntegrationTest`.
- Strip `@Tag` everywhere; rename untagged unit tests to `*Test` if needed (`MerchantStoreApiTest` → AssertJ).
- content-service's `ContentApiIntegrationTest` becomes the reference and uses `ApiClient` from test-support.
- Fix `implementation libs.spring.boot.starter.restclient.test` → `testImplementation` in catalog/merchant.
- Remove the hand-rolled test block from `store-commons/autoconfigure/build.gradle` (now inherited).
- Add one `<Domain>ArchitectureTest` to each `-service` using `CvhomeArchitectureRules` (catalog first as template).
- Seed one real unit test in each `-core` module that currently has none (e.g. a mapper or a facade with Mockito)
  so the coverage report is non-empty per service — minimal, template-quality, not exhaustive.

### 5. CI — `.github/workflows/code-test-check.yml`

Split into two jobs: `unit` (`./gradlew test --continue`) and `integration` (`./gradlew integrationTest
--continue`, ubuntu has Docker), then `coverage` (`./gradlew testCodeCoverageReport perServiceCoverage`) uploading
`build/reports/coverage/**` and `build/test-results/**` as artifacts; add `mikepenz/action-junit-report` (or
`dorny/test-reporter`) for PR annotations. No external SaaS.

### 6. Skill update — `.claude/skills/project-structure/`

- New `references/testing.md`: the table above, the naming standard, when to write which type, the
  `test-support` catalogue with snippets (`@ServiceIntegrationTest`, `ApiClient`, `Tokens`), tenant-isolation +
  403 rule, ArchUnit rules list, coverage commands and where reports land, "Docker missing → run
  `./gradlew test` only", how to ratchet `coverageMinimum`.
- `references/build-system.md`: rewrite "Toolchain, checkstyle and the test tasks" (no `@Tag`; source sets;
  jacoco tasks; root `build.gradle` now exists and only aggregates coverage); plugin table gets the two new plugins.
- `references/qa-testing.md` §9 + `SKILL.md` QA section and "Where to look": point at `references/testing.md`;
  add rows "Write a unit test" / "Write an integration test" / "See coverage for a service".
- `references/shared-libraries.md`: add `test-support`. `AGENTS.md` Build & test block and checklist: `./gradlew
  test` (fast) and `./gradlew integrationTest` (Docker), coverage path. Bump skill `metadata.version` to 3.3.

## Order of work

1. catalog + test-support + build-logic (steps 1–3) — build compiles with no tests moved yet.
2. Migrate content-service (reference), then the rest (step 4).
3. Root aggregate + CI (2 aggregate plugin, 5).
4. Skill/docs (6).

## Verification

- `./gradlew test` runs without Docker (stop Docker or `DOCKER_HOST=` to prove no container starts) and produces
  `build/reports/jacoco/test/jacocoTestReport.xml` in every Java module incl. `-core`.
- `./gradlew integrationTest` with Docker: content/catalog/tenancy/pod-registry suites green;
  `checkstyleIntegrationTest` clean (`maxWarnings=0`).
- `./gradlew check` = test + integrationTest + verifyTestNaming + jacocoTestCoverageVerification + checkstyle.
- `./gradlew testCodeCoverageReport perServiceCoverage` → `build/reports/coverage/<service>/index.html` for all 12
  services; open catalog's.
- `jar tf store-commons/autoconfigure/build/libs/*.jar | grep -c ServletTestCustomSecurityConfig` → 0.
- Deliberately misname a class `FooIT` in `src/integrationTest` → `verifyTestNaming` fails.
- `./extra/scripts/run-lcl.sh --list` still lists everything; a quick login via gateway still works (signer
  removal must not affect `lcl,test-stores` runtime).
- CI: PR shows unit + integration jobs and the coverage artifact.

---

## Implementation notes (as built)

Deviations from the plan above, all verified green:

- **`@DatabaseIntegrationTest` added** as a third meta-annotation. uaa and cua are authorization servers that own
  their own `@Primary JwtDecoder`; importing the test decoder made their contexts ambiguous
  (`NoUniqueBeanDefinitionException`), so those two get Postgres + `test-stores` without a security override.
- **`coverageReport` replaces `testCodeCoverageReport`.** Gradle's `jacoco-report-aggregation` resolves each
  module's runtime classpath from the root, where the per-service `dependencyManagement` BOMs (spring-cloud,
  opentelemetry) are not visible, so versionless aliases failed to resolve. The root plugin now builds a
  `JacocoReport` directly from the modules' `.exec` files and source/class dirs — no cross-project resolution.
- **ArchUnit rules are per-domain**, not global: `-core` calling *another* pod's `-external-api` client is the
  service-to-service pattern here and must not be flagged. `..api.errors..` is exempt from
  "services must not depend on api" — a typed exception is part of the domain's error contract.
- **Declared deviations** rather than deleted rules: tenancy (`manager.controller`, `controller`), checkout and
  payment (`controller.v1.auth`) keep controllers outside `..api..`, passed explicitly to `controllersLiveIn(...)`.
- **One real piece of debt found and recorded, not fixed:** checkout's v2 statistic APIs
  (`CustomerStatisticApi`, `OrderStatisticApi`, `ProductStatisticApi`) inject repositories directly — 9
  dependencies. `CheckoutArchitectureTest` documents what must move into checkout-core before its
  `API_GOES_THROUGH_SERVICES` rule can be switched on.
- **`SkuInventoryMapperTest` moved** from inventory-service to inventory-core, the module that owns the class.
- The reactive signer (`ReactiveTestCustomSecurityConfig`) was deleted alongside the servlet one, and tenancy's
  `permitAll` matcher for `/api/v1/test/sign` removed with it — that endpoint no longer exists in any jar.

Result: 198 unit tests and the full integration suite green, `./gradlew check` clean, per-service coverage
34.1% (content-service 75.7%, inventory-service 0.0%), monorepo aggregate 34.6% over 14,253 lines.
