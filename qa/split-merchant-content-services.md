# Split Merchant and Content Services QA

Existing databases must be recreated before starting this version. No data migration is provided.

## Service startup and schema

### [unit only] Content owns only its schema

Setup: fresh local PostgreSQL and MinIO; start the stack with `test-stores` enabled.

Steps: inspect PostgreSQL after both services start.

Expected: `content.sm_sequencer`, `content.content`, and `content.content_description` exist. Merchant has no
content tables. Content has no foreign key to merchant. The content integration context/schema assertion and
the merchant context test pass; the full stack has not been exercised.

## Canonical and compatibility routes

### [not verified] Public content routes are equivalent

Setup: seeded demo stores and running stack.

Steps: execute the canonical and legacy public requests in `content-api.http` for pages, boxes, and images.

Expected: `/spg/content/**` and `/spg/merchant/**` return equivalent payloads. Trace headers are present.

### [not verified] Private content routes are equivalent

Setup: seller session with `STORE-POD.CONTENT.*` for the selected store.

Steps: execute the canonical and legacy private list and file requests.

Expected: both routes reach content-service and return equivalent status and payloads.

### [not verified] Merchant routes remain on merchant

Setup: running stack.

Steps: call merchant store and router endpoints through `/spg/merchant/**`.

Expected: store and router behavior is unchanged; requests do not reach content-service.

## Seller CMS

### [not verified] CRUD through seller UI

Setup: sign in as a merchant with content permission.

Steps: create, edit, view, and delete one page and one box. Upload and delete an image.

Expected: operations succeed through `/spg/content/**` and the changes persist.

### [not verified] Permission denial

Setup: sign in as a principal without `STORE-POD.CONTENT.*`.

Steps: attempt page or box mutation.

Expected: response is 403 and no content changes.

### [not verified] Tenant isolation

Setup: create content in the first demo store and switch to the second store.

Steps: request, update, and delete the first store's content while scoped to the second store.

Expected: the content is not visible and cannot be changed or deleted.

## Storefront

### [not verified] Page and box rendering

Setup: seeded demo storefront and running stack.

Steps: open storefront pages that render CMS pages and boxes.

Expected: content renders through content-service with the existing fallback/degrade behavior unchanged.

### [not verified] Local media gap

Setup: local stack and MinIO.

Steps: upload an image, list images, and open the resulting URL.

Expected: upload/list behavior is tested. Record any known local MinIO/public-media URL failure explicitly;
do not treat that existing gap as a content routing failure.
