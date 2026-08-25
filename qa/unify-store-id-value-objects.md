# QA — one store id (`ManagerStoreId` merged into `StoreMerchantId`)

A store had two identifier types: `ManagerStoreId` (an `ObjectId` wrapper) in store-core, `StoreMerchantId` (a
`String` wrapper) in the pods. They always held the same value, and two files existed only to translate between
them. They are now one type, `StoreMerchantId`, and it serializes as a **bare string** rather than an object.

- **Scope** — store-commons · tenancy · billing · pod-registry · gateway · seller-ui (the pods' own code is
  unchanged)
- **Change** — branch `refactor/unify-store-id`, plan `.claude/plans/unify-store-id-value-objects.md`
- **Cases** — 18
- **No migration.** Column names, widths and values are untouched: `varchar(24)` in store-core, `varchar(50)`
  in the pods, the same 24-char hex in both. If you find yourself writing SQL to fix data, something is wrong.

Each case is tagged:

- **[verified]** — run against a live local stack (or a real test) during the build, and passed.
- **[unit only]** — proven by a test, never clicked through.
- **[not verified]** — never run end to end by anyone.

Most cases were executed against a running stack. **One real defect was found and fixed this way**, and it is
worth knowing about because a grep could not have caught it: `stores-list.component.html` rendered the store id
as `{{ value.id }}`, where `value` *was* the id object and is now the id string, so the column went blank. The
nesting was split between the `let-value` binding and the interpolation. If you are looking for more of the same
class of bug, look for a template that reaches into a store id rather than printing it.

What remains unverified is narrow and listed honestly: a genuine pre-merge outbox row (W4), the 403 path (T3),
and the `"*"` wildcard (E4, needs a super-admin session).

The most convincing case, if you only run one, is **R5**: a store provisioned from scratch, a category written
into it, read back, and confirmed not to leak — one freshly minted id crossing store-core, pod-registry, billing
and a pod's JPA column with no conversion anywhere.

---

## 00 — Before you start

```bash
sudo ./extra/scripts/configure-domain.sh        # once per machine
lcl start -d             # stop later with `lcl stop`
```

Seller console `http://gateway.com:8000` — `org1-admin` / `admin`. Local seed data only.

Store ids used below (from `http-client.env.json`):

| | id |
|---|---|
| ORG1-STORE1 | `65f023632bc46470c104b76f` |
| ORG1-STORE2 | `65f023632bc46470c104b75f` |

### Looking at the truth underneath

```bash
docker exec cvhome-postgres-1 psql -U postgres -d cvhome -c \
  "select id, name, org_id from tenancy.manager_store;"

# the placement table whose read converter was missing before this change
docker exec cvhome-postgres-1 psql -U postgres -d cvhome -c \
  "select store_id, pod_id from pod_registry.pod_store_placement;"

# a converter that is still missing shows up only here, at read time
grep -i 'ConverterNotFound' .lcl/default/logs/*.log
```

---

## EDGE — The malformed store parameter

Previously a bad `?store=` travelled inwards and blew up as an `IllegalArgumentException` deep inside the
permission evaluator (a 500). It is now refused at the argument resolver.

**E1 [verified]** — malformed store id is a 400, not a 500 or a 403.

```bash
curl -i 'http://spg-507f1f77.gateway.com/catalog/api/v1/category/anything?store=abc&lang=en'
```

Expect **400** with `"code":"COMMON.CONVERSION_FAILED"`, `"detail":"abc is not a valid store id."`,
`"params":{"store":"abc"}` and a `traceId`. No stack trace, no root-cause text in `detail`.

**E2 [verified]** — missing store parameter is a 400.

```bash
curl -i 'http://spg-507f1f77.gateway.com/catalog/api/v1/category/anything?lang=en'
```

Expect **400**, `"code":"COMMON.MISSING_PARAMETER"`, `"params":{"parameter":"store"}`.

**E3 [verified]** — a valid store id still resolves past the resolver. Same URL with
`?store=65f023632bc46470c104b76f&lang=en` returns **404** (no such category) — a 400 here would mean the
validation is rejecting good ids.

**E4 [not verified]** — the `"*"` wildcard still means "every store". A super-admin or `store_core` service
principal carries `store = "*"`, which is deliberately not a valid store id. Sign in as a super admin and list
stores; all four must come back. If this regressed, validation leaked into `StoreMerchantId`'s constructor.

---

## SW — Seller console

The console consumes `ManagerStore.id`, which changed from `{id: "65f0…"}` to `"65f0…"`; `ManagerStoreId` is
gone from `seller-core`. A wrong unwrap here shows as a blank store name, an empty switcher, or
`[object Object]` in a URL.

**S1 [verified]** — sign in as `org1-admin`. The header store switcher lists the org's stores **by name**
(ORG1-STORE1, ORG1-STORE2). An empty dropdown or a blank label means the id/name mapping broke.

**S2 [verified]** — switch stores in the header. The page reloads and the selection persists. The requests the
console makes carry the bare 24-char hex — observed
`spg/catalog/api/v2/private/base-products?…&store=65f023632bc46470c104b76f` flipping to `…b75f` after the
switch, all 200, never `[object Object]`.

**S3 [verified]** — a browser that used the console *before* this change holds a `Selected-ManagerStore-Id`
localStorage entry in the old shape. It self-heals: the shape mismatch fails the id comparison,
`currentSelectedStore()` returns undefined and the first store is selected instead. Reproduced by planting
`{"id":{"id":"65f023632bc46470c104b76f"},"name":"ORG1-STORE1"}` and reloading — the console recovered to
ORG1-STORE1, loaded its products, and **rewrote the key** with `id` as a scalar (`orgId`/`podId` still objects).

**S4 [verified]** — Store management → list. The ID column renders the plain hex.
**This is where the one real defect was found**: it rendered blank because the template did `{{ value.id }}`
(fixed to `{{ value }}`). Worth re-checking on any change to that table.

**S5 [verified]** — Catalogue → Products with a store selected: rows load. Exercises the permission gate
(`CustomPermissionEvaluator` → `PermissionAccessChecker.hasManageAccessOnStore`) on the merged type.

**S6 [verified]** — repeat S5 after switching stores: ORG1-STORE1 shows fashion SKUs (`SKU-NK-RUN-001`,
`SKU-GU-BG-MAR05`), ORG1-STORE2 shows `ELEC-SKU-*`. No overlap.

**S7 [verified]** — Subscription & usage renders the plan catalogue (Free/Basic/Pro with entitlements).
With no subscription row the page also fires `subscription/current` and shows a "couldn't find" toast — that is
a **404, not a 403**, which is the point: billing's `@PreAuthorize` on the merged type passed and the endpoint
reached "not found". `orgId`/`podId`/plan-price ids are deliberately **still** `{id: …}` objects — do not "fix"
those.

---

## T — Tenant isolation

**T1 [verified]** — the same public endpoint, two stores, two different catalogs:

```bash
B=http://spg-507f1f77.gateway.com/catalog/api/v1
curl -s "$B/category-hierarchy?count=20&page=0&store=65f023632bc46470c104b76f"   # Men, Women, Kids, Accessories
curl -s "$B/category-hierarchy?count=20&page=0&store=65f023632bc46470c104b75f"   # Computers, Mobile Phones, Audio, …
```

Any overlap between the two would mean store scoping was lost.

**T2 [verified]** — the storefront renders end to end: `http://org1-store1.spg-507f1f77.gateway.com` shows
navigation, categories and featured products. That is landing-ui → spg → catalog/merchant/content, every hop
carrying the merged store id. **Broken images are expected** (no MinIO locally).

**T3 [not verified]** — a principal without the permission token gets **403**, not 200 and not 500. Call a
`/private/**` catalog endpoint with a session for a store you do not own.

---

## CNV — The JDBC read converters

Spring Data JDBC needs a `String → StoreMerchantId` reading converter per module; a missing one is not a compile
error, it throws `ConverterNotFoundException` the first time that column is read.

**C1 [verified]** — tenancy reads stores. `POST /tenancy/api/v1/store-manager/list` → **200** with the store
rows, and `grep -i ConverterNotFound .lcl/default/logs/tenancy.log` is empty.

**C2 [verified]** — billing reads `store_subscription`, whose `@Id` **is** the store id. Provisioning a store
creates a TRIALING row; `GET /billing/api/v1/subscription/current?store=<new id>` → **200**. The response is
also the clearest single illustration of the new wire format:

```json
{"store":"6a7c775e2479528beff8a4c2","status":"TRIALING",
 "planPriceId":{"id":"6a7c754dcd4d53952a3244f1"}}
```

The store id is a bare string; `planPriceId` is still an object. Both are intended.

**C3 [verified — write path only]** — a placement row is written for a new store
(`pod_registry.pod_store_placement.store_id`), and no `ConverterNotFoundException` appears anywhere.

**Be precise about what this converter fixes.** `PodStorePlacementRepository` declares
`CrudRepository<PodStorePlacementEntity, StoreMerchantId>`, but both of its callers go through custom `@Query`
methods taking `String` (`claim`, `recountCapacity`) — **nothing calls an inherited `CrudRepository` method**.
So the missing reading converter was **latent, not live**: it could not have thrown for any current caller. The
converter makes the declared id type actually usable and stops the first `findById` anyone adds from failing.
Do not go hunting for a bug it was masking; there wasn't one in production use.

**C4 [verified]** — nothing regressed at startup: after a full `lcl start -d`,
`grep -i 'ConverterNotFound\|Cannot deserialize\|MismatchedInput' .lcl/default/logs/*.log` is empty across all ten
services.

---

## WIRE — Serialization and stored payloads

The store id now serializes as `"65f0…"`. Its deserializer still accepts the two older object shapes, because
outbox rows and stored event payloads written by the previous release hold them.

**W1 [unit only]** — `StoreMerchantIdJsonTest` (tenancy-service, 7 cases): serializes to a bare string; reads a
bare string, `{"id":…}`, `{"storeMerchantId":…}` and an explicit null; round-trips a `StoreCreatedEvent`; and
reads a `StoreCreatedEvent` payload stored in the **old** shape.

```bash
./gradlew :store-core:tenancy:tenancy-service:test --tests '*StoreMerchantIdJsonTest*'
```

**W2 [verified]** — the gateway ↔ billing boundary, where the shape change actually crosses a network hop.
`StoreBillingStatusClient.refresh()` polls billing's `blockedStores()`, which now returns `["65f0…"]` instead of
`[{"id":"65f0…"}]`. On a running stack the scheduled refresh completes with no error in
`.lcl/default/logs/gateway.log` — grep for `Could not refresh blocked stores`, which must **not** appear.

**W3 [verified]** — a non-empty blocked list is not just parsed but **honoured**. Creating stores whose
subscription is `PENDING` grew the gateway's set (`Blocked store set changed: 0 -> 1 -> 2 -> 3` in
`gateway.log`), and a catalog write to one was refused `402 BILLING.STORE.SUSPENDED`. Activating a subscription
shrank it (`3 -> 2`) within the refresh interval and the same write then returned 201. This is the case W2 could
not reach: the payload whose JSON shape changed carries real values end to end.

**W4 [not verified]** — **an outbox row written by the previous release is consumed by this one.** The unit test
covers the deserializer; nothing has exercised a real row. Before deploying, either drain the outbox or: on
`develop`, create a store so `StoreCreatedEvent` is written with the old shape; stop the stack before the
handler runs; switch to this branch and restart; confirm the event is handled rather than landing in
`select * from outbox_record where status='FAILED'`.

---

## REG — Regression watchlist

Things this change could plausibly have broken, each with a reason to look.

**R1 [verified]** — the `@OutboxEvent` keys are **SpEL strings**, so the compiler could not check them. All 13
were `"#this.store().id().toString()"` and are now `"#this.store().storeMerchantId()"`. A stale one throws at
publish time, not at build time. `grep -rn '@OutboxEvent' --include='*.java' .` — none may still contain
`.id().toString()`. (`StripeWebhookReceivedEvent` uses `#this.store()` on a plain `String` field and is
correctly untouched.)

**R2 [verified]** — the `@PreAuthorize` target-type literal. 21 annotations said
`hasPermission(#store,'ManagerStoreId',…)` and now say `'StoreMerchantId'`. The evaluator ignores this argument,
so a stale literal would not have failed anything — it is a correctness-of-the-record fix, and
`grep -rn "'ManagerStoreId'" --include='*.java' .` must return nothing.

**R3 [verified]** — **store provisioning end to end**, the only caller of `StoreMerchantId.newId()`. A store
created via `POST /tenancy/api/v1/store-manager/private/store` reached **`SUCCESSFULLY_PROVISIONING`**, and the
one freshly minted id `6a7c7a3e2479528beff8a4c5` came back as a **bare string** (with `orgId`/`podId` still
objects) and landed in all four schemas at once:

| where | column | type |
|---|---|---|
| `tenancy.manager_store` | `id` | `varchar(24)` |
| `pod_registry.pod_store_placement` | `store_id` | `varchar(24)` |
| `billing.store_subscription` | `id` | `varchar(24)` |
| `merchant.merchant_store` | `store_merchant_id` | `varchar(50)` |

One value, two column widths, no conversion anywhere — which is the whole point of the merge.

**The payload matters, and the errors are not store-id related.** Merchant rejects a hand-built body until
`address` is an **object**, the contact fields are `email`/`phone` (not `storeEmailAddress`/`storePhone`), and
`theme` **and** `colorTheme` are present (both not-null in the DDL; `Theme.BASIS` / `ColorTheme.LIGHT` work).
Easier to use the Create Store form, which fills all of it.

**R5 [verified]** — **a write into a brand-new store uses its id correctly.** With the new store selected,
`POST /spg/catalog/api/v1/private/category` → **201**, and the row persisted as
`catalog.category.store_merchant_id = 6a7c7a3e2479528beff8a4c5`. Reading back
`category-hierarchy?store=<new id>` returns exactly `[QA-CAT-STOREID]` while
`category-hierarchy?store=65f023632bc46470c104b76f` still returns `[MEN, WOMEN, KIDS, ACCESSORIES]` — no
leakage in either direction. The console agrees: switch to the new store and Category → List of category shows
one row, id 49. This is the strongest single case in the document: a fresh id minted by store-core, written
through a pod's JPA `@Embedded` column, read back, and tenant-scoped.

**Expect a 402 first.** An org gets **one trial grant**, so the *second* and later stores in an org are created
with a `PENDING` subscription, which billing reports as blocked — the write is refused
`402 BILLING.STORE.SUSPENDED`. That is correct behaviour, not a store-id bug. Give the store an active
subscription (or use the org's first store) before expecting a write to land; the gateway takes up to a minute
to notice.

**Leftovers:** four stores named `QA-*` sit in `tenancy.manager_store` — `QA-PROV-OK`
(`SUCCESSFULLY_PROVISIONING`, holds category 49) and three `QA-STOREID-MERGE*` in `FAILED_PROVISIONING` from
earlier payload attempts, each with placement and billing rows. The outbox has no pending or failed records, so
they are inert. `QA-PROV-OK`'s subscription was switched to TRIALING **by hand in the DB** (with
`plan_id`/`plan_price_id` copied from another row, because a `CHECK` constraint requires a plan for any status
but PENDING) — so it is not a faithful example of a subscribed store. Delete all four for a clean slate.

**R4 [not verified]** — storefront (cua) login. `SocialLoginConfigId` parses a store id out of a composite
OAuth2 registration id; that parsing is unchanged, but it constructs the merged type. Log in at
`http://org1-store1.spg-507f1f77.gateway.com` as `user` / `revo`. Note it only works **through the store host**
(a known local constraint, not a bug).

---

## 99 — Known gaps, expected

- **Broken images everywhere locally** — no MinIO in `docker-compose-lcl.yml`. Pre-existing.
- **`orgId`, `podId`, plan/price/invoice ids still serialize as `{id: "…"}`.** Only the *store* id became a bare
  string. The asymmetry is deliberate and out of scope; `seller-ui` still does `podId.id`.
- **`ManagerOrgId` is untouched**, including its real quirk: its `String` constructor yields a null inner
  `ObjectId` for malformed input while every sibling throws. `SecurityUtils` relies on that leniency for a
  missing `org` claim. Not a defect introduced here.
- **`OrgInvitationRepository` types its id as the store id**, which looks wrong for an invitation. Pre-existing
  and behaviour-preserved; flagged, not fixed.
- **`pod_store_placement` and `billing.store_subscription` are empty on a fresh local stack.** Provision a store
  and both get a row; that is how C2 and C3 were exercised.
- **`E4` (the `"*"` wildcard) is still unverified** — it needs a super-admin session, and `org1-admin` is not
  one.
