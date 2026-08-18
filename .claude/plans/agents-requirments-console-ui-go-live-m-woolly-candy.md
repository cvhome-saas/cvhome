# console-ui go-live — migration framework, and Module 2

## Context

`store-core/console-ui` is meant to replace `store-core/seller-ui` as the production seller
console. The new app already exists and renders: Angular 20 standalone + SSR, Tailwind v4 with a
three-theme token layer, Transloco (en/ar, RTL), its own `core/` tier, ~30 shared UI components,
and eight feature areas (`marketing`, `auth`, `first-run`, `dashboard`, `orders`,
`create-store`, `store-management`, `not-found`). It is served on port 8011 and already routed by
the gateway — `GatewayRouteLocatorImpl` sends `Host: console-ui.gateway.com` to `lb://console-ui`,
`configure-domain.sh` adds the hosts entry, and `run-lcl.sh:50` starts it. Nothing is missing
from the infrastructure.

**What is missing is the data.** Every data-bearing call in console-ui is a fixture. Each feature
owns a `*.api.service.ts` that returns `of(FIXTURE).pipe(delay(...))` over `src/app/mocks/*.fixture.ts`
(~1300 lines of fake data), `core/store-context/selected-store.service.ts` holds three hardcoded
"Acme" stores with an invented pod id, and `core/store-context/first-run-mock.ts` fakes the
zero-store account behind `?firstRun=1`. The app is unauthenticated — `canAccessSecuredPages`
exists in `core/auth/` but `app.routes.ts` references it nowhere.

That mock seam was deliberate (`.agents/plans/for-those-three-pages-fuzzy-parrot.md` records the
decision: "Typed models + API interfaces + facades, mock-backed. No live HTTP yet"), and it is
exactly the seam this migration consumes. Going live means replacing those `*.api.service.ts`
bodies with real HTTP, module by module.

The endpoint knowledge for that already exists and does not need rediscovering.
`.agents/plans/seller-core-shared-lib.md` extracted every HTTP service, DTO, mapper and validator
out of seller-ui into a library at `seller-ui/projects/seller-core`, split across eleven
ng-packagr entry points (`seller-core/catalog`, `/orders`, `/stores`, `/subscriptions`,
`/signup`, `/content`, `/customers`, `/payments`, `/orgs`, `/analytics`). Every path, DTO shape
and quirk of the backend is written down there, with doc comments naming the Java source. That
library is the **reference to port from**, not a dependency to link against.

The outcome: console-ui reaches functional parity with seller-ui, driven by real APIs, migrated
one module at a time — each module independently planned, implemented, tested against both UIs
side by side, and committed in its own phases. Design blocks with no backend behind them get a
`TODO` in code and an entry in `lessons.md`, never invented data. When the last module lands,
`seller-ui` and `seller-core` are deleted together and console-ui owns its own API tier outright.

## Decisions (settled with the user)

| Question | Decision |
|---|---|
| How console-ui gets its API layer | **Port `seller-core`'s services and models into console-ui, per module.** No tsconfig path mapping, no `dist/seller-core` build step, no cross-project dependency. seller-ui is left completely untouched. |
| i18n | console-ui's own **Transloco** throughout. seller-core's two `@ngx-translate/core` call sites are rewritten during the port, not bridged by a token. No `TRANSLATION_PORT`, no adapter, and `@ngx-translate/core` never enters console-ui's `package.json`. |
| console-ui's `core/` tier | **Authoritative.** Its `errors/`, `http/crud.service.ts`, `table/`, `auth/`, `platform/` are the ones that run. Ported services inject *these*, never a second copy. |
| Strictness | Ported code is hardened to console-ui's `strict: true` as part of the port. This is a feature, not a tax — see below. |
| Module order | Marketing/auth first (done), then console shell and store context. |

### Why the copy is cheaper than the link, here

Three facts made linking expensive and the copy nearly free:

1. **console-ui already has the whole infrastructure tier.** `core/errors/*` (RFC-7807 parser,
   `ApiError`, `apiErrorInterceptor`, `ApiErrorService`, `form-error.utils`),
   `core/http/crud.service.ts`, `core/http/request-context.ts`, `core/table/table.types.ts`,
   `core/platform/browser-storage.ts` are already present, already wired in `app.config.ts`, and
   already Transloco-native. Only the **domain** services and models are missing, and those are
   thin — a service is typically 5–15 one-line `crudService.get/post(...)` methods over a
   hand-written DTO file.
2. **The i18n stacks genuinely differ.** seller-core is built on `@ngx-translate/core` v17
   (`ApiErrorService`, `ConfigService`); console-ui is on `@jsverse/transloco` v8 with a different
   locale set (en/ar vs en/fr/ar/es/ru). Linking meant shipping a port token and, for anything
   depending on the primary entry point, keeping ngx-translate resolvable in console-ui. Copying
   deletes that problem instead of managing it.
3. **The strictness gap.** `seller-ui/tsconfig.json` sets `strictNullChecks: false` and
   `noImplicitAny: false`, and `projects/seller-core/tsconfig.lib.json` inherits them.
   Linking against the built `.d.ts` would have imported signatures that *overstate*
   non-nullability — `SelectedStoreService.getStore(id)` is typed `ManagerStore` but genuinely
   returns `undefined`, the exact latent bug `.agents/plans/seller-core-shared-lib.md` already
   flagged and deliberately deferred. Porting under `strict: true` forces each of those to be
   confronted at the moment the code is read, in a diff small enough to review.

The cost the copy accepts, stated plainly: until seller-ui is retired, the two apps hold two
copies of each domain service, and a backend contract change must be applied twice. That window
is the migration itself, and each module closes a slice of it.

---

## The migration framework

Everything in this section is the framework, and it governs every module. **Module 1 is done and
shipped** (see below); **Module 2 is the plan in this document.** Later modules are named, not
designed — each gets its own planning phase when requested, per the requirements doc's constraint
against one large plan.

### Per-module lifecycle

Each module runs three phases, **one commit each**:

1. **Planning** — a plan file in `.agents/plans/console-ui-<module>.md`. Commit: `plan(console-ui): <module>`
2. **Implementation** — real APIs, TODOs for gaps, `lessons.md` entries. Commit: `feat(console-ui): <module>`
3. **Testing** — Chrome, old vs new side by side, fixes. Commit: `fix(console-ui): <module> after QA`

`lessons.md` updates ride in the implementation commit unless a module's gaps are large enough to
stand alone.

### What a module plan must cover

Fixed template, so each plan is comparable to the last:

1. **seller-ui functionality** — routes, components, workflows (start from `.agents/plans/seller-ui-feature-inventory.md`, which already maps the whole app section by section).
2. **API surface to port** — which `seller-core` entry point, which services, which endpoints, which DTOs.
3. **console design** — which `console-template/*.dc.html` page(s), which blocks.
4. **Mapping table** — old capability → new location, one row each. Anything with no row is a deliberate removal and must say so.
5. **New components** — what `shared/ui/` is missing.
6. **Backend gaps** — design blocks with no API. TODO + `lessons.md` entry.
7. **Testing** — the specific comparisons to run in the two tabs.
8. **Scope + commits.**

### Porting API code from seller-core — the standing convention

This is the rule every module follows, so it is written once here.

**Where ported code lands.** A new top-level tier `src/app/api/`, mirroring seller-core's entry
points one directory per bounded context:

```
src/app/api/
  billing/     subscription.service.ts        <- from seller-core/subscriptions
  signup/      sign-up.service.ts             <- from seller-core/signup
  stores/      manager-store.service.ts, pod.service.ts, dns-check.service.ts
  catalog/     product|category|brand|type|product-group services
  orders/  content/  customers/  payments/  orgs/  analytics/
```

Add a `@api/*` path alias in `tsconfig.json` next to the existing ones. Wire DTOs go in the
existing `src/app/models/` (already documented as "wire DTOs, one file per bounded context") —
`models/billing.ts`, `models/signup.ts`, and so on. Dependency direction becomes
`features → layouts → shared → api → core → models`; extend the eslint rule that enforces it.

**How a feature reaches it.** Unchanged from console-ui's existing architecture: the facade calls
the feature's `*.api.service.ts`, which now delegates to `@api/*` and does the view-shaping
(mapping wire DTOs to `@models/*` view models). The `*.api.service.ts` files are the seam this
whole migration turns over; keeping them means facades, components and specs do not move.

**Port checklist**, applied per file:

- Rewrite `import {CrudService} from 'seller-core'` → `import {CrudService} from '@core/http/crud.service'`;
  same for `PageT`/`SpringPage`/`PageRequest` → `@core/table/table.types`, `ApiError` →
  `@core/errors/api-error`, `BrowserStorage` → `@core/platform/browser-storage`.
- Rewrite any `TranslateService` usage to `TranslocoService` (`.instant(k, p)` →
  `.translate(k, p)`). This affects `ConfigService` when the store-settings module ports it; the
  error stack needs nothing, since console-ui's `ApiErrorService` is already Transloco-native.
- Harden to `strict: true`. Do not paper over with `!` or `as`. Where seller-core says
  `getStore(id): ManagerStore` and can return `undefined`, the ported signature says
  `ManagerStore | undefined` and the caller narrows. Every such correction is a real bug found —
  note it in the module's plan under a **Deviations** heading.
- Keep the doc comments. They name the Java DTO each interface mirrors and record why several
  decisions were made; they are the most valuable thing in the library and cost nothing to carry.
- Add a provenance line at the top of each ported file:
  `/** Ported from seller-ui/projects/seller-core/<path>. */`
  so the two copies can be diffed while both exist, and so the line can be deleted wholesale when
  seller-ui is retired.
- Bring the spec across too when one exists, adapted to console-ui's `@testing/` harness.
- Port **only what the module needs.** Do not pre-port the whole entry point.

**seller-ui is not modified by any module** except the final retirement one.

### Reading the template

`console-template/*.dc.html` are **Claude Design Canvas files, not portable HTML** — a React
runtime (`support.js`) with `<sc-if>` / `<sc-for>` / `{{ }}` and a trailing
`class Component extends DCLogic`. Read them for structure, blocks and copy; never lift markup.
They also have **no toast, modal, drawer, confirm, loading, skeleton, or error state anywhere**,
and **no responsive layout** (fixed 1440–1760px, `Sign In` is `min-width:1100px`). Those are
console-ui's to design — the shared components for them mostly exist already
(`busy-overlay`, `toast`, `notice-bar`, `page-header`).

Where the template and seller-ui disagree, seller-ui defines *what the feature does* and the
template defines *how it looks*. A capability seller-ui has and the template omits is still in
scope unless the module plan explicitly records the removal.

### lessons.md

New file `store-core/console-ui/lessons.md`. Append-only, newest module last. One entry per gap:

```markdown
## <Module> — <capability>

- **Screen:** console-ui route + the `console-template` page it comes from
- **What the UI needs:** the interaction, in one or two sentences
- **What is missing:** the endpoint or service that does not exist
- **Why it is required:** what the seller cannot do without it
- **Expected contract:** method, path, request/response shape as far as it can be determined
- **Placeholder:** the `TODO(lessons.md):` marker left in code
```

Precedent for the depth worth reaching:
`console-template/Content Management Service - Backend Requirements.md`, a full spec for a
service that does not exist yet. Anything that large graduates out of `lessons.md` into its own
requirements doc, with `lessons.md` linking to it.

### TODO convention

Every unbacked block gets, at the call site:

```ts
// TODO(lessons.md): <capability> — no backend endpoint. See lessons.md "<Module> — <capability>".
```

and in the UI either omits the block or renders it disabled with an honest label. **Never a
fixture standing in for a real answer.** `orders.ts` already sets the precedent for unimplemented
actions — `ToastService.info('… is not available yet.')`.

### Migration order (named only — not planned here)

`1` marketing + auth → `2` console shell & store context → `3` dashboard → `4` orders →
`5` store management → `6` catalogue → `7` payments → `8` content → `9` users & profile →
`10` customers → `11` subscription & usage → `12` org & pod management (platform admin) →
`13` retire seller-ui.

There is no Module 0. With the copy approach nothing needs wiring up front — console-ui's
`CrudService`, interceptor, error stack and config token are already provided in `app.config.ts`
and merely unused. The `@api/*` alias, the `src/app/api/` directory and `lessons.md` are created
by Module 1 as its first files.

Rationale for the front of the queue: `2` unblocks every console route (nothing can be scoped to
a store until the real store list loads, replacing the hardcoded `STORES` array and
`FirstRunMock`), and `3`–`5` are the modules whose console-ui shells are already built against
fixtures, so they are pure api-service swaps.

---
## Module 1 — Marketing / landing + auth — **done**

Shipped in three commits: `plan(console-ui)…`, `feat(console-ui): marketing and auth on real APIs`,
`fix(console-ui): marketing and auth after QA against the live stack`.

What it established, which Module 2 builds on:

- `src/app/api/` exists as the ported HTTP tier, with the `@api/*` alias and the eslint rule that
  keeps `core/`, `shared/` and `models/` out of it.
- The port-by-copy convention above is proven: `models/billing.ts`, `models/signup.ts`,
  `api/billing/subscription.service.ts`, `api/signup/sign-up.service.ts`.
- `lessons.md` exists with twelve entries.
- QA against the live stack found four defects, three of them in claims the UI was making that the
  backend did not support. **That is the pattern to expect**: the expensive findings in this
  migration are not wiring errors, they are places where the design asserts something no service
  can answer.

---

## Module 2 — Console shell and store context

### Why this module is next, and why it is the hard one

Every remaining module is a reading of one store. `SelectedStoreRequestContext.params()` stamps
`?store=&pod=` onto every request and throws rather than guess, so until the real store list loads
there is nothing to scope a dashboard, an order list or a catalogue to. Module 2 is what turns
that key.

It is also the module where the console stops being anonymous. `canAccessSecuredPages` has existed
in `core/auth/` since the app was scaffolded and **`app.routes.ts` references it nowhere** — every
console route is currently reachable signed-out, rendering fixtures. That is the single largest
correctness gap in the app today.

### seller-ui today

The header (`theme/components/header/`) carries an `nb-select` of the stores the user can reach;
nearly every screen is scoped to the current selection and changing it reloads the page's data. It
is disabled on routes where a store context is meaningless
(`STORE_SELECT_DISABLED_ROUTE_PREFIXES`: store-management, subscription-and-usage, org-management).
`canAccessSecuredPages` guards `/pages/**` from `pages-routing.module.ts`. Store creation lives at
`/pages/store-management/create-store`. Provisioning is asynchronous and surfaced only as a status
column in the stores list.

### console-ui today

`ConsoleApi` (`layouts/console-shell/services/console.api.service.ts`) serves the whole chrome from
fixtures and holds mutable in-memory state for stores, pin and order.
`core/store-context/selected-store.service.ts` hardcodes three "Acme" stores and an invented
`DEFAULT_POD_ID`. `core/store-context/first-run-mock.ts` fakes the zero-store account behind
`?firstRun=1` — its own comment says it comes out once a stores endpoint exists.
`features/create-store` runs a seven-row provisioning checklist on a timer.

Design reference: `console-template/Create Store.dc.html`, `Create First Store.dc.html`,
`First Run.dc.html`, `First Run with Nav.dc.html`, and the shell in `Admin Dashboard.dc.html`.

### API surface to port

| From | To | Endpoint |
|---|---|---|
| `seller-core/src/lib/store/store.service.ts` (`ManagerStoreService.list`) | `api/tenancy/manager-store.service.ts` | `POST /tenancy/api/v1/store-manager/list`, body `{}` + `Pageable` |
| `seller-core/stores/.../store.service.ts` (`createStore`, `checkIfStoreExist`) | same file | `POST /tenancy/api/v1/store-manager/private/store`, `GET …/private/store/unique?name=` |
| — (new) | same file | `GET /tenancy/api/v1/store-manager/store-info?store=` — provisioning state |
| `seller-core/stores/.../pod.service.ts` (`listPods`) | `api/pod-registry/pod.service.ts` | `GET /pod-registry/api/v1/pod/list` |
| console-ui `core/auth/user.service.ts` (already present, unused) | reused as-is | `GET /tenancy/api/v1/user-account/current` |
| `seller-core/src/lib/models/commons.ts` | `models/tenancy.ts` | `ManagerStoreDto`, `PodRef`, `ProvisioningState`, `StoreStatus` |

**Do not port `ManagerStoreService.create()`.** It posts to `/tenancy/api/v1/store-manager/create`,
which **does not exist** — `StoreManagerApi` maps create at `private/store`. It is dead code in
seller-core; the working path is `seller-core/stores`' `StoreService.createStore`. Record it under
Deviations.

**Correct the DTO while porting.** seller-core's `ManagerStore` is missing two fields the server
sends (`status: StoreStatus`, `billingStatus: SubscriptionStatus | null`) and types
`provisioningState` as a bare `string` where it is a four-value enum. `billingStatus` is
**nullable by design** and must render as "unknown", never as a problem — a billing outage is not a
reason to tell a merchant their store has lapsed.

### Decisions (settled with the user)

| Question | Decision |
|---|---|
| Pin default store / reorder rail | **Removed.** No user-preferences endpoint exists anywhere. Both controls come out with a `TODO(lessons.md)` rather than being faked or persisted per-browser. |
| Hosting region picker | **Becomes a real pod picker**, sourced from `GET /pod-registry/api/v1/pod/list` and sent as `pod: {id}`. |
| Plan card in create-store | **TODO.** Store creation calls billing for a quota check, not a plan choice. |
| Provisioning checklist | **Replaced by polling** `store-info` for the real `ProvisioningState`, including `FAILED_PROVISIONING`, which the timer can never reach and the console cannot currently show at all. |

### The one architectural problem: sync context, async list

`SelectedStoreRequestContext.params()` is called **synchronously** inside `CrudService.getParams()`
on every request, but the real store list arrives over HTTP. The list must therefore be resolved
before any store-scoped request is issued.

**Load it in the guard.** `requiresStore` already runs before every console route activates and
already asks for the store count; it becomes the point where the directory is fetched and cached in
`SelectedStoreService`. `ConsoleApi.loadStores()` then reads the cache rather than fetching. This
keeps `params()` synchronous, costs no new mechanism, and — importantly — keeps the fetch off the
prerendered marketing and auth routes, which must not pay for it.

Rejected: `provideAppInitializer`, which would fetch on every entry to `/` and `/sign-in` for
visitors who are not signed in at all.

### Mapping

| seller-ui / design capability | console-ui destination | Backing |
|---|---|---|
| Auth gate on the console | `canAccessSecuredPages` on every `ConsoleShell` route | `GET /api/v1/auth/me` — **real, already written** |
| Store selector | `store-switcher` | `POST /store-manager/list` — **real** |
| Signed-in identity (name, initials, email) | `ConsoleShellFacade.user` | `GET /user-account/current` — **real** |
| First-run (zero stores) | `firstRun` computed, guards | real empty list; `FirstRunMock` **deleted** |
| Create store | `features/create-store` | `POST …/private/store` — **real** |
| Live store-name check | create-store form | `GET …/private/store/unique?name=` — **real** |
| Hosting region | pod picker | `GET /pod-registry/api/v1/pod/list` — real, but see gap |
| Provisioning progress | polled state | `GET …/store-info?store=` — **real** |
| Organization name in the shell | `ConsoleShellFacade.organization` | **no endpoint** → TODO |
| Notification bell + feed | `console-toolbar` | **no endpoint** → TODO |
| Sidebar badge counts (12 / 5 / 7) | `CONSOLE_NAVIGATION` | **no endpoint** → removed, TODO |
| Default store / rail order | removed | **no endpoint** → TODO |
| Plan selection at create | removed from form | **no endpoint** → TODO |
| Global search | `console-toolbar` | already dead in both UIs → TODO |

### Implementation

- **Port** the table above into `src/app/api/tenancy/` and `src/app/api/pod-registry/`, with
  `models/tenancy.ts` and `models/pod.ts`, following the standing port checklist. Correct the DTO
  as noted; harden to `strict: true`.
- **`SelectedStoreService`** loses `STORES` and `DEFAULT_POD_ID` and gains a `load()` returning a
  cached `Observable` of the real directory, plus the existing synchronous accessors reading that
  cache. `addStore` stays — create-store still needs to register a new store without a refetch.
  Keep the `cvhome.console.store` storage key: which store is *open* is genuinely a per-browser
  concern, unlike a default.
- **Delete `core/store-context/first-run-mock.ts`** and every reference. An empty list is now the
  real signal, exactly as its own comment anticipated.
- **`ConsoleApi`** keeps its shape but `loadStores()` reads `SelectedStoreService`, `addStore()`
  calls the real create, and `pinDefaultStore`/`reorderStores` are **removed** along with
  `ConsoleShellFacade.pinStore`, `toggleReorder`, `moveStore`, `reordering`, `defaultStoreId`,
  `defaultStore`, and the pin/reorder UI in `store-switcher`. `StoreDirectory.defaultStoreId` comes
  off the model.
- **Auth**: add `canAccessSecuredPages` to the `ConsoleShell` routes in `app.routes.ts`
  (`getting-started`, `dashboard`, `orders`, `store-management`), ordered before `requiresStore` so
  an unauthenticated visitor is sent to uaa rather than to getting-started. `ConsoleUser` is built
  from `/user-account/current`; drop `CONSOLE_USER`.
- **Navigation** stays a client-side constant — it is a map of the app, not server data — but the
  invented badge counts come off. Items with no `route` keep rendering and leading nowhere, which
  is the existing convention.
- **create-store**: real form (name + the merchant fields tenancy forwards), live uniqueness check,
  optional pod, then poll `store-info`. Delete the timer, `TASK_STAMPS_SECONDS`, `TICKS_PER_TASK`
  and `PROVISIONING_TASKS`. Handle `FAILED_PROVISIONING` and a polling timeout honestly — the store
  row exists either way, so the page must not imply it can be re-created.
- **`create-store.fixture.ts`** loses `HOSTING_REGIONS`, `STORE_PLANS`, `PROVISIONING_TASKS` and
  their defaults; `COUNTRIES` stays (reference data, and seller-ui reads the same list from a static
  JSON asset).

### Backend gaps → `lessons.md`

1. **No user-preferences endpoint** — default store and rail order. Expected: a small per-user
   preferences document on tenancy, or `defaultStore` on `ReadableUser`.
2. **An org admin cannot read its own organization** — `OrgManagerApi` is super-admin only on every
   method, so the shell has an org **id** from the principal and no name. Expected: a
   `GET /tenancy/api/v1/org/current` returning at least `{id, name}`.
3. **No merchant-readable list of placeable pods.** `PodService.listPlaceablePublicPods()` exists
   and is *not exposed on any endpoint*; `pod/list` returns only the caller org's own private pods,
   which is empty for a normal merchant. So the pod picker will usually have nothing in it and the
   page must fall back to "assigned automatically". `Pod` also carries no region, latency or data
   residency, all of which the design shows.
4. **No notifications service** — bell, unread count, feed, "mark all read". Nothing exists.
5. **No sidebar badge counts** — the design shows unread/attention counts per section; each would
   need a cheap count endpoint on its own service.
6. **No plan selection at store creation** — billing is asked for a quota decision, not a plan. A
   subscription belongs to a store, so this is arguably correct and the console should offer the
   plan step *after* provisioning; recorded so the design's plan card is not mistaken for missing work.
7. **Provisioning has four states and no detail** — no per-step progress, no failure reason, no
   retry. `FAILED_PROVISIONING` leaves a store row the merchant cannot act on.
8. **No global search** (carried forward from the template review).

### Testing

Both UIs, two tabs, `seller-ui.gateway.com:8000` and `console-ui.gateway.com:8000`, signed in as
the same org admin.

- **Auth**: open `/dashboard` signed out → redirected to uaa, not to getting-started. Sign in →
  land back on `/dashboard`. This is new behaviour; confirm it does not trap the marketing routes.
- **Store list**: the switcher shows the same stores as seller-ui's header select, same names, same
  count. Switching stores changes `?store=&pod=` on the next request — check the network tab, and
  that the pod id matches the store's real pod, not the invented `507f1f77…`.
- **First run**: an org with no stores lands on `/getting-started` and cannot reach `/dashboard`.
  Confirm without `?firstRun=1`, which no longer exists.
- **Create**: create a store from console-ui, watch it provision, confirm it appears in seller-ui's
  header select after a reload — the strongest single proof the two agree.
- Duplicate name → the live uniqueness check blocks before submit; confirm seller-ui behaves the same.
- Force `FAILED_PROVISIONING` if a pod can be drained, or stub it, and confirm the page says so.
- Arabic and all three themes on the shell; 1440 / 900 / 420.

### Commits

1. `plan(console-ui): console shell and store context` — this document.
2. `feat(console-ui): console shell and store context on real APIs`.
3. `fix(console-ui): console shell after QA`.

---

## Critical files

**seller-ui: not modified.** Read-only reference.

**New in console-ui:** `src/app/api/tenancy/manager-store.service.ts`,
`src/app/api/pod-registry/pod.service.ts`, `src/app/models/tenancy.ts`, `src/app/models/pod.ts`.

**Changed in console-ui:** `src/app/core/store-context/selected-store.service.ts`,
`src/app/app.routes.ts`, `src/app/layouts/console-shell/services/console.api.service.ts`,
`.../facades/console-shell.facade.ts`, `.../guards/first-run.guard.ts`,
`.../components/store-switcher/store-switcher.ts`, `.../components/console-toolbar/*`,
`src/app/features/create-store/**`, `src/app/models/console.ts`,
`src/app/mocks/console.fixture.ts`, `src/app/mocks/create-store.fixture.ts`, `lessons.md`.

**Deleted:** `src/app/core/store-context/first-run-mock.ts`.

**Reused, already present:** `src/app/core/auth/auth-guard.service.ts`,
`src/app/core/auth/auth.service.ts`, `src/app/core/auth/user.service.ts`,
`src/app/core/http/crud.service.ts`, `src/app/core/http/request-context.ts`.

## Verification

1. `cd store-core/console-ui && npm run build && npm run lint && npm test`;
   `git -C store-core/seller-ui status --porcelain` empty.
2. `grep -rn "FirstRunMock\|firstRun=1" src` → no hits.
3. `grep -rn "507f1f77bcf86cd799439011\|Acme Supply Co" src` → no hits outside specs.
4. `grep -rn "from 'seller-core" src` → no hits.
5. The two-tab comparison above, driven through Chrome.
6. Every `TODO(lessons.md):` marker in the diff has a matching heading in `lessons.md`.
