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
  estimate and **no retry**: `FAILED_PROVISIONING` leaves a store row the merchant can see and cannot act on.
- **Resolved since:** the *failure reason* is no longer missing. `ManagerStoreDto.provisioningError` now
  carries the pod's own refusal — for a validation failure, the fields it objected to — and the progress
  screen renders it verbatim under the translated failure line. Before that, `StoreProvisioningService`
  caught the pod's problem body, called `failProvisioning(store)` and put the whole thing in `log.error`,
  so the console could only ever say "failed".
- **Why it is required:** provisioning is the first thing a new merchant watches the product do, and a
  failure there is unrecoverable from the console.
- **Decision:** the seven-row checklist that animated on a client-side timer is **gone**. It reported
  success at a fixed moment regardless of what the server was doing, invented per-task timestamps and node
  names (`fra-07`, `pg-14`), and could never reach a failure state at all. The page now polls the real
  state, shows the four outcomes honestly, and stops after two minutes saying it lost track rather than
  claiming a failure it cannot see.
- **Expected contract:** an idempotent `POST /store-manager/private/store/{id}/reprovision` — the row
  already exists, so the console must not offer "create it again".
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

## Store creation — tenancy validates merchant's required fields, and has to

- **Screen:** create store.
- **What happened:** the create form posted four fields — name, country, currency, pod. Tenancy's
  `CreateStoreRequest` typed only `name` and `pod` and collected the rest into a `@JsonAnySetter` map to
  forward, so the body was accepted, the store row was created, and `POST /private/store` answered `200`
  with `IN_PROGRESS_PROVISIONING`. The pod then refused the create off the outbox: `MerchantStoreDetails`
  is `@NotNull` on `email` and `phone`, `PersistableMerchantStorePopulator.applyLanguages` dereferences
  `defaultLanguage` and `supportedLanguages` unguarded, and `merchant.merchant_store` is NOT NULL on
  `theme`, `color_theme`, `store_email`, `country_id`, `currency_id` and `language_code`. The merchant sees
  "provisioning", then "failed", and never learns which field was wrong. seller-ui's form collected all of
  them, which is why the same flow worked there.
- **Decision:** the fields the pod refuses without are **typed and validated on tenancy's
  `CreateStoreRequest`**, so an incomplete body is a synchronous `400` with field errors the form binds
  rather than a broken store row minutes later. This duplicates part of merchant's model inside tenancy —
  deliberately, and against that class's original argument for staying untyped. The reason the argument
  loses here is that creation is asynchronous: there is no later moment at which the caller can be told.
  Fields the pod merely tolerates (`inBusinessSince`, `dimension`, `weight`, `useCache`, `template`) stay
  in the forwarded map.
- **Cost, stated plainly:** a change to `MerchantStoreDetails`'s `@NotNull`s, to `merchant_store`'s NOT
  NULL columns, or to the `MerchantStore` **entity**'s `@NotEmpty`s has to be applied to
  `CreateStoreRequest` too. The javadoc on both sides says so.
- **Read the entity, not just the schema.** The first cut of this took the required set from the DDL and
  left `city` and `postalCode` optional, because both columns are nullable. They carry `@NotEmpty` on the
  `MerchantStore` entity, so Hibernate refuses them at *persist* time — below the layer that renders
  validation failures, so it surfaces as a `ConstraintViolationException` → **500**, not a 400 with field
  errors. QA caught it as a real FAILED store row reading `COMMON.INTERNAL_ERROR`. The full required set
  is: name, email, phone, theme, colorTheme, currency, defaultLanguage, supportedLanguages, and
  address.{country, city, postalCode}. The street line and stateProvince are genuinely optional.
- **Still open:** `PersistableMerchantStorePopulator.applyLanguages` remains an unguarded dereference, so a
  caller that reaches merchant directly with no `defaultLanguage` still gets a 500 rather than a 400. Not
  reachable through the console any more, but not fixed either.

## Getting started — three dead controls that already had somewhere to go

- **Screen:** getting started (`/getting-started`).
- **What happened:** "View plans", "Compare plans", the walkthrough tile and every short-guide row all
  called `notAvailable()`, which raises a "not built yet" toast. Two of the three were not gaps at all:
  - `GET /billing/api/v1/plan/public/plans` has been serving the real catalogue to the **marketing
    page** the whole time. The console simply never called it.
  - The written guides exist on the documentation site.
- **Decision:** the plan buttons open `PlanDialog`, which reads that same catalogue through
  `SubscriptionService` and renders it with the *same mapper* the marketing pricing section uses —
  moved to `@shared/billing/pricing.mapper` so there is one definition of how a catalogue becomes
  cards, and the two surfaces cannot disagree about a price. The guide rows are now `<a>` links to
  `DOCS_BASE_URL` (`https://cvhome-saas.github.io/`), opening in a new tab with
  `rel="noopener noreferrer"` — the console holds a session. The walkthrough opens `VideoDialog`, the
  console's own `<video>` player rather than a third-party embed, so no external player script runs on
  a page behind that session.
- **Still open:** *choosing* a plan from this page has nowhere to go, and for a structural reason — a
  subscription belongs to a store, and this is the one console page where no store exists. The dialog
  emits `chosen` and the page explains the ordering. "Book a call" stays a toast: unlike the other
  three it has no service and no page behind it.
- **Placeholder:** `FIRST_RUN_FEATURE.src` is a sample clip until an onboarding walkthrough is recorded.
  `VideoDialog` takes any URL, so replacing that one line is the whole change; setting it to `null`
  puts the player back to saying no walkthrough has been published.

## Billing — the plan banner was a constant, and every billing endpoint was unused

- **Screen:** the console chrome's plan strip, and the new billing page (`/subscription`).
- **What happened:** the banner rendered a hardcoded line — "You're on the Free plan — upgrade to add
  more stores" — on every page for every operator, whatever they were paying, with a dead Upgrade
  button. A paying customer was told to upgrade; a store whose card had just failed was told nothing.
  Meanwhile `billing-service` publishes the whole surface: `subscription/current`, `checkout`, `plan`,
  `cancel`, `resume` and `invoice/list`, none of which the console called.
- **Decision:** one `BillingFacade` reads `subscription/current` for the store the console has open,
  and both the banner and the page render from it, so they cannot disagree. The banner is now `null`
  for a healthy paid store — **no banner at all is the common case** — and appears only for a trial
  running down, a failed payment (with its grace countdown), a scheduled cancellation, or a store
  billing has not caught up with.
- **`/subscription`, not `/billing`.** The gateway claims `/billing/**` for the billing service and
  matches it *before* the console's catch-all, so a `/billing` route in this app is not merely oddly
  named — it never reaches Angular at all. `GatewayRouteLocatorImpl` reserves `tenancy`, `billing`,
  `pod-registry`, `uaa` and `spg`.
- **Checkout returns to the console now.** `SubscriptionApi` built its Stripe return URLs against the
  `seller-ui` service domain, so an operator who upgraded from the console was handed back to a
  different application after paying. The constant is `console-ui`, which already serves both outcome
  routes.
- **A 404 from `current` is a state, not a failure.** Billing learns about a store from
  `StoreCreatedEvent` through the outbox, so a store created seconds ago reaches the console before its
  subscription row exists. Both surfaces render that as "not caught up yet" rather than an error.
- **Absent-means-unlimited is a rule about a *plan*, not a subscription.** A row with no `planCode`
  carries an empty entitlement map, and reading it through the catalogue's rule rendered every
  allowance as "Unlimited" — telling a merchant mid-provisioning that they had unlimited everything.
  Caught in QA on a real store. No plan now means no allowances to report.
- **Still open:** entitlements are *ceilings* with no usage behind them — see "Billing — entitlements
  are ceilings with no usage behind them" below. Immediate cancellation is a super-admin operation and
  is never sent from the console.

## Console — the action vocabulary was copied into five stylesheets

- **What happened:** `.primary-action` and `.secondary-action` were redeclared in order-details,
  create-store, store-management, first-run, orders and settings-card. They had already drifted: one
  pinned a `block-size` where the others used padding, one dropped `white-space: nowrap`, one omitted
  `:not(:disabled)` so a disabled button still lit up on hover. The billing page declared *neither*,
  so its buttons rendered as unstyled text and red text — which is what "the design is so bad" was.
- **Decision:** one definition in `styles.css`, beside `.popover`, which is global for exactly the
  same reason: these classes are used by components that cannot see each other's encapsulated styles.
  The five copies are deleted. The set gained `.ghost-action` (tertiary, no chrome until touched) and
  `.danger-action` (danger hue in the *text*, not a red slab that competes with the primary action),
  plus one disabled treatment covering `:disabled`, `.disabled` and `[aria-busy]`.
- **Why not a component:** these are applied to `<button>`, `<a>` and — in first-run's trial gate — a
  `<span>`. A directive or component would have to reproduce all three.

## Console — money and dates were formatted against the wrong locale

- **What happened:** the billing page used Angular's `CurrencyPipe` and bare
  `Intl.DateTimeFormat(activeLang)`. `CurrencyPipe` reads `LOCALE_ID`, fixed at bootstrap and always
  `en-US`, so switching the console to Arabic left the money in English. `activeLang` is `ar`, but the
  console's locale for it is `ar-EG` — formatting on the language alone produced Latin digits and
  `2026/09/03` on an otherwise Arabic page.
- **Decision:** `TranslocoLocaleService` for dates and counts, and the existing `Money` service for
  amounts. `Money` was written for exactly this and already knew the answer.
- **Currency is an ISO code on account surfaces.** `symbol` renders USD as `US$` and GBP as `UK£` in
  Arabic — Latin script stranded in a right-to-left line — and `narrowSymbol` is identical. Only
  `code` and `name` localize; `name` is too long for a metric. `code` is also already this product's
  currency vocabulary: the merchant picks it from a select reading `SAR · Saudi Riyal`. Storefront
  money keeps `symbol`; `Money.account()` is the account-level form.
- **Plan names are translated on `code`, not `displayName`.** Billing authors one English
  `displayName` for every locale, so an Arabic console read "This store is on Free". `PlanLabel`
  translates the stable code and falls back to the server's name for a plan the console has no word
  for — so a plan added at runtime renders in English rather than throwing against the strict
  missing-key handler.

## Billing — the checkout return URL pointed at a path neither console served

- **What happened:** `SubscriptionApi` builds Stripe's return URLs from
  `SUCCESS_PATH = "/public/subscription/success"`. console-ui mounted those routes at
  `subscription/success` — no `public` — so a customer who had just paid landed on the not-found page.
- **seller-ui has the same gap.** Its outcome routes live in `src/app/public/subscription/`, a *source
  folder*, mounted at `''` — so they serve `/subscription/success` too. The `/public/` prefix in the
  server constant was reading a directory name as a URL segment, and has never matched either app.
- **Decision:** the console now serves `public/subscription/success` and `.../fail`, which makes the
  existing server constants correct without a backend change. The bare `subscription/*` mounts are
  gone, which also clears the collision with the billing page at `/subscription`.
- **On return, the console invalidates rather than trusts.** The outcome page still reads nothing from
  the URL — anyone can type it — but it does call `BillingFacade.refresh()`, because after a checkout
  the cached subscription is stale by definition. The URL says "go and look again"; the server still
  decides what is true. Both CTAs now lead to billing rather than the dashboard.

## Console — the section rail is shared, not store management's

- **What happened:** `SettingsNav` was written page-local, with a comment arguing that "nothing else in
  the console has that shape". Billing then grew exactly that shape.
- **Decision:** promoted to `@shared/ui/section-nav` as `SectionNav`, parameterised by `basePath` and
  `heading`, with the footer projected — store management's storefront-builder entry was the only
  genuinely page-specific part. Both pages now bind their sections to the router the same way, so a
  tab is linkable and survives a reload on either.
- **Billing's sections are `plan` and `invoices`**, at `/subscription/:section` with `''` redirecting
  to `plan`. Splitting them fixed a layout problem as much as a navigation one: the two panels had
  been sharing a row, and whichever one remained was stranded at half width.

## Catalogue — no multi-location inventory

- **Screen:** `/products`, from `console-template/Inventory.dc.html` — the "All locations" switcher,
  the per-location cards, and the on-hand / reserved / available split on every row.
- **What the UI needs:** stock held per warehouse or per region, so a seller with two locations can
  see where their stock is and move it.
- **What is missing:** there is no location entity anywhere in the catalog pod. `InventoryEntity`
  carries `region` and `regionVariant` as **free text**, with no repository, no endpoint and no
  constraint — nothing joins them to a place. Zero hits pod-wide for warehouse, location or
  fulfilment centre.
- **Why it is required:** the whole left-hand column of the inventory design rests on it, and a
  merchant trading from two warehouses cannot answer "where is this stock" at all.
- **Expected contract:** a `Location` entity per store, `GET/POST /private/locations`, and inventory
  keyed on `(product, location)` rather than on product alone.
- **Placeholder:** none in code. The blocks are removed rather than disabled — see the note under
  "Catalogue — the inventory KPI row" below.

## Catalogue — no reorder point and no low-stock threshold

- **Screen:** `/products` — the "12 SKUs are at or below their reorder point" banner, the "reorder at
  N" caption under each stock bar, and the Low stock KPI tile.
- **What the UI needs:** a per-product replenishment level, and a list of what has fallen below it.
- **What is missing:** no such field. `InventoryEntity.productQuantityOrderMin` and
  `productQuantityOrderMax` look like candidates and are not: they are **per-order purchase limits**
  — the smallest and largest quantity a shopper may put in one basket — read by the cart, not by
  replenishment. Reusing them would be a fixture standing in for a real answer.
- **Why it is required:** without it there is no low-stock signal at all, so a seller learns they
  are out of something when a shopper cannot buy it.
- **Expected contract:** `reorderPoint` on the inventory record, and a `lowStock=true` filter on
  `ProductCriteria`.
- **Placeholder:** `TODO(lessons.md)` in `features/products/products.ts`.

## Catalogue — no purchase orders and no supplier

- **Screen:** `/products` — the "Incoming purchase orders" panel and its Create purchase orders
  action; `/products/:id` — the Supplier field in Merchandising.
- **What the UI needs:** inbound stock the seller has ordered but not received, and who they order
  it from.
- **What is missing:** zero hits across the catalog pod for purchase order, supplier or vendor.
  Neither concept is modelled anywhere on the platform.
- **Why it is required:** it is half of what an inventory screen is for. Without it the page can
  only describe the present.
- **Expected contract:** a `PurchaseOrder` aggregate with a supplier, expected date, lines and a
  received-quantity per line, plus `GET /private/purchase-orders`.
- **Placeholder:** none — the panel and the field are removed.

## Catalogue — no stock-movement ledger

- **Screen:** `/products` — the "Recent stock movements · last 48 hours" panel.
- **What the UI needs:** an append-only record of every quantity change, with its reason and who
  made it.
- **What is missing:** quantity is a **mutable column** on the inventory row. `PATCH
  /private/product/{id}` overwrites it and keeps no history. The only movement-shaped surface on the
  platform is checkout's reserve / commit / release trio, which is service-to-service and not
  exposed to the console.
- **Why it is required:** without it "why is this figure wrong" is unanswerable, which is the
  question an inventory discrepancy always raises.
- **Expected contract:** a `StockMovement` record — product, delta, reason, actor, timestamp —
  written by every quantity change, with `GET /private/products/{id}/movements`.
- **Placeholder:** none — the panel is removed.

## Catalogue — no product cost, so no inventory valuation and no margin

- **Screen:** `/products` — the Value column and the Inventory value KPI tile; `/products/:id` —
  Unit cost and the "Margin 39.5% · $51.00 per unit" line derived from it.
- **What the UI needs:** what the seller paid for a unit.
- **What is missing:** a product carries exactly one money field, `price`, and it is the **sale**
  price. There is no cost column on the product, the inventory row or the price record.
- **Why it is required:** three separate blocks of the design compute from it, and margin is the
  number a merchant actually manages.
- **Expected contract:** `cost` on the inventory record, in the store's currency.
- **Placeholder:** none — all three blocks are removed.

## Catalogue — the inventory KPI row is removed rather than reported unavailable

- **Screen:** `/products` — the four tiles across the top of `Inventory.dc.html`.
- **What happened:** Module 3 established a pattern for an unbacked metric — an em dash under a
  "Not available yet" flag. It is the right answer for a row where *some* tiles are real. Here all
  four are unbacked at once: Stock on hand and Inventory value need sums the platform never
  computes, Low stock needs a reorder point that does not exist, and Out of stock needs a
  `quantity = 0` filter `ProductCriteria` does not offer.
- **Decision:** the row goes. Four em dashes in a row is not an honest page, it is a decoration —
  it occupies the most prominent band of the screen to say nothing four times. The one real figure
  the row carried, the total SKU count, is in the page header's context line and in the pagination
  footer, where it is next to the thing it counts.

## Catalogue — no CSV import or export, and no bulk operations

- **Screen:** `/products` — Import CSV, Stock count, Export list, the row checkboxes and the bulk
  action bar they reveal.
- **What the UI needs:** to change many products at once — a price rise across a brand, a visibility
  change across a category — and to move a catalogue in and out of a spreadsheet.
- **What is missing:** no import endpoint, no export endpoint, and no bulk write of any kind. Every
  catalog mutation is one record per request.
- **Why it is required:** a store with 1,482 SKUs cannot be operated one `PATCH` at a time, which is
  the size the design itself assumes.
- **Expected contract:** `POST /private/products/import` taking a CSV and answering a job id, a
  matching export, and `PATCH /private/products` taking a list of ids and one change.
- **Placeholder:** none — the controls are removed.

## Catalogue — no product tags and no collections

- **Screen:** `/products/:id`, the Merchandising block — the Collections list and the tag input.
- **What the UI needs:** free-form labels on a product, and curated sets that are not categories.
- **What is missing:** no tag table, no collection entity, no field on the product. **Product groups
  are a different concept and not a substitute**: a group is a named, code-addressed membership set
  with its own per-language name, edited on `/catalogue`, whereas a tag is a label anyone can type.
- **Why it is required:** collections drive storefront merchandising strips that categories cannot
  express, because a collection cuts across the tree.
- **Expected contract:** `tags: List<String>` on the product, and a `Collection` entity with an
  ordered membership.
- **Placeholder:** none — both blocks are removed.

## Catalogue — no barcode or GTIN on a product

- **Screen:** `/products/:id`, step 1 — the "Barcode (GTIN)" field beside the SKU.
- **What the UI needs:** the manufacturer's global identifier, so a warehouse scanner and a
  marketplace feed can both find the product.
- **What is missing:** no such field on `Product`, `ProductEntity`, `ProductDefinition` or
  `InventoryEntity`. `identifier` on the definition is unused and unpopulated, and `refSku` is an
  internal cross-reference, not a GTIN.
- **Why it is required:** every marketplace feed and every scanner integration is keyed on it.
- **Expected contract:** `gtin` on the product, validated as GTIN-8/12/13/14.
- **Placeholder:** none — the field is removed.

## Catalogue — no compare-at price and no quantity-break tiers

- **Screen:** `/products/:id`, step 3 — "Compare at" and the whole Bulk pricing tiers table.
- **What the UI needs:** a struck-through was-price, and automatic discounts at quantity
  breakpoints.
- **What is missing:** `ProductPriceApi` allows several price records per inventory row, but the
  price record carries **neither semantic** — no "this is the original price" flag and no minimum
  quantity. `ReadableProduct.originalPrice` and `discounted` exist on the read DTO and are populated
  from a promotion, which nothing in the console can create.
- **Why it is required:** both are ordinary retail mechanics, and the B2B wholesale case the design
  assumes is unserviceable without tiers.
- **Expected contract:** `type` on the price record (`REGULAR` / `COMPARE_AT`) and `minQuantity` for
  a tier, plus a private CRUD for prices.
- **Placeholder:** none — both blocks are removed.

## Catalogue — no tax class and no per-product currency

- **Screen:** `/products/:id`, step 3 — the "Tax class" and "Currency" selects.
- **What the UI needs:** which tax rate applies to this product, and which currency its price is in.
- **What is missing:** no tax-class field or table in the catalog pod, and price is a bare
  `BigDecimal` with the **store's** currency implied. A product cannot be priced in a currency other
  than its store's, and no rate can be varied per product.
- **Why it is required:** a store selling both books and electronics in a VAT jurisdiction needs two
  rates, and cannot express them.
- **Expected contract:** a `TaxClass` per store and a `taxClass` reference on the product.
- **Placeholder:** none — both selects are removed. The store's currency is shown beside the price
  as a read-only prefix, so the figure is not ambiguous.

## Catalogue — a product type carries no attribute definitions

- **Screen:** `/catalogue`, the Product types tab — the whole right-hand panel of
  `Catalog (standalone).html`: attribute name, kind, Required / Optional, Variant / Shared, and the
  "Used by categories" chip row.
- **What the UI needs:** a type that says which attributes a product of that type must carry, and
  which of them generate variants.
- **What is missing:** `ProductAttributeOptionApi` and `ProductPropertySetApi` are both mapped, and
  neither is reachable from a type. `ProductTypeEntity` is `{id, code, allowAddToCart, visible,
  descriptions}` — there is no join from a type to an attribute set, and no relation from a type to
  a category either.
- **Why it is required:** it is the entire purpose of a product type. Without it a type is a label,
  which is what the console now honestly presents it as.
- **Expected contract:** `attributes: List<ProductAttributeDefinition>` on the type, each with a
  name, a kind, a required flag and a variant-defining flag; and `GET /private/product/type/{id}` to
  return them.
- **Placeholder:** the tab shows a notice saying so, backed by
  `catalogue.types.noAttributes`.

## Catalogue — a category has no banner image and a brand has no logo or publish flag

- **Screen:** `/catalogue` — the category editor's "Banner image · 1600 × 400 recommended" well, and
  the brand editor's logo well and "Publish brand page" toggle.
- **What the UI needs:** artwork for a category landing page and a brand page, and control over
  whether the brand page exists.
- **What is missing:** `ReadableCategory` has no image field and no upload endpoint.
  `ReadableManufacturer` is `{id, code, order, descriptions}` — no image, no publish flag, and no
  product count either.
- **Why it is required:** a storefront category page with no banner and a brand page with no logo
  are visibly unfinished, and the seller has no way to fix it.
- **Expected contract:** the same shape the merchant pod already has for a store logo —
  `POST …/private/category/{id}/image` multipart, and the equivalent for a manufacturer — plus a
  `published` boolean on the manufacturer.
- **Placeholder:** both wells are removed. The brand card shows the brand's initials, which is
  visibly a stand-in rather than a broken image.

## Catalogue — no SKU generation

- **Screen:** `/products/:id`, step 1 — the "Available — generated from category" hint under the SKU
  field, and the "SKU prefix ACM-ELC · inherits Standard tax rate" line in the category picker.
- **What the UI needs:** a SKU proposed from the product's category, so a catalogue's codes stay
  consistent without the seller inventing a scheme.
- **What is missing:** nothing derives a SKU. `GET /private/product/unique?code=` answers only
  whether a given code is taken — it cannot suggest a free one, and there is no SKU-prefix field on
  a category.
- **Why it is required:** the design presents the SKU as assisted, and it is entirely manual.
- **Expected contract:** `skuPrefix` on a category, and `GET /private/product/next-sku?categoryId=`
  answering the next free code under that prefix.
- **Placeholder:** the field states that the code is the seller's own, and the uniqueness check
  reports rather than proposes.

## Catalogue — a product's default image cannot be changed after upload

- **Screen:** `/products/:id`, step 2 — "First image is the storefront thumbnail", and the MAIN badge
  the design puts on a hoverable image.
- **What the UI needs:** to pick which of a product's images is the storefront thumbnail, at any
  time.
- **What is missing:** the flag can only be set **at upload**. `POST …/product/{id}/image` takes
  `?defaultImage=`, and `PATCH …/product/{id}/image/{imageId}` sets `sortOrder` and nothing else —
  no endpoint re-designates an existing image. Worse, `ProductImageApi.buildContentImages` sets the
  flag on the new image without clearing it on the old one, so uploading with `defaultImage=true`
  onto a product that already has a default leaves **two** images flagged.
- **Why it is required:** the first photograph a seller happens to upload is not usually the one
  they want on the category grid, and today they must delete and re-upload the whole gallery to
  change it.
- **Expected contract:** `PATCH …/product/{id}/image/{imageId}?defaultImage=true`, clearing the flag
  on every sibling in the same transaction.
- **Placeholder:** the Media step marks which image is the thumbnail and does not offer to change
  it. `product-image.service.ts` carries the note at the call site.

## Catalogue — two seller-core calls have never worked

- **Screen:** `/products/:id`, step 4 (Organize) and step 2 (Media) — and, in seller-ui, the
  Category and Images sub-tabs of its product form.
- **What happened:** two paths in `seller-core/catalog` do not match any mapping, and both were
  found by porting them.
  - `ProductService.addProductToCategory()` builds
    `/api/v1/private/product/${productId}/category/${categoryId}}` — with a **literal trailing
    brace**. Every attempt to put a product in a category from the old console has 404'd.
  - `ProductImageService.createImage()` posts to `/api/v1/private/product/{id}/images`, **plural**.
    `ProductImageApi` maps only the singular `/image`. Every image upload from the old console has
    404'd.
- **Decision:** both are fixed in the port rather than in seller-ui, per the standing convention
  that a module does not modify the old console. `addToCategory` is what makes the Organize step's
  category diffing possible at all, which makes the category round-trip the sharpest test in this
  module. `createImage` is dropped and replaced by an upload that targets the mapping that exists.
- **Same class of finding as** Orders' Refund and Capture (Module 4) and `ManagerStoreService.create()`
  (Module 2): a client method whose endpoint has never existed, kept alive by a UI nobody exercised.

## Catalogue — `PATCH /api/v1/private/product/{id}` is mapped twice

- **Screen:** `/products` — the inline edit of price, quantity and availability.
- **What is wrong:** `ProductApi` declares two `@PatchMapping` handlers on the same path. One takes a
  `@Valid @RequestBody LightPersistableProduct`; the other takes `?order=` and changes the product's
  sort order. They differ only by `produces`, so which one answers depends on the caller's `Accept`
  header.
- **Why it matters:** it is a latent ambiguity rather than a live fault — the console sends a JSON
  body and gets the first — but a client that sets `Accept: */*` and passes `?order=` would get
  whichever Spring resolves first, and the two do entirely different things.
- **Expected fix:** give product ordering its own path, e.g. `PATCH …/product/{id}/order`.
- **Placeholder:** the console calls only the body form; the `?order=` form is not ported. Noted in
  `api/catalog/product.service.ts`.

## Catalogue — a console gap, not a backend one: variants, options and attributes

Recorded here for completeness because it is the reverse of every entry above, and belongs in the
module's plan rather than in this file's remit. `ProductVariantApi`, `ProductVariationApi`,
`ProductVariantGroupApi`, `ProductAttributeOptionApi`, `ProductPropertySetApi`,
`ProductInventoryApi`, `ProductPriceApi`, `ExternalProductApi` and `ExternalProductReservationApi`
are all **fully mapped on the backend** and have no seller-core client and no UI — seller-ui's menu
has its Options group commented out. Module 6 ships one product with no variants, matching what
seller-ui writes. Nothing is missing from the platform here; what is missing is a console for it.

## Catalogue — the category tree's "move to top level" is an undocumented `-1`

- **Screen:** `/catalogue`, the Categories tab — the row's "move out" button, and a drag that
  promotes a child to the root.
- **What is wrong:** `PUT …/category/{child}/move/{parent}` is the only way to re-parent, and there
  is no way to clear a parent through `PUT …/category/{id}` — the update maps a parent it is given
  and ignores its absence. Promoting a category to the top level works **only** by passing `-1` as
  the parent, which `CategoryFacadeImpl.move` special-cases into `addChild(null, category)`.
- **Why it matters:** it is real, working behaviour with nothing naming it. seller-ui never found
  it: its tree only nests, so a category dragged into another could never be got back out.
- **Expected fix:** `DELETE …/category/{id}/parent`, or at minimum an OpenAPI note on the `-1`.
- **Placeholder:** named as `ROOT_PARENT` in `api/catalog/category.service.ts` rather than left as a
  magic number at the call site.

## Catalogue — the private product list is stripped of everything a list needs

- **Screen:** `/products`, and both product pickers (`/catalogue` Groups, and the product form's
  related products).
- **What the UI needs:** a page of products carrying, per row, a name, its categories, its brand and
  a thumbnail — the columns `Inventory.dc.html` draws and seller-ui's own list shows.
- **What is missing:** the *authorised* list endpoints answer none of it.
  `GET /api/v2/private/base-products` returns `description: null`, `categories: []`,
  `manufacturer: null` and `image: null` on every row — verified on the running stack, in both
  languages. `GET /api/v2/private/tiny-products` is thinner still. A row from either has an id, a
  SKU, a price, a quantity and a flag, and **no name**.
- **Why it is required:** a product list that can only show SKUs is not a product list, and a picker
  whose results have no names cannot be used to pick anything.
- **What the console does instead:** reads `GET /api/v2/products`, which is **public** — no
  `@PreAuthorize`. It runs the identical query: `ProductFacadeV2Impl.getProductListsByCriteria` and
  `getBaseProductListsByCriteria` both delegate to the same `listProducts(...)` over the same
  `productService.findAll(criteria, store)`, and differ only in which mapper they pass. Checked
  before making the swap: it does **not** hide unpublished products, so a seller still sees their
  own drafts.
- **Expected contract:** `/private/base-products` populated by `readableProductMapper` — or, better,
  a `?fields=` on it, since the thin mapper is presumably there for the storefront's sake.
- **The gap that remains:** the console reads its own catalogue through an unauthenticated endpoint.
  It is still store-scoped by the `store` parameter and the data is the storefront's own public
  catalogue, but the asymmetry is real and should close when the private list is populated.

## Catalogue — the product-name filter is accepted and ignored

- **Screen:** `/products` — the search box the design puts at the top of the stock table; and both
  product pickers.
- **What the UI needs:** to narrow a list of 1,482 SKUs by typing part of a product's name.
- **What is missing:** `ProductCriteria.productName` exists, Spring binds it, and
  **`getProductName()` is never called anywhere in the pod.** `ProductRepository`'s predicate builder
  reads `sku` (as `LIKE %sku%`), `manufacturerId`, `categoryIds` and `available`, and nothing else.
  Verified twice: by reading the predicate builder, and against the running stack, where
  `?productName=scarf` returns all 45 products in the store.
- **Why it is required:** SKU is an internal code. A seller looking for "the blue scarf" knows its
  name, not its code, and this is the single most-used control on the screen.
- **Why it is worse than a missing filter:** a dead filter *looks* like it worked. The list re-renders,
  the spinner runs, and the operator concludes their catalogue contains every product they searched
  for.
- **Expected contract:** join `ProductDescription` and add a `LIKE %productName%` on `name`, in the
  request's language.
- **Placeholder:** the console does not offer a name search at all — only SKU, category and brand,
  which are the three that work. The box is labelled "Search by SKU" so it cannot be mistaken for one.

## Catalogue — two DTOs answer `null` where every sibling answers `[]`

- **What happened:** the catalogue page went down on first load against the live stack with
  "Cannot read properties of null (reading '0')".
- **Cause:** `ReadableManufacturer.descriptions` and `ReadableProductType.descriptions` are declared
  in Java with **no initialiser**, while `ReadableCategory`, `ReadableProductGroup` and
  `ReadableProductDefinition` all declare theirs `= new ArrayList<>()`. So a manufacturer answers
  `"descriptions": null` while a category in the same response answers `"descriptions": []`. The
  port had typed all five as required, reasoning from the DTOs that happened to be read first.
- **Decision:** the two are typed `readonly descriptions?: readonly …[] | null` and their mappers
  narrow with `?? []`. The other three stay required, because they genuinely are.
- **The lesson, which is about porting and not about the catalogue:** "the Java field is a `List`"
  does not mean "the wire value is an array". The initialiser is the test, it has to be checked per
  DTO, and one observed payload is not evidence — `ReadableProductType.descriptions` arrives as `[]`
  on the seeded store and is nullable by declaration all the same.

## Catalogue — a brand persists only its name and its description

- **Screen:** `/catalogue`, the Brands tab — the Slug field with its `/b/` prefix, and Sort order.
- **What the UI needs:** a storefront URL for a brand page, and control over where a brand sits in
  the storefront's brand list.
- **What is missing:** both, and neither fails loudly.
  - **No slug.** `manufacturer_description` has **no `sef_url` column** — the table is `description`,
    `name`, `title`, `manufacturers_url`, `url_clicked`, `language_code`. Every sibling description
    table has one; this one does not. `ManufacturerDescription` maps `MANUFACTURERS_URL` to a `url`
    field that is a *click-tracked outbound link*, not a storefront path.
  - **No sort order.** `PersistableManufacturerPopulator.populate` sets `storeMerchantId`, `code`,
    and per description `description`, `name` and `languageCode`. It never calls `setOrder`, so the
    `order` on `PersistableManufacturer` is bound by Jackson and then dropped on the floor.
  - The same populator also ignores `title`, `highlights`, `metaDescription` and `keyWords`, all of
    which `NamedEntity` carries. A brand is a name and a description, in each language.
- **Why it matters:** these are not absent controls, they are controls that accept input and throw it
  away. An operator sets a brand's order, saves, sees a success toast, reloads, and finds it back at
  zero — which reads as a bug in the console rather than a gap in the platform.
- **Expected contract:** `sef_url` on `manufacturer_description` and a populator that reads `order`
  and the remaining `NamedEntity` fields — the category populator's `buildDescription` is the model,
  and it maps six of them.
- **Placeholder:** both fields are removed. The Brands editor states that a brand is a name and a
  description here, so the absence is explained rather than merely absent.

## Catalogue — a category's meta keywords are dropped by its populator

- **Screen:** `/catalogue`, the Categories tab — not rendered, and this entry is why.
- **What is missing:** `category_description.meta_keywords varchar(255)` exists in the schema, and
  `PersistableCategoryPopulator.buildDescription` sets `categoryHighlight`, `description`, `name`,
  `metatagDescription`, `metatagTitle`, `seUrl` and `languageCode` — **not** `metatagKeywords`. A
  value sent for it is accepted and never stored.
- **The same defect, a third time.** Module 5 recorded it for `ContentDescription.keyWords`, where
  `ContentFacadeImpl.buildDescriptions` has the identical omission. Product descriptions *do* persist
  keywords (`PersistableProductDefinitionMapper` calls `setMetatagKeywords`), and so do product
  groups. So the field works on two of the four entities that declare it, which is the worst of both
  worlds — there is no rule to remember.
- **Expected contract:** one shared description populator, or at minimum `setMetatagKeywords` in
  `buildDescription`.
- **Placeholder:** the Categories editor does not offer a keywords field. The product form does,
  because there it is real.

## Catalogue — `PUT /private/category/{id}` fails for every caller

- **Screen:** `/catalogue`, the Categories tab — *Save category*, and anything built on it.
- **What happens:** every update returns 500. Verified against the running stack with four different
  request bodies (with and without `parent`, with and without description ids); the body makes no
  difference.
- **Root cause, from `catalog.log`:**
  ```
  java.lang.UnsupportedOperationException
    at java.util.ImmutableCollections.uoe
    at java.util.ImmutableCollections$AbstractImmutableCollection.clear
    at org.hibernate.type.CollectionType.replaceElements
    at org.hibernate.event.internal.DefaultMergeEventListener.entityIsPersistent
  ```
  `CategoryFacadeImpl.saveCategory` builds the children list with `Stream.toList()`, which is
  **immutable**, and assigns it to the managed entity:
  ```java
  List<Category> saveNow = children.stream()
          .filter(c -> c.getId() != null && c.getId() > 0)
          .toList();          // immutable
  category.setCategories(saveNow);
  categoryService.saveOrUpdate(category);   // Hibernate merge -> replaceElements -> clear() -> boom
  ```
  It fails on a leaf as readily as on a branch: the list is immutable whether or not it is empty.
- **Who it affects:** everyone. seller-ui calls the same endpoint from its category form, so category
  editing has been broken there too — this is not something the port introduced.
- **Expected fix:** one line — `new ArrayList<>(children.stream()...toList())`, or collect to a
  mutable list. `Collectors.toList()` or `.collect(Collectors.toCollection(ArrayList::new))`.
- **What the console does:** nothing special. The call is correct and the failure is surfaced through
  the normal error toast, because the console cannot make a broken endpoint work and pretending
  otherwise would be worse. `PATCH …/category/{id}/visible`, `PUT …/category/{child}/move/{parent}`,
  `POST /private/category` and `DELETE` are all unaffected and all verified working — so the tab is
  fully usable apart from saving edits to an existing category.

## Catalogue — sibling order is not expressible, twice over

- **Screen:** `/catalogue`, the Categories tab — "move up" / "move down" on a row, and dragging onto
  a row's top or bottom edge.
- **What the UI needs:** to put one category before another among its siblings.
- **What is missing — two independent blockers**, either of which alone would be enough:
  1. **Ordering is a field, and the only endpoint that writes it is broken.** `sortOrder` lives on
     the category, so "move up" is a save of a new number on two records — and
     `PUT /private/category/{id}` 500s for every caller (see the entry above). There is no
     reorder endpoint.
  2. **The hierarchy does not come back in `sortOrder` order anyway.**
     `GET /private/category-hierarchy` answers `MEN`'s children as
     `[MEN_TOPS:0, MEN_SHOES:2, MEN_BOTTOMS:1]` — the numbers are right there in the payload and the
     list is not sorted by them. `CategoryFacadeImpl.hierarchyList` builds each parent's `children`
     in repository order and never sorts. So even with a working write, the tree would redraw in the
     same order it had before and the operator would see nothing happen.
- **Expected contract:** sort `children` by `sortOrder` in the hierarchy populator, and either fix
  the update or add `PUT …/category/{id}/order`.
- **Placeholder:** sibling reordering is **removed** — the row's up/down buttons, their `Alt+Arrow`
  shortcuts and their menu items, and the tree's before/after drop zones. Dropping onto a row now
  always nests, which is the one rearrangement the platform can actually perform. Nesting, promoting
  out of a parent, visibility, add and delete are all real and all verified.

## Catalogue — the console-side lessons from the hardening pass

Not backend gaps. Recorded because each was a defect the module shipped with and each has a rule
behind it worth keeping.

- **A `<select>` bound with `[value]` loses its value when its options arrive later.** The Organize
  step's Brand and Type selects read "No brand" for a product that had one: the value binding is
  applied before the `@for` has produced any `<option>`, so the browser discards it. `formControlName`
  is the fix — `SelectControlValueAccessor` registers each option as it appears and re-applies. Any
  select whose options come from a request has this bug unless it is bound through the form.
- **A form nobody fills renders empty next to data.** The catalogue's editors were loaded from
  `select()`, `cancelEdit()` and after a write — but not on the first response, where there is no
  user action to hang a call on. An `effect` keyed on the selected record covers all of them, and
  the first load is just another case rather than a special one.
- **Focus and selection are different things in a tree.** `focusRow` emitted `selectedIdChange`, so
  arrowing down the category tree loaded each category into the editor in turn and discarded
  whatever was half-typed there. The WAI-ARIA tree pattern separates them for exactly this reason:
  you must be able to move a node without opening it.
- **`queueMicrotask` is not "after render" in a zone app.** `app.config.ts` uses
  `provideZoneChangeDetection({eventCoalescing: true})`, so change detection can run *after* a
  microtask — post-render focus has to use `afterNextRender(fn, {injector})`. `DatePicker` documents
  the same trap; the tree had to relearn it.
- **A "settled" check cannot key on a `busy()` input.** The tree's focus-restoration effect ran in
  the same tick as its own emit, before the page had started the request, and announced the node's
  *old* position — the one thing the move had changed. Keying on the identity of the node list is
  deterministic; a flag that has to propagate through an input is not. Caught by a spec, not by the
  browser, because the real round trip is fast enough to hide it.
- **Tailwind's Preflight removes list markers.** Right for app chrome built out of `<ul>`, wrong for
  a rich text editor holding a seller's prose — the bullet button appeared to do nothing. Content
  that is *the user's document* needs its list styles restored explicitly.
- **`.popover` already animates.** Adding `animate.enter` to an element that carries it runs two
  animations over the same travel. The global class owns the entry; a component adds only the leave,
  which `.popover` does not provide.
- **Bound `contenteditable` and a `role="toolbar"` container both trip
  `interactive-supports-focus`.** Both are correct — a contenteditable is focusable by definition,
  and a toolbar must not be focusable because its buttons are — so both carry an inline disable with
  the reason, rather than the rule being switched off.
- **A `computed` cannot see a plain field.** `RichText.editableDir` derived from `wrapperDir`, which
  was an ordinary property set by `render()` — so the editable stayed on `dir="auto"` after loading
  an Arabic document. `auto` guesses from the first strong character, which is right often enough to
  hide the bug and wrong for a description that opens with a Latin brand name or a digit. Anything a
  `computed` reads has to be a signal; the compiler will not say so.
- **Compare like with like when checking whether a document is untouched.** The editor's pristine
  short-circuit compared the sanitised whole document against the sanitised whole document, but
  sanitising drops whitespace-only nodes *between top-level blocks* and leaves them alone one level
  down. Inside a `<div dir="rtl">` wrapper the two sides could never agree, so every Arabic
  description came back re-serialised and showed as a diff nobody made. The comparison now happens
  inside the wrapper, where both sides have had the same rules applied.
- **`disable()` cannot null an error that has not arrived yet.** Every existing category, brand,
  product type, group and product carried a red "already taken" marker against its own code — the
  one field the form does not allow editing. The form is filled while the control is still enabled,
  so the async check starts; the facade disables the control a tick later, and Angular's `disable()`
  nulls the errors *present at that moment*; the server then truthfully answers "yes, a record has
  that code" and the result lands on a disabled control, where nothing will ever clear it. An async
  validator that can outlive its control's enabled state has to check `control.enabled` at the point
  it decides to report, not only at the point it starts.

## Catalogue — the second QA pass, and why the first one missed it

- **A spec that asserts presence proves nothing about behaviour.** `expect(querySelector(
  'app-export-button')).not.toBeNull()` passes whether or not the button is the same height as the
  one beside it, wired to anything, or visible. Nine of the thirteen defects found in review were
  under assertions that were green. Where a person would look at something, the test has to measure
  it; where a person would use something, the test has to drive it.
- **A list endpoint can answer with a hollow object.**
  `GET /private/products/groups` returns `products: []` for every group whatever they contain; only
  `GET /private/products/groups/{code}` populates it. The Groups tab built its member lists from the
  list response and showed every group as empty — which looked plausible, because empty is a
  perfectly ordinary answer. This is the second instance of the same trap after `base-products`
  answering `description: null`. **When a list and a by-id read return the same DTO, verify the list
  actually fills it** rather than assuming the shape is the contract.
- **The product-name filter is accepted and ignored, everywhere.** `ProductRepository.findAll` builds
  its `Specification` from store, language, `available`, `sku`, `manufacturerId` and `categoryIds`.
  `ProductCriteria.productName` and `Criteria.name` are bound by Spring and read by no one. So
  seller-ui's product autocomplete, which passes `name=` to `/api/v2/private/tiny-products`, **has
  never searched** — it shows the first twenty products in the store whatever you type, and looks
  like a working control until you want the twenty-first. The console filters on the client instead
  and says in the field hint that it is doing so.
- **An `rxResource` keyed on signals that settle late runs once per signal.**
  `params: () => ({id: this.productId(), store: this.shell.currentStoreId()})` ran three times on
  one page load — no id and no store, then the route effect, then the store directory — each a
  `forkJoin` of six requests, two rounds cancelled mid-flight. Eighteen requests to answer six
  questions. `params` returning `undefined` is the "not ready" signal; a resource that depends on
  more than one asynchronous input needs a gate, not a key.
- **A root-provided facade with a resource starts fetching when something injects it.** The product
  form held `ProductsFacade` purely to call `invalidate()` after a save, and paid for a page of the
  products list on every visit. What the form actually needed was a stamp to bump. **Inject the
  smallest thing that does the job**, because injection is construction and construction is a
  request.
- **Deleting a margin because a host rule replaces it — check the rule reaches.**
  `:host { display: contents }` hands the shell's `.workspace` gap to a page's children, but it stops
  at `app-busy-overlay`, which is a real box with its own formatting context. The previous pass
  deleted `.stacked`, `.box-panel` and `.copy-panel` on the strength of the host rule and left the
  four wizard steps with no gap between any two panels. The classes stayed in the templates, so
  nothing failed — the CSS simply matched nothing.
- **`appearance: none` cannot theme a dropdown.** It restyles the closed control; the open list is
  drawn by the operating system and no CSS reaches it. In a console with two dark themes that meant
  every brand, type and unit picker opened a white sheet. A listbox behind a button is more code and
  is the only thing that actually works.
- **A native checkbox reads as checked when it is not.** `accent-color` tints the checked state and
  leaves the unchecked one to the platform, which against a dark theme is a solid dark square —
  indistinguishable from selected. Controls in a themed surface have to be drawn, not tinted.
- **Never `kill` a process the stack manager is supervising.** `run-lcl.sh` records a pid per
  service; killing `console-ui` and starting an unsupervised `ng serve` in its place left the gateway
  routing to an instance it no longer knew, which then read as "the app does not compile". The stack
  has `restart console-ui` for exactly this, and it only works while the supervisor is alive.
- **The weight and dimension units were an invented uppercase subset.** The server enums are
  `WeightUnitOfMeasure {g, kg, l, lb, T}` and `DimensionUnitOfMeasure {cm, cu, ft, in, m}` — lowercase
  bar the ton. This console declared `['KG','LB']` and `['CM','IN']`, so the value never matched what
  the server sent, a save would have posted a constant the enum does not have, and three of five
  units in each set were unreachable. The native `<select>` hid all of it by falling back to its
  first option; the themed one rendered blank, which is how it was finally seen. **A `<select>` that
  cannot match its value shows something plausible instead of nothing** — that is the third defect in
  this module that a native select concealed.

## The design pass — encapsulation, and three more things a native control hid

- **A page cannot style anything inside a child component's template.** Angular scopes styles by the
  *defining* component's attribute, so `.step-body > *` reached each step's host and stopped there —
  the `[formGroup]` wrapper inside the step carries the step's attribute, not the page's. The rule
  computed `gap: 16px` on a container holding one child while the panels inside sat flush, which is
  why the spacing looked unfixed after it had been "fixed". The same trap then hit
  `.filter-select .select-trigger`. **Two ways out, and only two**: put the rule in a stylesheet the
  child itself includes (`editor-card.css`), or have the child expose a custom property
  (`--select-height`). A selector written from outside is not one of them.
- **`position: absolute` does not mean "free".** The autocomplete's results panel displaced nothing,
  yet the page still grew 84px whenever results appeared: an absolutely positioned element that
  overflows the bottom of the document still extends the scrollable area. It needed the same
  open-upward rule as the select.
- **The unit enums were an invented uppercase subset.** The server's are
  `WeightUnitOfMeasure {g, kg, l, lb, T}` and `DimensionUnitOfMeasure {cm, cu, ft, in, m}` —
  lowercase bar the ton. The console declared `['KG','LB']` and `['CM','IN']`, so the value never
  matched, a save would have posted a constant the enum does not have, and three of five units in
  each set were unreachable. **A native `<select>` hid it by falling back to its first option**;
  the themed one rendered blank, which is how it was finally seen. Third defect in this module that
  a native select concealed.
- **`shareReplay` caches a failure as faithfully as a value.** A reference read that 404s while a
  service is warming up would be replayed to every reader for the rest of the session. The cache
  drops its entry on error so the next reader tries again.
- **A product can have two default images.** `defaultImage: true` on more than one row of
  `product_image` for the same product — the upload endpoint sets the flag without clearing the
  previous one, and nothing enforces uniqueness. Which image the storefront picks is then arbitrary.
  Not fixed here; `store-pod` is not modified by a module.

## Billing — entitlements are ceilings with no usage behind them

- **Screen:** `/subscription/plan`, the "Plan allowances" list.
- **What the UI needs:** each allowance as a *fraction* — "3 of 500 products", "1 of 3 stores" — so an
  operator can see how close they are to the ceiling, which is the only reason to show an allowance at
  all. A bare "500 products" tells them what they bought, not whether it is running out.
- **What is missing:** the numerator. `PlanView.entitlements` publishes the ceiling for each
  `EntitlementKey`, and nothing anywhere counts what a store has actually used against one. Billing
  makes quota *decisions* (`ExternalStoreQuotaApi.private/store-create` answers yes or refuses) but
  exposes no counter, and each ceiling would have to be counted in a different pod anyway — products
  in catalog, orders in checkout, storage in whatever owns the object store.
- **Why it is required:** an allowance a merchant cannot measure themselves against is decoration, and
  the first they learn of a limit is the refusal when they cross it. That refusal arrives at store
  creation or product save, which is the worst possible moment to discover it.
- **Expected contract:** `GET /billing/api/v1/private/subscription/usage?store=` →
  `{MAX_PRODUCTS: 3, MAX_ORDERS_MONTH: 41, ...}`, one entry per entitlement key the plan carries,
  aggregated by billing from the owning services rather than by the console making one call per pod.
  Absent keys mean "not counted", which the console must render differently from zero.
- **Placeholder:** `TODO(lessons.md)` in `features/billing/billing.html`; the list renders the ceiling
  alone under a note saying usage is not available yet.

## The alignment pass — what six modules of drift looked like

Not backend gaps. The console-side findings from the pass between Module 6 and Module 7, recorded
because each has a rule behind it that `ARCHITECTURE.md` now states, and because several were
invisible until something was measured rather than read.

- **A field was defined five times.** `.field`, `.control`, `.field-label` and `.required` existed
  in the catalogue's `editor-card.css`, the product form's near-identical copy of it, store
  management's `settings-card.css`, create-store's page sheet and auth's element selectors. They had
  drifted to two paddings, two textarea heights and one invalid state between them. Nobody decided
  that; each was copied from the last one at the moment a module needed a form.

- **The same is true of everything a page does that is not its content.** Nine load-error blocks in
  four shapes, eight first-load slabs at three heights, seven entry keyframes from 8px to 20px, six
  spinners for one rotating circle, four names for one square icon button, seven class names for an
  empty state. The pattern is always the same: the second module copies the first because copying is
  cheaper than extracting, and the fourth no longer knows the first exists.

- **A `[value]` or `[checked]` binding only writes when the expression differs from what it last
  *wrote*.** A click changes the DOM behind its back, so a model reset to the value it held before
  the click writes nothing and the control stays visibly wrong — which is exactly what a rejected
  save looks like from the operator's side. A control that owns DOM state has to drive it from the
  signal. `app-toggle` never had the problem, because a button with `aria-checked` has no DOM state
  of its own to fall out of step.

- **Passing `id` to a component with an `id` input puts it on the host as well as on the control.**
  Two elements shared one id, `<label for>` resolved to the host — which is not labelable — and the
  association silently did not happen. Four of the six controls with an `id` input had shipped that
  way. Found by probing where the id actually went, not by reading the template.

- **Setting a `model()` emits synchronously.** A host listening to the output runs *inside* the
  input handler, so writing the form control after setting the model overwrites anything the host
  normalised. The domain field's paste-a-URL cleanup was being undone one line later.

- **A test fake must not import the production constant it stands in for.** `console-api.fake` read
  the real navigation, so a spec asserting on the rail could not fail however wrong that constant
  became.

- **Two of the audits I commissioned were confidently wrong, in both directions.** One reported 37
  dead translation keys; 25 were composed at runtime — `marketing.entitlement.${key}.limit`,
  `'legal.' + document + '.title'` — and deleting them would have taken the pricing table and both
  legal pages down under the strict missing-key handler. Another reported a dozen dead exports that
  were types in public signatures. And `details-section`'s `['KG','LB']` looks exactly like the
  invented uppercase subset lessons.md records fixing in the product form, but the backend genuinely
  has two enums: `WeightUnit {LB, KG}` for a store and `WeightUnitOfMeasure {g,kg,l,lb,T}` for a
  product. Verify before deleting; a grep does not know what a template literal builds.

- **A rule that cannot fire is worse than no rule.** An eslint rule to catch backticks inside inline
  templates can never run, because the backtick makes the file unparseable and the AST never forms.
  The parse error is the signal; it is just an unhelpfully worded one, and `CLAUDE.md` now says so.

- **`npm test` and `tsc --noEmit` both pass on templates that do not compile.** Strict template
  checking lives in the AOT build. A bare boolean attribute on a component input, an unknown
  element, a mistyped binding — none of them appear until `npm run build`.


## Catalogue — the inventory KPI row, revisited: the page wants tiles it can have

- **Screen:** `/products`, the band above the table — the four tiles of `Inventory.dc.html` that
  "Catalogue — the inventory KPI row is removed rather than reported unavailable" took out.
- **What happened:** with the row gone, `/products` is the only list page in the console that opens
  on a bare table. `/orders` and `/dashboard` both lead with a KPI grid, and a seller moving between
  them reads the products page as unfinished rather than as honest. The earlier decision is still
  right about those four tiles — Stock on hand, Inventory value, Low stock and Out of stock each
  need something the platform does not have — but "these four are unbacked" was answered by removing
  the band rather than by asking what a backed tile would be.
- **What the platform can already answer.** Three figures are in the snapshot the page fetches on
  every load, at no extra cost: the total SKU count (`page.totalElements` unfiltered), how many are
  available and how many are not (the counts behind the All / Available / Unavailable switcher), and
  how many categories and brands the catalogue spans (the two filter lists the same response
  carries). None of them needs a new endpoint.
- **What is missing for the rest:** a `quantity = 0` predicate on `ProductCriteria` would make Out of
  stock real without a reorder point; a sum of `price × quantity` over the filtered set would make
  Inventory value real. Both are aggregates over a query the repository already builds.
- **Expected contract:** `GET /private/products/summary?store=` answering
  `{total, available, unavailable, outOfStock, inventoryValue, currency}` for the current filter, so
  the tiles narrow with the table rather than standing apart from it. Failing that, `ProductCriteria`
  gaining `quantityEquals` is the smaller half and would carry two of the tiles on its own.
- **Placeholder:** none yet — the band is still absent, and the spec
  `shows no KPI row — all four tiles the design draws are unbacked` in `products.spec.ts` pins that.
  The console change is a follow-up, not a gap in the backend: the three figures above can be built
  from what `/products` already receives, and should be, before any of the endpoints above exist.


## Payments — nothing aggregates a transaction

- **Screen:** `/payments`, the KPI row and the whole upper band of `console-template/Payments.dc.html`
  — Captured `$48,230`, Pending approval `$12,480`, Refunded `$986`, the fourteen-bar "Volume by day"
  chart, and the Gateways panel's `Stripe $31,410 / 65%` split.
- **What the UI needs:** money totalled over a period, and grouped — by status, by day, by gateway.
- **What is missing:** every aggregate. `grep -i statistic` across `store-pod/payment` returns **zero
  hits**. `TransactionRepository` is a `JpaRepository` + `JpaSpecificationExecutor` with three
  finders and no `@Query`, no count projection and no group-by. checkout has `order-statistic`,
  `product-statistic` and `customer-statistic`; payment has nothing of the kind.
- **Why it is required:** "how much did we take this month" is the first question a payments page is
  opened to answer, and it is the one question this one cannot.
- **Expected contract:** `POST /spg/payment/api/v1/private/payment/transaction-statistic?store=` taking
  the same `StatisticRange` the checkout statistics take, answering entries keyed by status, by
  payment type and by day, each with a count **and** a summed amount with its currency.
- **What console-ui does meanwhile:** shows four **counts** instead of four amounts — awaiting
  approval, captured, failed, refunded — each a one-row fetch read for its `totalElements`. The
  volume chart, the gateway split and the settlement summary are absent rather than drawn from
  invented figures. See also "Dashboard — counting requires fetching", which this page pays four
  times over.
- **Placeholder:** `TODO(lessons.md):` in `features/payments/payments.html` and
  `features/payments/facades/payments.facade.ts`.

## Payments — no payouts, no settlement and no gateway fee

- **Screen:** `/payments` — the Payouts panel (`Payout to Chase •••• 8842 · Scheduled · $45,831.40`,
  four rows and a "Payout schedule" link) and the settlement stack beneath the Gateways panel
  (`Gross volume`, `Gateway fees −$1,412.60`, `Refunds`, `Net payout`). Plus the per-row `fee $36.20`
  under every amount in the transactions table.
- **What is missing:** all of it. `payout` has 16 hits repo-wide and **every one is inside
  `console-template/*.dc.html`**; `settlement` has two, both CMS seed copy. `gatewayFee` has zero.
  There is no fee column on `Transaction`, no fee field on any DTO, and no net-of-fees figure
  anywhere in payment or checkout — the single `fee` hit in either pod is a code comment.
- **Why it is required:** the difference between what a customer paid and what reaches the seller's
  bank is the number a merchant reconciles against. Without it the console can say what was charged
  and nothing about what was received.
- **Expected contract:** a `Payout` entity keyed by store with a scheduled date, a destination, a
  status and the transactions it covers; a `fee` and `netAmount` on `ReadableTransaction`, populated
  from the gateway's own settlement report.
- **What console-ui does meanwhile:** neither panel is built and the fee line is absent from the row.
  The amount shown is the gross the customer was charged, which is the only figure that exists.

## Payments — no disputes and no chargebacks

- **Screen:** `/payments` — the "Disputes open" KPI tile (`1`, `Evidence due`, `$447.00 · respond by
  Aug 11`) and the Disputed tab, whose subtitle reads "Chargebacks needing evidence within 7 days".
- **What is missing:** the concept. `chargeback` has exactly one hit repo-wide, in the mockup.
  `dispute` has nine, five in the mockup and four in **billing**'s subscription vocabulary, which is
  a different domain. `store-pod/payment` has neither word.
- **Why it is required:** a dispute has a deadline attached to it, and missing the deadline loses the
  money. It is the one thing on a payments page that is genuinely time-critical.
- **Expected contract:** a `Dispute` entity against a transaction, carrying a reason, an amount, an
  evidence-due date and a state, fed by the gateway's dispute webhooks — Stripe already posts them to
  `POST /api/v1/public/webhook/{storeId}/{paymentType}`, which currently only routes payment events.
- **What console-ui does meanwhile:** neither the tile nor the tab exists. The tile's slot is taken
  by a Failed count, which is real.

## Payments — no refund, no capture and no void

- **Screen:** `/payments`, the Refunds tab and its "Full and partial refunds issued" subtitle; and
  `/orders/:id`, where the same gap was recorded from the other side.
- **What is missing:** any endpoint that moves money back or completes an authorisation.
  `PaymentStatus.REFUNDED` exists as an enum constant and `TransactionType.REFUND` exists in an enum
  that is **dead code** — `TransactionType` is referenced by no entity, no DTO and no controller.
  Nothing sets either. There is no capture and no void: the only cancellation is
  `Transaction.canceled()`, driven by a Stripe webhook, never by an API.
- **Why it is required:** a refund is the second most common thing an operator does on a payments
  page, after confirming one.
- **Expected contract:** `POST …/private/payment/transaction/{internalRef}/refund` taking an amount
  and a reason, delegating to the processor, and recording a linked transaction rather than mutating
  the original's status.
- **What console-ui does meanwhile:** `REFUNDED` is offered as a **filter**, because a transaction
  could in principle arrive in that state, but there is no refund action anywhere. Cross-references
  "Orders — no refund and no capture", which found the checkout half of the same hole.
- **Note:** `payment/init-sql/schema.sql`'s `CHECK` constraint on `transaction.status` is stale — it
  permits `PAY_LATER`, which is not in the enum, and omits both `AUTHORIZED` and `REFUNDED`. Setting
  either of the two real statuses would violate it, so the refund endpoint above cannot be written
  without fixing the constraint first.

## Payments — a transaction carries no customer

- **Screen:** `/payments`, the Customer column — an initials avatar over a name and an email — and
  the Method column's `•••• 4242` / `Visa` meta line.
- **What is missing:** any reference from a transaction to a person or to a payment instrument.
  `ReadableTransaction` is `{id, internalRef, requestRef, amount, currency, paymentType, status,
  transactionDate, transactionNo}` and the entity behind it adds only gateway plumbing — an external
  id, redirect URLs, an expiry and a free-text `details`. The card brand and last four live inside
  the gateway; the platform never stores them.
- **Why it is required:** "who paid this" is how an operator finds a transaction a customer is
  telephoning about. Without it the only handles are two opaque references.
- **Expected contract:** the customer's id and display name denormalised onto the transaction at
  initiate time — checkout knows both — plus an optional `instrument` of `{brand, last4}` where the
  processor returns one.
- **What console-ui does meanwhile:** both columns are absent. The order link is the only route from
  a transaction to a person, and it is a convention — see the next entry.

## Payments — the link from a transaction to its order is a convention

- **Screen:** `/payments`, the Order column; and `/orders/:id`, the Payments panel.
- **What is present, and why it is not enough:** `checkout`'s `OrderPlacementFacadeImpl` builds its
  `PaymentRequest` with `.ref(modelOrder.getId().toString())`, and that value lands in
  `Transaction.requestRef`. So `requestRef` **is** the order id — as an untyped string, by a
  convention held in one line of a different service. The payment service does not know it is an
  order. `payment/init-sql/schema.sql` still declares an `order_id bigint` column on
  `payment.transaction` that no entity maps and nothing writes, which is the shape of the intended
  answer left unfinished.
- **Why it matters:** it is the only join between the money and what was bought, and it will break
  silently the moment anything else initiates a payment with a different kind of reference.
- **Expected contract:** a typed `orderId` on `ReadableTransaction` — the dead column made real — or
  `GET /spg/checkout/api/v1/private/orders/{id}/transactions?store=` so the traversal is the
  platform's rather than the console's.
- **What console-ui does meanwhile:** traverses it, in both directions, and says so. The ledger links
  a row to `/orders/{requestRef}` **only when the reference parses as a positive integer**; anything
  else is rendered as an opaque reference rather than a link that would 404. Order details reads the
  same convention backwards, listing `?requestRef={orderId}`. This supersedes the "what console-ui
  does" half of "Orders — no link from an order to its payment transactions": the panel that entry
  says is absent now exists, on this convention, with this caveat.
- **Placeholder:** `TODO(lessons.md):` in `models/payment.ts` and `features/payments/payments.html`.

## Payments — the approval queue's own status is never set

- **Screen:** `/payments`, the Awaiting approval tab and its KPI tile; and `/dashboard`, the "payment
  approvals waiting" figure shipped in Module 3.
- **What is missing:** anything that sets `PaymentStatus.WAITING_VERIFICATION`. The constant appears
  in exactly two places in the platform's Java — its own declaration in the enum, and one line of
  `TransactionServiceImpl` mapping it onto a gateway result. No processor returns it:
  `ManualTransferredProcessor.initiate` returns `PENDING`, the same status a card payment sits in
  while the gateway works.
- **Why it matters:** the status that names the queue is unreachable, so a console filtering on it
  counts **zero forever**. That is exactly what the dashboard tile did from the day it shipped — it
  was not visibly wrong, because zero is a plausible answer, which is what makes this the more
  dangerous kind of bug.
- **Expected contract:** `ManualTransferredProcessor.initiate` should return `WAITING_VERIFICATION`,
  and the status should mean "a person must act" for every processor that has such a state. Until
  then the two meanings of `PENDING` — "waiting on a machine" and "waiting on a human" — are
  distinguishable only by payment type.
- **What console-ui does meanwhile:** filters the queue on `status=PENDING` **and**
  `paymentType=MANUAL_TRANSFER`, which the server ANDs, and the dashboard tile was corrected to match.
  `WAITING_VERIFICATION` is still rendered wherever a transaction arrives in it, and is still a tab —
  it is only never *the* queue.
- **Placeholder:** `TODO(lessons.md):` in `models/payment.ts` and `api/payment/payment.service.ts`.

## Payments — rejecting a payment tells checkout nothing

- **Screen:** `/payments`, the Reject action and its confirmation dialog.
- **What happens:** `PrivatePaymentApi.reject` sets the transaction to `REJECTED` and fires **no
  event**. Its sibling `approve` sets `PAID` and registers `PaymentPaidEvent`, which is what reaches
  checkout and moves the order. So approving a manual transfer completes an order and rejecting one
  leaves the order exactly where it was, indefinitely, with a payment that will never arrive.
- **Why it is required:** the two halves of a decision should have symmetric consequences. As it
  stands an operator who rejects a payment has to remember to go and cancel the order by hand, and
  nothing tells them so.
- **Expected contract:** a `PaymentRejectedEvent` alongside `PaymentPaidEvent`, consumed by checkout
  the way the paid event already is, releasing any stock reservation the order holds.
- **What console-ui does meanwhile:** says so, at the moment of the action. The confirmation dialog
  reads "The order does not change status and the customer is not notified — you will need to follow
  up separately", and the toast afterwards repeats that the order was not changed. It is the only
  place an operator would ever find this out.
- **Placeholder:** `TODO(lessons.md):` in `features/payments/payments.html` and
  `features/payments/facades/payments.facade.ts`.

## Payments — approve and reject are unguarded and not idempotent

- **Screen:** `/payments`, the row actions.
- **What happens:** neither endpoint checks the transaction's current state. `approve` sets `PAID`
  and fires `PaymentPaidEvent` whatever the transaction was — including a transaction that is
  already `PAID`, which re-fires the event and hands checkout a second completion for the same
  order. `reject` will happily reject a paid payment. There is no idempotency key and no optimistic
  lock. `getTransaction` also throws a bare `IllegalArgumentException` for an unknown `internalRef`,
  which surfaces as a 500 rather than a 404.
- **Why it is required:** a double-click on Approve is not an exotic input, and the server is the
  only place this can be made safe — two operators on two screens cannot be prevented from the
  client.
- **Expected contract:** reject any transition out of a terminal status with a 409, and answer 404
  for an unknown ref.
- **What console-ui does meanwhile:** guards on two axes, in `isApprovable`. The **status** must be
  one of `PENDING`, `PROCESSING`, `WAITING_VERIFICATION`, `AUTHORIZED`, and the **gateway** must be
  one a person actually settles — `MANUAL_TRANSFER` or `COD`. Both buttons are disabled while a
  write is in flight, and the list is re-read afterwards rather than assumed.
- **The gateway half came out of QA against the live stack**, and is a deliberate narrowing of
  seller-ui, which offered the actions on every gateway. The store had one real transaction: a
  `PENDING` **Stripe** payment for SAR 8,500 that the processor had not settled, sitting under an
  Approve button. Pressing it would have set `PAID` and fired `PaymentPaidEvent` — telling checkout
  an order was paid for which no money had been taken, with no refund endpoint anywhere to reverse
  it. A card payment is never waiting on the operator; the processor has it.
- **Still only a guard on the common case, not on the race.** Two operators on two screens cannot be
  prevented from the client, and the server checks neither axis.
- **Placeholder:** `TODO(lessons.md):` in `api/payment/payment.service.ts`.

## Payments — a gateway is offered that cannot take money

- **Screen:** `/payments`, the gateway filter; and `/store-management/payments`, the gateway list.
- **What happens:** `GET …/private/payment-configuration/supported-payment-types` returns all four
  `PaymentType` values, `PAYPAL` among them, but there is no PayPal processor — only
  `StripeProcessor`, `CODProcessor` and `ManualTransferredProcessor` exist under
  `payment-service/.../service/processor/`. Configuring PayPal succeeds, enabling it succeeds, and
  the first customer to choose it gets `PaymentInitiateResult.failed()` with "un supported payment
  type" in the log.
- **Why it is required:** the endpoint's name promises support and the enum is what it answers with.
  A merchant has no way to discover the difference before a customer does.
- **Expected contract:** `supported-payment-types` should answer the types that have a registered
  processor, not `PaymentType.values()`.
- **What console-ui does meanwhile:** lists all four, because that is what the platform says, and
  filters by them. It does not warn — the warning belongs on the configuration screen, and that
  screen cannot tell which types are real either.
- **Placeholder:** `TODO(lessons.md):` in `features/payments/payments.ts` and
  `api/payment/payment-configuration.service.ts`.

## Payments — no transaction detail endpoint

- **Screen:** `/payments` — the row, and the detail view there is no room for in a row.
- **What is missing:** `GET …/private/payment/transactions/{internalRef}`. The list is the only read.
  The entity holds several fields the list's projection drops — `paymentGatewayExternalId`,
  `redirectUrl`, `successUrl`, `cancelUrl`, `expireAt` and a free-text `details` — and a support
  question about a failed payment is usually a question about exactly those.
- **Why it is required:** to answer "why did this fail" an operator needs the gateway's own reference
  and the failure detail, and both exist in the row the API declines to send.
- **Expected contract:** `GET …/transactions/{internalRef}?store=` answering a `ReadableTransaction`
  widened with the gateway reference, the expiry and the detail text.
- **What console-ui does meanwhile:** shows the row and nothing more. There is no detail route,
  because there would be nothing on it that the table does not already show.
- **Placeholder:** `TODO(lessons.md):` in `api/payment/payment.service.ts`.

## Payments — no export of any kind

- **Screen:** `/payments`, the header's "Export CSV" in `Payments.dc.html`.
- **What is missing:** any export endpoint, in payment or anywhere else — the same finding the
  catalogue reached ("Catalogue — no CSV import or export"). Reconciliation is a spreadsheet job and
  the platform offers no way to get the data into one.
- **Why it is required:** a merchant reconciling a month against a bank statement works in a
  spreadsheet, and re-keying a paged HTML table is not a workflow.
- **Expected contract:** `GET …/private/payment/transactions.csv?store=` taking the same
  `TransactionSearchFilter` and streaming the whole matching set, not one page.
- **What console-ui does meanwhile:** exports a **PDF** of what is on screen, through the existing
  `core/export/pdf-export.service.ts` and `shared/ui/export-button`, exactly as `/orders` does. It is
  the current page, not the full result set, and it is not machine-readable — but it needs no
  backend and it is honest about being a printout.
- **Placeholder:** `TODO(lessons.md):` in `features/payments/payments.html`.

---

## Users — the JWT carries no user id

**Found by reading `JwtCustomizerConfig` against what `/api/v1/auth/me` returns, on the running stack.**

- **Screen:** `/profile`, and every path by which the console would read the signed-in operator's own
  account record.
- **What is missing:** any way for the console to name itself to the server. Spring Authorization
  Server sets `sub` from `principal.getName()`, which here is the **username**, and
  `JwtCustomizerConfig.addUserClaims` adds `roles` and spreads `users.metadata` (`org`, `store`) into
  the access token — and **no id claim at all**. Meanwhile every read-by-id path in tenancy ends at
  `AdminUserClient.getUser(id)` → `GET {uaa}/api/v1/admin/users/{uuid}`, and uaa exposes no
  get-by-username (only `GET /exists?username=`, and that controller is super-admin only).
- **Why it matters:** it is the root cause of the two entries below it, and it is why the standing
  "Shell — user-account/current is broken for JWT callers" entry understates the problem. Binding
  `@AuthenticationPrincipal Jwt` there would fix the NPE and then pass a *username* to a by-UUID
  lookup, so the 500 becomes a 404 rather than an answer. **Both defects have to be fixed for either
  to matter.**
- **Expected contract:** add a `uid` claim carrying `users.id` in `JwtCustomizerConfig` — one line
  beside the metadata spread — or give uaa `GET /api/v1/admin/users/by-username?username=`. The claim
  is the cheaper of the two and closes the whole family.
- **What console-ui does meanwhile:** `/profile` shows the username and the roles, which are the only
  identity the token genuinely carries, and reads no account record at all. `/users` is unaffected,
  because a row there carries a real `ReadableUser.id`. The two sides are matched on **username** for
  "is this me", which is the only field they share.
- **Placeholder:** `TODO(lessons.md):` in `api/tenancy/user-account.service.ts`.

## Users — the ID token requests only the `openid` scope

**The one-row cause of the already-logged "Shell — uaa's ID token carries no profile claims".**

- **Screen:** the toolbar's profile menu and `/profile`, both of which show a username where a person
  should be.
- **What is missing:** the `profile` and `email` scopes. `uaa/init-sql/data-common.sql`'s `web-app`
  client row declares `scopes = 'openid'` and nothing else, so the ID token the gateway obtains has
  no `given_name`, `family_name` or `email` to carry — which is exactly what
  `AuthService`'s `AuthUser` documents as null.
- **Why it matters:** it is one row of seed data and one entry in whatever provisions that client in
  a real environment. Of everything in this file it is the cheapest thing to fix, and it would give
  the console a person's name without a single new endpoint.
- **Expected contract:** `scopes = 'openid,profile,email'` on the `web-app` client, and
  `JwtCustomizerConfig` populating the standard claims on the ID token as well as the access token.
- **What console-ui does meanwhile:** shows `principal.name`, the username.
- **Placeholder:** documented in `core/auth/auth.service.ts`.

## Users — the user list is store-scoped, so an org admin is in no list

- **Screen:** `/users`, the team table.
- **What is missing:** an org-scoped read. `ManagedUserAccountServiceImpl.list` filters uaa on
  `{org, store}` — both, always — and `validateUserAccess` refuses `find-one` for any user whose
  metadata `store` differs from the requested one. An org admin is stored with `{"org": …}` and **no
  store** (`uaa/init-sql/data-test-stores.sql`), so:
  - they appear in no store's user list, including the one they are looking at;
  - `find-one` refuses them under every store, including to themselves;
  - a console that only ever reads through this API cannot show an organization's full staff.
- **Why it is required:** the page is called user management and it silently omits the people with
  the most access. An operator counting heads gets the wrong number and has no way to know it.
- **Expected contract:** either `GET …/user-account/list` gains an org-scoped mode when the caller is
  an org admin, or a null `store` in a user's metadata is read as "every store in the org" by both
  the list filter and `validateUserAccess`. The second is closer to what the data already means.
- **What console-ui does meanwhile:** renders the list as the server scopes it, and the page header
  already says whose it is ("Everyone with access to ORG1-STORE1"). A notice restating it above the
  table was removed at the user's direction: it repeated the header on every visit to say something
  that is only occasionally relevant, and a banner an operator learns to skip is worse than no
  banner. **The gap itself is unchanged** — an org-level account is still in no list, and there is
  still no filter that would show one.
- **Placeholder:** `TODO(lessons.md):` in `features/users/facades/users.facade.ts`,
  `models/team.ts` and `models/users.ts`.

## Users — assignable-roles offers SUPER_ADMIN to an org admin

- **Screen:** `/users`, the role picker in the detail rail.
- **What is wrong:** `UserAccountServiceImpl.getAssignableRoles` filters uaa's role table down by
  removing exactly two names — `USER` and `ORG_ADMIN`. The seeded table is
  `{SUPER_ADMIN, USER, ORG_ADMIN, STORE_ADMIN, STORE_MODERATOR}`, so what an org admin is offered
  includes **platform superuser**. The endpoint also carries no `@PreAuthorize` at all.
- **Why it matters:** a console that rendered this list verbatim would put a privilege escalation in
  a checkbox. Whether `create` would then honour the role is a separate question the UI should not be
  the thing answering.
- **Expected contract:** filter to the roles the *caller* may grant — never above their own — and put
  a permission token on the endpoint.
- **What console-ui does meanwhile:** intersects the server's answer with `OFFERABLE_ROLES`
  (`models/team.ts`), which is `STORE_MODERATOR` and `STORE_ADMIN`. An intersection rather than a
  filter of one name, so a role added to uaa's table later cannot appear in a picker unreviewed
  either. **This is defence in depth and not a fix** — the server is still offering it to anything
  else that asks.
- **Placeholder:** `TODO(lessons.md):` in `api/tenancy/user-account.service.ts` and `models/team.ts`.

## Users — no self-service password change

- **Screen:** `/profile`, "Sign-in & security" in `Account Profile.dc.html`.
- **What is missing:** two things.
  1. **A self-service endpoint.** The only password path is `POST …/user-account/reset`, an
     administrative action addressed by `userId`, guarded by a maintain-level permission. There is
     nothing an operator can call about their own password — and per "Users — the JWT carries no user
     id" they could not name themselves to it anyway.
  2. **Any verification of the current password.** `UserPassword` has a `password` field beside
     `changePassword` and **it is read by nothing**: `UserAccountServiceImpl.changePassword` passes
     only `getChangePassword()` to uaa's admin reset. `grep -i "currentPassword\|oldPassword"` over
     the whole repo returns nothing.
- **Why it is required:** every console has this, and its absence means a password can only be
  changed by someone else, who then knows it.
- **Expected contract:** `POST /tenancy/api/v1/user-account/change-password` taking
  `{currentPassword, newPassword}` for the authenticated caller, verifying the first.
- **What console-ui does meanwhile:** the password card is **not built** on `/profile`. Setting a
  password lives on `/users`, where it belongs — an admin action on a named account — and its dialog
  asks for the new password twice and for no current one, because asking for a value nothing verifies
  would be theatre. The dialog says the operator has to hand the password over themselves.
- **Placeholder:** `TODO(lessons.md):` in `models/users.ts`.

## Users — a permission token that locked every password reset

**Fixed here, not merely recorded — the second resolved entry in this file, after the dashboard
statistics outage.**

- **What was wrong:** `UserAccountApi.resetPassword` declares
  `@PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-CORE.USERS.RESET_PASSWORD')")`, and
  that token appeared in exactly one file in the repository — the annotation itself.
  `CustomPermissionEvaluator.hasStoreCorePermission` wires the other six `USERS.*` tokens and falls
  through `hasBillingPermission` and `hasPodRegistryPermission` to `default -> false`. **The endpoint
  was 403 for every caller, super admin included, from the day it was written.**
- **Why nothing reported it:** an unmapped permission token is indistinguishable from a refused one,
  and no frontend called the endpoint. seller-ui's change-password screen points at
  `PATCH /v1/private/user/{id}/password`, which is mapped nowhere either, so it failed earlier for a
  different reason and the 403 behind it was never reached.
- **Resolved by:** `fix(commons): the permission token that locked every password reset` — one `case`
  resolving to `hasMaintainAccessOnUsers`, the same audience as create/update/delete/enable/disable,
  and deliberately not the moderator who has read access only. It carries the first tests in
  `store-commons`; reverting the case fails two of them.
- **Left standing:** nothing checks that a declared token is *mapped*. The next one added without a
  case will fail the same silent way, and only in a QA pass. A test that scans every `@PreAuthorize`
  literal in the repo against the evaluator's switches would close it for good.

## Users — no password policy anywhere

- **Screen:** `/users`, the create form and the set-password dialog.
- **What is missing:** any server-side rule. `AdminService.resetPassword` encodes whatever string it
  is handed, and `CreateUserRequest` validates only `@NotBlank username` and `@Email email`. Length,
  complexity, reuse, breach lists — none of it exists, and there is no place to configure any.
- **Why it is required:** the console's rule is currently the only rule, so anything that talks to
  uaa directly — a script, a migration, the admin API — can set a one-character password.
- **Expected contract:** validation on `ResetUserPasswordRequest` and `CreateUserRequest`, ideally
  configurable, so the policy lives with the store of record rather than with one of its clients.
- **What console-ui does meanwhile:** enforces eight or more characters with an uppercase, a
  lowercase and a digit (`PASSWORD_PATTERN` in `features/users/services/user-form.service.ts`).
  seller-ui's equivalent capped passwords at **twelve** characters; that upper bound is deliberately
  not carried over, because a maximum length blocks passphrases and bcrypt has no trouble with them.
- **Placeholder:** `TODO(lessons.md):` in `features/users/services/user-form.service.ts`.

## Users — a taken username cannot be checked before submitting

- **Screen:** `/users`, the username field when creating.
- **What is missing:** a uniqueness pre-flight reachable by a merchant. Every other create form in
  this console has one — `…/store/unique?name=`, `…/product/unique?code=`,
  `…/category/unique?code=` — and drives `uniqueAsync` with it. uaa does expose
  `GET /api/v1/admin/users/exists?username=`, but that controller is
  `hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')` and tenancy does not proxy it.
- **Why it is required:** usernames are unique platform-wide, so an operator picking one is guessing
  against a namespace they cannot see, and finds out only after filling in the whole form.
- **Expected contract:** `GET /tenancy/api/v1/user-account/unique?userName=` → `{exists}`, scoped so
  it answers only yes-or-no and cannot be used to enumerate other tenants' users — the same shape and
  the same caveat as `…/store/unique`.
- **What console-ui does meanwhile:** submits and binds the conflict onto the field.
  `ApiErrorService.applyToForm` handles it, and — as with signup — a 409 arrives as
  `COMMON.DATA_INTEGRITY_VIOLATION` with no `fieldErrors[]`, so it lands as a message rather than on
  the username itself.
- **Placeholder:** `TODO(lessons.md):` in `features/users/services/user-form.service.ts`.

## Users — no user search of any kind

- **Screen:** `/users`, the "Name or email" box in `User Management.dc.html`.
- **What is missing:** any query at all. `AdminService.getUsers` builds its `Specification` from
  `UserSpecifications.hasMetadataField` — **metadata equality and nothing else** — and tenancy passes
  it exactly `{org, store}`. There is no name, email or username predicate, no partial match, and no
  sort parameter that reaches the repository.
- **Why it is required:** the list is paged at twenty. A store with sixty staff is three pages an
  operator has to read in order to find one person.
- **Expected contract:** a `q` parameter on `GET …/user-account/list` matching `username`, `email`,
  `first_name` and `last_name` case-insensitively, and a `sort` that reaches the query.
- **What console-ui does meanwhile:** **the search box is not built.** A box that filtered only the
  twenty rows already on screen would look like a search and answer a different question — the exact
  "fixture standing in for a real answer" this file exists to prevent.
- **Placeholder:** `TODO(lessons.md):` in `features/users/services/users.api.service.ts` and
  `api/tenancy/user-account.service.ts`.

## Users — a user has no last-login, no avatar and no profile fields

- **Screen:** `/users`' detail rail and `/profile`, against `User Management.dc.html` and
  `Account Profile.dc.html`.
- **What is missing:** almost everything the design shows about a person. `uaa.users` is
  `{id, email, username, first_name, last_name, password_hash, metadata, enabled, created_at,
  updated_at}`. Verified by grepping the whole repository for each of the designed fields:
  - `avatar` — **zero hits.** No photo, no upload, no URL.
  - `lastLogin` / `last_login` — **zero hits.** `ReadableUser.lastAccess` and `.loginTime` exist on
    the DTO and are set by **no mapper** — dead fields, and the "Last active" column rests on them.
  - `jobTitle`, `timezone`, `dateFormat`, `bio` — **zero hits** each.
  - a phone number on a user — none; the design's phone row and the addresses card have no source at
    all, staff having no address model.
  - `defaultLanguage` is on `UserEntity` and has no uaa column either — declared, never persisted.
- **Why it is required:** less than it looks. Most of these are decoration. The two that are not are
  **last-login**, which is how an operator decides whether an account is still in use before removing
  it, and **a display name that is not a username**, which is what makes a console feel like it knows
  who is using it.
- **Expected contract:** a `last_login_at` column written on successful authentication and carried on
  `ReadableUser`; the rest belong in a per-user profile document that does not exist and should
  probably not live in uaa.
- **What console-ui does meanwhile:** renders a monogram from the name, and does not render the
  fields it has no source for — no greyed-out "Last active: —", no empty address card. Where the rail
  would show "Last active", it shows the username and whether sign-in is allowed, both of which an
  operator actually needs when someone says they cannot get in.
- **Placeholder:** `TODO(lessons.md):` in `features/users/services/users.api.service.ts`.

## Users — creating a user is two calls

- **Screen:** `/users`, "Add user".
- **What is wrong:** `UserAccountServiceImpl.createUser` calls uaa twice —
  `client.createUser(...)`, then `client.resetPassword(createdUser.id, user.getPassword())` — with no
  transaction spanning them, which there could not be across an HTTP boundary. If the second fails,
  the first has already committed: the account exists, has no password, and **until the permission
  fix above there was no endpoint that could give it one.**
- **Also:** `PersistableUser.repeatPassword` is never compared server-side. The confirmation is a
  client-side courtesy only, the same finding as "Auth — public signup validates nothing".
- **Why it matters:** the failure is silent from the console's side — the create returns an error, so
  an operator retries, and the retry fails with a username conflict against the half-made account
  they cannot see in the list until it appears there unusable.
- **Expected contract:** have uaa's `POST /api/v1/admin/users` accept the password on
  `CreateUserRequest`, so the account is made in one call or not at all.
- **What console-ui does meanwhile:** nothing it can. The failure is reported and the list re-read, so
  a half-made account at least becomes visible.
- **Placeholder:** `TODO(lessons.md):` in `api/tenancy/user-account.service.ts`.

## Users — nothing emails an invitation

**Not a defect. The constraint that shapes the invite flow, recorded so it is not mistaken for one.**

- **Screen:** `/users`, the Invitations tab, and the accept page.
- **What is missing:** a mail sender, anywhere on the platform. `OrgMemberApi` and
  `CreatedInvitationDto` both say so in their own javadoc: *"there is no mail sender in this
  platform"*.
- **The consequence for the UI:** the token is readable **exactly once**, in the response that
  created the invitation — only its hash is stored, so it cannot be fetched again and "resend" is a
  rotation rather than a repeat. The console therefore has to present the link at the moment of
  creation, prominently enough that an operator does not close the dialog past it, and say plainly
  that nothing was sent.
- **Why it is required:** an invitation flow whose UI implies an email was sent is worse than no
  invitation flow, because the invitee waits for something that will never arrive.
- **Expected contract:** a mail service, and `POST …/invitations` sending the link. Until then the
  API is correct to hand the token back — that is the honest design given the constraint.
- **What console-ui does meanwhile:** shows the link once, with a copy control, and names the
  constraint in the dialog.
- **Placeholder:** `TODO(lessons.md):` in `api/tenancy/org-member.service.ts` and `models/team.ts`.

## Users — no export of any kind

- **Screen:** `/users`, the header's "Export CSV" in `User Management.dc.html`.
- **What is missing:** any export endpoint — the third time this file has reached the same finding,
  after the catalogue and payments.
- **What console-ui does meanwhile:** exports a **PDF** of what is on screen, through the existing
  `core/export/pdf-export.service.ts` and `shared/ui/export-button`, as `/orders` and `/payments` do.
- **Placeholder:** `TODO(lessons.md):` in `features/users/users.html`.

## Users — the member list is not the team

**Found by reading `InvitationService` and `OrgMemberService` while testing the accept flow.**

- **Screen:** nothing, and that is the point — `GET /org-member/list` reads like the answer to "who
  is in this organization" and is not, so no console screen is built on it.
- **What is wrong:** `tenancy.org_member` has exactly one writer. `InvitationService.accept` adds a
  row; `OrgMemberService.add` exists and **is called by nothing**; signup does not add one, and
  neither does `user-account/create`. An organization records its founder as
  `manager_org.owner_user_id`, which is a different column in a different table. So the member list
  contains *people who accepted an invitation* and no one else — not the owner, not anyone an
  administrator created directly.
- **And the key does not join.** `accept` stores `authentication.getName()`, which for a JWT
  principal is the **username**; `OrgMemberDto.userId` is documented as "uaa's id for the user", and
  every other id-taking endpoint (`find-one`, `reset`, `delete`) wants uaa's **UUID**. A row written
  by accept therefore cannot be looked up against a user record without knowing which of the two it
  holds.
- **Why it matters:** it is a trap rather than a missing feature. A console that built its team page
  on the obvious-sounding endpoint would show an organization of twelve as an organization of one,
  and would do it silently.
- **Expected contract:** write a member row wherever membership actually begins — signup for the
  founder, `user-account/create` for a created account — and store uaa's id, consistently with every
  other endpoint that takes one.
- **What console-ui does meanwhile:** the Team tab reads `user-account/list`, which is the real
  answer for a store. `org-member` is used **only** for invitations, where it is correct.
- **Placeholder:** `TODO(lessons.md):` in `api/tenancy/org-member.service.ts`.

## Users — an invitee needs an account before the link can work

- **Screen:** `/accept-invitation`.
- **What is missing:** a path from the invitation link to *having an account*. `accept` requires an
  authenticated principal — it has to, since it is the caller who joins — so the guard sends a
  signed-out invitee to uaa's sign-in page. Someone who has never used cvhome has nothing to sign in
  with, and the only public route from there is `/sign-up`, which is
  `SignUpApi.public/create`: **it creates a new organization with the signer as its administrator.**
  An invitee who takes it ends up owning an org they did not want, and then accepts an invitation
  into a second one.
- **Why it is required:** it is the ordinary case. An invitation is normally the first thing a new
  colleague ever sees of the product.
- **Expected contract:** an invitation-scoped registration — `POST …/invitations/{token}/register`
  taking a username and password, creating the uaa account and accepting in one step — or a signup
  mode that takes a token and joins the inviting org instead of creating one.
- **What console-ui does meanwhile:** the invite dialog says plainly that the person needs a cvhome
  account to accept, so an operator inviting a colleague who has none finds out before sending the
  link rather than after. The accept page handles the authenticated case correctly and does not
  pretend to handle the other one.
- **Placeholder:** `TODO(lessons.md):` in `features/auth/accept-invitation/accept-invitation.ts`.

## Users — the login redirect broke every deep link with a query string

**Fixed here, not merely recorded — the third resolved entry in this file. Reported by the user
against the invitation link, and not an invitation defect at all.**

- **Screen:** every console route reached by a signed-out visitor with anything after the `?` — a
  filtered list, a selected row, an invitation link.
- **What was wrong:** `canAccessSecuredPages` sends an unauthenticated visitor to
  `/oauth2/authorization/uaa?redirectTo=<target>`, and the gateway's
  `CapturingServerOAuth2AuthorizationRequestResolver` forwards that parameter on to uaa. It read the
  value with `getQueryParams()` — which **decodes** — appended it verbatim, and then built the URI
  with `build(true)`, which asserts that everything in it is already encoded. The bare `?` and `=`
  inside the target were then illegal:
  ```
  IllegalArgumentException: Invalid character '=' for QUERY_PARAM
    in "/accept-invitation?token=abc"
  ```
  Reproduced side by side: `redirectTo=%2Fdashboard` answered **302**,
  `redirectTo=%2Faccept-invitation%3Ftoken%3Dabc` answered **500**.
- **Why nothing reported it:** every URL anyone types by hand is a bare path. The parameterised ones
  are produced by the app — a shared link, a copied row, an invitation — and only ever followed by
  someone who is *already* signed in, where the guard never fires. Module 8 is the first flow whose
  whole point is a link carrying a query string, followed by someone with no session.
- **Resolved by:** `fix(console-ui): the copy button, and every login redirect with a query string` —
  the forwarded values are `UriUtils.encode`d before they are appended, which is what `build(true)`
  was always promised.
- **Left standing:** the pairing of a decoding read with an encoded build is not checked anywhere,
  and the resolver forwards a configurable list of parameters, so a second one added later inherits
  the same trap.

## Users — copying inside a dialog was decorative

**Fixed here. A console defect rather than a backend gap, recorded because it was invisible for as
long as it existed and because the next dialog would have inherited it.**

- **Screen:** the invitation link dialog, and — as it turned out — every dialog in the app.
- **What was wrong:** `navigator.clipboard` is gated to secure contexts, and the console runs over
  plain HTTP on a named host, so **every** copy button in development takes the selection fallback:
  a hidden `<textarea>`, `select()`, `document.execCommand('copy')`. That carrier was appended to
  `document.body` — and a dialog opened with `showModal()` sits in the top layer and makes
  everything outside it inert. The carrier could not be selected, so the copy lifted nothing and the
  control reported failure.
- **Why nothing reported it:** the same component works perfectly in a table, which is where it had
  been used until now. `app-copy-field` had no spec at all, and its own failure toast made the
  breakage look like an environment limitation rather than a bug.
- **Resolved by:** the carrier is appended to the topmost `dialog:modal` when one is open. It
  carries the first spec for that helper; reverting the placement fails exactly the modal case.
- **Left standing:** the secure-context problem itself. Over HTTPS the fallback is never reached, so
  this was only ever a development defect — which is precisely why it survived: nobody copies a
  value in production from a machine that reproduces it. Same family as `crypto.randomUUID`, which
  `toast.ts` already works around, and as the DoH check in `dns-check.service.ts`.

## Users — what the first design pass got wrong

**Console-side, in the shape of "The design pass" and "The alignment pass" above. Every item was
found by a person looking at the built page, not by a check.**

- **A dashboard was built for a list of three people.** `/users` opened with a KPI row reporting
  "Team members 2" directly above a table whose own subtitle read "Showing 1–2 of 2 users". The
  template draws four tiles because it imagines a marketplace; a team is a handful of accounts, and
  a metric tile for a number the next line already states is decoration. The row was removed
  outright — the same call the catalogue made about its inventory KPIs, for the same reason.
- **A master-detail rail is for a list you scan, not a list you can count.** The rail was empty on
  arrival, permanently took a third of the width, and squeezed the table hard enough that
  `app-data-table` fell through its own 45rem container query into stacked cards — so the page had a
  two-pane layout whose left pane was no longer a table. Reading one account is a discrete task with
  a commit at the end, which is what a dialog is for, and it costs the list no width.
- **`.page-body` is cited in `ARCHITECTURE.md` and defined nowhere.** Two pages shipped stacking
  their panels with it and got no gap at all. Worse, `@shared/styles/field.css` is included **per
  component**, so a page that forgets it gets `.split` as a plain block while its own scoped rules
  apply normally — which reads as a broken breakpoint rather than a missing import. Now noted in
  `CLAUDE.md` and `ARCHITECTURE.md`.
- **A light token used as a background is a contrast failure waiting for a theme switch.**
  `/profile` painted the selected preference with `--primary-muted` — a *light* emerald whose job is
  to be ink on a dark field. Under Forest it looked merely loud; under Midnight it resolved to pale
  lavender carrying `--foreground-strong` white. The accent pair (`--accent` with
  `--accent-foreground`) is the system's idiom for "this one is chosen" and survives all three
  themes because both halves move together. **A token's name says what it is, not where it goes.**
- **`dir="auto"` on chrome makes a list disagree with itself.** The language picker set it per
  option, so "English" and "العربية" each took their own script's direction and aligned to opposite
  edges of the same list — and the pair swapped over when the page direction changed. Plaintext bidi
  is for *values* whose own direction matters: an email, a SKU, a domain. A language name in a fixed
  picker is a label, and labels align with the page.
- **A control the width of the page reads as a text field.** Two settings of two and three options
  were stacked full-width, giving a two-word label an 1100px measure. They sit side by side and cap
  at 20rem.
