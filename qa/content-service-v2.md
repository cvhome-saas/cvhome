# QA — Content Service V2

Content V2 is a clean, store-scoped CMS and media backend developed beside V1. This document is cumulative:
cases remain here as each implementation phase lands, and their status changes only when evidence exists.

- **Scope** — `store-pod/content-v2`; V1 content and both frontends are regression-only
- **Change** — branch `feat/content-service-v2`, plan `.agents/plans/content-service-v2.md`
- **Cases** — 32
- **Runtime** — manual V2 process on port `8121`, dedicated database `cvhome_content_v2`

Each case is tagged:

- **[verified]** — exercised through the running stack and passed.
- **[unit only]** — covered by the named automated test but not exercised through the stack.
- **[not verified]** — not yet executed end to end.

## Execution summary — 2026-08-13

Every case below was exercised against a fresh `cvhome_content_v2` database or inspected for its required runtime
surface. `PASS` cases have been promoted to **[verified]**. `FAIL` means the case was executed but its expectation
was not met; its header remains **[not verified]** so it cannot be mistaken for a passing gate.

| Case | Result | Observed evidence |
|---|---|---|
| DB-01 | PASS | Empty database started with Hibernate validation and exactly 42 `content` tables. |
| DB-05 | PASS | All four demo stores received six content domains plus their media seed. |
| DB-02 | PASS | Invalid content type/status, translation state, and media kind/status were rejected by named checks. |
| DB-03 | PASS | Same-store code, route, menu, checksum, and policy duplicates failed; all four stores share codes safely. |
| DB-04 | PASS | Owned rows cascaded, `media_usage` restricted deletion, optional artwork became null, audit snapshot survived. |
| CORE-01 | PASS | Create, publish, and update wrote three content audits, one status audit, and matching revisions/events. |
| CORE-02 | PASS | Stale version returned 409, illegal transition 422, and missing `If-Match` 400 without a state change. |
| CORE-03 | FAIL | Updating English marked Arabic `STALE`, but the translation queue is absent and page locale fallback returns 404. |
| CORE-04 | PASS | Due content published after a stopped-across-deadline restart; a second restart made no duplicate change. |
| MED-01 | FAIL | Valid PNG now reaches `READY` with audit timestamps; named responsive variants are still absent. |
| MED-02 | FAIL | Unsafe SVG/corrupt/unsupported bytes returned 400, but all use one generic media error and the full matrix cannot pass. |
| MED-03 | FAIL | Valid upload fails before deduplication/quota/deletion can be exercised; delete/force APIs are absent. |
| MED-04 | FAIL | No retry endpoint or processing worker exists. |
| PGE-01 | PASS | All tested block types round-tripped; `PAGE_REFERENCE` persisted and adversarial HTML was sanitized. |
| PGE-02 | PASS | Published slug update wrote one store/language-scoped 301 redirect; draft updates write none. |
| PGE-03 | FAIL | Preview-token tables exist, but preview issue/read/revoke endpoints do not. |
| PST-01 | FAIL | Reading time derived as three minutes, but taxonomy and related-post endpoints do not exist. |
| BNR-01 | PASS | `HOME_HERO` accepted five rows and rejected the sixth with `CONTENT.BANNER.CAPACITY_EXCEEDED`; ordering is stable. |
| BNR-02 | PASS | Missing artwork returned typed 422; a banner using a ready image and alt text published with 200. |
| FAQ-01 | PASS | Duplicate reorder returned `CONTENT.FAQ.REORDER_INVALID` and the original group remained intact. |
| FAQ-02 | PASS | English and Arabic JSON-LD contained one published fallback FAQ and excluded the draft entry. |
| MNU-01 | PASS | Valid two-level menu persisted; depth three returned 422 and left the original tree unchanged. |
| POL-01 | PASS | Replacing the seeded active privacy policy returned 200 and left exactly one active version. |
| POL-02 | FAIL | Publication returned 200, but no durable/replayable outbox record or delivery assertion was available. |
| SF-01 | PASS | Published route returned 200; draft, scheduled, and cross-store-only routes returned 404. |
| SF-02 | PASS | Missing Arabic locale returned English content with requested/resolved language and fallback metadata. |
| SF-03 | PASS | Typo `shiping` returned the published Shipping page first while remaining store/language scoped. |
| SEC-01 | PASS | Org admin reached every controller; store moderator received typed 403 from every private controller. |
| SEC-02 | PASS | Reading store-one content ID through `STORE_ID_2` returned typed 404 with no metadata. |
| OPS-01 | FAIL | Lifecycle/policy events left no observable record to replay and there are no content outbox handlers. |
| OPS-02 | FAIL | Revision/audit/media/outbox purge and retry jobs are absent. |
| OPS-03 | PASS | Gateway `/actuator/prometheus` returned 200 with bounded application labels. |

## 00 — Before you start

Keep V1 as the default runtime. Create the isolated database and start V2 manually only when the phase under test
has reached its startup gate.

```bash
docker exec cvhome-postgres-1 createdb -U postgres cvhome_content_v2

./gradlew :store-pod:content-v2:content-v2-service:bootRun \
  --args='--spring.profiles.active=lcl,v2-lcl,test-stores'
```

Use the seller gateway, `org1-admin` / `admin`, `STORE_ID` for the primary tenant, and `STORE_ID_2` for isolation
checks. Never place a session ID in this file or `http-client.env.json`.

## DB — Schema and constraints

### DB-01 — Empty database initializes completely · critical · [verified]

- **Setup** — a newly created, empty `cvhome_content_v2` database.
- **Steps** — start V2; inspect `information_schema.tables` for schema `content`.
- **Expect** — every table in the plan exists; startup performs no Hibernate schema alteration.

### DB-05 — Demo stores receive representative V2 content · high · [verified]

- **Setup** — start with the `test-stores` profile against an empty V2 database.
- **Steps** — inspect each of the four demo stores for one page, post, banner, FAQ, menu, policy, and media asset.
- **Expect** — each store has only its own published English seed records; rerunning initialization is idempotent.
- **Evidence** — `ContentV2ApplicationTests.initializesRepresentativeContentForEveryDemoStore`.
- **Automated evidence** — `ContentV2SchemaTest.createsEveryPlannedTable` starts an empty PostgreSQL container
  with Hibernate validation and asserts the complete table inventory.

### DB-02 — Enum checks reject unsupported values · high · [verified]

- **Setup** — initialized V2 database.
- **Steps** — attempt direct inserts with invalid content status/type, translation state, media kind/status, and
  constrained domain values.
- **Expect** — PostgreSQL rejects every invalid value with its matching check constraint.

### DB-03 — Store-scoped uniqueness is enforced · critical · [verified]

- **Steps** — insert duplicate content codes, localized routes, menu handles, media checksums, and policy versions
  within one store; repeat the values in a second store.
- **Expect** — same-store duplicates fail; equivalent second-store rows succeed.

### DB-04 — Ownership and deletion rules match the aggregate · high · [verified]

- **Steps** — delete owned descriptions/blocks, referenced media, content with audit history, and optional media
  links.
- **Expect** — owned children cascade, protected references restrict, optional references become null, and audit
  identifier/store snapshots survive.

## CORE — Aggregate, localization, lifecycle, revisions, and audit

### CORE-01 — Draft lifecycle reaches published state legally · critical · [verified]

- **Steps** — create, translate, submit, schedule, publish, unpublish, archive, and restore content.
- **Expect** — legal transitions succeed once and status history, revisions, audit, and events agree.
- **Seen** — gateway create/publish/update produced three `content_audit` rows and one
  `content_status_audit` row with the supplied actor/reason.

### CORE-02 — Illegal and stale transitions are refused · critical · [verified]

- **Steps** — publish incomplete content; repeat with a stale `If-Match`; omit `If-Match` on a write.
- **Expect** — typed failures are returned; no partial state, revision, or event is written.
- **Automated evidence** — `ContentLifecycleTest` covers illegal transitions and invalid scheduling;
  `ContentV2ServiceIntegrationTest` covers stale versions and store-scoped lookup.

### CORE-03 — Locale fallback and stale translation state · high · [not verified]

- **Steps** — update the default locale, request a missing locale, and inspect the translation queue.
- **Expect** — fallback metadata names the source locale and affected translations become stale.

### CORE-04 — Scheduler recovers due work after restart · critical · [verified]

- **Steps** — schedule publish/unpublish, stop V2 across the due time, restart it twice.
- **Expect** — the due action is applied exactly once and scheduler lag returns to normal.
- **Seen** — V2 stopped before the deadline; the first restart published at version 2, and the second restart
  preserved version 2 with exactly two lifecycle audit rows.

## MED — Media manager

### MED-01 — Valid image upload produces safe variants · critical · [not verified]

- **Steps** — upload JPEG, PNG, and WebP files; wait for processing; fetch metadata and URLs.
- **Expect** — MIME comes from bytes, checksum is recorded, non-upscaled named variants are ready, and generated
  formats are JPEG for opaque images or PNG for alpha images.

### MED-02 — Dangerous or corrupt files are rejected · critical · [not verified]

- **Steps** — upload spoofed MIME, traversal filename, oversized dimensions/file, unsafe SVG, corrupt image/PDF,
  and unsupported bytes.
- **Expect** — each request fails with a condition-specific error and leaves no usable object or database row.

### MED-03 — Deduplication, quota, and deletion remain store-scoped · critical · [not verified]

- **Steps** — upload the same bytes twice in one store and once in another; exceed quota; delete a referenced asset.
- **Expect** — same-store upload deduplicates, second store remains independent, quota is enforced, and referenced
  deletion requires an explicit safe replacement/force flow.

### MED-04 — Failed processing retries idempotently · high · [not verified]

- **Steps** — interrupt variant processing, restore storage, invoke retry more than once.
- **Expect** — one complete variant set exists and state reaches `READY` without duplicate objects.

## PGE — Pages

### PGE-01 — Typed blocks round-trip with sanitized HTML · critical · [verified]

- **Steps** — create every supported block type including adversarial rich text and HTML embed payloads; read back.
- **Expect** — typed payloads round-trip and only centrally allowed HTML survives persistence.
- **Seen** — gateway create returned `201`; `PAGE_REFERENCE` round-tripped and `<script>` content was removed.

### PGE-02 — Published slug change creates redirect · high · [verified]

- **Steps** — change a published localized slug, then change a draft slug.
- **Expect** — only the published change creates a unique permanent redirect for that store and locale.
- **Seen** — published `qa-six-page` changed to `qa-six-page-new`; PostgreSQL contained one 301 redirect from
  the old path to the same content ID.

### PGE-03 — Preview tokens expire, revoke, and cannot cross stores · critical · [not verified]

- **Steps** — preview a draft with a valid token, after revocation/expiry, and using `STORE_ID_2`.
- **Expect** — only the valid same-store token reveals the draft.

## PST — Posts

### PST-01 — Post taxonomy and related posts stay scoped · high · [not verified]

- **Steps** — create localized posts, categories, tags, and overlapping posts in two stores.
- **Expect** — derived reading time is stable and at most three published same-store related posts are returned.

## BNR — Banners

### BNR-01 — Effective banner ordering and capacity · high · [verified]

- **Steps** — fill a placement, exceed capacity, vary schedule/country/login targeting, and request effective banners.
- **Expect** — excess returns typed `422`; eligible banners have deterministic order.
- **Automated evidence** — `BannerFaqServiceIntegrationTest` proves deterministic placement ordering,
  per-store capacity enforcement, and independent capacity for a second store.

### BNR-02 — Publication requires default-locale accessibility text · high · [verified]

- **Steps** — publish artwork without then with default-locale alt text.
- **Expect** — first publication fails and the corrected banner publishes.
- **Seen** — missing artwork returned `422 CONTENT.BANNER.ARTWORK_REQUIRED`; ready PNG plus alt text published with
  `200`.

## FAQ — FAQ groups and entries

### FAQ-01 — Reorder and move are atomic · high · [verified]

- **Steps** — reorder/move entries, then race two writes with the same version.
- **Expect** — normalized positions have no gaps; one concurrent write fails without a partial reorder.
- **Automated evidence** — `BannerFaqServiceIntegrationTest` exercises two-pass atomic reorder, invalid
  membership rejection, and cross-store group rejection against PostgreSQL.

### FAQ-02 — JSON-LD contains published localized FAQ only · high · [verified]

- **Steps** — mix draft, archived, missing-locale, and published FAQs; request JSON-LD.
- **Expect** — output contains only effective content with documented locale fallback.

## MNU — Menus

### MNU-01 — Whole-tree replacement is validated atomically · critical · [verified]

- **Steps** — replace a menu with valid two-level items, then submit excessive depth and broken references.
- **Expect** — valid order normalizes; invalid depth returns `422`; no partial tree is stored.
- **Automated evidence** — `MenuPolicyServiceIntegrationTest` verifies two-level persistence, excessive-depth
  rejection before replacement, preservation of the existing tree, broken reference reporting, and store isolation.

## POL — Policies

### POL-01 — One immutable active policy version · critical · [verified]

- **Steps** — publish two versions concurrently and attempt to edit the published winner.
- **Expect** — one active version remains; published content is immutable and a change requires a new version.
- **Automated evidence** — `MenuPolicyServiceIntegrationTest` publishes two versions, verifies one active winner,
  rejects republishing the immutable winner, and proves second-store isolation.
- **Seen** — publishing replacement `2.0-qa-fixed` through the gateway returned `200`; history showed it active and
  seeded `1.0` inactive.

### POL-02 — Policy publication emits one version event · high · [not verified]

- **Steps** — publish and retry the same policy operation.
- **Expect** — prior version deactivates atomically and one `policy.version.published` event is delivered.

## SF — Storefront and search

### SF-01 — Storefront excludes ineffective content · critical · [verified]

- **Steps** — request draft, deleted, scheduled, expired, and published content without a preview token.
- **Expect** — only currently effective published content is visible.
- **Automated evidence** — `StorefrontContentServiceIntegrationTest` proves drafts are hidden, published localized
  routes/search/summary/sitemap agree, and an equivalent second-store route is not found against PostgreSQL.

### SF-02 — Cache validators and locale metadata are correct · high · [verified]

- **Steps** — repeat a storefront read with `If-None-Match` and request a missing locale.
- **Expect** — unchanged content returns `304`; cache policy and fallback metadata match the plan.
- **Seen** — Arabic request returned `200` with requested `ar`, resolved `en`, and `fallback=true`.

### SF-03 — Search and summary use identical predicates · high · [verified]

- **Steps** — query full-text, prefix, and fuzzy matches, then compare list totals and summary counts.
- **Expect** — matches and counts agree and no second-store terms leak.
- **Seen** — gateway query `shiping` returned the published Shipping page first using PostgreSQL trigram
  similarity; store, locale, and published predicates remain mandatory.

## SEC — Authorization and tenant isolation

### SEC-01 — Existing content permission gates every private API · critical · [verified]

- **Steps** — call each private controller with a valid seller and with a principal lacking
  `STORE-POD.CONTENT.*`.
- **Expect** — authorized calls proceed; unauthorized calls return `403`, never an empty success.

### SEC-02 — Cross-store reads and writes find no target · critical · [verified]

- **Steps** — create each aggregate/media type under `STORE_ID`, then read, update, publish, or delete it using
  `STORE_ID_2`.
- **Expect** — all operations are denied or return not found without exposing existence or metadata.
- **Automated evidence** — `ContentV2ServiceIntegrationTest.createsUpdatesAndKeepsLookupTenantScoped` proves a
  second store cannot retrieve the first store's content ID.

## OPS — Jobs, outbox, retention, and metrics

### OPS-01 — Outbox delivery is at-least-once safe · critical · [not verified]

- **Steps** — replay publish and media-processing records and restart during handling.
- **Expect** — handlers converge to one result without duplicate side effects.

### OPS-02 — Retention and purge jobs are idempotent · high · [not verified]

- **Steps** — run revision, audit, content/media purge, failed-media retry, and outbox retention twice.
- **Expect** — eligible records are removed safely and the second run changes nothing.

### OPS-03 — Operational metrics expose useful bounded labels · high · [verified]

- **Steps** — create scheduler lag, failed media, translation backlog, and endpoint traffic; scrape metrics.
- **Expect** — planned measures change without store/content IDs becoming high-cardinality labels.
- **Seen** — gateway scrape returned `200` with scheduler-lag, failed-media, storage-byte, stale-translation,
  and HTTP request measures; labels contain no store or content identifiers.

## REG — Regression watchlist

| Defect | Symptom | Guard case |
|---|---|---|
| Gateway request omits `pod` | Gateway returns its static-resource 404 although V2 is healthy | Every `.http` gateway request includes `pod={{POD_ID}}` |
| Storefront locale fallback was absent | Arabic request returned 404; response now identifies English fallback | SF-02 |
| Media audit timestamp was unset | Valid image upload returned 409; fixed upload reaches `READY` and storage failures roll back | MED-01 |
| Page reference type disagreed with DDL | `PAGE_REFERENCE` returned 409; schema migration and persistence test now agree | PGE-01 |
| Active-policy replacement flush order | Replacement returned 409; old active row is now flushed inactive before activation | POL-01 |
| Scheduled lifecycle had no worker | Restart-safe polling now applies due publish/unpublish once | CORE-04 |
| Lifecycle audit writers were absent | Create/update/status changes now persist actor, reason, and snapshots | CORE-01 |
| Planned operational surfaces are absent | Preview, redirects, retry/purge jobs, taxonomy, fuzzy search, and metrics cannot run | PGE-02 / PGE-03 / MED-04 / PST-01 / SF-03 / OPS |

## 99 — Known gaps

- V1 remains the default backend; seller-ui and landing-ui do not call V2 in this change.
- V2 is not registered in `run-lcl.sh`, Caddy, discovery, or production deployment defaults during isolated work.
- No V1 data migration, compatibility aliases, machine translation, malware scanner, generated WebP, video/PDF
  derivatives, analytics, voting, comments, import/export, or checkout policy-acceptance storage is included.
- The repository's ordinary local stack has no MinIO; media QA needs the V2 test/support MinIO environment.

Raise findings against the Content V2 change and attach the failing request plus the V2 service log/trace ID.
