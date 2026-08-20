# lessons.md — backend work the console needs

Kept during the migration of `seller-ui` → `console-ui`. One entry per capability the new design (or the
console's own honesty) needs and the backend does not have yet. Append-only, newest module last.

Every entry pairs with a `TODO(lessons.md):` marker at the call site, so the code and this file cannot
drift apart:

```
grep -rn 'TODO(lessons.md)' src
```

Anything large enough to be a service in its own right graduates out of this file into a dedicated
requirements document, with the entry here reduced to a link. There is already one precedent:
[`../console-template/Content Management Service - Backend Requirements.md`](../console-template/Content%20Management%20Service%20-%20Backend%20Requirements.md).

---

## Marketing — contact form has no endpoint

- **Screen:** `/` → `#contact`, from `console-template/cvhome Marketing.dc.html` §9 ("Send us a note").
- **What the UI needs:** post a composed enquiry — name, company, work email, one of four topics, message —
  and confirm it was received. The design promises "answers in under four hours".
- **What is missing:** any lead/enquiry endpoint. seller-ui has the same form and it is not connected either:
  `public/sections/contact/facades/contact.facade.ts` calls `form.reset({})` and reports success, which
  loses the message silently. console-ui does **not** reproduce that — the submit button is disabled and the
  form says it is not connected, pointing at the email and phone channels, which are real.
- **Why it is required:** it is the only inbound channel on the marketing page that does not require the
  visitor to leave it. Topic routing is the point — "migrating stores" and "custom plan" go to different
  people.
- **Expected contract:**
  ```
  POST /tenancy/api/v1/contact/public
  { "name": "...", "organization": "...", "email": "...",
    "topic": "MIGRATING_STORES|NEW_MARKET_SETUP|CUSTOM_PLAN|SOMETHING_ELSE", "message": "..." }
  → 202 Accepted, empty body
  ```
  Public and unauthenticated, so it needs rate limiting and a spam control. Validation failures should use
  the standard RFC-7807 `fieldErrors[]` shape so the console can bind them to the form it already built.
- **Placeholder:** `TODO(lessons.md)` in `src/app/features/marketing/facades/marketing.facade.ts`
  (`contactSubmitAvailable`).

## Marketing — newsletter subscribe was dropped

- **Screen:** none. seller-ui's `/` has a "Subscribe to get updates" section
  (`public/sections/subscribe/subscribe.component.ts`); the console design has no equivalent block.
- **What is missing:** it was never connected in seller-ui either — `sub()` clears a string and returns.
- **Decision:** deliberately not migrated. Recorded here so the omission is visible rather than looking like
  an oversight during the parity review. If it comes back it needs a real mailing-list integration, not a
  form that discards its input.

## Marketing — no recommended-plan flag in the billing catalog

- **Screen:** `/` → `#pricing`, plan cards.
- **What the UI needs:** to know which plan to emphasise. `console-template/cvhome Marketing.dc.html` §8
  highlights one card with a `featured` treatment.
- **What is missing:** `PlanView` (`billing-commons/dto/PlanView.java`) carries `id`, `code`, `displayName`,
  `description`, `tier`, `prices` and `entitlements` — nothing about presentation.
- **Why it is required:** which plan a business wants to push is a commercial decision that changes without a
  deploy. Today the console falls back to a rule about the row — the middle card, and nothing at all when
  fewer than three are shown, which is what the yearly view gets once the monthly-only FREE plan drops out.
  So the emphasis moves as a side effect of the billing interval, which nobody chose.
- **Expected contract:** a nullable `boolean recommended` (or a small `PlanPresentation` block) on `PlanView`,
  seeded from `plan-catalog.yml` alongside `tier`. At most one plan should carry it; the console should treat
  more than one as none.
- **Placeholder:** documented in `src/app/features/marketing/mappers/pricing.mapper.ts` (`middleCode`).

## Marketing — FREE has no yearly price, so the yearly view has no free plan

- **Screen:** `/` → `#pricing` with the toggle on Yearly.
- **What happens:** `plan-catalog.yml` gives FREE a MONTH price only, so it is not purchasable yearly and the
  console does not show it — leaving two cards where the design has three. This is correct behaviour, not a
  bug: "Free — not available yearly" would be a worse answer. Recorded so the two-card yearly view is not
  reported as a rendering fault.
- **What would change it:** a `USD 0 / YEAR` price on FREE in the catalog. Whether that is wanted is a
  commercial decision, not a frontend one.

## Marketing — the landing page's own content is hardcoded

- **Screen:** `/` — the metrics strip, the `#story` pillars, the `#stores` merchant showcase, the `#reviews`
  testimonials and their summary stats.
- **What the UI needs:** to publish and edit this copy without a deploy, in every locale the console supports.
- **What is missing:** any public content endpoint. It currently lives in
  `src/app/mocks/marketing.fixture.ts` as translation keys, which is honest about being authored copy but
  means marketing cannot change a testimonial without a release.
- **Why it is required:** merchant logos, review quotes and headline numbers change far more often than the
  app does, and the numbers ("99.95% uptime", "4 continents") are claims that should be traceable to a source.
- **Expected contract:** covered by the planned content service — see
  [`Content Management Service - Backend Requirements.md`](../console-template/Content%20Management%20Service%20-%20Backend%20Requirements.md),
  which already models org-scoped, per-locale `ContentItem`s. The marketing site needs an unauthenticated read
  of a published, org-less subset. Not urgent; noted so it is not rediscovered.

## Marketing — legal documents are unauthored

- **Screen:** `/terms` and `/privacy-policy`.
- **What the UI needs:** the actual text of each document, per locale, with a version and an effective date.
- **What is missing:** both. seller-ui has the same two routes and their templates are three lines that render
  a decorative header and no text. console-ui renders the page with a plain notice that the document is not
  published yet and a link to the contact channels, rather than inventing binding legal copy.
- **Why it is required:** the sign-up form's legal note refers to them, and checkout will need to link to them.
  A terms page with no terms is worse than a missing link.
- **Expected contract:** the content service's "legal & policies" domain, with `type`, `jurisdiction`,
  `effectiveAt` and per-locale bodies — `console-template/New Policy.dc.html` already designs the editor for it.
- **Placeholder:** `TODO(lessons.md)` in `src/app/features/legal/legal-page.ts`.

## Auth — no social sign-in providers

- **Screen:** `/sign-in`, from `console-template/Sign In.dc.html` (Google / Microsoft / Apple buttons).
- **What the UI needs:** three additional OAuth2 entry points beside the existing uaa handoff.
- **What is missing:** uaa registers one authorization server for staff. There are no social providers and no
  account-linking model — what happens when someone signs up by email and later signs in with the Google
  account carrying the same address is undefined.
- **Why it is required:** it is on the sign-in mockup, and it removes the password from the most common path.
- **Decision for now:** the buttons are **not** rendered. A button that cannot work is worse than no button,
  and the honest OAuth handoff is already the whole sign-in page.
- **Expected contract:** `spring-security-oauth2-client` registrations in uaa exposed as
  `/oauth2/authorization/{google|microsoft|apple}`, plus a linking rule keyed on verified email.

## Auth — no password reset, and no email verification

- **Screen:** `/sign-in` — `Sign In.dc.html` links "Forgot password?".
- **What the UI needs:** request a reset link, land on a reset form from that link, set a new password. Also a
  verification step after signup: `AuthUser` already carries an `email_verified` claim that nothing ever sets.
- **What is missing:** all of it. Neither UI has a screen, and no endpoint is known. seller-ui's
  "Forgot password?" does not exist at all; the link is only in the new design.
- **Why it is required:** signup creates a password-backed account. Without a reset path the only recovery is
  an administrator, and for the first user of an organization there is no administrator above them.
- **Expected contract:** `POST /uaa/api/v1/password/public/forgot {email} → 202` (always 202, so the endpoint
  cannot be used to test whether an address is registered), and
  `POST /uaa/api/v1/password/public/reset {token, password, repeatPassword} → 204`.

## Auth — signup collects no organization name

- **Screen:** `/sign-up`.
- **What the UI needs:** nothing more than it has — this entry records a **removal**, so the parity review does
  not read it as a bug.
- **What happened:** console-ui's form previously asked for a company name. `CreateOrgRequest`
  (`tenancy/manager/dto/CreateOrgRequest.java`) is a record with one component, `PersistableUser user`, and
  `SignupServiceImpl` derives the organization from the email alone. The field had nowhere to go, so it was
  collecting data it then discarded. It is also absent from `Sign In.dc.html`, which asks only for name,
  email and password.
- **What would be needed to bring it back:** a `name` on the organization created by signup — a second
  component on `CreateOrgRequest` and a column behind `InternalOrgService.createOrgForUser`. Worth doing: an
  org currently has no display name anywhere in the console.

## Auth — public signup validates nothing

**Found by probing the running stack, not by reading the code.** This is the most serious entry in this file.

- **Screen:** `/sign-up`.
- **What is missing:** all server-side validation on `POST /tenancy/api/v1/signup/public/create`. Verified
  against the local stack:
  ```
  POST .../signup/public/create
  {"user":{"firstName":"","lastName":"","emailAddress":"not-an-email","password":"a","repeatPassword":"b"}}
  → 200 OK, account and organization created
  ```
  Empty names, a string that is not an address, a one-character password, and a `repeatPassword` that does not
  match the password — all accepted. `CreateOrgRequest` has no `@Valid` and `PersistableUser` carries no
  constraints, so nothing downstream of the controller checks anything. uaa has no password policy either.
- **Why it is required:** this is the one endpoint on the platform that anyone on the internet may call, and it
  creates a tenant. Today it can be used to create unlimited organizations with junk data, and a client with a
  bug — or no client at all — can create an account whose password is one character. `repeatPassword` is
  particularly misleading: it exists on the DTO and is transmitted, which reads as if the server compares them.
  It does not.
- **What the console does meanwhile:** `SignUpFormService` is now the only validation there is, and says so.
  An earlier revision of it deliberately dropped the password minimum "because uaa owns the policy" — that
  assumption was wrong and the minimum is back.
- **Expected contract:** `@Valid` on the request, `@NotBlank` on `firstName`/`lastName`, `@Email` on
  `emailAddress`, a password policy (length and character classes) applied in uaa, and an `@AssertTrue` for
  the password match. Failures should come back as RFC-7807 with `fieldErrors[]` paths like
  `user.emailAddress`, which the console's form is already shaped to receive.
- **Note:** the probe left a junk organization and user (`not-an-email`) in the local development database.

## Auth — a taken email is indistinguishable from any other conflict

- **Screen:** `/sign-up`.
- **What the UI needs:** to tell the visitor that the address is already registered, on the email field.
- **What is missing:** a specific error. Signing up with an existing address answers:
  ```
  409 {"code":"COMMON.DATA_INTEGRITY_VIOLATION","category":"CONFLICT",
       "params":{"service":"uaa","path":"/api/v1/admin/users","remoteStatus":409}}
  ```
  No `fieldErrors[]`, and a code that says only "something violated a constraint somewhere". The message the
  console's error chain resolves for it is "This changed somewhere else. Refresh and try again." — correct for
  the generic code, wrong for this form. seller-ui has the same gap and does not handle it: its facade names
  `CUA.REGISTRATION.EMAIL_TAKEN` in a comment, but nothing sends that code.
- **Why it is required:** a duplicate address is the single most likely way a signup fails, and it is the one
  the visitor can act on — by signing in instead.
- **What the console does meanwhile:** `AuthFacade.bindTakenEmail` treats a 409 with no field errors on *this
  call* as a taken address and puts a specific message on the email control. Deliberately narrow, and it should
  be deleted the moment the server can say what it means.
- **Expected contract:** a distinct code (`TENANCY.SIGNUP.EMAIL_TAKEN`) with
  `fieldErrors: [{field: "user.emailAddress", code: "..."}]`. Note the conflict currently leaks the internal
  uaa path (`/api/v1/admin/users`) in `params`, which a public endpoint should not do.

## Auth — the trial a visitor is promised is not the trial the catalog publishes

- **Screen:** `/sign-up` ("14 days free", "Free for 14 days. No card."), `/` → `#pricing` call to action.
- **What is true:** there are **two** trials, and only one of them is real today.
  - The **org trial** — `com.asrevo.cvhome.billing.trial-period: P14D` (`BillingProperties`, defaulted to 14
    days in code as well). Granted once per organization, to the first store it creates
    (`StoreQuotaServiceImpl`, `OrgTrialGrantRepository`). This is what the sign-up page's "14 days free"
    refers to, and it is accurate.
  - The **per-price trial** — `PlanPriceView.trialDays`, "free days this price grants on its own, on top of
    the org-level trial". Every price in the seeded catalog has `trialDays: 0`.
- **What the console does:** the pricing cards name a trial only when the price grants one, so against the
  current catalog they read "Choose this plan" and never "Start 14-day trial". That is deliberate: the org
  trial is once per organization, not per plan, so advertising it on a plan card would promise something a
  returning visitor will not get.
- **What is missing:** any way for the console to know whether *this* visitor's org still has its trial. The
  grant is server state with no public read, so the sign-up page's "14 days free" is an unconditional claim —
  correct for a genuinely new visitor, wrong for someone whose org has already used it.
- **Expected contract:** either a `trialAvailable` boolean on the authenticated bootstrap (so the console can
  stop promising a trial that is spent), or drop the per-price `trialDays` concept if the org trial is the
  only one that will ever be granted — two trial mechanisms where one is always zero is a trap.

---

## Shell — no user-preferences endpoint

- **Screen:** the store switcher at the foot of the sidebar (`Admin Dashboard.dc.html`), which designs a
  "set as default" pin and a reorder mode.
- **What the UI needs:** to remember, per account, which store the console opens on and what order the
  rail lists them in.
- **What is missing:** anywhere to put either. `UserAccountApi` has create/update/list/enable/disable and
  no preferences; `ReadableUser` carries no `defaultStore`; nothing in tenancy holds a per-user document.
- **Why it is required:** an operator with several stores lands on an arbitrary one every session. The pin
  is the fix, and it is worthless if it does not follow them to another machine — which is exactly why it
  was not persisted in browser storage as a stand-in.
- **Decision:** both controls were **removed**, not faked. `cvhome.console.store` still records which store
  is *open*, which is genuinely a property of the tab rather than the account.
- **Expected contract:** `GET`/`PUT /tenancy/api/v1/user-account/preferences` returning
  `{defaultStore: string | null, storeOrder: string[]}`, or a `defaultStore` field on `ReadableUser`.
- **Placeholder:** `TODO(lessons.md)` in `store-switcher.ts`.

## Shell — an org admin cannot read its own organization

- **Screen:** the sidebar header, which shows the organization's name and initial.
- **What the UI needs:** the name of the org the signed-in user belongs to.
- **What is missing:** every method on `OrgManagerApi` is `@PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")`,
  including `find-one`. The principal carries an org **id** (uaa spreads user metadata into the token
  claims), so the console knows *which* org and can learn nothing else about it.
- **Why it is required:** the org name is the first thing in the sidebar, and it is the only place the
  console tells a user which tenant they are working in. It also has nowhere else to come from — an
  organization has no display name anywhere in the console today.
- **Decision:** the sidebar shows the product's own brand until there is a name to show, rather than the
  fixture's "ACME".
- **Expected contract:** `GET /tenancy/api/v1/org/current` → `{id, name}`, scoped to the caller's own org
  and readable by any authenticated principal that has one.
- **Placeholder:** `TODO(lessons.md)` in `console-sidebar.ts` and `console-shell.facade.ts`.

## Shell — no merchant-readable list of placeable pods

- **Screen:** create store, the "hosting region" section of `Create Store.dc.html` — region cards with a
  latency figure and a data-residency note.
- **What the UI needs:** the regions a merchant may place a store in, with something meaningful to choose
  between.
- **What is missing:** two things.
  1. **The list.** `GET /pod-registry/api/v1/pod/list` scopes to the caller: a super admin sees every pod,
     an org admin sees only *its own private* pods. The shared pods a normal merchant is actually placed
     into are found by `PodServiceImpl.listPlaceablePublicPods()` — which exists, is covered by tests, and
     **is exposed on no endpoint at all**. So for an ordinary merchant the list comes back empty.
  2. **The content.** `Pod` is `{id, name, shortenPodId, endpoint, orgId}`. There is no region, no
     latency, no data-residency jurisdiction — none of what the design's cards are made of.
- **Why it is required:** placement is permanent (`Create Store.dc.html` marks it so), and it decides
  where a merchant's customer data lives. That is not a decision to make for someone silently.
- **Decision:** the section renders **only when the operator actually has pods to choose from**, listing
  the real ones; otherwise it is omitted and the registry places the store, which is what already happens.
  The chosen pod is sent as `pod: {id}` — a hint the registry honours only if it finds it eligible.
- **Expected contract:** expose `listPlaceablePublicPods()` as `GET /pod-registry/api/v1/pod/public/placeable`,
  and add `region`, `jurisdiction` and a capacity or health hint to `Pod`.
- **Placeholder:** `TODO(lessons.md)` in `create-store.html` and `pod.service.ts`.

## Shell — provisioning has four states and no detail

- **Screen:** create store, the progress screen.
- **What the UI needs:** to show what is happening while a store is built, and what to do when it is not.
- **What is missing:** anything beyond `ProvisioningState` — `NOT_STARTED`, `IN_PROGRESS`, `SUCCESSFULLY`,
  `FAILED` — read by re-fetching the store's row from
  `GET /tenancy/api/v1/store-manager/store-info?store=`. There is no per-step progress, no percentage, no
  estimate, no failure reason, and **no retry**: `FAILED_PROVISIONING` leaves a store row the merchant can
  see and cannot act on.
- **Why it is required:** provisioning is the first thing a new merchant watches the product do, and a
  failure there is unrecoverable from the console.
- **Decision:** the seven-row checklist that animated on a client-side timer is **gone**. It reported
  success at a fixed moment regardless of what the server was doing, invented per-task timestamps and node
  names (`fra-07`, `pg-14`), and could never reach a failure state at all. The page now polls the real
  state, shows the four outcomes honestly, and stops after two minutes saying it lost track rather than
  claiming a failure it cannot see.
- **Expected contract:** a failure reason on the store row, and an idempotent
  `POST /store-manager/private/store/{id}/reprovision` — the row already exists, so the console must not
  offer "create it again".
- **Placeholder:** `TODO(lessons.md)` in `create-store.html` and `create-store.facade.ts`.

## Shell — no notifications service

- **Screen:** the toolbar bell, its unread count, the popover feed, "mark all read" and "view all"
  (`Admin Dashboard.dc.html`).
- **What is missing:** all of it. No service, no events, no read-state, and no notifications page for
  "view all" to lead to.
- **Decision:** the bell was **removed** rather than shown disabled or opening an empty popover. A bell is
  a promise that something will appear in it.
- **Expected contract:** a per-user feed scoped by org and store —
  `GET /notifications?unread=`, `POST /notifications/read`, with a websocket or poll for the count. Most of
  the entries the design shows (new order, payment held, low stock) are events other services already
  publish, so this is plausibly a consumer rather than a new source of truth.

## Shell — no sidebar badge counts

- **Screen:** the sidebar, which shows counts against Inventory, Orders and Payments.
- **What is missing:** any cheap count endpoint. The numbers in the fixture (12, 5, 7) were invented.
- **Decision:** removed. A number in a navigation rail is read as fact.
- **Expected contract:** one small count per section, ideally batched —
  `GET /spg/.../attention-counts?store=` → `{orders: n, payments: n, inventory: n}` — since three separate
  round trips to paint a sidebar is a poor trade.

## Shell — no plan selection at store creation

- **Screen:** create store, the plan cards and the "stores used" allowance meter.
- **What is missing:** the concept. Creating a store asks billing for a **quota decision**
  (`ExternalStoreQuotaApi.private/store-create`, which answers yes or refuses with
  `StoreQuotaRefusedException`), not for a plan. A subscription belongs to a store, so there is nothing to
  subscribe to until the store exists. The allowance meter is worse: every entitlement read is
  store-scoped, so the console cannot learn the org's store ceiling before it tries and is refused.
- **Decision:** both were removed. This is recorded so the design's plan card is not mistaken for missing
  work — the sequencing in the design is arguably wrong, and the plan step probably belongs *after*
  provisioning.
- **Expected contract:** either an org-scoped entitlement read for the store ceiling, or accept that the
  refusal is the answer and surface it well when it arrives.

## Shell — no global search

- **Screen:** the toolbar's "Search orders, products…" box.
- **What is missing:** any cross-entity search. Dead in seller-ui too, where it is a decorative
  `nb-search` in the header.
- **Status:** carried over as-is; not addressed in this module.

## Shell — uaa's ID token carries no profile claims

**Found by reading what `/api/v1/auth/me` actually returns, against the running stack.**

- **Screen:** the toolbar's profile menu, which shows a name and an email address.
- **What is missing:** the name and the email. `principal.claims` holds the **ID token's** claims and
  only those — `sub, aud, azp, auth_time, iss, exp, iat, nonce, jti, sid`. The OIDC standard fields are
  serialized on the principal itself (`givenName`, `familyName`, `email`, `preferredUsername`,
  `fullName`) and every one of them is `null`. The only human-readable identity available is
  `principal.name`, the username — `org1-admin`.
- **A pre-existing bug this surfaced:** `AuthService`'s `AuthUser` declared `given_name`,
  `family_name`, `preferred_username`, `email_verified` and `user_type` and read them from
  `principal.claims`. None of them is there; all five were silently `undefined` and had been since the
  interface was written. Nothing rendered them until this module, so nothing noticed. The interface now
  describes what the endpoint returns.
- **Why it matters:** `JwtCustomizerConfig` spreads user metadata and roles into the **access token**,
  but this endpoint exposes the ID-token principal, which uaa does not enrich. So the console shows a
  username where the design shows a person.
- **Expected contract:** have uaa request and populate the `profile` and `email` scopes on the ID token,
  so `givenName`, `familyName` and `email` arrive; or expose the access token's claims here instead.
- **Placeholder:** documented in `console.api.service.ts` (`loadUser`) and `auth.service.ts`.

## Shell — user-account/current is broken for JWT callers

- **Screen:** the toolbar's profile menu. This is the endpoint that *should* answer it.
- **What is broken:** `GET /tenancy/api/v1/user-account/current` returns **500 for every caller**.
  `UserAccountApi.current` binds `@AuthenticationPrincipal Principal principal`, but the authenticated
  principal is a `Jwt`, which does not implement `java.security.Principal`. The argument resolves to
  null and `principal.getName()` throws:
  ```
  java.lang.NullPointerException: Cannot invoke "java.security.Principal.getName()"
      because "principal" is null
  ```
  It also carries **no `@PreAuthorize`**, so an unauthenticated request reaches the method body and gets
  a 500 where it should get a 401.
- **Why it matters:** it is the only endpoint that returns the signed-in user's real name and email —
  the two things the previous entry says the token does not carry. Both routes to an identity are
  therefore closed. seller-ui never hit this because it reads users by id, not `current`.
- **Expected contract:** bind `@AuthenticationPrincipal Jwt` (or take the `Authentication` and read
  `getName()`), and add an authentication requirement so an anonymous call is refused rather than
  crashing.
- **Placeholder:** documented in `console.api.service.ts` (`loadUser`), which deliberately does not call
  it.

---

## Dashboard — no revenue anywhere

- **Screen:** `/dashboard`, the first and largest KPI tile in `Admin Dashboard.dc.html`.
- **What the UI needs:** money taken over the selected period, and its movement against the previous one.
- **What is missing:** any sum of any amount. All three merchant statistics are `count(...)` queries —
  `order-statistic` counts orders, `customer-statistic` counts orders, `product-statistic` counts order
  lines. Nothing anywhere sums `order.total`. Billing knows about subscriptions, which is what the
  *merchant* pays cvhome, not what the merchant's shoppers pay them.
- **Why it is required:** it is the headline figure of the whole console, and the one number a merchant
  opens the dashboard to see. Its absence is why the tile is rendered "Not available yet" rather than
  quietly dropped — a dashboard with no revenue on it should look like an unfinished dashboard.
- **Expected contract:** `POST /spg/checkout/api/v2/private/revenue-statistic` with the same
  `StatisticRange` body, answering `(day, currencyCode, sum(total))`. **Per currency, not a single
  total** — a store can take more than one, and adding them would be a wrong number rather than a
  missing one. Refunds should be signed or reported separately, not silently netted.
- **Placeholder:** `TODO(lessons.md)` in `dashboard.api.service.ts` (`kpis`).

## Dashboard — no stock levels

- **Screen:** `/dashboard`, the "Low stock items" KPI and the "Low stock products" attention row.
- **What is missing:** a way to ask the catalog how much of anything is left. `ProductCriteria`
  (`catalog-core/entity/product/ProductCriteria.java`) filters on name, SKU, category, manufacturer,
  availability and status — there is no quantity field and no threshold, so "products below their
  reorder point" is not expressible. Products do carry a quantity; nothing queries it.
- **Why it is required:** running out of stock is the failure a merchant most wants warning of, and it
  is the only item in the attention queue that is genuinely predictive rather than a backlog count.
- **Decision:** the KPI renders "Not available yet"; the attention row was removed, since a queue of
  two is fine but a queue row with no number is not.
- **Expected contract:** a `quantityBelow` filter on the product query, so the console can both count
  them and link through to the list. A reorder threshold per product would be better than a global one,
  but a global one would do.
- **Placeholder:** `TODO(lessons.md)` in `dashboard.api.service.ts` (`kpis`, `attention`).

## Dashboard — customer-statistic counts orders, not customers

- **Screen:** `/dashboard`, the donut. The design labels it "New vs. returning customers".
- **What is missing:** both halves of that. The endpoint behind it runs
  `select (null, billing.country, count(o.id)) from Order … group by billing.country` — it groups
  **orders** by billing country and counts orders. It cannot distinguish a new customer from a
  returning one, and it does not count customers at all: a store with one German buyer who ordered
  forty times reads identically to one with forty German buyers.
- **Decision:** the panel is retitled "Orders by customer country", which is what the query computes
  and what seller-ui renders from the same endpoint. The new-vs-returning split is not attempted.
- **Why the real thing is required:** repeat-purchase rate is the single most useful number a small
  merchant can see, and it is the one the design asked for.
- **Expected contract:** first, rename or add `orders-by-country`, since the current name will keep
  causing this mistake. Then a genuine `customer-statistic` needs a first-order date per customer —
  `(day, 'new'|'returning', count(distinct customer))`.

## Dashboard — product-statistic has no name and no quantity

- **Screen:** `/dashboard`, "Most ordered products".
- **What is missing:** the product's name, and the number of units. The query is
  `select (null, op.sku, count(o.id)) from OrderProduct … group by op.sku` — it returns the raw SKU,
  and counts **orders containing the line**, so a single order for ten units counts once.
- **Decision:** the list shows the SKU and is labelled "orders", not "sales". The console does not look
  the names up: matching SKUs against a page of `tiny-products` would be one more call and would still
  miss anything outside the page. The fixture's `{name, sales}` implied a lookup that never happened
  and a unit that never existed.
- **Expected contract:** `(sku, productId, localized name, sum(quantity), count(distinct order))`. The
  name has to come from the server because it is per-locale, and the console has no other way to
  resolve one from a SKU in a single call.

## Dashboard — no stale-order signal

- **Screen:** `/dashboard`, the attention row the design words as "Orders past 24 hours without a
  status update".
- **What is missing:** when a status last changed. `order-statistic` groups on `datePurchased`, so the
  console can see how old an *order* is but not how long it has been sitting in its current state. The
  order history holds the transitions, but no statistic exposes them.
- **Decision:** retitled to "Orders awaiting fulfilment", counting the statuses before `SHIPPED`
  (`CREATED, PENDING_PAYMENT, CONFIRMED, PROCESSING`) — a backlog rather than a staleness alarm. That
  is a weaker signal: a store that ships same-day and one that has ignored its queue for a week look
  the same.
- **Expected contract:** the last status-change timestamp on the order row, or a statistic grouped by
  `(status, age bucket)`.

## Dashboard — counting requires fetching

- **Screen:** `/dashboard`, the pending-payments tile and the payment-approvals row.
- **What is missing:** a count endpoint. The only way to learn how many transactions are waiting is
  `GET /spg/payment/api/v1/private/payment/transactions?status=WAITING_VERIFICATION&count=1` and to
  read `totalElements` off the page envelope — fetching a row in order to be told how many rows there
  are.
- **Why it matters:** cheap at one row, but it is the same shape of problem as the sidebar badge counts
  (see "Shell — no sidebar badge counts"), and both would be solved once by a small counts endpoint.
  Painting a dashboard should not require paging through anything.
- **Expected contract:** `GET …/transactions/count?status=` → `{count: n}`, and the same for orders, so
  the attention queue is a handful of counts rather than a handful of page requests.
- **Placeholder:** `TODO(lessons.md)` in `dashboard.api.service.ts` (`loadSnapshot`).

## Orders — no refund and no capture

- **Screen:** `/orders/:id`. `console-template/Order Details.dc.html` puts both in the payment panel,
  and seller-ui puts them in the header.
- **What is missing:** the endpoints. seller-core's `OrdersService` calls
  `POST /spg/checkout/api/v1/private/orders/{id}/refund` and `…/capture`; **neither is mapped anywhere
  in checkout**. Both buttons have always 404'd in seller-ui.
- **Why it is required:** taking money and giving it back are the two most consequential things a
  merchant does to an order, and the console currently cannot do either. The payment service has
  `approve` and `reject` for its own transactions, but nothing ties those to an order.
- **Decision:** both controls are absent from the page rather than present and broken.
- **Expected contract:** `POST …/orders/{id}/refund {amount?, reason?}` and `…/capture {amount?}`,
  each writing a status-history entry and delegating to the payment gateway. Partial refunds need an
  amount; a full refund should be the default.

## Orders — order addresses cannot be edited

- **Screen:** `/orders/:id`, the billing and delivery panels.
- **What is missing:** `PATCH /spg/checkout/api/v1/private/orders/{id}/customer`, which seller-core
  calls and checkout does not map. seller-ui renders both panels as editable forms with a Save button
  that has never saved.
- **Why it is required:** a mistyped delivery address is caught after the order is placed more often
  than before, and correcting it is the difference between a delivery and a return.
- **Decision:** both panels are **read-only** in console-ui. `GET /country` and `GET /zones` are still
  ported, because they turn the ISO codes on an order into names.
- **Expected contract:** the PATCH seller-core already assumes, validating that the order has not
  shipped.

## Orders — no link from an order to its payment transactions

- **Screen:** `/orders/:id`, the payment panel and the transactions list.
- **What is missing:** any shared key. `ReadableOrder` carries a `paymentStatus` string and nothing
  else; the payment service keys on its own `internalRef` and `requestRef`. seller-core records the
  consequence in a comment on its own model: *"No backend endpoint populates transactionListData
  anywhere."* — so seller-ui's transactions dialog has always been empty.
- **Why it is required:** when a payment is disputed or a capture fails, the transaction is the
  evidence, and the order is where an operator goes looking for it.
- **Expected contract:** an order reference on the transaction, or
  `GET …/orders/{id}/transactions` proxying the payment service.

## Orders — no channel, and no payment method on the order

- **Screen:** `/orders`, two columns the mockup draws.
- **What is missing:** the order records neither how it was placed (web, phone, marketplace) nor how
  it was paid beyond a status — the card brand and last four live in the payment service, unlinked
  (see above).
- **Decision:** both columns were removed, and the channel filter with them.
- **Expected contract:** a `channel` enum on the order, set at checkout; and the payment method
  summarised onto the order when the payment settles, since that is the only place it is read.

## Orders — no fulfilment or shipping model

- **Screen:** `/orders/:id` — the mockup's Create shipment, Tracking, Ships from, Promised by,
  shipping method and carrier; and `/orders` — Print picking lists and bulk "Mark as processed".
- **What is missing:** all of it. There is no shipment entity, no tracking number, no carrier, no
  promised date. `ReadableOrder.shippingModule` names a module and nothing more.
- **Why it is required:** fulfilment is most of what an operator does with an order all day, and
  right now the console can only record that a status changed.
- **Decision:** the detail screen omits every shipping block. The list keeps its bulk buttons wired to
  an honest "not available yet" toast, since the design leads with them.
- **Expected contract:** a shipment resource under the order — `POST …/orders/{id}/shipments`
  `{carrier, tracking, items[]}` — with the order's status derived from its shipments rather than set
  by hand.

## Orders — no internal notes

- **Screen:** `/orders/:id`, the "Internal notes — only visible to your team" panel, with attachments.
- **What is missing:** the concept. Status-history comments exist, but those are the customer-facing
  record; a note about a customer or a courier is a different thing and must not be mixed into it.
- **Expected contract:** `GET`/`POST …/orders/{id}/notes` with an author and a timestamp, plus file
  attachment once a media service exists.

## Orders — no cancel and no duplicate

- **Screen:** `/orders/:id`, header actions.
- **What is missing:** both. `CANCELLED` is a status, so the console *can* record it through the
  history endpoint — but that is a note, not a cancellation: nothing releases the stock reservation,
  refunds the payment or notifies the customer.
- **Decision:** neither control is offered. Recording `CANCELLED` through the status form remains
  possible and is honest about being only a status change.
- **Expected contract:** `POST …/orders/{id}/cancel {reason}` performing the whole transition, and
  `POST …/orders/{id}/duplicate` returning a new draft order.

## Orders — no customer analytics

- **Screen:** `/orders/:id`, the customer panel's "Spent", "Returns" and "Business account" figures,
  and its "View profile" link.
- **What is missing:** any per-customer aggregate. `ReadableCustomer` carries identity and addresses
  only, and there is no customer detail screen to link to — seller-ui's customer list is read-only
  with no detail view, which the feature inventory already calls the thinnest feature in the app.
- **Expected contract:** `GET …/customers/{id}/summary` → `{orderCount, lifetimeValue, returnRate,
  firstOrderAt}`. The same aggregate would answer the dashboard's new-vs-returning gap.
- **Placeholder:** `TODO(lessons.md)` on `customerStats` in `order-details.ts`. The three figures are
  drawn in the customer panel at the weight the design gives them, each reading an em dash under a
  "Lifetime figures are not available yet" note — the pattern Module 3 set for a figure with no
  source. Computing them from the one order on screen would be a different number under the same
  label.

## Orders — no invoice service

- **Screen:** `/orders/:id`, the invoice document.
- **What is present:** rendering, download and print — every figure on an invoice is already on the
  order, so console-ui builds it from `ReadableOrder` with `core/export/pdf-export.service.ts` and no
  backend at all.
  The letterhead is real too: `GET store-manager/private/store/{code}` answers with the selling
  store's trading name, logo, registered address, email and phone — the same source the old console
  printed from.
- **What is missing:** everything about an invoice being a *record* rather than a rendering — a
  stable invoice number, storage, a tax point, and emailing it to the customer. The mockup's "Email
  to customer" and its `INV-10482.pdf` filename both imply a document that exists somewhere. The
  seller's **tax registration number** has no field anywhere on the merchant store either, and an
  invoice in most jurisdictions is not valid without one.
- **Expected contract:** an invoice resource with a sequential per-store number issued when the order
  is confirmed, retrievable as a PDF, plus a send endpoint.
- **Placeholder:** `TODO(lessons.md)` on `emailInvoice()` in `order-details.ts`. The Email control is
  present in the invoice toolbar, because operators ask for it, and says it is not available yet
  rather than appearing to send.

## Orders — no stale-order signal

- **Screen:** `/orders`, the "unfulfilled for 6h" badge, the overdue notice above the table, and the
  tab badge that counted them.
- **What is missing:** the same gap the dashboard hit — nothing reports when an order's status last
  changed, only when it was placed. The status history holds the transitions, but the list endpoint
  does not join it and there is no statistic over it.
- **Decision:** all three surfaces removed. The KPI row counts orders *awaiting fulfilment* by status
  instead, which is a backlog rather than a staleness alarm.
- **Expected contract:** `lastStatusChangeAt` on the order row, or a statistic grouped by
  `(status, age bucket)`.

## Orders — the list omits line items and the customer

**Found by QA against the running stack, and it contradicted this module's plan.**

- **Screen:** `/orders`, the table's Items and Customer columns.
- **What is missing:** `GET /private/orders` returns `ReadableOrder` objects with **`products: null`
  and `customer: null`**. The detail endpoint populates both. So although the envelope is
  `ReadableList<ReadableOrder>` and each row *looks* like a whole order, two of its most useful
  fields are not filled in on the list.
- **Consequence:** an item count per row is not obtainable without one detail call per row, so the
  Items column was **removed from the table**. The customer column survives only because the buyer's
  name and email are also on `billing`, which the list does send — that is what the row falls back to.
- **Why it matters:** "how many things are in this order" is a column every order table has, and the
  data is one join away on a query the server is already running.
- **Expected contract:** populate `products` (or at least a `lineCount`) and `customer` on the list
  projection.

## Orders — totals arrive unformatted and unlabelled

- **Screen:** `/orders` (the Total column) and `/orders/:id` (the totals block).
- **What is missing:** `OrderTotal.text` and `OrderTotal.title` are **null on every total, on both the
  list and the detail endpoint**. Only `products[].price` and `products[].subTotal` come
  pre-formatted. `OrderApi.get` even declares `PriceNotFormattableException`, so formatting is clearly
  intended somewhere — it just does not happen for totals.
- **Consequence:** the console formats money itself from `value` (a `BigDecimal`, decimal units, not
  minor units) and the order's `currency`, and labels each line from `module` — `subtotal`, `total`,
  and whatever else a store's total modules add.
- **Worth recording:** seller-ui does not do this. Its template renders `US${{ total.value }}`
  literally, so a Saudi-riyal order displays as `US$9400` — wrong currency and unformatted. That is a
  seller-ui defect, not a backend gap, but it is why the two consoles disagree on this screen and the
  difference should not be read as a console-ui regression.
- **Expected contract:** populate `title` and `text` on `OrderTotal` the way `ReadableOrderProduct`
  already does for line prices.


## Orders — no seller-side order creation

- **Screen:** `/orders`, the "Create order" button in the page header.
- **What is missing:** checkout mints an order in exactly one way — `POST /cart/{code}/checkout`,
  which needs a cart built by a shopper session. `OrderApi` exposes no private create, so a seller
  cannot record a phone or counter order the way the design's button implies.
- **Why it is required:** every order book in this category can take an order the seller took
  themselves. Without it "Create order" is a button that can only apologise.
- **Expected contract:** `POST …/private/orders` taking a customer (or a walk-in), lines by sku and
  quantity, and an initial payment status, bypassing the cart.
- **Placeholder:** `TODO(lessons.md)` on `createOrder()` in `orders.ts`; the control says it is not
  available yet rather than failing.

## Orders — the store's logo URL is not reachable from the browser

- **Screen:** `/orders/:id`, the invoice letterhead.
- **What happens:** `store-manager/private/store/{code}` answers with
  `logo.path = http://localhost:9000/<bucket>/…/LOGO/logo.jpeg` — the object store's *internal*
  address. From the operator's browser that host is either wrong or unreachable, so the image fails
  and the letterhead printed a broken-image glyph.
- **Consequence:** the console falls back to the store's lettermark when the image does not load,
  which is right regardless, but every store logo in the console is affected, not just the invoice.
- **Expected contract:** the merchant service should return a gateway-relative or publicly resolvable
  URL for `ReadableImage.path`, the way it does for product images, rather than the storage host it
  happens to use internally.

## Orders — checkout's country list is the store's supported set

- **Screen:** `/orders/:id`, the address panels and the invoice.
- **What happens:** `GET /country` answers with the countries the *store* supports — four, on the
  store this was written against — not the ISO list. An order or a store address may legitimately
  name a country outside it: a store trading from Saudi Arabia had `SA` on its own invoice, because
  `SA` is not in its own supported list.
- **Consequence:** the console resolves anything the list does not know through `Intl.DisplayNames`,
  which is formatting rather than data — the code is still the server's, the browser only spells it
  out, in the reader's language.
- **Expected contract:** either a full ISO country reference endpoint distinct from the store's
  supported-shipping set, or names on the addresses themselves.

## Orders — line prices arrive formatted, with no number behind them

- **Screen:** `/orders/:id`, the items table and the invoice.
- **What happens:** `ReadableOrderProduct.price` and `.subTotal` are `String`, formatted by checkout
  in its own locale (`SAR550.00`), and `OrderProductEntity` carries no numeric amount at all — no
  unit price, no line total. Order *totals* are the opposite: raw `value`, no formatting (see
  "Orders — totals arrive unformatted and unlabelled").
- **Consequence:** one screen showed two formats for the same currency — `SAR550.00` on the lines
  above `٥٬٦٩٠٫٠٠ ر.س.` on the totals. The console now reads the number back out of the string and
  formats it with everything else, falling back to the server's string when it cannot be parsed.
  Parsing a server rendering is not something a client should have to do.
- **Expected contract:** numeric `price` and `subTotal` on `ReadableOrderProduct` — the same fix as
  the totals gap, from the other side: send numbers and let the client format.

## Store management — six designed store fields do not exist

- **Screen:** `/store-management/details`, from `console-template/Store Management.dc.html`'s
  "Store details" block.
- **What the UI needs:** the design's twelve-field identity grid — store name, legal entity name,
  store slug, category, support email, support phone, default currency, default language, timezone,
  tax/VAT number, business address, short description.
- **What is missing:** six of the twelve have no counterpart anywhere. `legalName`, `taxNumber`,
  `timezone` and `shortDescription` return zero hits across every `.java` file in `store-pod` and
  `store-core`; `slug` exists only on products, categories and content (`ResourceUrlAccess`), never
  on a store; and there is no store category concept at all. `PersistableMerchantStore` cannot carry
  any of them.
- **Why it is required:** the legal entity name and tax number are what an invoice needs to be a
  valid document in most jurisdictions — the invoice built in Module 4 currently prints the trading
  name because that is the only name there is. A slug is what makes a storefront URL readable, and a
  timezone is what makes "placed at 14:32" mean anything to a seller in a different one.
- **Expected contract:** additional columns on `MerchantStoreEntity`, surfaced through
  `ReadableMerchantStore`/`PersistableMerchantStore`: `legalName`, `taxNumber`, `slug` (unique per
  store, validated like a friendly URL), `category`, `timezone` (IANA zone id), `shortDescription`.
- **Placeholder:** the fields render disabled in a "Not recorded by the platform" block, with the
  reason beside them, and are `disabled` in `StoreSettingsFormService` so they cannot reach a
  request body. `TODO(lessons.md)` markers in `store-settings-form.service.ts` and
  `store-settings.api.service.ts`.

## Store management — a store has no published or maintenance state

- **Screen:** `/store-management/details` ("Store visibility"), and the page header's status pill.
- **What the UI needs:** a switch to take a storefront offline, a second one to put it into
  maintenance, and a badge in the header saying which it is.
- **What is missing:** nothing records either. `MerchantStoreEntity` has no visibility column, and
  `ProvisioningState` is a different thing — it says whether the store finished being *created*, not
  whether its owner wants it reachable. There is no endpoint to change either.
- **Why it is required:** a seller preparing a catalogue needs the storefront dark until it is
  ready, and a seller mid-migration needs a maintenance page rather than a broken one. Today the
  only way to take a store offline is to delete it.
- **Expected contract:** `published` and `maintenanceMode` booleans on the store, with
  `PUT /private/store` accepting both, and the storefront honouring them — a maintenance page rather
  than a 404.
- **Placeholder:** both switches render disabled in the "Not recorded by the platform" block. The
  header's published/unpublished badge is **removed** rather than disabled — a badge cannot carry a
  reason, and one that always reads "Published" is an assertion rather than a fact.

## Store management — a logo or banner can be uploaded but never removed

- **Screen:** `/store-management/branding`.
- **What the UI needs:** the design's "Replace" and "Remove" buttons under the logo tile and the
  banner row.
- **What is missing:** `MerchantStoreApi` maps `POST /private/store/marketing/logo` and
  `.../banner` and **no delete counterpart**, and `PersistableMerchantStore` carries neither image,
  so `PUT /private/store` cannot clear one either. seller-core has `removeStoreLogo` and
  `removeStoreBanner` posting to `/v1/private/store/{store}/marketing/logo|banner` — paths missing
  the `/spg/merchant/api` prefix every sibling carries and mapped by no controller. Those buttons
  have always 404'd in seller-ui.
- **Why it is required:** a seller who uploads the wrong logo, or rebrands and has no replacement
  ready, has no way back to the default. The image is permanent from first upload.
- **Expected contract:** `DELETE /spg/merchant/api/v1/private/store/marketing/logo` and
  `.../banner`, both store-scoped like their POST counterparts.
- **Placeholder:** Remove is not rendered. Replace is folded into the drop zone, since re-uploading
  is what replacing actually is, and the card says so: "Uploading again replaces the current image.
  The platform has no way to remove one once set."

## Store management — slider images carry no schedule, link or file metadata

- **Screen:** `/store-management/slider`, from `Store Management.dc.html`'s "Store slider images".
- **What the UI needs:** each slide row shows a `LIVE` or `SCHEDULED` tag, a click-through link, and
  a `1600×640 · 248 KB · JPG` metadata line under the name.
- **What is missing:** `ReadableSliderImage` is a record of exactly `(priority, name, url)` and
  `PersistableMerchantStore.sliderImages` carries only `(priority, name)`. There is no schedule, no
  target URL, no dimensions, no byte size and no format. `POST .../marketing/add-slider-image`
  answers with the same three fields.
- **Why it is required:** a carousel slide that links nowhere is decoration; the whole point of a
  storefront hero is to send the shopper at a product or a collection. Scheduling is what lets a
  seller prepare a sale banner in advance rather than uploading it at midnight.
- **Expected contract:** `link`, `startsAt` and `endsAt` on `ReadableSliderImage` and its
  persistable counterpart. Dimensions and byte size can be derived by the server at upload time —
  they are already known to `InputContentFile` — and returned on the readable record.
- **Placeholder:** the tag, link and metadata line are not rendered. Reordering and deleting *are*
  supported, because `PUT .../marketing/slider-images` replaces the whole list.

## Store management — the landing-page endpoints seller-ui calls do not exist

- **Screen:** `/store-management/home`, from `Store Management.dc.html`'s "Store home page".
- **What the UI needs:** per-language landing copy — title, body text, meta description and tags.
- **What is missing:** the three paths seller-core's `StoreService` uses are mapped by no
  controller. `GET /spg/content/api/v1/private/content/any/{pageCode}`, `PUT .../private/content/{code}`
  and `POST .../private/content` do not exist; `ContentApi` maps `/private/content/pages`,
  `/private/content/boxes`, `/private/content/page` and `/private/content/box`, and a bare
  `PUT`/`POST` on `/private/content` is a 405. seller-ui's landing-page screen has therefore never
  saved anything — the same class of finding as Orders' Refund and Capture buttons.
- **Why it is required:** it is the only copy on the storefront's front page.
- **What console-ui does instead:** builds against the endpoints that *do* exist, using **content
  boxes**. A `ContentBox` is a `code` plus per-language `descriptions[]`, and `ContentDescription`
  carries `name`, `description`, `metaDescription` and `keyWords` — a one-to-one fit for the four
  fields the design shows. `ContentPage` was rejected because it adds `linkToMenu`, a storefront
  navigation concern that means nothing for home-page copy. `GET .../box/{code}/exists` gives a real
  create-versus-update pre-flight rather than seller-ui's guess.
- **Expected contract:** none needed — this one is a client-side correction, not a backend gap. It
  is recorded because the *old* client is still shipping the broken calls until seller-ui is retired.

## Store management — a supported language can be added but never removed

- **Screen:** `/store-management/details`, the "Supported languages" checkboxes.
- **What happens:** `PUT /private/store` carries the whole `supportedLanguages` list, but
  `PersistableMerchantStorePopulator.applyLanguages` iterates it and calls
  `target.getLanguages().add(lang)` — the entity's existing set is never cleared, and the update
  path merges onto the loaded entity rather than a fresh one. Ticking a language on is applied;
  ticking one off is accepted with a 200 and silently ignored.
- **Consequence:** seller-ui had this too, and simply never said so — an operator who unticked
  Russian saw it come back on the next load with no explanation. The console cannot fix it from the
  client (there is no remove endpoint and no other body shape), so it states it: the note under the
  field says additions apply and removals do not, before the operator spends a save finding out.
- **Expected contract:** `applyLanguages` should replace the set rather than union with it —
  `target.getLanguages().clear()` before the loop, or `setLanguages(languages)` — so the list the
  client sends is the list the store ends up with. The same method also dereferences
  `source.getSupportedLanguages()` without a null check, so a body that omits the field is a 500.

## Store management — no reference lists for countries, currencies or storefront languages

- **Screen:** `/store-management/details` — the country, currency and language selects; the same
  three fields exist on `/create-store`.
- **What the UI needs:** the countries a store may trade from, the currencies it may price in, and
  the languages a storefront may be published in.
- **What is missing:** all three. `GET /spg/checkout/api/v1/country` answers with the countries the
  *store ships to* — four, on the store this was written against — which is a different question
  (see "Orders — checkout's country list is the store's supported set"). Nothing serves currencies
  at all. `GET /store/languages` answers with the languages this store has already turned **on**,
  which cannot drive the control that turns them on. seller-ui shipped a 50 KB
  `assets/data/countries.json`, a `currencies.json` and a hardcoded
  `environment.client.language.array` instead, which is why its country select listed English names
  only and its language checkboxes needed a translation key per language.
- **What console-ui does instead:** treats the first two as what they are — ISO registries, not
  platform data. `ReferenceDataService` holds the 249 ISO 3166-1 alpha-2 codes and reads
  `Intl.supportedValuesOf('currency')`, and names both through `Intl.DisplayNames` in the reader's
  own language, recomputing when the console's language changes. The stored code is always folded
  into the options so a select cannot disagree with the value bound to it. The five storefront
  languages remain a constant, `STOREFRONT_LANGUAGES`, because that set genuinely is the platform's
  and only the platform can say what it is.
- **Expected contract:** an endpoint listing the languages a storefront may be published in — the
  one list of the three that is not an ISO registry and that a client cannot derive. Countries and
  currencies need no endpoint; if one is ever added it should be the full ISO list, named per
  `LanguageCode`, and kept distinct from the store's shipping set.

## Store management — DNS verification is a browser-side check, not a platform one

- **Screen:** `/store-management/domain`, the "Check DNS" buttons and the status panel.
- **What the UI needs:** to tell an operator whether the custom domain they just allocated actually
  reaches their store, and whether a certificate has been issued for it.
- **What is missing:** nothing server-side ever confirms a CNAME. `POST /router/private/allocate`
  records the hostname in the routing map and answers `void`; it does not look the domain up, and
  there is no status on `ManagerStoreDomain` — the record is `(domain, domainType)` and that is all.
  The only DNS check that exists anywhere is `DnsCheckService`, which seller-ui runs **in the
  operator's browser** against `https://dns.google/resolve`. Certificate state is likewise
  unreadable: `GET /router/public/ask-for-tls` answers 200 or 400 for whether the edge *would* serve
  TLS for a hostname, which is a restatement of whether it is allocated, not whether a certificate
  was issued.
- **Why it is required:** a domain that is allocated but not pointed at us is the single most common
  way a storefront ends up unreachable, and it is invisible from the console. A verdict from one
  public resolver, from one machine, on one network, is not the platform's answer — and if the
  operator's browser cannot reach `dns.google` (a corporate network, an ad blocker, an offline
  laptop) there is no check at all.
- **Expected contract:** a server-side check — `GET /router/private/domain-status?domain=` answering
  the resolved CNAME, whether it matches the pod hostname, and the certificate's issue and expiry —
  run from the platform's own resolvers rather than the operator's, and ideally re-run periodically
  so the console can show a domain that has *stopped* pointing at us.
- **Placeholder:** the DoH lookup is carried over as-is, in `api/dns/dns-check.service.ts`, and it
  **gates the allocation** — the custom-domain field carries it as an async validator, so a domain
  whose CNAME does not already point at the pod cannot be added. That is seller-ui's rule, kept
  deliberately: the server accepts any hostname without looking, so if the client does not check,
  nothing does, and the seller is left with a storefront that is unreachable on a domain the console
  told them was fine. The copy no longer claims anything it cannot know — the fixture's panel said
  "SSL issued", "retrying every 5 minutes" and "we will email you when it resolves", three
  assertions with nothing behind any of them. Two outcomes deliberately do not block: a resolver the
  browser could not reach (a warning, because that says nothing about the operator's DNS and
  blocking would make the field unusable behind a filtering network), and a store whose pod lookup
  was refused, where there is no target to compare against.

## Store management — nothing answers what address a store is served at

- **Screen:** `/store-management/domain`, the default-subdomain row and the CNAME target.
- **What the UI needs:** the URL of the storefront, and the hostname a custom domain has to point at.
- **What is missing:** no endpoint answers either. A `SUB_DOMAIN` record stores only its label, and
  the rest of the hostname has to be assembled from two calls on two different tiers —
  `GET /tenancy/api/v1/saas/public/saas-properties` for `{alis, domain}` and
  `GET /tenancy/api/v1/router/store-pod-by-store-id` for the pod's `shortenPodId` — into
  `{label}.{alis}-{shortenPodId}.{apex}`. That formula exists in exactly one place on the platform:
  seller-ui's `StoreDomainFacade.podServerDomain()`, a client-side string concatenation. Neither
  service knows the other exists, and the pod lookup is refused outright for a suspended or archived
  store, so a store can be in a state where the console cannot say where it lives.
- **Why it is required:** it is the first thing a seller wants from this page, it is what the header's
  "Preview storefront" button would need a target for, and getting the CNAME instructions wrong by one
  segment sends the operator to their registrar to enter a hostname that will never resolve.
- **Expected contract:** `storefrontUrl` and `cnameTarget` on the store's own record, or a
  `GET /router/private/addresses` that answers both — assembled by the service that owns the routing
  map rather than by every client that needs to display a URL.
- **Placeholder:** `podHostname()` in `api/tenancy/saas.service.ts` reproduces the formula, with both
  legs optional. When either is refused the section shows "Address not available" and hides the DNS
  record block rather than printing a half-built hostname.

## Store management — a content description's keywords are dropped by both mappers

- **Screen:** `/store-management/home`, the "Search keywords" field.
- **What the UI needs:** the keywords a storefront's landing page is indexed on, per language.
- **What is missing:** the column exists and nothing uses it. `ContentDescription` has
  `META_KEYWORDS` on the entity and `keyWords` on the DTO, but `ContentFacadeImpl.buildDescriptions`
  sets `metatagDescription`, `title`, `name`, `seUrl`, `description` and `metatagTitle` and **never
  `metatagKeywords`**, while `ReadableContentBoxPopulator.populateDescription` reads `name`,
  `description`, `metaDescription`, `id`, `seUrl`, `title` and `language` and **never the keywords**
  either. A value sent is dropped on the way in; a value already in the database is invisible on the
  way out. seller-ui had the field in its form too, so it has never worked there.
- **Why it is required:** it is one of the four fields the design's landing-copy block asks for, and
  the only one of them that is about being found rather than about what the page says.
- **Expected contract:** two lines — `contentDescription.setMetatagKeywords(source.getKeyWords())`
  in `buildDescriptions`, and `d.setKeyWords(description.getMetatagKeywords())` in
  `populateDescription`. No schema change; the column is already there.
- **Placeholder:** the tag field renders disabled with the reason under it, and its control is
  `disabled` in `StoreSettingsFormService` so it cannot reach a request body — `sectionValueOf`
  reads `value`, which omits disabled controls. `TODO(lessons.md)` markers in
  `store-settings-form.service.ts` and `home-section.ts`.

## Store management — a content box save is all-or-nothing per language

- **Screen:** `/store-management/home`, the language track.
- **What happens:** `PUT /private/content/box/{id}` replaces the box's description list with exactly
  what the body carries — `buildDescriptions` matches an existing row by language and overwrites it
  field by field, then `contentModel.setDescriptions(descriptions)` swaps the list. Two consequences
  that are not visible from the endpoint's shape. A **field** omitted from a description is not left
  alone, it is written as null. And a **language** omitted from the list is not deleted either — the
  entity's `@OneToMany` has no `orphanRemoval`, so the row survives in the database and reappears on
  the next read, so an "edit" that dropped a language looks like it silently reverted.
- **Consequence:** the console sends every language and every field on every save, carrying through
  the ones it does not edit (`title`, `friendlyUrl`, the description's own id) from what the read
  returned. That is why the home section's *Save changes* posts the whole box rather than the
  language on screen.
- **And a description must be named.** `BaseDescription.name` is `@NotEmpty` with
  `@Column(nullable = false)`, so a language with body copy and no headline cannot be stored — and
  it arrives as a **500**, not as a 400 naming the field, because the violation surfaces from the
  persistence layer rather than from `@Valid` on the request. The first version of the console's
  save sent a placeholder description for every untranslated language and every create failed on
  it. The console now sends only the languages that have a title, and refuses in the form to let a
  language hold copy without one, so nothing an operator typed is dropped by that filter.
- **Also:** `buildDescriptions` iterates `content.getDescriptions()` with no null check, so a body
  that omits `descriptions` entirely is a 500 rather than a 400.
- **Expected contract:** either `orphanRemoval = true` so the list is genuinely the state, or a
  per-language endpoint (`PUT …/box/{id}/description/{language}`) so a client can edit one
  translation without having to hold all of them. And `@Valid` on the request body, so a missing
  name is a 400 that names the field instead of a 500 that names nothing.

## Store management — payment and social-login reads return secrets in cleartext

- **Screen:** `/store-management/payments` and `/store-management/social-login`.
- **What happens:** both mappers decrypt before serialising.
  `PaymentConfigurationMapper.toDTO` runs `decrypt()` over `apiKey`, `secretKey` and
  `webhookSecret`; `SocialLoginConfigMapper.toDTO` does the same for `appId` and `appSecret`. So
  `GET /private/payment-configuration` hands the browser the live Stripe secret key and webhook
  secret, and `GET /private/social-login-config` hands it the OAuth app secret — as plain strings,
  in a JSON body, over whatever the operator's network is.
- **Why it matters:** encryption at rest buys nothing on these two endpoints. The value is in the
  browser's memory, in the network tab, in any HAR the operator sends to support, and in any
  browser extension with host permissions. A secret key is a bearer credential for the store's
  Stripe account; a webhook secret is what makes a forged webhook detectable.
- **Expected contract:** never return a stored secret. Answer with a hint — the last four
  characters and the date it was last written — and take a new value on write only. That is what
  the console's design assumed, and it is why the mockup had a "rotate" action rather than a field.
- **What console-ui does meanwhile:** shows them, behind a click-to-reveal, in `shared/ui/secret-field`.
  Masking a value the API has already handed over would be theatre, and would leave an operator
  unable to check a key they can read in the network tab; the section says plainly that the screen
  is handed them decrypted. When the contract changes, this component is the one thing that has to
  change with it.

## Store management — a credential written before encryption reads back as nothing

- **Screen:** `/store-management/payments` and `/store-management/social-login`.
- **What happens:** both mappers only populate a field when the stored value is *in encrypted form*
  — `PaymentConfigurationMapper.decrypt()` returns `null` for anything else, and
  `SocialLoginConfigMapper.toDTO` guards each `set` with `EncryptedValue.isEncrypted(...)`. A row
  written before `secret-crypto` was introduced therefore reads back empty.
- **Consequence:** the console cannot tell "no credential stored" from "a credential stored in a
  form we no longer read", and neither can the operator. The screen shows an empty field for a
  gateway that is, as far as the payment service is concerned, configured and live. Saving over it
  is the only way out, and does not warn that something was there.
- **Expected contract:** a migration that re-encrypts legacy rows, and until then a mapper that
  treats an unencrypted stored value as the value rather than as nothing — or at minimum a flag
  saying a value exists but could not be read.

## Store management — neither the webhook URL nor the OAuth callback is served

- **Screen:** `/store-management/payments`, the endpoint under each webhook secret; and
  `/store-management/social-login`, the callback URL a seller pastes into Google or Facebook.
- **What is missing:** both routes exist and no endpoint hands either of them out, so every client
  has to reconstruct them from facts held in three different places.
  - The webhook is `POST /api/v1/public/webhook/{storeId}/{paymentType}` on
    `PublicPaymentWebhookApi`, reached through the pod's `handle_path /payment*` — which *strips*
    the segment, so a client has to put `/payment` back on.
  - The callback is `Constants.DEFAULT_REDIRECT_URI` (`{baseUrl}/login/oauth2/code/{registrationId}`)
    with `registrationId` = `{store}.{provider}` from `SocialLoginConfigId.toRegistrationId()`,
    reached through `handle /cua*` — `handle`, which *keeps* the segment and sets
    `X-Forwarded-Prefix: /cua`, so `{baseUrl}` already carries it.
  - Both need the store's storefront host, which is itself assembled from `saas-properties` and the
    pod (see "Store management — nothing answers what address a store is served at").
- **Why it matters:** these are the two values on the page that must be exactly right, because the
  failure lands on the shopper rather than on the seller — a provider rejects a callback that does
  not match its allow-list character for character, and a webhook posted to the wrong path is a
  payment the store never hears about. Two prefixes behaving differently (`handle` keeps,
  `handle_path` strips) is precisely the sort of detail a client should not be deriving.
- **What went wrong here, as evidence:** the first version of this console shipped a callback URL of
  `https://{storeId}.{podTarget}/login/oauth2/code/…` — an invented host and a missing `/cua`. It
  looked plausible and would never have worked. seller-ui sidestepped the host by printing the path
  fragment alone.
- **Expected contract:** `callbackUrl` on `ReadableSocialLoginConfig` and `webhookUrl` on
  `ReadablePaymentConfiguration`, each built by the service that owns the route.
- **Placeholder:** the console assembles both, and falls back to the bare path — which an operator
  can still recognise — when the pod lookup that supplies the host was refused.

## Store management — a payment type's required attributes are not served

- **Screen:** `/store-management/payments`, which cards show a credential grid.
- **What is missing:** `PaymentType` declares `attrs` — `STRIPE` and `PAYPAL` require `clientId` and
  `secretKey`, `COD` and `MANUAL_TRANSFER` require nothing — and `GET /supported-payment-types`
  serialises the enum by name, so the attrs never reach a client. The console hardcodes which two
  types have no credentials, which means a new gateway added to the enum renders a credential grid
  it may not want, or none when it needs one.
- **Also worth noting:** the attrs name `clientId` and `secretKey`, while the DTO carries `apiKey`,
  `secretKey` and `webhookSecret`. The two descriptions of the same thing do not line up.
- **Expected contract:** serialise `PaymentType` as an object with its `attrs`, so a client can
  render exactly the fields a gateway declares instead of guessing from a hardcoded list.

## Dashboard — the merchant statistics outage is over

- **Screen:** `/dashboard` and `/orders`, the KPI rows on both.
- **What was broken:** Module 3 recorded all three merchant statistics answering 500 for every
  caller, on a date-type mismatch in checkout. Both pages were built with that leg optional as a
  result, and both reported their counts unavailable.
- **What is true now:** fixed on the backend. `StatisticRange` is a `ZonedDateTime` pair, the API
  calls `.toInstant()`, and `Order.datePurchased` is an `Instant`, so the types line up. Verified
  against the running stack during Module 5: `POST /private/order-statistic` answers 200 with
  `StatisticList[entries=[StatisticEntry[date=2026-08-19, name=PENDING_PAYMENT, value=1]]]`.
- **What stays:** the `catchError` on that leg in both `dashboard.api.service.ts` and
  `orders.api.service.ts`. It is no longer working around an outage — it encodes that a row of
  counts is secondary and must never be able to take its page down.
- **Still open:** `customer-statistic` and `product-statistic` answer with `date=null`, which is the
  separate gap recorded above.
