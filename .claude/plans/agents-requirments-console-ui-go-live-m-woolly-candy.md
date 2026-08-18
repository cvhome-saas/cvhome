# console-ui go-live — migration framework + first module

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
| First module | Marketing / landing + auth, per the requirements doc. |

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

Everything below this line is the framework. Only **Module 1** is planned here. Later modules are
named, not designed — each gets its own planning phase when requested, per the requirements doc's
constraint against one large plan.

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

## Module 1 — Marketing / landing + auth

### seller-ui today

Routes in `src/app/public/public.routes.ts`: `/` (`IndexComponent`, five sections —
`welcome`, `features`, `pricing`, `subscribe`, `contact`), `/signup`, `/terms`,
`/privacy-policy`, `/external-logout-link`, `/subscription/success|fail`. SSR for `/`, `/signup`,
`/terms`, `/privacy-policy`; the rest client-only.

**Exactly one section calls an API.** `public/sections/pricing/facades/pricing.facade.ts` →
`SubscriptionService.plans()` from `seller-core/subscriptions` →
`GET billing/api/v1/plan/public/plans[?currency]`, the only billing call with no store and no
session. Guarded by `isPlatformBrowser` so SSR skips it. Mapping in
`public/sections/pricing/mappers/pricing.mapper.ts`: `PlanView`/`PlanPriceView` → `Pricing`; the
free plan is whichever price has `amount.minorUnits === 0`, deliberately detected by price and not
by code, so a renamed plan keeps working; features come from `plan.entitlements` entries whose
`flagValue !== false`.

Signup: `public/components/sign-up-form/` (component + `SignUpFormFacade` + `SignUpFormService`)
→ `SignUpService.signUp()` from `seller-core/signup` →
`POST /tenancy/api/v1/signup/public/create`, body
`{user: {firstName, lastName, emailAddress, password}, subscriptionPlan}` → `{status}`. Errors go
through `ApiErrorService.applyToForm`, which lands `CUA.REGISTRATION.EMAIL_TAKEN` on the field
that caused it. On success: toast, then redirect after `SIGN_UP_REDIRECT_DELAY_MS = 2000`.

Sign-in is **not a page** — `environment.LOGIN_URL = '/oauth2/authorization/uaa'`, an OAuth2
redirect handled by the gateway. Contact and newsletter-subscribe have **no backend in seller-ui
either**: `ContactFacade.submit()` calls `form.reset({})` and `SubscribeComponent.sub()` clears a
string.

### console-ui today

`features/marketing/marketing.html` (211 lines), route `''`, prerendered. Sections: sticky header
with nav drawer, hero, metrics strip, `#story` pillars, `#stores`, `#reviews`, pull-quote,
`#pricing` with a monthly/annual toggle, `#contact`, footer. All content from
`src/app/mocks/marketing.fixture.ts`; all copy already Transloco keys.
`features/auth/{sign-in,sign-up}` sit under `AuthShell`, and
`features/auth/services/auth.api.service.ts#createAccount()` returns `of(void 0)` — a no-op.
`features/marketing/services/marketing.api.service.ts#sendContactMessage()` likewise.

Design reference: `console-template/cvhome Marketing.dc.html` and `console-template/Sign In.dc.html`.

### What gets ported

| From seller-core | To console-ui | Notes |
|---|---|---|
| `subscriptions/src/lib/models/billing.model.ts` | `src/app/models/billing.ts` | Whole file. `Identifier`, `Money`, `PlanView`, `PlanPriceView`, `EntitlementValue`, `SubscriptionView`, … Keep the doc comment explaining why identifiers stay wrapped as `{id}`. |
| `subscriptions/src/lib/services/subscription.service.ts` | `src/app/api/billing/subscription.service.ts` | Port `plans()` only for this module. Leave `current/invoices/checkout/changePlan/cancel/resume` for Module 11 rather than porting dead code. |
| `subscriptions/src/lib/constants/subscription.constants.ts` | `src/app/api/billing/subscription.constants.ts` | Only if the pricing mapper needs it. |
| `signup/src/lib/domain/types.ts` | `src/app/models/signup.ts` | `SignUpForm`, `PersistableUser`, `SignUpResponse`. |
| `signup/src/lib/service/sign-up.service.ts` | `src/app/api/signup/sign-up.service.ts` | Whole file — one method, plus the `SIGNUP_API_BASE` constant. |

Both ported services inject console-ui's `@core/http/crud.service`. Neither touches
`@ngx-translate/core`, so nothing needs rewriting for i18n in this module.

### Mapping

| seller-ui capability | console-ui destination | API |
|---|---|---|
| Pricing plan cards, monthly/yearly toggle | `#pricing` section, existing toggle | ported `SubscriptionService.plans()` — **real** |
| Free plan shown apart | same rule: `minorUnits === 0`, not plan code | same |
| Plan feature bullets | plan card body | `plan.entitlements`, `flagValue !== false` |
| Sign up | `features/auth/sign-up` | ported `SignUpService.signUp()` — **real** |
| Field-level signup errors | `ApiErrorService.applyToForm` (console-ui's own) | RFC7807 `fieldErrors[]` |
| Sign in | `features/auth/sign-in` | OAuth2 redirect to `loginUrl` |
| Terms, Privacy policy | **new routes** under `MarketingShell` | static |
| Subscription success / fail | **new routes**, client-render | static landings |
| `external-logout-link` | **new route** | `AuthService.logout()` currently navigates here and it does not exist — a live bug |
| Contact form | `#contact` | no backend → TODO |
| Newsletter subscribe | not in console design | dropped; recorded here as deliberate |
| Metrics, pillars, stores, reviews | keep as constants | marketing copy, not data → TODO for a CMS |

### Implementation

- **Scaffolding first.** Create `src/app/api/`, add the `@api/*` alias to `tsconfig.json`, extend
  the eslint dependency-direction rule, create `lessons.md`.
- **Port** the five files in the table above, following the port checklist. Expect
  `strictNullChecks` corrections in the billing model's nullable fields (`description`,
  `planCode`, `amount`, `currentPeriodEnd`, `trialEnd`, `graceUntil`, `pendingPlanChange` are all
  already `| null`, so this file should port cleanly — confirm rather than assume).
- **Pricing.** Rewrite `features/marketing/services/marketing.api.service.ts` to delegate to the
  ported `SubscriptionService`. Port `public/sections/pricing/mappers/pricing.mapper.ts` into
  `features/marketing/mappers/pricing.mapper.ts`, adapting its `Pricing` output to console-ui's
  existing `MarketingPlan` (`@models/marketing`) — the mapper is view-shaping, so it belongs in
  the feature, not in `api/`. Keep the `isPlatformBrowser` guard: the route is **prerendered**,
  and `SelectedStoreRequestContext.params()` throws on the server. The page must render its full
  layout without plans and fill them in on the client. Remove `MARKETING_PLANS` from
  `marketing.fixture.ts`.
- **Sign up.** `features/auth/services/auth.api.service.ts#createAccount` delegates to the ported
  `SignUpService`. `sign-up-form.service.ts` gains the `repeatPassword` cross-field validator and
  the password policy seller-ui enforces. Errors → console-ui's `ApiErrorService.applyToForm`;
  success → `ToastService` then redirect. `SignUpForm.subscriptionPlan` is in the DTO but absent
  from seller-ui's form group — decide explicitly during implementation whether console-ui sends
  the plan chosen on the pricing section (a genuine improvement) or matches seller-ui's omission;
  record the choice under **Deviations**.
- **Sign in.** No credential form. The template's email/password fields and Google/Microsoft/Apple
  buttons have no backing in uaa — replace with the honest OAuth handoff already decided in
  `.agents/plans/for-those-three-pages-fuzzy-parrot.md`, and log the social providers in
  `lessons.md`.
- **New routes** in `app.routes.ts` under `MarketingShell`: `terms`, `privacy-policy`,
  `subscription/success`, `subscription/fail`, `external-logout-link`. Add the first two to
  `app.routes.server.ts` as Prerender, the rest as `RenderMode.Client`.
- **Contact.** Keep the form, keep validation, mark submit with the `TODO(lessons.md)` comment and
  surface it honestly rather than pretending it sent.
- Delete every fixture the module stops using; leave the rest.

### Backend gaps → `lessons.md`

1. **Contact form** — no lead/enquiry endpoint. Template promises topic routing and "answers in
   under four hours". Expected: `POST /tenancy/api/v1/contact/public` with
   `{name, organization, email, topic, message}` → 202.
2. **Social sign-in** — Google / Microsoft / Apple in `Sign In.dc.html`. Needs uaa providers plus
   account linking.
3. **Marketing content is hardcoded** — metrics, pillars, merchant showcase, reviews. Needs a
   public content endpoint. Related to `Content Management Service - Backend Requirements.md`;
   link rather than duplicate.
4. **Password reset / forgot password / email verification** — `Sign In.dc.html` links "Forgot
   password?"; no screen exists in either UI and no endpoint is known.
5. **14-day trial** — the template's pill and "Start 14-day trial" CTA. `PlanPriceView.trialDays`
   exists in the billing model; confirm whether public signup can select a trial.

### Testing

`extra/scripts/run-lcl.sh` starts both. Two tabs: `seller-ui.gateway.com:8000` and
`console-ui.gateway.com:8000`.

- Pricing: same plans, same order (`tier`), same monthly/yearly prices, same free-plan placement,
  same feature bullets. Check the network tab shows exactly one `billing/api/v1/plan/public/plans`
  and **no `?store=` or `?pod=`** on it.
- View-source the prerendered `/`: full layout present, plans absent, no SSR error in the server
  log.
- Sign up with a fresh email → both redirect and the account exists. Sign up with a taken email →
  both show the error on the email field, not as a bare toast.
- Password mismatch, weak password, empty required fields → same validation in both.
- `/terms`, `/privacy-policy`, `/subscription/success`, `/subscription/fail` render.
- Sign in → uaa → returns authenticated.
- Arabic: switch locale, confirm the marketing page mirrors and pricing still loads.
- All three themes (forest / midnight / daylight); 1440px, 900px, 420px.

### Commits

1. `plan(console-ui): marketing and auth module` — the module plan file.
2. `feat(console-ui): marketing and auth on real APIs` — the port, the feature rewiring, the new
   routes, `lessons.md`.
3. `fix(console-ui): marketing and auth after QA` — whatever the two-tab comparison finds.

---

## Critical files

**seller-ui: not modified.** Read-only reference for the whole migration until Module 13.

**New in console-ui:**
`src/app/api/billing/subscription.service.ts`, `src/app/api/signup/sign-up.service.ts`,
`src/app/models/billing.ts`, `src/app/models/signup.ts`,
`src/app/features/marketing/mappers/pricing.mapper.ts`, `lessons.md`, plus page components for
terms / privacy-policy / subscription-success / subscription-fail / external-logout-link.

**Changed in console-ui:**
`tsconfig.json` (`@api/*` alias), `eslint.config.js` (dependency direction),
`src/app/app.routes.ts`, `src/app/app.routes.server.ts`,
`src/app/features/marketing/services/marketing.api.service.ts`,
`src/app/features/marketing/facades/marketing.facade.ts`,
`src/app/features/auth/services/auth.api.service.ts`,
`src/app/features/auth/services/sign-up-form.service.ts`,
`src/app/features/auth/facades/auth.facade.ts`, `src/app/mocks/marketing.fixture.ts`.

**Ported from (read-only):**
`seller-ui/projects/seller-core/subscriptions/src/lib/{models/billing.model.ts,services/subscription.service.ts}`,
`seller-ui/projects/seller-core/signup/src/lib/{domain/types.ts,service/sign-up.service.ts}`,
`seller-ui/src/app/public/sections/pricing/{facades/pricing.facade.ts,mappers/pricing.mapper.ts}`,
`seller-ui/src/app/public/components/sign-up-form/**`.

**Reused, already present:** `src/app/core/http/crud.service.ts`,
`src/app/core/http/request-context.ts`, `src/app/core/errors/api-error.service.ts`,
`src/app/core/errors/form-error.utils.ts`, `src/app/core/table/table.types.ts`,
`src/app/shared/ui/toast/`.

**Read, not changed:** `.agents/plans/seller-ui-feature-inventory.md` (the parity contract),
`.agents/plans/seller-core-shared-lib.md` (why the library is shaped as it is),
`store-core/console-ui/DESIGN.md`, `console-template/cvhome Marketing.dc.html`,
`console-template/Sign In.dc.html`.

## Verification

1. `cd store-core/console-ui && npm run build && npm run lint && npm test`. No seller-ui build is
   involved — `git status` in `store-core/seller-ui` must be clean.
2. `grep -rn "@ngx-translate" store-core/console-ui/src store-core/console-ui/package.json` →
   no hits.
3. `grep -rn "from 'seller-core" store-core/console-ui/src` → no hits; the port is by copy, and a
   stray import would mean an accidental cross-project dependency.
4. `extra/scripts/run-lcl.sh`, then the two-tab comparison above via Chrome, driving both
   `seller-ui.gateway.com:8000` and `console-ui.gateway.com:8000`.
5. Network tab: every console-ui request is same-origin and relative; the public plans call
   carries no tenant scoping.
6. `lessons.md` exists and every `TODO(lessons.md):` marker in the diff has a matching entry
   (`grep -rn 'TODO(lessons.md)' src | wc -l` against the heading count).
