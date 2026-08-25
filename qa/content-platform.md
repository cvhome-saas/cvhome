# QA — the content platform

`store-pod/content` was rebuilt. The old service was two tables behind two flat CRUD screens; the new one is a
CMS: seven content domains, a draft→review→scheduled→published→archived workflow, per-locale translation state,
revisions, a media library with quota and usage tracking, navigation menus, versioned legal policies, and a
public read API the storefront renders from. console-ui gained `/content`, landing-ui gained `/blog`, `/help`,
`/policies/*` and a sitemap, and the legacy `/api/v1/content/**` compatibility surface was deleted with
seller-ui.

This is what to try in order to believe it works, and — just as usefully — the things that were already broken
once during the build and could break again.

- **Scope** — content · console-ui · landing-ui · spg · uaa permission evaluator
- **Change** — PR #276, branch `feat/mirror-console-ui`, plans `.agents/plans/console-ui-content.md`
  (Module 12) and `.agents/plans/console-ui-retire-seller-ui.md` (Module 13 / content phase 7)
- **Cases** — 81 (16 verified, 19 covered by tests only, 46 never run end to end)
- **Storage** — media uploads go to MinIO in `docker-compose-lcl.yml`. It runs **without a volume**, so
  everything uploaded is gone after a container restart. That is the local stack, not a defect.

Each case is tagged:

- **[verified]** — driven end to end against a running stack during the build and passed.
- **[unit only]** — covered by a named automated test, but nobody drove it through the stack. The test is named
  so you can judge how much that is worth.
- **[not verified]** — never run end to end by anyone. These are where a tester is most likely to find
  something, and they are called out rather than buried.

Sections [REG](#reg--regression-watchlist) and [99](#99--known-gaps) are the highest-value reading: one is
defects that have already happened here, the other is behaviour that looks wrong and is not.

---

## 00 — Before you start

```bash
sudo ./extra/scripts/configure-domain.sh        # once per machine
lcl start -d             # stop later with `lcl stop`
```

**Sign-in.** Console `http://gateway.com:8000` — `org1-admin` / `admin` (org owner), `org1-store1-admin` /
`admin` (store admin), `org1-store1-moderator` / `admin` (the read-only case). Storefront
`http://org1-store1.spg-507f1f77.gateway.com` — shopper `user` / `revo`.

### The demo stores, and which one to use for what

| Store | Id | Locales | Use it for |
|---|---|---|---|
| org1-store1 | `65f023632bc46470c104b76f` | en, **ar** | everything; the RTL cases need this one |
| org1-store2 | `65f023632bc46470c104b75f` | en, fr | a second store **of the same org** |
| org2-store1 | `65f020632bc46470c104b76f` | fr, en | a store of a **different org** — the isolation cases |
| org2-store2 | `65f023632bc26470c104b75f` | ar, fr | an Arabic-first store |

Every store is seeded with six legacy pages (`about-us`, `contact-us`, `terms`, `privacy`, `location`, `faq`),
four legacy boxes (`header-message`, `agreement`, `meta-title`, `meta-description`) and — added by Module 13 —
one published **TERMS policy with a LIVE version**, on negative ids. Nothing else: no posts, no banners, no FAQ
entries, no media. Those you create.

### Addressing

Everything goes through a gateway, never the service port. Two forms, both valid:

```
http://gateway.com:8000/spg/content/api/v1/...?store=<id>&pod=507f1f77bcf86cd799439011&lang=en   # seller path
http://spg-507f1f77.gateway.com/content/api/v1/...?store=<id>&lang=en                            # pod path
```

**The platform gateway route predicates on `pod` as well as `store`.** A request through
`gateway.com:8000/spg/**` with only `store` is a 404 that looks like missing content. The `.http` files in
`store-pod/content/content-service/http/` carry runnable blocks for every endpoint — nine files, one per API
class — and are the fastest way to work through the API cases. Session id goes in the gitignored
`http-client.private.env.json`.

### Looking at the truth underneath

```bash
docker exec cvhome-postgres-1 psql -U postgres -d cvhome -c \
  "select content_id, code, content_type, status, version, publish_at, unpublish_at, placement, policy_type
     from content.content where store_merchant_id='65f023632bc46470c104b76f' order by date_modified desc limit 20;"

# per-locale rows and their translation state
... "select content_id, language_code, state, name, left(description,40) from content.content_description
       where content_id=<id>;"

# every status change, and what drove it — 'scheduler' means the job, not a person
... "select content_id, from_status, to_status, actor, reason, occurred_at
       from content.content_status_audit order by occurred_at desc limit 20;"

... "select content_id, version, author, created_at from content.content_revision order by created_at desc limit 10;"
... "select * from content.policy_version order by content_id, version;"
... "select id, filename, bytes, checksum, folder_id, public_url from content.media_asset;"
... "select * from content.media_quota;"
... "select * from content.redirect;"
```

Service log: `.lcl/default/logs/content.log`. The scheduler prints `Content scheduler moved N item(s)` when it
does anything, and nothing when it doesn't.

---

## WRK — Workflow, versions and revisions

Everything in this section applies to all five workflow types (pages, posts, banners, FAQ entries, policies) —
they share one controller base class and one service. Run it once on pages, then spot-check the shape on one
other type.

The rules a tester needs in order to judge what they see:

- **A slug is unique per store across every type.** A page and a post cannot both be `spring-sale`. The
  database constraint is `(store_merchant_id, code)` and it predates this work.
- **Publishing needs *one* complete locale, not all of them.** Title, plus body for pages/posts/FAQ/policies.
  A store with Arabic missing everywhere can still publish.
- **Editing a published item's source-locale body marks the other locales STALE**, amber in the console. It
  does not unpublish anything.
- **Delete is a hard delete.** No trash, no undo. Revisions and audit rows go with it.

### WRK-01 — A page goes draft → published → readable → unpublished → deleted · critical · [unit only]

The spine of the whole feature. Covered by `ContentApiIntegrationTest.pageLifecycleCreatePublishReadUnpublishDelete`;
run it by hand at least once, because the test never crosses the gateway.

- **Steps** — create a page with EN and AR copy; read it back; publish; read it from the storefront API
  (`GET /api/v1/storefront/pages/<slug>`); unpublish; read it again; delete it.
- **Expect** — create returns `{id, status: DRAFT, version}`; the storefront read is **404 while it is a
  draft**, 200 once published, 404 again after unpublish. After delete, both the private and public reads are
  404 and `content_status_audit` has no orphan rows for that id.

### WRK-02 — The second save of an item works · critical · [verified]

This looks trivial and is not: it was broken, invisibly, for every content type. Title and body live on the
child `content_description` row, so a body-only edit left the parent untouched — no version bump, no
`dateModified`, and the revision snapshot collided with the previous one.

- **Steps** — open a page, change **only the body**, save. Then change only the body again, save. Then change
  only the title, save.
- **Expect** — every save succeeds. The version increments each time, the list's **Updated** column shows a
  time (not blank), and `content_revision` gains one row per save. A 409 on the second save is the regression.

### WRK-03 — A stale version is refused, cleanly · high · [not verified]

- **Setup** — open the same page in two browser tabs.
- **Steps** — save in tab A, then save in tab B without reloading.
- **Expect** — tab B gets **409 `CONTENT.VERSION.CONFLICT`** carrying `currentVersion`, and the console offers
  to reload rather than dropping the seller's text on the floor. Nothing in tab B's edit reaches the database.

### WRK-04 — A duplicate slug is refused · high · [unit only]

`ContentApiIntegrationTest.duplicateSlugAndIncompletePublishAreRefused`.

- **Expect** — 409 `CONTENT.SLUG.DUPLICATE`. Also try it **across types** — a post taking a page's slug must be
  refused the same way, and the console's async "slug available" check must agree with the server.

### WRK-05 — Publishing something incomplete is refused with the field named · critical · [unit only]

Same test as WRK-04. The point of the case is the *shape* of the refusal, not the refusal.

- **Steps** — create a page with a slug and no title; press Publish.
- **Expect** — **422 `CONTENT.PUBLISH.INCOMPLETE`** whose `fieldErrors` names `translations.en.title`, and the
  console highlights that field. Not a generic "something went wrong".

### WRK-06 — A refused publish leaves nothing behind · critical · [not verified]

The highest-value case in this section. A refused publish used to apply the schedule window *before* the gate
threw, and a checked exception does not roll a Spring transaction back — so the item kept a `publishAt` it had
never earned.

- **Steps** — on an incomplete draft, press Publish with a schedule date set. It is refused (WRK-05). Now read
  the row.
- **Expect** — `status` still `DRAFT`, `publish_at` **null**, no `content_status_audit` row. Repeat for a
  refused banner publish (capacity) and a refused policy publish.

### WRK-07 — Editing published source copy marks the other locales stale · high · [not verified]

- **Setup** — a page published with both EN and AR complete.
- **Steps** — change the **English** body and save.
- **Expect** — the page stays published; AR's chip turns **amber (STALE)** in the console and
  `content_description.state` for `ar` reads `STALE`. English stays `TRANSLATED`. The KPI "awaiting
  translation" increases by one.

### WRK-08 — One locale can be written without touching the others · [not verified]

- **Steps** — `PUT /{type}/{id}/translations/ar` with only Arabic in the body.
- **Expect** — Arabic is updated and returns to `TRANSLATED`; the English row is byte-identical afterwards; the
  version moves once.

### WRK-09 — A revision restores what it says it restores · high · [not verified]

- **Steps** — save a page three times with visibly different bodies; `GET /{type}/{id}/revisions`; restore
  version 1.
- **Expect** — the body is version 1's text, and the restore lands as a **new** revision (4), not by rewinding
  the counter. The list's Updated column moves.

### WRK-10 — Bulk actions report per id, and never fail the batch · [not verified]

- **Steps** — `POST /pages/bulk` with `{ids:[<publishable>, <incomplete>, <nonexistent>], action:"PUBLISH"}`.
- **Expect** — **207** with one row per id: `ok:true` for the first, `ok:false` with `CONTENT.PUBLISH.INCOMPLETE`
  for the second, `ok:false` with `CONTENT.NOT_FOUND` for the third. The first one really is published. More
  than 200 ids is refused outright with `CONTENT.BULK.TOO_LARGE`.

---

## SCH — Scheduling

A job runs every 60 seconds (30 s after startup) and does two things: promotes `SCHEDULED` items whose
`publishAt` has arrived, and archives `PUBLISHED` items whose `unpublishAt` has passed. Both are idempotent —
the predicates exclude anything already moved.

A `publishAt` **within 30 seconds of now counts as "publish now"**, deliberately: a seller who picks "today,
this minute" should not watch a spinner for a minute.

### SCH-01 — A future publish date schedules rather than publishes · critical · [unit only]

`ContentApiIntegrationTest.schedulingPublishesWhenTheClockArrives` drives this on an injected clock.

- **Steps** — publish a page with `publishAt` two minutes out.
- **Expect** — status **SCHEDULED**, not PUBLISHED. The storefront read is 404. The console shows the date.

### SCH-02 — The job actually flips it · critical · [not verified]

The live half of SCH-01, and the one the test cannot prove.

- **Steps** — from SCH-01, wait. Watch `.lcl/default/logs/content.log`.
- **Expect** — within ~60 s: `Content scheduler moved 1 item(s)`, status **PUBLISHED**, an audit row with actor
  **`scheduler`** and reason `publishAt reached`, and the storefront serves it. Allow for the storefront's own
  60-second cache before calling it a miss.

### SCH-03 — An unpublish date archives it · high · [not verified]

- **Steps** — publish an item with `unpublishAt` two minutes out; wait.
- **Expect** — status **ARCHIVED**, audit reason `unpublishAt reached`, storefront 404, and `unpublish_at`
  cleared so the row is not re-processed every tick.

### SCH-04 — Impossible windows are refused · [not verified]

- **Steps** — (a) `unpublishAt` before `publishAt`; (b) `unpublishAt` in the past; (c) status SCHEDULED with a
  `publishAt` in the past.
- **Expect** — **400 `CONTENT.SCHEDULE.INVALID`** for all three, with a message a seller can act on, and
  nothing written.

---

## PAG — Pages, snippets and redirects

Pages are the storefront's `/content/<slug>` routes. Snippets are the legacy `BOX` rows (`meta-title`,
`meta-description`, `header-message`, `agreement`, `LANDING_PAGE`) surfaced as a card on the Pages tab — they
have **no workflow**: `visible` and `status` mirror each other, and a `PUT` upserts by code.

### PAG-01 — The six seeded pages are there and editable · critical · [verified]

- **Steps** — open `/content/pages` as `org1-store1-admin`.
- **Expect** — `about-us`, `contact-us`, `terms`, `privacy`, `location`, `faq`, all **Published**, each with EN
  and AR chips. Open `about-us`: the title reads "About Us" (the short name), not the long `<title>` string.

### PAG-02 — A legacy row's meta title survives a round trip · high · [verified]

Legacy rows carry the page `<title>` in the `TITLE` column and nothing in `META_TITLE`. The mapper reads it back
as the meta title; save it again and it must not be lost or promoted into the visible name.

- **Steps** — open `about-us`, save without changing anything, then look at the storefront page's `<title>` and
  at `content_description` for that row.
- **Expect** — the storefront `<title>` is unchanged, the list still shows the short name, and `meta_keywords`
  (which the old service never wrote) is preserved.

### PAG-03 — Renaming a published page's slug leaves a redirect · high · [not verified]

- **Steps** — publish a page at `careers`; open the storefront at `/en/content/careers`; change the slug to
  `jobs` and save; reload the old URL.
- **Expect** — a row in `content.redirect` (`/content/careers` → `/content/jobs`), the old storefront URL lands
  on the new page rather than a 404, and `GET /private/content/redirects` lists it. Renaming a **draft** page
  writes no redirect — there was never a public URL to preserve.

### PAG-04 — Deleting a page that a menu links to · high · [not verified]

- **Setup** — a page that appears in the Main menu.
- **Steps** — delete it.
- **Expect** — **409 `CONTENT.PAGE.REFERENCED`** naming the menus, and the delete dialog says the link will be
  removed. Confirming with `?force=true` deletes the page; the menu item is then flagged **broken** in the
  console and simply absent from the storefront menu — never a menu entry leading to a 404.

### PAG-05 — Snippets round-trip, in every locale · [unit only]

`ContentApiIntegrationTest.summaryCountsAndSnippetsRoundTrip`.

- **Steps** — on the Pages tab's store-snippets card, edit `meta-description` in EN and AR, save, reload.
- **Expect** — both locales come back. The storefront `site` document's `snippets.metaDescription` changes with
  it. No status, no publish button on a snippet.

### PAG-06 — The store home card still writes the landing snippet · high · [not verified]

Module 5's store-management home card was repointed at `snippets/LANDING_PAGE` when the old box service was
deleted. Same screen, different backend — so it is worth one check that it did not silently stop saving.

- **Steps** — Store management → home section, edit the text, save, reload, then check the storefront home.
- **Expect** — it persists and renders. A 404 in the network panel here means the repoint is wrong.

---

## POS — Blog posts and categories

Posts add categories, tags, a hero image, an author and a derived reading time. The storefront serves them at
`/blog` and `/blog/<slug>`.

### POS-01 — A post publishes and appears on the blog index · critical · [not verified]

- **Steps** — create a category; create a post in it with a hero image from the media library, two tags and an
  excerpt; publish; open `/en/blog` on the storefront.
- **Expect** — the post is listed newest-first with its excerpt, hero image and reading time; `/en/blog/<slug>`
  renders the body; the category filter (`/blog?category=<slug>`) narrows the list.

### POS-02 — An absent excerpt is derived, not empty · [not verified]

- **Expect** — a post with no excerpt shows the first ~200 characters of its body as plain text with an ellipsis
  — never raw HTML tags on the index page.

### POS-03 — Related posts are related · [not verified]

- **Steps** — publish four posts sharing a category or a tag; open one.
- **Expect** — at most **three** related posts, all published, never the post itself.

### POS-04 — Deleting a category used by posts · [not verified]

- **Steps** — delete a category that posts are filed under.
- **Expect** — a defined outcome — either a refusal naming the posts, or the posts losing that category and
  staying published. Record which. What must **not** happen is a post disappearing from `/blog` or the index
  500ing.

---

## BAN — Banners and the announcement strip

Placement and window are columns; artwork, link target, theme and audience live in a JSON payload. Capacity per
placement: **HERO 1, CAROUSEL 8, STRIP 1, COLLECTION 1 per target**.

Two things that look wrong and are not:

- **Capacity is enforced at publish, not at save.** You can hold ten HERO drafts; the eleventh publish is what
  fails.
- A banner is only *effective* when it is published **and** inside both its publish window and its own
  `startsAt`/`endsAt`.

### BAN-01 — One HERO banner at a time · critical · [unit only]

`ContentPlatformIntegrationTest.heroPlacementHoldsOneBannerAndStripFeedsTheAnnouncement`.

- **Steps** — publish a HERO banner, then try to publish a second whose window overlaps.
- **Expect** — **422** naming `CONTENT.BANNER.CAPACITY_EXCEEDED` and the id of the banner in the way. Give the
  second a window that starts after the first ends and it publishes.

### BAN-02 — The strip banner becomes the storefront announcement · critical · [unit only]

Same test.

- **Steps** — publish a STRIP banner; open the storefront home.
- **Expect** — its text is the announcement bar. Unpublish it and the bar disappears. (The legacy
  `header-message` box is the fallback when no STRIP banner is live — still wired, worth one check.)

### BAN-03 — Artwork needs alt text before it publishes · high · [not verified]

- **Steps** — attach desktop artwork to a HERO banner, leave alt text empty, publish.
- **Expect** — 422 with `translations.<locale>.altText` named. STRIP banners are exempt (they are text).

### BAN-04 — A window that has not opened yet renders nothing · high · [not verified]

- **Steps** — publish a CAROUSEL banner with `startsAt` an hour out.
- **Expect** — status PUBLISHED in the console (that is correct — it is scheduled *within* its publication),
  and `GET /storefront/banners?placement=CAROUSEL` does **not** include it until the hour passes.

### BAN-05 — Mobile and desktop artwork resolve to real URLs · [not verified]

- **Expect** — `desktopUrl`/`mobileUrl` in the storefront response are MinIO URLs that load in a browser, and
  the storefront picks the mobile one at a narrow viewport. Broken images here after a Docker restart are the
  known MinIO gap, not this feature.

---

## FAQ — Groups and entries

Entries hang off groups by `parentId` and order by `sortOrder`. Reorder is **one atomic write** of both fields
across any number of entries — a half-applied reorder is a defect.

### FAQ-01 — Default groups arrive in both languages · high · [verified]

They were seeded in English only once, which made an Arabic console read "General · position 1".

- **Steps** — open `/content/faq` on a store with no FAQ, in **Arabic**.
- **Expect** — the starter groups have Arabic names. No English word baked into a row subtitle.

### FAQ-02 — Entries reorder, and stay reordered · high · [unit only]

`ContentPlatformIntegrationTest.faqGroupsReorderAndRenderWithJsonLd`.

- **Steps** — create four entries across two groups; move one entry to the other group and to the top; reload;
  open `/en/help`.
- **Expect** — console and storefront agree, positions renumber 0..n per group with no gaps, and one refused
  move (a group id that does not exist → `CONTENT.FAQ.GROUP_NOT_FOUND`) changes **nothing**.

### FAQ-03 — The help page carries FAQPage structured data · [unit only]

Same test.

- **Expect** — `GET /storefront/faq` returns a `jsonLd` string that parses as JSON, contains one `Question` per
  served entry, and the answers are plain text with the HTML stripped. It appears in the page source at
  `/en/help`.

### FAQ-04 — Deleting a group with entries in it · [not verified]

- **Expect** — a defined outcome, recorded. Entries must not become invisible orphans that no console screen
  can reach — check the FAQ list after the delete, and the storefront `/help`.

---

## POL — Legal policies and versions

A policy has a **head** (type, current text, workflow) and a list of **immutable published versions**.
Publishing cuts version n+1 from the head's current text, makes it LIVE and archives the previous LIVE. Old
versions stay readable at `?v=n`. One head per type per store.

**The important consequence:** editing the head's text does *not* change what shoppers see. The LIVE version
does. That will look like a bug the first time.

### POL-01 — Every store answers the TERMS read · critical · [verified]

The checkout agreement reads only `GET /storefront/policies/TERMS` since Module 13 — the legacy `agreement` box
fallback is gone, which is why all four demo stores were given a seeded TERMS policy.

- **Steps** — for each of the four demo stores, `GET /spg/content/api/v1/storefront/policies/TERMS`.
- **Expect** — 200 with heading, body, `version: 1`, `requiresAcceptance: true`. A 404 on any store means the
  checkout agreement has silently vanished for it.

### POL-02 — Publishing cuts a version the storefront then serves · critical · [unit only]

`ContentPlatformIntegrationTest.publishingAPolicyCutsVersionsTheStorefrontServes`.

- **Steps** — edit the TERMS text; publish; read `/storefront/policies/TERMS`, then `?v=1`.
- **Expect** — version **2** is LIVE with the new text; version 1 is ARCHIVED and **still readable at `?v=1`
  with its original text**. `policy_version` holds exactly one LIVE row for that policy.

### POL-03 — An old version cannot be rewritten · high · [not verified]

- **Steps** — attempt to change an archived version's text.
- **Expect** — 422 `CONTENT.POLICY.VERSION_IMMUTABLE`. A published legal text that can be edited after the fact
  is worthless as a record.

### POL-04 — A second policy of the same type is refused · high · [not verified]

- **Steps** — create a second PRIVACY policy for a store that has one.
- **Expect** — 409 `CONTENT.POLICY.TYPE_ACTIVE_EXISTS` naming the existing one, and the console offers to open
  it rather than leaving the seller stuck.

### POL-05 — The compliance cards read correctly · [not verified]

- **Steps** — open `/content/policies` on a store that has only TERMS.
- **Expect** — a card per type with its "required by" regions and its real status: TERMS published, the rest
  missing with a Create call to action. Publish one and its card changes without a hard reload.

### POL-06 — Templates fill both languages · [not verified]

- **Steps** — create a COOKIES policy and press **Insert template**.
- **Expect** — English and Arabic starter text and headings arrive together, editable, and the request
  (`GET /policies/templates?type=COOKIES`) returns 200 for every type — an unknown jurisdiction falls back to
  the plain template rather than erroring.

---

## MNU — Navigation menus

Two menus per store (MAIN, FOOTER), **one level of nesting**, replaced whole by the editor. A store that never
opened the editor gets its MAIN menu bootstrapped on first read from the legacy `linkToMenu` pages, so nobody
loses navigation.

### MNU-01 — The main menu bootstraps from the legacy pages · critical · [unit only]

`ContentPlatformIntegrationTest.menusBootstrapFromLegacyPagesAndRefuseDepth`.

- **Steps** — on an untouched store, open `/content/menus` and compare with the storefront navigation.
- **Expect** — the seeded pages marked `link_to_menu` are there, in order, with per-locale labels — not an
  empty editor.

### MNU-02 — Three levels are refused · high · [unit only]

Same test.

- **Expect** — 422 `CONTENT.MENU.DEPTH_EXCEEDED`; the indent control stops offering a third level.

### MNU-03 — Saving the tree replaces it exactly · critical · [not verified]

The whole tree is written in one request, so a partial save is a real risk.

- **Steps** — reorder two items, indent one under another, hide a third, remove a fourth, save, reload, then
  read `/storefront/menus/MAIN`.
- **Expect** — console and storefront agree; positions renumber per level; the hidden item is absent from the
  storefront but still in the editor; nothing duplicated.

### MNU-04 — A broken internal link is flagged, not served · high · [not verified]

- **Steps** — point a menu item at a page, then unpublish that page.
- **Expect** — the console marks the item **broken**; the storefront menu simply omits it. A shopper must never
  reach a 404 from the navigation.

### MNU-05 — Invalid targets are refused at save · [not verified]

- **Steps** — a URL item with `example.com` (no scheme, no leading slash); a PAGE item with no value.
- **Expect** — 400 `CONTENT.MENU.TARGET_INVALID` with a message naming the rule, and the whole save is rejected
  — not a half-written tree.

---

## MED — Media library

Uploads go through the service to MinIO — no presigned URLs on this platform. Deduplicated by sha-256 **per
store**, counted against a per-store byte quota (5 GiB, `com.asrevo.cvhome.content.media.quota`), max 50 MB per
file. Accepted: JPEG, PNG, WebP, GIF, SVG, MP4, WebM, PDF, ZIP.

### MED-01 — Upload, list, and the file really exists · critical · [unit only]

`ContentPlatformIntegrationTest.mediaUploadDedupesAndGuardsDelete`.

- **Steps** — upload three images from the Media tab; open one asset's URL in a browser tab; check MinIO
  (`http://localhost:9001`, `minioadmin`/`minioadmin`).
- **Expect** — three assets, each with dimensions, a thumbnail and a working public URL; three objects under
  `files/<storeId>/media/<assetId>/<filename>`; the quota bar and the KPI move.

### MED-02 — Uploading the same file twice creates one asset · high · [unit only]

Same test. This looks like a failed upload — be ready for it.

- **Steps** — upload a file, then upload the identical file again (rename it first; the checksum is what
  counts).
- **Expect** — the **existing** asset comes back, the file count does **not** move, and the quota does not
  grow. The console should say so rather than appearing to do nothing.

### MED-03 — A referenced asset cannot be deleted by accident · critical · [unit only]

Same test.

- **Steps** — use an image as a post's hero; delete it from the library.
- **Expect** — **409 `MEDIA.REFERENCED`** listing where it is used, and the drawer shows that list. `?force=true`
  deletes it; the post then renders without a hero rather than breaking.

### MED-04 — The quota refuses the file that would cross it · high · [not verified]

Best done by dropping the quota in config rather than uploading 5 GiB:
`com.asrevo.cvhome.content.media.quota=2MB`.

- **Expect** — **413 `MEDIA.QUOTA_EXCEEDED`** carrying `bytesUsed` and `bytesQuota`, a readable message in the
  console, and no orphan object left in MinIO. Deleting a file frees the space again.

### MED-05 — Types are checked by content, not by trust · high · [not verified]

- **Steps** — upload a `.exe` renamed to `.png`; upload a genuine `.svg` containing a `<script>` tag; upload a
  file with no content type declared (some browsers send `application/octet-stream`).
- **Expect** — the disallowed type is 400 `MEDIA.TYPE_NOT_ALLOWED`; the SVG is stored **with the script
  stripped** (fetch the stored object and read it); the third is classified by its extension and accepted.

### MED-06 — A file over 50 MB is refused · [not verified]

- **Expect** — 413 `MEDIA.TOO_LARGE` from the service, not a raw Tomcat multipart error page.

### MED-07 — Folders move files rather than losing them · [not verified]

- **Steps** — create a folder, move assets into it, then delete the folder — first with no target, then with
  `?moveTo=<other folder>`.
- **Expect** — 409 `MEDIA.FOLDER.NOT_EMPTY` first; then the assets land in the target folder and are still
  listed. Nothing becomes unreachable.

### MED-08 — Alt text per locale round-trips · [not verified]

- **Expect** — alt text set in EN and AR on an asset comes back on reload and reaches the storefront `alt`
  attribute for the locale being served.

---

## SF — The storefront read API

`/api/v1/storefront/**` is public, unauthenticated and cacheable (`Cache-Control: public, max-age=60`,
`stale-while-revalidate=60`). It never serves drafts except with a preview token.

**Locale fallback is deliberate and visible.** Asking for a locale that an item has not been translated into
returns the first complete locale and names it in `servedLocale`. Empty output would be worse.

### SF-01 — One call gives the layout everything · critical · [not verified]

- **Steps** — `GET /storefront/site?store=<org1-store1>&lang=en`.
- **Expect** — snippets, the announcement (or null), `menus.main` and `menus.footer` resolved with hrefs,
  `footerPages`, and `policies` listing only types with a LIVE version. This one call replaced three the old
  storefront made.

### SF-02 — A locale that is missing falls back and says so · high · [not verified]

- **Steps** — publish a page in English only; request it with `lang=ar`.
- **Expect** — 200, English body, `servedLocale: "en"`. Not a 404, not an empty body, not a mixed-locale
  document.

### SF-03 — Drafts are invisible without a token · critical · [unit only]

`ContentPlatformIntegrationTest.storefrontPageReadAndPreviewToken`.

- **Steps** — a draft page: read it publicly; then `POST /private/content/pages/{id}/preview-token` and read it
  with `?preview=<token>`.
- **Expect** — 404 without the token, 200 with it, and the preview response is `Cache-Control: no-store` so it
  never lands in a shared cache. A tampered token, another store's token, or a token for a different slug is
  404 — not 200.

### SF-04 — A preview token expires, and dies with a restart · [not verified]

Signed with a per-process key, TTL 30 minutes.

- **Expect** — a token stops working after a `content` restart. That is by design for a 30-minute preview
  link; confirm it fails **closed** (404) rather than throwing a 500.

### SF-05 — Publishing appears within a minute, not instantly · high · [not verified]

Worth stating plainly so it is not filed as a bug: the API caches for 60 s and the storefront has its own fetch
cache on top.

- **Steps** — publish a page, immediately reload the storefront URL, then reload again after a minute.
- **Expect** — it appears. A hard refresh or a fresh `.http` call proves the API side straight away.

### SF-06 — The sitemap lists what is public and nothing else · [not verified]

- **Steps** — `GET /storefront/sitemap`; then mark a page `noindex` and repeat.
- **Expect** — published pages, posts and policies with LIVE versions; `/help` when FAQ entries exist; the
  `noindex` page **absent**; no drafts, ever.

### SF-07 — Redirect lookup answers only for paths that moved · [not verified]

- **Steps** — `GET /storefront/redirects?path=/content/careers` after PAG-03, then for a path that never moved.
- **Expect** — `{from, to}` then **404** — the storefront treats 404 as "carry on", so a 500 here would break
  every unknown URL.

---

## LUI — The storefront itself

### LUI-01 — The home page renders content from the new API · critical · [verified]

- **Steps** — open `http://org1-store1.spg-507f1f77.gateway.com`.
- **Expect** — footer pages from the CMS, navigation from the MAIN menu, the announcement bar when a STRIP
  banner or `header-message` box is live. Broken product/logo images after a Docker restart are the known MinIO
  gap.

### LUI-02 — A content page renders with its title · critical · [verified]

- **Steps** — `/en/content/about-us`, then `/ar/content/about-us`.
- **Expect** — heading and body from the CMS, the browser tab title from the meta title, and the Arabic version
  right-to-left with the Arabic font (not Arial-substituted boxes).

### LUI-03 — Blog, help and policy routes exist and behave · high · [not verified]

- **Steps** — `/en/blog`, `/en/blog/<slug>`, `/en/help`, `/en/policies/terms`; then a slug that does not exist
  on each.
- **Expect** — content where there is content, the store's own not-found page where there is not — never an
  unhandled error page or an empty shell.

### LUI-04 — Checkout still shows the agreement · critical · [verified]

This is the one that breaks quietly: the agreement now comes only from the TERMS policy.

- **Steps** — add a product, reach checkout, look for the terms text.
- **Expect** — the LIVE TERMS text for that store, in the shopper's locale. Repeat on **all four** demo stores.

### LUI-05 — The CMS being down does not take the storefront down · high · [not verified]

The site loader degrades to an empty document on purpose; the page loader does not (a page with no content is a
404 by definition).

- **Steps** — stop `content`; open the storefront home, then `/en/content/about-us`.
- **Expect** — the home page still renders products with a plain header and no announcement; the content page
  gives the not-found page. Neither should be a stack trace.

---

## SEC — Permissions and tenant isolation

Writes need `STORE-POD.CONTENT.*`; private reads need `STORE-POD.CONTENT.READ`, which is mapped to
"read access on the store" so a moderator can look. The storefront API is deliberately open.

### SEC-01 — A moderator can read and cannot write · critical · [unit only]

`ContentApiIntegrationTest.moderatorCanReadButNotWrite`.

- **Steps** — sign in as `org1-store1-moderator`; open `/content`; try to create a page, publish one, upload
  media, save a menu.
- **Expect** — lists, editors and media load; every write is **403**. Test both halves — a missing permission
  entry fails silently in the "can read" direction too, which is how a moderator ends up staring at an empty
  screen with no error.

### SEC-02 — Another org cannot see or touch this store's content · critical · [unit only]

`ContentApiIntegrationTest.anotherStoreCannotSeeOrTouchThePage`. Use **org2-store1** — a store of a *different
org*; org1-store2 shares an admin and proves less.

- **Steps** — signed in for org 2, request org1-store1's page by id, then `PUT` and `DELETE` it, then read its
  media asset and its menus.
- **Expect** — **404** on all of them, indistinguishable from a row that does not exist, and org 1's page is
  byte-identical afterwards.

### SEC-03 — The private API needs a token at all · critical · [not verified]

- **Steps** — call `/private/content/pages` with no session.
- **Expect** — 401/403, never a 200 with an empty list. An empty list is how a missing filter looks like a
  working permission.

### SEC-04 — The storefront API is open, and only for published things · high · [not verified]

- **Steps** — signed out, in a private window, call `/storefront/site`, `/storefront/pages/<published>`,
  `/storefront/pages/<draft>`.
- **Expect** — 200, 200, **404**. The public surface must never leak a draft, and must never require a login.

### SEC-05 — Uploaded HTML cannot execute · critical · [unit only]

`HtmlSanitizerTest` covers the sanitiser; the case is whether it is actually in the write path.

- **Steps** — paste `<script>alert(1)</script>`, an `onerror=` attribute and an `<iframe>` into a page body via
  the rich-text editor's HTML mode; save; publish; open the storefront page and watch the console.
- **Expect** — formatting and images survive, the script and the event handler do not, and **no dialog appears**
  (a modal here also freezes browser automation for the rest of the session).

### SEC-06 — Nothing sensitive in the log · [not verified]

- **Steps** — after exercising uploads, grep `.lcl/default/logs/content.log` for `minioadmin`, `secret`,
  `Authorization`.
- **Expect** — no matches. Storage errors should name the key, never the credentials.

---

## UI — The console content module

### UI-01 — The hub shows seven tabs and honest counts · critical · [verified]

- **Steps** — open `/content` as a store admin.
- **Expect** — four KPI cards (published, drafts, awaiting translation, media) and seven tabs — pages, posts,
  banners, FAQ, media, menus, policies — each with a count that matches the number of rows in its list. "All
  files" comes from the summary, not from the current page of the grid.

### UI-02 — Every editor opens · critical · [verified]

All five editors crashed on open at one point, on three separate defects. This is the cheapest high-value check
in the document.

- **Steps** — open New Page, New Post, New Banner, New FAQ Entry, New Policy, and one existing item of each
  type by deep link (paste the URL into a fresh tab).
- **Expect** — every one renders with no console error. `NG01203`, `NG0951` or a blank panel is the regression.

### UI-03 — Save and Publish are never dead buttons · critical · [verified]

They used to disable themselves on a form invalidity whose cause (the slug) sits below the fold, so the seller
saw two buttons that did nothing and no reason why.

- **Steps** — open New Page, type a title only, press **Save draft**, then press **Publish**.
- **Expect** — the button is clickable, the first offending field is scrolled into view and marked, and a
  message says what is missing. Never a silent no-op.

### UI-04 — A newly published item shows as published · high · [verified]

Publishing ran two loads and the older response could land last, leaving a published item wearing a DRAFT
badge until a reload.

- **Steps** — create and publish in one go, without reloading.
- **Expect** — the badge reads **Published** immediately, and the success panel matches.

### UI-05 — The editor opens in the store's source language · high · [verified]

- **Steps** — on org2-store2 (Arabic-first), open any editor.
- **Expect** — the locale strip starts on the store's own source locale, not on English, and the publish
  checklist judges **that** locale — the same one the server's gate will judge.

### UI-06 — Arabic, right to left, on every content screen · high · [verified]

- **Steps** — switch the console to Arabic and walk the hub, all seven tabs, the bulk bar, all five editors, the
  media drawer, the menu editor and the policy version history.
- **Expect** — no raw keys such as `content.list.empty` on screen; the layout mirrors — rails, chips, the locale
  strip, table alignment, the up/down reorder arrows; no literal "null" tooltips; no English word baked into a
  row subtitle.

### UI-07 — Errors say something a seller can act on · high · [verified]

- **Steps** — trigger, in Arabic and English: a duplicate slug, an incomplete publish, a full banner placement,
  a referenced media delete, a version conflict.
- **Expect** — each shows copy specific to the code, in both languages. Where the failure names a narrower
  cause, that is what is shown — a full banner placement must not say "write the title and body". A bare
  `errors.content.…` key on screen is a defect.

---

## MIG — Migrating an existing installation

The new schema is additive: `content` and `content_description` keep their names and ids, every new column has
a default, and `schema.sql` is written as `create table if not exists` + `alter table … add column if not
exists` so it is both the fresh DDL and the migration.

> **One separate script.** `extra/migrations/2026-08-23-content-drop-duplicate-unique-indexes.sql` removes
> eleven redundant indexes that `ddl-auto: update` created before the entities named their constraints. It is
> safe to run at any time, before or after the release, and is separate only because Spring's script runner
> cannot parse its `do $$ … $$` block.

### MIG-01 — An old database boots and its rows are readable · critical · [not verified]

- **Steps** — restore a database created by the previous content service; start the new one; list pages in the
  console.
- **Expect** — startup clean, every legacy PAGE row visible with `status` PUBLISHED where it was `visible`, its
  `code` as the slug, and its `sef_url` still serving the storefront. Legacy BOX rows appear as snippets.

### MIG-02 — The duplicate-index cleanup is a no-op the second time · high · [not verified]

- **Expect** — the first run drops the `uk…`-named indexes; the second reports none; the named constraints from
  `schema.sql` are untouched and duplicate-slug inserts are still refused.

### MIG-03 — The legacy compatibility API really is gone · critical · [verified]

Phase 7 deleted `LegacyContentApi`, the Caddy `@legacy_content` alias and `store-pod/content-deprecated`.

- **Steps** — call `/api/v1/content/pages` and `/api/v1/content/boxes/header-message` directly on 8121 and
  through spg; then click through the storefront and the console watching the network panel.
- **Expect** — **404** on the legacy paths, 200 on `/api/v1/storefront/**`, and **nothing on any screen still
  calls the old paths**. The second half matters more than the first.

### MIG-04 — Seeded ids and generated ids cannot collide · high · [not verified]

The Module 13 TERMS seeds use **negative** ids on purpose: `SM_SEQUENCER` only counts upward.

- **Steps** — on a seeded store, create several content items and policy versions.
- **Expect** — new ids are positive and increasing; no primary-key violation on insert; the seeded TERMS policy
  is still readable afterwards.

---

## REG — Regression watchlist

Every row here was a real defect during this work, found by running the thing rather than reading it. They are
the highest-value re-tests, and several were invisible from the screen.

| What broke | How it looked | How to catch it again |
|---|---|---|
| **The second save of any item 409'd** | A body-only edit left the parent row untouched, so the version never moved and the revision snapshot collided on `(content_id, version)`. The "Updated" column stayed blank in every list. | WRK-02 |
| **A refused publish kept its schedule** | The publish gate threw *after* the window was applied, and a checked exception does not roll back a plain `@Transactional` — the item held a `publishAt` it never earned. | WRK-06 |
| **Every content editor crashed on open** | `NG01203` (a toggle bound with `formControlName` and no value accessor), `NG0951` (a dialog behind `*transloco`), and a checklist reading `control.valid` while an async check was pending. | UI-02 |
| **Save and Publish were dead buttons** | Disabled on a form invalidity whose field sits below the fold; no message, no scroll, nothing happened on click. | UI-03 |
| **A published item still showed DRAFT** | Two loads raced after publish and the older response landed last. | UI-04 |
| **Editors opened in the wrong language** | `init` ran before the store's languages resolved, so an Arabic-first store edited in English and the publish checklist judged the wrong locale. | UI-05 |
| **The checkout agreement silently vanished** | The demo stores seeded a legacy `agreement` box but no TERMS policy, and the agreement now reads only `GET /storefront/policies/TERMS`. | POL-01, LUI-04 — on **all four** stores |
| **Arabic showed English seed strings** | The starter FAQ groups were seeded in English only, and two row subtitles baked an English word into a string shown as-is. | FAQ-01, UI-06 |
| **Error copy was generic or missing** | A full banner placement said "write the title and body"; the slug field said "This is not a valid value."; some codes had no Arabic copy at all. | UI-07 |
| **Literal "null" tooltips** | `[title]` bound to null renders the string `null`; `[attr.title]` removes the attribute. | UI-06 |
| **Eleven duplicate unique indexes** | `ddl-auto: update` could not match unnamed entity constraints to the named ones in `schema.sql` and created a second index beside each — paid for on every insert. | MIG-02 |
| **The delete dialog lied to four types out of five** | Every content type was told "a page linked from a menu is removed from the menu too". | PAG-04 and one delete of each other type |
| **Requests through the platform gateway 404'd** | The `/spg/**` route predicates on `pod` as well as `store`; a URL with only `store` looks like missing content. | Any `.http` block — they all carry `pod={{POD_ID}}` |

---

## 99 — Known gaps

Behaviour that is expected today. Please don't spend time raising these — but do shout if you see something
*beyond* what is described.

**No in-console storefront preview.** The preview token exists and the storefront honours it, but the console
does not know a store's public host (that is a router allocation in `merchant-service` plus a Caddy lookup), so
no preview link is offered. The publish checklist and the SERP card are what the editor shows instead.

**MinIO has no volume.** Media uploaded locally disappears when the container restarts, and the seeded demo
images 404 from the start because the seeds reference file names that were never uploaded. Broken images on the
storefront are the local stack, not the CMS. A stale browser cart from before a restart also fails every
add-to-cart with `CHECKOUT.CART.NOT_FOUND` — clear the `cart` keys in `localStorage`.

**The media quota is one platform-wide number, not a plan entitlement.** 5 GiB for everyone, from
`com.asrevo.cvhome.content.media.quota`. Billing exposes no storage entitlement to read.

**List search is a substring match.** `?q=` is an `ILIKE` over slug, title and body — no prefix or fuzzy
matching, no full-text index. Fine for a store's few hundred items.

**Reordering is up/down buttons, not drag and drop.** The console has no drag-and-drop primitive; the server
takes the whole order either way.

**"Email existing customers about the change" is recorded, not sent.** A policy's `notifyCustomers` is stored
and nothing consumes it — there is no customer-notification channel on the platform.

**No audience targeting, machine translation, banner analytics, FAQ votes or blog comments.** A banner models
`loggedInOnly` and nothing else. None of the services these would need exist.

**Content is store-scoped, with no org-level sharing.** An item belongs to the store it was created in; the
design's "Stores: All stores" picker is omitted rather than shown disabled.

**No storefront builder.** `template`, `meta.blocks` and `ContentData.blocks` are a deliberate seam with no UI
behind them.

**Delete is permanent.** No soft-delete window and no trash, for any content type or media asset.

---

Raise anything unexpected against PR #276. Include the store id, the content id, the time and the matching lines
from `.lcl/default/logs/content.log` — the scheduler, the media writes and the storefront reads are all
asynchronous or cached, so the log is usually the only place the real cause appears. For a console defect,
attach the browser console and the failing request from the network panel: a 403 there is a permission problem,
a 409 is a version conflict, and a 404 through `/spg/**` is usually a missing `pod` parameter.
