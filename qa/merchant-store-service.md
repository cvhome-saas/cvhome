# QA — the merchant service (store identity, branding, routing)

`store-pod/merchant` is what a *store* is. After the CMS moved out to `store-pod/content` it holds one thing
and holds it for everybody: the store record — name, address, contact, currency, languages, theme, logo,
banner, slider, social links — plus the **routing map** that turns a hostname into a store. Every other pod
service asks it who a store is before it can answer anything, the storefront's edge asks it before it can serve
a request at all, and the control plane calls it to bring a store into existence.

That makes it small and disproportionately dangerous: a merchant outage is not a missing feature, it is a pod
that cannot identify its tenants.

- **Scope** — merchant · spg routing · tenancy provisioning · console-ui store management · landing-ui ·
  the pod services that cache the store record (catalog, checkout, payment, cua)
- **Change** — the service as it stands on PR #276, branch `feat/mirror-console-ui`. Its content ownership was
  removed in PR #273 (`.agents/plans/split-merchant-content-services.md`); its console screens were rebuilt in
  Module 5 of `.claude/plans/agents-requirments-console-ui-go-live-m-woolly-candy.md`.
- **Cases** — 59 (8 verified, 4 covered by tests only, 47 never run end to end)
- **Still current on PR #282** — merchant itself is untouched by the catalog/inventory split and rewrites; what changed
  is who reads it: catalog reads the store's units of measure, inventory no longer reads it at all. The sibling file is
  [`qa/catalog-and-inventory.md`](catalog-and-inventory.md).
- **Supersedes** — the six-case `MER` follow-up section at the foot of
  [`qa/split-merchant-content-services.md`](split-merchant-content-services.md). Those cases are folded in
  here (STR-03, STR-04, UPD-02, SEC-04, BRD-05, ARC-01); run this file, not that section.

Each case is tagged:

- **[verified]** — driven end to end against a running stack and passed.
- **[unit only]** — covered by a named automated test, nobody drove it through the stack.
- **[not verified]** — never run end to end by anyone. Most of this file. The upload endpoints in particular
  have no runnable request file and no test.

Sections [REG](#reg--regression-watchlist) and [99](#99--known-gaps) are the highest-value reading. This
service has an unusually long list of things that are **missing by design and look like defects** — no way to
remove a logo, a language you can add but not remove, a DNS check that runs in your browser and not on the
platform. Read section 99 before filing anything.

---

## 00 — Before you start

```bash
sudo ./extra/scripts/configure-domain.sh        # once per machine
./extra/scripts/run-lcl.sh                      # stop with SIGTERM, never SIGINT on a backgrounded run
```

**Sign-in.** Console `http://gateway.com:8000` — `org1-admin` / `admin` (org owner), `org1-store1-admin` /
`admin`, `org1-store1-moderator` / `admin` (the read-only case). Storefront
`http://org1-store1.spg-507f1f77.gateway.com`.

### The demo stores

| Store | Id | Theme / colour | Locales | Domains seeded |
|---|---|---|---|---|
| org1-store1 · Riyadh-Fashion-Hub | `65f023632bc46470c104b76f` | BASIS / DEFAULT | ar (default), en | `org1-store1` (SUB), `org1-store1.asrevo.com` (CUSTOM) |
| org1-store2 · USA Electronics Hub | `65f023632bc46470c104b75f` | — | en, fr | `org1-store2`, `org1-store2.asrevo.com` |
| org2-store1 | `65f020632bc46470c104b76f` | — | fr, en | `org2-store1`, `org2-store1.asrevo.com` |
| org2-store2 | `65f023632bc26470c104b75f` | — | ar, fr | `org2-store2`, `org2-store2.asrevo.com` |

org1-store1 and org1-store2 belong to the **same org**; org2-store1 is the one to use whenever a case says
"another org". Only the four `.spg-507f1f77.gateway.com` subdomains are in `/etc/hosts` — the `.asrevo.com`
custom domains are seeded in the routing table but do not resolve locally, which is exactly what makes them
useful for the router cases (you drive those through the API, not the browser).

### Addressing

```
http://gateway.com:8000/spg/merchant/api/v1/...?store=<id>&pod=507f1f77bcf86cd799439011&lang=en   # seller path
http://spg-507f1f77.gateway.com/merchant/api/v1/...?store=<id>&lang=en                            # pod path
```

**The platform gateway route predicates on `pod` as well as `store`** — a `/spg/**` URL carrying only `store`
is a 404 that looks like a missing store. Runnable blocks live in `store-pod/merchant/merchant-service/http/`
(`merchant-store-api.http`, `router-controller.http`, `external-merchant-store-api.http`); note that **none of
the upload or social-link endpoints have a block there**, so those cases are browser or `curl` work.

### Looking at the truth underneath

```bash
docker exec cvhome-postgres-1 psql -U postgres -d cvhome -c \
  "select store_merchant_id, store_name, org, theme, color_theme, language_code, currency_id, country_id,
          store_logo, store_banner from merchant.merchant_store;"

... "select * from merchant.store_domains order by store_merchant_id;"
... "select * from merchant.merchant_language order by store_merchant_id;"
... "select * from merchant.merchant_slider_images order by store_merchant_id, priority;"
... "select * from merchant.social_links order by store_merchant_id;"
```

Logs: `build/lcl-logs/merchant.log`. The router logs every edge lookup (`header lookup:` and `tls ask:`), which
is the fastest way to see what hostname Caddy actually asked about.

### The MinIO trap, before you test any image

`docker-compose-lcl.yml` runs MinIO **without a volume**, and the seeds reference file *names* that were never
uploaded. So on a fresh stack `logo.jpeg`, `banner.jpeg` and `slide-1..5.jpeg` are rows in the database with no
objects behind them: the store record is correct and the storefront images are broken. That is the local stack.
Upload your own image first if you need to see one render, and expect it to vanish on the next
`docker compose down`.

---

## STR — Reading a store

Three reads, deliberately different:

| Endpoint | Auth | Who calls it |
|---|---|---|
| `GET /api/v1/store?store=` | **none** | every other pod service, and landing-ui |
| `GET /api/v1/store/{code}?store=` | none | the compatibility shape — path and query must agree |
| `GET /api/v1/private/store?store=` | `STORE-POD.MERCHANT.READ` | the console |

The public read being public is not an oversight — the storefront has to render for a signed-out shopper, and
the pod's other services call it with a service token they would otherwise need for nothing else. What it
returns is therefore the thing to check.

### STR-01 — The canonical public read answers · critical · [verified]

- **Steps** — `GET /spg/merchant/api/v1/store?store=<org1-store1>&pod=…&lang=en`.
- **Expect** — 200 with the seeded Riyadh store: name, theme, colour theme, currency, default language,
  supported languages, address, social links, slider images, logo and banner paths.

### STR-02 — The public read exposes nothing private · critical · [not verified]

Nobody has audited this response against "what may a signed-out shopper see". Do it once, properly.

- **Steps** — signed out, in a private window, fetch the public read for org1-store1 and read every field.
- **Expect** — storefront-renderable facts only. Flag anything that looks like an internal identifier, an
  operational flag or a contact detail the merchant did not intend to publish — `org`, `lineage`,
  `continueshoppingurl` and the audit block are the ones to look at first. Record what you find either way;
  this case exists to produce a decision, not to pass.

### STR-03 — The compatibility path cannot select another tenant · critical · [unit only]

`MerchantStoreApiTest.compatibilityReadRejectsDifferentTenantContext`. This is the case that stops
`/store/{someone-elses-id}?store=<mine>` being a tenancy hole.

- **Steps** — `GET /store/{{STORE_ID_2}}?store={{STORE_ID}}` (block three of `merchant-store-api.http`).
- **Expect** — **400** ProblemDetail with `MERCHANT.STORE.CONTEXT_MISMATCH`, and **no** store read at all —
  neither the path one nor the query one.

### STR-04 — Canonical and compatibility reads agree · high · [unit only]

`MerchantStoreApiTest.compatibilityReadUsesResolvedTenantContext`.

- **Steps** — run the first two blocks of `merchant-store-api.http` and diff the bodies.
- **Expect** — identical JSON. landing-ui uses the canonical query-scoped form; the path form exists only for
  callers that have not moved.

### STR-05 — A store that does not exist is a typed 404 · high · [not verified]

- **Steps** — read `store=missing-store` on the public, compatibility and private reads.
- **Expect** — **404** `MERCHANT.STORE.NOT_FOUND` with a `traceId` on all three — never an empty 200, never a
  500, never a stack trace.

### STR-06 — The private read needs the read permission · critical · [not verified]

- **Steps** — call `/private/store` with no session; then as `org1-store1-moderator`.
- **Expect** — refused with no session; **200 for the moderator** — reading a store's settings is a read right.
  If the moderator is refused, the console's whole store-management page is dark for them, which is a defect in
  the other direction.

### STR-07 — Supported languages is the store's own list · [not verified]

- **Steps** — `GET /store/languages?store=<org1-store1>`.
- **Expect** — `ar` and `en` only — the languages this store has turned **on**, not the platform's five. (That
  is precisely why it cannot drive the console's "add a language" control — see
  [Known gaps](#99--known-gaps).)

---

## UPD — Updating a store

`PUT /private/store` takes a whole store body. Three things about it a tester must know before judging what
they see:

- **The tenant comes from `?store=`, never from the body.** The facade overwrites the body's `id` and `org`
  from the loaded entity.
- **Social links, slider images and domains in the body are ignored** by this endpoint — it copies them off
  the loaded entity before mapping. They have their own endpoints.
- **A supported language can be added but not removed.** The populator unions rather than replaces. Unticking
  one returns 200 and changes nothing. This is a known backend defect, stated in the console's own UI.

### UPD-01 — An ordinary edit saves and comes back · critical · [not verified]

- **Steps** — change the store name, phone, city and postal code; save; reload; check the row.
- **Expect** — all four persist, `date_modified` moves, and the storefront picks up the new name.

### UPD-02 — The body cannot select a tenant · critical · [not verified]

The highest-value case in this section.

- **Setup** — read both org1-store1 and org1-store2 first, so you have a before state for each.
- **Steps** — authenticated for org1-store1, `PUT /private/store?store=<org1-store1>` with a body whose `id`
  and `org` are **org1-store2's**.
- **Expect** — only org1-store1 changes; org1-store2 is byte-identical afterwards. A write that follows the
  body is a stop-ship defect.

### UPD-03 — Required fields are refused as validation, not as a 500 · high · [not verified]

The entity carries `@NotEmpty` on fields whose columns are nullable, so Hibernate refuses them at *persist*
time — below the layer that renders field errors. That is how a missing city became a 500 once.

- **Steps** — `PUT` with `name`, `email`, `phone`, `city` and `postalCode` blank in turn.
- **Expect** — **400** with field errors naming each one. Any **500** with `COMMON.INTERNAL_ERROR` is the
  regression; note which field caused it.

### UPD-04 — Adding a supported language works; removing it does not · high · [not verified]

Expected to fail in the removal direction. Record what you see; do not file it.

- **Steps** — tick a third language, save, reload. Then untick it, save, reload.
- **Expect** — the addition sticks. The removal returns **200 and is silently ignored**, and the console says
  so under the field before you spend the save. If the removal *does* apply, the backend has been fixed and
  this document is stale — say so.

### UPD-05 — Omitting `supportedLanguages` entirely · [not verified]

- **Steps** — `PUT` a body with the field absent (not empty — absent).
- **Expect** — a 400. It is currently an unguarded dereference, so a **500** here is the known state and worth
  recording rather than filing.

### UPD-06 — Changing the theme reaches the storefront · high · [not verified]

- **Steps** — change `theme` and `colorTheme` on org1-store1; save; hard-reload the storefront.
- **Expect** — the storefront renders the new theme and palette. It arrives through the edge's
  `Theme` / `Color-Theme` headers (see DOM-04), so allow for Caddy's lookup and the storefront's own cache
  before calling it a miss.

### UPD-07 — Deleting a store · high · [not verified]

- **Steps** — delete a scratch store you created; then attempt to delete the platform default
  (`org1-store1`); then delete a store that has orders against it.
- **Expect** — the scratch store goes; the default is refused with **422**
  `MERCHANT.STORE.DEFAULT_NOT_REMOVABLE`; the one with orders is refused with a **409** from the integrity
  handler, not a 500. Deleting a store is irreversible and there is no other way to take a storefront offline
  — see [Known gaps](#99--known-gaps).

---

## BRD — Logo, banner and slider images

Three upload endpoints and one list-replace endpoint. The order matters: the file goes to object storage
**first**, and only a successful upload writes the filename onto the store.

- **Slider ordering and deletion are both expressed by sending the list you want.** There is no delete-slide
  endpoint and no reorder endpoint: drop an entry to delete it, renumber `priority` to reorder.
- **A logo or banner cannot be removed at all** once set, only replaced. No endpoint exists.

### BRD-01 — Uploading a logo replaces the old one · critical · [not verified]

- **Steps** — upload a square PNG on `/store-management/branding`; reload; upload a different one.
- **Expect** — `merchant_store.store_logo` holds the new filename, the object is in MinIO under
  `files/<storeId>/LOGO/<filename>`, and the console preview and the storefront header both show it.

### BRD-02 — The console refuses the wrong shape before it uploads · high · [verified]

`accept=` is advisory in every browser, so type, weight and pixel dimensions are checked client-side and the
refusal quotes the actual file.

- **Steps** — drop a 1920×480 image on the **logo** well.
- **Expect** — refused by name and shape ("that image is 1920 × 480, which is the wrong shape for this slot")
  and **no request is made** — confirm in the network panel. Repeat on the banner well (4:1) and the slider
  add-zone (2.5:1).

### BRD-03 — An upload says it finished · [verified]

- **Expect** — the well spins while the request is in flight and holds a tick afterwards, both floored to a
  visible duration. A local upload round-trips faster than the eye, so without the floor a completed upload and
  a missed click look identical.

### BRD-04 — Slider images reorder and delete by list replacement · high · [not verified]

- **Steps** — add two slides; reorder them; remove one; reload; open the storefront home.
- **Expect** — `merchant_slider_images` matches the console exactly, priorities are contiguous from 0, and the
  storefront carousel is in the same order. A duplicate priority is a unique-constraint violation — it must
  surface as a 4xx, not a 500.

### BRD-05 — A failed upload leaves the old image intact · high · [unit only]

Storage is ordered before persistence precisely so this holds. Nobody has injected the failure live.

- **Steps** — force the storage call to fail (stop MinIO, or point the bucket at a nonexistent name) and upload
  a logo.
- **Expect** — a named storage error (`MERCHANT.UPLOAD.UNREADABLE` for an unreadable body, or the CMS storage
  error for a refused write), and `store_logo` **unchanged** in the database. The store must never end up
  naming a file that was never stored.

### BRD-06 — There is no way to remove a logo, and the console says so · [not verified]

Expected behaviour, not a defect.

- **Expect** — no Remove button on the logo or banner; the card reads "uploading again replaces the current
  image. The platform has no way to remove one once set." If a Remove button exists, it is a 404 waiting to
  happen — file that.

---

## SOC — Social links

`PUT /private/store/social-links` replaces the set. The body is store-shaped even though only `socialLinks` is
read — that is the declared type, not a convenience.

### SOC-01 — The seeded links load and save · critical · [verified]

- **Steps** — open `/store-management/social`; save without changing anything; reload.
- **Expect** — the four seeded providers come back unchanged. (This section once sat under its loading veil
  forever on a single missing translation key, with four of five rows unlabelled and an idle network — so a
  hung veil here is a known shape of failure, not a slow request.)

### SOC-02 — A link must belong to the provider whose row it is in · high · [verified]

Nothing server-side checks that a `SocialLink`'s provider and URL agree, so the console is the only place it
can be enforced — and a TikTok URL under a Facebook mark sends shoppers somewhere else.

- **Steps** — put a TikTok URL in the Facebook field; then a bare `facebook.com` with no profile after it.
- **Expect** — both refused by name, with the expected shape spelled out. `twitter.com` and `fb.com` are
  accepted deliberately (those companies still serve them), and subdomains count.

### SOC-03 — Removing a link removes it · [not verified]

- **Steps** — clear one provider's field, save, reload, check the storefront footer.
- **Expect** — the row is gone from `social_links` and the icon is gone from the storefront. A cleared field
  that comes back on reload is the same union-instead-of-replace shape as UPD-04 — worth checking for.

---

## DOM — Domains and edge routing

This is the part of merchant nothing else can cover for. Two **public, unauthenticated** endpoints are called
by Caddy on every storefront request:

- `GET /router/public/lookup-by-domain?domain=` → the headers that identify the store
  (`Store-Id`, `Theme`, `Color-Theme`, `Default-Language`, `Supported-Languages`)
- `GET /router/public/ask-for-tls?domain=` → 200 or 400, deciding whether the edge issues a certificate

A `SUB_DOMAIN` row stores only its **label** (`org1-store1`) and is matched by concatenating the pod's domain;
a `CUSTOM_DOMAIN` row is matched exactly. `store_domains.domain` is globally unique.

### DOM-01 — A storefront hostname resolves to its store · critical · [not verified]

- **Steps** — `GET /merchant/api/v1/router/public/lookup-by-domain?domain=org1-store1.spg-507f1f77.gateway.com`
  through spg; then the seeded custom domain `org1-store1.asrevo.com`.
- **Expect** — both return the same header map naming org1-store1, its theme, its colour theme and its
  languages. Watch `merchant.log` for the `header lookup:` line to confirm what hostname Caddy actually sent.

### DOM-02 — An unknown hostname resolves to nothing, harmlessly · critical · [not verified]

- **Steps** — look up `not-a-store.example.com`.
- **Expect** — an **empty map with a 200**, and the storefront's store-not-found page rather than an error. A
  500 here would be served to every stray request that reaches the edge.

### DOM-03 — TLS is only offered for hostnames we actually serve · critical · [not verified]

This endpoint is what stops the edge being made to request certificates for domains that are not ours.

- **Steps** — `ask-for-tls` for a seeded custom domain, for the pod's own domain, and for
  `www.google.com`.
- **Expect** — 200, 200, **400**. Anything that answers 200 for an unrelated hostname is a stop-ship defect.

### DOM-04 — The store's identity headers reach the storefront · high · [not verified]

- **Steps** — open org1-store1's storefront and inspect the rendered page; then change the store's colour theme
  and reload.
- **Expect** — the theme and palette follow the store record. A store with no `Theme` header falls back rather
  than failing — check by looking up a store whose theme column is null, if one exists.

### DOM-05 — Allocating a custom domain · high · [not verified]

- **Steps** — `POST /router/private/allocate?domain=shop.example.com&store=<org1-store1>`; then `GET
  /router/private/allocates`; then look it up through `lookup-by-domain`.
- **Expect** — the domain is listed as `CUSTOM_DOMAIN` and resolves to the store immediately. **The server
  performs no DNS check** — it records whatever hostname it is given (see 99).

### DOM-06 — The same domain cannot belong to two stores · critical · [not verified]

`store_domains.domain` is globally unique, so this is a database constraint reaching the API.

- **Steps** — allocate `shop.example.com` to org1-store1, then allocate the same domain to org2-store1.
- **Expect** — refused. A **409** would be right; a **500** from the raw constraint violation is the likely
  finding and worth filing with the exact response. What must not happen is the domain silently moving stores —
  that would hand one org's traffic to another.

### DOM-07 — Removing a domain removes only custom ones · high · [not verified]

Expected asymmetry: the remove path constructs a `CUSTOM_DOMAIN` record, and equality includes the type.

- **Steps** — remove the custom domain (works); then attempt to remove the `SUB_DOMAIN` label.
- **Expect** — the custom one goes and stops resolving. The subdomain removal returns 200 and **changes
  nothing** — a store cannot be detached from its own default address through this API. Record it; it is the
  current design, not a bug you found.

### DOM-08 — The router's private endpoints are actually gated · critical · [not verified]

Worth its own case: the security filter chain matches `/api/*/private/**`, which does **not** match
`/api/v1/router/private/**` — one path segment too deep. Those endpoints are therefore `permitAll` at the
filter and rely entirely on `@PreAuthorize`.

- **Steps** — call `/router/private/allocates`, `/router/private/allocate` and `/router/private/remove` with
  **no token at all**, then with a token for a different store.
- **Expect** — **403** every time, and nothing written. A 200 — or an allocation that succeeds — is a
  stop-ship defect, and the fix is the filter chain, not just the annotation.

### DOM-09 — A domain lookup for a missing store is a typed 404 · [not verified]

- **Steps** — the fourth block of `router-controller.http` (`store=missing-store`).
- **Expect** — 404 `MERCHANT.STORE.NOT_FOUND`.

---

## CRT — Creating a store

A store is created by the control plane, not by the merchant directly: tenancy writes its own row, then calls
`POST /private/store` on the pod **through the outbox**. Creation is therefore asynchronous, and the merchant
sees "provisioning" then "ready" or "failed".

Because there is no later moment at which the caller can be told what was wrong, **tenancy re-validates
merchant's required fields up front** — deliberately duplicating part of merchant's model. The required set is
name, email, phone, theme, colorTheme, currency, defaultLanguage, supportedLanguages, and address.{country,
city, postalCode}.

### CRT-01 — Creating a store from the console works end to end · critical · [not verified]

- **Steps** — create a store through `/create-store`; watch `merchant.log` and `control.outbox_record`.
- **Expect** — the row appears in `merchant.merchant_store` within seconds, with its `SUB_DOMAIN` allocated
  from the store name, and the storefront answers on its hostname once `/etc/hosts` has the entry.

### CRT-02 — An incomplete body is refused synchronously, with fields · critical · [not verified]

The whole point of the duplicated validation. It has already been wrong once, in a way that produced a real
FAILED store row.

- **Steps** — post a create body missing `city`, then missing `postalCode`, then missing `phone`.
- **Expect** — **400 with field errors** from tenancy, before any store row exists. A 200 followed minutes
  later by a failed provisioning — or a **500** reading `COMMON.INTERNAL_ERROR` — is the regression.

### CRT-03 — A duplicate store id is a conflict · high · [not verified]

- **Steps** — call `POST /private/store` directly with an id that already exists.
- **Expect** — **409** `MERCHANT.STORE.DUPLICATE`, not a 400 and not a silent overwrite.

### CRT-04 — A pod that refuses and a pod that never answers are recorded differently · high · [not verified]

The client names these failures separately because the caller acts on the difference: a refusal is a verdict
and is recorded as failed provisioning; a timeout decided nothing and must be left for the outbox to retry.

- **Steps** — (a) stop `merchant`, create a store; (b) make merchant refuse (post a body it will reject).
- **Expect** — (a) the outbox row stays pending and retries, and the store completes when merchant returns;
  (b) the store is recorded as failed with the pod's own code. A timeout recorded as a rejection is the
  regression this contract exists to prevent.

### CRT-05 — Reaching merchant directly with no default language · [not verified]

Known unguarded dereference; not reachable through the console any more.

- **Expect** — a 500 rather than a 400. Record it against the known gap rather than filing it.

---

## DEP — Everything that depends on merchant

Catalog, checkout, payment and cua all resolve the store record through `ExternalMerchantStoreService`, each
behind its own `@Cacheable("STORE")`. The cache is what makes a merchant blip survivable — and what makes a
store edit take a while to show up elsewhere.

### DEP-01 — A store edit reaches the other services · high · [not verified]

- **Steps** — change the store's currency (or default language); then load a product page, a cart and an order
  in the console.
- **Expect** — the new value appears in prices and formatting **eventually**. If it does not appear at all,
  find out whether the `STORE` cache has any eviction — a permanently stale store record across four services
  is worth knowing about either way.

### DEP-02 — A merchant outage degrades rather than 500s · critical · [not verified]

The single most valuable case in this file, because merchant is the one service everything else asks first.

- **Steps** — with a warm stack, stop `merchant`. Then: open the storefront home; open a product; add to cart;
  open the console's dashboard, catalogue and orders.
- **Expect** — whatever is served from cache keeps working. What must **not** happen is an unhandled 500 or a
  blank page under a 200 — landing-ui's store service exists specifically to stop a merchant-pod outage
  rendering a blank page. Record precisely which screens survive and which do not; nobody has mapped this.

### DEP-03 — A cold start with merchant down · high · [not verified]

- **Steps** — stop merchant, restart `catalog` (so its cache is empty), then load a product.
- **Expect** — a defined failure the caller can act on — a typed remote-unavailable error, not a null
  dereference. This is the case the cache cannot cover for.

### DEP-04 — The storefront edge with merchant down · critical · [not verified]

- **Steps** — stop merchant and open a storefront hostname.
- **Expect** — Caddy's `lookup-by-domain` fails, so the request cannot be identified. Confirm the shopper gets
  a store-not-found or error page rather than a hanging request or another store's content. Then restart
  merchant and confirm it recovers **without** clearing anything by hand.

---

## SEC — Permissions and tenant isolation

### SEC-01 — A moderator can read the store and cannot change it · critical · [not verified]

- **Steps** — as `org1-store1-moderator`, open `/store-management` and try to save details, upload a logo,
  change social links and allocate a domain.
- **Expect** — the page and every section **load**; every write is **403**. Test both halves — a missing
  permission entry fails silently in the "can read" direction too, which looks like an empty page rather than
  an error.

### SEC-02 — Another org cannot read or write this store · critical · [not verified]

Use **org2-store1**; org1-store2 shares an admin and proves less.

- **Steps** — authenticated for org 2, call `/private/store`, `PUT /private/store`, the upload endpoints and
  `/router/private/allocates` against org1-store1's id.
- **Expect** — refused on every one, and org1-store1 is unchanged afterwards. Note which status comes back;
  a 403 and a 404 are both defensible, an authorized answer is not.

### SEC-03 — Deleting another store is refused · high · [not verified]

Block seven of `merchant-store-api.http`.

- **Steps** — as org1-store1's admin, `DELETE /private/store?store=<org1-store2>`.
- **Expect** — **403**, and org1-store2 still exists. This is the most destructive endpoint in the service.

### SEC-04 — A service principal reads through the permission evaluator · critical · [not verified]

- **Steps** — read a store through tenancy as its store-core service principal; then repeat with an
  unauthorized principal.
- **Expect** — the service principal succeeds through `STORE-POD.MERCHANT.READ`; the unauthorized caller gets
  403. A service-to-service path that bypasses the evaluator entirely is worth reporting even if it "works".

### SEC-05 — Store creation is gated on the org, not the store · high · [not verified]

`POST /private/store` is authorized on `#store.org` — the only endpoint here that does not key on a store id,
because the store does not exist yet.

- **Steps** — attempt a create with an `org` the caller does not administer.
- **Expect** — 403. A create that trusts the body's `org` would let any authenticated seller plant a store in
  another org.

### SEC-06 — Nothing sensitive in the log · [not verified]

- **Steps** — after exercising uploads and domain allocation, grep `build/lcl-logs/merchant.log` for
  `minioadmin`, `Authorization`, `secret`.
- **Expect** — no matches. The router logs hostnames deliberately; that is fine.

---

## UI — The console's store management

Eight sections behind one rail: details, branding, slider, social, domain, home, payments, social login. (The
last two belong to payment-service and cua; they are listed here only because they share the page.)

### UI-01 — The page follows the store switcher · critical · [verified]

The bug worth the whole QA pass: switching stores left the page showing the **previous** store's settings — its
domains, its landing copy, its gateway secrets — while the request context had already moved on, so the next
save would have written one store's values onto the other.

- **Steps** — open store management for org1-store1, note a distinctive value, switch to org1-store2 with the
  rail, and look again. Then switch back and save something.
- **Expect** — every section reloads for the new store. Nothing from the previous one survives on screen for
  even a moment. This is the worst kind of wrong because it looks fine.

### UI-02 — Fields the platform does not record are honest about it · high · [verified]

Six designed fields (legal name, tax number, slug, category, timezone, short description) and both visibility
switches have no counterpart anywhere on the platform.

- **Expect** — they render **disabled** inside a "Not recorded by the platform" block with the reason beside
  them, and they are disabled in the form service so they can never reach a request body. The header carries
  **no** published/unpublished badge — a badge that always reads "Published" is an assertion, not a fact.

### UI-03 — The home section writes the landing snippet · high · [not verified]

This card was repointed at content-service's snippets API when the old box endpoints were deleted. Same screen,
different backend.

- **Steps** — edit the headline, body and search snippet; save; reload; check the storefront home.
- **Expect** — it persists and renders. A 404 in the network panel means the repoint is wrong. Arabic copy
  typed while the console is in English must render right-to-left (`dir="auto"`), not as reversed nonsense.

### UI-04 — The custom-domain field refuses a domain that does not point here · high · [not verified]

The server records whatever hostname it is given, so the client check is the only check there is.

- **Steps** — type a domain with no CNAME at all; then one pointing elsewhere; then block `dns.google` (an
  offline network or a blocker) and type a valid one.
- **Expect** — the first two are refused with what the resolver found and Save stays out of reach; the third
  **warns and allows**, because a resolver the browser could not reach says nothing about the operator's DNS.
  An allocated domain shows a badge only once a re-check has actually run — never a "not checked" badge under a
  domain the console itself required a passing lookup for.

### UI-05 — The address section says where the store lives, or says it cannot · [not verified]

The storefront hostname is assembled client-side from two calls on two tiers; either can be refused.

- **Expect** — the default subdomain row and the CNAME target render for a healthy store. When either leg is
  refused, the section reads "Address not available" and hides the DNS record block — never a half-built
  hostname, which would send an operator to their registrar with a value that can never resolve.

### UI-06 — Arabic, right to left, across all eight sections · high · [not verified]

- **Steps** — switch the console to Arabic and walk every section, including the branding wells, the slider
  rows, the domain panel and the settings rail folded to its icon strip.
- **Expect** — no raw keys on screen, mirrored layout, and accessible names surviving the rail's fold. A
  section stuck under its loading veil with an idle network is the known missing-key failure — `npm run lint`
  now has `lint:i18n-missing` to catch it before it ships.

---

## SF — The storefront

### SF-01 — The store's identity renders · critical · [not verified]

- **Steps** — open org1-store1's storefront.
- **Expect** — store name, logo, banner, slider and social links come from the merchant record. Broken images
  on a fresh stack are the MinIO gap; the *names* must still be right in the API response.

### SF-02 — Arabic default language is honoured · high · [not verified]

org1-store1's default language is `ar`.

- **Steps** — open the storefront root with no locale in the path.
- **Expect** — Arabic, right-to-left, with the Arabic font actually applied — a Latin fallback face silently
  drops Arabic glyphs, so check the rendering, not just the direction.

### SF-03 — A store with a custom domain serves the same content · [not verified]

- **Steps** — add a hosts entry for a custom domain you allocated in DOM-05, then open it.
- **Expect** — the same storefront as the subdomain, identified by the same `Store-Id` header.

---

## ARC — What the split left behind

merchant used to own the CMS. PR #273 moved it out; Module 13 deleted the compatibility alias.

### ARC-01 — The merchant schema holds only merchant data · high · [unit only]

- **Steps** — start the merchant integration test against a fresh Postgres container and inspect the schema;
  or `\dt merchant.*` on a running stack.
- **Expect** — exactly five tables: `merchant_store`, `merchant_language`, `merchant_slider_images`,
  `social_links`, `store_domains`. **No** `content`, `content_description` or `sm_sequencer`, and no foreign
  key pointing at content.

### ARC-02 — `/merchant/api/v1/content/**` is gone · critical · [not verified]

- **Steps** — call the old compatibility paths directly on 8120 and through spg; then click through the
  storefront and the console watching the network panel.
- **Expect** — **404**, and **nothing on any screen still calls them**. The second half matters more.

### ARC-03 — Merchant's seed files carry no content fragments · high · [verified]

The first full-stack run of the split failed merchant startup on an orphaned `font-size:0.9em` fragment — a
semicolon inside seeded HTML had split a statement.

- **Steps** — drop the merchant schema and start the stack fresh.
- **Expect** — clean startup. Each `init-sql/stores/*/01-store.sql` ends after merchant-specific data.

---

## REG — Regression watchlist

Every row was a real defect. Several were invisible from the screen.

| What broke | How it looked | How to catch it again |
|---|---|---|
| **Store management showed the previous store's settings** | Switching stores on the rail left the page holding one store's domains, landing copy and gateway secrets while the request context had moved on — the next save would have written them onto the other store. The resource had no params, so it loaded once and never again. | UI-01 |
| **A missing translation key took a whole section down** | `/store-management/social` sat under its loading veil forever with four of five rows unlabelled, an idle network and a clean console. Transloco throws on a missing key, and a throw during template evaluation aborts the change-detection pass. | UI-06, and `npm run lint` |
| **A created store failed provisioning with no reason** | Tenancy accepted a four-field create body and forwarded the rest untyped; the pod refused it off the outbox on `@NotNull`s and NOT NULL columns. The merchant saw "provisioning", then "failed", and never learned which field was wrong. | CRT-02 |
| **A nullable column that is still required** | `city` and `postalCode` are nullable in the DDL and `@NotEmpty` on the entity, so Hibernate refused them below the layer that renders field errors — a **500**, not a 400. QA found it as a real FAILED store row. | CRT-02, UPD-03 |
| **Seeded HTML broke merchant startup** | A semicolon inside a seeded content fragment split the SQL statement and left `font-size:0.9em` as its own command. | ARC-03 |
| **Requests through the platform gateway 404'd** | The `/spg/**` route predicates on `pod` as well as `store`. | Any `.http` block — they all carry `pod={{POD_ID}}` |
| **An allocated domain claimed to be unchecked** | Every custom domain row showed a "not checked" badge and "no lookup yet" — the console doubting a rule it had itself enforced before allowing the domain. | UI-04 |
| **A social link pointed at the wrong site** | A TikTok URL in the Facebook row renders under a Facebook mark and sends shoppers elsewhere; nothing server-side checks the two agree. | SOC-02 |
| **An upload that ran and a click that missed looked identical** | A local upload round-trips faster than the eye registers a spinner. | BRD-03 |
| **Arabic copy rendered left-to-right** | The home section is written in languages the console is not running in; without `dir="auto"` the text read as nonsense. | UI-03 |
| **Buttons that had always 404'd** | seller-core's `removeStoreLogo`/`removeStoreBanner` posted to paths missing the `/spg/merchant/api` prefix and mapped by no controller. | BRD-06 — the Remove button must not exist |

---

## 99 — Known gaps

Behaviour that is expected today. Please don't spend time raising these — but do shout if you see something
*beyond* what is described. This service has more of these than any other, because it is the oldest model on
the platform and the console was built against it honestly rather than pretending.

**A supported language can be added but never removed.** The populator unions the incoming list onto the
entity's existing set instead of replacing it. Unticking a language is accepted with a 200 and silently
ignored. The fix is one `clear()`; until then the console states it under the field.

**A logo or banner can be uploaded but never removed.** There is no delete endpoint and `PUT /private/store`
cannot clear one, so the image is permanent from first upload — only replaceable.

**A store has no published or maintenance state.** Nothing records either, and there is no endpoint to change
one. The only way to take a storefront offline today is to delete the store.

**Six designed store fields do not exist anywhere.** `legalName`, `taxNumber`, `slug`, `category`, `timezone`
and `shortDescription` have no column, no DTO field and no endpoint. The invoice prints the trading name
because that is the only name there is.

**Slider images carry no schedule, link or file metadata.** `ReadableSliderImage` is `(priority, name, url)`.
A carousel slide cannot link anywhere, which makes it decoration.

**DNS verification runs in the operator's browser, not on the platform.** Nothing server-side ever confirms a
CNAME: `POST /router/private/allocate` records whatever hostname it is given and answers `void`. The console
queries Google's public DNS-over-HTTPS resolver and uses that to gate the field — one resolver, one machine,
one network. `ask-for-tls` restates whether a domain is *allocated*, not whether a certificate was issued.

**Nothing answers what address a store is served at.** The storefront hostname is assembled client-side from
two calls on two different tiers, and the pod lookup is refused outright for a suspended or archived store — so
a store can be in a state where the console cannot say where it lives.

**A subdomain cannot be removed through the router API.** The remove path constructs a `CUSTOM_DOMAIN` record
and equality includes the type, so only custom domains can be detached.

**The router's private endpoints are not covered by the security filter chain.** `/api/*/private/**` matches
one segment too few for `/api/v1/router/private/**`; those endpoints depend entirely on `@PreAuthorize`. DOM-08
exists to prove that is enough — it is not a theoretical concern.

**No reference lists for countries, currencies or storefront languages.** The console derives countries and
currencies from ISO registries and `Intl`, and keeps the five storefront languages as a constant, because
`GET /store/languages` answers with the languages a store has already turned on — which cannot drive the
control that turns them on.

**Calling merchant directly with no `defaultLanguage` is a 500.** An unguarded dereference in the populator.
Not reachable through the console any more, and not fixed either.

**The runnable request file is incomplete.** `merchant-store-api.http` has no block for any upload endpoint,
for social links, or for domain allocate/remove — so those paths have neither a test nor a runnable request.
That is why so much of BRD, SOC and DOM is `[not verified]`.

---

Raise anything unexpected against PR #276. Include the store id, the time, and the matching lines from
`build/lcl-logs/merchant.log` — for a routing problem the `header lookup:` / `tls ask:` lines say exactly what
hostname the edge asked about, which is usually the whole answer. For a console defect attach the browser
console and the failing request: a 403 is a permission problem, a 400 with `MERCHANT.STORE.CONTEXT_MISMATCH` is
a path/query disagreement, and a 404 through `/spg/**` is usually a missing `pod` parameter.
