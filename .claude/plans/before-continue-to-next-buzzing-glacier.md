# console-ui — the alignment pass before Module 7

## Context

Six modules of the seller-ui → console-ui migration have shipped (marketing/auth, shell, dashboard,
orders, store management, catalogue). Each was built against its own plan and QA'd on its own, and the
shared layer grew by accretion — a control was added when a module needed it and never retrofitted to
the modules before it. The app is feature-complete to Module 6 and **green** (lint clean, 539/539 specs,
`tsc` clean, en/ar at exact 1352-key parity, zero Tailwind palette classes, zero hardcoded template
strings, zero bare `TODO`s), but it does not yet have *one* way of doing the things every module does.

Three read-only audits (features; shared layer + i18n; architecture/routes/tests) found the same shape of
problem everywhere: **the features re-implement the shared library instead of using it.** 7,690 lines of
feature CSS against 4,345 of shared UI CSS; five copies of the field vocabulary; nine `.load-error`
blocks in four visual shapes; seven empty-state class names; six "fade-up" keyframes and six spinners;
four names for the icon-row button; 14 native `<select>`s beside a themed `app-select`; four dead shared
components (`app-button`, `app-card`, `app-menu`, `app-avatar`) and a dead `app-form-field`; the facade
snapshot pattern copy-pasted into seven facades under six names for "busy"; `optional` helpers copied
four times with two meanings; `shared/validators/` and `shared/pipes/` empty while validators are
imported across features; `models/` importing *upward* 13 times through an eslint gap; `layouts/billing`
forming the app's only tier cycle; the `api/` tier 4 % tested; two broken `lessons.md` citations; no
architecture document at all — `README.md` is CLI boilerplate.

This pass makes the six modules agree with each other, hoists what each re-implemented into `shared/`,
deletes what nothing uses, closes the lint gaps that let the drift in, and writes the base architecture
down so Module 7 (payments) and every module after it is planned against a documented contract rather
than the previous module's habits.

## Decisions (settled with the user)

| Question | Decision |
|---|---|
| Form controls | **Shared components everywhere.** Build the missing text controls and migrate every raw `<input>`/`<textarea>`/`<select>` in every feature. |
| Delivery | **One commit per concern, then a Chrome QA pass** over every console page in en/ar, all three themes, 1440/900/420, followed by a fix commit. |
| Docs | **Yes** — `ARCHITECTURE.md` + `CLAUDE.md` in console-ui; README rewritten to point at them. |

Standing rules carried over unchanged: seller-ui and store-pod are not modified; no fixture stands in for
a real answer; every unbacked block carries `TODO(lessons.md)`; `lessons.md` is append-only.

---

## What the audits found — grouped

Full detail lives in the audit transcripts; this is the working list the commits below consume.

**A. Controls and field vocabulary**
- `app-form-field` (label-only wrapper) has 0 uses; `app-button`, `app-card`, `app-menu`, `app-avatar` have 0 uses. `app-button` also contradicts the global `.primary-action` family (pill vs `--radius-md`).
- `.field / .control / .field-label / .field-grid / .field-hint` defined five times, drifted: `catalogue/components/editor-card.css`, `product-form/components/editor-card.css` (42 diff lines of 238), `store-management/components/settings-card.css`, `create-store/create-store.css:60-95` (the only one with `.control.invalid`), `auth/auth.css:12-20` (element selectors, no wrapper).
- Raw controls: store-management 23 inputs / 7 selects / 2 textareas; create-store 11 / 7 / 0 (a field-for-field clone of details-section); catalogue 7 inputs; product-form 7 / 1 / 1; auth 5; marketing 3 + 1 textarea; order-details 1 select + 1 textarea + 2 raw `<table>` + a hand-rolled `role="dialog"` modal; `payments-section.ts` and `social-login-section.ts` use raw inputs for credentials while `app-secret-field` is used three lines away.
- Icon-row button: `.row-action` (products, orders, tree), `.icon-action` (slider, domain), `.icon-button` (dashboard, toolbar), `.invoice-action` (order-details).
- Async "is it taken?" indicator drawn three ways (catalogue/product-form vs create-store vs social-links); the validator itself written three times (`catalogue-form.service.ts:134`, `product-form.service.ts:175`, `create-store.facade.ts:561`, + `dnsNotPointing` same shape).
- `field-error` takes a single `fallback` string — no shared message map, so 30 call sites hand-translate `required` and cannot tell it from `maxlength`. `required` asterisk: 15 × with `aria-hidden`, 11 × without (create-store, `copy-fields.ts:65`).
- `clearServerErrorsOnChange` missing in `auth.facade.ts` and `create-store.facade.ts` — a bound server error never clears (the bug `form-error.utils.ts:62-66` describes). marketing binds nothing.
- `shared/validators/` empty; `phoneNumber`, `socialProfileUrl`, `credentialsWhenEnabled`, `defaultLanguageIsSupported` live in `store-settings-form.service.ts` and are imported by create-store; `passwordsMatch` in auth; `slugify`/`CODE_PATTERN` in catalogue; `SKU_PATTERN`, `SLUG_PATTERN`, `CUSTOM_DOMAIN_PATTERN`, `SOCIAL_URL_PATTERN` scattered.

**B. States, layout and motion**
- `.load-error` ×9 in 4 shapes, 4 retry-button looks, all keyed `dashboard.tryAgain`. `app-notice-bar` already exists.
- `.first-load` ×8 with heights 26rem / 24rem / 60vh; billing has none.
- `.panel-pad` ×3 with 3 values because `panel.css:41 .panel-body` has no padding.
- Empty state: `.table-empty` (two different designs in orders vs products), `.list-empty`, `.editor-empty`, `.picker-empty`, `.member-empty`, `.empty-note`, `.empty`, `.plan-placeholder`, `.related-empty`.
- Keyframes: `rise`, `panel-in` ×2, `section-in` ×2 (10px vs 14px), `order-rise`, `billing-rise`, `create-store-rise`, `tab-panel-enter`; spinners `busy-spin`, `export-spin`, `picker-spin`, `autocomplete-spin` ×2, `domain-spin`, `create-store-spin`; `popover-in` ×4. Entry mechanism split: `animate.enter` (catalogue, product-form, shared) vs bare CSS `animation:` (seven pages); products has no entry motion, orders does.
- `billing.css` has no `:host { display: contents }` (every other console page has it), adds its own `margin-block-start: 1.75rem`, no tab-switcher fallback under its section-nav (store-management has one). `order-details` is the only console page without `app-page-header`; first-run puts it below a notice bar. Nine bespoke body-wrapper class names.
- `.split` two-pane grid defined 6×; `.progress-card/.progress-head/.run-track` duplicated between first-run and create-store; first-run hand-rolls a stepper (`<ol class="task-list">`) where `app-stepper` exists.
- Header fed six different ways (`heading().title/context`, `heading()/subtitle()`, `title()/context()`, …).
- Tab switcher bound `[(active)]` in orders vs `[active]/(activeChange)` elsewhere; orders/products tab+filter state in memory while catalogue/store-management route it. `products.css:28` dead duplicate `.panel-controls`.
- Colour literals: `order-details.css:839-1047` (invoice "paper", 17 hexes), three scrims hardcoded while `--scrim` exists, `locale-chips.css:40` raw shadow; ad-hoc custom properties `--icon-size` (67 definitions), `--status-ink` (5).
- RTL: marketing 16 and auth 7 physical-direction properties; shared date-picker / date-range-picker chevrons don't flip; `marketing.html` `arrowRight` ×3 unflipped; `app-icon` `flip` is opt-in.

**C. Facades, services, state**
- `rxResource` + `linkedSignal(next ?? prev?.value)` + `isLoading` + `error as Error|undefined` + `isEmpty` + `retry()` copy-pasted in 7 facades. Busy named `saving` / `isSaving` / `submitting` / `working` / `isDeleting` / `uploading`. Toast injected as `toast` (10) vs `toasts` (3); raised from the facade in 7 features, from the component in 5 (order-details does both). Every call site does its own `transloco.translate`.
- `optional` copied in products / product-form / catalogue (→ `null`) and store-settings (three variants, one → `[]`).
- Facade DI: 9 page facades `providedIn: 'root'`, 3 component-provided (order-details, product-form, billing). Nothing outside a feature injects its page facade.
- billing and create-store have no `*.api.service.ts` — facades call `@api/*` directly; create-store builds its 20-control form inline in a 578-line facade.
- Cross-feature imports (3): `create-store.facade.ts:24` ← store-management validators; `catalogue.facade.ts:27` ← `product-form/services/product-search`; `product-form.facade.ts:25` ← `products/services/products-cache`.
- `layouts/billing/` (BillingFacade + plan-dialog) is a feature in the layout tier; `features/billing` imports it, it imports the shell facade; `console-shell.ts:64-71` routes around the cycle.
- Dirty tracking computed three ways (`toSignal(form.events)`, `copyStamp` counter, `toSignal(form.valueChanges)`); confirm state machine re-implemented in four facades/components.

**D. Architecture, routes, tests, hygiene**
- `models/` imports `@shared`/`@core` 13× (`IconName`, `Tone`, `PageT`, `KpiDatum` from a component file, `ConsoleLocale`); eslint's models block omits those groups. Nothing bans `layouts → features` or `features → features`.
- `app.routes.server.ts`: `subscription/**`, `public/subscription/**`, `external-logout-link` undeclared (work only via `**`); three declaration styles. Route-param validation done three ways (`computed`+effect, imperative effect, in-page key guards); `isSectionKey`/`CATALOGUE_TABS`/`nameIn`/`TAB_TONE`/`SECTION_KEYS` exported from models and **uncalled** — verify whether they are the abandoned first attempt before deleting. Route `data` untyped (four `as string | undefined` casts). Breadcrumb single-level.
- Fixtures still shipping: `first-run.fixture` (entire getting-started page behind a fake-latency `FirstRunApi`), `console.fixture` (entire nav tree, also read by `testing/console-api.fake.ts`), `create-store.fixture`, `marketing.fixture`.
- Dead: `core/auth/user.service.ts`, `core/table/table-state.service.ts`, `core/table/table-events.ts`, `core/config/console-core.config.ts` (whole files); `codeToKey`/`categoryToKey`, `ISO_3166_ALPHA2`, `isAfter`, `AuthUser`, `HttpParamsLike`/`RequestOptions`, `ThemeOption`, `ClientErrorCategory`, `toneInk`, `CurrencyDisplay`, `DropPosition`, `ToastMessage`, `BarDatum`, `SplitDocument`; 37 dead i18n keys in both locales (`createStore.region.*` 12, `marketing.entitlement.*` 21, `legal.{terms,privacy}.*` 4) matching dead `HostingRegionOption` and `Entitlements` types; 12 empty directories; `create-store.facade.spec.ts` misplaced; `shared/directives/image-loaded.ts` exports `ImageBroken`; `actions.*` namespace has one key.
- Tests: api tier 1/24 specs; 28/43 shared/ui components untested incl. `panel` (25 uses), `field-error` (20), `icon` (52), `notice-bar`, `toggle`, `page-header`, `busy-overlay`, `badge`, `confirm-dialog`, `secret-field`; `features/subscription` (paid-checkout return page with a documented prior 404) untested; three component specs bypass `@testing/transloco-testing`.
- `lessons.md` citations: `billing.html:160` quotes a heading that does not exist; `reference-data.service.ts:36` quotes a renamed one. No check exists (31 of ~56 citations wrap across lines). `/** Ported from … */` leading-line convention: 0 % adherence in form, 83 % in substance. `npm run i18n:missing` exists, no `i18n:unused`, neither in CI. No stylelint, so the Token Rule is unenforced.
- Docs: no `ARCHITECTURE.md`, no `CLAUDE.md`; tier rule lives in an eslint comment; `:host { display: contents }` rule lives in one CSS comment.

---

## The plan — nine commits, then QA

Each commit leaves `npm run build && npm run lint && npm test` green. Order matters: shared primitives
land before the features that consume them; deletions land last among the refactors so nothing is
deleted while still referenced.

### Commit 1 — `chore(console-ui): the dead layer, and two broken citations`

Pure removal + hygiene; no behaviour change.
- Delete `shared/ui/{button,card,menu,avatar}/` (keep `shared/ui/form-field/` — rebuilt in commit 2).
- Delete `core/auth/user.service.ts`, `core/table/table-state.service.ts`, `core/table/table-events.ts`, `core/config/console-core.config.ts`; delete the unused exports listed in D (after `grep` re-confirms each).
- Delete the 12 empty directories (`features/auth/{constants,state}`, `features/{dashboard,marketing}/{components,constants,state}`, `core/layout`, `core/testing`, `shared/pipes`); `shared/validators/` stays — commit 2 fills it.
- Delete the 37 dead keys from `src/locale/en.json` and `ar.json`, plus `HostingRegionOption` (`models/create-store.ts`) and `Entitlements` (`models/billing.ts`) if nothing else reaches them. Fold `actions.*` (1 key) into `shared.actions.*`; move `dashboard.tryAgain` → `shared.actions.retry` (9 call sites).
- Fix the two citations: `features/billing/billing.html:160` → point at the real heading (or add the missing heading to `lessons.md` if the gap is genuine — check git log for the intended one); `core/reference/reference-data.service.ts:36` → "Store management — no reference lists for countries, currencies or storefront languages".
- Move `features/create-store/create-store.facade.spec.ts` → `facades/`. Rename `shared/directives/image-loaded.ts` → `image-broken.ts`. Delete `products.css:28-32` (dead `.panel-controls`) and `auth.css:16-17` (dead `.field-error` rules).
- Re-check the 8 suspicious en==ar values (`dashboard.heading.context`, `products.heading.context`, `shared.export.pageOfPages`, `firstRun.guide.meta`, `marketing.footer.copyright`, `billing.invoices.pdf`, `catalogue.copy.counter`, `shared.richText.linkPlaceholder`) and translate the ones that are copy.

### Commit 2 — `feat(console-ui): one field vocabulary`

The shared primitives every later commit migrates onto. All new components: standalone, signals,
`ChangeDetectionStrategy.OnPush`, spec with `@testing/transloco-testing`, logical properties only,
tokens only, `animate.enter/leave` for any motion.

New in `shared/ui/`:
- **`form-field`** (rebuilt, same selector `app-form-field`): `label!`, `hint`, `required`, `control: AbstractControl`, `for`/generated id wired onto the projected control, optional `[fieldAction]` slot (e.g. the async-check indicator). Renders label + projected control + `app-field-error` + hint in one encapsulation scope so the five `.field` stylesheets become zero. Exposes `--field-gap` only.
- **`text-field`** (`app-text-field`, CVA): `type` = text|email|url|tel|search|password, `placeholder`, `prefix`/`suffix` text, `maxlength` + counter (`catalogue.copy.counter` already exists), `invalid`, `disabled`, `dir` passthrough (`unicode-bidi: plaintext` for Latin data inside RTL), optional `check: 'idle'|'pending'|'free'|'taken'` indicator — the one async-uniqueness affordance, replacing the three drawings. Password type carries the reveal toggle so `sign-in`/`sign-up` stop hand-rolling it.
- **`textarea-field`** (`app-textarea`, CVA): `rows`, `maxlength` + counter, `invalid`, `dir`. (Rich prose keeps `app-rich-text`.)
- **`empty-state`** (`app-empty-state`): `icon`, `title`, `message`, projected actions. Built from `orders.css:157-202` (the richer of the two `.table-empty`s).
- **`load-error`** (`app-load-error`): wraps `app-notice-bar tone="danger"` + one `secondary-action` retry → `(retry)`. Copy key `shared.actions.retry`. Replaces the nine blocks.
- **`search-box`** (`app-search-box`): icon + `<input type="search">` + sr-only label, `value` model, `placeholder`, `debounceMs`. Replaces `orders.html:52-62`, `products.html:80-93`, `group-tab.html:170`.
- **`locale-switcher`** (`app-locale-switcher`): promote `features/catalogue/components/locale-chips/` (radiogroup, roving tabindex, translated-set dots) with inputs `languages`, `active` model, `filled: ReadonlySet<string>`, `label`. Replaces essentials-step's `.language-track` and home-section's `tab-switcher.lang-track`.
- **`icon-action`**: not a component — one global `.icon-action` class added to the action vocabulary in `styles.css` (square `ghost-action`, `--icon-size` aware), replacing `.row-action` / `.icon-action` / `.icon-button` / `.invoice-action`. A class because it decorates `<button>` and `<a>`, same reason as the rest of the family.
- **`panel`**: `.panel-body` gets a default `padding: 1.25rem` (and a `padless` input for tables), removing the three `.panel-pad`s. **`busy-overlay`**: new `reserve` input (`'page'` = 60vh, `'panel'` = 26rem) rendering the placeholder slab itself, removing the eight `.first-load`s. **`icon`**: `flip` inferred from a `DIRECTIONAL_ICONS` set (arrows, chevrons, back/forward) with the input kept as an override; fix the date-picker / date-range-picker chevrons that way. **`copy-field`/`secret-field`**: share one `CopyToClipboard` helper (`shared/ui/copy-to-clipboard.ts`) so both give the same toast.

New in `shared/forms/` (new directory — forms are not UI and not validators):
- `validation-messages.ts`: `VALIDATION_MESSAGE_KEYS` — `required`, `email`, `minlength`, `maxlength`, `pattern`, `min`, `max`, `url`, `phone`, `taken`, `mismatch` → `shared.validation.*` (add to both locales). `field-error` resolves: server error → mapped key (with `{requiredLength}`-style params) → `fallback` override → nothing. `fallback` becomes optional.
- `form-dirty.ts`: `formDirty(form, destroyRef): Signal<boolean>` via `form.events` — one implementation for the three.
- `unique-async.ts`: `uniqueAsync(check: (v) => Observable<boolean>, errorKey = 'taken', debounceMs = 300)` — the debounce/switchMap/`control.enabled`-guard/`catchError`/`first()` pipeline, honouring the lesson about disabled controls.

New in `shared/validators/`: `phone-number.ts`, `social-profile-url.ts`, `credentials-when-enabled.ts`, `default-language-is-supported.ts`, `passwords-match.ts`, `patterns.ts` (`CODE_PATTERN`, `SKU_PATTERN`, `SLUG_PATTERN`, `CUSTOM_DOMAIN_PATTERN`, `SOCIAL_URL_PATTERN`, `slugify`) — moved, not rewritten; their specs move with them.

New in `shared/state/`: `snapshot.ts` — `snapshot<T, P>(params, stream): {value, isLoading, error, isEmpty, retry, invalidate}` wrapping `rxResource` + the last-good `linkedSignal`, with the "params returning `undefined` means not ready" gate from the catalogue lesson. Facades adopt it in commits 4–6.

New in `core/http/`: `optional.ts` — `optionalOne<T>()` (→ `null`) and `optionalList<T>()` (→ `[]`) as RxJS operators; the four copies are deleted when their features migrate.

Tokens (`src/styles/theme.css` / `theme-bridge.css` / the three themes): declare `--icon-size` default and `--status-ink`; add `--paper`, `--paper-foreground`, `--paper-muted`, `--paper-border` (light in every theme — the invoice is paper by design; stated in DESIGN.md's Token Rule as the sanctioned exception); `--scrim` used by confirm-dialog, video-dialog, plan-dialog. `styles.css`: one `rise` and one `spin` keyframe; `popover-in` owned by `.popover` only; delete the local copies as each feature migrates.

Also here: `shared/styles/field.css` reduced to layout only — `.field-grid`, `.field-wide`, `.split` (the two-pane grid, with its ≤900px stack) — imported by the features that need a grid; the control look lives in the components. `settings-card.css` keeps only its provider-card and slider residue after commit 4.

Specs: every new component, plus the untested high-traffic existing ones — `panel`, `field-error`, `notice-bar`, `toggle`, `page-header`, `busy-overlay`, `badge`, `confirm-dialog`, `secret-field`, `tab-switcher`, `pagination`. Bring `number-field.spec`, `select.spec`, `toast.spec` onto `@testing/transloco-testing`. Follow the catalogue lesson: measure what a person would look at, drive what a person would use.

### Commit 3 — `refactor(console-ui): tiers, routes and the api seam`

Structure-only; done before the feature migrations so they land in the right places.
- **`layouts/billing/` dissolves.** `BillingFacade`'s subscription state → `api/billing/subscription-state.service.ts` (root; the precedent is `api/tenancy/selected-store.service.ts` holding state in the api tier). `plan-banner` and `plan-dialog` stay under `layouts/console-shell/components/` and read that service; `features/billing` reads the same service through its own `billing.api.service.ts`. `console-shell.ts:64-71`'s workaround goes. One facade per concern; no layout imports a feature.
- **Cross-feature imports.** `product-search.ts` → `api/catalog/product-search.service.ts` (it is a query over `tiny-products` with the client-side filter the lessons describe); `products-cache.ts` → `api/catalog/products-cache.ts` (an invalidation stamp beside `catalog-reference`'s `invalidate`). The store-management validators already moved in commit 2.
- **`models/` is a leaf.** `models/ui.ts` with `Tone`, `IconName` (the name union; `shared/ui/icon/icon-paths.ts` becomes `Record<IconName, string>` against it), `KpiDatum`; `PageT`/`PageRequest` → `models/page.ts` (a wire envelope belongs there; `core/table/table.types.ts` re-exports for the api tier); `ConsoleLocale` → `models/locale.ts`. Then the 13 imports are rewritten.
- **eslint**: models block bans `@shared/*` and `@core/*` too; `layouts/**` bans `@features/*`; `features/**` bans `@features/*` (a feature imports its own files relatively — the three existing `@features/` imports are exactly the three violations, all removed above); `core/**` bans `@shared/*` (already clean, now enforced).
- **Routes**: `app.routes.server.ts` declares one `path/**` entry per top-level branch incl. `subscription/**`, `public/subscription/**`, `external-logout-link`; `app.routes.spec.ts` asserts every top-level path in `routes` has a server entry. `core/routing/route-data.ts`: `ConsoleRouteData {titleKey, breadcrumbKey?, document?, succeeded?}` removing the four casts. `core/routing/route-params.ts`: `positiveIntParam(route, name): Signal<number | null>` used by order-details and product-form, `enumParam(route, name, set)` used by catalogue and store-management — and `isSectionKey`/`CATALOGUE_TABS` either become the `set` argument or are deleted. Spec for `features/subscription` (both outcomes render; `refresh()` is called).
- **Fixtures leave `@mocks/`.** `first-run.fixture` → `features/first-run/first-run.content.ts`, `console.fixture` → `layouts/console-shell/console-navigation.ts`, `create-store.fixture` → `features/create-store/create-store.content.ts`, `marketing.fixture` → `features/marketing/marketing.content.ts`; each keeps its `TODO(lessons.md)`. `FirstRunApi`'s fake `delay(220)` goes — the content is a constant and says so. `testing/console-api.fake.ts` gets its own minimal nav so a spec can fail. `src/app/mocks/` and the `@mocks` alias are deleted; eslint's `@mocks` bans go with it.
- **api tier**: hoist each file's provenance into a leading `/** Ported from seller-ui/projects/seller-core/<path>. */` line (20 files; the three console-native ones get `/** Console-native; not a port. */`). Give `billing` and `create-store` a `services/*.api.service.ts`; facades stop importing `@api/*` directly (enforce: eslint bans `@api/*` in `features/**/facades/**` and `features/**/*.ts` except `services/`).
- **Scripts**: `scripts/check-lessons-citations.mjs` (handles wrapped JSDoc; fails on an unknown heading) and `npm run i18n:unused` (flatten + grep, prefix-aware); both wired into `npm run lint`. Add `stylelint` with one rule set: no hex/`rgb(` literals under `src/app/**` (the `src/styles/` theme files are exempt), `declaration-property-value-disallowed-list` for physical direction properties (`margin-left`, `padding-right`, `left`, `right`, `text-align: left|right`, `border-left/right`, `float`) — logical only. Run in `npm run lint`.

### Commit 4 — `refactor(console-ui): store management and create-store on the shared fields`

- `details-section`, `branding`, `home`, `domain`, `social-links`, `slider`, `social-login`, `payments` sections: every raw `<input>` → `app-form-field` + `app-text-field`/`app-number-field`/`app-secret-field`; 7 native `<select>` → `app-select`; 2 textareas → `app-textarea`; `.icon-action` for slider/domain rows; `.cross-field-error` kept but styled once in `field.css`. `settings-card.css` shrinks to provider-card + slider residue.
- create-store: extract `services/create-store-form.service.ts` (same shape as `store-settings-form.service.ts`: `create()`, `reset()`, payload mapper; the fields it shares with details-section use the same validators from `shared/validators`), `services/create-store.api.service.ts`; 8 inputs + 7 selects migrated; pod radio cards keep `sr-only` radios but get `aria-hidden` asterisks fixed; `clearServerErrorsOnChange` added; `.progress-card/.run-track` — the provisioning progress reuses `app-stepper` + `app-progress-track` from first-run's migration in commit 6 (whichever lands first owns the shared `progress-card` in `shared/ui/` — decide: **`shared/ui/progress-card`** built here, first-run consumes it in commit 6).
- `store-management.html`: header actions unchanged; the `.settings-section > * { animation }` → `animate.enter="rise"`. `store-settings.facade.ts`: `snapshot()`, `optionalOne/optionalList` from core, `saving`/`deleting`/`uploading` names (see naming below), toasts from the facade only.

### Commit 5 — `refactor(console-ui): catalogue and the product form on the shared fields`

- Both `editor-card.css` deleted. Four tabs + four steps migrate to `app-form-field` + `app-text-field`/`app-textarea`/`app-select`/`app-number-field`/`app-rich-text`; code fields use `check` indicator; `app-locale-switcher` in all four tabs and essentials-step; `app-empty-state` for `.list-empty`/`.editor-empty`/`.picker-empty`/`.related-empty`; `app-search-box` in group-tab; `.split` from `field.css`; `catalogue-form.service.ts` and `product-form.service.ts` use `uniqueAsync` and `shared/validators/patterns`. Rename `product-form/services/product-form.service.ts` → `product-draft-form.service.ts` so it reads as a `*-form.service.ts` (the form builder) like its four siblings. media-step's image delete gets `app-confirm-dialog`. Facades: `snapshot()`, `optionalOne`, one busy name, `catalogue.facade.ts`'s `copyStamp` → `formDirty`.

### Commit 6 — `refactor(console-ui): orders, order details, products, dashboard, billing and first-run on the shared chrome`

- **order-details**: `app-page-header` first; items and invoice tables → `app-data-table` + `app-table-row` (the invoice's print layout keeps its own `.invoice-sheet` on `--paper-*` tokens); the invoice modal → native `<dialog>` via a small **`shared/ui/dialog`** host (extracted from `confirm-dialog`'s `showModal()` handling so confirm/video/image-preview/plan-dialog/invoice share one) ; status `<select>` → `app-select`; comment `<textarea>` → `app-textarea`; `.invoice-action` → `.icon-action`; `toasts` → `toast`, raised only in the facade; `order-rise` → `animate.enter="rise"`; `.load-error` → `app-load-error`. Stylesheet target: ≤ 600 lines.
- **orders / products**: `app-search-box`, `app-empty-state` (one design), `app-load-error`, `.icon-action`, `[active]/(activeChange)`; tab + filters become query params (`?status=`, `?q=`, `?category=`, `?brand=`, `?page=`) so a filtered list is linkable, matching catalogue's routed tab; `panel-in` → `animate.enter="rise"` on both (products gains the motion orders has). `products.html` inline-edit affordance unchanged.
- **dashboard**: `.load-error` → `app-load-error`, `.icon-button` → `.icon-action`, `panel-in` → `rise`; `DashboardFacade` onto `snapshot()`.
- **billing**: `:host { display: contents }`, remove the 1.75rem margin, `app-busy-overlay reserve="page"`, `app-tab-switcher` fallback under `app-section-nav` exactly as store-management, `billing-rise` → `rise`, `.empty-note` → `app-empty-state`, `.panel-pad` gone; `BillingPageFacade` → `billing.api.service.ts` → `@api/billing`.
- **first-run**: `app-page-header` first (notice bar inside the body), `task-list` → `app-stepper` + `shared/ui/progress-card`, `.load-error`/`.first-load` → shared, `section-in` → `rise`; content from `first-run.content.ts`.
- Header feed: every page exposes `heading(): {title, context}` from its facade (the dashboard/first-run shape); `app-page-header` takes `[heading]` as well as the two strings, so the six variants collapse. Body wrapper: one `.page-body` utility in `field.css`/`theme-bridge.css` (flex column, `gap: 1.5rem`) replacing the nine names where the wrapper is only a stack; `.split` where it is two-pane.
- Facade naming, applied across all twelve: `isLoading` / `error` / `isEmpty` / `retry()` from `snapshot()`; **`busy`** for the single in-flight write (`saving`, `submitting`, `working` → `busy`), with additional named flags only where two writes genuinely overlap (`uploading`, `deleting`); `toast` injection name; toasts raised by facades, components never inject `ToastService`; `toast.success(key, params)` overload that translates (so `transloco.translate` leaves the call sites). Page facades `providers: [XFacade]` on the page component (order-details/product-form/billing pattern) — verified per facade that nothing outside the route injects it (the grep says nothing does); `ConsoleShellFacade` and the new `SubscriptionState` stay root.

### Commit 7 — `refactor(console-ui): marketing and auth — shared fields and a right-to-left page`

- `sign-in`/`sign-up`: `app-form-field` + `app-text-field type="password"` (reveal toggle from the component), `app-field-error` messages from the shared map, `clearServerErrorsOnChange`, `auth.css` loses its field rules and its 7 physical properties; `auth-story.css` 3.
- marketing: contact form on `app-form-field`/`app-text-field`/`app-textarea`/`app-select` with its disabled-submit notice unchanged; 16 physical properties → logical; `arrowRight` ×3 flips via the icon default. Stylelint now keeps this from coming back.

### Commit 8 — `test(console-ui): the api tier`

One `HttpTestingController` spec per service in `src/app/api/**` (22), in the shape of `features/products/services/products.api.service.spec.ts`: URL, verb, `?store=` stamping, params, body, error mapping. Start with `catalog-reference` (shareReplay + `invalidate` + error eviction), `product`, `orders`, `subscription`, `manager-store`, then the rest. Each is small; the value is that a contract change shows up as a red spec rather than a QA finding.

### Commit 9 — `docs(console-ui): the base architecture`

- **`store-core/console-ui/ARCHITECTURE.md`** — the contract every module plan must cite: the six tiers and direction (with the eslint blocks that enforce it); the canonical feature shape (`feature.ts/.html/.css` + `components/` + `facades/` + `services/*.api.service.ts` + `services/*-form.service.ts`; page → facade → api service → `@api/*`; facade component-provided; `snapshot()`; `optionalOne/optionalList`; writes reload rather than echo); the shared control catalogue (one line each: when to use `app-form-field`+`app-text-field`, `app-select`, `app-number-field`, `app-textarea`, `app-rich-text`, `app-secret-field`, `app-toggle`, `app-date-picker`, `app-search-box`, `app-locale-switcher`, `app-empty-state`, `app-load-error`, `app-busy-overlay reserve`, `app-panel`, `app-page-header`, `app-data-table`, `app-pagination`, `app-tab-switcher` vs `app-section-nav` vs `app-stepper`, `app-confirm-dialog`, `app-notice-bar`, `app-badge`, the action classes incl. `.icon-action`); the page contract (`:host { display: contents }`, `app-page-header` first with `heading()`, `.page-body`/`.split`, busy overlay with reserve, `app-load-error`, `app-empty-state`, `animate.enter="rise"` as the only entry motion, routed tab/filter state); forms (form service, `shared/validators`, `uniqueAsync`, `formDirty`, `applyToForm` + `clearServerErrorsOnChange` always together, messages from the shared map, `required` marker `aria-hidden`); i18n (`*transloco="let t"` in templates, `translateSignal` in facades, `shared.*` for cross-feature copy, known-set-then-fallback for server enums, `Money`/`TranslocoLocaleService`, `unicode-bidi: plaintext`); styling (Token Rule, logical properties, `--icon-size`, `--paper-*` exception, stylelint); routes (`ConsoleRouteData`, param helpers, server-route entry per branch); testing (`@testing/*`, measure-don't-assert-presence, one api spec per service); the `TODO(lessons.md)` convention and the citation check; the QA checklist (en/ar, three themes, 1440/900/420, network tab).
- **`store-core/console-ui/CLAUDE.md`** — short: read `ARCHITECTURE.md`, `DESIGN.md`, `lessons.md`; the commands; the rules that are easiest to break (no raw controls, no `@api` in facades, no fixture, no hex, no physical direction props, every unbacked block gets a `TODO(lessons.md)`).
- `README.md` rewritten (what the app is, how to run it inside the local stack, links). `lessons.md` gains one entry, "Console — the alignment pass", recording the console-side rules this found (the field-vocabulary drift, the dead primitives, the busy-name drift, the models-upward imports) in the file's established form. The migration plan doc (`.claude/plans/agents-requirments-console-ui-go-live-m-woolly-candy.md`) gets a short "Between Module 6 and Module 7 — the alignment pass" section stating that module plans from Module 7 on cite `ARCHITECTURE.md` and that the template's "New components" item must first check the catalogue there.

### QA, then `fix(console-ui): after the alignment pass`

Chrome against the live local stack (`run-lcl.sh`; never `kill` a supervised process — `restart console-ui`), org admin, `ORG1-STORE1`:
- Every console page — dashboard, orders, orders/:id, products, products/new, products/:id, catalogue × 4 tabs, store-management × 8 sections, subscription × 2, getting-started, store-management/create — plus sign-in, sign-up, `/`, terms, not-found, `/public/subscription/success`.
- Each in en and ar; each in Forest, Midnight, Daylight; at 1440 / 900 / 420. Look for: a field that lost its value (the native-select lesson), a select whose options arrive late, focus order, the check indicator, an error message that now comes from the shared map, tab/filter survive reload via the URL, entry motion once per panel, no horizontal scroll, RTL mirroring on marketing/auth and the calendar chevrons.
- Write paths spot-checked once each: save a store detail, create a store (throwaway), save a category/brand/type/group, save a product draft, add a status comment, change a plan dialog (cancel).
- Network tab: `?store=` on every private call; no request fired twice on page load (the `snapshot()` gate).

---

## Critical files

**New:** `shared/ui/{form-field (rebuilt), text-field, textarea-field, empty-state, load-error, search-box, locale-switcher, progress-card, dialog}/`, `shared/forms/{validation-messages,form-dirty,unique-async}.ts`, `shared/validators/*.ts`, `shared/state/snapshot.ts`, `shared/styles/field.css`, `core/http/optional.ts`, `core/routing/{route-data,route-params}.ts`, `models/{ui,page,locale}.ts`, `api/billing/subscription-state.service.ts`, `api/catalog/{product-search.service,products-cache}.ts`, `features/{billing,create-store}/services/*.api.service.ts`, `features/create-store/services/create-store-form.service.ts`, `features/*/…content.ts` ×4, `scripts/check-lessons-citations.mjs`, `.stylelintrc`, `ARCHITECTURE.md`, `CLAUDE.md`, 22 api specs, ~15 shared specs.

**Changed:** every feature's templates/stylesheets/facades (see commits 4–7), `styles.css`, `src/styles/theme*.css`, `eslint.config.js`, `package.json` (scripts + stylelint), `tsconfig.json` (drop `@mocks`), `app.routes.ts`, `app.routes.server.ts`, `app.routes.spec.ts`, `src/locale/{en,ar}.json`, `shared/ui/{panel,busy-overlay,icon,field-error,copy-field,secret-field,date-picker,date-range-picker,confirm-dialog,video-dialog,image-preview}`, `layouts/console-shell/**`, `testing/console-api.fake.ts`, `lessons.md`, `README.md`, the migration plan doc.

**Deleted:** `shared/ui/{button,card,menu,avatar}/`, four `core/` files, `src/app/mocks/`, `layouts/billing/`, both `editor-card.css`, most of `settings-card.css`, `features/catalogue/components/locale-chips/`, the nine `.load-error`/eight `.first-load`/three `.panel-pad`/seven empty-state/six keyframe/six spinner blocks, 37 i18n keys, 12 empty dirs.

**Reused, already present:** `app-select`, `app-number-field`, `app-toggle`, `app-date-picker`, `app-rich-text`, `app-secret-field`, `app-notice-bar`, `app-panel`, `app-data-table`, `app-pagination`, `app-tab-switcher`, `app-section-nav`, `app-stepper`, `app-progress-track`, `app-confirm-dialog`, `app-busy-overlay`, `core/errors/{api-error.service,form-error.utils}`, `shared/i18n/{money,status-label}`, `testing/*`.

## Verification

1. After every commit: `cd store-core/console-ui && npm run build && npm run lint && npm test -- --watch=false --browsers=ChromeHeadless`; `git -C store-core/seller-ui status --porcelain` and `git -C store-pod status --porcelain` empty.
2. Greps that must return nothing at the end: `grep -rn "<input\|<select\|<textarea" src/app/features src/app/layouts --include='*.html' --include='*.ts'` (outside `shared/ui`); `grep -rn "\.load-error\|\.first-load\|\.panel-pad\|\.table-empty\|\.list-empty\|\.editor-empty" src/app/features`; `grep -rn "@keyframes" src/app/features` (only genuinely page-specific motion survives, each with a comment); `grep -rn "from '@api/" src/app/features --include='*.ts' | grep -v services/`; `grep -rn "from '@features/" src/app`; `grep -rn "@mocks" src`; `grep -rn "app-button\|app-card\|app-menu\|app-avatar" src`; `grep -rn "margin-left\|margin-right\|padding-left\|padding-right\|text-align: left\|text-align: right" src/app` (stylelint enforces this too); `grep -rln "providedIn: 'root'" src/app/features/*/facades` → none.
3. `npm run lint` now runs eslint + stylelint + the lessons-citation check + `i18n:missing` + `i18n:unused`, all clean.
4. `grep -rn "TODO(lessons.md)" src` — every marker resolves (the script proves it); count unchanged or higher, never lower without a matching deletion.
5. The Chrome pass above, with screenshots of each page in ar/Daylight/420 as the hardest combination, and a fix commit for what it finds.
6. `npm test` count goes up by the new shared + api specs and loses nothing but the deleted components' (none had specs).
