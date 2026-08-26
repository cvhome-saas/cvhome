# Coverage to ≥85% per domain (follow-up to `to-support-advanced-types-spicy-nautilus.md`)

## Context

The previous round built the test architecture (unit/integrationTest source sets, `test-support`, per-service jacoco
reports, soft gate at 0.0). Coverage is now measurable but low: per-service 0–76%, aggregate 34.6%. Two structural
problems block a meaningful target:

- `perServiceCoverage` only reports the `-service` module; the logic in `-core`/`-commons` (content-core 5.5k lines,
  checkout-core 3.9k, catalog-core 2.9k) is not in any per-service number — and content's ITs already execute it.
- No exclusions: `*Application`, `@Configuration` wiring and MapStruct impls count as missed lines.

Decisions (user): **85% LINE coverage per domain** = union of that domain's `-commons/-core/-events/-external-api/
-service` modules, unit + integration merged; exclusions = `*Application`, config classes, MapStruct impls (Lombok is
already filtered — root `lombok.config` has `addLombokGeneratedAnnotation=true`); **ratchet** floors per domain, hard
fail in `check`/CI, ending at 0.85 for all 12 domains.

Domains: inventory, pod-registry, merchant, gateway, payment, content, tenancy, catalog, checkout, uaa, cua, billing.
**Deferred (user): uaa, cua, checkout** still carry legacy code slated for refactor — they get reports but stay at
0.0 floors (not gated) until the refactor lands; Wave C is postponed except billing.
`store-commons/*` and `store-pod/commons/*` report as a `shared` pseudo-domain, gated at current value only.

## Part A — Plumbing (one PR)

### A1. Per-domain merged report + verification — `build-logic/src/main/groovy/com.asrevo.jacoco-aggregate-conventions.gradle`

Keep `coverageReport`; replace `perServiceCoverage` with domain tasks derived from project path
(`:store-pod:<domain>:*`, `:store-core:<domain>:*`; single-module domains uaa/cua/gateway resolve to themselves;
`:store-commons:*` and `:store-pod:commons:*` → `shared`; a module may override with `ext.coverageDomain = 'x'`).

```groovy
def coverageExcludes = [
  '**/*Application.class', '**/*Application$*.class',
  '**/config/*Config.class', '**/config/*Config$*.class',
  '**/config/*Configuration.class', '**/config/*Configuration$*.class',
  '**/config/*Configurer.class', '**/config/*Properties.class', '**/config/JobScheduling.class',
  '**/*AutoConfiguration.class',
  '**/*MapperImpl.class', '**/*MapperImpl$*.class', '**/*MappersImpl.class',
]
```
Patterns are scoped to `**/config/*` on purpose: bare `*Configuration`/`*Config` would drop domain classes
(`payment-core/.../entity/payment/PaymentConfiguration`, `cua/.../domain/SocialLoginConfig`). Hand-written
`*Mapper`/`*Populator` classes stay in scope — only tenancy's two MapStruct `*MappersImpl` are generated.

Per domain register **three** report/verification pairs, differing only in which exec files feed them
(`classDirectories` = each module's `sourceSets.main.output.classesDirs` filtered by the excludes, `sourceDirectories`
= each module's `allJava.srcDirs`; LINE COVEREDRATIO; `minimum` read lazily from
`rootProject.domainCoverageMinimum[domain][kind] ?: 0.0`):

| kind | exec files | report path | task names |
|---|---|---|---|
| `unit` | `build/jacoco/test.exec` of every module | `build/reports/coverage/<domain>/unit/` | `jacoco<Domain>UnitReport` / `jacoco<Domain>UnitVerification` |
| `integration` | `build/jacoco/integrationTest.exec` of every module | `build/reports/coverage/<domain>/integration/` | `jacoco<Domain>IntegrationReport` / `jacoco<Domain>IntegrationVerification` |
| `merged` | both | `build/reports/coverage/<domain>/{coverage.xml,html}` | `jacoco<Domain>Report` / `jacoco<Domain>Verification` |

Each gate is real on its own: the unit gate proves `-core` logic is tested without Docker; the integration gate
proves the HTTP/tenant/403 layer is exercised, not just context-loads. The integration verification must **fail,
not pass vacuously, when no `integrationTest.exec` exists** (JaCoCo reports 0% on missing exec data, so a floor
>0 fails naturally; additionally `onlyIf`-guard it to error with "integrationTest did not run for <domain>" when
the task graph excluded `integrationTest`, so `-x integrationTest` locally skips the gate explicitly rather than
silently). `dependsOn` the modules' `test` / `integrationTest` tasks respectively.

Umbrella tasks: `domainCoverage` (all reports), `domainUnitCoverageVerification`,
`domainIntegrationCoverageVerification`, `domainCoverageVerification` (= all three kinds; wired into root `check`),
`perServiceCoverage` kept as alias, and `printDomainCoverage` (parses the last `<counter type="LINE">` of each xml
and prints a domain × {unit, integration, merged} table — used for ratcheting and `$GITHUB_STEP_SUMMARY`).

### A2. Root `build.gradle`

```groovy
// Per-domain line-coverage floors, one per test kind. Bump after `./gradlew printDomainCoverage`; never lower.
// Targets: merged 0.85 everywhere; unit and integration each ratcheted to the value the domain PR achieves
// (guideline: unit ≥ 0.60 for domains with a -core, integration ≥ 0.50 for every -service).
ext.domainCoverageMinimum = [
  inventory: [unit: 0.0, integration: 0.0, merged: 0.0],
  'pod-registry': [unit: 0.0, integration: 0.0, merged: 0.0],
  // ... merchant, gateway, payment, content, tenancy, catalog, checkout, uaa, cua, billing, shared
]
```
Fill with the first `printDomainCoverage` run (rounded down to 0.01) in the same PR. Every domain gets a non-zero
integration floor from day one (all 12 services already have at least a context-load IT, so the number is >0),
which is what turns the integration gate on.

### A3. `com.asrevo.java-common-conventions.gradle`

Remove module-level `jacocoTestCoverageVerification` from `check` and the `ext.coverageMinimum` hook; the domain gate
is the only gate. Optional chore: delete the 47 per-module `lombok.config` copies (root already has `stopBubbling`).

### A4. `store-commons/test-support` additions

- `annotations/ReactiveIntegrationTest` — `@SpringBootTest(RANDOM_PORT)` + `ReactiveTestSecurityConfiguration`, no
  Postgres (gateway). Document `WebTestClient.bindToServer().baseUrl(...)` (webflux already on classpath).
- `annotations/RepositoryIntegrationTest` — `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` +
  `PostgresTestConfiguration` + `test-stores`; add `api libs.spring.boot.starter.data.jpa.test` to test-support.
- `security/Tokens` — add `superAdmin()`, `orgAdmin(org)`, `s2s(scope)`, constants for seeded org/store ids (check
  claim names against `store-commons/autoconfigure/.../s2s/services/PermissionAccessChecker.java`).
- `http/StoreApi` — promote content-service's `ApiTestSupport` (token+path+expect helpers) so no service copies it.
- Per-service `ExternalClientsTestConfiguration` pattern (in `src/integrationTest`) that `@MockitoBean`s the
  `-external-api` clients of other pods (tenancy, checkout, cua need it).
- `-core` modules whose main deps are `compileOnly` add `testImplementation` for spring-data/tx/validation
  (pattern: `store-pod/inventory/inventory-core/build.gradle`).

### A5. CI — `.github/workflows/code-test-check.yml`

`unit` and `integration` jobs upload `**/build/jacoco/*.exec` (`if: always()`); `coverage` job downloads both
(`merge-multiple: true`) and runs `./gradlew domainCoverage domainCoverageVerification coverageReport -x test
-x integrationTest -PcoverageFromArtifacts` (the property tells the integration gate that the exec files were
produced upstream so its "did integrationTest run" guard checks for the downloaded `integrationTest.exec` files
instead of the task graph), appends `printDomainCoverage` output to `$GITHUB_STEP_SUMMARY`, uploads
`build/reports/coverage/**`. `coverage` `needs: [unit, integration]` so a failed integration job blocks the gate
rather than letting the merged number pass on unit data alone. Fallback if class-id mismatch appears: single job
running `./gradlew check`.

### A6. Docs

Update `.claude/skills/project-structure/references/testing.md` and `build-system.md`: report path is now
`build/reports/coverage/<domain>/`, ratchet procedure, exclusion rules (every `@Configuration` must live in a
`config` package), the new annotations/Tokens helpers.

## Part B — Tests per domain (one PR per domain, parallelisable)

Yield order in this codebase: (1) `@ServiceIntegrationTest`/`@StorageIntegrationTest` over real HTTP, one class per
controller, every store-scoped endpoint gets a tenant-isolation + 403 case — covers controller→facade→repo→entity
across all modules; (2) Mockito unit tests in `-core/src/test` for branchy logic (state machines, mappers/populators,
gateways); (3) repository slices only for query classes ITs can't steer. Don't unit-test 12-dependency facades
(`OrderFacadeImpl`) — cover them via IT flows. Each PR ends by bumping all three floors (unit, integration,
merged) in `domainCoverageMinimum` to what it achieved; the last PR per domain sets merged 0.85.

| Wave | Domain (est. lines, start) | Main targets | Technique |
|---|---|---|---|
| A | inventory (~280, ~15%) | `ReservationServiceImpl`, `InventoryServiceImpl`, `InventoryApi`, `ExternalProductReservationApi/Service` | create `src/integrationTest` (missing today), 2 IT classes; unit for reservation branches; `MockRestServiceServer` for ext-api client |
| A | pod-registry (~400, ~45%) | `PodApi`, `PodPlacementApi`, `PodHealthProbe`, `PodSeedInitializer`, `ExternalPodPlacementService` | ITs for both APIs; unit `PodHealthProbe` (mock RestClient), seeder |
| A | merchant (~500, ~20%) | `StoreFacadeImpl`, `MerchantStoreApi`, `MerchantStore`, both populators, `MerchantRoutingService`, `RouterController` | `@StorageIntegrationTest`; unit populators + routing |
| A | gateway (~200, ~42%) | `PodClient`, `CapturingServerOAuth2AuthorizationRequestResolver`, `RedirectingServerAuthenticationSuccessHandler`, `StoreBillingGuardFilter`, `StoreBillingStatusClient`, `GatewayRouteLocatorImpl`, auth/logout controllers | `@ReactiveIntegrationTest` + `@MockitoBean ReactiveExternalPodService` pointing at an echo `@RestController` in integrationTest so `/spg/**` routing is proven; StepVerifier + `MockServerWebExchange` units |
| A | payment (~580, ~25%) | `StripeProcessor`, `TransactionServiceImpl`, `Transaction`, `PaymentGatewayService`, `PaymentConfigurationMapper/Service`, controllers | ITs on config controller + external gateway API; `StripeProcessor` via `mockStatic(PaymentIntent)` or inject `StripeClient` |
| B | content (~1500, ~55–65% once core counted) | `StorefrontFacade`, `MediaService`, `ContentItemService`, `MenuService`, `PolicyService`, `FaqService`, `BannerBinding`, `ContentMapper`, `PublishingService`, `ContentSpecifications` | run domain report first, fill gaps from HTML: unit for bindings/mapper, extra IT methods in the 2 existing IT classes |
| B | tenancy (~850, ~45%) | `InternalStoreServiceImpl`, `ManagedUserAccountServiceImpl`, `OrgManagerApi`, `StoreManagerApi`, `StoreManagerServiceImpl`, `UserAccountApi`, `OrgMemberApi`, `OrgOwnerBackfill`, `OrgLifecycleService`, outbox processors | IT per controller with `ExternalClientsTestConfiguration` (uaa, pod, billing, pod-registry clients mocked); unit `*Impl` + processors; needs `Tokens.superAdmin/orgAdmin` |
| B | catalog (~1000, ~15%) | `ProductServiceImpl`, `CategoryServiceImpl`, `ProductMapper`, `Product`, `CategoryApi`, `ProductGroupServiceImpl/Api`, `ProductImageServiceImpl`, `ManufacturerApi` | `@StorageIntegrationTest` per API (images → MinIO); unit `ProductMapper` |
| C | checkout (~1200, ~10%) | `OrderFacadeImpl`, `ShoppingCartFacadeImpl`, `OrderApi`, `ReadableShoppingCartMapper`, `OrderPlacementFacadeImpl`, `OrderServiceImpl`, `ShoppingCartServiceImpl`, `OrderRepository`, `ReadableOrderPopulator`, v2 statistic APIs | IT flows cart → order → customer order with inventory/payment/catalog clients mocked; mappers/populators (~650 lines) as pure units with fixture builders (`java-test-fixtures` on checkout-core) |
| C | uaa (~500, 22%) | `AdminService`, `ClientClientDetailsMapper`, `AdminUserController`, `AdminClientService`, `OAuth2ClientDatabaseInitializer`, `KeyPairService`, `AdminClientController`, `JwtCustomizerConfig`, `AdminRoleController` | `@DatabaseIntegrationTest`: real `POST /oauth2/token` with seeded `admin-sdk` client_credentials (`init-sql/data-common.sql`), then drive `/api/v1/admin/**`; form login + auth-code chain with seeded `org1-admin`; unit mapper + `AdminService` |
| C | cua (~480, 18%) | `SecurityUser`, `SocialProvider`, `RegistrationController`, `KeyPairService`, `DynamicRegisteredClientRepository`, `SocialLoginConfigMapper`, `DynamicClientRegistrationRepository`, `CustomOAuth2UserService`, `PathPrefixFilter`, `LoginController`, `JpaUserDetailsService`, `UserService` | `@DatabaseIntegrationTest`: `/register`, `/login`, `/oauth2/token`, `/userinfo`, `/api/v1/auth/me`; social-login-config API with `@MockitoBean(name="jwtDecoderByIssuerUri")` + mocked `CachedExternalMerchantStoreService`; units for the rest |
| C | billing (~1700, 23%) | `StoreSubscriptionEntity` (state machine), `SubscriptionServiceImpl`, `WebhookApplyServiceImpl`, `WebhookIngestService`, `StripeSubscriptionGateway`, `SubscriptionApi`, `PlanCatalogSeeder`, `PlatformBillingServiceImpl`, `BillingStatisticApi`, `PlanCatalogServiceImpl`, `StoreQuotaServiceImpl` | unit first (entity, webhook apply/ingest with Stripe event JSON fixtures in `src/test/resources/stripe/`, service with mocked gateways); ITs on all APIs with `Stripe*Gateway` beans mocked; **refactor `StripeGatewaySupport` to an injected `com.stripe.StripeClient`** (~900 static-call lines are otherwise unreachable; fallback `mockStatic`) |

Optional: assign `store-pod/commons/customer-*` and `reference-*` (packages are `…checkout…`) to checkout via
`ext.coverageDomain = 'checkout'` — decide when checkout PR starts.

## Order of work

1. Part A PR → baselines recorded, gate green at current floors, CI shows per-domain table.
2. Wave A (5 small domains, one agent/PR each) → Wave B → Wave C (billing needs the Stripe refactor decision first).
3. Final PR: all floors = 0.85, docs bumped.

## Verification

```
./gradlew domainCoverage printDomainCoverage            # table + build/reports/coverage/<domain>/{coverage.xml,html/index.html}
./gradlew tasks --group verification | grep -E 'jacoco(Content|Uaa|PodRegistry)(Report|Verification)'
grep -c 'MappersImpl\|S3InitConfigurer\|JobScheduling\|ContentApplication' build/reports/coverage/*/coverage.xml   # 0 → excluded
grep -c 'PaymentConfiguration"' build/reports/coverage/payment/coverage.xml                                       # >0 → NOT excluded
# set content.merged: 0.99 temporarily → ./gradlew jacocoContentVerification fails with "lines covered ratio is 0.xx, but expected minimum is 0.99"
# set content.integration: 0.99 → jacocoContentIntegrationVerification fails; unit gate unaffected (proves the kinds are independent)
./gradlew jacocoContentIntegrationVerification -x integrationTest   # must FAIL with "integrationTest did not run for content", not pass
./gradlew domainUnitCoverageVerification -x integrationTest         # unit gate alone is runnable without Docker
./gradlew check                                         # test + integrationTest + verifyTestNaming + checkstyle + all three domain gates
./gradlew test -x integrationTest && ./gradlew domainCoverage -x integrationTest   # no-Docker laptop still gets unit + (empty) integration reports
```
Per domain PR: `./gradlew :<path>:test :<path>:integrationTest jacoco<Domain>Report` → open the HTML, confirm
unit, integration and merged each ≥ their new floor; CI coverage job green; `printDomainCoverage` in the PR summary
shows all three columns bumped. Done when every domain's merged row reads ≥85.0%, `merged` floors are 0.85 across
the board, and no domain has a 0.0 unit or integration floor.

---

# Wave S — store-commons coverage (the `shared` domain)

## Context

Waves A–C gated nine service domains at ≥85% merged. `shared` was never targeted: the plan gated it "at current
value" (0.03) as a no-regression floor. It is now the only domain carrying real, widely-depended-on library code at
effectively zero coverage — 3,832 executable lines, 139 covered (**3.6 %**). Everything in it is a dependency of
every service: the secret-crypto providers that hold merchant payment credentials, the s2s security layer that every
controller authorizes through, the shared error/ProblemDetail translation, the uaa admin SDK, and the ECS/Fargate
discovery used in production deploys. A bug here is a bug in all thirteen services at once, and Wave A already
produced one such incident (payment's seeded secrets only decrypted on a machine that happened to hold the key).

Goal: **`shared` = the `store-commons/*` libraries, gated at 85 % merged**, reached with unit tests only.

Decisions (user): split `store-pod/commons/*` into its own domain; target 85 %; exempt `test-support`.
Integration floor stays **0.0** — no module here applies `java-integration-test-conventions`, and the user has
explicitly accepted a unit-only approach for this domain.

### Measured starting point (JaCoCo executable lines, Lombok already filtered)

`shared` today is two unrelated things: `store-commons/*` (2,335 lines) and `store-pod/commons/*` (1,497 lines —
customer/reference/CMS code whose packages are literally `com.asrevo.cvhome.checkout`). After the S0 split and
exemptions, `shared` is **2,027 lines, 130 covered (6.4 %)**; 85 % means covering **1,593 more lines**.

| module | lines | now |
|---|---|---|
| `autoconfigure` | 765 | 15.8 % |
| `uaa-client-impl` | 321 | 0 % |
| `errors` | 319 | 0 % |
| `commons` | 190 | 4.7 % |
| `ecs-service-discoveryclient` | 142 | 0 % |
| `uaa-client` | 111 | 0 % |
| `secret-crypto-local` | 88 | 0 % |
| `secret-crypto-core` | 36 | 0 % |
| `fargate-task-info` | 24 | 0 % |
| `secret-crypto-aws` | 22 | 0 % |
| `secret-crypto-caffeine` | 9 | 0 % |

**One correction to note:** widening the jacoco excludes is *not* a shortcut. Measured, dropping `test-support`,
the nested `config/**` wiring and every `*Properties` removes 314 of 3,838 lines and moves the number from 3.6 % to
**3.7 %**. It is worth doing for correctness, not for the metric. Verified separately: **no other domain loses a
single line** to that change, so the twelve floors already green stay green.

## S0 — plumbing (one PR, no tests)

All in `build-logic/src/main/groovy/com.asrevo.jacoco-aggregate-conventions.gradle` + root `build.gradle`.

1. **Split the domain.** In `domainOf`, map `:store-pod:commons:*` to `pod-commons` instead of folding it into
   `shared` (currently `segs[1] == 'commons' → 'shared'`, line ~48). `shared` becomes `store-commons/*` only.
   Add `pod-commons: [unit: 0.0, integration: 0.0, merged: 0.0]` to `domainCoverageMinimum` — it reports at its real
   0 % and is gated against regression, visibly, in the same table as everything else. It is not hidden and not
   claimed as covered; it is queued behind the checkout refactor the user already deferred.
2. **Exempt `test-support`.** Honour `ext.coverageExempt = true` in `domainOf`/`javaProjects` and set it in
   `store-commons/test-support/build.gradle`. It is test infrastructure: it executes thousands of times per CI run
   inside *other* domains' integration tests, and none of that ever credits `shared`.
3. **Fix the exclude globs** — `*` does not cross `/`, so `**/config/*Config.class` silently misses everything under
   `s2s/config/internal/`. Change the `config` patterns to `**/config/**/*Config.class` (and `*Configuration`,
   `*Configurer`), and add `**/*Properties.class` / `**/*Properties$*.class`. Keep the name-based filter rather than
   excluding `**/config/**` wholesale: `CustomPermissionEvaluator` (140 lines of real permission logic, already 27
   covered) lives in `s2s/config/internal/` and must stay counted.
4. Re-run `./gradlew domainCoverage printDomainCoverage` and set `shared` floors to the new measured baseline.

## S1 — secret-crypto + ecs (321 lines)

Pure logic, constructor-injected collaborators, **zero new dependencies** except two `testImplementation` mirrors of
`compileOnly` deps (`fargate-task-info` → jackson-databind; `errors` → jackson-annotations in S2).

- `EncryptedValueTest` — `isEncrypted` null/unprefixed; `deserialize` null → null, empty → null, no `ENC:` prefix →
  IAE, wrong part count → IAE; serialize ↔ deserialize round trip.
- `SecretCryptoProviderRegistryTest` — routes decrypt by `providerId() == algorithm`, no match → ISE, encrypt always
  via the active provider.
- `LocalAesCryptoProviderTest` — empty key provider → IAE at construction; AES-GCM round trip; two encrypts of one
  plaintext differ (random IV); tampered ciphertext → "Decryption failed"; key vanishing mid-life → IAE.
- `FileSystemKeyProviderTest` (`@TempDir`) — `expandSystemProperties` passthrough / `${user.home}` / unknown
  property → IAE; `getKey` missing file, directory, raw bytes, base64, invalid base64 → empty, IOException → empty.
- `RandomKeyProviderTest`, `StaticKeyProviderTest`, `CustomCallbackKeyProviderTest` — small, cheap.
  `EnvironmentVariableKeyProvider` needs `mockStatic(System.class)`; if that proves brittle on JDK 25, leave it and
  say so rather than contorting the class.
- `AwsKmsCryptoProviderTest` — mocked `KmsClient`, `ArgumentCaptor` on the requests; assert decrypt keys off
  `encryptedValue.getKeyId()`, not the constructor keyId.
- `CachingSecretCryptoProviderTest` — equal envelopes hit the cache once (this only works because `@Data` gives
  `EncryptedValue` array-aware equals — worth pinning); different ciphertext misses; `encrypt` is never cached.
- `CloudMapServiceInstanceTest`, `EcsDiscoveryClientTest`, `EcsReactiveDiscoveryClientTest` — the meaty methods are
  already `public static` taking the client, and `HttpInstanceSummary` is a real builder POJO. Cover the
  serviceId/namespace splitting (no dot, leading dot, trailing dot), per-service port override vs default, the
  `NAMESPACE_ID` filter, and `includeServices` appending. Use `.collectList().block()` rather than adding
  `reactor-test`.
- `DefaultServiceInstanceTest` — the 178-line vendored Spring Cloud copy: hand-written `setUri` (derives host/port,
  flips `secure` on https), `equals`/`hashCode`. Largest single line win in ecs-commons.

**Production defect to fix, with a failing-first test** (verified by reading both files): `AwsKmsCryptoProvider.encrypt`
never sets `iv`, and `EncryptedValue.serialize()` calls `Base64.getEncoder().encodeToString(getIv())` — so **every
KMS-encrypted value NPEs on serialize**, and even if it didn't it would emit 4 colon-separated parts while
`deserialize` demands exactly 5. The AWS provider cannot round-trip the envelope format at all. Fix in the envelope
(tolerate a null/empty IV on both sides) rather than inventing an IV KMS does not use.

`EcsTaskFetcher` (24 lines) is the one genuinely untestable class: it captures
`System.getenv("ECS_CONTAINER_METADATA_URI_V4")` and its `HttpClient` in `private static final` fields at
class-init, with no URI/client overload. Cover only the failure fallbacks; do not reflect on static finals. Note
while there: `getObjectMapper()` calls `mapper.isEnabled(FAIL_ON_UNKNOWN_PROPERTIES)` and **discards the result** —
a no-op that was meant to be a `disable`. Raise it; don't silently change behaviour under cover of a test PR.

## S2 — errors + uaa-client + uaa-client-impl (751 lines)

- `errors` has **no dependencies at all** (one `compileOnly jackson-annotations`) — the best ratio in the domain.
  `RemoteFailuresTest` (199 lines: `contextOf` param precedence, non-Map/non-List node handling, `codeFor` 504/408 →
  `REMOTE_TIMEOUT`, `isTimeout` walking the cause chain), `UncheckedTest`, `ErrorBuilderTest`,
  `RemoteErrorCatalogTest`, `ErrorCategoryTest`, plus a parameterized sweep over `CommonErrors` pinning the wire
  contract.
- `uaa-client`: `UaaApiErrorsTest` — copy `store-core/pod-registry/pod-registry-external-api/src/test/.../PodRegistryApiErrorsTest.java`
  verbatim in shape; plus `UaaApiUnavailableException.wrapping` branches.
- `uaa-client-impl`: `OAuth2TokenManagerTest` — the class already ships a 4-arg constructor taking the `HttpClient`
  "so a test can drive both without a uaa to talk to". Mock it. Branches: cached token reused, expiry refresh,
  client_secret_post → non-200 → **Basic-auth fallback**, fallback also failing → `UaaApiUnavailableException`,
  `IOException`, `InterruptedException` re-interrupting. Assert the failure carries **no body** — the code
  deliberately drops it because it can echo client credentials.
  `AbstractAdminClientTest` (mapped vs unmapped error fork, `readProblem` null/blank/non-JSON), `AdminUserClientTest`
  (URL/query building), `UserAccountServiceImplTest` (mock `AdminUserClient`; 12 methods each with a typed/undecided
  catch fork, `extractMetadata` null org/store, `getAssignableRoles` filtering).

## S3 — autoconfigure + commons (955 lines)

Needs `testImplementation` additions, since every Spring dep here is `compileOnly`: `spring-webmvc`, `spring-web`,
`spring-webflux`, `jakarta.servlet-api`, `jackson-databind`, `spring-tx`. `spring-security-test` is **not** on the
unit classpath (it ships in the integration bundle) — build `JwtAuthenticationToken` by hand, exactly as the two
existing tests in this module already do.

Template to extend: `store-commons/autoconfigure/src/test/java/com/asrevo/cvhome/s2s/config/internal/BillingSuperAdminAccessTest.java`.

- Security layer (the highest-value target in the domain): `SecurityUtilsTest` — `getOrgStoreIdentity`'s four-way
  ladder, including the trap this session already fixed once (**scope is tested before role**, so an org admin on
  `store_core` silently resolves to platform-wide with a null org). `StoreRoleAccessCheckerTest`,
  `PermissionAccessCheckerTest` (267 lines, ~20 methods), `CustomPermissionEvaluatorTest` (extend the existing two).
- Error translation: `ProblemDetailFactoryTest` (MDC trace id vs UUID fallback, `statusOf` unresolvable → 500,
  `typeUri` derivation), `GlobalErrorHandlerTest` (`codeFor` switch, 3-way `renderBody`, remote 4xx passthrough vs
  502, debug-detail branch), plus the three thin advices.
- JWT: `MultiIssuerJwtDecoderTest` (+ reactive twin), `UaaJwtGrantedAuthoritiesConverterTest` (claim as String /
  blank / Collection / other; already-prefixed vs uppercased authorities), `UrlNormalizeTest` (default-port
  stripping, `JwtException` paths).
- Small pure utilities: `RedirectionUrlBuilder`, `ServiceUrlBuilder`, `LanguageUtils`, `ObjectIdDeserializer`,
  `RequestCacheAwareLocaleInterceptor`, `S2sErrorHandler.declaredOrCarrier`, `RemoteProblemTranslator`.
- `commons`: `LanguageCodeTest` (6-branch `isLanguage`), `StoreMerchantIdTest` (the nested `Reader` accepting bare
  string / `{"storeMerchantId":…}` / legacy `{"id":…}` — it is what keeps old outbox rows readable), `RolesTest`.

## Reaching 85 %

85 % of 2,027 is 1,723 lines. S1 ≈ 90 %, S2 ≈ 90 %, S3 ≈ 80 % lands at ~1,730 — **it clears the bar with almost no
slack.** If a wave falls short, the honest move is to name the specific classes that cannot be unit-tested and
exclude them explicitly, not to pad with assertion-free tests that execute lines. The known candidates, all already
identified: `EcsTaskFetcher` (static env capture), `EcsConfig`'s `ServiceDiscoveryClient.create()` bean methods
(static factory, needs real AWS credentials), `WebClientsUtils.withTypedErrors` (dynamic `Proxy`), and
`SecretCryptoAutoConfiguration.tryCreateAws` (builds a `KmsClient` inline). Each exclusion goes in with a comment
saying why, or the class gets a seam — the user's call, not a silent edit.

## Conventions every test must satisfy

Checkstyle runs on `src/test` with `maxWarnings = 0`, so: no repeated string literals (hoist to
`private static final`), **no `+` concatenation involving a literal** (`String.format`), import groups
`java/javax/jakarta/org/com/io/software/tools/lombok/reactor` blank-line separated with statics at the bottom, no
star imports, 140-column lines, no `TODO`. `verifyTestNaming` requires `*Test.java` in `src/test` and forbids
`*IntegrationTest` there. House style: package-private `class FooTest`, AssertJ only, `@Nested` groups per
operation, long lowerCamelCase method names stating the invariant (no `should`/`test` prefix), `private static`
fixture factories.

## Verification

```
./gradlew :store-commons:errors:test :store-commons:uaa-client-impl:test        # per wave, fast
./gradlew domainCoverage printDomainCoverage                                    # shared + pod-commons rows
./gradlew check                                                                 # all gates, incl. the twelve already green
```
Per wave: confirm the `shared` unit and merged columns rose, bump both floors to the achieved value rounded **down**
to 0.01 (JaCoCo truncates to two decimals before comparing), leave `integration` at 0.0, and confirm `pod-commons`
still reports its real number. Done when `shared` merged reads ≥ 85.0 % with floors to match, and `./gradlew check`
is green locally and in CI.
