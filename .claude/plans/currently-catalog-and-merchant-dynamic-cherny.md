# Retire `store-cms-commons`; route all asset uploads through content-service

## Context
Catalog (product images) and merchant (store logo/banner/slider) each embed their own S3 upload path via
`store-pod/commons/store-modules/store-cms-commons` — a Spring-free S3 helper that duplicates what
`store-pod/content/content-service` already does properly (tenant-scoped media library, dedupe, quota,
SVG sanitising, folders, delete-with-usage-check). Goal: one upload path (content), then delete the cms
module, the per-service `FileManagerConfig`/`MerchantAssetsConfig` beans, and the `StoreProductImageProperties`
resize machinery that is configured nowhere.

Decisions (confirmed with user):
- **console-ui uploads directly to content** (`POST /spg/content/api/v1/private/content/media`, existing
  `store-core/console-ui/src/app/api/content/media.service.ts`), then sends the returned `url` to catalog/merchant as JSON.
  No S2S client, no new auth path.
- **Catalog/merchant persist the full public URL** in the existing columns. Legacy rows hold a bare file name;
  reads stay backward compatible via dual-read (starts with `http` → use as-is, else legacy `ImageFilePath` rebuild).
  No data migration; old objects stay in the bucket under `products/…` / `files/…`.

## Phase 1 — Backend: catalog
- `catalog-service/.../api/v1/ProductImageApi.java`
  - `POST /private/product/{id}/image` → accept JSON body `{ url, name?, position?, defaultImage? }` (new
    `PersistableProductImage`-style DTO in catalog-commons) instead of multipart. Keep PATCH/DELETE/GET as they are.
- `catalog-core/.../services/image/ProductImageServiceImpl.java`
  - `add(...)`: drop `ProductFileManager`; set `image_type` for URL images and store `url` in `product_image_url`
    (already exists for external images — reuse it; keep `product_image` for legacy names).
  - `delete`/`removeFiles`: remove `removeProductImage` calls (object is owned by content now; deleting a product image only deletes the row).
  - Drop the `ProductImageNotPersistedException` catch of cms exceptions.
- `catalog-core/.../services/image/ImageMapper.java:38` — dual-read: `product_image_url` present → use it; else `ImageFilePath.buildProductImageUtils(...)`.
- `catalog-service/.../config/FileManagerConfig.java` — delete. Remove `store-cms-commons` from
  `catalog-core/build.gradle:10` and `catalog-service/build.gradle:54`. Widen `product_image_url` if < 512 in `init-sql/schema.sql:170` (+ a migration under `extra/migrations/`).

## Phase 2 — Backend: merchant
- `merchant-service/.../api/v1/MerchantStoreApi.java` — `POST /private/store/marketing/logo|banner|add-slider-image`
  → JSON `{ url }`; delete `createInputContentFile`.
- `merchant-core/.../service/facade/merchant/StoreFacadeImpl.java` — `addStoreLogo/Banner/SliderImage(StoreMerchantId, String url)`;
  remove `ContentAssetsManager` + `addImageToAssets`; `StoreFacade.java` drop `throws AssetUploadFailedException`.
- `ReadableMerchantStorePopulator.java:111,123,135` and `MerchantStoreApi.java:187` — dual-read as in catalog.
- `merchant-service/.../config/MerchantAssetsConfig.java` — delete. Remove dep from `merchant-core/build.gradle:10`, `merchant-service/build.gradle:56`.
  Widen `store_logo`/`store_banner` varchar(100) and `merchant_slider_images.name` in schema.sql + migration.

## Phase 3 — Content: nothing functional; cleanup
- Remove the unused `store-cms-commons` dep from `content-core/build.gradle:10` and `content-service/build.gradle:55`.
- Optional: add system folders `products` / `store` via `media_folder` so uploads from the product/store editors land in a predictable folder (pass `folderId`).

## Phase 4 — console-ui
- `src/app/api/catalog/product-image.service.ts` — `upload(file)` → call `MediaService.upload([file])`, then `POST /private/product/{id}/image` with `{ url: asset.url, name: asset.filename }`.
- `src/app/api/merchant/store.service.ts:93-107` — same for logo/banner/slider.
- Product/store editors: optionally offer the existing `media-picker-dialog` (`features/content/components/media-picker/`) so a merchant can pick an already-uploaded asset.
- Users need `STORE-POD.CONTENT.*` to upload now — check the demo roles in uaa grant it alongside CATALOG/MERCHANT manage; if not, add.

## Phase 5 — Delete the module
- Remove `store-pod/commons/store-modules/store-cms-commons` and its line in `settings.gradle:41`.
- Delete now-dead pieces in `store-commons`: `StoreProductImageProperties` (autoconfigure + `CvhomeSharedConfig.java`),
  `FileContentType` values / `InputContentFile`/`ImageContentFile`/`OutputContentFile` if no remaining imports (grep first — checkout/payment reference `ImageFilePath`, keep that).
- Catalog/merchant no longer need `S3Config`/`S3InitConfigurer`/`aws.sdk.s3` unless used elsewhere in the service — grep and drop.
- Update `.http` QA files (`catalog-service/http`, `merchant-service/http`) and `qa/*.md` sections that describe multipart uploads.

## Verification
1. `./gradlew :store-pod:catalog:catalog-service:test :store-pod:merchant:merchant-service:test :store-pod:content:content-service:test` and a full `./gradlew build` to prove nothing still imports `modules.cms`.
2. Run the stack (`extra/scripts/run-lcl.sh`), in console-ui: upload a product image and a store logo/banner/slider → asset appears in the content Media library, product/store read APIs return content's `public_url`, landing-ui renders it.
3. Legacy: a product/store seeded with a bare file name still renders via the old `products/…`/`files/…` URL.
4. Delete a product image → row gone, asset still listed in media library (expected). Deleting the asset from the media library while referenced by a product is not tracked (documented limitation; `media_usage` only covers content).
5. Tenant check: uploading with store A's token and posting the URL to store B's product is just a URL string — acceptable, but verify the product endpoint still enforces `hasPermission(#merchantStore…)`.
