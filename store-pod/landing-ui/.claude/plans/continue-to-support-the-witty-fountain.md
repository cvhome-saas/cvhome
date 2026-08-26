# Content owns appearance and media

## Context

The content platform (`store-pod/content`, port 8121) is built and running: pages, posts, banners, FAQ,
policies, menus, a media library with folders/quota/usage/dedupe, a cache-friendly `/api/v1/storefront` read
API, and a full console-ui `features/content` module. What has not happened is the *alignment* around it —
three services still own pieces of "how the store looks", and the old CMS storage library is still alive:

- **merchant** uploads store logo, banner and slider images itself, holds social links, and its
  store-management screen writes storefront landing copy into a content `LANDING_PAGE` BOX row. It does this
  through `store-cms-commons`, the deprecated CMS module.
- **catalog** uploads product images into its own S3 key space (`products/{storeId}/{sku}/SMALL/...`), with no
  alt text, no metadata and no usage tracking — a second, poorer media system beside content's library.
- **content** still serves the legacy BOX "snippets" surface (`meta-title`, `meta-description`,
  `header-message`, `agreement`, `LANDING_PAGE`) alongside the real components that supersede them, and still
  declares a dependency on `store-cms-commons` that no source file under `store-pod/content` uses.

The result: the storefront home page merges two competing hero concepts (merchant `sliderImages` + content
HERO/CAROUSEL banners), the console's store-management page calls two services to render one settings screen,
and deleting a media asset cannot know a product is using it.

**Intended outcome.** Content becomes the single owner of store appearance and all media. Merchant becomes a
pure store-*config* service (languages, currency, domains, address/contact). Catalog references content media
assets instead of storing files. `store-cms-commons` is deleted. The BOX/snippet surface is gone, each legacy
code replaced by the component that supersedes it.

### Decisions settled with the user (do not re-litigate)

1. **No data migration.** Fresh start — local and deployed DBs are recreated, following the precedent of
   `.agents/plans/split-merchant-content-services.md` and `tenancy-and-pod-registry-split.md`. Existing images
   are abandoned and sellers re-upload. No backfill job, no compat shim, no legacy fallbacks.
2. **Catalog references media as `media_asset_id` + a denormalized cached `image_url`.** The id is the source
   of truth; the cached URL keeps product reads free of a cross-service call on the hot path. Content is told
   about the usage so deletes still 409.
3. **In scope beyond media:** home-page sections + social links move to content; SEO snippets and announcements
   fully migrate off BOX; logo, favicon and brand assets become media assets referenced by a content-owned
   site-settings record.
4. **Deferred, deliberately:** theme selection and colour scheme (`THEME` / `COLOR_THEME` on `merchant_store`,
   `libs/theme/src/merchant-bridge.ts`) stay in merchant. Content is meant to own views config eventually —
   the obvious next step, recorded under *Deferred* — but not part of this change.
5. **One change, not phased.** A single sweep, `store-cms-commons` deleted at the end. Because there are no
   compat layers the build is red in the middle; follow *Execution order* so it converges.

### Working-tree hazard — deal with this before touching anything

`store-pod/landing-ui/libs/types/src/store.ts` is **modified** and seven themes (`cosmetics`, `furniture`,
`glasses`, `hunger`, `jewellery`, `pink`, `sports`) plus several `libs/types` and `locales` files are
**untracked**. A `git checkout`, `git stash` or branch switch mid-sweep silently destroys them, and the
untracked themes will not fail any build if missed — they break at runtime. Commit the working tree first
(§ Execution order step 0).

---

## 1. Content — site settings, home sections, and the end of BOX

### 1.1 `site_settings` — the singleton appearance record

It is deliberately **not** a `Content` row: it has no slug, no workflow, no revisions and no publish window,
and it is read on every layout render. Forcing it through `Content` + `content_description` +
`WorkflowContentApi` buys nothing and costs a fake slug.

New table in `content-service/src/main/resources/init-sql/schema.sql` (schema `content`), one row per store,
created lazily on first read the way `MediaService`'s quota row already is:

```sql
create table if not exists content.site_settings
(
    store_merchant_id  varchar(50) not null primary key,
    logo_media_id      bigint,
    logo_dark_media_id bigint,
    favicon_media_id   bigint,
    og_media_id        bigint,
    seo                jsonb not null default '{}'::jsonb,
    social_links       jsonb not null default '[]'::jsonb,
    updated_at         timestamp(6),
    updated_by         varchar(120)
);
```

Translated copy goes in the `seo` jsonb (`{"metaTitle":{"en":"…","ar":"…"},"metaDescription":{…},"keywords":{…}}`)
rather than a companion description table — that is the established shape here for translated leaf data
(`faq_group.names`, `post_category.names`, `policy_version.translations` all do it), and site SEO is never
queried by locale.

`social_links` is `[{"provider":"INSTAGRAM","url":"…"}]`, reusing
`com.asrevo.cvhome.commons.domain.SocialLink` / `SocialProvider` — they stay in `store-commons/commons` and
simply change consumer from merchant to content.

- Entity `content-core/.../entity/SiteSettings.java` — `@Id storeMerchantId`, jsonb columns via `JsonCodec`,
  the same pattern as `Content.meta`
- Repository `content-core/.../repository/SiteSettingsRepository.java`
- Service `content-core/.../service/SiteSettingsService.java` — `get(store)` (creates the empty row lazily),
  `put(store, PersistableSiteSettings, actor)`. `put` validates every media id belongs to the store via
  `MediaAssetRepository`, then records usage (§1.5).
- DTOs in `content-commons/.../model/site/`: `PersistableSiteSettings`, `ReadableSiteSettings`,
  `SiteBranding` (`record SiteBranding(MediaRef logo, MediaRef logoDark, MediaRef favicon, MediaRef og)`),
  `MediaRef` (`record MediaRef(Long id, String url, String alt, Integer width, Integer height)`)
- API `SiteSettingsApi` at `/api/v1/private/content/site-settings` — `GET` (`ContentPermissions.READ`),
  `PUT` (`.MANAGE`). Both take `StoreMerchantId merchantStore` + `LanguageCode language`. No new permission
  token needed; both already have `case`s.
- `.http`: new `content-service/http/site-settings-api.http`

### 1.2 Home-page sections — use the dormant `SECTION` type

`ContentType.SECTION` already exists in
`store-pod/commons/store-commons/.../store/core/entity/content/ContentType.java` with `workflow() == false` and
**no binding** — it is the seam this change was waiting for. Rather than a new table, add a
`SectionBinding` to the existing `BindingRegistry` alongside `PageBinding`/`PostBinding`/etc., so sections get
list/get/create/update/delete/translations/reorder for free from `WorkflowContentApi`.

- `SECTION` rows gain workflow (`workflow()` returns true for it; only `BOX` was ever excluded, and `BOX` is
  being deleted — so `workflow()` can simply be deleted along with the enum constant, see §1.3).
- `Content.meta` jsonb for a SECTION holds `{kind, position, mediaId, target{kind,value}, settings{}}` where
  `kind` ∈ `RICH_TEXT | IMAGE | PRODUCT_GROUP | CATEGORY_GRID | VIDEO | HTML`. `PRODUCT_GROUP` carries a
  catalog `ProductGroupCode` in `target.value`, which is how today's hardcoded
  `FEATURED_ITEMS`/`NEWLY_ADDED`/`HOME_PAGE`/`RECOMMENDED` list in `storefront/src/shell/loaders/home.ts:9`
  becomes merchant-controlled instead of a constant.
- `sort_order` (already on `content`) orders them; `PATCH /sections/reorder` mirrors the existing
  `PATCH /faq/reorder` atomic-reorder implementation in `FaqApi`.
- New `SectionApi extends WorkflowContentApi<PersistableSection, ReadableSection>` at
  `/api/v1/private/content/sections`, plus `.http`.
- Per-locale copy (heading, body, CTA label) uses the existing `content_description` columns — `title`,
  `description`, `subtitle`, `cta_label` are all already there.

### 1.3 Delete the BOX / snippet surface

Each legacy code is replaced, not bridged:

| Legacy BOX code | Replaced by |
|---|---|
| `meta-title`, `meta-description` | `site_settings_description.meta_title` / `.meta_description` (§1.1) |
| `header-message` | a `STRIP` banner — the only source, no fallback |
| `agreement` | the LIVE `TERMS` policy — the only source, no fallback |
| `LANDING_PAGE` | site SEO (§1.1) + home `SECTION` rows (§1.2) |

Delete:
- `content-commons/.../model/snippet/Snippet.java`
- `content-core/.../service/SnippetService.java`
- `ContentAdminApi` snippet endpoints (`GET/PUT/DELETE snippets[/{code}]`) and their `.http` blocks in
  `content-service/http/admin-api.http`
- `StorefrontFacade.legacyAnnouncement(...)` (~:488) and the `SNIPPET_CODES` constant; `site()` (~:123-128) now
  reads the announcement from the STRIP banner only, and its `snippets` map is replaced by a typed
  `StorefrontSeo` built from `site_settings_description`
- `BOX` from the `ContentType` enum **and** from the `content_type` CHECK constraint in `schema.sql`;
  `ContentType.workflow()` disappears with it (SECTION now has workflow)
- `PersistablePage.linkToMenu` and the MAIN-menu bootstrap it feeds (`MenuService.java:35`). The MAIN menu is
  now only what the seller builds in the Menus tab; `StoreDefaultsService` seeds an empty MAIN + FOOTER menu.
- `ContentRepository.findBySeUrl` (legacy lookup) and the `ContentErrors` compat alias (`ContentErrors.java:14`)
- The per-store BOX seed rows in `content-service/src/main/resources/init-sql/stores/*/` — replaced by
  seeded `site_settings` + a couple of demo `SECTION` rows

### 1.4 `StorefrontSite` grows

`content-commons/.../model/storefront/StorefrontSite.java` today is
`{servedLocale, snippets: Map<String,String>, announcement, menus, footerPages, policies}`. It becomes:

```java
String servedLocale;
StorefrontSeo seo;                       // replaces the untyped snippets map
StorefrontBranding branding;             // { logoUrl, logoAlt, faviconUrl, ogImageUrl }
List<SocialLink> socialLinks;
StorefrontBanner announcement;           // STRIP banner only
Map<String, List<StorefrontMenuNode>> menus;
List<StorefrontLink> footerPages;
List<StorefrontLink> policies;
```

And `GET /api/v1/storefront/sections` returns the ordered home sections with media URLs already resolved.
Both keep the existing 60s `CacheControl` + ETag treatment.

### 1.5 Media usage becomes owner-based

`content.media_usage` today is `(asset_id, content_id, content_type, field)` with `content_id` **not null** and
`content_type varchar(10)` — it cannot express "a catalog product uses this asset", nor "site settings use it".
Widen it rather than collapse `content_type` away, so existing content rows keep their typed meaning:

```sql
alter table content.media_usage add column if not exists owner_kind  varchar(20) not null default 'CONTENT';
alter table content.media_usage add column if not exists owner_ref   varchar(120);
alter table content.media_usage add column if not exists owner_title varchar(200);
alter table content.media_usage alter column content_id   drop not null;
alter table content.media_usage alter column content_type drop not null;
alter table content.media_usage drop constraint if exists media_usage_unique;
alter table content.media_usage add constraint media_usage_unique
    unique (asset_id, owner_kind, owner_ref, field);
alter table content.media_usage add constraint media_usage_owner_kind_check
    check (owner_kind in ('CONTENT', 'SITE_SETTINGS', 'PRODUCT', 'CATEGORY', 'BRAND'));
create index if not exists media_usage_owner_idx on content.media_usage (owner_kind, owner_ref);
```

`varchar(20)`, not 16 — `'SITE_SETTINGS'` is 13 characters and the CHECK list should have headroom.

- New enum `content-commons/.../model/MediaOwnerKind.java`: `CONTENT, SITE_SETTINGS, PRODUCT, CATEGORY, BRAND`.
  It is **orthogonal** to `ContentType`, not a replacement for it — a content-owned row keeps
  `owner_kind='CONTENT'` plus its existing `content_type`, so nothing about the current usage rows changes
  meaning.
- `MediaUsageRow` gains `ownerKind`, `ownerRef`, `ownerTitle`; `contentId` / `contentType` become nullable.
- `owner_title` is stored, not resolved. For `CONTENT` owners `MediaService.usage(...)` may still look the
  title up locally; for `PRODUCT` it uses the stored value the caller supplied. This is the point: content
  must never have to call *back* into catalog to answer "which product uses this image", because that would
  invert the dependency the whole change is establishing.
- `MediaUsageTracker` gains
  `record(MediaOwnerKind kind, String ref, ContentType type, Long contentId, String title, Map<String,Long> refs)`
  and `forget(kind, ref)`; the existing `record(Content, Map)` becomes a thin delegate passing
  `kind=CONTENT, ref=String.valueOf(item.getId())`.
- `MediaUsage` DTO gains `ownerKind` / `ownerRef` and keeps `itemType` / `itemId` / `itemTitle` / `field`.
- `MediaService.delete` needs **no change** — it already 409s on any non-empty usage list, so an asset a
  product references now 409s `MEDIA.REFERENCED` for free.

## 2. New `content-external-api` module

There is no content external API today; catalog needs one. Follow
`store-pod/merchant/merchant-external-api` exactly (see
`.claude/skills/project-structure/references/service-to-service.md`).

- New module `store-pod/content/content-external-api`, package `com.asrevo.cvhome.content.api`, registered in
  `settings.gradle` beside the other three content lines, plus a `lombok.config` copied from merchant's.
  `build.gradle`: `api project(':store-pod:content:content-commons')`, `compileOnly libs.spring.web`,
  `annotationProcessor libs.lombok`.
- One interface, two operations. It declares no failures of its own, so it stays a **single** interface
  implemented by the controller and proxied by the caller:

```java
@HttpExchange("/api/v1")
public interface ExternalMediaService {

    @GetExchange("/private/content/external/media")
    List<ReadableMediaAsset> resolve(StoreMerchantId merchantStore, @RequestParam("ids") List<Long> ids);

    @PutExchange("/private/content/external/media/usage")
    void replaceUsage(StoreMerchantId merchantStore, @RequestBody ExternalMediaUsage body);
}
```

  `StoreMerchantId` carries **no annotation** — the argument resolver serializes it, per the convention. New
  DTO `content-commons/.../model/media/ExternalMediaUsage.java`:
  `record ExternalMediaUsage(MediaOwnerKind ownerKind, String ownerRef, String ownerTitle, List<Ref> refs)`
  with `record Ref(String field, Long assetId)`.

- **Idempotency is structural, not conventional.** There is deliberately no register/release pair.
  `replaceUsage` states the *complete* set of references for `(store, ownerKind, ownerRef)`; the server deletes
  every row for that owner and inserts the given ones in one transaction. Release is the same call with
  `refs: []`. A retry, a re-save and a partial failure therefore all converge on the same state, and no
  counter can drift.

- Server side: `content-service/.../api/v1/ExternalMediaApi.java` **implements `ExternalMediaService`**, so the
  route and the client contract cannot drift. `resolve` delegates to the existing
  `MediaService.urls(store, ids)` (`MediaService.java:252`) — already exactly this shape, reuse it.

- **Permissions — verified, and the likeliest silent failure in this change.**
  `CustomPermissionEvaluator.java:65` maps `CONTENT_READ` to `hasReadAccessOnStore`, which
  (`PermissionAccessChecker.java:121`) already falls through to `isSameStorePod` — so `resolve` works
  service-to-service under `ContentPermissions.READ` with **no evaluator change**. But `:68` maps `CONTENT_ALL`
  to `hasManageAccessOnStore`, which accepts only org/store admins, so the usage *write* cannot reuse it. Add a
  new token:

```java
private static final String CONTENT_MEDIA_USAGE = "STORE-POD.CONTENT.MEDIA-USAGE";
// …and in the switch, beside CATALOG_RESERVE / INVENTORY_RESERVE:
case CONTENT_MEDIA_USAGE -> checker.isSameStorePod(authentication, (StoreMerchantId) targetId, this.pod);
```

  It must also be added to the **outer** `switch`'s `case` list at `:52-53`. Miss either and the evaluator's
  `default -> false` denies with no useful log.

- Both endpoints sit under `/api/*/private/**`, so content's `SecurityConfig` already requires a bearer token
  and `RestClientBuilder`'s `microServiceRestClientBuilder` already attaches the s2s client-credentials token.
  No security config change.
- Consumer side: `catalog-service/.../config/ClientsConfig.java` gains
  `b.buildClient("content", ExternalMediaService.class, RemoteErrorCatalog.none())`. `catalog-core` and
  `catalog-service` `build.gradle` swap `store-cms-commons` for `content-external-api`. Verify `content` is in
  `common-config.yml` / `lcl-config.yml` / `fargate-config.yml` (it should be — the service runs on 8121).
- `.http`: new `content-service/http/external-media-api.http`.

**When content is unreachable, fail the request — do not degrade.** Both legs let the typed
`RemoteServiceUnavailableException` / `RemoteServiceTimeoutException` propagate:
- *resolve*: never persist a `product_image` row with a null `image_url`. A half-attached image is worse than
  a failed save, and the caller is a human in the console who can retry.
- *replaceUsage*: call it inside the catalog `@Transactional` method, after `saveAndFlush` has assigned ids. A
  remote call inside a transaction is normally wrong, but the alternative is a silent hole — the rows commit,
  content never learns the asset is in use, the seller deletes it from the library with a 200, and the
  product's cached URL 404s. It is one small idempotent PUT; a failure rolls the image rows back and the
  console shows the error. Say so in the method's javadoc.
- *Floor*: if usage does drift (a forced delete, or a bug), the failure mode is a dead cached URL, which
  console-ui's `shared/directives/image-broken.ts` and landing-ui's `PLACEHOLDER_IMAGE` already absorb.

## 3. Catalog — product images become media references

### 3.1 Schema and entity

`catalog.product_image` (DDL at `catalog-service/src/main/resources/init-sql/schema.sql:170`) is rewritten:

```
product_image_id  bigint primary key
product_id        bigint not null → catalog.product
media_asset_id    bigint          -- content media asset; null for external/video rows
image_url         varchar(500)    -- cached public URL (content-owned assets) or the external URL
alt_text          varchar(255)    -- new; product images never had alt text
video_url         varchar(500)
image_type        integer         -- 0 = media asset, 1 = external URL/video (kept)
default_image     boolean
sort_order        integer
```

`product_image` (filename) and `image_crop` are dropped — both were artefacts of catalog owning the file.
`ProductImage.java` follows; `Product.images` stays a non-cascaded `Set<ProductImage>` with `defaultImage()`.

### 3.2 API

`catalog-service/.../api/v1/ProductImageApi.java` loses its upload endpoint entirely. Uploads go to content's
`POST /api/v1/private/content/media`; catalog only ever attaches ids:

| Method | Path | Notes |
|---|---|---|
| `GET` | `/api/v1/product/{productId}/images` | unchanged shape-wise, still public |
| `POST` | `/api/v1/private/product/{id}/images` | body `[{mediaAssetId?, externalUrl?, videoUrl?, altText?, order?, defaultImage?}]` — attach, replacing the multipart upload |
| `PATCH` | `/api/v1/private/product/{id}/image/{imageId}` | body `{order?, defaultImage?, altText?}` — this finally makes the default image changeable, which the old `?order=`-only PATCH could not do |
| `DELETE` | `/api/v1/private/product/{id}/image/{imageId}` | detaches; **does not** delete the asset from the library |

All keep `@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")`. On attach,
catalog calls `ExternalMediaService.resolve` to fill `image_url` and to reject an id that does not exist in this
store's library (new `ProductImageAssetNotFoundException` + `CATALOG.PRODUCT_IMAGE.ASSET_NOT_FOUND` in
`CatalogErrors`), then `registerUsage` with `ownerLabel` = the product's default-locale name.

### 3.3 Consequences

- `ProductImageService`/`Impl` lose all file handling; `ImageMapper` stops building URLs from
  `ImageFilePath.buildProductImageUtils` and just reads `image_url`.
- `ReadableImage` becomes `{id, mediaAssetId, imageUrl, externalUrl, videoUrl, altText, imageType, order,
  defaultImage}` — `imageName` goes (the filename lives in the media library now).
- **Delete** `catalog-service/.../config/{S3Config,S3InitConfigurer,FileManagerConfig}.java` and
  `catalog-service/.../utils/CloudFilePathUtils.java`. Catalog no longer needs an `S3Client` at all.
- `ImageFilePath` (`store-pod/commons/store-commons/.../store/utils/ImageFilePath.java`): delete
  `buildProductImageUtils`, `buildStoreLogoFilePath`, `buildStoreBannerFilePath`, `buildStoreSliderFilePath`,
  `buildProductPropertyImageUtils`, `buildCustomTypeImageUtils` and the now-unused `PRODUCTS_URI` /
  `SMALL_IMAGE` constants in `store-commons/.../store/core/constants/Constants.java`.
- **The other two `CloudFilePathUtils` copies are already dead — verified, delete them outright.**
  `payment-service`'s copy has no consumer at all in `src/main`. `checkout-service`'s exists only to satisfy an
  injected `ImageFilePath imageUtils` field at
  `checkout-core/.../populator/order/ReadableOrderProductPopulator.java:41` that is **never read** — that
  populator takes the URL straight off `ReadableImage.getImageUrl()` (:99), which catalog already populates, so
  it keeps working unchanged. Remove the field, both `CloudFilePathUtils` classes and their two
  `CloudFilePathUtilsTest`s. With catalog's copy gone too, `ImageFilePath` has one possible survivor
  (`buildStaticImageUtils`); grep it, and if it has no caller delete the interface as well.
- Seeds `catalog-service/src/main/resources/init-sql/stores/*/16-catalog-product-image.sql` must reference
  media assets seeded by content. Because the two services own separate schemas and neither may reach into the
  other's, seed **fixed asset ids** in content's per-store seeds and reference those numbers from catalog's,
  with a comment naming the coupling. This is a seed-data convention, not a foreign key.
- `.http`: rewrite `catalog-service/http/product-image-api.http`; IT: rewrite
  `catalog-service/src/integrationTest/.../ProductImageApiIntegrationTest.java` (it currently posts multipart).

### 3.4 Category and brand images — recommended, flagged

`catalog.category.category_image` and `catalog.manufacturer.manufacturer_image` exist in the DDL but map to
nothing, and console-ui documents the gap (`brand-tab.ts:20`, `category-tab.ts:26-29`). Now that attaching an
image is just "store an id + URL", wiring them is cheap and closes a known hole. **Recommendation: do it** —
same `media_asset_id` + `image_url` + `alt_text` treatment, same `MediaOwnerKind` extension (`CATEGORY`,
`BRAND`). If scope needs cutting at implementation time, this is the one piece to drop; say so explicitly
rather than half-doing it.

---

## 4. Merchant — strip appearance down to config

**Delete** from `merchant-service/.../api/v1/MerchantStoreApi.java`: `PUT /private/store/social-links` (:132),
`POST /private/store/marketing/logo` (:144), `POST /private/store/marketing/banner` (:158),
`POST /private/store/marketing/add-slider-image` (:172), `PUT /private/store/marketing/slider-images` (:191),
and their `.http` blocks.

**Delete** `merchant-service/.../config/MerchantAssetsConfig.java` (the `ContentAssetsManager` bean).

**Delete** from `StoreFacade`/`StoreFacadeImpl` (~:160-227): `addStoreLogo`, `addStoreBanner`,
`addStoreSliderImage`, `updateSocialLinks` (:221), `updateSliderImages` (:226); same methods from
`MerchantStoreService`/`Impl`. `StoreFacadeImpl:134` (`setSocialLinks`) goes with them.

**Entity/DDL** — `MerchantStore.java` loses `sliderImages` (:145) and `socialLinks` (:148-152);
`merchant-service/src/main/resources/init-sql/schema.sql` loses `store_logo` (:25), `store_banner` (:26),
table `merchant.merchant_slider_images` (:50) and table `merchant.social_links` (:58).

**DTOs** — `ReadableMerchantStore` / `PersistableMerchantStore` lose `logo`, `banner`, `sliderImages`,
`socialLinks`; populators `ReadableMerchantStorePopulator` (:73) and `PersistableMerchantStorePopulator`
(:88-99) follow. Update the merchant tests that assert these (`MerchantStoreApiTest`, `MerchantStoreTest`,
`StoreFacadeImplTest`, both populator tests, `MerchantStoreServiceImplTest`).

**Merchant keeps:** `LANGUAGE_CODE` + `merchant.merchant_language`, `CURRENCY_ID`,
`CURRENCY_FORMAT_NATIONAL`, `merchant.store_domains`, address/phone/email/units, `USE_CACHE`,
`REQUIRE_LOGIN_FOR_ORDER_PLACEMENT`, `STORE_TEMPLATE`, `INVOICE_TEMPLATE`, and — deferred — `THEME` /
`COLOR_THEME`. Note: payment gateway secrets and social-login secrets are **not** in merchant (they are in
`payment` `PaymentSecret`/`PaymentConfiguration` and `cua` `SocialLoginConfig`, encrypted via `secret-crypto`);
they are untouched by this change.

This move also closes a documented gap: there was never a delete endpoint for logo or banner, so a logo could
be uploaded and never removed. In the media library, deleting is just deleting an asset.

---

## 5. Delete `store-cms-commons`

Order matters — do this **last**, once nothing imports it.

1. Drop the already-dead declarations first: `content-core/build.gradle:10` and
   `content-service/build.gradle:55`. No source under `store-pod/content` imports
   `com.asrevo.cvhome.store.core.modules.cms` — the comment claiming the media service names cms exception
   types is stale. This is a zero-risk warm-up commit.
2. After §3: drop `catalog-core/build.gradle:18` and `catalog-service/build.gradle:55`.
3. After §4: drop `merchant-core/build.gradle:11` and `merchant-service/build.gradle:56`.
4. `git rm -r store-pod/commons/store-modules/store-cms-commons` and remove `settings.gradle:41`. If
   `store-modules/` is then empty, remove the directory.

**Nothing needs rehoming.** Content's storage layer is already complete and independent (`MediaStorage`,
`ImageProbe`, `SvgSanitizer`, its own `MediaStorageException`). The one judgement call is
`ProductImageCropUtils` / `ProductImageSizeUtils`: content's `MediaService` does not resize or crop, so
dropping them means uploads are stored at their original dimensions. That is already true of every asset in
the media library today, so this is consistent rather than a regression — but note in `lessons.md` that
server-side derivatives are now nobody's job, and that `ProductFileManagerImpl`'s LARGE variant was written but
never linked anyway. Client-side crop already exists in console-ui's `media-step` validation rules.

`CmsErrors` codes that disappear entirely: `CMS.ASSET.NOT_FOUND`, `CMS.ASSET.READ_FAILED`,
`CMS.ASSET.UPLOAD_FAILED`, `CMS.ASSET.DELETE_FAILED`, `CMS.ASSET.LIST_FAILED`, `CMS.IMAGE.UNREADABLE`,
`CMS.IMAGE.SIZE_MISCONFIGURED`. Grep console-ui `src/locale/*.json` for `errors.cms.*` keys and remove them.

---

## 6. console-ui

### Store management sheds appearance

`features/store-management/` — **delete** `components/branding-section/`, `components/slider-section/`,
`components/social-links-section/`, `components/home-section/`. Delete the logo (`store.service.ts:97`), banner
(:101) and add-slider-image (:107) calls, `facades/store-settings.facade.ts` `uploadLogo`/`uploadBanner`
(~:339), and `HOME_BOX_CODE = 'LANDING_PAGE'` plus its use in `services/store-settings.api.service.ts` (:67,
:167). What remains is genuinely store config: `details-section`, `domain-section`, `payments-section`,
`social-login-section`.

### Content hub absorbs them

`features/content/` gains a **Branding** tab (logo, favicon, OG image via the existing `media-picker-dialog`;
social links; site SEO per locale — the `seo-block` component already exists) and a **Sections** tab (ordered
home sections using the existing `sortable-list` and `content-list` patterns). **Delete**
`components/snippets-card/` and `api/content/snippets.service.ts`. New `api/content/site-settings.service.ts`
and `api/content/sections.service.ts` beside the existing ones in `api/content/content-api.ts`.

### Product form reuses the media library

`features/product-form/components/media-step/` stops uploading through
`api/catalog/product-image.service.ts` and instead opens `features/content/components/media-picker/
media-picker-dialog.ts` — the picker already exists and is exactly this interaction. `product-image.service.ts`
loses `upload()` and gains `attach(mediaAssetIds)`; `services/product-form.api.service.ts` `uploadImages()`
becomes `attachImages()` (the sequential `concat` + `settling()` re-read pattern stays), and
`facades/product-form.facade.ts` keeps its `images` linkedSignal / `moveImage` renumbering. The media-step's
validation rules (jpeg/png/webp, 5 MB, min 800×800, square ±0.25) move to the picker's upload path. Default
image becomes changeable now that `PATCH` supports it.

Models to update: `models/{merchant.ts, store-settings.ts, content.ts, catalog.ts (ReadableImage ~:213),
products.ts (ProductImageItem ~:124)}`.

i18n: add `content.branding.*`, `content.sections.*`, remove `storeManagement.branding/slider/social/home.*`
and `errors.cms.*` — in **`src/locale/en.json` and `ar.json` together**; the missing-key handler is strict.

Append to `store-core/console-ui/lessons.md`: the advisory-usage trade-off (§2), no server-side derivatives
(§5), and deferred theme/colour ownership.

---

## 7. landing-ui

### Types and services

- `libs/types/src/store.ts` — `Store` loses `logo`, `banner`, `sliderImages`, `socialLinks`. Keep `theme` and
  `colorTheme` (deferred). `ImageFile` / `SliderImage` / `SocialLink` move to `content.ts` if still needed.
  **Note this file is already modified in the working tree** — rebase onto that, don't overwrite it.
- `libs/types/src/content.ts` — `SiteContent` gains `seo`, `branding {logoUrl, logoAlt, faviconUrl,
  ogImageUrl}`, `socialLinks`, and drops `snippets`. Add `HomeSection`.
- `libs/types/src/product-groups.ts` — `Image` becomes `{id, mediaAssetId, imageUrl, externalUrl, videoUrl,
  altText, imageType, order, defaultImage}`.
- `libs/services/src/content-service.ts` — add `getSections(ctx)`.
- `libs/services/src/product-presenter.ts` — `primaryImage`'s alt now prefers `img.altText` before falling back
  to the product name (it currently falls back to the *filename*, which was never useful alt text).

### Shell

- `storefront/src/shell/loaders/site.ts` — `EMPTY` updated for the new `SiteContent` shape;
  `bannerAsAnnouncement` keeps working (it already reads a `Banner`).
- `storefront/src/shell/loaders/home.ts` — drop `getStore()` from the hero path. `hero.slides` is now derived
  from content HERO/CAROUSEL banners instead of `store.sliderImages` (:22) and `hero.banner` from the STRIP or
  hero banner instead of `store.banner` (:23). The hardcoded `GROUPS` constant (:9) is replaced by the ordered
  `SECTION` rows, with the existing `renderable()` guard retained for `PRODUCT_GROUP` sections.
- `storefront/src/shell/loaders/layout.ts` — populate the new `LayoutData` fields from the site document.
- `storefront/src/shell/seo/metadata.ts` — `:11-14` read `site.seo.metaTitle/metaDescription` instead of the
  snippets map; `:15` the favicon comes from `site.branding.faviconUrl` rather than doubling as the logo.

### Theme contract and the 12 themes

`libs/theme/src/contract.ts`: `LayoutData` gains `branding` and `socialLinks`; **keep `HomeData.hero`'s shape
as-is**. This is a deliberate cost decision — repointing `hero`'s *source* in the loader means none of the 12
`pages/Home.tsx` files or their `Hero`/`Masthead`/`HeroFrame` components need to change, which removes roughly
half the theme churn for no loss of honesty (the field was always "what goes at the top of the home page").

What each theme **must** change is mechanical and confined to two files:

- `src/layout/Header.tsx` — `store.logo?.path` → `data.branding.logoUrl` (basic, cosmetics, jewellery, grocery,
  glasses, beauty, sports, furniture, pink, starter, hunger; fashion passes `store.logo` to a `Wordmark`)
- `src/layout/Footer.tsx` — `store.socialLinks` → `data.socialLinks` (all 12)

`ProductCard.tsx` / `CartLineItem.tsx` / `sections/Gallery.tsx` need no change — they consume
`product-presenter` helpers, not `Image` fields directly. `defineTheme` throws on a missing *page*, not a
missing layout field, so nothing here is compiler-enforced: **grep for `store.logo` and `store.socialLinks`
after the edit** to confirm none survive. Themes live in `store-pod/landing-ui/themes/` and several
(cosmetics, furniture, glasses, hunger, jewellery, pink, sports) are **untracked in the working tree** — they
must be edited too or they break at runtime, silently.

`storefront/next.config.ts` needs no change (`images: {unoptimized: true}`, so the CDN URL is used verbatim).

---

## Execution order

The build is intentionally red in the middle. Converge in this order:

0. Move this plan to the repo convention location — `.agents/plans/content-owns-appearance-and-media.md`,
   beside `console-ui-content.md` and `split-merchant-content-services.md`, which it continues — and branch off
   an up-to-date `develop` (`git fetch && git switch -c feat/content-owns-appearance origin/develop`). Never
   commit to `develop` directly.
1. `content-core` / `content-service` drop the dead `store-cms-commons` declaration (green immediately).
2. Content: `media_usage` owner rewrite → `site_settings` → `SectionBinding` → delete BOX/snippets →
   `StorefrontSite` reshape → `content-external-api` + `ExternalMediaApi` → `CustomPermissionEvaluator` case →
   schema.sql + seeds + `.http` + tests. Content is green and self-contained here.
3. Catalog: schema + entity + API + service + mapper + `ClientsConfig` → delete S3 config → seeds, `.http`, IT.
4. Merchant: delete endpoints, facade methods, DTO fields, entity fields, DDL, `MerchantAssetsConfig`, tests.
5. `ImageFilePath` / `Constants` cleanup, plus the dead `CloudFilePathUtils` in **payment** and **checkout**
   (§3.3) — these two services are in the blast radius even though they own no media.
6. Delete `store-cms-commons` + `settings.gradle:41`.
7. console-ui, then landing-ui.

---

## Verification

**Gates (all mandatory):**

```bash
./gradlew checkstyleMain checkstyleTest checkstyleIntegrationTest   # warnings = errors
./gradlew build -x test -x check
./gradlew test
./gradlew integrationTest                                          # Docker up; this change touches SQL, HTTP, security
cd store-core/console-ui && npm run build && ng test
cd store-pod/landing-ui  && npm run build                          # from the module root: libs → templates → app
```

Grep gates: no `com.asrevo.cvhome.store.core.modules.cms` import survives; no `store.logo` /
`store.socialLinks` in `themes/`; no `LANDING_PAGE` / `header-message` / `meta-title` outside a comment.

**End-to-end on a running stack** (`sudo ./extra/scripts/configure-domain.sh` once, then `lcl start -d`;
console `http://gateway.com:8000`, `org1-admin` / `admin`; storefront via `lcl urls`). Databases must be
recreated first — this change has no migration.

1. Content → Media: upload a logo, a favicon and two product photos. Quota bar moves; a duplicate upload
   dedupes to the existing asset.
2. Content → Branding: set logo, favicon, social links and per-locale site SEO (EN + AR). Storefront header
   shows the logo, footer shows the socials, `<title>`/`<meta description>` and the favicon match — and the
   favicon is the favicon, not the logo.
3. Content → Sections: create a rich-text section and a `PRODUCT_GROUP` section, reorder them. The storefront
   home page renders them in that order — proving the home page is no longer driven by the hardcoded
   `GROUPS` constant.
4. Publish a STRIP banner → announcement appears. Publish a LIVE TERMS policy → checkout agreement shows it.
   Confirm with the snippets API gone that there is no fallback path left.
5. Products → Media step: attach an image from the picker, set alt text, change the default image, reorder.
   The storefront product card shows the image with the alt text.
6. Content → Media: delete that asset → **409 `MEDIA.REFERENCED`** naming the product; `?force=true` succeeds.
   This is the cross-service usage path and is the single most important case in this plan.
7. Store management: only details / domains / payments / social-login remain; no upload controls anywhere.
8. **Tenant isolation:** as store 2, confirm store 1's assets, site settings and sections are invisible and
   unfetchable by id.
9. **Permission gate:** as a content moderator (`STORE-POD.CONTENT.READ` only), lists load but Save/Publish are
   hidden and a direct `PUT` returns 403. Confirm `ExternalMediaApi` rejects a caller from another pod.
10. Repeat 1–3 in Arabic to confirm RTL and per-locale SEO.

**QA doc:** one file, `qa/content-owns-appearance-and-media.md`, structured like
`qa/billing-per-store-subscriptions.md` — scope line, change line, case count, setup section, cases tagged
**[verified] / [unit only] / [not verified]**, plus a regression-watchlist and known-gaps section. State
plainly that the deferred theme/colour split is expected to still live in merchant, so a tester does not
report it as a bug.

---

## Deferred (record in `lessons.md`, do not build now)

- **Theme + colour scheme move to content.** `THEME` / `COLOR_THEME` on `merchant_store` and
  `libs/theme/src/merchant-bridge.ts` are the last appearance concern outside content. Moving them completes
  "content owns views config" and is the natural next change.
- **The page builder.** `Content.meta.blocks` and `StorefrontPage.blocks` remain the carved seam; the
  `SECTION` type introduced here is the first real occupant of it and should generalize from the home page to
  any page.
- Server-side image derivatives / CDN purge — nobody's job after `store-cms-commons` goes.
- Media quota driven by billing entitlements rather than a fixed 5 GiB config value.
