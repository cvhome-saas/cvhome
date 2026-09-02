# Align uaa-fe to the SSO design

## Context

`store-core/uaa/sso/` holds nine new design mocks — a sidebar-railed SSO admin console in a light
slate-and-emerald palette. uaa-fe was rebuilt last week on `@cvhome-saas/ui-kit` and works, but it wears the
kit's dark Forest theme behind a single horizontal top bar, and its three admin screens use an inline form
panel (roles, clients) and dialogs (users). The mocks organise the same job differently: a grouped rail, a
topbar, and a **list + detail pane** on every screen.

This aligns the four screens that exist — sign-in, users, roles, clients — to that design. **Features drawn in
the mocks that the platform cannot do are out of scope** and are removed rather than faked; they are recorded
so the next person knows they were considered.

The happy finding: the mocks' palette *is* Tailwind slate + emerald, which is exactly what the kit's **Light**
theme already encodes (`--background` slate-100, `--card` white, `--border` slate-200, `--primary`
emerald-500). Most of the visual alignment is configuration and composition, not new CSS.

**Decisions locked with the user:**

| | |
|---|---|
| **Scope** | Chrome **and** page layouts — the master–detail restructure, replacing today's inline form panel and dialogs |
| **Nav** | All seven rail items, with the four that have no backend visibly **disabled** |
| **Theme** | Default to **Light**, keep the four-theme switcher |
| **Gaps** | A new `uaa-fe/lessons.md`, following console-ui's convention |

**Where this branches from.** The rebuilt uaa-fe exists *only* on `refactor/ui-kit` (PR #316, open, not
merged) — a worktree cut fresh from `origin/main` would contain the old Nebular app and none of this applies.
So this work stacks: branch `refactor/ui-kit-sso-design` from `refactor/ui-kit`, in its own worktree with its
own `lcl --stack`, and open its PR against `refactor/ui-kit` — or rebase onto `main` once #316 merges,
whichever lands first. The first commit also copies this plan to
`.agents/plans/uaa-fe-sso-design-alignment.md`, which is where the repo keeps plans.

## What the design is, and what it is not

Everything below was checked against `UserDto`, `RoleDto`, `ClientSummary`/`ClientDetails` and the three admin
controllers. **The right column is why a mock element is or is not built.**

| Mock element | Reality |
|---|---|
| Rail: Users, Roles, Clients | ✅ built |
| Rail: Dashboard, Audit log, Identity providers, Settings | ❌ no backend — rendered **disabled**, per the locked decision |
| Rail: `Users {{ total }}` count badge | ❌ **dropped.** console-ui already faced this exact mock element and removed it: lessons.md, *"Shell — no sidebar badge counts"* — *"A number in a navigation rail is read as fact."* |
| Rail: realm switcher (`production` / `id.cvhome.app`) | ❌ uaa has no realm concept |
| Topbar: breadcrumb, user menu | ✅ built |
| Topbar: notification bell with a count | ❌ no source; same reasoning as the rail badge |
| Users KPIs: Total users | ✅ `SpringPage.totalElements` |
| Users KPIs: Sign-ins·24h, Without MFA, Pending invites | ❌ no source |
| Users table: user, roles, organization, status | ✅ — and the mock *omits* roles/organization, which we have and keep |
| Users table: MFA, Last sign-in | ❌ not on `UserDto` |
| Users detail: first name, last name, enabled, roles | ✅ `UpdateUserRequest` |
| Users detail: email, username | ⚠️ **read-only** — `UpdateUserRequest` carries neither |
| Users detail: Send reset | ✅ as *set* password (`app-set-password-dialog`); uaa has no reset email |
| Users detail: Active sessions / Sign out everywhere | ❌ no session store exposed |
| Users: Import CSV, Invite user | ❌ out of scope (see below) |
| Roles table: role name | ✅ |
| Roles table: Scope, Users, Perms, Type · detail: Description, Inherits from, Permissions, Assigned users | ❌ **uaa's `Role` is `{id, name}`.** Roles reduces to a name, and create/rename/delete |
| Clients KPIs: Registered clients | ✅ |
| Clients KPIs: Tokens issued, Failed authorizations, Secrets expiring | ❌ no source |
| Clients table: client id, name | ✅ — `ClientSummary` is only `{id, clientId, clientName}` |
| Clients table: Type, Protocol, Last token, Status | ❌ not on `ClientSummary`; a per-row fetch would be needed |
| Clients detail: display name, client id, grant types, auth methods, scopes, redirect URIs | ✅ already built |
| Clients detail: Rotate secret | ✅ already built |
| Clients detail: Token lifetimes (access, refresh) | ✅ on `ClientTokenSettings`, **not currently edited** — cheap to add, and the one genuinely new field this plan builds |

## Design direction

Mode is **Operate** (`impeccable`): the operator is in a task, so scanability, consistency and earned
familiarity outrank expression. Three calls follow from that and from the mocks:

- **Master–detail over dialogs is the right call, not just the drawn one.** *"Modal as first thought. Modals
  are usually laziness. Exhaust inline / progressive alternatives first."* Users' three dialogs become one
  pane. **Delete stays a typed confirmation** — it is destructive, has no undo, and the confirm-on-type guard
  is already built and verified.
- **Do not port the mocks' entrance choreography.** Every mock card carries `animation: sk-up .45s ease both`
  with staggered delays. *"No orchestrated page-load sequences. Product loads into a task; users don't want to
  watch it load."* Motion stays what the kit already does: 150–250 ms, state only. The kit's
  `dialog-motion.css` (220 ms, with the travel behind `prefers-reduced-motion`) is the reference.
- **The rail is the second neutral layer** the mode asks for — white card on a slate-100 page. The Light theme
  gives this for free; nothing needs hand-mixing.

**Icons:** the mocks use PrimeIcons. Every in-scope glyph maps onto the kit's existing 94-icon set
(`pi-th-large`→`layoutGrid`, `pi-history`→`clock`, `pi-id-card`→`shield`, `pi-key`→`lock`,
`pi-verified`→`checkCircle`). **No new icon dependency.**

## The shape of the work

### Phase 0 — verify the current status

Baseline before touching anything, because the alignment's bar is "still works, looks different":

- `npm run build`, `npm run lint` in uaa-fe; `npm run test:ci` in the kit and console-ui (**311 + 701**).
- Bring up `lcl start -d --stack ui-kit uaa` and screenshot all four screens as the before.
- Re-run the QA cases verified last week — CON-01…CON-05, CON-07, CON-09 in
  `store-core/uaa/qa/uaa-qa.md`. Everything below has to keep passing them.

### Phase 1 — theme and shell

The largest visible change, and the one that makes the rest look designed.

- `src/index.html`: `data-theme="forest"` → `"light"`.
- **Extend the kit's `app-section-nav`** (`store-commons/ui-kit/ui/src/lib/section-nav/`) rather than writing
  a bespoke rail: `NavSection` gains `disabled?: boolean` and `disabledHint?: string`, and the component
  accepts **grouped** sections so one rail can carry Overview / Identity / Applications / System under one
  collapse toggle. It is already *"a vertical, router-bound section rail, collapsible to an icon strip"* with
  heading, active state, counts and an attention dot — the group and the disabled state are what it lacks.
  A disabled row is not a link, is `aria-disabled`, and carries the hint as its title.
- `layouts/admin-shell/` becomes rail + topbar, following `console-shell`'s shape (which composes
  `app-console-sidebar` + `app-console-toolbar` and handles the mobile toggle and scrim). Keep the language
  toggle and Sign out; add the breadcrumb and move the username into a menu.
- **Responsive is structural, not fluid:** the rail collapses to an icon strip, then to the off-canvas
  drawer + scrim console-shell already models. The mocks declare `min-width:1280px`; we will not.

### Phase 2 — Roles, master–detail

The smallest of the three, so the pattern is proved once cheaply.

- `features/roles/`: list pane (name, id) + detail pane. The detail is **one field** — the role name — plus
  Save and Delete, because that is the whole of `RoleDto`. New role opens the pane empty.
- The amber notice about renaming not re-issuing tokens stays; it is uaa's behaviour and still true.

### Phase 3 — Clients, master–detail

- `features/clients/`: list + detail, replacing the inline panel. The detail already exists and only moves.
- Add the **token lifetimes** (access, refresh) to the form, mapped to `ClientTokenSettings`. They arrive as
  ISO-8601 duration strings (`PT30M`), so the field takes and renders that, and the rest of `tokenSettings` /
  `clientSettings` keeps being preserved rather than edited.
- One KPI only — registered clients — or none. Three of the four the mock draws have no source.

### Phase 4 — Users, master–detail

- `features/users/`: list + detail. The detail carries first name, last name, roles and the enabled toggle
  (all on `UpdateUserRequest`), with **email and username read-only** and labelled as such.
- `app-roles-dialog` and `app-set-password-dialog` retire *from this screen* in favour of the pane and an
  inline password action — they stay in the kit, because console-ui still uses both.
- **Delete keeps its typed confirmation.**

### Phase 5 — sign-in

Take the mock's split layout — dark brand panel left, card right — and nothing else. The mock's provider
buttons, passkey, MFA step, "keep me signed in", forgot-password and create-account are all unbuilt.

**The form must stay a native POST to `/login`** with `name="username"` / `name="password"`: that is Spring
Security's `formLogin`, and the redirect it answers with is what resumes the OAuth2 flow. This is the single
easiest thing to break here.

### Phase 6 — lessons.md, i18n, QA

- **`uaa-fe/lessons.md`**, append-only, in console-ui's format (*Screen / What is missing / Decision /
  Expected contract*), with one entry per ❌ row in the table above. Cite it from the code with
  `TODO(lessons.md):` markers — but note **checkstyle fails the build on a bare `TODO`**, so the marker
  convention and the lint script that checks citations resolve
  (`store-core/console-ui/scripts/check-lessons-citations.mjs`) both need porting to uaa-fe's `npm run lint`.
- New i18n keys in **both** `src/locale/en.json` and `ar.json` — parity is checked, and AR is verified as
  **layout**, not only strings.
- Append the new cases to `store-core/uaa/qa/uaa-qa.md` §CON, tagged honestly.

## Verification

```bash
cd store-core/uaa/src/main/resources/uaa-fe && npm run build && npm run lint
cd ../../../../../../store-commons/ui-kit && npm run build && npm run test:ci     # 311, section-nav changed
cd ../../store-core/console-ui && npm run test:ci                                  # 701, must not move
./gradlew :store-commons:ui-kit:build :store-core:uaa:build
```

Then `lcl start -d --stack ui-kit uaa`, and in the browser:

| | |
|---|---|
| Rail | groups render; Users/Roles/Clients navigate; the four disabled rows are not links, are `aria-disabled`, and say why |
| Rail responsive | expanded → icon strip → off-canvas drawer; the scrim closes it |
| Roles | select → detail; create, rename, delete; **delete still gated on typing the name**; seed restored |
| Clients | select → detail; edit incl. token lifetimes; rotate secret; the option hints still come from `GET /clients/options` |
| Users | select → detail; edit names and roles; enable/disable; set password; delete typed-confirm; **email/username visibly read-only** |
| Sign-in | `super-admin`/`admin` signs in — the native POST still works — and a bad password shows the error |
| Themes | all four still render; Light is the default on a fresh profile |
| Arabic | the whole layout mirrors; `latin mono` fields (client ids, grant types, role names) stay LTR |
| Regression | CON-01…CON-05, CON-07, CON-09 still pass |

Finally, the mechanical design pass, once, over the changed UI:
`node /Users/ashraf/.claude/skills/impeccable/scripts/detect.mjs --json <changed targets>`.

## Out of scope, deliberately

Named so they are not read as oversights:

- **Dashboard, Audit log, Identity providers, Settings** — no backend. Rail entries only, disabled.
- **Create/invite a user.** `CreateUserRequest` and `AdminUserService.create` both exist, so this is cheap —
  but uaa-fe has no create flow today and adding one is a feature, not an alignment.
- **Import CSV**, realm switcher, notification bell, passkeys, MFA, social providers, active sessions,
  role permissions/inheritance, and every invented count.
- **console-ui adopting the grouped `section-nav`.** Its own sidebar has the same shape and could move onto
  the extended component; that is a separate PR, and this one must not move console-ui's 701 specs.
