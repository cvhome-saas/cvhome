# uaa-fe — popups, a client page, and the capabilities the redesign dropped

## Why

Two problems, and the second is the serious one.

1. **The detail pane is the wrong container.** Clicking a user, role or client opens an editor beside
   the table it was clicked in. Feedback: it should open a **popup** or **navigate**.
2. **The redesign narrowed the client form.** Checked against `git show origin/main` — the pre-kit
   Nebular app — and against `ClientDetails` / `ClientOptions`, the API still carries everything the
   old form edited. Today's form edits **7 comma-separated text fields** where the old one had
   multi-selects, two URI arrays with add/remove, four TTLs, all of `clientSettings`, all of
   `tokenSettings`, and two custom-settings maps. That is not an alignment; that is a regression I
   shipped, and "take all ideas and improve, not throw" is the correct reading of it.

`GET /clients/options` already returns `idTokenSignatureAlgorithm`,
`tokenEndpointAuthenticationSigningAlgorithm` and `accessTokenFormat` — three lists the current form
never asks for. The server was always ready.

## The container decision

Both were offered ("popup or navigate"). Split by the size of the form, which is also what the two
prior designs do:

| Screen | Container | Why |
|---|---|---|
| **Roles** | popup | `Role` is `{id, name}`. One field. A route for one field is ceremony. |
| **Users** | popup | Names, enabled, roles, metadata rows. Fits a dialog; delete and set-password already are dialogs. |
| **Clients** | **route** — `/clients/new`, `/clients/:id` | Five sections, ~20 controls, two arrays, two maps. The old app routed it; the mocks have a whole *SSO New Client* page for it. A modal here is wrong. |

## What comes back (all of it API-backed)

**Client form** — sectioned page, `Back to clients`, sticky Cancel / Save:

- **Basic** — client id (`^[a-z0-9-]{3,}$`, with **Generate** from the name), client name.
- **Authentication** — auth methods, grant types, scopes as **checkbox groups fed by `options()`**,
  not comma text. `app-select` is single-value; `app-checkbox` is the catalogue's control for "one
  member of a set the operator picks several of".
- **Redirects** — `redirectUris` and `postLogoutRedirectUris` as **FormArrays**: one row per URI, a
  **+ Add**, a per-row remove, per-row format validation, and a per-row status glyph that flags a
  plain `http://` that is not localhost.
- **Client settings** — requireProofKey, requireAuthorizationConsent, jwkSetUrl,
  tokenEndpointAuthenticationSigningAlgorithm, x509CertificateSubjectDN, **customSettings** map.
- **Token settings** — access / refresh / authorization-code / device-code TTLs, reuseRefreshTokens,
  x509CertificateBoundAccessTokens, idTokenSignatureAlgorithm, accessTokenFormat (the `{value}`
  wrapper), **customSettings** map.
- **Readiness panel** — the mock's best idea, and it encodes real OAuth rules rather than decoration:
  redirect URI required with `authorization_code`; PKCE required when the only auth method is `none`;
  JWK Set URL required for `private_key_jwt`; Subject DN for `tls_client_auth`.

**Users popup** — first/last name, enabled, roles, **metadata key/value rows** (`UpdateUserRequest`
carries `metadata`, and nothing edited it), plus **create**, which the old app had and this did not:
`CreateUserRequest` then `reset-password`, the two calls lessons.md already documents.

## Kit additions — additive, opt-in, console-ui specs unmoved

- **`app-form-dialog`** — the modal shell the roles and users popups need. The kit has three
  purpose-built dialogs and no shell; each reimplements `<dialog>`, `showModal()`, `dialog-motion.css`.
- **`app-duration-field`** — ISO-8601 ↔ (amount, unit). The old app had four number boxes; the current
  form asks an operator to type `PT30M`. Parses to the largest unit that divides evenly, so `PT1H30M`
  reads as **90 minutes** rather than losing the 30.
- **`uriValidator`** in `/forms`, with its message.

## Verification

Builds and specs as before (kit, uaa-fe, console-ui 701 unmoved), then in the browser against
`lcl --stack sso`: every validator above driven to its failure state; a client round-tripped with
every restored field read back from the server; metadata written and re-read; create-user through
both calls; Arabic; and the dialogs' motion under `prefers-reduced-motion`.

QA cases append to `store-core/uaa/qa/uaa-qa.md`.
