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
