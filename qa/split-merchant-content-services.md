# QA — split merchant and content services

CMS ownership moved from merchant-service to content-service while `/merchant/api/v1/content/**` remains a
compatibility alias. Existing databases must be recreated; this change provides no data migration.

- **Scope** — merchant · content · SPG routing · seller-ui · landing-ui
- **Change** — PR #273, branch `feat/split-merchant-content-services`, plan
  `.agents/plans/split-merchant-content-services.md`
- **Run** — 2026-08-13, fresh `run-lcl.sh` stack with `test-stores`

Tags: **[verified]** was exercised end to end; **[unit only]** was covered by automated checks but not the
live stack; **[not verified]** remains for a human tester.

## 00 — Startup

### START-01 — Both services start from fresh schemas · critical · [verified]

- **Setup** — no prior local stack; run `./extra/scripts/run-lcl.sh` and wait for all readiness checks.
- **Steps** — confirm merchant on 8120, content on 8121, SPG on 8000, seller-ui on 8010 and landing-ui on 8110.
- **Expect** — every service is ready. Content seed SQL initializes content-service; merchant seed SQL contains
  no content fragments.
- **Observed** — passed after removing four orphaned HTML/CSS fragments from merchant store seed files.

### START-02 — Content owns the content tables · [unit only]

- **Steps** — run the content schema/context assertion and merchant context test.
- **Expect** — `content.sm_sequencer`, `content.content`, and `content.content_description` belong to content;
  merchant has no content tables or content foreign keys.
- **Observed** — automated assertions pass. Live `psql` inspection was unavailable because the local database
  had exhausted its connection slots while the full stack was running.

## ROUTE — Gateway and compatibility

### ROUTE-01 — Public page routes are equivalent · critical · [verified]

- **Steps** — through `gateway.com:8000`, request canonical `/spg/content/api/v1/content/pages` and legacy
  `/spg/merchant/api/v1/content/pages` with `store`, `pod`, `lang`, and pagination.
- **Expect** — both return 200 and byte-identical JSON through Caddy.
- **Observed** — 200/200; payload comparison passed. The runnable `.http` file was corrected to include the
  required `pod={{POD_ID}}` route selector.

### ROUTE-02 — Public box routes are equivalent · critical · [verified]

- **Steps** — repeat ROUTE-01 for `/content/boxes`.
- **Expect** — both return 200 and equivalent payloads.
- **Observed** — 200/200; payload comparison passed.

### ROUTE-03 — Merchant routes remain on merchant-service · critical · [verified]

- **Steps** — request `/spg/merchant/api/v1/store/<store>` through the platform gateway.
- **Expect** — 200 with the seeded Riyadh merchant store.
- **Observed** — passed.

### ROUTE-04 — Typed missing-content response survives routing · [verified]

- **Steps** — request a nonexistent page through `/spg/content/**`.
- **Expect** — 404 Problem Detail with `CONTENT.NOT_FOUND`, store and code parameters, and trace id.
- **Observed** — passed.

### ROUTE-05 — Private canonical and compatibility routes match · [not verified]

- **Setup** — obtain a seller session and place it in `http-client.private.env.json`.
- **Steps** — run canonical and legacy private list/file blocks in `content-api.http`.
- **Expect** — equivalent statuses and payloads; mutations happen once.

## SEC — Permission and tenancy

### SEC-01 — Missing content permission is denied · critical · [verified]

- **Setup** — sign in as `org1-store1-moderator` / `admin`.
- **Steps** — directly open `/pages/content/pages/list` even though CMS navigation is hidden.
- **Expect** — private content requests return 403 and no content is exposed.
- **Observed** — UI displayed “You don't have permission to do that”; Problem Detail was
  `COMMON.ACCESS_DENIED` with status 403. No rows rendered.

### SEC-02 — Public reads are store-scoped · critical · [verified]

- **Steps** — request `about-us` for demo store 1 and demo store 2 using otherwise identical gateway URLs.
- **Expect** — both resolve only their own seeded rows; their payloads differ.
- **Observed** — 200/200 and payloads differed.

### SEC-03 — Cross-store mutation is refused · critical · [not verified]

- **Setup** — create uniquely coded content in store 1 and authenticate for store 2.
- **Steps** — fetch, update and delete the store-1 id while scoped to store 2.
- **Expect** — no visibility or mutation; store-1 content remains unchanged.

## UI — Seller and storefront

### UI-01 — Seller CMS reads from content-service · critical · [verified]

- **Setup** — sign in as `org1-store1-admin` / `admin`.
- **Steps** — open **Content management → Content Pages**.
- **Expect** — six seeded pages render through the new canonical content route without console errors.
- **Observed** — six pages rendered (`about-us`, `contact-us`, `terms`, `privacy`, `location`, `faq`); no
  console errors on the list page.

### UI-02 — Seller page, box and file CRUD · critical · [not verified]

- **Steps** — create, edit, view and delete one page and box; upload and delete one image.
- **Expect** — operations persist through `/spg/content/**`.
- **Observed** — create-page form and content editor loaded. Submission stayed client-side with “Please, fill
  required fields” despite all visibly required fields being populated, so no mutation was claimed as verified.

### UI-03 — Storefront CMS page renders · critical · [verified]

- **Steps** — open the store-1 storefront and follow **About Riyadh Fashion Hub**.
- **Expect** — `/en/content/about-us` renders seeded title/body from content-service.
- **Observed** — passed. Home navigation also contained the seeded content links.

### UI-04 — Storefront box rendering · [verified]

- **Steps** — open the store-1 home page.
- **Expect** — seeded announcement box renders.
- **Observed** — “Eid Collection Has Arrived!” announcement rendered.

### UI-05 — Local media upload/public URL · [not verified]

- **Steps** — upload an image, list it, and open its public URL.
- **Expect** — upload/list succeed. Record the known local MinIO/public-media gap separately from routing.

## REG — Regression watchlist

### REG-01 — Semicolons inside seeded HTML do not leave merchant SQL fragments · [verified]

The first full-stack run failed merchant startup on an orphaned `font-size:0.9em` fragment. The four merchant
store seed files now end after merchant-specific data; a fresh full-stack startup passed.

### REG-02 — Platform SPG requests include the pod selector · [verified]

Requests with only `store` returned 404 because the platform gateway route also predicates on `pod`. All
blocks in `content-api.http` now send `pod={{POD_ID}}`; canonical and compatibility requests then returned 200.

## 99 — Known gaps

- Full CRUD, upload/delete, private alias equivalence, and cross-store mutation remain **[not verified]**.
- Landing-ui dev mode logs an existing Next.js `legacyBehavior` deprecation; CMS rendering still succeeded.
- Live schema inspection hit PostgreSQL's local `too many clients` limit; automated schema assertions remain
  the evidence for table ownership in this run.

## MER — Merchant modernization follow-up

### MER-01 — Canonical and compatibility store reads agree · critical · [not verified]

- **Steps** — run the first two requests in `merchant-store-api.http` through the platform gateway.
- **Expect** — both return the same store JSON; landing-ui uses the canonical query-scoped request.

### MER-02 — Path/query tenant mismatch is rejected · critical · [unit only]

- **Steps** — run the mismatch request in `merchant-store-api.http`.
- **Expect** — 400 Problem Detail with `MERCHANT.STORE.CONTEXT_MISMATCH`; no other store is read.
- **Observed** — covered by the merchant module build and typed exception contract; gateway execution remains.

### MER-03 — Updates cannot select a tenant from the request body · critical · [not verified]

- **Setup** — authenticate for store 1 and submit an update whose body id is store 2.
- **Steps** — read both stores before and after the request.
- **Expect** — only the query-scoped store can be addressed; store 2 is unchanged.

### MER-04 — Store-core read uses the permission evaluator · critical · [not verified]

- **Steps** — read a store through tenancy as its store-core service principal, then repeat without an authorized
  principal.
- **Expect** — the service principal succeeds through `STORE-POD.MERCHANT.READ`; the unauthorized caller gets 403.

### MER-05 — Branding metadata follows successful storage · high · [unit only]

- **Steps** — force asset storage to fail during a logo, banner, or slider upload.
- **Expect** — the named storage error is returned and the prior database metadata remains unchanged.
- **Observed** — implementation orders storage before persistence; live MinIO failure injection remains.

### MER-06 — Merchant schema contains only merchant data · high · [unit only]

- **Steps** — start the merchant integration test against a fresh PostgreSQL container and inspect its schema.
- **Expect** — store, language, slider, social-link, and domain tables exist; `sm_sequencer` does not. Enum and
  uniqueness constraints reject invalid or duplicate data.
- **Observed** — merchant context/integration test starts successfully against the revised DDL.
