# Storefront Home-Page Builder

## Context

Every theme's home page today is hard-coded (`themes/*/src/pages/Home.tsx`: Hero + `data.groups.map(...)`), so all stores look structurally identical. We want a drag-and-drop builder in console-ui (design mockup: `store-core/console-template/Storefront Builder.dc.html`) that stores the page layout/config in DB and has the storefront render it dynamically — with real per-theme uniqueness.

**Exploration found most of the backend already exists**: `store-pod/content` is a full CMS with `SECTION` rows (`meta jsonb`), CRUD + `PATCH reorder` (`SectionApi`), `GET /api/v1/storefront/home-sections`, draft/publish workflow, revisions, preview tokens (`support/PreviewTokens.java`), and media library. But: no console UI edits sections (`sections.service.ts` has zero consumers), and **no theme renders `data.sections`** — `storefront/src/shell/loaders/home.ts` only uses them to pick product-group codes. The builder nav item is already stubbed disabled at `store-management.html:52-59` with i18n keys in place.

**User decisions (locked):**
1. **Scope: home page only** (model designed to extend to other pages later).
2. **Persistence: new `page_layout` JSONB document table** replacing the SECTION rows flow (drop/recreate freedom, no migration).
3. **Uniqueness: kinds + variants per theme** — themes register renderers per kind+variant, may add exclusive variants; builder shows only what the active theme supports; shell fallbacks render everything everywhere.
4. **Canvas: live storefront iframe** via preview token + postMessage bridge.
5. **Competitor-inspired scope (from research on Shopify OS 2.0 / Wix / Salla-Zid):** blocks/items inside sections, section presets in the Add library, and merchant-saved reusable sections. (Per-section scheduling deferred.)

## Architecture: one coherent content model (not a JSONB workaround)

The user's concern: layout-as-JSONB must not become a second, competing content system beside the existing SECTION/banner/FAQ features. Resolution — a strict ownership boundary, with the old overlap deleted:

- **The layout document owns arrangement and page-owned copy.** Section order, variant, props, items (slides, USP entries, testimonial quotes), and localized text that exists only as part of this page live inline in the document. This is genuinely document-shaped data: ordered, atomically published, versioned/undone as a whole, never queried per-field — exactly what JSONB is for (same model as Shopify JSON templates). Storing it as per-section rows is what exists today, and it's why reorder, publish-atomicity, and undo were awkward — the old `content_type='SECTION'` rows were layout data shoehorned into the content table. They get **deleted**, not wrapped.
- **Content entities own reusable, individually-managed things.** FAQ documents, blog posts, policies, menus, product groups/categories (catalog), and the media library stay relational rows with their own workflow — the layout references them by slug/code. A layout never copies their content; a section renders whatever the referenced entity currently publishes.
- **Media library is the only asset store.** Every image/video in a layout is a `mediaId` ref; `media_usage` tracking covers layouts like it covers banners today.
- **Hero/slider slides move inline.** Instead of hero sections referencing banner rows (`BANNER_REF`, a double indirection the merchant has to manage in two screens), hero/slider slides are inline items — image (media ref) + heading + CTA — edited right in the builder. Consequence: the Banner entity's `HERO`/`CAROUSEL` placements retire with the SECTION flow; the Banner type survives v1 only for the announcement `STRIP` and `COLLECTION` placements, which are outside the home-body builder's scope (a future header/footer or category-page builder absorbs them, then Banner can go entirely).
- **Saved reusable sections are snapshots, not live references.** A merchant-saved section is a copy-on-insert template (own small table), so no cross-page invalidation graph appears.

End state: no duplicated model, no orphaned features — layout (document) / entities (rows) / assets (media), each with one owner.

### New stores: every store always has a layout — one rendering path

A brand-new store never renders a special "no layout" branch. On first read (`loadOrDefault`) the content service materializes a **default layout document** from a catalog-defined "starter home" preset — designed to look complete with zero merchant data:

1. `hero.minimal` — text slide (store name + tagline) over a tone background: needs no media.
2. `categories.grid` — renders whatever categories exist.
3. `products` (source: featured group) and `products` (source: newest).
4. `richtext.centered` — welcome copy (localized defaults).
5. `newsletter.inline`.

Empty-data rule: on the published storefront, a section whose data resolves empty (no products, no categories) **collapses silently**; in the builder canvas it renders with an "empty — add products" hint. So a day-one store shows hero + welcome + newsletter and grows as data arrives.

Consequence — **the legacy path is deleted, not kept as a fallback**: since a layout always exists, the shell always composes home from it. `theme.pages.Home`, `HomeData`, and the hard-coded Hero+groups pages in all 12 themes are removed in Phase 2. The 8 designed themes temporarily render home via shell fallbacks (acceptable pre-launch), and their bespoke home looks are recaptured properly as theme variants + presets in Phase 4 (fashion's interleaved-slider home becomes a fashion preset, etc.). One pipeline, no dual code path.

### Legacy hard-coded product groups

Today `home.ts` whitelists `GROUPS = [FEATURED_ITEMS, NEWLY_ADDED, HOME_PAGE, RECOMMENDED]` and special-cases `code === 'HOME_PAGE'`. Decision:

- **Keep the catalog `ProductGroup` entity** — it also powers per-product `RELATED_ITEM`, and named curated sets are genuinely useful.
- **Delete the whitelist and the `HOME_PAGE` special-case.** The `products` section gets a real source model in `props`: `source: {type: 'group' | 'category' | 'manual' | 'newest', code? | codes? | productIds?, limit}` — `group` lists the store's groups dynamically from the catalog API (`ref:product-group` field, no hard-coded codes), `category` pulls a category's products, `manual` is a product picker, `newest` uses catalog date sort. Shopify's "featured collection" equivalent, superset of today.
- **Retire the seeded `HOME_PAGE` group** (a manual-source section replaces its purpose); `FEATURED_ITEMS`/`NEWLY_ADDED`/`RECOMMENDED` remain ordinary named groups a merchant may point a section at (`NEWLY_ADDED` seeding can go too once `newest` source exists — prefer computed over curated for that case).

## The layout document (heart of the design)

One JSONB doc per (store, page), draft + published copies:

```json
{
  "schemaVersion": 1,
  "page": "HOME",
  "sections": [
    {
      "id": "sec_h7f3k2",
      "kind": "hero",
      "variant": "carousel",
      "props": { "autoplay": true, "interval": 5, "height": "lg" },
      "items": [
        {
          "id": "itm_a1",
          "props": { "mediaId": 1041, "link": { "type": "category", "code": "spring" } },
          "text": { "heading": {"en": "Spring edit", "ar": "…"}, "cta": {"en": "Shop now"} }
        }
      ],
      "text": { "title": {"en": "…"} },
      "style": { "spacing": "md", "width": "content", "tone": "default" },
      "visibility": { "hidden": false, "devices": ["desktop","tablet","mobile"] },
      "anchor": "spring"
    }
  ]
}
```

- `id`: client-generated stable string — DnD key, postMessage select/hover, `data-section-id` in DOM.
- `props`: per-kind/variant machine props; references to entities use **business codes** (product-group code, category code, faq slug), not row ids.
- `items[]`: first-class repeatable blocks (Shopify-style), each with its own `props`/`text`, own field schema per kind, add/remove/reorder in the inspector. Bounded (50/section).
- `text`: localized copy as JSONB locale maps (matches `menu.names` / `site_settings.seo` precedent).
- `style`: common knobs — `spacing` (none/sm/md/lg), `width` (content/wide/full), `tone` (default/muted/inverse); themes interpret loosely.
- Flat section list in v1; `items` covers one nesting level; container kinds remain possible later.

**v1 kind catalogue** (mockup's 12 kinds mapped to real capabilities):

| kind | data | items? | fallback variants |
|---|---|---|---|
| `hero` | inline slides (media refs) | slides | classic, split, carousel, minimal |
| `products` | source: group/category/manual/newest | — | rail, grid |
| `categories` | category tree | — | grid, pills |
| `promo` | media + text/CTA | — | strip, card |
| `image` | media ref | — | full, contained |
| `richtext` | `text.body` per locale | — | default, centered |
| `faq` | FAQ document (slug ref) | — | accordion |
| `posts` | blog posts (ref) | — | cards |
| `testimonials` | inline | quotes | cards, quotes |
| `newsletter` | inline | — | inline, boxed |
| `usp` | inline | badges | row |
| `video` | url or media ref | — | embed |
| `brands` | inline | logos | row |

(Mockup's "slider" = `hero.carousel`.) Unknown variant → kind's first fallback variant.

## Theme capability manifest — decision

**TypeScript registry in each theme + a landing-ui route `GET /api/theme-manifest` the console fetches from the storefront origin.**

- Shared catalogue `libs/theme/src/sections/catalog.ts`: canonical JSON-serializable kinds, fallback variants, **presets**, and **inspector field schemas** (small DSL): `{key, type: 'text'|'textarea'|'richtext'|'select'|'media'|'toggle'|'range'|'color'|'link'|'ref:product-group'|'ref:category'|'ref:faq'|'ref:post-category', label, localized?, min?, max?, options?, visibleIf?: {key, equals}}`. Kinds with items also declare `itemFields` + item limits.
- **Presets**: catalog (and themes) declare presets per kind — `{id, label, kind, variant, props, items, text}` with finished-looking demo defaults; the Add-section library offers presets, not bare kinds, so a new section never lands empty.
- `ThemeDefinition` gains optional `sections?: Partial<Record<SectionKind, Record<VariantId, ComponentType<SectionRenderProps>>>>` (+ optional field/preset additions), validated in `define-theme.ts`.
- New route `storefront/src/app/api/theme-manifest/route.ts`: resolves active theme via existing `get-theme.ts`, merges theme registry over the catalogue, returns `{themeId, kinds:[{kind, variants:[{id, source, fields, itemFields?}], presets:[…]}]}`. Public, `max-age=60`.
- Rationale: the manifest reads the very registry the renderer uses — zero drift; console already needs the storefront origin for the iframe anyway.


## Status (2026-09-01)

Phases 0-6 are implemented, committed and live-QA'd on the `storefront-builder` stack. Phase 6 canvas
parity verified in the browser end to end: select/hover sync both ways, scrollTo, floating toolbar,
in-canvas grip reorder, library drag→drop with insertion targeting, add-here zones, guides, locks
(with inspector lockdown), Alt+Arrow + Delete, undo/redo, publish + revisions drawer, 409 conflict
bar recovery, RTL chrome. The builder now runs shell-free at its own top-level route (full viewport).

Phase 7 batch 1 done: registries for fashion (wall hero recaptured), grocery, pink, hunger,
furniture (DirectoryBoard as `categories`); `slidesAsBanners` shared via @store-front/theme; fallback
audit fixes (drawn usp icons, translated newsletter copy); duplicate policy links dropped from all 12
footers; "Section design rules" contract in themes/README.md. Phase 5's deferred retirements are now done too: BannerPlacement is COLLECTION+STRIP only (schema,
seeds, tests, console, landing types), the seeded HOME_PAGE group is gone from all four catalog
stores, and the builder route has a confirm-leave guard. Phase 7 verification complete: per-theme audit pass (registries delegate to each theme's own
Named-Rules components) and the screenshot matrix — all five themes desktop+mobile en, fashion ar
desktop, furniture ar mobile — clean, RTL fully mirrored. The `manual` product source and newsletter
list wiring stay future work. The branch is ready for PR via /go.

Post-review addition — **the section alignment architecture** (user feedback: themes read
inconsistent): section semantics moved into unit-tested models (`libs/theme/src/sections/models.ts`),
structure into a composer (`libs/ui/src/sections/compose.tsx`), and each theme now supplies only a
`SectionChrome` of voice primitives plus bespoke hero/products overrides on the models. The shell
fallbacks run the same composer with a neutral chrome. All seven designed themes cover all 13 kinds;
hero autoplay/interval, image alt/caption/link and promo background artwork are honored everywhere.

## Phase 0 — Worktree setup (per AGENTS.md worktree-per-feature rules)

The current checkout sits on `feat/product-variants` with uncommitted work — none of this feature touches it. All work happens in a fresh worktree cut from up-to-date `main`:

```bash
git fetch origin
git worktree add .claude/worktrees/feat-storefront-builder -b feat/storefront-builder origin/main
```

- All code, builds, and QA happen inside that worktree; its own stack: `lcl start -d --stack storefront-builder` run from inside the worktree. Never assume ports — read them from `lcl urls` / `lcl ports --stack storefront-builder`, and always pass `--stack`.
- Copy this plan into the worktree as `.AGENTS/plans/storefront-builder.md` (in-repo plan location per AGENTS.md) as the first commit's companion.
- Ship phases via `/go` from the worktree (PRs into `main`); after merge: `lcl stop --stack storefront-builder`, `git worktree remove`, delete the branch.

## Phase 1 — Backend (content service)

**Schema** (`content-service/src/main/resources/init-sql/schema.sql`):

Why two document columns: `draft` is the builder's working copy (autosave target, never served to shoppers); `published` is what the storefront serves. Publish = validate + copy draft→published in one transaction + snapshot to `page_layout_revision`; discard = copy published→draft. Same staging pattern as the existing `content_revision.snapshot`, without a status machine over partial rows.

```sql
create table content.page_layout (
    id bigserial primary key,
    date_created timestamp, last_modified timestamp, modified_by varchar(100),
    store varchar(100) not null,
    page varchar(32) not null,
    draft jsonb not null,
    published jsonb,
    draft_version integer not null default 1,
    published_version integer, published_at timestamp,
    unique (store, page)
);
create table content.page_layout_revision (
    id bigserial primary key,
    layout_id bigint not null references content.page_layout(id) on delete cascade,
    version integer not null, snapshot jsonb not null,
    published_by varchar(100), date_created timestamp,
    unique (layout_id, version)
);
create table content.section_preset (       -- merchant-saved reusable sections (copy-on-insert)
    id bigserial primary key,
    date_created timestamp, modified_by varchar(100),
    store varchar(100) not null,
    name varchar(120) not null,
    kind varchar(40) not null,
    snapshot jsonb not null                  -- one LayoutSection subtree, ids re-generated on insert
);
```

**New files** (following -commons/-core/-service pattern, ArchUnit-clean):
- `content-commons/.../model/layout/`: `LayoutDocument`, `LayoutSection`, `LayoutItem`, `PersistableLayout`, `ReadableLayout`, `LayoutMeta`, `PageKind` (HOME only), `SavedSection` — plain records.
- `content-core/.../entity/PageLayoutEntity.java`, `PageLayoutRevisionEntity.java`, `SectionPresetEntity.java` — `@JdbcTypeCode(SqlTypes.JSON)` String + existing `JsonCodec`.
- `content-core/.../repository/` for the three.
- `content-core/.../service/PageLayoutService.java` — `loadOrDefault` materializes the starter-home default document (see Architecture) on first read; save draft with optimistic `draft_version` check (mismatch → 409); publish (validate → copy draft→published, bump version, snapshot revision); discard (published→draft); revisions restore into draft. `SectionPresetService` for saved sections.
- `content-core/.../service/binding/LayoutBinding.java` — modeled on `SectionBinding.java`: media_usage sync over sections AND items; `publishProblems()` (unknown kind, missing media/faq block; catalog refs warn-only — storefront tolerates missing refs).
- `content-core/.../facade/PageLayoutFacade.java`; extend `StorefrontFacade.java` with `layout(store, language, page)`.
- `content-service/.../api/v1/LayoutApi.java`:
  - `GET/PUT /api/v1/private/content/layouts/{page}` (draft + meta / save draft)
  - `POST .../publish`, `.../discard`
  - `GET .../revisions`, `POST .../revisions/{version}/restore`
  - `POST .../preview-token` (reuse `PreviewTokens`)
  - `GET/POST/DELETE /api/v1/private/content/section-presets` (saved sections)
  - Permissions: `ContentPermissions.READ`/`MANAGE`; params `StoreMerchantId, LanguageCode`.
- `StorefrontApi.java`: `GET /api/v1/storefront/layout/{page}` — published, `max-age=60, stale-while-revalidate=60`; `?preview=<token>` → draft, `no-store`.
- Seed: replace `init-sql/stores/*/10-sections.sql` with `10-layout.sql` (translate SECTION rows to a layout doc; banner slides become inline hero items pointing at the same media); demo store `65f020632bc46470c104b76f` gets all 13 kinds.
- `.http` files under `extra/requests/` per repo convention.

**Legacy retirement** (staged — delete in Phase 5 once storefront reads layouts): `SectionApi`, `SectionBinding`, section models + `HomeSectionKind` in content-commons, `homeSections` in `StorefrontFacade`/`StorefrontApi`, console `sections.service.ts`, `'SECTION'` from the content_type check, and the Banner `HERO`/`CAROUSEL` placements (Banner keeps `STRIP`/`COLLECTION` only; console banners tab narrows accordingly).

## Phase 2 — Storefront rendering (landing-ui)

- `libs/types/src/layout.ts`: `PageLayoutDocument`, `SectionInstance`, `SectionItem`, `SectionKind`, `LocalizedText`, resolved-data types.
- `libs/services/src/content-service.ts`: `getPageLayout(ctx, page, previewToken?)`.
- `libs/theme/src/contract.ts` + `define-theme.ts`: optional `sections` registry; `SectionRenderProps {ctx, section, data}`. New `libs/theme/src/sections/catalog.ts` (kinds, variants, fields, presets).
- `storefront/src/shell/loaders/home.ts`: rewritten — fetch layout (always exists; preview token from `searchParams`, page becomes no-store in preview); walk sections, collect referenced codes/mediaIds/product sources, batch-fetch grouped by service in one `Promise.all`; produce `HomeLayoutData {layout, resolved: Record<sectionId, payload>}`. The `GROUPS` whitelist and `HOME_PAGE` special-case are deleted.
- Renderer pipeline `storefront/src/shell/sections/`:
  - `resolve-renderer.ts`: theme `kind.variant` → theme `kind` first variant → shell fallback → skip w/ dev warning.
  - `section-list.tsx`: server component; skips hidden; wraps each in `<section id={anchor} data-section-id data-section-kind>`; device visibility via responsive classes.
  - `fallbacks/<kind>.tsx`: 13 fallback renderers from `libs/ui` (copy `default-search-page.tsx` precedent), item-aware (hero slides, USP badges, testimonials, brand logos); empty-data sections collapse (render nothing) outside preview, show an "empty" hint in preview.
- **Single path:** the shell always composes home from the layout inside `theme.layout.Root`. `theme.pages.Home`, `HomeData`, and every theme's `pages/Home.tsx` are deleted here (contract's `ThemePages.Home` removed; `define-theme.ts` updated; 12 themes touched mechanically). Bespoke home designs return as theme variants/presets in Phase 4.
- **Builder bridge** `storefront/src/shell/sections/builder-bridge.tsx` (client, preview-mode only): validates `event.origin` against `NEXT_PUBLIC_BUILDER_ORIGINS`; in: `select|hover|scrollTo`; out: `ready`, `sectionClicked`, `height`. Outline CSS only in preview.
- `storefront/src/app/api/theme-manifest/route.ts`.
- Theme edits here are mechanical only (delete `pages/Home.tsx` + its registration in all 12 themes); no new theme design work — fallbacks render every kind everywhere until Phase 4.

## Phase 3 — Console builder MVP (console-ui)

- `src/app/api/content/layouts.service.ts` (over `crud.service.ts`, `CONTENT_PRIVATE` base): layout CRUD/publish/discard/revisions/previewToken + section-presets; theme-manifest fetch against storefront origin (origin from merchant router/domain services — closes the "console lacks the storefront host" gap noted in `page-editor.ts:41`).
- `src/app/features/storefront-builder/` (three-pane per the mockup):
  - `storefront-builder.ts/.html/.css` — top bar: back, device switcher, language switcher (store languages), undo/redo, guides toggle, Preview, Publish, dirty label.
  - `builder-facade.ts` — signals: `doc`, `selectedId`, `hoveredId`, `dirty`, `device`, `lang`, `saving`; undo/redo = bounded (50) JSON snapshot stacks; single `apply(mutator)` entry point. Section ops: add (from preset), duplicate, remove, reorder, save-as-preset.
  - `panels/layer-list/` — **@angular/cdk/drag-drop** (installed, unused; shared `tree` is menu-shaped, don't reuse); eye toggle per mockup.
  - `panels/section-library/` — two groups from the manifest + saved presets: **Presets** (catalog/theme presets, finished-looking) and **My sections** (saved reusable sections, insert = deep copy with new ids, save/delete). Click-or-drag insert.
  - `panels/inspector/` — generated from field DSL incl. `visibleIf`; typed field components under `fields/` (text, textarea, select, media reusing `shared/ui/image-picker`, toggle, range, color, link, ref-lookups). **Items editor**: cdk-drag list of item cards with add/remove/reorder, each expanding to its `itemFields` form. Localized fields edit `text[key][lang]` for the active language.
  - `canvas/preview-frame.ts` — iframe at storefront `/{lang}/?preview=<token>`; device switcher sets width (full/768/390); postMessage bridge with origin checks both ways; canvas click → select; layer select → scrollTo.
- **Save strategy: debounced draft PUT (~2.5s) + iframe reload on save success** ("Saving…/Saved" indicator). Publish → confirm dialog → flush pending save → publish → surface `publishProblems` warnings.
- Route `store-management/builder` in `app.routes.ts` (guards + `canManageContent()`); enable stubbed nav item `store-management.html:52-59`; update `content-hub.ts:36`, `section-nav.ts:35`; i18n `builder.*` in en.json + ar.json.

## Phase 4 — Theme variant sets

- Real `sections` registries for **starter, basic, beauty** (`themes/<id>/src/sections/*.tsx` + registration); beauty adds ≥1 exclusive variant (e.g. `hero.editorial`) and ≥1 themed preset to prove the mechanism.
- Update `scripts/new-theme.mjs` to scaffold `sections: {}`.
- Remaining real themes keep fallbacks (fast-follow).

## Phase 5 — Polish + retirement

- Undo/redo keybindings, dirty `CanDeactivate` guard, revisions drawer, publish-problem badges in layer list, guides toggle via bridge.
- Delete the legacy SECTION flow + HERO/CAROUSEL banner placements (list in Phase 1) + landing-ui `HomeSection` types; retire the seeded `HOME_PAGE` (and optionally `NEWLY_ADDED`) product groups in catalog seeds.
- Docs: `themes/README.md`, `PRODUCT.md`.

## Risks

- **Iframe cross-origin:** preview token in the URL is the entire credential (no cookies needed); postMessage origins: console gets the storefront origin from the domain list, storefront validates `NEXT_PUBLIC_BUILDER_ORIGINS`. Storefront preview responses must send `Content-Security-Policy: frame-ancestors <console origins>` (and no `X-Frame-Options: DENY`) so the canvas can embed it. Verify locally that the storefront host resolves in the console user's browser (`configure-domain.sh` /etc/hosts). **Escape hatch (first-class, not an afterthought):** the top-bar Preview button opens the draft in a new tab at `/{lang}/?preview=<token>` — if embedding fails in any environment, the builder remains fully usable via the layer list + new-tab preview.
- **Autosave vs publish race:** publish carries `draft_version`; 409 on stale; builder flushes debounce first.
- **Preview token expiry:** re-mint on iframe error / ~25-min timer (tokens are 30-min HMAC).
- **Theme switch after building on exclusive variants:** variant falls back to kind default; builder shows "not supported by this theme" note.
- **Per-section data fan-out:** loader dedupes codes, groups per service; published path stays cacheable; watch p95 with the 13-section seed.
- **Document bloat:** items bounded (50/section, 25 sections/page — Shopify's own limits); server rejects oversize documents (e.g. >256KB).

## Verification

All QA runs against the worktree's own stack (`lcl start -d --stack storefront-builder` from inside the worktree; live ports from `lcl urls`), and per repo convention includes tenant isolation (repeat as a second store) and the permission gate (no token → 403).

- **Phase 1:** stack up; `.http` round-trip GET default draft → PUT → publish → storefront GET; preview token returns draft with `no-store`; 409 on stale version; section-preset save/list/insert; ArchUnit green; seeded store returns 13-kind layout.
- **Phase 2:** demo store home renders seeded layout on starter AND an untouched scaffold theme (pure fallbacks), inline hero slides included; `?preview=` shows draft; `?theme=` cookie switch keeps rendering; ar/RTL sane; `curl /api/theme-manifest` with Store-Id header returns merged manifest with presets.
- **Phase 3:** browser QA on demo store: add-from-preset/duplicate/reorder/edit/delete sections; item add/reorder inside hero and USP; save-as-preset then re-insert it; debounced PUT visible in network tab; iframe reflects saves; canvas↔layer selection both ways; publish then verify live storefront (no preview param); read-only content user blocked from route.
- **Phase 4:** beauty-store manifest lists exclusive variant + preset, builder shows them only there; theme switch → graceful fallback, no crash.


## Phase 6 — canvas parity: the complete drag-and-drop builder

### Why

Phases 1–5 delivered a working builder; the `Storefront Builder.dc.html` design promises more: the
canvas itself as the editing surface — drag sections onto it and around it, edit-in-place chrome
(name tags, floating toolbar, add-here zones, guides), locks, and page-level links. This phase closes
every remaining gap with the design and hardens the editor.

The constraint shaping everything: the canvas is the real storefront in a **cross-origin iframe**.
No console DOM, no cdk drag, no shared event loop can reach into it. Every canvas affordance is
therefore split in two: **the bridge draws and reports; the console decides and writes.**
`builder-bridge.tsx` (landing-ui, mounted only in preview) owns all in-iframe overlays and pointer
tracking; `preview-frame.ts` (console) owns the protocol; `builder.facade.ts` stays the single writer
of the document. The bridge never mutates anything — it emits intents.

### 6.0 Feature inventory (design → mechanism)

| Design feature | Where it lives | Mechanism |
|---|---|---|
| Drag a library tile onto the canvas | console + bridge | native HTML5 drag in console; pointer forwarded over the bridge (§6.2a) |
| Insertion line following the pointer | bridge overlay | boundary math on section rects (§6.2c) |
| Reorder sections by dragging on the canvas | bridge only | in-iframe pointer drag from the toolbar grip (§6.2e) |
| Floating toolbar on the selected block (↑ ↓ ⧉ 🗑 + grip) | bridge overlay → intents | `toolbar` messages → facade ops (§6.3) |
| Per-block name tag on hover/selection | bridge overlay | `sectionHovered` mirror + guides mode (§6.4) |
| “Add section here” dashed zones | bridge overlay → intents | `addHere {beforeId}` → targeted insert (§6.5) |
| Guides toggle (outline everything) | top bar → bridge | `guides {on}` (§6.4) |
| Hover sync layer list ⇄ canvas | both | `hover` in, `sectionHovered` out (§6.4) |
| Reorder in the layer list | console (exists) | cdk drag — unchanged |
| Move up/down from the layer row keyboard | console | Alt+ArrowUp/Down on the focused row |
| Delete key removes the selected section | console | window keydown, guarded like undo (not in inputs, not locked) |
| Section lock (🔒, everything disabled) | schema + console + bridge | additive `locked` flag (§6.6) |
| Page group: “Theme & colors”, “SEO & metadata” | console | nav rows → `/store-management/details`, `/content/branding` |
| Publish-problem badge on the offending row | console | `publishWarnings` keyed by section id (§6.7) |
| Revisions / history | console | drawer over existing revisions API (§6.7) |
| Draft-conflict recovery, canvas resilience | console | §6.8 |

### 6.1 Bridge protocol v2

Every message carries `{v: 2, type, ...}`; both sides drop messages whose origin fails the allowlist
(console: the origin the manifest answered from; storefront: `NEXT_PUBLIC_BUILDER_ORIGINS`) or whose
`v` is unknown. **Step 0:** add `NEXT_PUBLIC_BUILDER_ORIGINS` to landing-ui's env in `lcl.yml`
(`http://gateway.com:${port.store-core-gateway.8000}`) — without it the bridge stays deliberately
silent, which is why canvas click-select does nothing locally today.

Console → iframe: `select {sectionId|null}`, `hover {sectionId|null}`, `scrollTo {sectionId}`,
`guides {on}`, `dragState {active, label}`, `dragOver {y}` (pointer Y in iframe viewport coords:
`clientY − iframeRect.top`; bridge adds its own scrollY), `locks {ids}`.

Iframe → console: `ready` (console replays select/guides/locks/dragState — every reload re-handshakes),
`height {px}`, `sectionClicked {sectionId}`, `sectionHovered {sectionId|null}`,
`toolbar {action: moveUp|moveDown|duplicate|remove, sectionId}`, `dropTarget {beforeId|null}`
(throttled answer to dragOver; null = end), `reorder {sectionId, beforeId|null}`,
`addHere {beforeId|null}`.

### 6.2 Drag-and-drop, end to end

**(a) Source — library tiles.** `draggable="true"` (native HTML5 — cdk cannot cross documents).
`dragstart`: custom drag image (the tile), `{presetId|savedId}` into dataTransfer,
`facade.startDrag(kind,label)` → `dragState {active:true,label}`. `dragend` (drop, Esc and
drop-outside alike) always sends `dragState {active:false}` — the single cancel path.

**(b) Target — the canvas wrapper.** The iframe never receives cross-origin native drags, so the
console's `.canvas` wrapper is the drop target: `dragover` (preventDefault, dropEffect copy) forwards
`clientY − iframeRect.top` via `dragOver`, one message per animation frame. `drop` calls
`facade.insertAt(preset, facade.dropBeforeId())` — the console's copy of the last `dropTarget`, so a
lost final message costs one section of precision, never a crash. The layer list is a parallel native
drop target (row-midpoint math locally, no bridge).

**(c) Insertion math — in the bridge.** On `dragState {active}` snapshot every `[data-section-id]`
rect (re-snapshot on height change and scroll). Per `dragOver {y}`: `docY = y + scrollY`; insertion
point = before the first section whose `top + height/2 > docY`, else null (end). Move a 2px accent
insertion line there; answer `dropTarget` only when the target *changed*. Locked sections are valid
boundaries, never replacements.

**(d) Auto-scroll.** `dragOver.y` within 48px of the viewport edge → the bridge scrolls its own
document ±12px/frame (the console cannot scroll the iframe).

**(e) In-canvas reorder.** Entirely inside the iframe (single-document): pointerdown on the toolbar
grip → `setPointerCapture`, ghost outline follows, boundary math from (c) drives the line, pointerup
emits one `reorder {sectionId, beforeId}` → `facade.moveById`; Esc cancels. The bridge moves nothing
itself — the canvas reorders on the post-save reload.

**(f) The honest latency.** The canvas is server-rendered truth: intent → facade → debounced save →
iframe reloads at the new `savedRevision`. The layer list is the instant view; the bridge shows a thin
“updating…” shimmer from accepted drop until the next `ready`. No optimistic DOM surgery in the iframe.

### 6.3 Floating toolbar

Bridge-rendered, pinned to the selected section's top-end corner (logical properties, RTL-correct),
mockup's dark pill from theme tokens: grip, ↑, ↓, duplicate, remove → `toolbar` intents →
`facade.moveById(id, ∓1)` / `duplicate` / `remove`. First/last disables ↑/↓; locked sections get no
toolbar. Real `<button>`s with aria-labels.

### 6.4 Hover, tags and guides

Layer-row mouseenter/leave → `hover`; bridge mouseover (delegated, throttled) → `sectionHovered` →
row highlight. Name tag chip at each section's top-start corner (kind off `data-section-kind`,
humanized) on hover/selection/guides. Guides toggle in the top bar → `guides {on}`: dashed outline +
tag on every section, add-here zones visible. Signal only, not persisted.

### 6.5 “Add section here”

Bridge renders a dashed zone between each pair of sections and after the last (guides/drag mode).
Click → `addHere {beforeId}` → console opens the library with `facade.insertTarget = beforeId`, a
banner “inserting here” with ✕; next click/drop inserts there, then clears. `insertAt(section,
beforeId|null)` generalizes today's after-the-selection insert.

### 6.6 Locks

`locked?: boolean` on `LayoutSection` (console model + landing-ui type + content-commons record —
codecs ignore unknown fields, additive, no migration). Layer row: lock glyph, no drag handle/eye;
Delete/duplicate/toolbar/canvas-drag refused; inspector header swaps Remove/Duplicate for Unlock (any
manager may unlock in v1 — parity + future plan-gating). `locks {ids}` syncs the canvas.

### 6.7 Chrome completeness

Page group under the layer list (“Theme & colors” → `/store-management/details`, “SEO & metadata” →
`/content/branding`). Publish-problem badges: `publishWarnings` keyed by section id (backend sets
`FieldError.field` to it) → warning dot on the row, message as tooltip. Revisions drawer: history icon
→ side sheet over `layouts.revisions('HOME')` with restore-to-draft (confirm via app-confirm-dialog).

### 6.8 Stability

409 → `app-notice-bar` “Someone else saved this page” + Reload (`facade.load()`), mutations disabled
until reloaded. Save error → same bar with Retry; Publish disabled while saveState ∈ {saving,
conflict, error}. Canvas: iframe error or 15s without `ready` → re-mint token once, reload; still dead
→ placeholder + Retry; timer re-mints at 25min (TTL 30). Every `ready` replays
select/guides/locks/dragState. All overlay positioning logical-properties; insertion math Y-only, so
RTL and device widths never affect it.

### Files

landing-ui — `storefront/src/shell/sections/builder-bridge.tsx` (protocol v2, overlay layer, insertion
math, in-canvas reorder, auto-scroll); `lcl.yml` (builder-origins env). No theme changes.
console-ui — `components/preview-frame.ts`, `components/section-library.ts`,
`components/layer-list.ts`, `components/inspector.ts/html`, `storefront-builder.ts/html/css`,
`facades/builder.facade.ts` (`insertAt`, `moveById`, drag/insert-target/lock signals, conflict
gating), `models/layout.ts` (`locked`), i18n `builder.*` en + ar.
content-commons — `LayoutSection` gains `locked` (additive).

### Sequencing

1. lcl env + protocol v2 skeleton + replay-on-ready (hover sync, toolbar) — canvas interactive locally.
2. Library drag → canvas drop (a–d) + insertAt/add-here targeting.
3. In-canvas reorder (e) + keyboard moves + Delete.
4. Locks, page group, badges, revisions drawer, guides.
5. Stability pass + full QA; console build/lint/test:ci and landing build/lint gate every commit.

### Phase 6 verification

Browser QA on the running `storefront-builder` stack: drag a preset into mid-page (line tracks
pointer, auto-scroll near edges, drop lands at the line; Esc disarms); reorder via canvas grip, layer
list and Alt+Arrow; toolbar ↑/↓/duplicate/remove respect first/last; hover sync both ways; guides show
tags + zones; add-here targets the insert (banner + ✕); locked seeded section → glyph, no
handle/toolbar/Delete, Unlock restores; publish with a sourceless products section → badge on that
row; two tabs → conflict bar + Reload recovers; content-service restart → token re-mint recovers;
beauty + an RTL store render toolbar/tags/lines sanely; builds/lint/tests green in both apps.


## Phase 7 — theme design pass: every theme well designed, aligned (impeccable)

**Load the `impeccable` skill before touching any visual file in this phase** and keep its guidance
active through all of it — this phase is a design review with fixes, not a feature.

### Scope

1. **Registries for the five remaining designed themes** — fashion, grocery, pink, hunger, furniture
   each get `src/sections/LayoutSections.tsx` wired to their own components (each already owns a
   designed Hero/ProductRail/ProductGrid/SectionHeading), following the starter reference. After this,
   every real theme renders builder sections in its own voice; only the four untouched scaffolds stay
   on fallbacks (they inherit starter's registry when regenerated). fashion's interleaved-slider home
   identity returns as its hero variant; furniture/hunger reuse their signature pieces (PlateKey,
   Masthead) where a section kind naturally maps to one.
2. **Shell fallback audit** — the 13 fallback renderers reviewed as one system: hierarchy, spacing
   rhythm, type scale, empty-state quality, focus states, motion restraint, contrast in every
   `tone` (default/muted/inverse), hover affordances. They must read as "the theme's neutral voice",
   never as unstyled placeholders.
3. **Per-theme alignment audit** — for each designed theme, its section renderers against its own
   DESIGN.md/tokens: aspect ratios, radius, display font usage, RTL (logical properties, re-keyed
   sliders), badge/price conventions matching that theme's product cards.
4. **Builder chrome audit** — the console builder's three panes and the bridge overlays (toolbar,
   tags, insertion line) against console DESIGN.md: tokens only, both console themes, ar RTL.
5. **Consistency contract** — write the outcome down: a short "section design rules" block in
   `themes/README.md` (what every kind's renderer must honor: heading scale, spacing tokens,
   tone handling, empty behaviour) so the next theme starts aligned instead of drifting.

### Method

Per theme: render the demo store's full 13-kind layout with the `?theme=` QA cookie, screenshot
desktop/mobile and en/ar via the browser tools, review against impeccable + the theme's DESIGN.md, fix,
re-shoot. The design hook stays on for every write; findings triaged, not suppressed. Accessibility
sweep (landmarks, alt text, focus order, contrast) rides the same pass.

### Verification

Every real theme renders the 13-kind demo layout with no fallback-styled section standing out as
foreign; ar/RTL correct on all of them; `npm run build && lint` green; before/after screenshots
attached to the PR for each theme.
