# Automated testing — types, layout, naming, coverage

Three kinds of automated test, three different costs. Pick by what the test needs, not by what it covers.

| Type | Source set | Class suffix | Needs | Runs in |
|---|---|---|---|---|
| **Unit** | `src/test/java` | `*Test` | nothing — no Spring context, no Docker | `./gradlew test` |
| **Architecture** | `src/test/java` | `*ArchitectureTest` | ArchUnit only | `./gradlew test` |
| **Integration** | `src/integrationTest/java` | `*IntegrationTest` | full Spring context + Testcontainers | `./gradlew integrationTest` |

**The source set is the contract, not a `@Tag`.** `test` includes `**/*Test.class` and excludes `**/*IntegrationTest.class`;
`integrationTest` includes only `**/*IntegrationTest.class`. A class in the wrong place simply never runs, so
`verifyTestNaming` (wired into `check`) fails the build on a misplaced or misnamed test. There is no `@Tag` split any
more — do not add one.

```bash
./gradlew test                      # every unit + architecture test. No Docker needed.
./gradlew integrationTest           # every integration test. Docker MUST be running.
./gradlew check                     # test + integrationTest + checkstyle + naming + coverage gate
./gradlew check -x integrationTest  # the laptop-without-Docker build
./gradlew :store-pod:catalog:catalog-service:test --tests '*PagesTest*'
```

A container failing to start is an environment problem, not a test failure.

## Where a test goes

Test the code in **the module that owns it**. Business logic lives in `-core`, so its unit tests live in
`-core/src/test` — not in the `-service` that happens to wire it up. `SkuInventoryMapperTest` lives in
`inventory-core` for exactly this reason. Every Java module (`-commons`, `-core`, `-external-api`, `-service`) now
gets JUnit, AssertJ, Mockito, ArchUnit and a coverage report from `java-common-conventions`, so there is nothing to
set up.

Only `-service` modules (and `uaa` / `cua`) get `src/integrationTest`, from
`java-integration-test-conventions`.

## Naming standard

**Classes** mirror the package of the thing under test:

| | |
|---|---|
| `ProductServiceTest` | unit test of `ProductService` |
| `ProductApiIntegrationTest` | integration test of the product API |
| `<Service>ContextIntegrationTest` | the context-loads / schema test of a service |
| `CatalogArchitectureTest` | the domain's ArchUnit rules |

**Methods are behaviour sentences in lowerCamelCase** — the assertion, not the mechanics:

```java
failedRefreshKeepsLastKnownGood()
secondStoreCannotSeeFirstStoresRow()
currenciesAreNeverSummedTogether()
nullStaysNullInBothDirections()
```

Not `testGetProduct()`, not `given_when_then` (checkstyle `MethodName` rejects underscores). Add `@DisplayName` only
when the sentence needs more prose than a method name holds. Group many cases of one method in a `@Nested` class.

**Assertions: AssertJ** (`assertThat`, `assertThatThrownBy`). Not Hamcrest, not JUnit's `assertEquals`. Tables of
cases go through `@ParameterizedTest`.

**Say why in a class javadoc** when the test guards a specific bug or a rule that looks arbitrary — the existing
tests do this and it is the difference between a test that survives a refactor and one that gets "fixed".

## Writing an integration test

Everything shared lives in **`store-commons/test-support`** (`com.asrevo.cvhome.testsupport`), already on the
`integrationTest` classpath of every service. Never copy a container or a JWT signer into a service again.

```java
@StorageIntegrationTest              // context + Postgres + MinIO + test JwtDecoder, profile test-stores
class ProductApiIntegrationTest {

    @LocalServerPort private int port;
    @Autowired private TestJwtSigner signer;

    private ApiClient api;
    private Tokens tokens;

    @BeforeEach
    void setUp() {
        api = new ApiClient(port);
        tokens = new Tokens(signer);
    }

    @Test
    void secondStoreCannotSeeFirstStoresProduct() {
        String other = tokens.staff(Tokens.ROLE_STORE_ADMIN, STORE_B);
        var response = api.get(ApiClient.scoped(ApiClient.path(PRODUCTS, id), STORE_B), other);
        ApiClient.expect(response, HttpStatus.NOT_FOUND);
    }
}
```

| What you need | Use |
|---|---|
| context + Postgres + test JWT decoder | `@ServiceIntegrationTest` |
| the above + MinIO (media) | `@StorageIntegrationTest` |
| context + Postgres, service owns its own decoder (**uaa, cua only**) | `@DatabaseIntegrationTest` |
| the gateway (WebFlux) | `@SpringBootTest` + `@Import(ReactiveTestSecurityConfiguration.class)` |
| a token for a role on a store | `Tokens.staff(role, store)` / `.custom(claims)` for the negative cases |
| real HTTP that does not throw on 4xx | `ApiClient` — `get` / `send` / `upload`, `scoped`, `path`, `query`, `json`, `expect`, `slug` |
| to travel in time | `@Import(TestClockConfiguration.class)` + `MutableClock` |
| a container by hand | `PostgresTestConfiguration`, `MinioTestConfiguration` |

Seed data comes from the **`test-stores`** profile (already active): the same `init-sql` the local stack uses. There
is no `src/test/resources` in this repo — do not start one for fixtures that belong in `init-sql`.

**Two cases every store-scoped integration test owes**, mirroring the QA checklist in `qa-testing.md` §8:

1. **Tenant isolation** — repeat the action as a principal on a second store and prove it cannot see or mutate the
   first store's row.
2. **The permission gate** — a principal without the token gets 403 (or 404), never a 200 with empty data.

## Architecture tests

`CvhomeArchitectureRules` holds the layering as executable rules, bound to one domain:

```java
@AnalyzeClasses(packages = "com.asrevo.cvhome.catalog", importOptions = ImportOption.DoNotIncludeTests.class)
class CatalogArchitectureTest {
    static final String DOMAIN = "com.asrevo.cvhome.catalog";
    @ArchTest static final ArchRule API_GOES_THROUGH_SERVICES = CvhomeArchitectureRules.apiDoesNotTouchRepositories(DOMAIN);
    ...
}
```

`CatalogArchitectureTest` is the template — copy it and change `DOMAIN`. The rules: api does not touch repositories ·
services do not depend on their own api (`..api.errors..` exempt, since a typed exception is part of the error
contract) · entities depend on neither · `@RestController` lives in a declared api package · no production class
depends on `testsupport`.

Rules are **scoped to one domain on purpose**: calling *another* pod's `-external-api` client from `-core` is how
services talk here and must not be flagged.

Where a service genuinely deviates, **declare the deviation in its own test** rather than deleting the rule — pass the
legacy package to `controllersLiveIn(DOMAIN, "…controller..")`, so the debt is visible and no *new* deviation can
appear. One live one: tenancy/payment keep controllers outside `..api..`. Checkout cleared both of its deviations in
the 2026-09 rewrite and binds all five rules.

## Coverage — reported and gated per domain

JaCoCo is applied to **every** Java module by `java-common-conventions`; the root `jacoco-aggregate-conventions`
plugin groups modules into **domains** (a service's `-commons/-core/-events/-external-api/-service` union, derived
from the project path; `store-commons/*` and `store-pod/commons/*` are `shared`; override with
`ext.coverageDomain = 'checkout'` in a module) and produces three reports per domain:

```bash
./gradlew domainCoverage printDomainCoverage   # table + build/reports/coverage/<domain>/{unit,integration}/ and /coverage.xml + /html (merged)
./gradlew jacocoCatalogReport                  # one domain, merged; jacocoCatalogUnitReport / jacocoCatalogIntegrationReport per kind
./gradlew coverageReport                       # build/reports/jacoco/aggregate/html  ← whole monorepo
./gradlew domainCoverage -PcoverageFromArtifacts   # re-render from existing .exec files without re-running tests
```

Per module `jacocoTestReport` / `jacocoIntegrationTestReport` still exist for a quick local look. CI uploads
`coverage-per-domain` and `coverage-aggregate` on every PR and prints the domain table in the step summary.

**Three hard gates per domain, ratcheted.** `domainCoverageMinimum` in the root `build.gradle` holds a
`[unit:, integration:, merged:]` LINE-ratio floor per domain; `check` (and CI's coverage job) runs
`domainCoverageVerification` = `jacoco<Domain>UnitVerification` + `…IntegrationVerification` + `…Verification`
(merged). Integration gates never pass vacuously: with a non-zero floor and `-x integrationTest` (or missing
`integrationTest.exec` in CI) they fail with "integrationTest did not run". `domainUnitCoverageVerification` is the
Docker-free subset.

Ratchet: after a test PR run `./gradlew printDomainCoverage`, bump the domain's three floors to what it achieves
(rounded down to 0.01); never lower a floor to make a build pass — fix the tests. Target is `merged: 0.85` for every
domain. `uaa` and `cua` stay at `0.0` until their legacy code is refactored.

Excluded from every number: `*Application`, classes in a `config` package named `*Config`, `*Configuration`,
`*Configurer`, `*Properties`, `JobScheduling`, `*AutoConfiguration`, MapStruct `*MapperImpl`/`*MappersImpl`, and
Lombok-generated code (`lombok.config`). Hence: **every `@Configuration` class lives in a `config` package**; a
domain class like `PaymentConfiguration` (an entity) stays in scope on purpose.

## Rules of thumb

- A bug fix ships with the test that fails without it. Name the method after the bug's behaviour.
- Prefer a unit test in `-core`. Reach for an integration test when the thing under test *is* the wiring: HTTP
  contract, security, SQL, schema, transactions.
- Never assert on a log line or a sleep. Move `MutableClock` instead of `Thread.sleep`.
- Do not mock what you own end to end in an integration test; do not boot Spring for logic a constructor can exercise.
- Automated tests gate the merge; they do not replace the browser/`.http` QA pass in `qa-testing.md`.
