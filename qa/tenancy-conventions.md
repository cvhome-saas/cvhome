# QA — tenancy conventions

Phase 11 of `.claude/plans/tenancy-and-pod-registry-split.md`, and the last of them. Naming, the typed create
request, the URL collision, and a `http/` directory that can actually be run.

## What changed and why you are testing it

- **`*Controller` → `*Api`** for all seven classes. No URL changes; the repo's convention is `*Api` and tenancy
  was the last holdout.
- **`CreateStoreRequest` replaces `Map<Object, Object>`**, which was threaded through six signatures and
  serialized into the outbox. It was read with `request.get("name").toString()` — an NPE for anyone omitting a
  name — and no signature said what a caller should send.
- **Signup moved to `api/v1/signup`**, off `api/v1/user-account` which it shared with `UserAccountApi`. Two
  controllers on one base path is legal and misleading: everything on the other one needs a session and a
  store-scoped permission, while signup is the one endpoint anyone on the internet may call.
- **Nine `http/` files replace five stale ones.** The old files sat in the module root, addressed
  `localhost:8020` and `localhost:8083` directly, and two called endpoints that had been deleted.
- **`dnsjava` dropped** from `tenancy-commons` — the audit flagged it as unused and it still was.

**Only two fields of the create request are typed**, deliberately. Tenancy needs the name (it owns the row and
the uniqueness constraint) and the preferred pod (it asks the registry for placement). Everything else —
address, email, phone, currency, units, the whole of merchant's store model — is collected by
`@JsonAnySetter` and forwarded untouched. Duplicating merchant's model here would mean two definitions of a
store to keep in step forever, and the wire shape stays flat so the console's create form did not have to
change.

> **Migration note.** `StoreCreatedEvent` now carries `CreateStoreRequest` where it carried a map. Outbox
> records written by the old release will not deserialize into the new shape. Drain the outbox before deploying,
> or accept that in-flight store creations fail and are re-driven by the reaper.

## Setup

```bash
docker compose -f docker-compose-lcl.yml up -d
# uaa, billing, pod-registry, tenancy, gateway, merchant
```

`merchant` matters here: it is the only way to see provisioning actually finish.

---

## Case 1 — the gate: every endpoint has a runnable request, none aimed at a service port

```bash
grep -hE "^(GET|POST|PUT|DELETE) " store-core/tenancy/tenancy-service/http/*.http | grep -vc SELLER_UI_URL
```

**Expect:** `0` — every request addressed through `{{SELLER_UI_URL}}`. And every `*Api` class should appear in
some `http/` file.

## Case 2 — the gate: seller-ui builds

```bash
cd store-core/seller-ui && npm run build
```

## Case 3 — the signup URL moved

| Request | Expect |
|---|---|
| `POST /tenancy/api/v1/signup/public/create` | reaches the endpoint (not 404) |
| `POST /tenancy/api/v1/user-account/public/create` | **404** — the collision is gone |

## Case 4 — creating a store with the typed request

`POST /store-manager/private/store` with a body the console would send.

**Expect:** 200, `status: "ACTIVE"`, and — with a *complete* payload — `SUCCESSFULLY_PROVISIONING` a few
seconds later, plus the pod's `capacity_stores` rising.

**A complete payload matters.** Merchant requires `theme` ∈ {BASIS, FOOD, …}, `colorTheme` ∈ {OCEAN, SKY, …},
`defaultLanguage`, and an address. An incomplete one is refused and correctly recorded `FAILED_PROVISIONING`.

---

## Results

Run 2026-08-12, branch `refactor/tenancy-conventions`, against all six services.

| Case | Result | Evidence |
|---|---|---|
| 1 — http files | **PASS** | 40 requests, all via `{{SELLER_UI_URL}}`; all 11 `*Api` classes covered |
| 2 — seller-ui build | **PASS** | `dist/seller-ui` produced |
| 3 — signup moved | **PASS** | new path reached (409 on an empty body), old path **404** |
| 4 — typed create | **PASS** | 200 + `SUCCESSFULLY_PROVISIONING` + `capacity_stores` 4 |

Automated: 27 tenancy tests, full `build -x test -x check`, module build and checkstyle all clean.

### A correction to an earlier QA note

**`qa/tenancy-robustness.md` says store creation "still fails at the pod". That was wrong, and the fault was in
my test data, not the code.** Merchant was rejecting `theme: "DEFAULT"` and `colorTheme: "BLUE"` — neither is a
valid enum constant — and later NPE'ing on a missing `defaultLanguage`. With a payload of the shape the console
actually sends, a store provisions end to end. This run is the first time that has been demonstrated:

| Payload | Outcome |
|---|---|
| `colorTheme: BLUE` | refused — not a `ColorTheme` |
| `theme: DEFAULT` | refused — not a `Theme` |
| valid enums, no language | refused — merchant NPE on `defaultLanguage` |
| complete | **`SUCCESSFULLY_PROVISIONING`** |

The three refusals are themselves a result worth having: each was classified as *refused* rather than
*unreachable*, recorded once, and not retried forever — which is exactly what phase 9's error split was for.

### Found while testing

**`PersistableMerchantStorePopulator.applyLanguages` throws a `NullPointerException`** when
`defaultLanguage` is absent, so a merchant-side validation problem surfaces as a 500 rather than a 400 naming
the field. It is in `merchant-core`, unrelated to this phase, and it is the reason an incomplete payload is
harder to diagnose than it should be.

## Still open across the whole plan

1. **`isOrgAdmin` is still unfixed** — the largest open item. Every pod service still lets an org admin manage
   any store on the platform. It has its own PR waiting, and `hasReadAccessOnStore` never checking
   `isSuperAdmin` belongs with it.
2. **No seller-ui for phase 10's features.** Store suspend/archive/delete, org profile, members and invitations
   all have endpoints and none have screens. Invitations especially need one, since the token is shown once.
3. **`WebClientsUtils`' clone fix has no regression test**, because `store-commons/autoconfigure` has no test
   source set at all.
4. **Retention jobs** for `tenancy_audit`, `pod_audit`, `pod_health_check` and the outbox tables — all grow
   unbounded, repo-wide.
5. **The `Manager*` type names** (`ManagerStoreId`, `ManagerOrgId`) survive the rename by design; anyone
   "finishing" it must sweep every `hasPermission(...,'ManagerStoreId',...)` string or it 403s silently.
