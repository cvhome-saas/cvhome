# seller-ui — Product, User & UX Audit (Rebuild Brief)

Audit of `store-core/seller-ui` as it exists today, written as input for a UI/UX
rebuild that **keeps every existing feature**. Companion documents:
`ARCHITECTURE.md` (target code layering) and `ANGULAR_MODERNIZATION_PLAN.md`
(framework-level migration).

**Scale audited:** 112 components · 99 templates · 72 facades · ~21.6k LOC (TS + HTML)
**Stack:** Angular 20.3 + SSR (`@angular/ssr`) · Nebular 16 (`@nebular/theme`) ·
Bootstrap 5 · ngx-datatable · ngx-echarts · ngx-quill · ngx-translate ·
ngx-toastr · ngcx-tree · moment · jQuery

---

## 1. What this product is

`seller-ui` is the **single admin console for a multi-tenant e-commerce SaaS**.
It is not one app for one audience — it serves three very different audiences
inside one shell:

| Audience | Job to be done |
|---|---|
| **Platform staff** | Run the SaaS: onboard orgs, place stores on pods, watch growth |
| **Org owner** (paying customer) | Run their business: stores, subscription, staff |
| **Store operator** (their staff) | Run day-to-day commerce: catalog, orders, payments, content |

It *also* contains a **public marketing site + pricing + signup** at route `''`
(`src/app/public/`) — a Bootstrap-based landing template, visually and
architecturally separate from the Nebular console. That is a second, distinct
design problem living in the same codebase.

**Runtime path:** `gateway.com:8000` → gateway terminates the OAuth2 (uaa)
browser session → proxies to seller-ui (:8010). Data calls go out through the
gateway to `control-plane` (`/control-plane/api/v1/...`) and to pod services
(`/spg/...`).

---

## 2. User types — two orthogonal models

The most important thing to get right in the redesign: the app models users
**twice**, and the two models do not align.

### Axis A — `user_type` claim (`shared/services/auth.service.ts`)

Decides **which dashboard you land on** (`home/facades/home.facade.ts`):

| `user_type` | Dashboard |
|---|---|
| `SUPPER_USER` | Admin dashboard — platform metrics |
| `ORG_USER` | Client dashboard — commerce metrics |
| `MANAGED_USER` | Client dashboard — commerce metrics |

### Axis B — authorities (`pages-menu.ts` guards)

Decides **which menu items exist**:

| Role | Home | Users | Stores | Orgs | Pods | Catalog | Content | Customers | Orders | Payments | Subs |
|---|---|---|---|---|---|---|---|---|---|---|---|
| `ROLE_SUPER_ADMIN` | ✔ | | | ✔ | ✔ | | | | | | |
| `ROLE_SUPPORT` | ✔ | | | ✔ | | | | | | | |
| `ROLE_ORG_ADMIN` | ✔ | ✔ | ✔ | | | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| `ROLE_STORE_ADMIN` | ✔ | ✔ | | | | ✔ | ✔ | ✔ | ✔ | ✔ | |
| `ROLE_STORE_MODERATOR` | ✔ | | | | | ✔ | | | ✔ | ✔ | |

**Key insight:** super-admin and org-admin have *almost disjoint* menus. Today
they share one sidebar, one header, one dashboard component and one visual
language. In practice these are **two products** — a platform control panel and
a merchant console — sharing a shell. The new IA must decide deliberately
whether to keep them fused or split them into two shells over a shared design
system.

Note also that the two axes are enforced independently: role gates the menu,
`user_type` gates the dashboard. Nothing guarantees they agree.

---

## 3. Current user flows

### 3.1 Acquisition → onboarding (public site)

```
/                       marketing: welcome → features → pricing → subscribe → contact
  → /signup             first name, last name, email, password + chosen plan
                        POST /control-plane/api/v1/user-account/public/create
                        toast → setTimeout redirect to login
  → Stripe checkout     /control-plane/api/v1/subscription/subscribe?priceId=…
      → /subscription/success
      → /subscription/fail
  → /oauth2/authorization/uaa   → lands in /pages
```

Pricing is live from the backend (`/subscription-plan/public/tables`), with a
month/year toggle plus a FREE option. Plans: **FREE / LIMITED / BASIC /
PERFORMANCE**, each carrying limits on stores, orders, products, visitors and
accounts.

Also public: `/terms`, `/privacy-policy`, `/external-logout-link`.

### 3.2 The console shell

Fixed header + collapsible sidebar + fixed footer
(`theme/layout/one-column/one-column.layout.ts`).

Header, left → right: hamburger · "seller-ui" text logo · **store selector** ·
language selector · search · inbox · bell · user menu (Profile / Log out).

**The store selector is the app's global tenant context** — the most
load-bearing UX element in the product (`shared/services/selected-store.service.ts`):

- loads all stores once, restores the last selection from `localStorage`,
  otherwise auto-selects the first store
- the router outlet does not render until stores resolve (`storesReady()`)
- every list/detail facade re-triggers its query when the store changes
- it is **disabled** on `/pages/store-management`, `/pages/subscription-and-usage`
  and `/pages/org-management` (`header.constants.ts`)

Access control on the whole `/pages` tree: `canAccessSecuredPages` calls
`/api/v1/auth/me`; on failure it redirects to
`${LOGIN_URL}?redirectTo=<encoded url>`.

### 3.3 Daily merchant loop

Login → dashboard (order status, customer countries, top-selling products, with
a from/to date range) → **Orders** list (filter by name / phone / email /
status) → order detail (billing, shipping, items, totals, status update +
comment; dialogs for Transactions / Invoice / History; Refund or Capture button
depending on transaction type) → **Payments** list (filter by status, type,
request ref, internal ref, date range) → approve or reject a manual transaction
by entering a transaction number.

### 3.4 Catalog loop

Products list (inline **double-click** edit of quantity and price, checkbox
toggle for availability, filters by SKU and availability) → product form
(definition + per-language SEO/description) → tabs: **Images · Category ·
Related · Discount**. Plus Categories (list + drag-and-drop hierarchy tree),
Brands, Product Groups, Product Types.

### 3.5 Platform-staff loop

Orgs list → org detail (update details / change password / that org's stores)
· Pods list → create/edit pod (name, endpoint URL, type, org id) · Admin
dashboard (subscriptions, new org joiners, new stores created).

---

## 4. Feature inventory — everything the rebuild must preserve

| Area | Features |
|---|---|
| **Home** | Two dashboards (admin / client), 6 ECharts widgets, shared date-range filter |
| **User management** | List, create (scoped to a store), details, change password, own profile, role checkboxes, active toggle |
| **Store management** | List (shows podId + provisioningState), create, store info form, branding (logo/banner), custom domain with live DNS CNAME verification, social links, slider images, social login config (app id/secret per provider), payment configuration (API key, secret key, webhook secret, enabled), landing page (title, meta description, main text) |
| **Org management** | List, create, update details, change password, org's stores |
| **Pod management** | List, create, edit — super-admin only |
| **Catalogue** | Products (+ images, category assignment, related products, discount, inventory quantity, price, SKU uniqueness check), categories (+ drag-drop hierarchy), brands, product groups, product types |
| **Content** | Pages (rich text, friendly URL, meta tags, show-in-menu), boxes, files |
| **Customers** | List only — id, first name, last name, email |
| **Orders** | List + filters, detail (billing / shipping / items / totals), status workflow, invoice print, status history, transactions |
| **Payments** | Transaction list, filters, approve / reject |
| **Subscription & usage** | Plan cards, month/year toggle, current-plan alerts, Subscribe → Stripe. **Usage page is an empty stub** |
| **Cross-cutting** | 5 languages (en, fr, es, ru, **ar**), image browser, image uploader, product autocomplete, store autocomplete, generic paginated table state, showcase dialog |

### Store settings form fields (`store-form`)

Name · unique code · phone · email · address · country · state/province · city ·
postal code · currency · national currency format · supported languages ·
default language · weight units · size units · theme · color theme · operating
since · use cache · require login for order placement.

### Domain vocabulary encoded in the UI

**Order statuses:** `ORDERED` → `PROCESSED` → `DELIVERED` → `REFUNDED` / `CANCELED`

**Payment transaction statuses (10):** `PENDING` · `PROCESSING` · `PAID` ·
`FAILED` · `EXPIRED` · `CANCELLED` · `WAITING_VERIFICATION` · `REJECTED` ·
`AUTHORIZED` · `REFUNDED`
Actionable (approve/reject shown): `PENDING`, `PROCESSING`,
`WAITING_VERIFICATION`, `AUTHORIZED`.

**Payment types (4):** Cash on Delivery · Manual Transfer · Stripe · PayPal

**Subscription plans:** `FREE` · `LIMITED` · `BASIC` · `PERFORMANCE`
(each with stores / orders / products / visitors / accounts limits)

**Pod endpoint types:** INTERNAL / EXTERNAL

### Features present in translations but absent from the UI

`en.json` still carries a full vocabulary for **Shipping** (configuration,
expedition, origin, packaging, rules, methods), **Tax** (classes, rates) and
**Product options / variations** (option sets, option values, variations list —
the menu entries are commented out in `pages-menu.ts`). Treat these as a likely
roadmap, and design an IA with room for them.

---

## 5. The de-facto current design system

Everything is assembled from about six repeated compositions:

1. **Page shell** — `nb-card` with a header row (h1 title + right-aligned
   primary action) and a spinner-wrapped body
2. **List page** — filter row → Reset / Search buttons → `ngx-datatable` with
   server-side paging → round icon buttons (edit = primary, delete = danger) in
   a trailing Actions column
3. **Form page** — two 50/50 columns of `nb-card`s, Cancel (warning) + Save
   (success) in the header, per-field inline `err-message` spans
4. **Detail-with-tabs** — parent form plus a tab strip of child routes
   (e.g. `PRODUCT_FORM_TABS`)
5. **Dialog** — `NbDialogService` for invoice, history, transactions, approve
6. **Feedback** — Toastr for every success and error

Theming: four Nebular themes are registered (`default`, `cosmic`, `corporate`,
`dark`) but only `default` is provided at bootstrap — there is no theme switcher.

Architecturally the code is mid-migration to a strict **Component → Facade →
Api / Form / Mapper / State** layering (`ARCHITECTURE.md`) and is largely there:
72 facades, signals throughout, components as dumb renderers.
**That layer is worth keeping wholesale; only components and templates need to
be rebuilt.**

---

## 6. UX findings — what to fix in the rebuild

Roughly in priority order.

1. **Orphaned pages.** `store-branding/:code`, `store-domain/:code`,
   `store-social-links/:code`, `store-slider-images/:code`,
   `store-social-login/:code`, `store-payment-configuration/:code` and
   `store-landing/:code` are all routed but **nothing in the UI links to them** —
   the store detail page renders only the info form, with no sub-navigation.
   Real, shipped features are currently unreachable. A store settings page with
   a proper section nav is the single biggest available win.
2. **Two products in one shell.** Super-admin and org-admin share a sidebar but
   not a job. Consider distinct navigation models over a shared design system.
3. **Decorative chrome.** Header search, inbox and bell are wired to nothing.
   Either build them — a global search across products / orders / customers is
   genuinely valuable here — or remove them.
4. **Store context is invisible and fragile.** The selector silently auto-picks
   the first store, silently disables itself on some routes with no explanation,
   and no page reminds you which store you are editing. Every edit and delete
   action inherits that ambiguity.
5. **Dead ends.** The Usage page renders the literal text "USAGE". The Customers
   list has four columns, no search, no detail view and no actions — despite a
   full set of customer-detail translations already existing.
6. **No empty / loading / error states as design.** Only `nbSpinner` overlays
   and toasts. No skeletons, no empty-state guidance, no inline error surfaces.
7. **Inline table editing is undiscoverable.** Double-click to edit quantity and
   price, with a `title` tooltip as the only affordance.
8. **Arabic is supported but there is no RTL implementation** — only Nebular's
   internal `nb-rtl` mixins. The whole layout must be direction-aware.
9. **Navigation depth.** Catalogue sits three levels deep in the sidebar
   (Inventory management → Products → List of products) for what is a two-item
   list.
10. **Mobile.** Nebular's responsive sidebar is the only concession; data
    tables, two-column forms and dashboards are desktop-only in practice.
11. **Dead weight inherited from the Nebular starter template** — `NbChatModule`
    registered with a hardcoded Google Maps key, three unused theme
    registrations, jQuery, moment.
12. **Two visual languages in one product.** The public marketing site
    (Bootstrap template) and the console (Nebular) share nothing. The rebuild
    should decide whether they converge on one brand system.
13. **Inconsistent form technology.** Order details still uses template-driven
    `ngModel` forms while newer features use reactive forms via a
    `*.form.service.ts`. Validation and error display differ accordingly.

---

## 7. Constraints the new design must respect

- **Tenant context is mandatory.** Nearly every backend call takes a `store`
  query param. The store switcher can be redesigned but not removed.
- **Role-gated navigation must stay data-driven.** The guard functions in
  `pages-menu.ts` are the contract.
- **Language is a data dimension, not just a locale.** Products, categories,
  content and the store landing page all carry per-language name / description /
  SEO fields — forms need a language axis, not just translated labels.
- **Auth is redirect-based.** `/oauth2/authorization/uaa?redirectTo=…`; the
  console has no in-app login form. The public site *does* own signup.
- **SSR is on.** The marketing side genuinely needs it; several facades already
  guard with `isPlatformBrowser`.
- **The facade / service layer is reusable.** Swapping Nebular + ngx-datatable +
  the templates while keeping `*.facade.ts`, `*.api.service.ts` and
  `*.form.service.ts` intact makes a feature-by-feature rebuild possible rather
  than a big-bang rewrite.

---

## 8. Suggested next artifacts

- A proposed information architecture and navigation model for the new console,
  including the platform-admin / merchant split decision
- A screen inventory mapping every current route to its new-IA destination
- A design-token and component set to replace the Nebular primitives
