# `@cvhome-saas/ui-kit` — one Angular library for console-ui and uaa-fe

Supersedes `.agents/plans/seller-core-shared-lib.md`, which described extracting a library out of
`seller-ui`. That app no longer exists (retired at tag `seller-ui-final`); `store-core/console-ui`
replaced it, and several of that plan's ideas — `NOTIFICATION_PORT`, `REQUEST_CONTEXT`,
`BrowserStorage`, a config `InjectionToken` — were applied **in place** inside `console-ui/src/app/core`
rather than as a library.

Repo convention puts plans in `.agents/plans/`, so the first commit in the worktree copies this file to
`.agents/plans/ui-kit-shared-angular-library.md` and deletes `.agents/plans/seller-core-shared-lib.md`.

---

## Context

Two Angular apps talk to the same platform and share nothing.

**`store-core/console-ui`** (474 `.ts` files) is the good one: Angular 20 standalone + SSR, Tailwind v4
on a four-theme CSS token layer, Transloco (en/ar, RTL), ~46 shared controls in `@shared/ui`, and a tier
system — `features → layouts → shared → api → core → models` — enforced by `no-restricted-imports`
blocks in `eslint.config.js`.

**`store-core/uaa/src/main/resources/uaa-fe`** is the pre-console world, frozen: Nebular 16, ngx-datatable,
jQuery, Bootstrap, ngx-toastr, ngx-echarts, module-federation, `@ngx-translate`. It re-implements
`AuthService`, an error/toast service, `Roles`, a `BaseTable`/`PageT` pagination base and four guards —
all of which console-ui also has, better. Its `/login` route is the **staff sign-in page for the whole
platform** (`AppSecurityConfig.formLogin(loginPage("/login"))` → `StaticController` → `static/index.html`),
so it is on every operator's critical path while looking nothing like the console it leads to.

The goal: extract console-ui's design system and infrastructure into a real library, and rebuild uaa-fe
on it. One definition of a text field, one error stack, one theme, two apps.

**Decisions locked with the user:**

| | |
|---|---|
| **Library scope** | Core infrastructure + the control catalogue + the theme layer + i18n plumbing + form helpers. **No** shared sign-in page — each app writes its own against the shared controls. |
| **uaa-fe** | Stays a full admin SPA (sign-in, users, roles, clients), rebuilt on the kit. Nebular, swimlane, jQuery, Bootstrap and ~15 other dependencies are dropped. Accepts that `/platform/users` and uaa-fe's users screen both exist. |
| **cua** | Out of scope. Its Thymeleaf `login.html` / `register.html` stay as they are; see Follow-ups. |
| **Layout** | The kit is its own Gradle+npm module. Each app keeps its own workspace and `node_modules`, and consumes the built package through a `file:` dependency. |

**Package name** `@cvhome-saas/ui-kit`, not `@cvhome/ui-kit` as sketched during the decision — it matches
the npm org that already publishes `@cvhome-saas/lcl`, so the name stays valid if the kit is ever pushed
to a registry. Nothing else about the choice changes.

---

## What moves, what stays

**Moves into the kit** (paths relative to `store-core/console-ui/src/app`):

| Kit entry point | From |
|---|---|
| `@cvhome-saas/ui-kit` | `core/config/console-core.config.ts`, `core/http/{crud.service,optional,request-context}.ts`, all of `core/errors/`, `core/auth/{auth.service,auth-guard.service,roles}.ts`, `core/platform/{browser-storage,clipboard}.ts`, `core/table/table.types.ts`, `core/routing/*`, `shared/state/snapshot.ts`, and the app-neutral models `models/{page,ui,locale,auth,uaa}.ts` |
| `…/ui` | `shared/ui/*` — all 46 **except** `charts/`, `export-button/`, `video-dialog/` (see below). Plus `shared/styles/{field,dialog-motion}.css` |
| `…/theme` | `core/theme/{theme.provider,theme.service}.ts` and `src/styles/theme*.css` (all six files) |
| `…/i18n` | `core/i18n/*` — loader, `locale.service`, `strict-missing.handler`, `lang-storage`, `calendar`, `translated-title.strategy` — plus kit-owned `en`/`ar` dictionaries |
| `…/forms` | `shared/forms/*`, `shared/validators/*`, `shared/i18n/*` |
| `…/uaa` | `api/uaa/admin-user.service.ts` + `models/uaa.ts`, and two new siblings (below) |

**Stays in console-ui**: every `features/**`, `layouts/**`, all of `api/` except `api/uaa`, the domain
models (`catalog`, `orders`, `billing`, …), `core/export/pdf-export.service.ts`, `core/reference/`,
`shared/auth/console-permissions.ts`, `shared/billing/`, `shared/directives/`, and the three heavy-dependency
widgets: `shared/ui/charts/*` (echarts), `shared/ui/export-button/` (jsPDF + modern-screenshot),
`shared/ui/video-dialog/`. Keeping those out is what holds the kit's peer set down to Angular + rxjs +
Transloco + `@angular/cdk`, so uaa-fe never installs echarts to render a login form.

**`…/uaa` is the one domain entry point, deliberately.** Both consoles administer the same uaa; without it
uaa-fe would re-implement the `AdminUserService` console-ui already owns, which is the exact duplication
this work exists to remove. It gains `admin-role.service.ts` and `admin-client.service.ts` (new — uaa's
`AdminRoleController` and `AdminClientController` have no TypeScript client today). If the kit later needs
to stay strictly UI-only, this entry point is the seam to cut; nothing else depends on it.

---

## Module layout

```
store-commons/ui-kit/                  new Gradle module — `store-commons:ui-kit` in settings.gradle
  build.gradle                         node plugin; `build` → npm_run_build; `check` → npm_run_lint
  package.json                         @cvhome-saas/ui-kit; ng-packagr; Angular/rxjs/Transloco as peers
  angular.json                         one project, builder @angular/build:ng-packagr
  ng-package.json                      dest ../dist/ui-kit
  tsconfig.json  tsconfig.lib.json  tsconfig.lib.prod.json  tsconfig.spec.json
  eslint.config.js  .stylelintrc.json
  scripts/{check-css-tokens,check-css-vocabulary}.mjs     moved from console-ui
  DESIGN.md                                               moved from console-ui
  src/public-api.ts        → @cvhome-saas/ui-kit
    lib/{config,http,errors,auth,platform,table,routing,state,models}/
  ui/    ng-package.json + src/public-api.ts    → @cvhome-saas/ui-kit/ui
  theme/ ng-package.json + src/public-api.ts    → @cvhome-saas/ui-kit/theme   (+ css/ shipped as assets)
  i18n/  ng-package.json + src/public-api.ts    → @cvhome-saas/ui-kit/i18n
  forms/ ng-package.json + src/public-api.ts    → @cvhome-saas/ui-kit/forms
  uaa/   ng-package.json + src/public-api.ts    → @cvhome-saas/ui-kit/uaa
  dist/ui-kit/                         the built package the apps link to
```

`store-commons/` holds Java libraries today, but it is the tree that means "shared by store-core and
store-pod", which is exactly what this is — and `store-pod:landing-ui` is the precedent for a non-Java
npm module registered in `settings.gradle`.

Secondary entry points are used rather than one flat barrel because the theme and the controls have
different peer requirements, and because a cycle between two entry points is a hard ng-packagr error —
which is the boundary check we want for free.

Consumer `package.json`:

```jsonc
// store-core/console-ui
"@cvhome-saas/ui-kit": "file:../../store-commons/ui-kit/dist/ui-kit"
// store-core/uaa/src/main/resources/uaa-fe
"@cvhome-saas/ui-kit": "file:../../../../../../store-commons/ui-kit/dist/ui-kit"
```

### The three settings that make linking work

1. **`"preserveSymlinks": true`** in every consumer's `angular.json` `build`, `test` and `server`
   options. `file:` makes npm symlink the package; without this Angular resolves through the symlink to
   the real path and then hunts for `@angular/core` from `store-commons/ui-kit/`, where there is none.
   This is the single setting that decides whether the whole approach works — Phase 1 exists to prove it
   before anything has moved.
2. **`dist/ui-kit` must exist before `npm install` runs** in a consumer: npm reads the target's
   `package.json` at install time. Enforced three ways — a `"kit"` script in each consumer
   (`npm --prefix <rel> run build`) that `"build"` and `"start"` chain, `npmInstall.dependsOn(':store-commons:ui-kit:build')`
   in each consumer's `build.gradle`, and a `prepare` step in `lcl.yml`. After the first install the
   symlink means a kit rebuild is picked up with no reinstall.
3. **The theme CSS is `@import`ed, never listed in `styles`.** `theme.css` and `theme-bridge.css` are
   Tailwind v4 `@theme` / `@utility` sources; they must be processed in the same PostCSS pass as
   `@import 'tailwindcss'`. So `src/styles.css` keeps its `@import 'tailwindcss'` and follows it with
   `@import '@cvhome-saas/ui-kit/theme/css/theme.css'` and the five theme files, in the order the current
   file documents (Forest first — it claims bare `:root`). If the bare specifier does not resolve through
   Tailwind's importer, fall back to a relative path into `dist/ui-kit/theme/css/`; verify this in Phase 1,
   not later.

**The controls do not need Tailwind content-scanning.** Every `shared/ui` component styles itself with
semantic class names and `var(--token)` in its own stylesheet — zero Tailwind utility classes in any of
their templates, verified across all 46. So no consumer needs an `@source` line pointing at the kit. What
a consumer does need is Tailwind v4 itself, to emit the token layer.

---

## API changes made during the move

Four, all small, all worth doing while the files are in flight:

- **`CONSOLE_CORE_CONFIG` → `UI_KIT_CONFIG`** (`UiKitConfig`, same three fields), plus a
  `provideUiKit(config, ...features)` environment provider following Angular's feature idiom, with
  `withNotifications(impl)` for `NOTIFICATION_PORT` and `withRequestContext(impl)`.
- **`REQUEST_CONTEXT` gains a no-op default factory** returning `{params: () => ({})}`. Today it has no
  default and console-ui provides `SelectedStoreRequestContext`; uaa-fe has no selected store, and
  without a default every `CrudService` call there would fail at injection. console-ui keeps overriding it.
- **`Roles` keeps console-ui's five flags** (`isSuperAdmin`, `isSupport`, `isOrgAdmin`, `isStoreAdmin`,
  `isStoreModerator`). uaa-fe's one-flag version is dropped.
- **Kit dictionaries.** Kit code reads `shared.*` (135 call sites), `errors.*` (11) and — from
  `user-admin-table` — `platform.*` (21) and `shell.*` (9). Those namespaces move to
  `ui-kit/i18n/dictionaries/{en,ar}.ts` and are exported as objects; each app's `TranslocoDictionaryLoader`
  deep-merges kit dictionary under app dictionary, so the app can still override a string and nothing has
  to be copied as an asset at build time. `platform.*`/`shell.*` keys used only by `user-admin-table` move
  under `shared.userAdmin.*` on the way, so the kit does not own a namespace named after one app's route.

---

## Steps

Cut the worktree first — `git fetch origin && git worktree add .claude/worktrees/refactor-ui-kit -b refactor/ui-kit origin/main`
— and work entirely inside it, with its own stack (`lcl start -d --stack ui-kit`).

**Checkpoint C** = `npm run build && npm run lint && npm run test:ci` in console-ui, plus
`npm run build && npm run lint` in the kit. Commit at every checkpoint.

**Phase 0 — baseline.** Run Checkpoint C on untouched `HEAD` and record it, plus `dist/console-ui` bundle
sizes and screenshots of the dashboard, the product form and the orders table in all four themes. A pure
refactor's only real assertion is "identical", and you cannot assert it against a memory.

**Phase 1 — scaffold the kit and prove the pipeline, empty.** Create `store-commons/ui-kit` with the files
above, `settings.gradle` entry, and exactly two things in it: a trivial exported constant, and the six
theme CSS files (moved out of `console-ui/src/styles/`, shipped as ng-packagr `assets`). Wire console-ui's
`file:` dependency, `preserveSymlinks`, the `"kit"` script, the Gradle `dependsOn`, the `lcl.yml` prepare
step, and the `@import` rewrite in `src/styles.css`. → Checkpoint C, **and the four theme screenshots must
be pixel-identical to Phase 0.** Nothing else in this plan is worth starting until this phase is green;
every packaging risk lives here.

**Phase 2 — write the mover, then move the core tier.** A Node script in the scratchpad taking an
`{oldPath → newPath}` table: `git mv` each file, then rewrite import specifiers across every `.ts` and
`.html` under both projects. **Resolve specifiers to absolute paths before matching** — never match on
basename. Merge duplicate `from '@cvhome-saas/ui-kit'` lines; handle `export … from`, `import type` and
dynamic `import()`. Dry-run and read the diff before applying.

Then move the core tier: config, http, errors, auth, platform, table, routing, state, neutral models —
~219 import lines across 153 files. Do the `UI_KIT_CONFIG` rename, `provideUiKit`, and the `REQUEST_CONTEXT`
default here. Move the co-located specs (`api-error.interceptor`, `problem-detail.parser`, `form-error.utils`,
`clipboard`, `snapshot`) and confirm the kit's own `ng test` picks them up. → Checkpoint C.

**Phase 3 — move the control catalogue.** All of `shared/ui` except charts, export-button and video-dialog;
~691 import lines across 153 files — the largest mechanical step, and the one the mover was written for.
Move `field.css` and `dialog-motion.css` with them, and keep the rule that a feature using `.field-grid`
lists `field.css` in its own `styleUrls` (`scripts/check-css-vocabulary.mjs` enforces it and moves too).
Move `DESIGN.md`, `check-css-tokens.mjs` and `check-css-vocabulary.mjs` into the kit and run them there —
then keep running them in console-ui as well, since features still write CSS against the same tokens.
→ Checkpoint C.

**Phase 4 — i18n, forms, uaa, and the tier rules.** Move `core/i18n/*`, `shared/forms/*`,
`shared/validators/*`, `shared/i18n/*`; build the kit dictionaries and the merging loader; move
`api/uaa/admin-user.service.ts` and add `admin-role.service.ts` + `admin-client.service.ts`. Rewrite
console-ui's `eslint.config.js`: `@shared/ui` is mostly gone, so the floor is now the kit — the rules
become "features may not import features", "layouts may not import features", "api may not import UI",
and a new block forbidding any kit entry point from importing `@api/*`, `@features/*` or `@layouts/*`
(mirrored inside the kit's own `eslint.config.js`, where such an import cannot even resolve).
Teach `scripts/i18n-missing.mjs` and `i18n-unused.mjs` about the kit's namespaces. → Checkpoint C.

**Phase 5 — rebuild uaa-fe.** Replace `store-core/uaa/src/main/resources/uaa-fe/src` wholesale:

- Angular 20 standalone (no NgModules), Tailwind v4 + PostCSS, Transloco en/ar with RTL, the kit.
- Tiers mirroring console-ui: `src/app/{core,api,features,layouts,models}`, `src/styles.css`,
  `src/locale/{en,ar}.json`, `src/environments/`.
- Four routes: `sign-in` (public, the `formLogin` page), and `users` / `roles` / `clients` behind an
  admin shell guarded by `authGuard`.
- Built from the kit: `app-text-field`, `app-form-field`, `app-select`, `app-checkbox`, `app-panel`,
  `app-page-header`, `app-data-table`, `app-pagination`, `app-search-box`, `app-toast`, `app-busy-overlay`,
  `app-empty-state`, `app-load-error`, `app-confirm-dialog`, and — the reason `…/uaa` exists —
  `app-user-admin-table`, `app-roles-dialog`, `app-set-password-dialog` against `AdminUserService`.
- `package.json` drops `@nebular/theme`, `@nebular/eva-icons`, `eva-icons`, `@swimlane/ngx-datatable`,
  `jquery`, `bootstrap`, `bootstrap-icons`, `@ng-bootstrap/ng-bootstrap`, `@popperjs/core`, `ngx-toastr`,
  `ngx-echarts`, `ngx-lightbox`, `ngx-file-drop`, `@fortawesome/fontawesome-free`,
  `@angular-architects/module-federation`, `ngx-build-plus`, `es-module-shims`, `ngx-cookie-service`,
  `libphonenumber-js`, `moment`, `@ngx-translate/core`, `@ngx-translate/http-loader`; adds
  `@jsverse/transloco` (+ `-locale`, `-messageformat`, `-persist-lang`), `tailwindcss`,
  `@tailwindcss/postcss`, `postcss`, `@angular/cdk`, and the kit.
- `public/` loses `css/`, `js/`, `webfonts/` and the jQuery/Bootstrap vendor bundles; the `assets/i18n/`
  files are replaced by `src/locale/`.
- `store-core/uaa/build.gradle`: `npmInstall.dependsOn(':store-commons:ui-kit:build')`. The existing
  `copyAngularApp` → `processResources` → `exclude 'uaa-fe/**'` flow is unchanged.
- New `store-core/uaa/http/{admin-user-api,admin-role-api,admin-client-api}.http` — those controllers have
  no runnable requests today, and this work is what makes the gap visible.

**Phase 6 — verification and QA.** Below.

---

## Verification

```bash
# from the worktree root
./gradlew :store-commons:ui-kit:build
./gradlew :store-core:console-ui:clean :store-core:console-ui:build
./gradlew :store-core:uaa:clean :store-core:uaa:build          # builds and embeds uaa-fe
./gradlew build -x test -x check                                # nothing else regressed

cd store-commons/ui-kit && npm run build && npm run lint
cd store-core/console-ui && rm -rf node_modules dist && npm install && npm run build && npm run lint && npm run test:ci
cd store-core/uaa/src/main/resources/uaa-fe && rm -rf node_modules dist && npm install && npm run build
```

The `rm -rf node_modules` runs are not ceremony: they are the only way to catch a `file:` link that
happens to work because a stale symlink survived.

Boundary gates — each **must return nothing**:

```bash
grep -rn "@features/\|@layouts/\|@api/\|environments/environment" store-commons/ui-kit/{src,ui,theme,i18n,forms,uaa}
grep -rn "@nebular\|@swimlane\|jquery\|bootstrap\|ngx-toastr\|@ngx-translate" \
     store-core/uaa/src/main/resources/uaa-fe/src store-core/uaa/src/main/resources/uaa-fe/package.json
grep -rn "echarts\|jspdf\|modern-screenshot" store-commons/ui-kit
```

Then `ls store-commons/ui-kit/dist/ui-kit` shows `fesm2022/`, `index.d.ts`, `theme/css/` and one folder per
secondary entry point each with its own `package.json`. Compare `dist/console-ui` bundle sizes against the
Phase-0 baseline — a large jump means an entry point pulled in something it shouldn't.

### End-to-end QA

Bring up the worktree's own stack — `lcl start -d --stack ui-kit`, read the live ports from
`lcl urls --stack ui-kit` — and drive both apps through the gateway.

**console-ui — the bar is *identical*, since nothing about it changed but where its code lives:**

| Flow | Watches |
|---|---|
| All four themes on dashboard, product form, orders | The token layer through the packaged `theme/css/` — compare against the Phase-0 screenshots |
| Sign in → dashboard → switch store → reload | `AuthService`, `SessionService`, `BrowserStorage`; **network tab: every request still carries `store=` and `pod=`** — the `REQUEST_CONTEXT` default must not have displaced console-ui's provider |
| Product list → paginate → sort → edit → save | `CrudService`, `snapshot()`, the moved `data-table`/`pagination` |
| Force a 4xx (duplicate SKU) | `ApiErrorService` → `NOTIFICATION_PORT` → toast copy and field errors **byte-identical**, not "System Error" — proves the merged kit/app dictionary resolves `errors.*` |
| Force a 401 (clear the session cookie mid-session) | one redirect, not N — the `SessionService` latch |
| Switch to Arabic | RTL, `lang-storage`, the `@custom-variant rtl` still applying to kit component CSS |
| SSR routes via `npm run serve:ssr:console-ui` | no `localStorage is not defined` from Node — `BrowserStorage` still guarding after the move |

**uaa-fe — new code, so the bar is *works*:**

| Flow | Watches |
|---|---|
| `/login` as a demo staff user → redirected back to the console | The page Spring Security actually serves; the whole platform's sign-in |
| Bad password, then a locked account | Error copy through the kit's error stack, not a blank field |
| Users: list → paginate → search → create → edit → disable → reset password | `AdminUserService` from `…/uaa`, `app-user-admin-table`, `app-set-password-dialog` |
| Users: grant and revoke a role | `app-roles-dialog` emitting a diff, and both `POST …/roles` and `POST …/roles/remove` firing |
| Roles: list → create → edit → delete | New `AdminRoleService`, and the new `.http` blocks run clean |
| Clients: list → create an OAuth2 client → edit → delete | New `AdminClientService`; then actually authorize with the created client |
| Consent page after a new client's first authorization | `consent.html` is Thymeleaf and untouched — confirm the SPA rewrite did not break the route it sits beside |
| Arabic + all four themes | The kit's theme layer working in a second app, which is the whole point |

Permission and tenant-isolation behaviour is server-side and unchanged, but repeat the user-edit flow as a
non-super-admin to confirm uaa's authorization still 403s and the kit's error stack renders it as a
permission failure rather than a generic one.

Append the console-ui cases to `store-core/console-ui/qa/console-ui-qa.md` and the uaa-fe cases to
`store-core/uaa/qa/uaa-qa.md`, each tagged **[verified] / [unit only] / [not verified]**. The kit is a
library and gets no QA file of its own — QA is end to end.

---

## Follow-ups (separate PRs, deliberately out of scope)

1. **cua.** Its `login.html` and `register.html` are 197- and 177-line Thymeleaf files with inline hex
   colours, and they are the shopper's first impression of every storefront. Two routes forward: ship a
   compiled `@cvhome-saas/ui-kit/css` bundle (tokens + a `.field`/`.btn` vocabulary) that the templates
   link, keeping the login server-rendered; or build a `cua-fe` SPA mirroring uaa's, which additionally
   needs a `StaticController` and a login-context endpoint, because the store name, logo, social providers
   and `client_id` are Thymeleaf model attributes today.
2. **Retire the duplicate user admin.** console-ui's `/platform/users` and uaa-fe's users screen now render
   the same table against the same service. One of them should go; deciding which is a product call, not a
   refactor.
3. **Publish the kit.** Nothing needs it while both consumers are in this repo, but `@cvhome-saas` is
   already a real npm org and the `file:` link is the only thing tying the apps to this checkout.


---

## What actually happened

Written after the fact. The plan held; these are the places it was wrong, and why.

**Phases 3 and 4 merged.** The dependency graph decided the grouping, not the plan: `tone.ts` needs the theme
provider, the date pickers need `calendar.ts`, and `field-error` needs `validation-messages`. `/ui` could not
move without `/theme`, `/i18n` and `/forms` moving with it.

**"The controls need no Tailwind content-scanning" was wrong.** I checked it in Phase 1 by grepping for
utility classes in `class="…"` attributes and concluded there were none. There are: badge, tag-input, tree and
the chart legends use `inline-flex`, `gap-1`, `ring-1`, `bg-chart-3-wash`, `group-aria-expanded:rotate-180`.
ng-packagr does not run Tailwind over component CSS, so a consumer must add
`@source '../node_modules/@cvhome-saas/ui-kit'` or ~2.6 kB of utilities silently vanish. Only a byte
comparison of the emitted stylesheet showed it — the build, the specs and the page all looked fine.

**`shared/styles/field.css` did not move.** 29 features list it in `styleUrls` by relative path and no kit
control needs it. `dialog-motion.css` did move, because `app-confirm-dialog` needs it; console-ui's copy is now
a one-line `@import` so its eleven dialogs and `check-css-vocabulary.mjs` are untouched.

**npm does not fail on a missing `dist/ui-kit`.** The plan said a consumer's `npm install` would fail with
ENOENT. It exits 0 and writes a dangling symlink; the failure surfaces much later as
`Could not resolve "@cvhome-saas/ui-kit"` from the build, which reads like a broken import.

**`core/table/table.types.ts` was deleted** rather than moved. It existed to re-export `models/page` under the
`@core/table` alias so callers need not change their import; behind one entry point every caller writes
`@cvhome-saas/ui-kit` whichever file the type sits in, so it had become pure indirection.

**Two bugs that only end-to-end QA could find**, both from the same root: uaa serves its console *itself*, so
every assumption the library had absorbed from "console-ui reaches uaa through the gateway" was wrong in a way
that answers **200**. `/api/v1/auth/me` returns a different shape on each side, and `/uaa` is the gateway's
prefix — asking for it on uaa lands on `StaticController` and returns the SPA's HTML. Neither could fail a
unit test. A third, `ClientAuthMethod.from` recursing into itself, was uaa's own and had survived because the
endpoint had no caller.

**Not done, deliberately:** `strictNullChecks` is not flipped anywhere (it was already `strict: true` in both
apps, so the plan's follow-up 1 was moot); console-ui's `/platform/users` and uaa-fe's Users screen both still
exist, which the locked decision accepted; cua is untouched.
