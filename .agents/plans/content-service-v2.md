# Content Service V2 — Phased Implementation Plan

## Summary

Implement `store-pod/content-v2/` as a clean replacement backend in one PR, using internal phase
commits/checkpoints. Every phase must compile, pass its scoped tests and checkstyle, and update the same cumulative
QA document.

V2 preserves the existing model style:

- `Content` remains the aggregate root in `content.content`.
- `ContentDescription` remains its localized child in `content.content_description`.
- Entities use `Long` IDs, `SM_SEQUENCER`, `@TableGenerator`, `SalesManagerEntity`, `AuditSection`,
  `StoreMerchantId`, and `LanguageCode`.
- Domain-specific tables extend `Content`; they do not replace it with a generic `content_item` model.

V1 remains the default local runtime. V2 is started manually on port `8121` against a dedicated
`cvhome_content_v2` database. `run-lcl.sh`, seller-ui, and landing-ui remain unchanged until the later
client-migration project.

## Implementation Status

- **Phase 0 — complete (2026-08-13):** feature branch created; V1 content tests/checkstyle and the repository
  build are clean; cumulative QA contract created at `qa/content-service-v2.md`.
- **Phase 1 — complete (2026-08-13):** four V2 modules, isolated profile, security/S3/merchant/billing/outbox
  wiring, complete schema, PostgreSQL/MinIO Testcontainers, and schema metadata assertions implemented.
- **Phases 2–4 — in progress (2026-08-13):** the shared aggregate/lifecycle/revision API, streamed validated media
  upload foundation, typed sanitized page blocks, and post foundation are implemented. Their remaining gate items
  stay open; this is not recorded as phase completion until every listed behavior and test is present.
- **Current verification (2026-08-13):** repository-wide `checkstyleMain checkstyleTest`, repository build without
  tests/checks, the complete repository test suite, scoped V2 tests, grep gates, controller/`.http` coverage, and
  `git diff --check` pass. End-to-end gateway QA remains not verified.
- **Phase 5 — in progress (2026-08-13):** typed banner and FAQ models, store-scoped persistence, locked placement
  capacity, artwork/media validation, publication accessibility validation, FAQ groups/references, atomic two-pass
  reorder, APIs, gateway requests, and PostgreSQL integration coverage are implemented. Effective storefront
  resolution, JSON-LD, full update/move flows, and end-to-end QA remain open.
- **Phase 6 — in progress (2026-08-13):** two-level whole-tree menus, normalized ordering, localized labels,
  visibility/login flags, broken content-reference reporting, versioned policies, display locations, atomic active
  version replacement, immutable publication, outbox event registration, APIs, gateway requests, and PostgreSQL
  integration coverage are implemented. Storefront reads and a committed outbox-delivery assertion remain open.
- **Phase 7 — in progress (2026-08-13):** tenant-scoped published page/post routes, effective banners, FAQ JSON-LD,
  published menus, active policies, bounded search, summary counts, sitemap output, ETags/conditional responses,
  public shared-cache headers, gateway requests, and PostgreSQL storefront boundary coverage are implemented.
  Preview-token reads, fuzzy ranking/search documents, bulk multi-status operations, retention/purge/retry jobs,
  and operational metrics remain open.
- **Phase 8 — in progress (2026-08-13):** idempotent representative seeds now cover every V2 domain for all four
  demo stores, the `test-stores` profile loads them, and an integration test verifies their tenant/domain coverage.
  All four automated final gates are complete. All 32 QA cases were executed on a fresh isolated database: 22 pass
  and 10 expose implementation gaps recorded in `qa/content-service-v2.md`. Gateway authorization and tenant
  isolation pass with an org admin and a store moderator; Phase 8 cannot complete until the failed cases are fixed.

## Database Design

Phase 1 creates the complete schema up front. `schema.sql` remains authoritative and v2 overrides Hibernate to
`ddl-auto: validate`.

### Shared content tables

- `sm_sequencer`: sequence values for every generated `Long` ID.
- `content`: `content_id`, audit columns, `store_merchant_id`, stable `code`, `content_type`, `status`,
  `publish_at`, `unpublish_at`, `deleted_at`, and optimistic `version`.
  - Unique `(store_merchant_id, code)`.
  - Status/type checks and store/status/scheduling indexes.
  - Remove legacy-only `visible`, `link_to_menu`, `content_position`, and `product_group`; their behavior moves to
    lifecycle or typed tables.
- `content_description`: existing description fields plus `store_merchant_id`, `content_type`,
  `translation_state`, localized `sef_url`, canonical URL, OG media, `no_index`, and audit columns.
  - Unique `(content_id, language_code)`.
  - Unique localized route `(store_merchant_id, content_type, language_code, sef_url)`.
  - Composite FK ensures denormalized store/type values match the parent `Content`.
- `content_revision`: content ID, content version, immutable JSON snapshot, author, timestamp; unique
  `(content_id, version)`.
- `content_status_audit`: content ID snapshot, store, from/to status, actor, reason, timestamp.
- `content_audit`: content ID snapshot, store, action, actor, IP, user agent, before/after summary, timestamp.
- `content_search_document`: content, store, locale, normalized searchable text and PostgreSQL `tsvector`.
  - Use the `simple` text-search configuration for multilingual content.
  - Add `pg_trgm` for fuzzy title/slug matching; every query must include the store predicate.
- Audit records survive hard content deletion by retaining identifier/store snapshots and using nullable or no
  destructive FK.

### Pages and posts

- `content_page`: `content_id` PK/FK, template, `show_in_sitemap`, optional parent page.
- `page_block`: generated ID, page content ID, block type, position, and a bounded JSON payload serialized from a
  sealed block DTO—not arbitrary metadata.
- `page_block_description`: block ID, language, validated localized payload; unique `(block_id, language_code)`.
- `content_redirect`: store, locale, old path, destination content, HTTP status; unique old path per store/locale.
- `content_preview_token`: hashed token, content ID, expiry, revocation timestamp.
- `content_post`: content ID, hero media, author snapshot, reading minutes, featured.
- `content_post_description`: description ID PK/FK and localized excerpt.
- `post_category`, `post_category_description`, `post_tag`, `content_post_category`, and `content_post_tag`, all
  store-scoped.

### Banners, FAQ, menus, and policies

- `content_banner`: content ID, placement, position, target kind/value, theme values, logged-in targeting.
- `content_banner_description`: description ID, headline, subhead, CTA label.
- `banner_artwork`: content, locale, desktop/mobile media IDs, alt text.
- `banner_country`: content and country code.
- `faq_group` and `faq_group_description`: store-scoped group and localized name.
- `content_faq`: content ID, group, position.
- `faq_reference`: FAQ content, reference kind and opaque reference value.
- `content_menu`: content ID, store, handle; unique `(store, handle)`.
- `menu_item`: menu, optional parent, position, target, new-tab, visible, and login-required flags.
- `menu_item_description`: menu item and localized label.
- `content_policy`: content ID, store, policy type/version, effective date, acceptance flag, jurisdiction, active
  flag.
  - Unique `(store, policy_type, policy_version)`.
  - Partial unique `(store, policy_type)` where active.
- `policy_display_location`: policy and enum location.

### Media and infrastructure

- `media_folder`: ID, store, parent folder, name, audit/version fields; maximum one parent level and unique sibling
  name.
- `media_asset`: ID, store, folder, original/normalized filename, detected MIME, kind, bytes, checksum, dimensions
  or page count, storage key, processing status, deletion timestamp, audit/version.
  - Unique `(store, checksum)` for deduplication.
- `media_asset_description`: asset, locale, alt text, title, caption.
- `media_variant`: asset, variant name, format, dimensions, bytes, storage key; unique `(asset, variant_name)`.
- `media_tag` and `media_asset_tag`: normalized store-scoped tags.
- `media_usage`: asset, content ID, usage type, field/block identifier; unique logical reference.
- Standard JPA outbox tables and indexes: `outbox_record`, `outbox_instance`, `outbox_partition`.

All persisted enums receive matching DDL `CHECK` constraints. Internal FKs use `CASCADE` for true owned children,
`SET NULL` for optional media references, and `RESTRICT` where deletion must be handled explicitly.

## Implementation Phases

### Phase 0 — Preflight and contract freeze

1. Create a fresh feature branch from `origin/develop`; never implement on `develop`.
2. Preserve unrelated untracked `content.md` and `req.md`; commit the plan only when intentionally included.
3. Record the current v1 baseline:
   - Content checkstyle and tests.
   - Full build without tests/check.
   - Existing schema/table list and current API routes.
4. Create `qa/content-service-v2.md` with every planned case initially marked `[not verified]`.
5. Lock API naming, enums, table ownership, and dependency choices before writing entities.

Gate:

- Clean baseline or every pre-existing failure documented.
- No v1 source, routing, UI, or database behavior changed.

### Phase 1 — Modules, complete DDL, and isolated runtime

1. Add `content-v2-commons`, `content-v2-core`, `content-v2-events`, and `content-v2-service` to
   `settings.gradle`.
2. Keep Java packages under `com.asrevo.cvhome.content.*`; v2 service depends only on v2 modules.
3. Configure application identity `content`, port `8121`, schema `content`, JPA validation, security, S3, outbox,
   merchant locale client, and billing entitlement client.
4. Add the complete schema above in one `init-sql/schema.sql`, including all constraints, indexes, `pg_trgm`,
   sequencer entries, and outbox tables.
5. Add `application-v2-lcl.yml` overriding only the datasource URL to
   `jdbc:postgresql://localhost:5432/cvhome_content_v2`.
6. Do not register v2 in `run-lcl.sh`, discovery configuration, Caddy, or deployment defaults; it reuses the
   existing `content` identity and route when started manually.
7. Add PostgreSQL/MinIO Testcontainers and schema metadata assertions.

Manual environment:

```bash
docker exec cvhome-postgres-1 createdb -U postgres cvhome_content_v2

./gradlew :store-pod:content-v2:content-v2-service:bootRun \
  --args='--spring.profiles.active=lcl,v2-lcl,test-stores'
```

Gate:

- Application starts on an empty dedicated database.
- Hibernate validation passes without creating or altering tables.
- Tests assert every planned table, critical FK, enum check, unique constraint, and index.
- Starting v1 against `cvhome` remains unaffected.

### Phase 2 — Shared aggregate, translations, lifecycle, revisions, audit, and events

1. Implement `Content` and `ContentDescription` in the established JPA format.
2. Add v2-local enums for content type, lifecycle, and translation state; do not expand legacy shared v1 enums.
3. Put lifecycle transitions on the `Content` aggregate and register domain events there.
4. Implement tenant-scoped repositories; no unscoped `findById` followed by an in-memory store comparison.
5. Implement:
   - Draft creation/update.
   - Per-locale update and default-locale fallback.
   - Translation stale marking.
   - Review, schedule, publish, unpublish, archive, restore.
   - `If-Match`/`@Version`.
   - Immutable revisions and restore-as-new-revision.
   - Audit and status history.
   - Soft deletion and purge.
   - Durable publish/unpublish scheduler.
6. Add common item list/search/filter, revision, translation queue, lifecycle, audit, summary skeleton, and bulk
   contracts.
7. Introduce condition-named `ContentV2Errors` exceptions and shared Problem Detail rendering.

Gate:

- Unit tests cover every legal and illegal transition.
- Concurrent updates produce `409`; missing `If-Match` is rejected.
- Scheduler is idempotent and recovers due work after restart.
- Repository tests prove second-store reads and writes return no target.
- Common API `.http` file covers happy path, stale version, invalid transition, 403, and cross-store failure.

### Phase 3 — Media manager

1. Implement streamed single-file multipart upload:
   - `50 MiB` file limit and `52 MiB` request limit.
   - Disk-backed multipart threshold; always use `getInputStream()`, never `getBytes()`.
   - Magic-byte MIME detection with Apache Tika.
   - SHA-256 checksum and per-store deduplication.
2. Accept JPEG, PNG, WebP, SVG, and PDF.
   - Decode raster images defensively and reject malformed or oversized dimensions.
   - Parse SVG through a secure, no-DTD/no-external-entity XML parser and an explicit element/attribute allowlist.
   - Read PDF page count with PDFBox.
3. Generate non-upscaled `thumb-320`, `card-640`, `hero-1600`, and `full-1920` derivatives.
   - JPEG for opaque output.
   - PNG for alpha output.
   - WebP originals remain acceptable, but v2 does not generate WebP.
4. Store originals and variants under opaque keys containing asset ID and checksum.
5. Process derivatives using idempotent outbox commands and `PROCESSING -> READY|FAILED`.
6. Enforce `MAX_STORAGE_MB` through `StoreEntitlements`; count original bytes and preserve its degraded-open
   behavior.
7. Implement folders, metadata, tags, listing/search, usage index, URL resolution, retry, safe delete, forced
   replacement, restore, and purge.
8. Stop creating a globally public bucket policy at application startup.

Dependencies go through `libs.versions.toml`: OWASP Java HTML Sanitizer `20260313.1`, Apache Tika `3.3.2`, PDFBox
`3.0.8`, and TwelveMonkeys `3.14.0`.

Gate:

- MinIO integration tests cover upload, deduplication, variants, retrieval, retry, delete, and cleanup.
- Negative tests cover spoofed MIME, traversal filenames, oversized files/dimensions, unsafe SVG, corrupt
  PDF/image, quota, and referenced deletion.
- Media asset/folder `.http` files execute through the gateway.

### Phase 4 — Pages and posts

1. Implement page CRUD, localized routes, slug availability, SEO, parent validation, templates, sitemap visibility,
   redirects, and hashed preview tokens.
2. Implement sealed typed blocks: rich text, image, gallery, video link, product grid, FAQ reference, banner
   reference, HTML embed, spacer, and CTA.
3. Sanitize all HTML on write using one centrally configured allowlist; only sanitized output is persisted.
4. Validate internal content/media references; retain opaque product/category IDs without synchronous catalog
   validation.
5. Implement posts, localized excerpts, categories, tags, reading-time derivation, hero media, featured flag, and
   related-post resolution.

Gate:

- Block round-trip and sanitizer adversarial tests pass.
- Published slug changes create redirects; draft changes do not.
- Preview tokens cannot cross stores and expire/revoke correctly.
- Post relationships return at most three published same-store results.
- Page/post private `.http` files cover full lifecycle and failures.

### Phase 5 — Banners and FAQ

1. Implement banners, localized artwork/copy, placement capacity, schedules, positions, country/login targeting,
   media validation, and effective-banner resolution.
2. Reuse the shared content scheduler; do not add a second scheduling mechanism.
3. Implement FAQ groups, localized questions/answers, atomic reorder/move, opaque product/page references, and
   JSON-LD output.
4. Defer segments, banner analytics, and FAQ voting exactly as scoped.

Gate:

- Capacity conflicts return typed `422`.
- Default-locale alt text gates publication.
- Effective-banner ordering is deterministic.
- Concurrent FAQ reorder is atomic and version-protected.
- Banner/FAQ `.http` files cover normal and conflicting writes.

### Phase 6 — Menus and policies

1. Implement whole-tree menu replacement, two-level depth validation, normalized ordering, localized fallback,
   visibility/login flags, and broken internal-reference reporting.
2. Implement policy types, immutable published versions, effective dates, display locations, jurisdiction metadata,
   acceptance flag, supersession, and versioned reads.
3. Publishing a new policy version deactivates the previous version atomically and emits
   `policy.version.published`.
4. Do not implement legal advice/templates, regional compliance decisions, or checkout acceptance storage.

Gate:

- Invalid menu depth returns `422`; replacement is all-or-nothing.
- One active policy per store/type is enforced under concurrency by the database.
- Published policy rows cannot be edited; updates create a new version.
- Menu/policy `.http` files include conflicts and storefront reads.

### Phase 7 — Storefront, search, aggregates, bulk, and operational jobs

1. Complete storefront endpoints for pages, posts, banners, FAQ, menus, policies, and sitemap.
2. Return only effective published content unless a valid preview token is supplied.
3. Add locale fallback metadata, ETags, and
   `Cache-Control: public, s-maxage=300, stale-while-revalidate=60`.
4. Complete shared PostgreSQL full-text/prefix/fuzzy search, dashboard summary, and per-type counts.
5. Implement bulk operations with a 200-ID maximum and `207 Multi-Status`.
6. Add revision retention, audit retention, content/media purge, failed-media retry, and outbox-retention jobs.
7. Add metrics for scheduler lag, media processing depth/failures, storage bytes, translation queue, and endpoint
   latency.

Gate:

- Storefront draft/deleted/expired exclusion tests pass.
- ETag and conditional GET behavior pass.
- Summary predicates match list predicates.
- Bulk partial success is deterministic and tenant-safe.
- Retention jobs are idempotent.

### Phase 8 — Seeds, full API QA, hardening, and handoff

1. Add representative v2 seed data for every domain and every demo store without converting v1 data.
2. Finish one `.http` file per controller using gateway URLs, `store`, `pod`, `lang`, correct seller cookie, chained
   IDs/ETags, and meaningful non-2xx cases.
3. Run the support stack without v1 content, then start v2 manually:
   - Run `uaa`, `billing`, `gateway`, `merchant`, required supporting services, and infra through `run-lcl.sh`.
   - Run v2 separately on 8121 with `lcl,v2-lcl,test-stores`.
4. Execute every API flow through `/spg/content/api/v2/**`.
5. Repeat critical operations with `STORE_ID_2` and a principal lacking `STORE-POD.CONTENT.*`.
6. Update `qa/content-service-v2.md`; no unexecuted case may be labelled verified.
7. Document that seller-ui and landing-ui still use v1 and are intentionally outside this PR.

Final gates:

```bash
./gradlew :store-pod:content-v2:content-v2-service:test
./gradlew checkstyleMain checkstyleTest
./gradlew build -x test -x check
./gradlew test
```

Also run grep gates for generic exception declarations, legacy error throw sites, TODO comments, raw store/language
IDs, unscoped repository methods, and endpoints lacking `@PreAuthorize` or `.http` coverage.

## Public APIs and Contracts

All private APIs take bare `StoreMerchantId merchantStore` and `LanguageCode language` parameters and use:

```java
@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
```

- `/api/v2/private/content/items`: shared list/search/filter, summary, bulk actions, lifecycle, revisions,
  translations, translation queue, restore, and audit.
- `/api/v2/private/content/pages|posts|banners|faq|menus|policies`: typed CRUD and domain operations.
- `/api/v2/private/media/assets|folders`: upload, metadata, listing/search, usage, URL, folder management,
  delete/restore, and retry.
- `/api/v2/storefront/pages|posts|banners|faq|menus|policies|sitemap.xml`: published cache-oriented reads.
- Pagination is zero-based, defaults to 25, caps at 100, and accepts only explicit sort fields.
- Every controller has one gateway-addressed `.http` file with success and declared failure cases.
- Published event contracts live in `content-v2-events`, use aggregate/asset ID ordering keys, and include store,
  type, ID, version, actor, and occurrence time.

## QA Structure

Maintain exactly one file: `qa/content-service-v2.md`.

Sections:

- `00`: environment and dedicated database setup.
- `DB`: schema and constraint verification.
- `CORE`: lifecycle, translations, revisions, concurrency, audit.
- `MED`: upload, variants, folders, quota, usage, deletion.
- `PGE`, `PST`, `BNR`, `FAQ`, `MNU`, `POL`: domain behavior.
- `SF`: storefront cache, fallback, preview, sitemap.
- `SEC`: permission and tenant isolation.
- `OPS`: scheduling, restart recovery, outbox, retention, metrics.
- `REG`: defects found during implementation.
- `99`: known gaps and deferred behavior.

Every case includes setup, steps, expected result, severity, and `[verified]`, `[unit only]`, or `[not verified]`.

## Assumptions and Exclusions

- One PR contains all phases, preferably one coherent commit per green phase.
- Full schema is introduced in Phase 1; later changes correct discovered defects but do not defer table design.
- V2 uses a dedicated database named `cvhome_content_v2` during development and QA.
- V1 remains the default runtime and client backend until the later UI/storefront migration.
- No v1 compatibility API, legacy alias, production data migration, or simultaneous v1/v2 deployment is included.
- No frontend code or frontend build is required unless implementation unexpectedly touches a frontend.
- Content remains strictly store-scoped; cross-store/org sharing is excluded.
- Existing `STORE-POD.CONTENT.*` authorization remains intentionally coarse.
- Machine translation, comments, customer segments, banner analytics, FAQ voting, legal templates/compliance advice,
  checkout policy acceptance, outbound webhooks, import/export, malware scanning, video/PDF derivatives, generated
  WebP, and synchronous catalog-reference validation are deferred.
