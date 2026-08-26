# QA — content owns store appearance and media

Store appearance and every uploaded file moved to the content service. Merchant stopped owning the logo,
banner, slider images and social links; catalog stopped storing product image files; the legacy `BOX`
"snippets" are gone; and `store-cms-commons`, the old CMS storage library, was deleted. This is what to try
in order to believe it works — and the things most likely to be wrong.

- **Scope** — content · catalog · merchant · checkout · payment · console-ui · landing-ui
- **Change** — branch `feat/content-owns-appearance`, plan
  `.agents/plans/content-owns-appearance-and-media.md`
- **Cases** — 34

Each case is tagged:

- **[verified]** — run during the build and passed.
- **[unit only]** — proven by a unit or integration test, not exercised in a browser.
- **[not verified]** — never run end to end by anyone. These are where a tester is most likely to find
  something, and they are called out rather than buried.

Sections [REG](#reg--regression-watchlist) and [99](#99--known-gaps) are the highest-value reading: one is
behaviour that has already broken once, the other is behaviour that looks wrong and is not.

---

## 00 — Before you start

**The databases must be recreated.** This change has no migration by design — see the plan's decision 1. A
database carrying the old shape will start, because the DDL is written defensively, but merchant's slider and
social-link tables and catalog's product-image filenames are dropped, and the demo photos are re-registered
under new ids. Drop the `content`, `catalog` and `merchant` schemas before the first run.

```bash
sudo ./extra/scripts/configure-domain.sh        # once per machine

docker exec cvhome-postgres-1 psql -U postgres -d cvhome \
  -c 'drop schema if exists content cascade; drop schema if exists catalog cascade; drop schema if exists merchant cascade;'

lcl start -d
lcl status                                      # everything green before starting
lcl urls                                        # the console and storefront entry points
```

**The demo images have to exist in MinIO.** The local MinIO runs without a volume, so a Docker restart empties
it and every storefront image 404s. That is a pre-existing local-stack gap, not this change — see the
`local-stack-minio-demo-images` note for how to regenerate the placeholders. The seeds register the objects at
the keys that generator writes: `products/<store>/<sku>/SMALL/<file>` for product photos and
`files/<store>/{LOGO,BANNER,SLIDER}/<file>` for the store's own images.

**Sign-in.** Console `http://gateway.com:8000` — `org1-admin` / `admin`. The console works one store at a
time; use the store switcher in the header.

### Looking at the truth underneath

```bash
# the store's appearance record — one row per store
docker exec cvhome-postgres-1 psql -U postgres -d cvhome -c \
  "select store_merchant_id, logo_media_id, favicon_media_id, jsonb_pretty(seo), jsonb_pretty(social_links)
     from content.site_settings;"

# who is holding a media asset — content items, site settings and catalog products alike
... "select asset_id, owner_kind, owner_ref, owner_title, field from content.media_usage order by asset_id;"

# a product's gallery: ids and the cached urls, no filenames
... "select product_image_id, product_id, media_asset_id, alt_text, default_image, sort_order
       from catalog.product_image where product_id = 1 order by sort_order;"

# merchant should have no appearance left at all
... "\d merchant.merchant_store"      # no store_logo, no store_banner
... "\dt merchant.*"                  # no merchant_slider_images, no social_links
```

---

## APP — Store appearance

The console's **Content → Store appearance** tab is the only place these live now.

### APP-01 — The seeded store's title and description survived the move · critical · [verified]

- **Steps** — open **Content → Store appearance** on `org1-store1`.
- **Expect** — Store title and Store description already filled, in each of the store's languages. They were
  the `meta-title` / `meta-description` BOX rows; the seed carries them across as per-locale maps.

### APP-02 — Uploading and setting a logo · critical · [not verified]

- **Steps** — **Choose** on the Logo slot; upload a PNG in the picker; pick it.  Save.
- **Expect** — the thumbnail fills, the toast confirms, and the storefront header shows it after a reload.
  `content.site_settings.logo_media_id` names the asset.

### APP-03 — Clearing a logo · critical · [not verified]

- **Steps** — **Clear** on a filled Logo slot. Save.
- **Expect** — the thumbnail empties and the storefront falls back to the store name. This was impossible
  before: merchant had upload endpoints and no delete.

### APP-04 — The favicon is not the logo · [not verified]

- **Steps** — set a Logo and a different Favicon. Save. Open the storefront.
- **Expect** — the browser tab shows the favicon, the header shows the logo. They used to be one field.

### APP-05 — Social links round-trip, and an emptied one is removed · [not verified]

- **Steps** — fill two providers, Save; reload; empty one, Save.
- **Expect** — the storefront footer shows two, then one. The whole set is sent each time and the server
  replaces, which is what makes clearing work.

### APP-06 — Per-locale SEO · [not verified]

- **Steps** — switch the locale chip, write a different Store title, Save, reload.
- **Expect** — both languages hold their own copy; the storefront `<title>` follows `?lang=`.

### APP-07 — A read-only operator cannot save · critical · [not verified]

- **Setup** — sign in as a store moderator.
- **Expect** — the tab loads and every field is disabled; Save is absent. A direct
  `PUT /spg/content/api/v1/private/content/site-settings` returns **403**.

### APP-08 — Store management has no appearance left · [verified]

- **Steps** — open **Store management**.
- **Expect** — four sections: details, domains, payments, social login. No branding, home, slider or social
  links. `POST …/private/store/marketing/logo` returns **404**.

---

## SEC — Home-page sections

### SEC-01 — The seeded home page still renders · critical · [not verified]

- **Steps** — open the storefront home page.
- **Expect** — the hero carousel shows the five seeded slides (now CMS `CAROUSEL` banners, formerly merchant's
  slider) and the product rails below it.

### SEC-02 — A product-group section · [not verified]

- **Steps** — create a `PRODUCT_GROUP` section pointing at `FEATURED_ITEMS`, publish it.
- **Expect** — the rail appears on the home page. `GET /spg/content/api/v1/storefront/home-sections` lists it.

### SEC-03 — Reordering the page · [not verified]

- **Steps** — with two published sections, reorder them.
- **Expect** — the home page follows. `PATCH /sections/reorder` sends the whole order in one request.

### SEC-04 — Publishing an incomplete section is refused · [unit only]

- **Steps** — publish a `PRODUCT_GROUP` section with no `targetValue`.
- **Expect** — **422 `CONTENT.PUBLISH.INCOMPLETE`**, naming `targetValue`. An `IMAGE` section with no image is
  refused the same way.

---

## MED — The media library and its usage index

### MED-01 — The seeded photos are library assets · critical · [verified]

- **Steps** — open **Content → Media library**.
- **Expect** — the store's product photos, logo, banner and slides are all there. They were registered by the
  seeds rather than re-uploaded, so the bytes never moved.

### MED-02 — Deleting an asset a product uses is refused · critical · [verified]

- **Steps** — find a product photo in the library; delete it.
- **Expect** — **409 `MEDIA.REFERENCED`**, and the drawer names the product. This is the whole point of the
  cross-service usage index — nothing could know this before.

### MED-03 — A forced delete goes through · [verified]

- **Steps** — delete the same asset with **Delete anyway**.
- **Expect** — 204. The product's card falls back to the placeholder, because the cached URL now 404s. That is
  the documented cost of forcing.

### MED-04 — The usage list names non-content owners · [verified]

- **Steps** — open the asset drawer for a product photo.
- **Expect** — "Product · <sku>". The title travels with the registration; content never asks catalog for it.

### MED-05 — Uploading deduplicates · [verified]

- **Steps** — upload the same file twice.
- **Expect** — one asset, and the quota moves once.

### MED-06 — The starter folders appear even when the seller made one first · [unit only]

- **Steps** — on a store with no folders, create a folder of your own, then reload the tab.
- **Expect** — the five system folders are there beside it. The starter set used to be skipped whenever *any*
  folder existed, so a seller who made one first never got the defaults.

---

## CAT — Product images

### CAT-01 — The seeded gallery reads · critical · [verified]

- **Steps** — `GET /catalog/api/v1/product/1/images?store=…`.
- **Expect** — images in sort order, each with a `mediaAssetId` and a cached `imageUrl`. No `imageName`.

### CAT-02 — Attaching from the library · critical · [not verified]

- **Steps** — open a saved product's **Media** step; **Choose from media library**; pick two images.
- **Expect** — both appear; the first becomes the default on an empty gallery.

### CAT-03 — Changing the default image · critical · [not verified]

- **Steps** — mark the second image as the storefront thumbnail.
- **Expect** — it takes, and the first is no longer default. This was impossible before: `PATCH ?order=` only
  renumbered.

### CAT-04 — Reordering · [not verified]

- **Steps** — move an image up.
- **Expect** — the whole gallery is renumbered in one `PUT`, with no two images sharing a position.

### CAT-05 — An asset from another store is refused · critical · [verified]

- **Steps** — `POST /private/product/1/images` with a `mediaAssetId` belonging to another store.
- **Expect** — **400 `CATALOG.PRODUCT_IMAGE.ASSET_UNKNOWN`**, and nothing written. Reported as 400 rather than
  404 so probing tells the caller nothing about whether the asset exists.

### CAT-06 — Detaching leaves the asset alone · [verified]

- **Steps** — remove an image from a product; open the media library.
- **Expect** — the asset is still there. Another product may be showing the same photo.

### CAT-07 — Deleting a product releases its assets · [unit only]

- **Steps** — delete a product that held images; check `content.media_usage`.
- **Expect** — no rows for that product. The assets themselves remain.

### CAT-08 — Catalog no longer uploads · [verified]

- **Steps** — `POST /spg/catalog/api/v1/private/product/1/image` (singular, multipart).
- **Expect** — **404**. There is no upload endpoint in catalog any more.

---

## SF — Storefront

### SF-01 — The announcement comes from the STRIP banner alone · critical · [not verified]

- **Steps** — unpublish the seeded `announcement` banner; reload the storefront.
- **Expect** — the strip disappears. It used to fall back to a `header-message` snippet, so unpublishing
  silently resurrected whatever that row still held.

### SF-02 — The checkout agreement comes from the live TERMS policy · [not verified]

- **Steps** — edit the TERMS policy's text, publish a new version, open checkout.
- **Expect** — the new text. No `agreement` box is consulted.

### SF-03 — Product alt text · [not verified]

- **Steps** — set alt text on an asset in the library; open a product card that shows it.
- **Expect** — the `alt` is that text. It used to be the *filename*.

### SF-04 — The store record carries no appearance · [verified]

- **Steps** — `GET /spg/merchant/api/v1/store?store=…`.
- **Expect** — no `logo`, `banner`, `sliderImages` or `socialLinks`.

---

## ISO — Isolation & permissions

### ISO-01 — A second store sees none of the first store's appearance · critical · [verified]

- **Steps** — switch to `org1-store2`; open Store appearance and the media library.
- **Expect** — its own record and its own assets. Fetching store 1's asset by id answers as though it does not
  exist.

### ISO-02 — The usage index is not writable by a seller · critical · [verified]

- **Steps** — as a store admin, `PUT /private/content/external/media/usage`.
- **Expect** — **403**. That token is for services in the same pod, not for operators — `CONTENT.*` resolves to
  "org or store admin", which is why the write needed its own token.

### ISO-03 — A service token for another pod is refused · [verified]

- **Expect** — **403** on the external media API.

### ISO-04 — A moderator can read but not write content · [verified]

- **Expect** — lists load, `PUT`s are 403.

---

## REG — Regression watchlist

Things that broke once during this change, and would break quietly again.

- **Re-saving a product's images duplicated a usage row.** Hibernate flushes inserts before deletes, so
  re-stating an owner's references inserted a duplicate of a row it was about to remove and tripped the unique
  constraint. The delete is a bulk `@Modifying` query for that reason. Re-save a gallery twice and watch for a
  409.
- **…and clearing the persistence context to fix that broke every page save.** The usage delete runs inside the
  caller's transaction, so `clearAutomatically` detached the content item mid-save. Editing a page is the
  canary.
- **The media library's starter folders depended on test ordering.** They were seeded only when a store had no
  folders at all. See MED-06.
- **A media filter test asserted against page one of the whole library.** With hundreds of seeded assets that
  says nothing about the asset under test. Any new filter assertion should be scoped.
- **`social_links` came back from jsonb as maps, not `SocialLink`s.** A generic `List` deserialisation returns
  maps and the cast only fails later, when the response is written — as a 500 with "Failed to write request".

---

## 99 — Known gaps

Expected. Do not report these.

- **Product thumbnails are full-size originals.** `store-cms-commons` used to write a cropped `SMALL`
  derivative; content stores what it is given. The console still enforces ≥800×800 square on upload, and
  `next/image` runs unoptimised, so pages are heavier than they will be once the media service grows
  derivative sizes.
- **Theme and colour scheme are still merchant's.** Deliberately out of scope — content is meant to own views
  config, and this is the next step. Store management still edits them.
- **The seeded asset URLs are local.** The demo seeds hard-code `http://localhost:9000/<bucket>/…`, so on a
  `+1000` shifted stack the demo images 404 until they are re-uploaded. Real uploads resolve against the
  configured CDN base and are unaffected.
- **Demo media assets report 0 bytes.** The seed registers pre-existing objects without weighing them, so the
  quota bar ignores them.
- **There is no Sections tab in the console yet.** `SectionApi` and `SectionsService` exist and `.http` blocks
  drive them, but the hub has no editor — sections are creatable over HTTP only. SEC-02 and SEC-03 are `.http`
  cases until it lands.
