# Content Management Service — Backend Requirements

Service that powers the **Content management** screen of the cvhome seller console and the
storefront rendering of all authored content. Scope covers seven content domains:
pages, blog posts, banners & promos, FAQ entries, media library, navigation menus,
and legal & policies.

- **Service name:** `content-service`
- **Consumers:** Seller console (admin UI), Storefront renderer, Home page builder, Checkout (policies)
- **Multi-tenant:** every record is scoped to an organization and to one or more stores
- **Multi-locale:** every authored item carries per-locale content with independent status

---

## 1. Core concepts

### 1.1 Tenancy scope

Every request is resolved against three scopes, in order:

| Scope | Source | Notes |
|---|---|---|
| Organization | auth token | hard isolation boundary; no cross-org reads |
| Store | `X-Store-Id` header or `storeId` query param | a seller may own several stores (e.g. Acme Supply Co., Acme Outlet — West, Acme Wholesale) |
| Locale | `locale` query param, default = store default locale | `en`, `ar`, `fr`, `de` in the current design |

Content may be **store-scoped** (belongs to one store) or **org-scoped and shared**
(published to many stores). Policies and legal text are typically shared; banners are
usually store-scoped.

### 1.2 Content item — common model

All seven domains share a base entity so list views, search, status filters, audit and
translation tracking can be implemented once.

```
ContentItem
  id                uuid
  orgId             uuid
  storeIds          uuid[]        # empty = all stores in org
  type              enum(page, post, banner, faq, menu, policy)
  slug              string        # unique per (orgId, type, storeId)
  status            enum(draft, review, scheduled, published, archived)
  publishAt         timestamp?    # required when status = scheduled
  unpublishAt       timestamp?
  createdBy         userId
  createdAt         timestamp
  updatedBy         userId
  updatedAt         timestamp
  version           int           # incremented on every saved revision
  translations      map<locale, Translation>
  seo               SeoBlock
  meta              jsonb         # type-specific payload
```

```
Translation
  locale            string
  title             string
  body              richtext?     # HTML or portable-text JSON
  state             enum(missing, machine, draft, translated)
  updatedBy         userId
  updatedAt         timestamp
```

```
SeoBlock
  metaTitle         string(70)
  metaDescription   string(160)
  canonicalUrl      string?
  ogImageId         mediaId?
  noindex           boolean
```

### 1.3 Status model

`draft → review → scheduled → published → archived`, with `published → draft`
(unpublish) and `archived → draft` (restore) allowed.

Rules:

- Publishing requires the store's **default locale** translation to be complete.
- Non-default locales may lag; the item then publishes with fallback to the default locale.
- `scheduled` requires `publishAt` in the future; a scheduler promotes it to `published`.
- `archived` items are removed from storefront routes but keep their slug reserved
  so links can be redirected rather than 404.
- Every transition writes an audit entry (actor, from-status, to-status, timestamp, reason).

### 1.4 Revisions

Each save creates an immutable revision row `{itemId, version, snapshot, author, createdAt}`.

Requirements:

- Retain at least 50 revisions or 12 months per item, whichever is larger.
- `GET /revisions` for a diff view; `POST /revisions/{version}/restore` creates a new
  revision equal to the old snapshot (never mutates history).
- Concurrent editing is protected by optimistic locking: clients send
  `If-Match: <version>`; mismatch → `409 Conflict` with the current version.

### 1.5 Roles & permissions

| Capability | Owner | Admin | Editor | Translator | Viewer |
|---|---|---|---|---|---|
| Read content | ✓ | ✓ | ✓ | ✓ | ✓ |
| Create / edit draft | ✓ | ✓ | ✓ | – | – |
| Edit non-default locale only | ✓ | ✓ | ✓ | ✓ | – |
| Submit for review | ✓ | ✓ | ✓ | ✓ | – |
| Publish / schedule | ✓ | ✓ | – | – | – |
| Edit legal & policies | ✓ | ✓ | – | – | – |
| Delete / archive | ✓ | ✓ | – | – | – |
| Upload media | ✓ | ✓ | ✓ | – | – |
| Manage navigation menus | ✓ | ✓ | ✓ | – | – |

Permission checks are per store; a user may be Editor on one store and Viewer on another.

---

## 2. Dashboard aggregates

The console header shows four KPI cards and per-tab counts. One endpoint serves them.

`GET /v1/content/summary?storeId=…`

```json
{
  "publishedItems": 41,
  "drafts": { "total": 9, "staleOver30Days": 3 },
  "awaitingTranslation": { "total": 12, "byLocale": { "ar": 6, "fr": 3, "de": 3 } },
  "media": { "bytesUsed": 1932735283, "bytesQuota": 5368709120, "fileCount": 248 },
  "counts": {
    "pages": 14, "posts": 26, "banners": 8, "faq": 19,
    "media": 248, "menus": 2, "policies": 5
  }
}
```

Requirements:

- Values are **per store**, computed from the same predicates the list endpoints use, so
  the KPI and the list never disagree.
- `awaitingTranslation` counts items where the item is published (or scheduled) in the
  default locale and at least one enabled locale has `state != translated`.
- `staleOver30Days` counts drafts whose `updatedAt` is older than 30 days.
- Cacheable for 60 s per (org, store); invalidated on any publish/unpublish/upload/delete.

---

## 3. List, search, filter, bulk

All four list domains (pages, posts, banners, FAQ) share one list contract.

`GET /v1/content/{type}?status=&locale=&q=&folder=&page=&pageSize=&sort=`

| Param | Behaviour |
|---|---|
| `status` | `all` (default), `published`, `draft`, `review`, `scheduled`, `archived` |
| `locale` | `all` (default) or a locale code; filters on translation state |
| `q` | full-text over title, slug, body, SEO fields; prefix and fuzzy matching |
| `sort` | `updatedAt` (default desc), `title`, `status`, `publishAt` |
| `page`, `pageSize` | pageSize default 25, max 100 |

Response row carries exactly what the table renders: title, slug, status, per-locale
badge states, `updatedAt`, and the display name of `updatedBy`.

```json
{
  "items": [
    {
      "id": "…", "title": "About Acme Supply", "slug": "/pages/about",
      "status": "published",
      "locales": [
        {"code":"en","state":"translated"},
        {"code":"ar","state":"translated"},
        {"code":"fr","state":"translated"},
        {"code":"de","state":"missing"}
      ],
      "updatedAt": "2026-08-02T09:14:00Z",
      "updatedBy": {"id":"…","name":"Jordan Diaz"}
    }
  ],
  "page": 1, "pageSize": 25, "total": 14
}
```

### Bulk operations

`POST /v1/content/bulk` — `{ "ids": [...], "action": "publish|unpublish|archive|delete|assignStores|setLocaleState" }`

- Partial success is expected: return `207` with a per-id result array
  (`ok` / `error` with reason), never fail the whole batch on one bad id.
- Bulk publish enforces the same per-item validation as single publish.
- Maximum 200 ids per call.

---

## 4. Pages

Static storefront pages (`/pages/{slug}`).

```
meta:
  template        enum(default, full-width, landing, contact)
  blocks          Block[]        # ordered content blocks
  showInSitemap   boolean
  parentPageId    uuid?          # one level of nesting
```

`Block` is a discriminated union: `richText`, `image`, `gallery`, `video`, `productGrid`,
`faqGroupRef`, `bannerRef`, `htmlEmbed`, `spacer`, `cta`.

Requirements:

- Slug is generated from the title, kebab-cased, uniqueness-checked per store;
  `GET /v1/content/pages/slug-available?slug=` backs the live validation in the editor.
- Changing the slug of a **published** page auto-creates a 301 redirect from the old path.
  Redirects are stored and served by the storefront router; list at `GET /v1/redirects`.
- `htmlEmbed` blocks are sanitized server-side (allowlist of tags/attributes; scripts
  stripped unless the org has the `rawHtml` capability).
- Deleting a page with inbound menu links returns `409` listing the referencing menus
  unless `?force=true`.
- Draft preview: `POST /v1/content/pages/{id}/preview-token` returns a short-lived signed
  token; the storefront renders the unpublished draft at `/?preview=<token>`.

---

## 5. Blog posts

Editorial content (`/blog/{slug}`).

```
meta:
  excerpt         string(280)
  heroMediaId     mediaId?
  authorId        userId
  categoryIds     uuid[]
  tags            string[]
  readingMinutes  int            # derived, recomputed on save
  featured        boolean
  commentsEnabled boolean
```

Requirements:

- Categories and tags are first-class: `GET/POST/PATCH/DELETE /v1/content/post-categories`
  and `/post-tags`; deleting a category reassigns or orphans posts per `?onDelete=`.
- Scheduling is a hard requirement (the UI shows "Publishes Aug 9"): the scheduler must
  publish within 60 s of `publishAt`, be idempotent, and survive restarts.
- Feeds: the service exposes RSS/Atom and JSON feed documents per store and locale,
  regenerated on publish.
- `readingMinutes` derived at 220 words/minute from the default-locale body.
- Related posts: resolved by shared category then tag overlap, max 3, published only.

---

## 6. Banners & promos

Merchandising placements rendered by the storefront and the home page builder.

```
meta:
  placement       enum(hero, carousel, promoStrip, sidebar, popup)
  position        int            # order within the placement
  target          { kind: enum(collection, product, page, url), value: string }
  artwork         map<locale, { desktopMediaId, mobileMediaId, altText }>
  copy            map<locale, { headline, subhead, ctaLabel } >
  theme           { textColor, overlayOpacity, alignment }
  schedule        { startAt, endAt }
  audience        { segments: string[], countries: string[], loggedInOnly: boolean }
```

Requirements:

- Placement capacity is enforced: `hero` accepts 1 active banner per store at a time,
  `carousel` up to 8, `promoStrip` 1 sitewide. Exceeding capacity → `422` naming the conflict.
- Artwork validation on save: recommended dimensions per placement
  (hero 1920×900, carousel 1600×640, promo strip 1600×200); accept JPG, PNG, WebP up to 5 MB;
  warn (not block) on mismatch; require `altText` in the default locale before publish.
- Overlapping schedules within one placement resolve by `position`, then most recent
  `publishAt`; the API must expose which banner wins: `GET /v1/content/banners/effective`.
- Impression and click counters increment through a fire-and-forget endpoint
  `POST /v1/content/banners/{id}/events` and are surfaced as daily aggregates.

---

## 7. FAQ entries

Grouped question/answer content shown on the storefront and in support surfaces.

```
FaqGroup   { id, name, position, storeIds, translations }
FaqEntry   { id, groupId, position, translations{question, answer}, status,
             linkedProductIds, linkedPageIds, helpfulYes, helpfulNo }
```

Requirements:

- Groups are ordered, and entries are ordered within a group; the UI shows
  "Delivery · position 1". Reordering is a single call:
  `PATCH /v1/content/faq/reorder` with `[{id, groupId, position}]`, applied atomically.
- Moving an entry between groups renumbers both groups' positions server-side.
- Helpfulness votes are anonymous, rate-limited per IP/session, and exposed as aggregates.
- Entries can be attached to products or pages so the storefront can render a contextual
  FAQ block; those references are validated on save and cleaned up when a target is deleted.
- FAQ content is exposed as `FAQPage` JSON-LD by the storefront; the API returns
  the structured-data payload alongside published entries.

---

## 8. Media library

Binary asset storage plus metadata, folders, and usage tracking.

```
MediaAsset
  id, orgId, storeIds
  filename, originalFilename
  mimeType, kind enum(image, video, document, archive, vector)
  bytes, width?, height?, durationMs?, pageCount?
  folderId
  altText map<locale, string>
  title, caption, tags[]
  checksum sha256
  variants  [{ name, width, height, bytes, url, format }]
  uploadedBy, uploadedAt
  usage    [{ type, itemId, itemTitle, field }]
```

### Upload

1. `POST /v1/media/upload-intent` → `{ uploadUrl, assetId, expiresAt }` (presigned, direct-to-storage).
2. Client PUTs the bytes.
3. `POST /v1/media/{assetId}/complete` → server verifies size/type/checksum, probes
   dimensions, enqueues derivative generation, returns the asset.

Requirements:

- Accepted types: JPG, PNG, WebP, SVG, MP4, PDF, ZIP. Per-file limit **50 MB**
  (banners artwork limited to 5 MB at the banner layer).
- Storage quota per plan (Free = 5 GB); uploads that would exceed quota fail with `413`
  and a payload stating used/quota so the UI can prompt an upgrade.
- Derivatives generated for images: `thumb 320`, `card 640`, `hero 1600`, `full 1920`,
  each in WebP plus the original format; videos get a poster frame; PDFs get a first-page thumbnail.
- SVG uploads are sanitized (scripts, external references and event handlers stripped).
- Deduplication by checksum within an org: re-uploading an identical file returns the
  existing asset instead of storing a second copy.
- Malware scan on completion; assets stay `quarantined` and unservable until clean.
- Serving is CDN-backed with immutable, content-hashed URLs; `GET /v1/media/{id}/url`
  returns the public URL for the requested variant.

### Folders

`GET/POST/PATCH/DELETE /v1/media/folders` — flat or one-level nesting, per store.
Default folders match the console: Banners, Product shots, Brand assets, Video, Documents.
Counts returned per folder. Deleting a non-empty folder requires `?moveTo=<folderId>`.

### Usage tracking and deletion

- The service maintains a reverse index of every reference to an asset (pages, posts,
  banners, products, policies, menus), so the UI can show "used on 3 pages" / "unused".
- Deleting a referenced asset returns `409` with the reference list unless `?force=true`;
  forced deletes replace references with a placeholder and log the action.
- `GET /v1/media?used=false` powers an "unused files" cleanup view.

### Listing

`GET /v1/media?folder=&kind=&q=&sort=&page=` — supports grid and list views,
type filter ("All types"), free-text search on filename, title, tags and alt text.

---

## 9. Navigation menus

Storefront navigation (Main menu, Footer menu), drag-ordered, one level of nesting.

```
Menu      { id, handle, name, storeIds, translations{name}, items[] }
MenuItem  { id, parentId?, position, label map<locale,string>,
            target { kind: enum(page, collection, product, policy, url, blogIndex), value },
            openInNewTab, visible, requiresLogin }
```

Requirements:

- Nesting is limited to **two levels** (parent + children); deeper writes → `422`.
- `PUT /v1/content/menus/{id}` replaces the whole tree atomically (the drag-and-drop UI
  saves the full order); positions are normalized server-side to 0..n.
- Internal targets are validated on save; a target pointing at an archived or deleted
  item is flagged `broken` in the response so the UI can warn, but does not block saving.
- Labels fall back to the default locale when a translation is missing.
- Menus are cached aggressively and invalidated on save; the storefront reads a single
  resolved document per store+locale: `GET /v1/content/menus/resolved?handle=main`.

---

## 10. Legal & policies

Policy documents linked from the storefront footer and shown at checkout.

```
Policy
  id, orgId, storeIds
  type enum(terms, privacy, returns, shipping, cookies, custom)
  translations map<locale, { heading, body }>
  status
  effectiveFrom timestamp
  requiresAcceptance boolean       # forces re-acceptance at checkout on new version
  displayAt        { footer, checkout, signup }
  jurisdiction     string          # e.g. "NL" — drives template selection
  version int
```

Requirements:

- One active policy per `type` per store; publishing a new version supersedes the previous
  one and keeps it readable at a versioned URL (`/policies/returns?v=3`) for audit.
- Templates: `GET /v1/content/policy-templates?type=&jurisdiction=` returns starter text
  (the editor offers "Start from the … template for the Netherlands").
- Required-policy check: the service knows which policy types are mandatory for the
  store's selling regions and reports gaps (the console shows Cookie notice as
  "Not written — required in EU regions"):
  `GET /v1/content/policies/compliance` → `[{type, requiredBy: ["EU"], status}]`.
- When `requiresAcceptance` is true and a new version publishes, the service emits
  `policy.version.published`; checkout must re-prompt acceptance and store
  `{customerId, policyId, version, acceptedAt, ip}` — retained for 7 years.
- Policy bodies are sanitized rich text; no scripts, no external embeds.
- Policy text is immutable once published — edits create a new version, never mutate.

---

## 11. Translation workflow

- Locales are configured per store: `GET/PUT /v1/stores/{id}/locales`
  → `[{code, name, default, enabled, fallback}]`.
- Every translation carries its own `state` so the UI can render per-locale badges
  (green = translated, grey = missing/draft).
- `GET /v1/content/translations/queue?locale=ar` returns everything awaiting translation
  for that locale, ordered by publish status then update time.
- `POST /v1/content/{type}/{id}/translations/{locale}` writes one locale without touching
  others, so a Translator role can work in isolation.
- Optional machine-translation pass: `POST …/translate` fills `state = machine`,
  which never counts as translated for publish gating and is visually distinct.
- Changing the default-locale body marks dependent translations `stale` (still published,
  flagged for review) rather than reverting them to missing.

---

## 12. Storefront read API

Separate, cache-optimized, read-only surface used by the storefront renderer.
Never exposes drafts except with a valid preview token.

| Endpoint | Returns |
|---|---|
| `GET /storefront/v1/pages/{slug}` | published page + resolved blocks + SEO |
| `GET /storefront/v1/posts?page=&category=&tag=` | paginated published posts |
| `GET /storefront/v1/posts/{slug}` | single post + related posts |
| `GET /storefront/v1/banners?placement=` | effective banners for the placement, audience-filtered |
| `GET /storefront/v1/faq?group=` | grouped published entries + JSON-LD |
| `GET /storefront/v1/menus/{handle}` | resolved menu tree |
| `GET /storefront/v1/policies/{type}` | active policy version |
| `GET /storefront/v1/sitemap.xml` | all published, indexable routes per locale |

Requirements:

- Responses carry `ETag` and `Cache-Control: public, s-maxage=300, stale-while-revalidate=60`.
- Publish/unpublish emits targeted cache purges by surrogate key
  (`store:{id}`, `type:{type}`, `item:{id}`).
- All reads are locale-aware with fallback to the store default; the response states which
  locale actually served each field.
- p95 latency target ≤ 120 ms warm, ≤ 400 ms cold.

---

## 13. Events

Published to the org's event bus; also available as outbound webhooks.

```
content.item.created          content.item.updated
content.item.published        content.item.unpublished
content.item.scheduled        content.item.archived
content.item.deleted
content.translation.updated   content.translation.stale
media.asset.uploaded          media.asset.deleted
media.quota.threshold         # 80% and 95% of plan quota
menu.updated
policy.version.published
banner.schedule.started       banner.schedule.ended
```

Payloads include `orgId`, `storeIds`, `type`, `id`, `version`, `actorId`, `occurredAt`.
Delivery is at-least-once with an idempotency key; consumers must dedupe.

---

## 14. Non-functional requirements

**Validation & safety**
- All rich text sanitized server-side on write, not on read.
- Slugs, handles and menu targets validated against a strict pattern.
- Request bodies capped at 1 MB (media goes through presigned upload, never the API).

**Search**
- Full-text index over title, slug, body, excerpt, SEO fields, media filename, tags.
- Index updates within 5 s of a write; search must never return another org's content.

**Rate limits**
- 600 read req/min per org; 120 write req/min; 20 upload intents/min.
- Bulk endpoints count as one request but are capped at 200 items.

**Audit**
- Every write records actor, IP, user agent, before/after diff summary.
- `GET /v1/content/audit?itemId=&actorId=&from=&to=` with 12-month retention minimum.

**Soft delete**
- Deletes are soft for 30 days with a restore endpoint; hard purge runs after that,
  including media bytes.

**Observability**
- Metrics per endpoint (latency, error rate), scheduler lag, derivative queue depth,
  storage bytes per org, translation queue size.
- Alerts on scheduler lag > 5 min, derivative queue > 500, quota threshold events.

**Data residency & export**
- Content and media stored in the org's chosen region.
- `POST /v1/content/export` produces a JSON + media archive of everything for the org;
  `POST /v1/content/import` restores it (used for store cloning and migrations).

---

## 15. Open questions

1. Should blog comments be in scope, or handled by a third-party embed?
   (`commentsEnabled` is modelled but no comment storage is specified.)
2. Banner audience targeting depends on a customer-segments service — is that available,
   or do we ship with country + logged-in targeting only?
3. Is machine translation in scope for v1, or is the translation queue manual-only?
4. Do policies need per-store variants, or is one org-wide policy set per jurisdiction enough?
5. Approval workflow depth: is single-step review (Editor → Admin publishes) sufficient,
   or is a multi-approver chain required?
