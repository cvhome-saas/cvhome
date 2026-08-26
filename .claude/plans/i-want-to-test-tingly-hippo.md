# E2E test: super-admin registers a pod → org3 → store org3-store1 → full store configuration (console-ui)

## Context

Goal: prove the full tenant-onboarding path works end to end, driven **entirely through console-ui**
(the new console on the current `feat/mirror-console-ui` branch):

1. super-admin creates an organization (org3) with its org-admin user,
2. super-admin registers a new (dummy) pod **owned by org3** (`orgId` = org3 → a PRIVATE/dedicated pod),
3. the org admin creates store `org3-store1` **on the newly registered pod**,
4. the store gets configured: categories, products, content pages, staff users,
5. the storefront answers on `org3-store1.spg-507f1f77.gateway.com`.

### What already works with no change: gateway pickup

The gateway needs **no code change and no restart**: `PodClient` (store-core-gateway) implements
`RouteDefinitionRepository` and re-reads the pod list from pod-registry every minute
(`cvhome.gateway.route-refresh-rate`, default PT1M). A pod registered through console-ui becomes a
live `/spg/**?pod=<id>` route within one refresh period — this is verified explicitly in Phase 3/verification.

### The one blocker found during exploration (needs a small code fix)

Pods now live in **pod-registry** (DB, seeded from config by `PodSeedInitializer`), and placement,
gateway routing and tenancy's read path all consult the registry. But **store provisioning does not**:

- `store-core/tenancy/tenancy-service/.../manager/service/StorePodClientFactory.java:34` resolves the
  pod via `serviceDomainProperties.getPodByPodId(podId)` — **YAML config only**
  (`store-core-lcl-config.yml` lists just `507f1f77...`).
- A pod registered through the API/UI therefore throws `IllegalArgumentException: Pod not found for id`
  inside the outbox handler → the store sticks at `IN_PROGRESS_PROVISIONING` and the outbox retries forever.

This is a genuine product bug (any pod not baked into YAML can never host a store), not just a local gap.
The user chose "nothing manual, all through console-ui", so the plan fixes it properly rather than
hand-editing config.

**Fix (Phase 0):** tenancy already wires `CachingPodDirectory` (`tenancy/.../config/ClientsConfig.java`,
bean around line 100) whose `find(PodId)` consults pod-registry and falls back to the config seed.
Change `StorePodClientFactory` to resolve through `CachingPodDirectory` instead of
`ServiceDomainProperties`. Same fail-behaviour (throw when truly unknown).

### Facts the plan relies on (verified)

- Super admin seeded in `data-common.sql`: **`super-admin` / `admin`** (same bcrypt hash as `org1-admin`).
- `POST /api/v1/pod` (pod-registry, `PodApi.java:116`) — super-admin only; fields `name` (unique),
  `endpoint{endpoint,type}`, optional `orgId` (blank ⇒ PUBLIC pod). `domain` is **not persisted**.
- Org creation: `POST /api/v1/org-manager/create` (super-admin) takes just a `PersistableUser`; it creates
  the org **and** its ORG_ADMIN user in uaa in one transaction. No Stripe/subscription prerequisite
  (billing quota only blocks when the org already has pending stores; trial subscription arrives via outbox).
  Password rule: 6–12 chars, upper+lower+digit (e.g. `Passw0rd`).
- Store creation: `POST /api/v1/store-manager/private/store` — org comes from the **caller's identity**,
  so the store must be created while logged in as **org3's admin**, not super-admin. The store **name is the
  storefront host prefix** (`StoreFacadeImpl.java:106` registers `name` as a SUB_DOMAIN) → name it exactly
  **`org3-store1`**. Pod is a placement hint dropdown.
- The physical local pod identifies itself as `spg-507f1f77.gateway.com` (`store-pod-lcl-config.yml`), and
  `docker-compose-lcl.yml` hard-codes the Caddy lookup URLs to it — so **the storefront host is always
  `<store-name>.spg-507f1f77.gateway.com`**, no matter which logical pod the store is placed on. The dummy
  pod's own host only serves the provisioning/seller path (tenancy/gateway → Caddy → merchant).
- Products require a **manufacturer (brand)** — create one brand first. Images are a separate tab and MinIO
  is not in the local compose, so **skip images** (broken image URLs are a known local gap).
- Storefront shopper login `user`/`revo` is seeded only for the demo stores; org3-store1 gets an anonymous
  smoke check only (user chose staff users, not shoppers).

## Decisions (from the user)

- Drive everything through **console-ui** as super-admin (and org3-admin for store steps).
- Pod registration must work for real, with **no manual config edits** → Phase 0 code fix.
- "Some users" = **staff users** (store admin / moderator) via the console, not shoppers.

## Phase 0 — code fix: provisioning resolves pods from the registry

File: `store-core/tenancy/tenancy-service/src/main/java/com/asrevo/cvhome/tenancy/manager/service/StorePodClientFactory.java`

- Inject `CachingPodDirectory` (already a bean in `ClientsConfig`) instead of `ServiceDomainProperties`.
- `createMerchantStorePodClient`: `podDirectory.find(podId).orElseThrow(...)` (keep the exception message).
- Note: the `clients` map already caches per `PodId`; a newly registered pod is resolved on first use, which
  is the case we need. No cache-invalidation work in this pass.
- Match surrounding style; run `./gradlew :store-core:tenancy:tenancy-service:test` (or at least compile)
  before bringing the stack up.

## Phase 1 — hosts entries (user runs the script as root)

Edit `extra/scripts/configure-domain.sh` `run-append()` — add:

```bash
append "127.0.0.1 spg-org3.gateway.com" "$file"                      # dummy pod endpoint host
append "127.0.0.1 org3-store1.spg-507f1f77.gateway.com" "$file"      # storefront host (name + physical pod domain)
```

Then **the user runs**: `sudo ./extra/scripts/configure-domain.sh` (script is idempotent).
Suggest they type `! sudo ./extra/scripts/configure-domain.sh` in the session so the output lands here.

## Phase 2 — bring the stack up

- Check first: `./extra/scripts/run-lcl.sh --list`, `lsof -i :8000 -i :8011 -i :8110`.
- If not running: `./extra/scripts/run-lcl.sh` via Bash `run_in_background`; watch `build/lcl-logs/*.log`
  until uaa, gateway, tenancy, billing, pod-registry, merchant, catalog, content, console-ui, landing-ui are up.
- If already running: rebuild + restart **tenancy only** so Phase 0 is live
  (per console-ui CLAUDE.md, use the supervisor's restart; otherwise
  `./gradlew :store-core:tenancy:tenancy-service:bootRun --args='--spring.profiles.active=lcl,test-stores'`).
- Stop later with `pkill -TERM -f "bash ./extra/scripts/run-lcl.sh"` — never SIGINT.

## Phase 3 — drive the flow in the browser (claude-in-chrome skill, new tab, record with gif_creator)

Console entry: `http://console-ui.gateway.com:8000/` (behind store-core-gateway; if the gateway routes it
differently, fall back to `http://console-ui.gateway.com:8011/`). Login flows via uaa.

1. **Super-admin session** (`super-admin` / `admin`):
   - **Organizations** feature → create org3's admin first: e.g. `org3-admin@mail.com` / `Passw0rd`,
     first/last name. Verify the org row appears; note its org id — call it `<ORG3_ID>`.
   - **Pods** feature → create pod: name `org3-pod`, endpoint `http://spg-org3.gateway.com`, type
     `EXTERNAL`, **owner org = `<ORG3_ID>`** (non-null orgId ⇒ `PodVisibility.PRIVATE` — only org3's
     stores may be placed there, which the store-creation step then proves). Verify it appears in the
     fleet table with org3 as owner; note its generated id — call it `<POD_ID>`.
   - **Gateway pickup check**: within ~1 minute (route-refresh interval) the gateway must route
     `/spg/**?pod=<POD_ID>` — probe with an authenticated request through `gateway.com:8000`
     (e.g. the merchant store endpoint) and expect a pod answer, not a gateway 404/503. No restart.
2. **Org3-admin session** (fresh window/incognito or logout): log in as the new org admin.
   - **Create store**: name **`org3-store1`** (exact — it's the host prefix), pod = `org3-pod`
     (`<POD_ID>`), theme + color theme, currency, default language `en`, supported `en` (+`ar` if we want
     the RTL check), email/phone/address filled.
   - Wait ~a few seconds (async provisioning), then confirm the store shows as provisioned in the UI.
3. **Configure the store** (as org3-admin, store `org3-store1`):
   - Brand/manufacturer: one brand (e.g. `Acme`).
   - Categories: 2–3 (e.g. `Clothing`, `Accessories`, child `T-Shirts`).
   - Products: 3–4 across the categories — sku, name, friendly URL, price, quantity, brand; **no images**.
   - Content pages: 1–2 pages (e.g. `about-us`, `shipping`) via the content feature.
   - Staff users: one STORE_ADMIN and one STORE_MODERATOR for org3-store1 via user management
     (e.g. `org3-store1-admin@mail.com`, `org3-store1-moderator@mail.com` / `Passw0rd`).
4. **Storefront smoke check**: open `http://org3-store1.spg-507f1f77.gateway.com` — page renders with the
   chosen theme, categories/products visible (images broken — expected), content page reachable.

## Verification / evidence

- Provisioning: `select id, name, org_id, pod_id, provisioning_state from tenancy.manager_store where name='org3-store1';`
  → `SUCCESSFULLY_PROVISIONING`, `pod_id = <POD_ID>`.
- No stuck events: `select * from outbox_record where status='FAILED';` → empty.
- Pod row survives restart of pod-registry (PodSeedInitializer never deletes operator rows) — optional.
- Gateway route: seller calls to the store go out as `/spg/**?store=<id>&pod=<POD_ID>` and succeed
  (network panel; route table refreshes within 1 min of pod creation).
- Storefront headers: merchant lookup maps `org3-store1.spg-507f1f77.gateway.com` → `Store-Id` (visible as
  a working storefront; on failure check `build/lcl-logs/merchant.log`).
- Permission spot-check: the STORE_MODERATOR login sees the store but not org-level screens.
- Private-pod scoping: as `org1-admin`, the store-creation pod dropdown / pod list must **not** offer
  `org3-pod` (org-scoped `listPods`); as org3-admin it must.
- Console clean / no failed requests during the flow; failures joined by `traceId` → `build/lcl-logs/*.log`.

## Deliverables

- Phase 0 fix committed on the current branch (small, self-contained; its own commit).
- `configure-domain.sh` host entries committed.
- QA findings written to `qa/org3-pod-store-e2e.md` (repo QA-doc format: scope, tags
  `[verified]`/`[not verified]`, evidence, known-gaps — MinIO images, storefront host pinned to the
  physical pod's domain, `domain` not persisted by pod-registry).
- Flow recording gif(s) from the browser session for review.

## Known limitations to state up front (not failures)

- All images broken locally (no MinIO).
- The dummy pod is logical only: it routes to the same physical local stack, and the storefront host stays
  under `spg-507f1f77.gateway.com`.
- Pod `domain` field is accepted by the API but never persisted (`PodEntity.toPod()` returns null) —
  recorded as a finding, not fixed in this pass.
