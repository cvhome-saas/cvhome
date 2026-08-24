# Content platform — Module 12: new `content` service, console-ui Content module, storefront adoption

## Context

Module 12 ("content") is the last unmigrated module in
`.claude/plans/agents-requirments-console-ui-go-live-m-woolly-candy.md`. Unlike every module before it,
there is nothing worth porting from seller-ui: its content screens are two flat CRUDs (pages, boxes) and a
bare image uploader, and the backend behind them (`store-pod/content`, split out of merchant) is a
single `content`/`content_description` table pair with no status, no schedule, no media metadata, no menus,
no policies. The new console design (`console-template/Content Management.dc.html` + `New Page`, `New Banner`,
`New FAQ Entry`, `New Policy`) and the spec already written for it
(`console-template/Content Management Service - Backend Requirements.md`) describe a real CMS: seven
domains, per-locale translation state, draft→review→scheduled→published→archived, revisions, a media
library with folders/quota/usage, navigation menus, versioned legal policies, and a cache-friendly storefront
read API.

**Decisions settled with the user (do not re-litigate):**

1. `git mv store-pod/content store-pod/content-deprecated` (kept as reference, unregistered from Gradle and
   `run-lcl.sh`); a **new `store-pod/content`** is created with the **same service name `content`, port 8121,
   schema `content`, `/spg/content/**` routing** — so Caddy, discovery, `common-config.yml`, `run-lcl.sh`,
   `configure-domain.sh`, docker `extra_hosts` are all untouched. Fresh DBs come from seeds; existing local/deployed DBs are migrated in place by the additive DDL (see decision 5).
2. **All seven domains** in v1: pages, blog posts, banners & promos, FAQ, media library, navigation menus,
   legal & policies.
3. **landing-ui keeps working on day one** (compat layer for the three endpoints it calls) **and later adopts
   the new read API + new surfaces** (menus, blog, FAQ, policies, banners, sitemap) through the theme contract.
4. **Storefront Builder UI is out of scope now**, built later — leave a clean seam (`template`, `meta.blocks`,
   `ContentData.blocks`, `HomeData.banners`), build no blocks UI.
5. **Reuse the old table/entity architecture where it fits.** `content.content` + `content.content_description`
   (and `sm_sequencer`) are kept by name and extended with additive columns; the `Content` / `ContentDescription`
   entities, `BaseDescription`, `ContentRepository`/`PageContentRepository` and the existing per-store seeds are
   carried into the new service and extended rather than replaced. Existing databases migrate with
   `ALTER TABLE … ADD COLUMN IF NOT EXISTS` (all new columns have defaults); legacy BOX/PAGE rows keep working
   unchanged. New domains that have no legacy counterpart (media, menus, policy versions, FAQ groups, revisions…)
   get new tables.

Intended outcome: the seller edits pages/posts/banners/FAQ/media/menus/policies in console-ui at `/content`,
publishes with schedule, and the storefront renders it; seller-ui's content screens and the old private API
die with seller-ui (Module 13).

### What exists and is reused (verified)

- Pod conventions: `StoreMerchantId` embeddable + `ServletStoreMerchantIdArgumentResolver`/
  `ServletLanguageCodeArgumentResolver` (`?store=&lang=`), `AuditSection`/`AuditListener`,
  `LanguageCodeConverter`, `SalesManagerEntity`, `AbstractDataPopulator`, `ReadableList`, `EntityExists`,
  shared `@ControllerAdvice` (RFC-7807) — all in `store-pod/commons/store-commons` / `store-commons/autoconfigure`.
- Storage: `store-pod/commons/store-modules/store-cms-commons` (`ContentAssetsManager`,
  `S3StaticContentAssetsManagerImpl`, `InputContentFile`, `FileContentType`, `CmsErrors`) — **stays**, catalog and
  merchant depend on it. One pod bucket, key-prefixed per store, public-read via bucket policy, no presigned URLs.
- Permission token `STORE-POD.CONTENT.*` in `CustomPermissionEvaluator.java:49`; `MERCHANT_READ →
  hasReadAccessOnStore` at line 61 is the pattern for a read token.
- Copy verbatim from `content-deprecated/content-service`: `S3Config`, `S3InitConfigurer`, `LocalConfig`,
  `SwaggerConfig`, `SecurityConfig`, `application*.yml`, `build.gradle`, test `MinioS3Config` +
  `TestcontainersConfiguration`. Copy `store-pod/catalog/catalog-service/.../config/SchedulingConfig.java`.
- console-ui: `core/http/crud.service.ts` (`request(..., {reportProgress})` for uploads), `core/errors/*`,
  `shared/ui/{rich-text, rich-text-html, image-picker, image-preview, data-table, pagination, tab-switcher,
  locale-switcher, badge, section-nav, tag-input, tree, date-picker, kpi-card, kpi-grid, confirm-dialog,
  empty-state, toast, …}`, `shared/validators/slug.ts`, the store-management facade/form pattern
  (`features/store-management/components/home-section` for per-locale forms).
- landing-ui: `libs/services/src/content-service.ts` (3 calls), `libs/types/src/content.ts`,
  `libs/theme/src/contract.ts`, shell loaders `storefront/src/shell/loaders/*.ts`, `shell/seo/metadata.ts`.

---

## A. Backend — new `store-pod/content`

### A.1 Modules

```
store-pod/content/
├── content-commons/   DTOs (Readable*/Persistable*), enums, ContentErrors + exceptions   (java.library.conventions)
├── content-core/      JPA entities, repositories, services, facades, populators, storage, html, jobs
└── content-service/   ContentApplication, api/v1/*, config/*, resources, http/*.http, tests
```

Package root `com.asrevo.cvhome.content`. **JPA** (pod-layer convention; `StoreMerchantId`, `AuditSection`,
populators all assume it; `@JdbcTypeCode(SqlTypes.JSON)` for jsonb). `init-sql/schema.sql` is the DDL source of
truth, `ddl-auto: update` a safety net. **No `content-external-api`** — no Java caller exists. `settings.gradle`
lines 54–56 stay as they are (paths unchanged). Add `jsoup` to `gradle/libs.versions.toml` (HTML sanitising).

### A.2 Schema (`content-service/src/main/resources/init-sql/schema.sql`, schema `content`)

Ids keep the legacy `content.sm_sequencer` `@TableGenerator` (`CONTENT_SEQ_NEXT_VAL`,
`CONTENT_DESCRIPTION_SEQ_NEXT_VAL`) for the two legacy tables so existing ids stay valid; new tables use one
sequence `content.content_seq`. Every table: `store_merchant_id varchar(50) not null` + audit columns
(`date_created`, `date_modified`, `updt_id`); every unique includes the store. The file is written as
`create table if not exists` + `alter table … add column if not exists … default …` so it is both the fresh DDL
and the in-place migration.

| Table | Key columns | Notes |
|---|---|---|
| `content` (**legacy, extended**) | kept: `content_id` PK, `code varchar(100)` (= slug), `content_type` CHECK widened to (BOX, PAGE, SECTION, POST, BANNER, FAQ, POLICY), `visible`, `link_to_menu`, `sort_order`, `content_position`, `product_group`, `store_merchant_id`, unique `(store_merchant_id, code)`. **added** (all `if not exists`, with defaults): `status` CHECK (DRAFT, REVIEW, SCHEDULED, PUBLISHED, ARCHIVED) default `'DRAFT'` — migration: `update … set status='PUBLISHED' where visible`; `publish_at`, `unpublish_at`; `version int default 1` (`@Version`); `created_by`, `updated_by`; `parent_id` (PAGE parent / FAQ → `faq_group.id`); `template` CHECK (STANDARD, LANDING, CONTACT, FAQ_PAGE) null; `noindex bool default false`, `canonical_url`, `og_media_id`, `show_in_footer bool default false`; `placement` CHECK (HERO, CAROUSEL, COLLECTION, STRIP) null; `starts_at`, `ends_at`; `policy_type` CHECK (TERMS, PRIVACY, RETURNS, SHIPPING, COOKIES, CUSTOM) null; `meta jsonb` | new idx `(store_merchant_id, content_type, status)`, `(status, publish_at)`, `(store_merchant_id, placement, status)`. `BOX` rows are the "snippets" (meta-title, meta-description, landing, header-message, agreement — legacy codes untouched). `meta` = non-queried per-type payload: PAGE `{blocks:[]}` (builder seam), POST `{heroMediaId, featured, readingMinutes, authorName}`, BANNER `{target{kind,value}, artwork{desktopMediaId,mobileMediaId,mobileCrop}, theme{textColor,overlayOpacity,alignment}, audience{loggedInOnly}}`, FAQ `{keywords[], showInCheckoutHelp}`, POLICY `{jurisdiction, requiresAcceptance, notifyCustomers, displayAt{footer,checkout,signup}}` |
| `content_description` (**legacy, extended**) | kept: `description_id` PK, `content_id` FK, `language_code varchar(6)`, `name(120) not null` (= title shown in lists), `title(100)`, `description text` (= body), `meta_title`, `meta_description`, `meta_keywords` (**finally read/written** — closes the lessons.md keywords entry), `sef_url(120)` (= per-language friendly URL, still served to the storefront), unique `(content_id, language_code)`. **added**: `state` CHECK (MISSING, DRAFT, TRANSLATED, STALE) default `'TRANSLATED'`; `excerpt(300)`; `alt_text(255)`; `cta_label(60)`; `subtitle(300)` | entity `ContentDescription extends BaseDescription` kept; `@OneToMany` gains `orphanRemoval=true` (closes the "all-or-nothing" lessons entry); `@UniqueConstraint` fixed to `LANGUAGE_CODE` |
| `content_revision` | `content_id`, `version`, `snapshot jsonb`, `author`, `created_at` | unique `(content_id, version)`; restore = new revision |
| `content_status_audit` | `content_id`, `from_status`, `to_status`, `actor`, `reason`, `occurred_at` | append-only |
| `faq_group` | `name_key(60)`, `position`, `names jsonb {locale:name}` | unique `(store, name_key)` |
| `post_category` / `post_category_link` / `post_tag` | `slug`, `names jsonb`, `position` / `(content_id, category_id)` / `(content_id, tag)` | idx `(store, tag)` |
| `policy_version` | `content_id`, `version`, `effective_from`, `note(200)`, `translations jsonb {locale:{heading,body}}`, `status` CHECK (DRAFT, LIVE, ARCHIVED), `published_at`, `published_by` | unique `(content_id, version)`; partial unique one LIVE per item; published text immutable |
| `menu` / `menu_item` | `handle` CHECK (MAIN, FOOTER), `names jsonb` / `menu_id`, `parent_id`, `position`, `labels jsonb`, `target_kind` CHECK (PAGE, CATEGORY, PRODUCT, POLICY, BLOG_INDEX, FAQ_INDEX, URL), `target_value(255)`, `open_in_new_tab`, `visible` | unique `(store, handle)`; depth ≤ 2 enforced in service (422) |
| `media_folder` | `name(60)`, `key(60)`, `position`, `system` | unique `(store, key)`; defaults banners, products, brand, video, docs |
| `media_asset` | `folder_id`, `filename`, `original_filename`, `mime_type`, `kind` CHECK (IMAGE, VIDEO, DOCUMENT, ARCHIVE, VECTOR), `bytes`, `width`, `height`, `checksum char(64)`, `storage_key(255)`, `public_url(500)`, `alt_texts jsonb`, `title`, `tags jsonb`, `uploaded_by`, `uploaded_at` | unique `(store, checksum)` = dedupe; idx `(store, folder_id)`, `(store, kind)` |
| `media_usage` | `(asset_id, content_id, field)` | rebuilt on every item save from media ids in `meta`/`og_media_id` |
| `media_quota` | `store_merchant_id PK`, `bytes_used` | quota limit from `com.asrevo.cvhome.content.media.quota-bytes` (default 5 GiB) |
| `redirect` | `from_path`, `to_path`, `created_at` | unique `(store, from_path)`; auto-row when a PUBLISHED page's slug changes |

**Legacy rows need no mapping.** Today's BOX rows (`meta-title`, `meta-description`, `header-message`,
`agreement`, `LANDING_PAGE`) stay BOX rows and are edited as "snippets"; today's PAGE rows stay PAGE rows with
`status=PUBLISHED` after migration, `code` as slug, `sef_url` as the per-language friendly URL, and `link_to_menu`
honoured by the compat list. The richer domains layer on top: the storefront announcement is the effective STRIP
banner **falling back to the `header-message` box**; the checkout agreement is the LIVE TERMS policy **falling back
to the `agreement` box**. Existing seeds `init-sql/stores/<storeId>/01-store.sql` are copied unchanged (plus
`status`); `data-common.sql` keeps the two sequencer rows. `StoreDefaultsService.ensure(store)` lazily creates
menus (MAIN seeded from `link_to_menu` pages on first read) + default folders per store.

### A.3 Core (`content-core`)

```
entity/      Content + ContentDescription (carried from content-deprecated/content-core, extended), ContentRevision,
             ContentStatusAudit, FaqGroup, PostCategory, PolicyVersion, Menu, MenuItem, MediaFolder, MediaAsset,
             MediaUsage, MediaQuota, Redirect
repository/  ContentRepository + PageContentRepository (carried; PageContentRepository's Specification extended with
             status / locale-state / q / placement / effective window) + one per new entity
service/     ContentService (carried, reduced to CMS CRUD — asset methods move to MediaService; slug rules,
             revisions, optimistic version), PublishingService (state
             machine + audit + scheduler tick), TranslationService (per-locale write, STALE marking), PageService,
             PostService, BannerService (capacity + effective), FaqService (groups, atomic reorder), PolicyService
             (versions, supersede, compliance, templates), MenuService (tree replace, depth, broken targets),
             MediaService (upload→S3, probe, dedupe, quota, usage, 409 delete), SummaryService, BulkService,
             StoreDefaultsService, RedirectService
facade/      ContentFacade (private), StorefrontFacade (public, locale fallback + servedLocale),
             LegacyContentFacade (compat shapes)
populator/   Readable*Populator extends AbstractDataPopulator (ReadableContentPage/BoxPopulator carried for compat, now mapping keywords)
storage/     MediaStorage (wraps ContentAssetsManager; key files/{storeId}/media/{assetId}/{filename};
             url ${cdn.base-path}/… ), ImageProbe (ImageIO), SvgSanitizer
html/        HtmlSanitizer (jsoup Safelist.relaxed + figure/img from media host + div[dir])
job/         ScheduledPublishJob (@Scheduled fixedDelay 60s; SCHEDULED&publish_at<=now→PUBLISHED;
             PUBLISHED&unpublish_at<=now→ARCHIVED; idempotent by predicate; audit rows)
```

State machine (`ContentStatus` in commons, `canTransitionTo`): DRAFT→REVIEW→SCHEDULED→PUBLISHED→ARCHIVED,
REVIEW→DRAFT, PUBLISHED→DRAFT (unpublish), ARCHIVED→DRAFT (restore), DRAFT→PUBLISHED direct. Publish gate:
default-locale `title` (+ `body` for PAGE/POST/FAQ/POLICY; `alt_text` for BANNER with artwork); SCHEDULED needs a
future `publishAt`. Editing a PUBLISHED item's default-locale body marks other locales STALE.

### A.4 Errors (`content-commons/.../errors/ContentErrors`, same style as today's enum)

`CONTENT.NOT_FOUND`404 · `CONTENT.SLUG.DUPLICATE`409 · `CONTENT.VERSION.CONFLICT`409(`currentVersion`) ·
`CONTENT.STATUS.TRANSITION_NOT_ALLOWED`422 · `CONTENT.PUBLISH.INCOMPLETE`422(`fieldErrors[]`) ·
`CONTENT.SCHEDULE.INVALID`400 · `CONTENT.PAGE.REFERENCED`409(`menus[]`) · `CONTENT.BANNER.CAPACITY_EXCEEDED`422 ·
`CONTENT.MENU.DEPTH_EXCEEDED`422 · `CONTENT.POLICY.VERSION_IMMUTABLE`422 · `CONTENT.POLICY.TYPE_ACTIVE_EXISTS`409 ·
`CONTENT.FAQ.GROUP_NOT_FOUND`404 · `MEDIA.NOT_FOUND`404 · `MEDIA.TYPE_NOT_ALLOWED`400 · `MEDIA.TOO_LARGE`413 ·
`MEDIA.QUOTA_EXCEEDED`413(`bytesUsed,bytesQuota`) · `MEDIA.REFERENCED`409(`usage[]`) · `MEDIA.FOLDER.NOT_EMPTY`409 ·
`MEDIA.UNREADABLE`400 · `MEDIA.STORAGE_FAILED`500 (wraps `CmsErrors`). Bulk → 207 `[{id, ok, errorCode?}]`.

### A.5 REST API (`content-service/.../api/v1/`)

All endpoints take `StoreMerchantId merchantStore`, `LanguageCode language`. Writes:
`@PreAuthorize(hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*'))`; private reads:
`'STORE-POD.CONTENT.READ'` — **add `CONTENT_READ` to `CustomPermissionEvaluator` mapped to
`hasReadAccessOnStore`** (beside `MERCHANT_READ`). `SecurityConfig` unchanged.

**Private — `/api/v1/private/content`** (controllers `PageApi`, `PostApi`, `BannerApi`, `FaqApi`, `PolicyApi`,
`MenuApi`, `MediaApi`, `ContentAdminApi`; `{type}` ∈ pages|posts|banners|faq|policies):

| Endpoint | Shape |
|---|---|
| `GET /summary` | `{publishedItems, drafts{total,staleOver30Days}, awaitingTranslation{total,byLocale}, media{bytesUsed,bytesQuota,fileCount}, counts{pages,posts,banners,faq,media,menus,policies}}` |
| `GET /{type}?status&locale&state&q&page&count&sort` | `ReadableList<ContentRow{id,type,slug,title,status,publishAt,locales[{code,state}],updatedAt,updatedBy,subtitle}>`; `count`≤100; `q` ILIKE slug/title/body (no FTS — lessons) |
| `GET /{type}/{id}` · `POST /{type}` · `PUT /{type}/{id}` (body has `version`) · `DELETE /{type}/{id}?force` | common `{id,type,slug,status,version,publishAt,unpublishAt,noindex,canonicalUrl,ogMediaId,translations[{language,state,title,body,excerpt,metaTitle,metaDescription,altText,ctaLabel,subtitle}],audit…}` + per-type fields; `POST`→`Entity{id}`; `PUT`→`{id,version}`; pages in menus → 409 unless force; hard delete |
| `POST /{type}/{id}/publish {publishAt?}` · `/unpublish` · `/submit-review` · `/archive` · `/restore` | `{id,status,version}` |
| `GET /{type}/{id}/revisions` · `POST …/revisions/{v}/restore` · `PUT /{type}/{id}/translations/{lang}` · `GET /{type}/slug-available?slug&excludeId` (`EntityExists`) · `POST /{type}/{id}/preview-token` | |
| `POST /bulk {ids[],action:PUBLISH\|UNPUBLISH\|ARCHIVE\|DELETE}` | 207, max 200 |
| Pages | `template, parentId, showInFooter`; `GET /redirects` |
| Posts | `categoryIds[], tags[], heroMediaId, featured, authorName`, derived `readingMinutes`; `/post-categories` CRUD |
| Banners | `placement, startsAt, endsAt, target, artwork, theme, audience{loggedInOnly}`; `GET /banners/effective?placement`; capacity HERO 1, CAROUSEL 8, STRIP 1, COLLECTION 1 per target |
| FAQ | `groupId, keywords[], showInCheckoutHelp`; `/faq-groups` CRUD; `PATCH /faq/reorder [{id,groupId,position}]` atomic |
| Policies | `policyType, jurisdiction, requiresAcceptance, notifyCustomers, displayAt`; `GET/POST /policies/{id}/versions`; `POST …/versions/{v}/publish {effectiveFrom}` (supersedes LIVE→ARCHIVED); `GET /policies/compliance` `[{type,requiredBy[],status}]` (config rule table: EU → COOKIES+PRIVACY+TERMS+RETURNS); `GET /policy-templates?type&jurisdiction` (classpath en+ar starters) |
| Menus | `GET /menus`; `GET /menus/{handle}` tree `{id,handle,items[{id,position,labels,target{kind,value,broken},openInNewTab,visible,children[]}]}`; `PUT /menus/{handle}` replaces tree |
| Media | `GET /media?folder&kind&q&used&page&count&sort` → `ReadableList<MediaAsset{id,filename,mimeType,kind,bytes,width,height,url,folderId,altTexts,title,tags,uploadedAt,uploadedBy,usageCount}>`; `POST /media` multipart `files[]`,`folderId?` (JPG/PNG/WebP/SVG/MP4/PDF/ZIP, 50 MB, quota 413, sha256 dedupe returns existing); `PATCH /media/{id}`; `DELETE /media/{id}?force` (409 `MEDIA.REFERENCED`); `GET /media/{id}/usage`; `/media/folders` CRUD (`DELETE ?moveTo`) |
| Snippets | `GET /snippets` · `PUT /snippets/{code}` `{id, code, translations[{language,title,body,metaDescription,keywords}]}` — BOX rows; `LANDING_PAGE` keeps its code so Module 5's card needs no data change |

**Public storefront — `/api/v1/storefront`** (no auth; `?store&lang`; `Cache-Control: public, max-age=60` + ETag;
locale fallback to store default with `servedLocale`; drafts only with `?preview=<HMAC token>`):

| Path | Returns |
|---|---|
| `GET /site` | `{snippets{metaTitle,metaDescription}, announcement\|null (STRIP banner, else `header-message` box), menus{main[],footer[]}, footerPages[{slug,title}], policies[{type,slug,heading}]}` — one call replaces today's 3 layout fetches |
| `GET /pages/{slug}` | `{id,slug,title,body,template,blocks[],seo{metaTitle,metaDescription,canonicalUrl,noindex,ogImageUrl},breadcrumbs,updatedAt}` |
| `GET /posts?page&count&category&tag` · `GET /posts/{slug}` (+`related[≤3]`) · `GET /post-categories` · `GET /posts/feed.xml` | post `{slug,title,excerpt,heroImageUrl,publishedAt,authorName,readingMinutes,categories[],tags[]}` |
| `GET /banners?placement` | effective `[{id,placement,position,title,subtitle,ctaLabel,target,artwork{desktopUrl,mobileUrl,altText},theme}]` |
| `GET /faq?group` | `{groups[{key,name,entries[{slug,question,answer}]}], jsonLd}` |
| `GET /menus/{handle}` · `GET /policies/{type}?v` · `GET /sitemap?locale` · `GET /redirects/{path}` | resolved tree with `href` / `{type,version,heading,body,effectiveFrom,requiresAcceptance}` / `[{loc,lastmod,changefreq}]` / `{to}` |

**Legacy compat — `/api/v1/content`** (`LegacyContentApi`, `@Deprecated`, deleted in phase 7), exact shapes
landing-ui's `libs/services/src/content-service.ts` expects:
- `GET /pages?page&count` → `{totalPages, number, recordsTotal, recordsFiltered, content:[{id, code=slug, visible:true, linkToMenu=(in MAIN menu), contentType:"PAGE", description:{id, language, name=title, description=body, friendlyUrl=slug, title, metaDescription, keyWords:null, highlights:null, priceAppender:null}}]}`
- `GET /pages/name/{slug}` → same item; 404 `CONTENT.NOT_FOUND`
- `GET /boxes/{code}` → `{id, code, visible, contentType:"BOX", description}` — a direct read of the BOX row (all
  legacy codes keep working); 404 otherwise.
The old **private** API (`/private/content/box|page|files`, `/content/images`) is **not** reimplemented —
seller-ui's content screens 404 from phase 1 (recorded in lessons.md; seller-ui retires in Module 13).

### A.6 `.http` + tests

`content-service/http/{pages,posts,banners,faq,policies,menus,media,storefront,legacy}.http` — one runnable block
per endpoint via `http://spg-507f1f77.gateway.com/content/...`.
Tests (testcontainers postgres+minio): context starts, only schema `content` created; `ContentStatusTest`
(transitions); `PublishGateTest`; `ScheduledPublishJobTest` (injected clock); `TenantIsolationTest`;
`PermissionTest` (moderator 403 write / 200 READ); `LegacyCompatShapeTest` (field-exact JSON for the 3 endpoints);
`MediaUploadTest` (dedupe, quota 413, type 400, svg sanitised, delete 409→force); `BannerCapacityTest`;
`MenuDepthTest`; `PolicyVersionTest`.

---

## B. console-ui — `features/content/`

### B.1 API tier + models

Rewrite `src/app/models/content.ts` (drop `ReadableContentBox`/`PersistableContentBox`/`ContentEntityId`):
`ContentType`, `ContentStatus`, `TranslationState`, `ContentRow`, `ContentTranslation`, `ReadablePage/Post/Banner/
Faq/Policy` + `Persistable*`, `ContentSummary`, `FaqGroup`, `PostCategory`, `PolicyVersion`, `Menu`, `MenuItem`,
`MediaAsset`, `MediaFolder`, `Snippet`, `BulkResult`, `Redirect`.
`src/app/api/content/`: `content-items.service.ts` (generic typed by `ContentType`: list/get/create/update/delete/
transition/revisions/slugAvailable/bulk), `content-summary.service.ts`, `pages.service.ts`, `posts.service.ts`,
`banners.service.ts`, `faq.service.ts`, `policies.service.ts`, `menus.service.ts`, `media.service.ts`
(`CrudService.request(..., {reportProgress:true})`), `snippets.service.ts`. Delete `content-box.service.ts`;
`features/store-management/services/store-settings.api.service.ts` (`HOME_BOX_CODE`, line 68/167) switches the
home card to `snippets.get/put('LANDING_PAGE')` — same UI, so Module 5 keeps working from phase 1.

### B.2 Routes, nav, permissions

`app.routes.ts` (guards `[canAccessSecuredPages, consoleContext, merchantOnly, requiresStore]`, data
`titleKey:'route.content.title'`, `breadcrumbKey:'shell.breadcrumb.content'`):
```
content                      → redirect content/pages
content/:tab                 ContentHub  (pages|posts|banners|faq|media|menus|policies; unknown → pages)
content/{pages|posts|banners|faq|policies}/new | /:id   → the five editors
```
`layouts/console-shell/console-navigation.ts`: add `route: '/content'` to the existing `contentManagement` item.
`shared/auth/console-permissions.ts`: `canManageContent()`, `canReadContent()`; Save/Publish hidden for read-only.

### B.3 Feature tree

```
features/content/
  content-hub.ts/.html/.css          page-header (title, "Home page builder" disabled via storeSettings.nav.builderUnavailable,
                                     primary create button per tab) + kpi-grid (4 cards from /summary) + section-nav rail
                                     (7 tabs with counts) + @switch tab
  facades/content-hub.facade.ts      rxResource summary, tab signal
  facades/content-list.facade.ts     generic: type, status, locale/state, q, page, sort, selection, bulk
  facades/media-library.facade.ts    folders, kind filter, grid/list, uploads w/ per-file progress, quota
  facades/menus.facade.ts            two trees, dirty, save whole tree
  services/content.api.service.ts    composes api/content/* into view models (row subtitle, locale badges)
  components/content-list/           data-table + tab-switcher (All/Published/Draft/Review/Scheduled) + locale select
                                     + search-box + pagination + row actions (edit, ⋯: publish/unpublish/archive/delete)
                                     + bulk-bar when selection > 0
  components/locale-badges/          green/grey/amber chips from locales[]
  components/media-tab/              folder chips + quota bar + file-drop-zone + media-grid | media-list + asset drawer
                                     (usage, alt text per locale, copy URL, move, delete)
  components/menus-tab/              Main + Footer cards; sortable-list rows (label, target, up/down, indent/outdent,
                                     visible, remove); add-link dialog (page/category/product/policy/url picker); Save
  components/policies-tab/           compliance cards (type, meta, status, required-by) → editor
  components/editor-shell/           header (Cancel / Save draft / publish-menu), 2-col layout, locale strip
                                     (locale-switcher [filled]=state≠MISSING), fallback note, visibility sidebar,
                                     preview card, publish-checklist, success panel
  components/seo-block/              slug (async slug-available + shared/validators/slug.ts), meta title/description
                                     counters, serp-preview
  editors/page-editor/               template cards → rich-text per locale → seo-block → sidebar (publish date →
                                     date-time-picker, add-to-footer, noindex, parent page)
  editors/post-editor/               hero (media-picker), excerpt, categories (autocomplete), tags (tag-input),
                                     featured, schedule
  editors/banner-editor/             placement cards + size hint, artwork (image-picker | media-picker), alt text,
                                     mobile crop, copy per locale, link target, starts/ends, logged-in only, preview
  editors/faq-editor/                question / rich-text answer per locale, group cards, position (after X),
                                     keywords, checkout-help toggle, accordion preview
  editors/policy-editor/             type cards + Insert template, heading/body per locale, jurisdiction, effective
                                     date, requires-acceptance, notify toggle, version history (restore as draft),
                                     checkout preview
```
Editor mechanics copy `store-management`: `rxResource` + `linkedSignal` snapshot, one `FormGroup` per locale,
`ApiErrorService` + `clearServerErrorsOnChange`, 409 version conflict → confirm-dialog "Reload", publish = save
then `/publish` (`{publishAt}` → SCHEDULED), then the success panel from the `.dc.html`.

### B.4 New `shared/ui/` components

`file-drop-zone` (multi-file, accept list, per-file progress/error) · `media-grid` + `media-list` · `media-picker-dialog`
· `date-time-picker` · `publish-menu` (status-aware split button) · `bulk-bar` · `serp-preview` · `publish-checklist` ·
`sortable-list` (up/down + keyboard; no DnD lib — lessons) · `version-list`. Icons in `icon/icon-paths.ts`:
`image, images, comment, questionCircle, sitemap, shield, cloudUpload, link, thLarge, bars, language`.

### B.5 i18n, lessons, mapping

- `src/locale/en.json` + `ar.json` in the same commit (strict missing handler): `content.*`, `route.content.*`,
  `shell.breadcrumb.content`, `errors.content.*` for every `ContentErrors` code.
- `lessons.md` entries for deferred backend: audience segments/country targeting, machine translation, malware
  scan, image derivatives/CDN purge, banner impression/click events, FAQ helpfulness, blog comments, export/import,
  full-text search, per-store locale config endpoint (uses store `supportedLanguages`), drag-and-drop reorder, in-console
  draft preview (token exists, console links out), media quota from billing entitlements, soft-delete window,
  seller-ui content screens 404 until Module 13.

| seller-ui capability | New location |
|---|---|
| Pages list/add/edit (code, visible, linkToMenu, order, per-lang name/friendlyUrl/title/meta/body) | `content/pages` + PageEditor (slug = friendlyUrl; `code` dropped; linkToMenu → Menus tab + "Add to footer menu"; order → menu position; visible → status) |
| Boxes (`meta-title`, `meta-description`, `header-message`, `agreement`, arbitrary) | "Store snippets" card on the Pages tab edits **every** BOX row (so nothing in an existing DB becomes unreachable); the designed richer editors — Banners → Announcement strip, Policies → Terms with acceptance — supersede `header-message`/`agreement` when a STRIP banner / LIVE TERMS policy exists. |
| Files list/upload/delete/lightbox, image browser dialog | Media library tab + `media-picker-dialog` |
| Recommended-code placeholder rows | Policies compliance cards + empty states with Create CTAs |
| Landing page (never worked) | Module 5 home card on the `landing` snippet (unchanged UI) |

---

## C. landing-ui

**Phase A (ships with backend phase 1):** nothing changes; compat keeps `getContents/getPage/getBox` valid.
QA: home, `/content/about-us`, announcement, checkout agreement, `<title>`.

**Phase B:**
- `libs/types/src/content.ts`: add `SiteContent, StorefrontPage, PostSummary, PostDetail, PostCategory, Banner,
  FaqGroup, FaqEntry, MenuNode, Policy, SitemapEntry`; keep `Page/Box` until phase 7.
- `libs/services/src/content-service.ts`: `getSite, getPage, getPosts, getPost, getBanners(placement), getFaq,
  getMenu, getPolicy(type), getSitemap, getRedirect` on `${storeBaseServiceUrl('content', ctx)}/api/v1/storefront`.
- `libs/theme/src/contract.ts`: `LayoutData` += `menus{main,footer}`, `announcement?: Banner`, `policies[]`
  (keep `pages` one release); `HomeData` += `banners{hero[],carousel[],strip?}` (fallback to
  `store.sliderImages`/`banner` when empty); `ContentData` += `seo, template, blocks: unknown[]` (seam); new
  `BlogIndexData, BlogPostData, FaqData, PolicyData`; `ThemePages` += `BlogIndex, BlogPost, Faq, Policy`
  (`defineTheme` throws if missing → implement in starter first, then basic/beauty/fashion).
- Shell: loaders `site.ts` (replaces layout's 3 calls), `blog.ts`, `faq.ts`, `policy.ts`; `seo/metadata.ts` adds
  og + canonical; routes `/[locale]/blog`, `/blog/[slug]`, `/help`, `/policies/[type]`, `app/sitemap.ts`,
  `app/robots.ts`; `proxy.ts` consults `/redirects/{path}` before 404 on `/content/*`.
- **Keep `/content/[url]`** as the page route (12 theme files build it); no `/pages/[slug]`.
- Nav/MobileNav/IndexStrip render `menus.main` (category tree fallback when empty); Footer renders `menus.footer` +
  `policies`; CheckoutForm agreement uses `getPolicy('TERMS')`; search providers index pages + posts.

---

## D. Phases & commits

| # | Commit | Scope | Verify |
|---|---|---|---|
| 0 | `plan(content): content platform — service, console module, storefront` | copy this plan to `.agents/plans/console-ui-content.md`; link from `lessons.md` | — |
| 1 | `feat(content): new content service — items, pages, snippets, legacy compat` | `git mv store-pod/content store-pod/content-deprecated`; new modules A.1 (entities, repos, populators, facades carried from content-deprecated then extended); schema A.2 incl. the `alter table … add column if not exists` migration block; state machine/revisions/audit/redirects; `PageApi`, snippets, summary (pages only), `LegacyContentApi`; seeds copied + `status`; MAIN menu derived from `link_to_menu`; `CONTENT_READ` in evaluator; `SchedulingConfig` + job; tests (context, isolation, permission, compat shapes, **migration test: boot against a DB created by content-deprecated's schema + seeds, assert rows readable with status PUBLISHED**); console-ui `store-settings.api.service.ts` → `landing` snippet | `./gradlew :store-pod:content:content-service:test checkstyleMain checkstyleTest`, `./gradlew build -x test -x check`, `run-lcl.sh`, landing-ui smoke (home, `/content/about-us`, announcement, agreement, title), `.http` blocks, console home card saves |
| 2 | `feat(content): posts, banners, faq, policies, menus, media` | remaining domains, bulk, effective banners, media upload/S3, folders, usage, quota, policy versions/templates/compliance, full storefront read API + sitemap/feed | per-domain tests; `.http`; objects visible in MinIO |
| 3 | `feat(console-ui): content hub, pages, posts, media` | api tier, models, routes, nav, hub, list, media tab, page+post editors, new shared components, i18n | `npm run build`, `ng test`; browser: create→publish page → visible at `/content/<slug>`; upload media; KPIs match lists |
| 4 | `feat(console-ui): banners, faq, menus, policies` | remaining editors/tabs, compliance cards, version history, menu editor | same; storefront strip banner/agreement via compat |
| 5 | `fix(console-ui,content): content after QA` | side-by-side QA vs seller-ui files/pages screens, a11y, RTL, lessons.md | — |
| 6 | `feat(landing-ui): storefront content — menus, blog, faq, policies, banners, sitemap` | section C phase B across shell + 4 themes | landing-ui `npm run build`; per-theme QA; Lighthouse SEO on blog/policy |
| 7 | `chore(content): retire legacy compat` (with Module 13) | delete `LegacyContentApi`, Caddy `@legacy_content` alias, `store-pod/content-deprecated`, `Page/Box` types | full build |

## Verification (end-to-end, after phase 4)

1. `run-lcl.sh` stack up; sign in to console-ui as org admin; `/content` shows KPIs and 7 tabs with counts.
2. Create a page (EN+AR), publish → `GET /spg/content/api/v1/storefront/pages/<slug>?store=&lang=` and the storefront
   `/en/content/<slug>` render it; change slug → `redirect` row + old URL redirects.
3. Schedule a post 2 min ahead → job flips it to PUBLISHED within 60 s; KPI "Scheduled" → "Published".
4. Upload 3 files (one duplicate) → 2 assets, quota bar moves; delete a referenced asset → 409, force works.
5. Strip banner → storefront announcement; TERMS policy v2 publish → checkout agreement shows new text.
6. Sign in as moderator → lists load, Save/Publish hidden, `PUT` → 403.
7. Second store cannot read the first store's items/media/menus (`.http` isolation block).
8. `./gradlew test`, console-ui `npm run build && ng test`, landing-ui `npm run build`.
