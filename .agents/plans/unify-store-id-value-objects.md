# Unify `ManagerStoreId` into `StoreMerchantId`

## Context

A store has two identifier types in this repo, for no reason other than history:

| | `ManagerStoreId` | `StoreMerchantId` |
|---|---|---|
| Shape | `record(ObjectId id)` | `record(String storeMerchantId)` |
| Used by | store-core only — tenancy, billing, pod-registry, gateway (89 Java files) | store-pod — every pod (~1,570 refs / ~465 files) |
| Persisted as | `varchar(24)` (Spring Data JDBC, `Identifier`→`String` writer) | `varchar(50)` (JPA `@Embedded`, column `STORE_MERCHANT_ID`) |
| JSON | `{"id":"65f0…"}` | `{"storeMerchantId":"65f0…"}` |
| Validation | `new ObjectId(s)` — throws on non-hex | none |

**They hold the same value.** Every store id in the system is a 24-char ObjectId hex minted by
`ManagerStoreId.newId()` in tenancy (`ManagerStoreEntity.java:58`) and handed to the pods as a string. The
proof is that two places exist only to translate between them:

- `CustomPermissionEvaluator.java:60,64,67` — `new ManagerStoreId(((StoreMerchantId) targetId).storeMerchantId())`
- `StoreEntitlements.java:67` — same conversion, on every catalog write

That translation is also a latent 500: a request with `?store=abc` passes the argument resolver unvalidated,
then `new ObjectId("abc")` throws `IllegalArgumentException` deep inside the permission evaluator.

**Outcome:** one type, `StoreMerchantId`, carrying a `String`. `ManagerStoreId` is deleted. The pods are
untouched (that is why this direction was chosen — the alternative rewrites ~465 files and remaps ~20 JPA
embeddables). `ManagerOrgId` is **not** part of this: it identifies an organization, which owns many stores.

## Decisions taken

1. **`StoreMerchantId` survives**, keeping its name and its `storeMerchantId` component — so no
   `@AttributeOverride`, no column rename, no SQL migration anywhere.
2. **Wire shape becomes a bare string** (`"65f0…"`), via `@JsonValue`, with a **tolerant deserializer** that
   still accepts `{"id":…}` and `{"storeMerchantId":…}`. The tolerance is not politeness — outbox rows and
   stored event payloads (`tenancy-events StoreEvent`, `billing-events SubscriptionEvent`) already hold the
   old object shape on disk and must still deserialize after deploy.
3. Four adjacent fixes ride along (each forced open by the merge anyway): pod-registry's missing read
   converter, typing the JWT store claim, 400 on a malformed `?store=`, and deleting the duplicated
   serialize-param resolver.

## Non-goals

- `ManagerOrgId` — untouched. (It does have a real defect: its `String` ctor silently yields a `null` inner
  `ObjectId` on short input while `ManagerStoreId` throws, and `SecurityUtils:76-82` leans on that leniency
  for a missing `org` claim. Separate change.)
- Column widths. `varchar(24)` in store-core and `varchar(50)` in the pods both keep working; no migration.
- `PodId` / `IdentityId` keep their `{"id":…}` JSON. Only the store id changes shape.

## Step 1 — the unified type (`../../store-commons/commons`)

Rewrite `commons/domain/StoreMerchantId.java`; delete `commons/domain/ManagerStoreId.java`.

```java
@JsonDeserialize(using = StoreMerchantId.Reader.class)
public record StoreMerchantId(String storeMerchantId) implements Identifier, Comparable<StoreMerchantId> {

    /** Ids are still minted as ObjectId hex — tenancy is the only caller. */
    public static StoreMerchantId newId() {
        return new StoreMerchantId(new ObjectId().toHexString());
    }

    @JsonValue
    @Override
    public String getId() { ... }

    @Override
    public int compareTo(StoreMerchantId o) { ... }   // unchanged

    /** Accepts "hex", {"storeMerchantId":"hex"} and {"id":"hex"} — the last two are legacy payloads on disk. */
    static final class Reader extends ValueDeserializer<StoreMerchantId> { ... }
}
```

Notes:
- `Identifier` (`commons/domain/Identifier.java:5`) already extends `Serializable` and declares
  `Object getId()`, so the covariant `String` return satisfies it and `BaseEntity<E, T extends Identifier>`
  accepts `StoreMerchantId` as an `@Id` type unchanged.
- **No validation in the constructor.** The wildcard sentinel (Step 2) is the non-hex value `"*"`, and cua
  builds ids from OAuth2 client ids. Validation belongs at the HTTP edge — Step 2.
- `commons/build.gradle` keeps `libs.mongodb.bson` (still needed for `newId()`) and `compileOnly
  libs.jackson.databind` — Jackson 3, `tools.jackson.*`, matching what `ManagerStoreId` used.
- Check for `Map<StoreMerchantId, …>` fields that get serialized — `@JsonValue` covers values, not map keys.
  (The known caches in `StoreEntitlements` are Caffeine, not JSON.)

## Step 2 — security and s2s plumbing (`../../store-commons/autoconfigure`, 6 files)

- `s2s/config/internal/CustomPermissionEvaluator.java` — the three `new ManagerStoreId(((StoreMerchantId)
  targetId)…)` conversions collapse to a plain `(StoreMerchantId) targetId` cast, and the `STORE-CORE.*` /
  billing casts change type. This is the file the whole refactor is for.
- `s2s/services/PermissionAccessChecker.java` (19 refs), `StoreRoleAccessChecker.java`,
  `StoreSecurityService.java`, `StoreOrgOwnerRetriever.java` — signature swaps.
- `commons/domain/UserOrgStoreIdentity.java` — `String store` → `StoreMerchantId store`, removing the
  org-typed / store-untyped asymmetry. **The `"*"` wildcard needs a home:** keep it in the security layer,
  not the domain — turn `SecurityUtils.WILD_CARD_STORE_ACCESS` into a
  `static final StoreMerchantId WILD_CARD_STORE_ACCESS = new StoreMerchantId("*")` and compare against it in
  `PermissionAccessChecker`. Do not push a `"*"` sentinel into `commons/domain/`.
- `s2s/utils/SecurityUtils.java:71,73,77,82` — wrap the `store` claim: `new StoreMerchantId(adminStore)`.
  The JWT itself is unchanged (the claim stays a raw string).
- **Delete** `s2s/utils/StoreSerializeParamArgumentResolver.java` (the `ManagerStoreId` → `?store` one) and
  its registration at `s2s/utils/WebClientsUtils.java:70`; `StoreMerchantIdSerializeParamArgumentResolver`
  (registered at line 69) is now the only one and covers both sides.
- `s2s/config/internal/ServletStoreMerchantIdArgumentResolver.java:18-32` — reject a malformed value with
  `ObjectId.isValid(...)`. Prefer a Spring binding exception (`ServletRequestBindingException` /
  `MissingServletRequestParameterException`) so Spring's own handler yields 400 without a second
  `@ControllerAdvice`; confirm the choice against `references/error-handling.md` before writing it, and
  replace the existing `IllegalArgumentException` for the missing-param case at the same time.

## Step 3 — store-core services (mechanical type swap, ~85 files)

Same pattern per module: swap the type, drop the `.getId().toString()` hops that existed only to unwrap an
`ObjectId`, update the `'ManagerStoreId'` SpEL literal, register the read converter, update SpringDoc.

**tenancy** (24 service + 2 commons + 2 events files)
- Entities/repos: `ManagerStoreEntity`, `OrgMemberEntity`, `OrgInvitationEntity`, `TenancyAuditEntity`,
  `ManagerStoreRepository`, `OrgInvitationRepository`. *(`OrgInvitationRepository.java:15` types its id as the
  store id, which looks wrong for an invitation — preserve behavior, flag in the PR, fix separately.)*
- `config/JdbcConfig.java:33-38` — `Converter<String, ManagerStoreId>` → `StoreMerchantId`.
- `config/SwaggerConfig.java:16-17` — `replaceWithClass(StoreMerchantId.class, String.class)`.
- 12 `'ManagerStoreId'` literals across `RouterApi`, `UserAccountApi` (8), `StoreLifecycleApi:39`,
  `StoreManagerApi:111,119` → `'StoreMerchantId'`. *(The evaluator ignores this argument, but a stale literal
  is exactly the kind of drift the review policy calls out.)*
- Services, DTOs (`ManagerStoreDto`, `OrgMemberDto`, `InvitationDto`, `ListManagerStoreQuery`), events
  (`StoreEvent.store()`, `StoreCreatedEvent`), `StoreNotFoundException`, and the 4 affected tests.

**billing** (27 service + 13 events + 4 external-api + 3 commons files)
- Entities keyed by the store id: `StoreSubscriptionEntity:44` (PK), `SubscriptionInvoiceEntity`,
  `SubscriptionAuditEntity`, `StripeRequestEntity`, `OrgTrialGrantEntity`, + repos.
- `config/JdbcConfig.java:52`, `config/SwaggerConfig.java`, 9 SpEL literals in `SubscriptionApi`,
  `InvoiceApi`, `ExternalEntitlementApi`.
- `guard/StoreEntitlements.java` — caches key on `StoreMerchantId` directly; `snapshot()`/`fetch()` lose the
  conversion at line 67. This is the second payoff site.
- `billing-external-api` client signatures (`ExternalEntitlementService.snapshot/snapshots`,
  `ReactiveExternalEntitlementService.blockedStores`, `IEntitlementService`) and
  `EntitlementSnapshot.degradedOpen`.
- `billing-events` — `SubscriptionEvent.store()` + 8 events + 4 commands.

**pod-registry** (5 + 1 files)
- `domain/PodStorePlacementEntity.java:33-35` (`@Id`), `repository/PodStorePlacementRepository`,
  `PodPlacementService`, `PlacementRequest`, `RecordPlacementRequest`, tests.
- `config/JdbcConfig.java` — **add** the `Converter<String, StoreMerchantId>` that is missing today. The
  file's own javadoc (lines 17-21) warns a missing reader is a runtime `ConverterNotFoundException`, and
  `PodStorePlacementEntity`'s `@Id` has no reader — so first read of a placement row throws. Verify by
  reading a placement back in the QA pass.

**gateway** (1 file)
- `client/StoreBillingStatusClient.java:11,75` — `List<StoreMerchantId>`, and `it.getId().toString()` →
  `it.storeMerchantId()`. Note the payload from billing changes from `[{"id":"65f0…"}]` to `["65f0…"]`; both
  ends ship in this PR, and billing's reader stays tolerant either way.

## Step 4 — seller-ui (5 TS files)

- `projects/seller-core/src/lib/models/commons.ts:1-3` — delete the `ManagerStoreId` interface;
  `ManagerStore.id` becomes `string`. Leave `orgId: IdentityId` and `PodId` alone.
- Consumers: `store-autocomplete.component.ts`, `store-autocomplete.facade.ts`, `header.component.ts`,
  `header.facade.ts` — drop the `.id` unwrap.
- `projects/seller-core/stores/src/lib/services/store-service.model.ts:88,97,106,116` —
  `storeMerchantId?: unknown` → `string`, now that the shape is pinned.
- `projects/seller-core/stores/src/lib/services/pod.service.ts:51,68` declares a local `ManagerOrgId` — leave
  it; org ids are out of scope.

## Step 5 — docs

Update the `ManagerStoreId` references in the authoritative map:
`.claude/skills/project-structure/references/{multi-tenancy,shared-libraries,database-schemas,api-conventions}.md`.
Leave the historical records alone (`.claude/plans/*`, `qa/*`, `extra/migrations/*.sql`).

No `.http` changes are expected — the `?store=` query param is unchanged — but re-run the touched services'
blocks, since response bodies now carry a bare string.

## Verification

Gates:
- [ ] `./gradlew checkstyleMain checkstyleTest` (warnings = errors)
- [ ] `./gradlew build -x test -x check`
- [ ] `./gradlew test` (Docker up for Testcontainers) — tenancy, billing, pod-registry all have tests that
      touch these ids
- [ ] `cd store-core/seller-ui && npm run build`

New tests worth adding — **`StoreMerchantId` has zero tests today**, and the tolerant reader is the one piece
that cannot be caught by the compiler:
- [ ] Deserializes all three shapes (`"hex"`, `{"storeMerchantId":"hex"}`, `{"id":"hex"}`) and serializes to a
      bare string.
- [ ] A `StoreCreatedEvent` / `SubscriptionEvent` payload captured in the **old** object shape round-trips.

End-to-end, against `./extra/scripts/run-lcl.sh` (background it; stop with `SIGTERM`):
- [ ] Log in to seller-ui as a store admin — header and store-autocomplete still resolve store names (the TS
      shape change).
- [ ] A catalog write — exercises `CustomPermissionEvaluator` → `PermissionAccessChecker` and
      `StoreEntitlements` on the merged type.
- [ ] Read a billing subscription and an invoice; confirm the gateway's blocked-store cache still populates
      from `blockedStores()`.
- [ ] Read a pod placement back (proves the added pod-registry converter).
- [ ] **Tenant isolation:** repeat the catalog write as store 2 (`65f023632bc46470c104b75f`) and confirm no
      cross-store read; confirm a principal without the token gets 403.
- [ ] `?store=abc` now returns 400, not 500.
- [ ] Provision a new store end to end — `StoreMerchantId.newId()` still mints 24-hex, and the row lands in
      `tenancy.manager_store` (`varchar(24)`) and in a pod (`varchar(50)`).

## Sequencing

One PR, one commit-series — a type cannot be half-merged. Branch `refactor/unify-store-id`. Deploy
store-core services together (billing↔gateway share the changed `blockedStores` shape); the pods need no
redeploy, since their `?store=` contract and DDL are unchanged.
