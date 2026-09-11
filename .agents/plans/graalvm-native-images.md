# GraalVM native images for every Spring service

## Context

Twelve Spring services ship as JVM images: `store-core-gateway`, `uaa`, `tenancy`, `billing`, `pod-registry`,
`merchant`, `content`, `catalog`, `checkout`, `cua`, `payment`, `inventory` (`cvhome-platform/services.yaml`,
`runtime: spring`). Each `build.gradle` carries its own copy of the same `bootBuildImage` block — Paketo buildpacks,
jlink, the image name (`store-pod/catalog/catalog-service/build.gradle:15-31` and eleven twins). CodeBuild runs
`./gradlew bootBuildImage --publishImage -Pversion=$IMAGE_TAG` (`cvhome-platform/bootstrap/bootstrap.yaml:714`).

What the JVM costs us is written down in `cvhome-platform/flavours.yaml:11-18`: *"Floor for a Spring service:
1024 MB. The buildpack's memory calculator reserves ~680 MB of fixed regions … (240 MB code cache, ~170 MB metaspace
for ~28k classes, 250 x 1 MB thread stacks), so a 512 MB task cannot start a JVM at all."* Every Spring task in
every environment pays that floor, and pod services pay it once per pod. Startup is slow enough that
`health_check_grace_seconds` is 90 / 120 / 180 s per flavour.

A native executable has no code cache, no metaspace and no JIT: the fixed regions go away, the task fits in 512 MB,
and it starts in well under a second.

Native was tried before and abandoned. `native (#22)` (2024-06) and a run of `fix native` commits added the GraalVM
plugin; by 2025-09 every service had it commented out (`// alias(libs.plugins.graalvm)`, still in all twelve
`build.gradle` files), and the JDK 25 upgrade (#172) dropped the plugin from `libs.versions.toml`. What is left is
debris: `META-INF/native-image/resource-config.json` files with empty include lists (tenancy, catalog, merchant,
payment) and one `reflect-config.json` for a Spring Cloud Gateway class (gateway). No hint is registered in code
anywhere (`RuntimeHintsRegistrar`: zero hits).

### Why it will not "just work": the bean graph is decided at runtime today

Spring AOT evaluates `@Profile` and `@ConditionalOn…` **once, at build time**, and the native image contains only
the beans that matched. The same image must serve Fargate (every flavour, every pod) and the load-testing stack,
which runs these images with `SPRING_PROFILES_ACTIVE: lcl,test-stores` (`load-testing/stack/docker-compose.yml:24`).
Today these beans exist or not depending on the deployment:

| Where | Condition | What the native image would get wrong |
|---|---|---|
| `store-commons/autoconfigure/.../s2s/config/internal/EcsInfoConfig.java:13` | `AWS_EXECUTION_ENV == AWS_ECS_FARGATE` — an env var only Fargate sets | Absent at build time → no `EcsTask`, no `EcsTaskHealthIndicator` on AWS |
| `store-commons/ecs-commons/ecs-service-discoveryclient/.../ConditionalOnEcsDiscoveryEnabled.java:16`, `EcsConfig.java:24,52` | `spring.cloud.ecs.discovery.enabled` / `.namespace` — set only in `fargate-config.yml` | Built without the `fargate` profile → no Cloud Map discovery client → every `lb://` call fails on AWS |
| `store-commons/sso/sso-core/.../TestUserDatabaseInitializer.java:22` | `@Profile("test-stores")` | Load-testing stack gets no demo users |
| `sso-core/.../AdminUserDatabaseInitializer.java:21`, `OAuth2ClientDatabaseInitializer.java:23` | `com.asrevo.cvhome.uaa.seed.apply-on-boot` — `false` in `application.yml`, `true` in the `lcl` and `test-stores` slices, and an operator's one-shot toggle on Fargate (`uaa/.../application-fargate.yml:14`) | Frozen at the build-time value: either never seeds, or seeds on every start |
| `billing-service/.../PlanCatalogPublisher.java:37`, `PlanCatalogSeeder.java:48` | `stripe-sync-enabled` (`true` only in `application-lcl.yml:12`), `seed-enabled` | Frozen |
| `store-commons/autoconfigure/.../metrics/MetricsAutoConfiguration.java:50` | `cvhome.metrics.outbox.enabled` | Frozen |
| `fargate-config.yml:5-21` | `spring.cloud.loadbalancer.eager-load.clients` only under `fargate` | Spring Cloud LoadBalancer builds its per-client child contexts at AOT time from this list; without it no `lb://` client has one |

The per-flavour telemetry switches the platform injects (`OTEL_SDK_DISABLED`, `MANAGEMENT_OTLP_METRICS_EXPORT_ENABLED`,
computed in `cvhome-platform/modules/*/main.tf`) hit framework conditions the same way; whether the SDK and the
Micrometer push registry still honour them at runtime inside a bean that AOT kept is verified in Phase 4, not assumed.

### What native needs told explicitly (reflection, proxies, resources, serialization)

- **Service-to-service clients.** Every `-external-api` interface becomes two JDK dynamic proxies at runtime:
  `HttpServiceProxyFactory.createClient` and the typed-error wrapper `Proxy.newProxyInstance`
  (`store-commons/autoconfigure/.../s2s/utils/WebClientsUtils.java:66-87`). Native needs a proxy hint per interface
  and binding hints for every DTO those interfaces carry. `WebClientsUtils.java:73` also probes
  `Class.forName("org.springframework.data.domain.Pageable")`.
- **Outbox payloads.** `@OutboxHandler` methods in uaa/cua (sso-core), billing, tenancy, catalog and payment receive
  deserialized payloads.
- **Session serialization.** uaa and cua keep sessions in JDBC (`spring-session-jdbc`), so every type in a session is
  Java-serialized: Spring Security's own types plus `sso-core`'s `BrokeredPrincipal` and `PendingLink`.
- **Resources by name.** `init-sql/schema.sql`, `init-sql/data-common.sql`, `init-sql/data-test-stores.sql`,
  `init-sql/drop-legacy.sql` (`spring.sql.init.*-locations` in every JPA service's `application*.yml`) — Spring
  Boot's SQL-init hints cover only `schema.sql` / `data.sql`. The four empty `resource-config.json` files were an
  earlier attempt at this.
- **Stripe.** `stripe-java` (billing-service, payment-core) deserializes with Gson reflection over `com.stripe.model`.
- **Hibernate lazy to-one.** 30+ `FetchType.LAZY` associations across catalog, checkout, content, inventory. Native
  Hibernate cannot generate proxy classes at runtime; the six JPA services already apply the `hibernate-orm` Gradle
  plugin with `enhancement` commented out.

## Why the design is what it is

**Native, not the JVM with an AOT cache.** JDK 25's AOT cache (Leyden) with Spring AOT on the JVM gives a 2–3× faster
start with none of the constraints below — but it keeps the JVM's fixed memory regions, so the 1024 MB floor stays.
The goal is cost, and the floor is the cost. Native is the tool that removes it.

**One image per service for every deployment.** No `fargate`-profile AOT build next to an `lcl` one: the load-testing
stack measures exactly the bits that run on AWS, and a release stays one image per service. That forces the bean-graph
rule below.

**Bean existence never depends on a profile or a deployment property.** A bean that used to be conditional always
exists and decides at runtime (an initializer that returns early, a discovery client that is empty when disabled,
an AWS SDK client created lazily so it never touches credentials on a laptop). Profiles keep doing what they do well —
supplying property *values* through `application-<profile>.yml` and the config slices — because property sources are
still read at runtime in a native image. A build check (`verifyNoConfigurationSwitchedBeans`, in every module's
`check`) forbids `@Profile` and `@ConditionalOnProperty` in main code, so the next conditional bean fails the PR instead
of a release.

**Hints are typed code in the module that owns the class.** A `RuntimeHintsRegistrar` or AOT processor next to the
thing that needs it, found by the build — never a hand-maintained `reflect-config.json` dump, which is what the last
attempt left behind. The GraalVM tracing agent is a way to *discover* a missing hint; what it finds is turned into a
registrar. The reachability metadata repository (on by default in Native Build Tools) covers third-party libraries
where it has entries.

**A switch, not a fork.** The `org.graalvm.buildtools.native` plugin is applied to every service always, so
`processAot` runs in every `./gradlew build` and CI catches an AOT failure on the PR that caused it. Whether
`bootBuildImage` produces a native or a JVM image is one flag: `-Pnative`. Default stays JVM, so nothing changes for a
developer or for CodeBuild until `cvhome-platform` opts in, and a service that misbehaves natively can be pinned
back to the JVM image in its own `build.gradle` without touching the others. `lcl` and `bootRun` stay on the JVM:
the dev loop is not what we are optimising.

**Buildpacks, as today.** `bootBuildImage` with the plugin present switches the Paketo builder to its
`java-native-image` buildpack (Liberica NIK, GraalVM CE based: Serial GC, no PGO). Same task, same image names, same
registry, same tags — the image build gains one flag. Build args pin `-march=compatibility`: GraalVM defaults to
`x86-64-v3`, and Fargate does not promise which CPU generation a task lands on.

**The trade-offs, stated.** Build time and build memory grow: a Spring native image is one to two minutes and up to
12 GB of compiler heap per service (measured, *Verification*), twelve of them. Peak throughput under sustained load can be lower than a warmed JIT (no PGO in CE); at
0.25–0.5 vCPU the JIT's own warm-up usually costs more than it returns, but that is measured on the load-testing stack
before any task is resized. JVM-only tooling (`JAVA_TOOL_OPTIONS`, agents) does nothing in a native image.

### What it should save (estimate, to be confirmed by measurement)

Fargate list price, us-east-1, Linux/x86: $0.04048 per vCPU-hour, $0.004445 per GB-hour (confirm for the deployed
region). The `dev` flavour today, one task each, on-demand before Spot: 4.0 vCPU and 12 GB across the twelve Spring
services ≈ **$157/month**. At a uniform 256 CPU / 512 MB — the smallest Fargate shape — that is 3.0 vCPU and 6 GB ≈
**$108/month (−31%)**. Memory is the certain half (the floor exists only because of the JVM); CPU is the half
load-testing has to confirm. Prod multiplies it: its sizes are twice dev's, `desired_count` is 2, autoscaling reaches
12, and the seven pod services are billed once per pod. Faster start also shrinks the health-check grace and makes
the scale-to-zero schedules and Spot replacements cheaper in practice.

## One PR, a commit per phase

This plan ships as **one cvhome PR** with one commit per phase (`AGENTS.md` → *A plan is one PR; each phase is one
commit*). The other repos get their own PRs, in the order under *Other repos*.

**Split at the owner's request (2026-09-11):** the code that stands without GraalVM — Phase 1, the explicit PostgreSQL
dialect, beans wired by the type that carries their behaviour, the layout copy read as resources — is
cvhome-saas/cvhome#350, merged first. cvhome-saas/cvhome#349 is the native build and its hints, stacked on it.

## Phase 1 — One bean graph for every deployment

Make every row of the table above decide at runtime, then lock the rule in.

- `EcsInfoConfig`: beans always present; `EcsTask` resolves lazily and only when `AWS_EXECUTION_ENV` says Fargate;
  the health indicator reports *not on ECS* (UP, detail only) elsewhere.
- `ecs-service-discoveryclient`: `EcsDiscoveryClient` / `EcsReactiveDiscoveryClient` always registered; when
  `spring.cloud.ecs.discovery.enabled` is false or the namespace is blank they return no services and never build an
  AWS SDK client (lazy `Supplier`), so the composite discovery client falls through to the simple one as it does
  today. Delete `ConditionalOnEcsDiscoveryEnabled`.
- `sso-core` seed initializers and `TestUserDatabaseInitializer`: always beans, each checks its switch at run time
  (`apply-on-boot`; for test users, whether the `test-stores` profile is active, read from the `Environment`).
- billing `PlanCatalogPublisher` / `PlanCatalogSeeder`, `MetricsAutoConfiguration` outbox metrics: same pattern.
- `spring.cloud.loadbalancer.eager-load.clients`: Spring Cloud LoadBalancer builds a child context only for the client
  names it can see at build time. Proven on the first native `lb://` call (see *Deviations*); resolved without moving
  the list — `fargate-config.yml` and the platform's drift check stay as they are.
- `verifyNoConfigurationSwitchedBeans` (a Gradle check beside `verifyTestNaming`, every module): no `@Profile`, no
  `@ConditionalOnProperty`, `@ConditionalOnBooleanProperty` or `@ConditionalOnExpression` in main code; the failure
  message says why and points here.
- Unit tests for each converted class: switch on → acts, switch off → no-op, and no AWS client is built off-Fargate.
- QA: the lcl stack still starts and seeds (`qa/lcl-qa.md` smoke), and nothing about local behaviour changes.

## Phase 2 — Build: the native switch

- `gradle/libs.versions.toml`: `graalvm-native = { id = "org.graalvm.buildtools.native", version = "0.11.3" }` (the
  version Spring Boot 4.0.1's BOM names).
- `build-logic`: `com.asrevo.java-application-conventions` takes over what the twelve `bootBuildImage` blocks copy
  today — the ECR publish registry, the jlink settings, and the native/JVM switch: `-Pnative` →
  `BP_NATIVE_IMAGE=true`, `BP_JVM_VERSION=25`, `BP_NATIVE_IMAGE_BUILD_ARGUMENTS=-march=compatibility`; otherwise
  today's JVM image, unchanged. A service can opt out with `ext.nativeImage = false` and stays on the JVM under
  `-Pnative`.
- The twelve service `build.gradle` files: apply the plugin, drop the copied settings and the dead `graalvm` /
  `bellsoft` comments. Each keeps its `imageName = createImageName("…")` and `tags` lines exactly as they are:
  `cvhome-platform/scripts/check-catalog-drift.py` finds image paths with that regex.
- Remove the stale `META-INF/native-image/*.json` once Phase 4 shows what (if anything) replaces them.
- Gate: `./gradlew build -x test -x check` now runs `processAot` for all twelve and is green; `bootBuildImage`
  without `-Pnative` produces the same JVM images and tags as before.

## Phase 3 — Runtime hints

Each in the module that owns the code, each with a unit test using `RuntimeHintsPredicates`:

- `store-commons:autoconfigure`: an AOT processor that registers, for every interface built through
  `RestClientBuilder` / `WebClientBuilder.buildClient`, both JDK proxy hints and binding hints for its method
  signatures (found by scanning `@HttpExchange` interfaces under `com.asrevo.cvhome` at build time);
  the `Pageable` probe; the `init-sql/**` resource pattern.
- Outbox: binding hints for every `@OutboxHandler` parameter type, from a bean-registration AOT processor next to the
  outbox wiring.
- `sso-core`: serialization hints for what sits in a JDBC session (`BrokeredPrincipal`, `PendingLink`, and any
  Spring Security type the reachability metadata does not already cover).
- billing / payment: Stripe model reflection, unless the reachability metadata repository covers `stripe-java` 31.x.
- Hibernate: decide from the first native run — build-time enhancement (`hibernate { enhancement { … } }`, already
  stubbed in the six JPA services) versus eager to-one; write down which and why. (Decided: enhancement — see
  *Deviations*.)

## Phase 4 — Every service native, verified

Order, simplest first: `pod-registry` → `tenancy` → `billing` (Stripe) → `store-core-gateway` (WebFlux, Spring Cloud
Gateway) → `uaa` / `cua` (authorization server, JDBC sessions, Thymeleaf, embedded SPA) → the six JPA pod services.

Per service:
1. `./gradlew :<module>:nativeCompile` on the host (GraalVM 25), then run the executable in place of the JVM process
   on this worktree's `lcl --stack` (same ports, env and `lcl,test-stores` profiles), and exercise its
   `http/*.http` blocks and QA cases. Failures go back to Phase 3 as typed hints; the tracing agent only to find them.
2. Record start time and RSS against the JVM process for the same service.
3. Check each deployment toggle from the Context table and the telemetry switches behaves identically.

Then the image path once end to end: `./gradlew :store-core:pod-registry:pod-registry-service:bootBuildImage -Pnative`
(proves Liberica NIK 25 on the Paketo builder and the build args), and the full set for the load-testing stack as
the owner's pre-step.

## Phase 5 — Keep it native

- `.github/workflows/native-compile.yml`: nightly and `workflow_dispatch`, one matrix job per service running
  `nativeCompile`, so a change that breaks the native build is found before a release, not by CodeBuild. (A compile
  check, not a deploy: nothing is published.)
- Docs: `references/build-system.md` in the `project-structure` skill — the switch, the bean-graph rule and why, how
  to add a hint, the tracing agent; the `AGENTS.md` build line for `bootBuildImage -Pnative`; a native section in
  `qa/lcl-qa.md`.

## Other repos

1. **cvhome** `feat/graalvm-native` — this plan. Land first.
2. **cvhome-platform** `feat/graalvm-native`, after 1 merges:
   - `bootstrap/bootstrap.yaml`: a second image project, `<project>-<env>-2-images-native` — the same build with
     `-Pnative -PnativeImageArgs=-J-Xmx12g --max-workers=4` on `BUILD_GENERAL1_XLARGE`, started only by hand and
     chained into `3-apply`. `2-images` stays the JVM build and the line's default (the owner's call: native is
     opt-in per run). Timeout and machine confirmed by its first run; the bootstrap stack update is an operator step.
     As built: cvhome-saas/cvhome-platform#10.
   - (No drift-check change: the `lb://` names reach the build through an AOT-only environment post-processor, not
     by moving `eager-load.clients`.)
3. **load-testing** `feat/graalvm-native`: JVM vs native on `make stack-up` with the same `LOAD_TAG` workload — start
   time, RSS, p95, throughput at a fixed rate — as rows in `docs/baseline.md`; the compose `x-limits` for native;
   note that `JAVA_TOOL_OPTIONS` is inert for native images.
4. **cvhome-platform** again, from those numbers: native sizes in `flavours.yaml`, lower
   `health_check_grace_seconds`. This is the change that lowers the bill — and with JVM the default build, it needs an
   environment that runs native by default first (a flavour key choosing the image project), since a JVM task cannot
   start in 512 MB.
5. Nothing in `public-dkr` (the default Paketo tiny builder already carries the native-image buildpack) or `lcl`
   (Java runs on the host JVM).

## Deviations, as built

- **Spring Data generates repositories at build time, and that needs two things the build did not have.**
  - *The JDBC dialect.* `AbstractJdbcConfiguration.jdbcDialect` asks the live database; Spring Data's AOT repository
    contributor looks the bean up during `processAot`, which instantiated the `DataSource` chain and failed with
    "Failed to determine a suitable driver class". `store-commons/autoconfigure/.../jdbc/PostgresJdbcConfiguration`
    states PostgreSQL (the only database any deployment runs) with the unused argument `@Lazy`; tenancy, billing and
    pod-registry's `JdbcConfig` extend it. Disabling AOT repositories (`spring.aot.jdbc.repositories.enabled=false`)
    was the alternative, and would have given up the start-time and reflection savings for nothing.
  - *Parameter names.* The JPA repositories live in the `-core` libraries, which Spring Boot's plugin does not compile
    with `-parameters` (it only configures the module it is applied to): "MethodParameter.getParameterName() must not
    be null". `-parameters` now applies to every module (`com.asrevo.java-common-conventions`).
- **The GraalVM plugin is loaded once, at the root** (`apply false`), and applied by each service. Twelve subprojects
  each resolving it gave its shared build service twelve incompatible classes: "GraalVMReachabilityMetadataService$Inject_
  cannot be cast to GraalVMReachabilityMetadataService".
- **Spring Boot 4.0.1 does not set `BP_NATIVE_IMAGE`.** With the GraalVM plugin applied it stamps
  `Spring-Boot-Native-Processed` into the jar manifest, which on its own makes the Paketo builder go native. The
  convention therefore sets `BP_NATIVE_IMAGE` explicitly in both directions rather than only adding it for `-Pnative`.
- **The shared config slices are resources a native image leaves out.** `common-config.yml` and the other
  `*-config.yml` files ship in the autoconfigure jar and arrive through `spring.config.import`; the first native start
  refused with "Config data resource ... does not exist". `SharedResourcesRuntimeHints` (autoconfigure,
  `META-INF/spring/aot.factories`) includes them and `init-sql/**`.
- **The guard is a Gradle check, not ArchUnit.** The ArchUnit rules are bound per domain package, so they would never
  see `store-commons`, where most of the switches were. `verifyNoConfigurationSwitchedBeans` sits beside
  `verifyTestNaming` in `com.asrevo.java-common-conventions` and runs in every module's `check`.
- **`IssuerRealmsCondition` stays a condition.** It is evaluated at build time like the others, but it keys on the
  issuer map in `common-config.yml` (profile-neutral) and on `issuer-uri` / `jwk-set-uri`, which no deployment sets, so
  it answers the same everywhere. It is the one custom `Condition` left in main code.
- **`EcsTaskHealthIndicator` is present everywhere**; off Fargate it reports UP with `ecs: not an ECS task` instead of
  not existing. The task metadata is fetched on first read, not at start.
- **`lb://` names reach the build without moving a contract.** Proven on the native gateway's first `lb://pod-registry`
  call: "GenericApplicationContext must be an instance of AnnotationConfigRegistry" — Spring Cloud LoadBalancer can
  only use child contexts generated at build time, for the names in `eager-load.clients` as the build sees it, and
  that list lives in `fargate-config.yml`. `LoadBalancerClientsAotEnvironmentPostProcessor` (autoconfigure,
  `spring.factories`) adds, during `processAot` only, every name in `com.asrevo.cvhome.services` and every
  `<gateway-service-name>.<namespace>`. The list in `fargate-config.yml` and the drift check that reads it are
  untouched. Known limit: a pod registered with an `INTERNAL` endpoint is called as `lb://spg.<its namespace>`, a name
  only pod-registry knows; every deployment registers pods `EXTERNAL`.
- **Spring Data's generated property accessors are off** (`spring.aot.data.accessors.enabled: false` in
  `common-config.yml`, read by `processAot`). In the native billing the generated `PlanEntity` accessor was handed an
  `Integer` for the boolean `active` ("Can not set boolean field PlanEntity.active to java.lang.Integer"); the same jar
  on the JVM with `spring.aot.enabled=true` read it correctly, and the native binary with generation off reads it
  correctly. Spring Data falls back to reflective access, which its entity hints cover; the generated repositories stay.
- **`authorizationService` is declared as `JdbcOAuth2AuthorizationService`.** Spring Security's AOT processor
  recognises that bean by its declared type and only then registers the security Jackson modules (loaded by class
  name) and their mixins; declared as the interface, native uaa and cua failed on the first token with
  "ClassNotFoundException: CoreJacksonModule", then "Could not resolve type id 'java.util.Collections$UnmodifiableMap'".
- **More registrars than planned**, each from a native failure, each scanning rather than listing: `ValueObjectRuntimeHints`
  (every `Identifier` and `commons.domain` value object — the pod services refused to bind `pods[0].id` to `PodId`),
  `HibernateRuntimeHints` (custom `UserType`s, and the array class of every entity's id type — Hibernate's multi-id
  loader failed on `UUID[]` in uaa/cua), `OutboxRuntimeHints` (namastack hands Spring a `ScheduledMethodRunnable` over
  methods it looks up by name, found by the bytecode's reference to that class; and every `@OutboxHandler`),
  `UaaSdkRuntimeHints` (the admin SDK's own Jackson DTOs, including a private record). `BytecodeReferences` is the
  shared constant-pool reader for the two that find reflection by what the bytecode names.
- **Hibernate: build-time enhancement, lazy loading only, in native builds only**, for the four modules with lazy
  to-one associations (catalog-, checkout-, content-, inventory-core) and `store-pod/commons/store-commons` (their mapped
  superclasses and embeddables). The decision the plan deferred, taken on evidence: catalog's native search answered
  500, "Generation of HibernateProxy instances at runtime is not allowed when the configured BytecodeProvider is 'none'".
  Enhanced, a lazy reference is an uninitialised instance of the entity class instead of a generated proxy subclass.
  Dirty tracking and association management stay off; there is no lazy basic attribute or `@Lob` anywhere.
  - *Native builds only* (`com.asrevo.java-common-conventions`: `-Pnative`, or a `native*` task such as `nativeCompile`;
    it enhances every module that applies the Hibernate plugin, and the services that apply it hold no entities). The
    first cut enhanced every build, and CI's coverage job failed on the four enhanced domains: enhanced bytecode is not
    reproducible across machines — the same commit gave `Product` three different class checksums on the unit runner,
    the integration runner and a laptop, while every run on one machine (isolated, parallel, fresh JVM, any file order)
    matched — and that job recompiles, then matches JaCoCo's data to the classes by checksum. The JVM generates its
    proxies and never needed enhancement, so a JVM build now compiles plain entities, byte-identical to `main`, and the
    JVM's lazy-loading behaviour does not change at all.
  - The four services' integration suites pass both ways: plain (every JVM build and CI) and enhanced (`-Pnative`,
    *Verification*). An enhanced class implements Hibernate's interfaces (`ManagedComposite`,
    `PersistentAttributeInterceptable`), so `store-pod/commons/store-commons` exports Hibernate as `compileOnlyApi`:
    merchant-core's tests, which build an `AuditSection`, do not compile against enhanced classes without it.
- **What uaa and cua persist about an authentication** is registered in sso-core (`AuthenticationStateRuntimeHints`):
  every `Serializable` class of Spring Security and of sso-core, for the Java-serialized JDBC session (first native
  sign-in: "SerializationConstructorAccessor class not found for FactorGrantedAuthority"), and the same types plus every
  Spring Security `*Mixin` for reflective construction, for the authorization rows' JSON ("Cannot construct instance of
  FactorGrantedAuthority"). Spring Security 7.1's own AOT lists do not include its new `FactorGrantedAuthority`. The
  test serializes real session contents through a recording stream and fails on any class it writes unregistered.
- **cua injects its storefront entry point instead of calling the `@Bean` method.** `appSecurity(...)` called
  `storefrontEntryPoint(requestCache, csrfCookies)` directly; an inter-bean call with arguments bypasses the instance
  supplier a native image generated ("Could not resolve matching constructor on bean class AuthenticationEntryPoint").
  A scan for other `@Bean` methods with parameters called directly found none.
- **`ErrorAndValidationRuntimeHints`**: every `ConstraintValidator` and its constraint (Spring's AOT skips a bean class
  whose method constraints trip HV000151, as tenancy's and checkout's external APIs do — tenancy's signup failed on
  `StrongPasswordValidator`), and the error contract's records (`FieldError` in every validation body). The outbox
  registrar also covers `@OutboxEvent` types, whose SpEL keys call their accessors.
- **Outbox metrics** keep their per-service switch (`cvhome.metrics.outbox.enabled`, set in each service's own
  `application.yml`, so it never differed by deployment) as a runtime check: a disabled binder registers no meter.
- **Tests stay on the JVM; `processTestAot` is off.** With both plugins applied, `test` puts the AOT-processed test
  classes on its classpath, so every test run first booted each Spring test context at build time — and failed in
  gateway-service, whose tests are not Spring tests ("Could not find or load main class SpringBootTestAotProcessor").
  Found by `verify-before-push.sh`. `graalvmNative { testSupport = false }` does not remove that dependency; disabling
  the task does. `nativeTest` is not used.
- **The `aot` packages are out of the integration coverage column**, like the AWS-only code before them
  (`jacoco-aggregate-conventions`, `buildTimeOnlyExcludes`): only `processAot` runs them, and no integration test runs
  the AOT engine. They still count in unit and merged, where their tests credit them. Counted, they took `shared`'s
  integration figure to 63.3% against its 0.69 floor; excluded, it is 70.2% (`sso` 84.1%). The floor is unchanged.

- **Only a native build's jar says `Spring-Boot-Native-Processed`.** With the GraalVM plugin Spring Boot stamps it
  into every bootJar, and that alone brings Paketo's native-image buildpack into a JVM build: the image stayed a JVM
  one (`BP_NATIVE_IMAGE=false`), but the build downloaded Liberica NIK (GraalVM, ~450 MB from GitHub) instead of the
  JDK it jlinks, and eleven parallel builds failed fetching it. `java-application-conventions` removes the
  attribute without `-Pnative`; a JVM image build is main's recipe again.
- **What the first native load test found** (load-testing stack, 2026-09-11; every one passed on the JVM images and
  on the lcl run, whose `.http` suite never reached these paths):
  - *JSON no controller names.* Content's banner, FAQ, post and policy metas are records kept as JSON columns
    (`JsonCodec.read(entity.getMeta(), BannerMeta.class)`), and payment's `ReadableTransactionList extends
    ReadableList<ReadableTransaction>` hides its element behind `T`: "Record components not available for record
    class BannerMeta" on every storefront banner read. `ModelTypeRuntimeHints` registers every type in a `model`
    package — where this codebase keeps its JSON shapes — for binding.
  - *Spring Data JDBC's generated repositories.* They convert a `@Query`'s rows into the entity even when the method
    returns a DTO: billing's `findVisible` (`PlatformSubscriptionRow`) failed with "Required property store not
    found for class StoreSubscriptionEntity", tenancy's `storesPerPod` likewise. `spring.aot.jdbc.repositories.enabled:
    false` — the alternative this plan had set aside, now with a reason; JPA's generated repositories stay on.
  - *A `ResourceBundle` in a native image.* `LayoutDefaults` read its starter copy with `ResourceBundle.getBundle`:
    "Can't find bundle for base name layout-defaults.messages", and a registered bundle would have carried only the
    build locale. It now reads the five files as classpath resources (UTF-8, English base underneath), with a
    resource hint beside it.
  - *Telemetry frozen off.* The OpenTelemetry starter picks its real SDK or a no-op one on `otel.sdk.disabled`,
    which `common-config.yml` defaults to `true` (environments turn it on: `OTEL_SDK_DISABLED=false` on the load
    stack and on Fargate flavours with monitoring). AOT decided that at build: "OpenTelemetry Spring Boot starter has
    been disabled" in every native service, none of them in Prometheus. The rule of Phase 1, for a third-party
    auto-configuration `verifyNoConfigurationSwitchedBeans` cannot see: `OpenTelemetryAotEnvironmentPostProcessor`
    sets it `false` during `processAot` only, so the real SDK is generated, and the SDK itself honours
    `otel.sdk.disabled=true` at run time with a no-op. Micrometer's own OTLP registry stays off natively whatever
    `MANAGEMENT_OTLP_METRICS_EXPORT_ENABLED` says — which is `common-config.yml`'s intent (the bridge is the one path);
    the platform turning it on for monitoring flavours is a JVM-side double export to raise with cvhome-platform.
  - *A cache that never ran* (second native run, once telemetry worked): merchant served 190 req/s against 7.6 on
    the JVM, and catalog's list reads were 3-7x slower. Traces showed catalog calling merchant's `/api/v1/store` on
    every product mapping; the STORE cache showed 0 lookups in catalog, checkout and payment. Their
    `externalMerchantStoreService` beans were declared as `ExternalMerchantStoreService` while the object was a
    `CachedExternalMerchantStoreService` with `@Cacheable`; Spring builds AOP proxies ahead of time from the declared
    type, so natively there was no caching proxy at all, silently. The beans now declare the caching class (the fix
    `authorizationService` got for Spring Security's Jackson modules).
  - Not a finding: "Cannot change HTTP Accept-Language header" in eight services is
    `RequestCacheAwareLocaleInterceptor` swallowing, at DEBUG and by design, a resolver that takes no locale.
## Verification

All on 2026-09-10, in this worktree, GraalVM CE 25.0.2 on an M4 Pro (14 cores, 48 GB).

- **Unit, per touched module** — autoconfigure, sso-core, ecs-service-discoveryclient, billing, tenancy, pod-registry,
  cua: green, including a `RuntimeHintsPredicates` test per registrar and the session-serialization guard.
- **Integration** — catalog, checkout, content, inventory with Hibernate enhancement on (`-Pnative`): 244 tests, 0 failures (rerun 2026-09-11
  after enhancement became native-only). Plain, the same suites run in every build and in CI. The whole
  pipeline — every module's unit and integration tests, the coverage floors, both frontends — is
  `verify-before-push.sh`, whose receipt the push requires.
- **`processAot`** — all twelve services.
- **`nativeCompile`** — all twelve: 17 min three at a time; 52 s (gateway) to 1m37s (billing, payment) each;
  payment peaks at 11.9 GB uncapped, and fails at 7 GB (`GC overhead limit exceeded`), passes at 12 GB.
- **Native on a live stack** — every executable swapped in for its JVM process (`qa/lcl-qa.md` § 17): all twelve
  healthy; console sign-in through native gateway and native uaa; the full `.http` suite passes the same 392 of 592
  requests as the JVM, on `main` and on this branch (0 regressions either way). Start 0.12–1.07 s native vs 1.2–4.3 s
  JVM; physical footprint 103–141 MB vs 329–552 MB (≈1.6 GB vs 5.3 GB for the twelve).
- **Images** — `bootBuildImage` builds the JVM image with main's recipe (the jar is not native-stamped: no
  native-image buildpack, Liberica JDK, launch process `java … JarLauncher`); `bootBuildImage -Pnative` builds the
  native one (Liberica NIK 25.0.4, arm64 locally). All twelve native images built on this laptop (Docker Desktop at
  24 GB, one compile at a time, `-J-Xmx12g`): ~4 minutes each, 216–309 MB images.
- **Load-testing stack, JVM vs native** (2026-09-11, `load-testing` `make stack-up` with `LOAD_TAG`, fresh database
  each run; JVM images at `LOAD_MEM=1g`, native at `512m`; smoke, storefront-browse 30 VUs 3m, guest checkout 3m,
  production mix 3m, breakpoint to 600 req/s). Three native runs, each fixing what the one before found (see
  *Deviations*); the last, against the JVM:

  | | JVM @ 1 GB | native @ 512 MB |
  | --- | --- | --- |
  | failed requests, every script | 0 | 0 |
  | start, per service (all twelve at once) | 4.4–10.2 s | 0.3–2.2 s |
  | memory peak under load, twelve services | 4,475 MiB | 2,284 MiB |
  | browse p95: `catalog:listing` / `catalog:search` / `catalog:product` | 10.0 / 24.8 / 11.0 ms | 11.1 / 24.9 / 11.5 ms |
  | guest checkout p95: `checkout:checkout` | 60.8 ms | 30.0 ms |
  | breakpoint: requests before k6 ran out of VUs | 146,309 | 168,858 |
  | breakpoint p95: `catalog:product` / `inventory:availability` | 18.4 / 8.0 ms | 19.1 / 11.6 ms |

  No restart and no OOM kill in any container. Open: `catalog:search` inside the production mix is ~90 ms natively
  against 24 ms on the JVM in all three native runs (in the browse script the two are equal); not yet explained.
  The storefront image is amd64 and emulated on this arm64 host in both runs, so `page:*` is slow in both.
- **Not verified:** the Fargate deployment switches with a native image (Cloud Map discovery on a real namespace,
  `OTEL_SDK_DISABLED` read at run time now that the real SDK is built) — they need an environment; the discovery
  and ECS paths are unit-tested, and the load stack proved telemetry exports natively.
