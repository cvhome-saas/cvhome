# QA — console-ui (`store-core/console-ui`)

The seller console is the one screen a merchant actually uses: the team, invitations and the account page, the
Catalogue, Content and Store-management modules, Subscription and usage, the platform's pod screens, and the
four themes all of it is drawn in. It owns no data — every case here is the console's handling of another
service's answer.

- **Scope** — every console module and the cross-cutting behaviours: the store switcher, RTL, themes, and
  honest reporting of a partial failure when one screen writes two services
- **Runs on** — `lcl start -d --stack <name>`; the console is served through the gateway at
  `http://gateway.com:8000/` and also answers on `http://console-ui.gateway.com:8000`. Read the live port from
  `lcl urls` — **never assume 8000**
- **Cases** — 86 (37 verified, 12 unit only, 37 not verified)
- **Also see** — the service behind each module:
  [tenancy](../../tenancy/tenancy-service/qa/tenancy-qa.md) (users, stores, invitations),
  [catalog](../../../store-pod/catalog/catalog-service/qa/catalog-qa.md),
  [content](../../../store-pod/content/content-service/qa/content-qa.md),
  [merchant](../../../store-pod/merchant/merchant-service/qa/merchant-qa.md),
  [billing](../../billing/billing-service/qa/billing-qa.md),
  [pod-registry](../../pod-registry/pod-registry-service/qa/pod-registry-qa.md)

Each case is tagged:

- **[verified]** — run against a running stack and passed.
- **[unit only]** — covered by the named test; nobody drove it through the browser.
- **[not verified]** — never run end to end by anyone.

**Prefixes name the module**, because five source documents each called their console section `UI`:
`U`/`INV`/`P`/`L` are the team, invitations, account page and layout; `CAT`, `CNT`, `MER`, `BIL` are the
Catalogue, Content, Store-management and Subscription modules; `THM`/`TOK`/`SEL`/`A11Y` are the themes;
`SW` is the store switcher.

---

## 00 — Before you start

**Shared prerequisites** — starting the stack, the demo logins and the seeded ids are in
[`references/qa-testing.md`](../../../.claude/skills/project-structure/references/qa-testing.md) §§1–5. Only
what is specific to the console is below.

**Sign-in.** `http://gateway.com:8000` (or `http://console-ui.gateway.com:8000`) — `org1-admin` / `admin`.
Every page is scoped to the store in the sidebar switcher; open `ORG1-STORE1` unless a case says otherwise.

**Accounts the seed gives you** (`store-core/uaa/src/main/resources/init-sql/data-test-stores.sql`), all with
password `admin` until a case changes one:

| Username | Role | Store metadata |
|---|---|---|
| `org1-admin` | ORG_ADMIN | **none** — this is the point of [U-05](#u-05--an-org-admin-is-in-no-list-and-the-page-says-so--critical--verified) |
| `org1-store1-admin` | STORE_ADMIN | ORG1-STORE1 |
| `org1-store1-moderator` | STORE_MODERATOR | ORG1-STORE1 |
| `org1-store2-admin` | STORE_ADMIN | ORG1-STORE2 |
| `org2-admin` | ORG_ADMIN | another org — for isolation cases |
| `super-admin` | SUPER_ADMIN | the platform screens |

**If you change a password, change it back**, or note it: the seed only runs on a clean database.

**Switching theme.** Fastest is the **swatch-and-name control in the console toolbar**, left of the account
menu; Profile → *Preferences* → **Theme** has the same list with hints. Four choices: Forest (default),
Midnight, Daylight, **Light**. The choice is stored in `localStorage` under `cvhome.console.theme` and applied
by an inline script in `index.html` *before first paint* — which is why THM-02 exists.

**Reading a failure.** `read_console_messages` and `read_network_requests` (the `claude-in-chrome` skill) are
the fast path for "the page is blank" or "the save button does nothing": a 401 there is a missing session, a
403 is a permission problem, a 409 is a version conflict, and a 404 through `/spg/**` is usually a missing
`pod` parameter.

Log: `.lcl/<stack>/logs/console-ui.log`.

---

## U — the team list

### U-01 — The same people as the old console · critical · [verified]

`/users` in the new console, `/pages/user-management/users` in the old, both on ORG1-STORE1.

**Expect** — the same usernames, emails and active flags, and the same total.
- **Seen** — two users in both: `org1-store1-admin` and `org1-store1-moderator`.

### U-02 — Paging is by `count`, not `size` · critical · [verified]

Network tab on `/users`.

**Expect** — `GET …/user-account/list?page=0&count=20&store=…&pod=…`, and the response honours it.
- **Why it is called out** — `UserAccountApi.list` takes a bare `Pageable`, and nothing in tenancy names the
  parameter. It is `count` because `store-commons:autoconfigure`'s `ServletWebConfig` renames it
  platform-wide and tenancy depends on that module. seller-ui sends `count` while rendering ten rows a page
  and its author assumed the server ignored it; it does not.
- **Seen** — `count=20` on the wire, 200.

### U-03 — Every private call is store-scoped, and nothing fires twice · high · [verified]

**Expect** — on one load of `/users`: `store-manager/list`, `user-account/list`, `user-account/assignable-roles`
and `org-member/invitations`. Four requests, each with `?store=` and `?pod=`, none repeated.
- **Seen** — exactly that.

### U-04 — Switching store changes the team · critical · [not verified]

Switch the sidebar to ORG1-STORE2.

**Expect** — the list becomes ORG1-STORE2's staff, the page resets to the first page, and the notice above the
table names the new store.

### U-05 — An org admin is in no list, and the page says so · critical · [verified]

Look for `org1-admin` in ORG1-STORE1's list, then in ORG1-STORE2's.

**Expect** — **absent from both.** `ManagedUserAccountServiceImpl.list` filters uaa on `{org, store}` and
`org1-admin` has no `store` in its metadata, so it is in no store's list — including its own. This is a
platform gap, not a console one: `lessons.md`, "Users — the user list is store-scoped, so an org admin is in
no list".
- **Expect also** — the blue notice above the table explains it. Without that, an operator counts heads and
  gets the wrong number with no way to know.
- **Seen** — absent, notice shown.

### U-06 — Create, edit, enable, disable, delete · critical · [not verified]

Create `qa-newbie` with an email, a password and the Store moderator role. Then edit the name, block sign-in,
allow it again, and delete.

**Expect** — each round-trips: reload the old console and it agrees. A write **re-reads** rather than patching
the row, so the page shows what the server normalised.
- **Delete only accounts you created.**

### U-07 — A taken username is reported, not swallowed · high · [not verified]

Create a user with the username `org1-store1-admin`.

**Expect** — a 409 surfaced as a message. It arrives as `COMMON.DATA_INTEGRITY_VIOLATION` with no
`fieldErrors[]`, so it lands as a toast rather than on the username field — the same shape signup hits. There
is no pre-flight check because none is reachable: `lessons.md`, "Users — a taken username cannot be checked
before submitting".

### U-08 — You cannot act on yourself · high · [unit only]

Sign in as `org1-store1-admin` and open your own row.

**Expect** — **Block sign-in** and **Delete** are disabled with a reason on hover. Disabling yourself ends
your own session on the next request; the server stops neither, so the console does.

### U-09 — A moderator reads and cannot write · critical · [not verified]

Sign in as `org1-store1-moderator`.

**Expect** — the list renders in full, and **no Add user, no row pencil, no rail actions**. `USERS.LIST`
resolves to the store's read audience while every write resolves to `hasMaintainAccessOnUsers`.
Then confirm the server agrees by calling `create` directly — it must be **403**, not 200. The console mirrors
the server; it does not replace it.

### U-10 — The role picker never offers platform superuser · critical · [unit only]

Open the create form.

**Expect** — **Store administrator** and **Store moderator**, and nothing else.
`GET …/assignable-roles` really does return `SUPER_ADMIN` to an org admin — it filters uaa's role table by
removing only `USER` and `ORG_ADMIN`. The console intersects rather than filters one name, so a role added to
uaa later cannot appear unreviewed either. **This is defence in depth, not a fix**: `lessons.md`, "Users —
assignable-roles offers SUPER_ADMIN to an org admin".

### U-11 — A role the console has never seen · high · [unit only]

Add a role to `uaa.roles` and grant it to a user.

**Expect** — the row humanizes it (`REGIONAL_BUYER` → `Regional Buyer`) rather than the page going blank.
Transloco throws on a missing key and a role is a database row, not an enum.

---

---

## INV — invitations

`OrgMemberApi` has been complete and tested since tenancy phase 11 and **no frontend has ever called it**.
seller-ui has no invitations at all, so there is nothing to compare against — read
`qa/tenancy-and-pod-registry-split.md` LIF-04 for the API-level run.

### INV-01 — The token is shown once · critical · [verified]

`/users` → **Invitations** → **Invite user** → `QaNewbie@Example.COM`, Store administrator → create.

**Expect** — a dialog with a copyable link and a warning that this is the only time it can be shown. It is a
**dialog, not a toast**: only the token's hash is stored, so closing it loses the link for good.
- **Expect also** — the row appears immediately as `qanewbie@example.com` — the server lowercases it.
- **Seen** — both.

### INV-02 — The list never leaks a token · critical · [verified]

`GET …/org-member/invitations` in the network tab.

**Expect** — no `token` field on any row. `InvitationDto` deliberately omits it.
- **Seen** — absent.

### INV-03 — Only a pending invitation is actionable · high · [verified]

**Expect** — Resend and Revoke on `PENDING` rows, and **no actions** on `ACCEPTED`, `REVOKED` or `EXPIRED`.
- **Seen** — two actions on the pending row, none on the revoked one.

### INV-04 — Resend rotates rather than repeats · high · [unit only]

Resend a pending invitation.

**Expect** — a link dialog with a **different** token. The old link stops working. There is no "show me that
link again" because it is impossible.

### INV-05 — A duplicate invitation is refused · high · [not verified]

Invite the same address twice while the first is pending.

**Expect** — **409** `INVITATION.ALREADY_EXISTS`, surfaced as a message. Revoke first, or resend.

### INV-06 — Accepting · critical · [not verified]

Sign in as a user who is **not** in ORG1 and open the link.

**Expect** — the accept page (auth chrome, not the console shell), a button, then "You are in" and a link to
the console. Afterwards the invitation reads `ACCEPTED` and the user is in `GET …/org-member/list`.
- **Note** — the page is deliberately outside the console shell: an invitee is authenticated and not yet a
  member, so `consoleContext` and `requiresStore` would both refuse them. `OrgMemberApi.accept` carries no
  permission token for the same reason — the token in the link is the authorization.

### INV-07 — Accepting is never automatic · critical · [unit only]

**Expect** — opening the link sends **nothing**. The request only fires when the button is pressed, and a
second press does not fire it again. The token is single-use and burned on the first success, so accepting on
load would turn a refresh or a link preview into "already used".

### INV-08 — A used, revoked or bogus token · high · [verified]

Open `/accept-invitation?token=not-a-real-token`, and `/accept-invitation` with no token at all.

**Expect** — "This invitation cannot be used", the reason on the page rather than in a toast, and advice to
ask for a new one. A toast dismisses itself, and this is the whole content of the screen.
- **Seen** — the no-token case reads correctly.

### INV-09 — A store admin cannot invite · high · [unit only]

Sign in as `org1-store1-admin`.

**Expect** — **no Invitations tab at all**, and the pending count on the KPI row reads **—**, not 0.
`OrgMemberApi` is org-admin-only class-wide, so a store admin can create accounts in their own store and
cannot invite anyone into the organization — and "nobody is waiting to join" is a claim the page has not
earned.

---

---

## P — the account page

### P-01 — The toolbar goes somewhere · high · [verified]

Profile menu → **Profile**.

**Expect** — `/profile`. There is **no Settings item** beside it any more: there is no console-wide settings
page and settings are per store, at `/store-management`.

### P-02 — What it shows, and what it says instead · high · [not verified]

**Expect** — the username, the roles, and a notice explaining that the console can see nothing else.
**No name, email, avatar, phone, job title, timezone, date format or bio** — none has a column anywhere, and
the account record is unreachable twice over (`lessons.md`, "Users — the JWT carries no user id"). Empty
fields would read as "you have not filled these in"; the notice is the honest version.

### P-03 — No password control · high · [unit only]

**Expect** — nothing about passwords on `/profile`. A self-service change needs the caller's own user id and
the JWT carries the username instead, so the action lives on `/users` where a row has a real id.

### P-04 — The preferences are real, and say what they are · high · [not verified]

Switch language and theme.

**Expect** — both take effect immediately, and the page says they are remembered **in this browser only**.
There is nowhere to keep them against an account: `lessons.md`, "Shell — no user-preferences endpoint".

### P-05 — `/profile` works without a store · high · [not verified]

An account with no store at all (a fresh signup, before create-store).

**Expect** — the page opens. It is the only console route without `requiresStore`, deliberately: a personal
page is not a reading of a store.

---

---

## L — layout, language and themes

### L-01 — The master-detail layout appears when it fits · high · [verified]

Open a user on `/users`, then collapse the sidebar or widen the window.

**Expect** — narrow: the rail sits **under** a full-width table. Wide (container ≥ 1120px): the rail sits
**beside** it and the table is still a table.
- **Why 1120** — `app-data-table` drops to stacked cards below its own 45rem container query, so the table
  needs 720px; 720 + 1rem + 24rem of rail is 1120. Splitting sooner gave a two-pane layout whose left pane
  was no longer a table.
- **Seen** — both, and the intermediate state that motivated the number.

### L-02 — Arabic · critical · [not verified]

Every screen in this document, in Arabic.

**Expect** — the page mirrors; **emails, usernames and the invitation link stay left-to-right** inside it
(`unicode-bidi: plaintext`); dates and counts are in Arabic digits.

### L-03 — Three themes, three widths · high · [not verified]

Forest, Midnight and Daylight at 1440 / 900 / 420.

**Expect** — no literal colours, no clipped dialog, and the four dialogs on `/users` (delete, set password,
invite, invitation link) all readable at 420.

---

---

## CAT — The Catalogue module

_From `qa/catalog-and-inventory.md` §UI, renumbered `UI-NN` → `CAT-NN`. Four of the five source documents called their console section `UI`; the prefix now names the module._

Products (`/products`), the product form (`/products/new`, `/products/{id}`) and Catalogue (categories,
brands, types, groups). Specs: `features/products/**`, `features/product-form/**`, `features/catalogue/**`,
`api/catalog/*.spec.ts`, `api/inventory` — 962 Karma cases pass, which is what every [unit only] below means.

Products (`/products`), the product form (`/products/new`, `/products/{id}`) and Catalogue (categories,
brands, types, groups). Specs: `features/products/**`, `features/product-form/**`, `features/catalogue/**`,
`api/catalog/*.spec.ts`, `api/inventory` — 962 Karma cases pass, which is what every [unit only] below means.

### CAT-01 — The product table merges price and stock from inventory · critical · [verified]

- **Steps** — open Products as org1-admin.
- **Expect** — rows with name, categories, brand, image, **price and quantity** (one bulk inventory call for
  the page's skus — check the network tab: `GET /spg/inventory/api/v1/availability?skus=…`). A product with
  no inventory row shows `0` and no price, not an error.

### CAT-02 — Inline edit writes two services · critical · [unit only] (`products.api.service.spec.ts`)

- **Steps** — change price and quantity of a row inline, toggle availability, save.
- **Expect** — a `PATCH /spg/catalog/api/v1/private/product/{id}` with **both** switches and a
  `PUT /spg/inventory/api/v1/private/inventory/{sku}` with `{productId, quantity, available, price: {amount}}`;
  the row re-renders from the reload. An empty price sends `amount: 0`, not nothing.

### CAT-03 — The product form loads the definition and the stock · critical · [unit only]

- **Steps** — open a seeded product.
- **Expect** — the Pricing step shows the inventory price (`originalPrice`, falling back to `finalPrice`) and
  quantity; "can be purchased" reflects the inventory record's `available`; the definition steps show every
  language, the brand and type by code, the categories ticked.

### CAT-04 — Create from the form is two writes, and a partial failure is honest · critical · [unit only]

- **Steps** — create a product with a price; then repeat with inventory stopped.
- **Expect** — `POST /spg/catalog/api/v2/private/product` then `PUT /spg/inventory/.../{sku}`; on the second
  run the definition lands, the inventory write fails, and the form **says so** (`inventoryApplied: false`)
  rather than pretending — the product exists with no stock, which UI-01 then shows as `0`.

### CAT-05 — Update merges categories by diff · high · [unit only]

- **Steps** — tick one category, untick another, save.
- **Expect** — one `POST .../category/{added}` and one `DELETE .../category/{removed}`, run **sequentially**
  (`concat`, not `forkJoin`), never a `PUT` with `categories` in the body.

### CAT-06 — Delete cleans inventory best-effort · high · [unit only]

- **Steps** — delete a product from the table.
- **Expect** — `DELETE /spg/catalog/api/v1/private/product/{id}` then
  `DELETE /spg/inventory/api/v1/private/inventory/by-product/{id}`; the second failing does not fail the
  delete.

### CAT-07 — The category tree page · critical · [not verified]

- **Steps** — open Catalogue → categories; create a root, a child under it; rename; move; hide; delete.
- **Expect** — each maps to the CAT endpoints (create/`PUT`/`move`/`visible`/`DELETE`); the tree re-renders
  with names in the console's language from `descriptions`; the uniqueness check runs on the code field.

### CAT-08 — Brands, types and groups pages · high · [not verified]

- **Steps** — create / edit / delete one of each; add and remove a group member.
- **Expect** — the BRD / TYP / GRP endpoints, in the shapes those sections describe; the brand page shows no
  logo and no publish flag (by design — `lessons.md`).

### CAT-09 — Related products picker · high · [not verified]

- **Steps** — on a product, search the picker by **sku** (not name), add two related products, remove one.
- **Expect** — the picker searches `GET /api/v2/products?sku=` (the only text filter the catalog has); adds
  and removes hit `/private/products/{id}/relationship/{productId}`.

### CAT-10 — The moderator can read and cannot write · critical · [not verified]

- **Steps** — as `org1-store1-moderator`, open every page above and try one write on each.
- **Expect** — pages render (the reads are public or read-permitted); every write is a **403** surfaced as a
  disabled control or an error, never a silent no-op.

### CAT-11 — Arabic, right to left · high · [not verified]

- **Steps** — switch the console to `ar`, open Products, the form, Catalogue.
- **Expect** — product and category names in Arabic (from `descriptions`, matched on `ar`), layout mirrored,
  the price column still showing the store currency correctly formatted.

---

---

## CNT — The Content module

_From `qa/content-platform.md` §UI, renumbered `UI-NN` → `CNT-NN`._



### CNT-01 — The hub shows seven tabs and honest counts · critical · [verified]

- **Steps** — open `/content` as a store admin.
- **Expect** — four KPI cards (published, drafts, awaiting translation, media) and seven tabs — pages, posts,
  banners, FAQ, media, menus, policies — each with a count that matches the number of rows in its list. "All
  files" comes from the summary, not from the current page of the grid.

### CNT-02 — Every editor opens · critical · [verified]

All five editors crashed on open at one point, on three separate defects. This is the cheapest high-value check
in the document.

- **Steps** — open New Page, New Post, New Banner, New FAQ Entry, New Policy, and one existing item of each
  type by deep link (paste the URL into a fresh tab).
- **Expect** — every one renders with no console error. `NG01203`, `NG0951` or a blank panel is the regression.

### CNT-03 — Save and Publish are never dead buttons · critical · [verified]

They used to disable themselves on a form invalidity whose cause (the slug) sits below the fold, so the seller
saw two buttons that did nothing and no reason why.

- **Steps** — open New Page, type a title only, press **Save draft**, then press **Publish**.
- **Expect** — the button is clickable, the first offending field is scrolled into view and marked, and a
  message says what is missing. Never a silent no-op.

### CNT-04 — A newly published item shows as published · high · [verified]

Publishing ran two loads and the older response could land last, leaving a published item wearing a DRAFT
badge until a reload.

- **Steps** — create and publish in one go, without reloading.
- **Expect** — the badge reads **Published** immediately, and the success panel matches.

### CNT-05 — The editor opens in the store's source language · high · [verified]

- **Steps** — on org2-store2 (Arabic-first), open any editor.
- **Expect** — the locale strip starts on the store's own source locale, not on English, and the publish
  checklist judges **that** locale — the same one the server's gate will judge.

### CNT-06 — Arabic, right to left, on every content screen · high · [verified]

- **Steps** — switch the console to Arabic and walk the hub, all seven tabs, the bulk bar, all five editors, the
  media drawer, the menu editor and the policy version history.
- **Expect** — no raw keys such as `content.list.empty` on screen; the layout mirrors — rails, chips, the locale
  strip, table alignment, the up/down reorder arrows; no literal "null" tooltips; no English word baked into a
  row subtitle.

### CNT-07 — Errors say something a seller can act on · high · [verified]

- **Steps** — trigger, in Arabic and English: a duplicate slug, an incomplete publish, a full banner placement,
  a referenced media delete, a version conflict.
- **Expect** — each shows copy specific to the code, in both languages. Where the failure names a narrower
  cause, that is what is shown — a full banner placement must not say "write the title and body". A bare
  `errors.content.…` key on screen is a defect.

---

### PAG-06 — The store home card still writes the landing snippet · high · [not verified]

Module 5's store-management home card was repointed at `snippets/LANDING_PAGE` when the old box service was
deleted. Same screen, different backend — so it is worth one check that it did not silently stop saving.

- **Steps** — Store management → home section, edit the text, save, reload, then check the storefront home.
- **Expect** — it persists and renders. A 404 in the network panel here means the repoint is wrong.

---

---

## MER — Store management

_From `qa/merchant-store-service.md` §UI, renumbered `UI-NN` → `MER-NN`. Eight sections behind one rail; payments and social login belong to payment-service and cua and are listed there only because they share the page._

Eight sections behind one rail: details, branding, slider, social, domain, home, payments, social login. (The
last two belong to payment-service and cua; they are listed here only because they share the page.)

Eight sections behind one rail: details, branding, slider, social, domain, home, payments, social login. (The
last two belong to payment-service and cua; they are listed here only because they share the page.)

### MER-01 — The page follows the store switcher · critical · [verified]

The bug worth the whole QA pass: switching stores left the page showing the **previous** store's settings — its
domains, its landing copy, its gateway secrets — while the request context had already moved on, so the next
save would have written one store's values onto the other.

- **Steps** — open store management for org1-store1, note a distinctive value, switch to org1-store2 with the
  rail, and look again. Then switch back and save something.
- **Expect** — every section reloads for the new store. Nothing from the previous one survives on screen for
  even a moment. This is the worst kind of wrong because it looks fine.

### MER-02 — Fields the platform does not record are honest about it · high · [verified]

Six designed fields (legal name, tax number, slug, category, timezone, short description) and both visibility
switches have no counterpart anywhere on the platform.

- **Expect** — they render **disabled** inside a "Not recorded by the platform" block with the reason beside
  them, and they are disabled in the form service so they can never reach a request body. The header carries
  **no** published/unpublished badge — a badge that always reads "Published" is an assertion, not a fact.

### MER-03 — The home section writes the landing snippet · high · [not verified]

This card was repointed at content-service's snippets API when the old box endpoints were deleted. Same screen,
different backend.

- **Steps** — edit the headline, body and search snippet; save; reload; check the storefront home.
- **Expect** — it persists and renders. A 404 in the network panel means the repoint is wrong. Arabic copy
  typed while the console is in English must render right-to-left (`dir="auto"`), not as reversed nonsense.

### MER-04 — The custom-domain field refuses a domain that does not point here · high · [not verified]

The server records whatever hostname it is given, so the client check is the only check there is.

- **Steps** — type a domain with no CNAME at all; then one pointing elsewhere; then block `dns.google` (an
  offline network or a blocker) and type a valid one.
- **Expect** — the first two are refused with what the resolver found and Save stays out of reach; the third
  **warns and allows**, because a resolver the browser could not reach says nothing about the operator's DNS.
  An allocated domain shows a badge only once a re-check has actually run — never a "not checked" badge under a
  domain the console itself required a passing lookup for.

### MER-05 — The address section says where the store lives, or says it cannot · [not verified]

The storefront hostname is assembled client-side from two calls on two tiers; either can be refused.

- **Expect** — the default subdomain row and the CNAME target render for a healthy store. When either leg is
  refused, the section reads "Address not available" and hides the DNS record block — never a half-built
  hostname, which would send an operator to their registrar with a value that can never resolve.

### MER-06 — Arabic, right to left, across all eight sections · high · [not verified]

- **Steps** — switch the console to Arabic and walk every section, including the branding wells, the slider
  rows, the domain panel and the settings rail folded to its icon strip.
- **Expect** — no raw keys on screen, mirrored layout, and accessible names surviving the rail's fold. A
  section stuck under its loading veil with an idle network is the known missing-key failure — `npm run lint`
  now has `lint:i18n-missing` to catch it before it ships.

---

### MER-07 — The console refuses the wrong shape before it uploads · high · [verified]

`accept=` is advisory in every browser, so type, weight and pixel dimensions are checked client-side and the
refusal quotes the actual file.

- **Steps** — drop a 1920×480 image on the **logo** well.
- **Expect** — refused by name and shape ("that image is 1920 × 480, which is the wrong shape for this slot")
  and **no request is made** — confirm in the network panel. Repeat on the banner well (4:1) and the slider
  add-zone (2.5:1).
- _Was BRD-02 in `qa/merchant-store-service.md`._

### MER-08 — An upload says it finished · [verified]

- **Expect** — the well spins while the request is in flight and holds a tick afterwards, both floored to a
  visible duration. A local upload round-trips faster than the eye, so without the floor a completed upload and
  a missed click look identical.
- _Was BRD-03._

### MER-09 — There is no way to remove a logo, and the console says so · [not verified]

Expected behaviour, not a defect.

- **Expect** — no Remove button on the logo or banner; the card reads "uploading again replaces the current
  image. The platform has no way to remove one once set." If a Remove button exists, it is a 404 waiting to
  happen — file that.

---
- _Was BRD-06._

### MER-10 — A link must belong to the provider whose row it is in · high · [verified]

Nothing server-side checks that a `SocialLink`'s provider and URL agree, so the console is the only place it
can be enforced — and a TikTok URL under a Facebook mark sends shoppers somewhere else.

- **Steps** — put a TikTok URL in the Facebook field; then a bare `facebook.com` with no profile after it.
- **Expect** — both refused by name, with the expected shape spelled out. `twitter.com` and `fb.com` are
  accepted deliberately (those companies still serve them), and subdomains count.
- _Was SOC-02. There is **no server-side equivalent** — nothing checks that a link matches the provider whose row it is in._

---

## BIL — Subscription and usage

_From `qa/billing-per-store-subscriptions.md` §UI, renumbered `UI-NN` → `BIL-NN`. UI-07 (the public price list) is the storefront's and moved to [landing-ui-qa.md](../../../store-pod/landing-ui/qa/landing-ui-qa.md)._



### BIL-01 — The page follows the store switcher · high · [verified]

Subscriptions belong to stores now. Switching store must change what the page says.

- **Steps** — with one store paying and another not, switch between them.
- **Expect** — status, plan, renewal date and invoices all change with the store. No stale figures from the
  previous one.

### BIL-02 — Every status reads correctly · high · [not verified]

Walk one store through each state and check the wording and colour each time.

| State | The page should say |
|---|---|
| Trial | Trial, with the date it ends |
| Active | Active, with the next renewal date |
| Renewal off | Still Active, plus "will not renew" and the date access ends |
| Downgrade pending | The *old* plan, plus the new one and when it starts |
| Payment failed | Payment failed, with the date it must be fixed by |
| Suspended / not subscribed | A prompt to choose a plan |

### BIL-03 — Plan cards compare like for like · [verified]

A more expensive plan showing *fewer* lines than a cheaper one was a real bug here.

- **Expect** — every card lists the same features in the same order, so values line up across columns. Pro shows
  **∞** for products and orders rather than omitting those rows.

### BIL-04 — The Month/Year toggle is honest · [verified]

- **Expect** — yearly shows yearly prices. Free, sold monthly only, disappears from the yearly view rather than
  showing a made-up figure.

### BIL-05 — Invoices link to Stripe's own documents · [verified]

- **Expect** — each paid invoice has a working PDF link and an amount matching what was charged. A store that
  has never paid shows no invoice section — not an error.

### BIL-06 — All five languages, and Arabic right-to-left · high · [not verified]

> **Known to be unchecked.** The Arabic strings are in, but the layout was never reviewed right-to-left. Treat
> this as a real hunt.

- **Steps** — switch through en, ar, es, fr, ru on the subscription page. In Arabic, check the plan cards,
  status chips, alerts and the invoice table.
- **Expect** — no raw keys such as `SUBSCRIPTION.RENEWS_ON` on screen. In Arabic the layout mirrors properly —
  values, chips and the table read right-to-left rather than sitting on the wrong edge.

### MIG-05 — The old endpoints are gone and nothing still calls them · high · [verified]

- **Steps** — call `/control-plane/api/v1/subscription-plan/public/tables` and
  `/control-plane/api/v1/subscription/subscription-plan-details`; then click through the console — org
  management, store management, the public pricing page — watching the browser's network tab for 404s.
- **Expect** — the endpoints are gone, and **no screen calls them**. The second half matters more than the
  first.

---

---

## PDR — The platform pod screens

_From `qa/tenancy-and-pod-registry-split.md` §PDR and §CNV — the cases whose assertion is the console's. They
need the `super-admin` login. `pod-registry` owns the data:
[pod-registry-qa.md](../../pod-registry/pod-registry-service/qa/pod-registry-qa.md)._

### PDR-14 — seller-ui builds and points at the new service · [verified]

```bash
cd store-core/seller-ui && npm run build
grep -rn "tenancy/api/v1/pod" projects/ src/     # expect nothing
```

`POD_API_BASE` moved to `/pod-registry/api/v1/pod` — one line, because phase 1 hoisted it.

### PDR-15 — The super-admin pod screens in the browser · high · [not verified]

- **Steps** — create, rename and delete a pod through the console as `super-admin`.
- **Why it matters** — `getPod(id)` now reaches an endpoint that is **super-admin only**, where tenancy's
  equivalent also admitted org admins. An org admin opening a pod detail page now gets a 403 rather than a body.
  That is intentional — lifecycle, capacity and health are operator data — but it is a user-visible change
  nobody has looked at. Exercised at the API level only (PDR-11, PDR-12, `pod-api.http`).

---

### CNV-02 — seller-ui builds · [verified]

`cd store-core/seller-ui && npm run build`.

> PDR-14 and CNV-02 were written against **seller-ui**, which no longer exists. They are kept because the
> console inherited both screens; re-run them there.

---

## SW — The store switcher and the merged store id

_From `qa/unify-store-id-value-objects.md` §SW, renumbered `S1…S7` → `SW-01…SW-07` and reformatted into the
case shape used everywhere else. **Every one is re-tagged `[not verified]`**: they were verified against
seller-ui, which has since been deleted, so a passing run on seller-ui says nothing about the console._

The console consumes `ManagerStore.id`, which changed from `{id: "65f0…"}` to `"65f0…"`. A wrong unwrap shows
as a blank store name, an empty switcher, or `[object Object]` in a URL.

### SW-01 — The switcher lists the org's stores by name · [not verified]

Sign in as `org1-admin`. The header store switcher lists the org's stores **by name** (ORG1-STORE1,
ORG1-STORE2). An empty dropdown or a blank label means the id/name mapping broke.

### SW-02 — Switching store carries the bare id into every request · critical · [not verified]

Switch stores in the header. The page reloads and the selection persists. The requests the console makes carry
the bare 24-char hex — observed `spg/catalog/api/v2/private/base-products?…&store=65f023632bc46470c104b76f`
flipping to `…b75f` after the switch, all 200, never `[object Object]`.

### SW-03 — A browser holding the *old* stored shape self-heals · high · [not verified]

A browser that used the console *before* the id merge holds a `Selected-ManagerStore-Id` localStorage entry in
the old shape. It self-heals: the shape mismatch fails the id comparison, `currentSelectedStore()` returns
undefined and the first store is selected instead. Reproduce by planting
`{"id":{"id":"65f023632bc46470c104b76f"},"name":"ORG1-STORE1"}` and reloading — expect a recovery to
ORG1-STORE1, its products loaded, and the key **rewritten** with `id` as a scalar (`orgId`/`podId` still
objects).

### SW-04 — The store list renders the plain hex · critical · [not verified]

Store management → list. The ID column renders the plain hex. **This is where the one real defect of that
change was found**: it rendered blank because the template did `{{ value.id }}` (fixed to `{{ value }}`). Worth
re-checking on any change to that table — a grep could not have caught it, because the nesting was split
between the `let-value` binding and the interpolation.

### SW-05 — The permission gate passes on the merged type · [not verified]

Catalogue → Products with a store selected: rows load. Exercises
`CustomPermissionEvaluator` → `PermissionAccessChecker.hasManageAccessOnStore` on the merged type.

### SW-06 — Switching store changes the catalog, with no overlap · [not verified]

Repeat SW-05 after switching stores: ORG1-STORE1 shows fashion SKUs (`SKU-NK-RUN-001`, `SKU-GU-BG-MAR05`),
ORG1-STORE2 shows `ELEC-SKU-*`. No overlap.

### SW-07 — Subscription and usage renders, and a missing row is a 404 not a 403 · [not verified]

Subscription & usage renders the plan catalogue (Free/Basic/Pro with entitlements). With no subscription row
the page also fires `subscription/current` and shows a "couldn't find" toast — that is a **404, not a 403**,
which is the point: billing's `@PreAuthorize` on the merged type passed and the endpoint reached "not found".
`orgId`/`podId`/plan-price ids are deliberately **still** `{id: …}` objects — do not "fix" those.

---

## THM — The theme itself

Light is one `[data-theme='light']` block and four wiring points: the stylesheet, `CONSOLE_THEMES`, the
pre-paint allow-list in `index.html`, and the locale keys. Each case below covers one of them, because each
fails differently and only one fails loudly.

### THM-01 — Light is offered and applies · critical · [verified]

- **Steps** — sign in, open Profile → Preferences → Theme.
- **Expect** — four options. **Light** with the hint "White panels on cool grey". Selecting it repaints
  immediately, with no reload: grey canvas, white panels, emerald accents.
- **Seen** — all four listed with swatches and hints, Light ticked. Note Daylight's and Light's swatches are
  near-identical pale-emerald circles; see [99](#99--known-gaps).

### THM-02 — The choice survives a reload · critical · [verified]

The trap this case exists for: `index.html` holds a `themes` allow-list, and a theme missing from it is stored
correctly and then **silently rejected on the next load**. It presents as "my theme keeps resetting to Forest"
and points at nothing — the array is in an inline script, not in any Angular code.

- **Steps** — select Light, then hard-reload the page. Then close the tab and reopen the console.
- **Expect** — Light both times, with **no flash of dark** before it paints. `localStorage` and
  `document.documentElement.dataset.theme` both read `light`.
- **Seen** — after reload: `dataset.theme` `light` *at first paint*, `--muted` `#fff`, `--border` `#e2e8f0`,
  `color-scheme: light`. The allow-list accepted the new id.

### THM-03 — Arabic and RTL · high · [verified]

- **Steps** — with Light active, switch the language to العربية. Walk two or three screens.
- **Expect** — the label and hint are translated ("فاتح" / "لوحات بيضاء على رمادي فاتح"). Layout mirrors as it
  does on every other theme — this change adds no direction-sensitive property, so an RTL break here would be
  pre-existing.
- **Seen** — orders page fully mirrored (rail right, breadcrumb right-aligned, KPI order reversed), theme
  control reading **فاتح**. No layout defect attributable to the theme.

### THM-04 — The other three themes are unchanged · critical · [verified]

The fourteen stylesheet edits and the `::selection` change are shared by every theme, so this is the case that
proves the blast radius.

- **Steps** — cycle Forest → Midnight → Daylight on the dashboard, an orders list and a settings page.
- **Expect** — each looks as it did before the branch, except the two deliberate improvements in
  [REG](#reg--regression-watchlist): hover fills are slightly more distinct, and selected text is legible.
- **Seen** — Forest and Daylight both correct on the orders page. **Midnight was not opened in the app** —
  only in a static render of the built bundle. Worth a minute of someone's time.

---

---

## TOK — The repaired tokens

`--muted` is the panel surface; `--input` is hover fills, icon tiles and inset wells. Fourteen rules were
using `--muted` for `--input`'s job. On the dark themes the two are 2% and 6% white — near enough that it read
as intentional. On Light `--muted` is **opaque white**, so every one of them vanished against its own panel.

These cases each look at one repaired rule on Light. If a tint is invisible, the rule was missed.

### TOK-01 — Row hovers are visible · high · [verified]

- **Steps** — on Light: hover rows in a notification list, a ranked list (dashboard), the customer dialog's
  order list, and platform billing's blocked-store row.
- **Expect** — a distinct grey fill under the pointer on every one. White-on-white means a missed rule.
- **Seen** — sidebar nav hover shows a clear grey `--input` fill with emerald ink against the white rail.
  Only the sidebar was hovered; the four lists named above are still worth walking.

### TOK-02 — Zebra striping is visible · [not verified]

- **Steps** — open an organisation's billing rows and the store billing panel.
- **Expect** — alternating rows tinted. Flat white means a missed rule.

### TOK-03 — The segmented control reads correctly · [verified]

Its selected tab is `--muted` and its trough is `--input`, so on Light it becomes exactly the
template's control: a white tab lifted out of a grey trough.

- **Expect** — the selected tab is white, clearly raised, on a grey track.
- **Seen** — the orders status filter renders exactly that. But read A11Y-02 before ticking this: the tab is
  correct in *shape* and fails on *ink contrast*.

### TOK-04 — Inset wells and chips · [not verified]

- **Steps** — settings → a provider card header and a stored secret; a product's image well; the users list
  "you" tag; a payment's transaction id chip.
- **Expect** — each reads as inset or as a chip, not as bare panel.

### TOK-05 — The same rules are still right on Forest · high · [not verified]

- **Steps** — repeat TOK-01 and TOK-02 on Forest.
- **Expect** — hovers and stripes are slightly *more* distinct than before, which is the documented intent
  (`--input` is 6% where `--muted` was 2%), and nothing looks washed out.

---

---

## SEL — Text selection

Selection was `--primary` mixed toward `--primary-foreground`, inked with `--primary-muted`. That pairing only
ever suited Forest. It is now `--accent-strong` under `--foreground-strong`.

### SEL-01 — Selected text is legible on all four themes · critical · [not verified]

- **Steps** — on each theme in turn, drag-select a paragraph and a table row.
- **Expect** — the selection is an accent wash with the strongest ink on top, readable throughout. **Before
  this branch, Daylight was 1.03:1 and Midnight 1.23:1** — text disappeared into the highlight. Re-check
  Daylight especially; it is the one that was worst.
- **Seen** — verified in Chrome against the built stylesheet, all four themes, but **outside the console app**
  (the session expired before it could be repeated in place), so it stays untagged. Measured ≥14:1 on every
  theme after the fix. Ten seconds of drag-selecting in the console would close this.

---

---

## A11Y — Contrast

### A11Y-01 — Text clears AA on both planes · high · [not verified]

Light has two backgrounds — the white panel and the grey canvas — and a token that passes on one can fail on
the other. `--muted-foreground` is slate-600 rather than slate-500 for exactly this reason: the page-header
subtitle renders on the canvas, where slate-500 is 4.35:1.

- **Steps** — run an axe/Lighthouse contrast pass on the dashboard and a settings page with Light active.
- **Expect** — the text ladder clears AA on both planes: body 10.36:1 on panel / 9.45:1 on canvas, strong
  17.83 / 16.28, subtle and muted-foreground 7.58 / 6.92. **`--primary-emphasis` is a known exception — see
  A11Y-02.**
- **Seen** — audited the orders page in Light: 60 visible text nodes, the ladder tokens all clear, one real
  failure (A11Y-02). *Caveat for whoever repeats this:* a naive audit script mis-reads this app twice over —
  `oklch()` colours parsed as RGB give nonsense, and `sr-only` text is not `display:none` so it must be
  excluded by class. Normalise every colour through a 1×1 canvas, the way `theme.provider.ts` does.

### A11Y-02 — Emerald text fails AA on light surfaces · high · [verified — FAILS]

**This is an open defect, not a gap.** It predates the branch — Daylight is worse — but Light inherits it, so
it is called out rather than buried in [99](#99--known-gaps).

`--primary-emphasis` is emerald-600 on both light themes and is used as a **text colour in 73 places** (links,
the selected tab in every segmented control, emphasised labels). On white that is **3.77:1** on Light and
**3.3:1** on Daylight, against AA's 4.5:1 for normal text. On the dark themes it is fine — 9.4:1 on Forest,
6.24:1 on Midnight — which is why it survived.

- **Steps** — on Light, look at the orders status filter: the selected tab's emerald label on its white pill.
  Repeat on Daylight, which is worse.
- **Expect** — **it fails today.** Report it as seen, do not re-file it.
- **Why it is not fixed here** — the token does two jobs with opposite requirements on a light surface. As ink
  on white it needs to be *darker*; as the primary button's hover fill under near-black ink it needs to be
  *lighter*. Neither value satisfies both:

  | `--primary-emphasis` | as ink on white | as primary-button hover fill |
  |---|---|---|
  | emerald-600 (today) | **3.77:1 ✗** | 5.35:1 ✓ |
  | emerald-700 | 5.48:1 ✓ | **3.68:1 ✗** |

  The real fix is to split the fill role from the ink role across all four themes and 84 call sites. That is a
  design-system change with its own review, not a line to slip into a theme PR.

---

---

## REG — Regression watchlist

Every row was a real defect, and several were invisible from the screen.

| What broke | How it looked | How to catch it again |
|---|---|---|
| **Store management showed the previous store's settings** | Switching stores on the rail left the page holding one store's domains, landing copy and gateway secrets while the request context had moved on — the next save would have written them onto the other store. The resource had no params, so it loaded once and never again. | MER-01 |
| **A missing translation key took a whole section down** | `/store-management/social` sat under its loading veil forever with four of five rows unlabelled, an idle network and a clean console. Transloco throws on a missing key, and a throw during template evaluation aborts the change-detection pass. | MER-06, and `npm run lint` |
| **Every content editor crashed on open** | `NG01203` (a toggle bound with `formControlName` and no value accessor), `NG0951` (a dialog behind `*transloco`), and a checklist reading `control.valid` while an async check was pending. | CNT-02 |
| **Save and Publish were dead buttons** | Disabled on a form invalidity whose field sits below the fold; no message, no scroll, nothing happened on click. | CNT-03 |
| **A published item still showed DRAFT** | Two loads raced after publish and the older response landed last. | CNT-04 |
| **Editors opened in the wrong language** | `init` ran before the store's languages resolved, so an Arabic-first store edited in English and the publish checklist judged the wrong locale. | CNT-05 |
| **Paging asked for the wrong parameter** | The list sent `size` where the platform reads `count`, so page two repeated page one. | U-02 |
| **Setting a password 403'd** | The permission evaluator had no `case` for the token, and it denies by default. | U's PERM cases, now in [tenancy-qa.md](../../tenancy/tenancy-service/qa/tenancy-qa.md) |
| **The store id column went blank** | A template reached into the id after it became a bare string. | SW-04 |
| **An allocated domain claimed to be unchecked** | Every custom domain row showed a "not checked" badge and "no lookup yet" — the console doubting a rule it had itself enforced before allowing the domain. | MER-04 |
| **Arabic copy rendered left-to-right** | The home section is written in languages the console is not running in; without `dir="auto"` the text read as nonsense. | MER-03 |
| **Buttons that had always 404'd** | seller-core's `removeStoreLogo`/`removeStoreBanner` posted to paths missing the `/spg/merchant/api` prefix and mapped by no controller. | MER-09 — the Remove button must not exist |
| **Literal "null" tooltips** | `[title]` bound to null renders the string `null`; `[attr.title]` removes the attribute. | CNT-06 |
| **Best plan looked worst** | Pro showed fewer feature lines than Basic, because unlimited items were omitted. | BIL-03 |
| **Store list 500s** | A multi-store lookup failed and took the console's main screen with it. | Open store management with several stores, and again with billing stopped. Must render either way. |
| Fourteen rules used `--muted` (panel) where the job was `--input` (hover/well) | Hovers, zebra stripes, chips and wells invisible on Light; on the dark themes a hover fainter than the field it sat in | TOK-01 … TOK-04 |
| `::selection` built from `--primary`/`--primary-muted` | Selected text ~1.03:1 on Daylight, 1.23:1 on Midnight — effectively invisible. Unnoticed because Forest is the default and the only theme it suited | SEL-01 |
| `--muted-foreground` at slate-500 | Page-header subtitle at 4.35:1 on the grey canvas — passes on a panel, fails on the page | A11Y-01 |
| A theme absent from `index.html`'s `themes` array | Stored correctly, silently rejected on next load — "my theme keeps resetting to Forest" | THM-02 |
| `--primary-emphasis` as text on a light surface — **still open** | Emerald labels and selected tabs at 3.77:1 (Light) / 3.3:1 (Daylight); invisible to every test, and to any audit that mis-parses `oklch()` | A11Y-02 |

---

## VAR — the variant model in the console

Added by the variant rework (PR #306): a store-wide **Options tab** in Catalogue, a **Variants step** in the
product form, variant-aware product rows and order lines. The model is
[catalog](../../../store-pod/catalog/catalog-service/qa/catalog-qa.md#var--the-uniform-variant-model).

### CON-01 — The Options tab · high · [verified]

- **Expect** — a fifth Catalogue tab listing the store vocabulary with each option's values summarised; the
  editor writes the whole document (values carrying their id keep their row, and therefore the store-wide value
  id every variant references); per-language names park across a locale switch.
- **Result** — confirmed. Deleting an in-use option surfaces the 409 as the named toast ("This option is still
  used by a product or one of its variants…") and the option survives.

### CON-02 — The Variants step · high · [verified]

- **Expect** — a fifth wizard step, locked until the product is saved; axes picked from the vocabulary generate
  the cartesian matrix; each row carries sku, price, quantity, available and a default toggle, with exactly one
  default; the step saves itself (atomic catalog PUT, then the inventory bulk upsert and retired-sku cleanup).
- **Result** — matrix rows carry the right per-row price and stock merged from inventory, exactly one default,
  and every control is labelled with the combination it belongs to ("Price for Red / M").
- **The save round-trip, both directions, driven from the UI on the seeded simple product 3:**
  - *Adding an axis* — picking Colour generated the two combinations, **seeding row 1 from the product's own
    sku, price and stock** (SKU-AD-CL-TPT03 / 320 / 35) and suggesting `…-BLUE` for the second. The readiness
    item swapped to "Every variant has a price", went unmet, dropped the product to 86% and blocked publish
    until the second row was priced — then returned to 100% the moment it was.
  - *Saving* — "Variants saved." Catalog got both variants with the right signatures, exactly one default and
    the assignment row; inventory got both skus priced (320/35 and 345/12). The storefront PDP picked it up
    with no further action: `variantCount: 2`, Colour chips, Red and Blue.
  - *Removing the axis* — "Remove variants — sell as one SKU again" restored a single `DEFAULT` variant
    **keeping the original sku**, cleared the assignment, and **deleted only the retired sku's inventory row
    while the surviving default kept its price and stock**. That is the specific hazard the post-write diff
    exists for (diffing against the request instead would have deleted the restored default's row), confirmed
    live rather than reasoned about.
- **Fixed during QA** — the SKU column was 11rem, which clipped every row to `SKU-ZR-CL-DRS02`; the suffix is
  the only thing distinguishing rows, so the column was showing nothing useful. Now 15rem.

### CON-03 — Publish gating · [verified]

- **Expect** — with options assigned, the readiness checklist swaps its "price" item for **"Every variant has a
  price"**, and publish stays blocked until every combination sku is priced.
- **Result** — confirmed; the pricing step defers to a pointer at the matrix so one number has one home.

### CON-04 — The products list · high · [verified]

- **Expect** — one row per product: an "N variants" badge, the **default** variant's price, and the product's
  **total** stock across its variants. Inline edit is disabled on a variant row (it writes one sku and the row
  stands for several) and routes to the form instead.
- **Result** — Nike reads 37 (25 + 12), the Zara dress 46 (38 + 8 + 0), single-variant rows unchanged.
- **Fixed during QA** — the row previously showed the **default variant's** quantity as the product's, because
  the listing payload carries only the default sku. Inventory gained a product-addressed bulk read
  (`GET /private/inventory/by-products`) and the row totals from it.

### CON-05 — Arabic / RTL · [verified]

- **Expect** — the Variants step mirrors correctly, with SKU and figures staying left-to-right.
- **Result** — confirmed, including the matrix column order and the axis chips.

### CON-06 — Responsive · [verified]

- **Expect** — the matrix is wide, so it must scroll **inside its own container** and never make the page
  scroll sideways; the axis chips, the add-combination row and the footer all wrap.
- **Result** — at a 360px panel the page does not scroll sideways, the scroller fits, the table overflows into
  its own scroll and no input is clipped. The Options tab uses the shared `.split`, which stacks below 1100px.

**Console-ui — four ways to lose data in one click**

- *Save draft wiped an unsaved matrix.* `variantAxes`/`variantRows` were `linkedSignal`s off the snapshot, so
  every assignment to `loaded` reset them — and the same save passed `writeInventory = !hasOptions()`, so the
  Pricing-step price went too. The matrix is now the operator's, seeded once per product and re-seeded only
  when a variants save reloads the truth.
- *"Retry prices and stock" wrote the server's stale numbers back* and passed no retired skus, orphaning
  inventory rows that the products list then counts forever. The failure branch no longer reloads, and the
  retry replays the payload the failed leg reported (`VariantSaveOutcome.pendingInventory`).
- *Adding an axis nulled every price and id* — rows were kept only on an exact signature match, which only
  survives a reorder — and the save then deleted the old inventory and wrote nothing back, because unpriced
  rows are skipped. Rows now carry forward onto the wider or narrower combination.
- *A failed variant read looked like "no variants"*, so the step invited a whole-set replace over combinations
  it had never seen. The snapshot carries `variantsUnavailable` / `vocabularyUnavailable`, the step says so
  with a retry, and `saveVariants` refuses.

---

## 99 — Known gaps

**No console screens for the store lifecycle.** Store suspend / archive / delete, org profile, members and
invitations all have endpoints and none have screens. **Invitations most of all**, since the token is displayed
exactly once and the console is supposed to be what shows the link.

**No in-console storefront preview.** The preview token exists and the storefront honours it, but the console
does not know a store's public host (a router allocation in merchant plus a Caddy lookup), so no preview link
is offered.

**Nothing answers what address a store is served at.** The storefront hostname is assembled client-side from
two calls on two different tiers, and the pod lookup is refused outright for a suspended or archived store — so
a store can be in a state where the console cannot say where it lives.

**DNS verification runs in the operator's browser, not on the platform.** The console queries Google's public
DNS-over-HTTPS resolver and uses that to gate the custom-domain field — one resolver, one machine, one network.

**No reference lists for countries, currencies or storefront languages.** The console derives countries and
currencies from ISO registries and `Intl`, and keeps the five storefront languages as a constant, because
`GET /store/languages` answers with the languages a store has already turned on — which cannot drive the
control that turns them on.

**Org management still offers a plan picker.** It lists real plans but no longer applies anywhere, because a
plan belongs to a store now. It needs a product decision rather than a fix.

**Reordering is up/down buttons, not drag and drop.** The console has no drag-and-drop primitive; the server
takes the whole order either way.

**There is no Sections tab.** `SectionApi` exists and `.http` blocks drive it, but the hub has no editor.

**Primary buttons do not match the console template.** It paints white on emerald-500 — **2.54:1**, below every
threshold. DESIGN.md holds that `--primary`/`--primary-foreground` does not move between themes, so Light keeps
near-black ink at 7.95:1. A deliberate departure from the reference design, on contrast grounds.

**Uppercase section labels are darker than the template's.** It uses slate-400, which is 2.56:1 on white. Those
jobs go to slate-500.

**`--input` is ~ΔRGB 6 from the template's `#f1f5f9`,** because it is an alpha rather than an opaque colour —
it has to work on a panel *and* on the canvas, and an opaque slate-100 would vanish against the canvas.
Imperceptible in place.

**`--foreground-quiet` is 4.35:1 on the canvas** and 4.76:1 on a panel. The console's uses are all inside a
panel (the rail, the toolbar, popovers) — but one sits outside it: the **sign-in page footer** ("Trusted by
1,400 merchants…") sits on the canvas and measures 4.35:1. Small, decorative, and one line from being fixed by
moving the token to slate-600 — which would flatten the ladder's last step, so it is recorded rather than done
silently. The same caution applies to any new screen that puts quiet text on the canvas.

**Light and Daylight sit next to each other in the picker** and both are light. They are genuinely different
themes, but the names invite confusion; if it becomes a support question, retiring Daylight is the cheaper fix.

**The console reads its product table through a public endpoint.** `/api/v2/products` carries no
`@PreAuthorize`; the private lists that used to exist returned rows with no name, and were removed. Documented
in console-ui `lessons.md`.

**A product's default image cannot be changed after upload** from the console (`lessons.md`).

---

Raise anything unexpected against the console PR. Attach the browser console, the failing request from the
network panel, and `.lcl/<stack>/logs/console-ui.log` — and say which theme and which language were active, as
a bug that only appears in one of the four is the interesting kind.

---
