# seller-ui — business feature inventory

Purpose: a functional map of everything the seller console does today, written for a **UI/UX revamp**.
Backend services, domains, permissions and guards stay as-is; this document is the contract the new UI
must still satisfy. Derived from the component tree, routes, menu and templates — not from service code.

Stack today: Angular 20 standalone + Nebular (`nb-card`, `nb-select`, `nb-datepicker`, `nb-dialog`),
`ngx-datatable` for every list, `ngx-echarts` for charts, `ngx-file-drop` for uploads, `@ngx-translate`
for i18n (en/ar/es/fr/ru, AR is RTL).

---

## 1. Who uses it — five roles

`Roles` (`pages/shared/models/roles.ts`) drives the entire sidebar. Every menu section is gated by a
predicate in `pages-menu.ts`.

| Role | Scope | What it is for |
|---|---|---|
| `isSuperAdmin` | platform | Runs the SaaS: orgs, pods, platform statistics |
| `isSupport` | platform | Read/assist on orgs |
| `isOrgAdmin` | one organization | The paying tenant owner: stores, billing, users, everything commercial |
| `isStoreAdmin` | one store | Day-to-day store operator |
| `isStoreModerator` | one store | Restricted operator — catalogue, orders, payments only |

Section access matrix (as coded):

| Section | superAdmin | support | orgAdmin | storeAdmin | storeModerator |
|---|:--:|:--:|:--:|:--:|:--:|
| Home / statistics | ✓ | ✓ | ✓ | ✓ | ✓ |
| Org management | ✓ | ✓ | — | — | — |
| Pod management | ✓ | — | — | — | — |
| Store management | — | — | ✓ | — | — |
| Subscription & usage | — | — | ✓ | — | — |
| User management | — | — | ✓ | ✓ | — |
| Customers | — | — | ✓ | ✓ | — |
| Content management | — | — | ✓ | ✓ | — |
| Inventory (catalogue) | — | — | ✓ | ✓ | ✓ |
| Orders | — | — | ✓ | ✓ | ✓ |
| Payments | — | — | ✓ | ✓ | ✓ |

**Revamp note:** two products are wearing one skin — a *platform admin console* (org/pod/platform stats)
and a *merchant console* (catalogue/orders/payments/store). Today they share one sidebar with hidden
branches. Worth separating visually, if not structurally.

---

## 2. The two global controls that colour everything

Both live in the top header (`theme/components/header/`) and are the most important pieces of state in
the app.

**Store selector** — a `nb-select` of the stores the signed-in user can reach. Nearly every screen is
scoped to "the currently selected store"; changing it reloads the current page's data. It is
**disabled** on routes where a store context is meaningless (`STORE_SELECT_DISABLED_ROUTE_PREFIXES`):
store-management, subscription-and-usage, org-management.

**Language selector** — switches the console's own UI language *and* acts as the working language for
multilingual content. Separately, most content forms have their **own** in-form language selector for
the per-language description being edited (product, category, brand, type, content page, store landing
page). Two different language concepts on one screen is a known confusion to fix in the revamp.

Also in the header, currently non-functional/decorative: global search (`nb-search`), inbox icon, bell
(notifications) icon. User menu = Profile, Log out.

---

## 3. Public / pre-login site (`/`)

A marketing site bundled into the same app, shown to signed-out visitors.

- **Landing page** — welcome hero, features grid, pricing plans, newsletter subscribe, contact form.
  (Several sections exist but are commented out: counter, service, discover, how-it-works, screenshots,
  reviews, FAQ, team, download.)
- **Pricing** — plan cards driven by the same pricing data as in-app subscription; monthly/yearly toggle.
- **Sign up** — firstName, lastName, emailAddress, password, repeatPassword. Creates an organization.
- **Terms**, **Privacy policy** — static pages.
- **Subscription success / fail** — return landings from the external payment/checkout flow.
- **External logout link** — logout bounce route for the OAuth2 provider (uaa).

Login itself is delegated (uaa), not a page in this app; `canAccessSecuredPages` guards `/pages/**`.

---

## 4. Home — dashboards (`/pages`)

One route, two dashboards chosen by role. Both have a **from-date / to-date range picker** in the header
that drives every chart.

**Admin dashboard** (platform roles) — three ECharts panels:
- Subscriptions
- New org joiners
- New stores created

**Client dashboard** (merchant roles):
- Order status breakdown (full width)
- Customer countries — where customers come from
- Top selling products

**Gap to exploit in the revamp:** no KPI/stat tiles, no revenue figure, no "needs your attention"
(pending payments, unfulfilled orders, low stock). Everything is a chart. This is the thinnest screen in
the app relative to how important it is.

---

## 5. Organization management (`/pages/org-management`) — platform only

The tenant registry.

- **Org list** — table: ID, name, contact email, created date, actions. Row actions → edit, stores, change password.
- **Create org** — firstName, lastName, emailAddress, password (+ policy: 6–12 chars, upper, lower, digit), subscriptionPlan.
- **Update org details** — same fields, editable.
- **Change org password** — new password + repeat, with match validation.
- **Org stores list** — the stores belonging to that org (ID, store name, actions).

## 6. Pod management (`/pages/pod-management`) — superAdmin only

Pods are the physical per-region deployments stores get placed into.

- **Pod list** — ID, name, endpoint, actions (edit).
- **Create pod / Edit pod** — name, endpoint URL, type (select), org id.

---

## 7. Store management (`/pages/store-management`) — orgAdmin

The largest configuration surface in the app. A store is created, then configured through **eight
sub-screens** reached by a `nb-select` "sub-navigation" dropdown in the card header (`sideMenuLinks`) —
not tabs, not a sidebar. This is the single worst navigation pattern in the current UI and the most
obvious revamp target.

- **Stores list** — ID, name, domain, pod id, provisioning state, actions. Provisioning state means store
  creation is **asynchronous** — the UI must express "being provisioned" as a first-class state.
- **Create store** — full store form.

The eight configuration screens, all keyed by store `:code`:

| # | Screen | What it configures |
|---|---|---|
| 0 | **Store branding** | Logo + banner upload (drag & drop) |
| 1 | **Store home page** | Per-language landing page title, landing text, tag/meta description |
| 2 | **Store domain** | Custom domain: pattern-validated, live DNS/CNAME verification with a "checking / waiting for DNS" state and a CNAME hint |
| 3 | **Store social links** | Social profile URLs |
| 4 | **Store slider images** | Homepage carousel — add/remove/order slider images |
| 5 | **Store details** | The core store form (below) |
| 6 | **Store social login** | Per-provider: enabled, appId, appSecret, callback URL |
| 7 | **Store payment configuration** | enabled, apiKey, secretKey, webhookSecret (tenant secrets — encrypted server-side, must never be echoed in plaintext in the UI) |

**Store details form fields:** name, email, phone, address, city, stateProvince, postalCode, country,
currency, currencyFormatNational, defaultLanguage, supportedLanguages (multi), theme, colorTheme,
weight (unit), dimension (unit), inBusinessSince, requireLoginForOrderPlacement.

Validation is field-by-field with required messages; name uniqueness is checked live.

---

## 8. Subscription & usage (`/pages/subscription-and-usage`) — orgAdmin

- **Subscription** — plan cards with monthly/yearly toggle, strike-through previous price vs current
  price, per-plan feature bullet list, Subscribe action (redirects to external checkout → returns to the
  public success/fail routes). Contextual alert banners for three states: active FREE plan (with end
  date), inactive FREE plan, inactive paid plan.
- **Usage** — **stubbed.** The template literally renders the word "USAGE". A real usage/metering screen
  (stores, products, orders, storage against plan limits) is an open design opportunity.

---

## 9. User management (`/pages/user-management`) — orgAdmin, storeAdmin

Staff accounts (not shoppers).

- **Users list** — userName, emailAddress, active, actions.
- **Create user** (scoped to a `:store`) — userName, firstName, lastName, emailAddress, password, active, roles (multi).
- **User details** — same form, editable.
- **Change password** — new + repeat, same password policy.
- **My profile** — the signed-in user's own details; also reachable from the header user menu.

User detail screens use the same dropdown sub-navigation pattern (Details / Change password).

---

## 10. Inventory / catalogue (`/pages/catalogue`) — the operational core

The biggest merchant-facing area. Five entities.

### 10.1 Products
- **Products list** — filters: SKU, availability (all / available / not available), with Search and Reset
  Filter buttons; server-side paging. Columns: Id, SKU, quantity, available, price, creation date, actions.
  **Inline editing:** double-click quantity or price to edit in place; availability is a checkbox toggled
  directly in the row. Row actions: edit, delete.
- **Create / edit product** — two-column form:
  - *Definition:* visible, SKU (unique, live-checked, alphanumeric pattern), dateAvailable (datepicker),
    sortOrder, manufacturer/brand (select), product type (select).
  - *Per-language descriptions (SEO panel):* language selector + name, title, friendlyUrl, highlight,
    description (rich text), meta description, keywords.
  - Also carried by the model: price, quantity, shipeable, virtual, canBePurchased, and
    productSpecifications (height, weight, length, width, model, dimension/weight units).
- **Product detail sub-tabs** (real router tabs here, unlike store management):
  - **Images** — multi-image upload, remove, set/update, default image, ordering.
  - **Category** — multi-select assignment of the product to categories.
  - **Related** — product autocomplete search to attach related products, listed in a paged table with remove.

### 10.2 Categories
- **Categories list** — Id, code, name, visible, actions.
- **Create / edit category** — code (unique, live-checked), visible, parent category, sortOrder, plus
  per-language descriptions (name, title, friendlyUrl, highlight, description, meta description, keywords).
- **Category hierarchy** — a **drag-and-drop tree** (`ngcx-tree`) for re-parenting/reordering categories.
  One of the few genuinely interactive screens; keep it, upgrade it.

### 10.3 Brands (manufacturers)
- **Brands list** — Id, code, name, actions.
- **Create / edit brand** — code, order, per-language: name, title, friendlyUrl, highlights, meta
  description, keywords.

### 10.4 Product groups
- **Groups list** — Id, code, active, actions.
- **Create / edit group** — code, active, and a **product picker**: autocomplete search → add products to
  the group, paged table of members with remove. Used to power merchandising surfaces on the storefront.

### 10.5 Product types
- **Types list** — Id, code, name, visible, actions.
- **Create / edit type** — code, visible, allowAddToCart, per-language name.

---

## 11. Content management (`/pages/content`) — orgAdmin, storeAdmin

Storefront CMS.

- **Pages** — list (Id, code, actions); add/edit: visible, code (unique, live-checked), linkToMenu, order,
  and per-language name, friendlyUrl, title, meta tag, page content (rich text/HTML).
- **Boxes** — same shape as pages: reusable content blocks placed on the storefront. visible, code,
  per-language name/title/content.
- **Files / media library** — drag-and-drop or browse to stage images, preview thumbnails, remove staged
  items, bulk **Upload**; grid of already-uploaded images with preview (lightbox) and delete. A shared
  **image browser dialog** lets other screens pick from this library.

---

## 12. Customers (`/pages/customer`) — orgAdmin, storeAdmin

- **Customer list** — Id, firstName, lastName, emailAddress. **Read-only, no filters, no detail view.**
  The thinnest real feature in the app. Customer data clearly exists (orders carry full customer, billing
  and delivery records) — a customer detail / order history / lifetime value view is an obvious addition.

---

## 13. Orders (`/pages/orders`) — orgAdmin, storeAdmin, storeModerator

- **Order list** — filters: customer name, email, phone, status (Ordered / Processed / Delivered /
  Refunded / Canceled), with Search + Reset. Columns: Id, customer name, customer email, customer phone,
  order date, status, total, actions (Details).
- **Order details** — the single richest screen in the app:
  - Header actions: **Update status**, **Refund**, **Capture**, **Print invoice**, **View invoice**, Cancel/back.
  - **Billing info** panel — first/last name, company, street address, city, state/province (free text or
    select depending on country), postal code, country select, phone, email. Editable and saveable.
  - **Shipping/delivery info** panel — same address fields, independently editable.
  - **Items** — product lines: item, SKU, quantity, price, subtotal, thumbnail.
  - **Totals** — subtotal lines, tax, shipping, grand total, currency.
  - **Status history** — timeline of status + comment + date; add a new status change with a comment
    (`ORDER_FORM.UPDATE_STATUS`, `STATUS_COMMENT`).
  - **Transactions list** — the payment transactions behind the order.
  - **Invoice** — printable invoice view.
  - Flags shown: customerAgreed (terms & conditions), confirmedAddress, paymentStatus, reservationStatus.
  - Refund and Capture both go through a confirmation dialog with an amount.

---

## 14. Payments (`/pages/payment`) — orgAdmin, storeAdmin, storeModerator

Payment transaction ledger, separate from orders.

- **Transaction list** — filters: internal ref, request ref, payment type, status, date from, date to;
  Search + Reset. Columns: transaction no, internal ref, request ref, payment type, amount, transaction
  date, status, actions.
- **Approve / Reject** — only for rows whose status is in `ACTIONABLE_PAYMENT_STATUSES`. Approve opens a
  dialog asking for the external **transaction number**, then confirms. This is a manual
  bank-transfer/offline-payment reconciliation workflow — a genuinely important operator task that is
  currently buried in a table row.

---

## 15. Cross-cutting UI mechanics to carry over (or deliberately replace)

These are used everywhere; whatever the new design system is, it needs an answer for each.

| Mechanic | Today | Notes for revamp |
|---|---|---|
| Data table | `ngx-datatable`, server-side paging, 50/page | Consistent, but no sorting, no column choice, no bulk actions, no export |
| Filters | Ad-hoc row of inputs + Search / Reset Filter buttons | Not URL-persisted; filters lost on navigate. Fix. |
| Inline edit | Double-click cell (products qty/price only) | Undiscoverable — only a `title="Double click to edit"` tooltip |
| Forms | `nb-card` header with Cancel + Save (spinner on Save) | Save always in the card header, top-right. No dirty-state guard on navigate away. |
| Validation | Per-field, shown after dirty/touched; live uniqueness checks for code/SKU/store name | Keep the live uniqueness checks — they're good |
| Multi-language content | In-form language `nb-select` + `formArray` of descriptions | The #1 UX problem: no indication which languages are filled in or missing |
| Sub-navigation | `nb-select` dropdown in the card header (store management, user details) | Replace with real tabs/sidebar |
| Images | `ngx-file-drop` drag & drop, image-uploading component, image-browser dialog, showcase/lightbox dialog | Reusable, keep the concepts |
| Autocomplete | product-autocomplete, store-autocomplete | Reused in related products + product groups |
| Feedback | `NotificationService` toasts, `ApiErrorService` for typed API errors, `nbSpinner` overlays per card | Typed error model already landed (see recent commits) — the new UI should surface field-level API errors, not just toasts |
| Empty states | Effectively none | Every list needs one |
| Charts | ECharts, lazily provided on the home route only | |

---

## 16. Summary of gaps worth designing for

Not bugs — product gaps visible from the UI alone:

1. **Dashboard is charts-only** — no KPIs, no action queue, no revenue at a glance.
2. **Usage screen is a stub.**
3. **Customers are a bare list** — no detail, no order history, no segmentation.
4. **No global search** despite a search box in the header.
5. **No notifications** despite inbox and bell icons in the header.
6. **Store configuration is 8 screens behind a dropdown** — should be one coherent settings experience.
7. **No bulk operations anywhere** (bulk price change, bulk visibility, bulk category assignment).
8. **Multilingual completeness is invisible** — you can't see which locales a product is missing.
9. **Async store provisioning has no progress/feedback surface** beyond a status column.
10. **Payment approve/reject** — a real approval workflow squeezed into a table row action.
11. **No dirty-form protection, no filter persistence, no deep-linkable list state.**
12. **Platform admin and merchant console are the same app with hidden menus.**
