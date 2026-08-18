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

## Dashboard — the three merchant statistics 500 for every caller

**This is a live defect, not a missing feature, and it blocks the whole dashboard in both UIs.**

- **Screen:** `/dashboard` in console-ui; `/pages` in seller-ui.
- **What happens:** `order-statistic`, `customer-statistic` and `product-statistic` all answer
  `500 COMMON.INTERNAL_ERROR`. The checkout log gives the cause:
  ```
  org.springframework.dao.InvalidDataAccessApiUsageException:
    Argument to query parameter has an incompatible type
    (java.util.Date is not assignable to java.time.Instant)
  ```
  `Order.datePurchased` is an `Instant`, and the three API classes convert the range back to
  `java.util.Date` before calling the repository:
  ```java
  orderRepository.orderStatistic(Date.from(range.fromDate().toInstant()),
                                 Date.from(range.toDate().toInstant()), merchantStore);
  ```
  while `OrderRepository.orderStatistic` / `customerStatistic` and
  `OrderProductRepository.productStatistic` declare their parameters as `Date`. Hibernate refuses the
  binding, so the query never runs. Nothing about the request matters — every range, every store,
  every caller fails identically.
- **Why it was not noticed:** seller-ui's dashboard renders **blank** when this happens. Its ECharts
  panels swallow the failure and draw nothing, so the screen looks empty rather than broken.
  console-ui shows the error and its trace id, which is how it was found.
- **Expected fix** (six lines, all mechanical, in `store-pod/checkout`):
  - `OrderRepository.orderStatistic`, `OrderRepository.customerStatistic` and
    `OrderProductRepository.productStatistic` — change the `@Param("from")` / `@Param("to")` types from
    `java.util.Date` to `java.time.Instant`.
  - `OrderStatisticApi`, `CustomerStatisticApi`, `ProductStatisticApi` — drop the `Date.from(...)`
    wrappers and pass `range.fromDate().toInstant()` / `range.toDate().toInstant()` straight through.
- **Consequence for this module:** the three real panels are **implemented but unverified against live
  data**. Their shaping is covered by `dashboard.api.service.spec.ts`; what could not be checked is
  whether the numbers match seller-ui's, because seller-ui cannot show any either. Re-run the two-tab
  comparison in the Module 3 plan once checkout is fixed.

---

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

## Orders — no invoice service

- **Screen:** `/orders/:id`, the invoice document.
- **What is present:** rendering, download and print — every figure on an invoice is already on the
  order, so console-ui builds it from `ReadableOrder` with `core/export/pdf-export.service.ts` and no
  backend at all.
- **What is missing:** everything about an invoice being a *record* rather than a rendering — a
  stable invoice number, storage, a tax point, and emailing it to the customer. The mockup's "Email
  to customer" and its `INV-10482.pdf` filename both imply a document that exists somewhere.
- **Expected contract:** an invoice resource with a sequential per-store number issued when the order
  is confirmed, retrievable as a PDF, plus a send endpoint.

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

