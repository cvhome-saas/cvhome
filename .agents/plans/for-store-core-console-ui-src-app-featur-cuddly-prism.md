# Store Management page for console-ui, from `Store Management.dc.html`

## Context

`../../store-core/console-ui` is the Angular rewrite of the seller console. Its shell
(`src/app/layouts/console-shell/` — plan banner, navigation rail, toolbar, store switcher) and
four features (`marketing`, `auth`, `dashboard`, `orders`) are already built. `features/orders`
is the current reference for how a page is assembled: a thin component over a root-provided
facade over a mock API service, styled in plain CSS against the theme tokens, with a
`shared/ui/` vocabulary underneath.

`store-core/console-template/Store Management.dc.html` is the next page. Unlike Orders it is not
a table — it is the store's **settings surface**: a sticky left sub-nav of eight sections, each
rendering one card of fields, with a single page-level *Save changes* action. Everything outside
the `<main>` grid in that file (plan banner, sidebar, toolbar) is shell that already exists.

The outcome is a working `/store-management` route reachable from the sidebar's *Organization*
group, visually faithful to the mockup but expressed in design tokens rather than its hardcoded
emerald hexes, with genuinely editable forms.

## Decisions (settled with the user)

| Area | Decision |
|---|---|
| Scope | **All eight sections** in one feature. |
| Interactivity | **Editable reactive forms** with validation, dirty tracking and a working Save. File uploads, DNS verification, slide reordering and secret rotation raise a toast — no backend for them. |
| Data | **Mock-backed**, mirroring `orders.api.service.ts` (latency + failure-rate constants, `of().pipe(delay())`). No live HTTP; models shaped to the real DTOs so swapping in HTTP later is mechanical. |
| Providers | **Backend enums win over the mockup's lists** (see below). |

### Provider lists come from the backend, not the mockup

The mockup invents providers the platform cannot store. Use the real enums:

- **Social links** — `SocialProvider` (`store-commons/commons/.../domain/SocialProvider.java`):
  `FACEBOOK, X, TIKTOK, INSTAGRAM, GITHUB`. Drops the mockup's LinkedIn and YouTube.
- **Social login** — `cua`'s `SocialProvider` (`store-pod/cua/.../config/SocialProvider.java`):
  `GOOGLE, FACEBOOK, GITHUB`. Drops Apple, adds GitHub.
- **Payments** — `PaymentType`: `COD, MANUAL_TRANSFER, STRIPE, PAYPAL`. Drops Tap Payments.
  Only `STRIPE`/`PAYPAL` carry credentials (`PaymentType.attrs`); `COD` and `MANUAL_TRANSFER`
  are toggle-only, so their cards render the switch and no credential grid.

## Grounding the model in real DTOs

The mockup's fields map almost one-for-one onto existing backend contracts. Shape the TypeScript
models after them so this is a rename away from real:

| Section | Backend contract |
|---|---|
| Details / Branding | `ReadableMerchantStore` + `MerchantStoreDetails` (`store-pod/merchant/merchant-commons/.../model/merchant/`) — `name`, `email`, `phone`, `theme` (`Theme` enum), `colorTheme`, `inBusinessSince`, `useCache`, `requireLoginForOrderPlacement`, `dimension`/`weight` units, `address`, `logo`/`banner` (`ReadableImage`), `supportedLanguages` |
| Domain | `ManagerStoreDomain(domain, DomainType)` where `DomainType = SUB_DOMAIN \| CUSTOM_DOMAIN` |
| Slider | `ReadableSliderImage(priority, name, url)` / `SliderImage(priority, name)` |
| Social links | `SocialLink(provider, url)` |
| Social login | `ReadableSocialLoginConfig{providerId, appId, appSecret, enabled}` |
| Payments | `ReadablePaymentConfiguration{paymentType, apiKey, secretKey, webhookSecret, enabled}` |

Two consequences worth honouring, both already true in the mockup:

- **Secrets are write-only.** `appSecret`, `secretKey` and `webhookSecret` never come back from
  the server. The model carries a `secretHint` (last four / "not set") plus a `lastRotated`, and
  the form field is a *Replace* / *Rotate* action, never a populated input.
- **Store home page copy is per-language.** `supportedLanguages` drives the EN/AR/FR/DE pill
  track; each language holds its own `{title, text, metaDescription, tags[]}`.

## Work

### 1. Shared UI additions — `src/app/shared/ui/`

The page needs four primitives the console does not have yet. Everything else it needs already
exists and should be reused as-is: `app-panel` (card chrome + `[panelAction]` slot),
`app-page-header`, `app-form-field` + `app-field-error`, `app-badge` (which already has the
`shape: 'square'` input the mockup's `ENABLED`/`LIVE`/`SYSTEM` tags need), `app-notice-bar`,
`app-busy-overlay`, `app-icon`, `app-tab-switcher`, `app-progress-track` (the banner upload
bar), and `ToastService`. Follow the house style for these: signal `input()`/`output()`/`model()`,
inline `template`, no `standalone: true`, no `changeDetection`.

- **`toggle/`** — `app-toggle`, the 38×22 pill switch used by Details visibility, social login
  and payments. Two-way `checked = model<boolean>()`, plus `label`, `description` and
  `disabled` inputs. Renders a real `<button role="switch" [attr.aria-checked]>` so it is
  keyboard-operable; the mockup's is a styled `<div>`.
- **`file-drop/`** — `app-file-drop`, the dashed drop zone (logo, banner, slider). Inputs
  `label`, `hint`, `accept`, `multiple`; output `filesSelected`. Wraps a visually-hidden
  `<input type="file">` so click, keyboard and drag all work. Projects a preview via
  `<ng-content>` so the logo tile and the banner placeholder share one component.
- **`tag-input/`** — `app-tag-input` for the Home page *Tags* field. Two-way
  `tags = model<readonly string[]>()`; Enter/comma commits, Backspace on empty removes the last,
  each chip has a labelled remove button.
- **`copy-field/`** — `app-copy-field`, the read-only monospace value with a copy icon (DNS
  record value, OAuth callback URL, webhook endpoint). Input `value`, optional `label`; copies
  via the Clipboard API and confirms through the existing toast service.

Add the icons the page needs to `shared/ui/icon/icon-paths.ts` (24×24 stroke paths in the
existing style; `IconName` widens automatically) — `palette`, `desktop`, `share`, `images`,
`signIn`, `upload`, `copy`, `link`, `lock`, `grip`, `trash`, `pencil`, `shield`,
`questionCircle`, `externalLink`. `building`, `globe`, `creditCard`, `cog`, `plus`, `check`,
`x`, `envelope`, `phone`, `mapPin` and `pin` already exist.

### 2. Settings sub-nav

The left rail of section links is page-local, not a shared primitive — it is a vertical
`role="tablist"` bound to the router, not a generic list. Put it in
`features/store-management/components/settings-nav/`. Inputs: `sections: readonly SettingsSection[]`
(`{key, label, icon, attention?}`), `active`. `attention` renders the mockup's amber dot; drive
it from real state (an unverified custom domain), not a hardcoded flag. Below the divider, the
*Home page builder* link is a plain `routerLink` out to the storefront builder route (or a
disabled item with a "coming soon" title if that route does not exist yet).

**Section selection is a route param**, not component state: `/store-management/:section`, so a
tab is linkable, survives reload, and the browser back button works. Redirect the bare path to
`branding`. `withComponentInputBinding()` is already on in `app.config.ts`, so the page takes
`section = input.required<SettingsSectionKey>()` directly — no `ActivatedRoute` in the component.

### 3. Models and fixture

**`src/app/models/store-settings.ts`** — the union of the sections, each section a discrete
interface so a section component takes exactly its own slice:

```ts
export type SettingsSectionKey =
  | 'branding' | 'home' | 'domain' | 'social'
  | 'slider' | 'details' | 'social-login' | 'payments';

export type DomainStatus = 'unverified' | 'checking' | 'waiting' | 'verified' | 'failed';
export type SocialLinkProvider = 'FACEBOOK' | 'X' | 'TIKTOK' | 'INSTAGRAM' | 'GITHUB';
export type LoginProvider      = 'GOOGLE' | 'FACEBOOK' | 'GITHUB';
export type PaymentType        = 'COD' | 'MANUAL_TRANSFER' | 'STRIPE' | 'PAYPAL';

export interface StoreSettings {
  readonly branding: BrandingSettings;
  readonly home: Readonly<Partial<Record<ConsoleLocale['code'], HomePageCopy>>>;
  readonly domains: readonly StoreDomain[];
  readonly socialLinks: readonly SocialLinkSetting[];
  readonly slides: readonly SliderSlide[];
  readonly details: StoreDetails;
  readonly socialLogin: readonly SocialLoginConfig[];
  readonly payments: readonly PaymentGatewayConfig[];
}
```

Plus an exported `DOMAIN_STATUS_TONE: Record<DomainStatus, Tone>` and
`SECTIONS: readonly SettingsSection[]`, replacing the mockup's inline `DOMAIN_STATES` style
dictionary of hexes. The mockup's five state colours map onto the `Tone` vocabulary:
`checking`→`blue`, `waiting`→`amber`, `verified`→`green`, `failed`→`red`,
`unverified`→`slate`.

**`src/app/mocks/store-settings.fixture.ts`** — the mockup's Acme Supply Co. data verbatim
(three slides, the twelve detail-field values, the two domain records), retargeted onto the real
provider enums, with home-page copy for all four console locales. The mockup ships EN/AR/FR and
leaves DE empty — keep that, since an untranslated language is exactly what its "untranslated
languages fall back to English" notice is for. The store name should match
`CONSOLE_STORES[0]` in `console.fixture.ts` so the shell's switcher and this page agree.

The `ConsoleLocale` list in `core/i18n/locale.service.ts` (`en`/`ar`/`fr`/`de`) drives the
language track — do not hardcode a second list of languages.

### 4. Feature — `src/app/features/store-management/`

Mirrors `features/orders/` file for file.

**`services/store-settings.api.service.ts`** — mock, no `HttpClient`, same `MIN_LATENCY_MS` /
`MAX_LATENCY_MS` / `FAILURE_RATE` shape as `orders.api.service.ts`.
`loadSettings(): Observable<StoreSettings>` and `saveSection(key, patch): Observable<StoreSettings>`,
plus `verifyDomain(domain)` which walks `checking → waiting|verified` so the domain panel's
three visual states are all reachable (the mockup wires them to a *Verify* button that just
cycles).

**`facades/store-settings.facade.ts`** — `@Injectable({providedIn: 'root'})`, following
`OrdersFacade`: `rxResource` for the load, `linkedSignal` holding the last good snapshot so the
form does not blank between requests, and `isLoading` / `error` / `retry()` with the same
meanings. Adds:

- `activeSection` — derived from the route param, not held here
- `activeLanguage` — signal for the home-page pill track
- `isDirty` / `save()` / `discard()` — the page-level *Save changes* button is enabled only when
  the section form is dirty and valid; save routes to `saveSection` and raises a toast
- `domainStatus` and `verifyDomain()`

**`services/store-settings-form.service.ts`** — builds the per-section `FormGroup`s with
`NonNullableFormBuilder`, following `features/auth/services/sign-up-form.service.ts` (the only
existing forms precedent) so the component never injects `FormBuilder`. Validators from the
mockup's own hints: title ≤ 60 chars, meta description 150–160 recommended (warning, not error),
slug lowercase-kebab, custom domain `^[a-z0-9.-]+$` with no protocol or trailing slash, support
email, short description ≤ 160. Server errors go through the existing
`core/errors/` utilities — `ApiErrorService.applyToForm(raw, form)` on a failed save,
`clearServerErrorsOnChange(form, destroyRef)` at build time, and `app-field-error` reading
`serverErrorOf(control)`. Do not hand-roll any of that.

**Section components** — `components/branding-section/`, `home-section/`, `domain-section/`,
`social-links-section/`, `slider-section/`, `details-section/`, `social-login-section/`,
`payments-section/`. Each is a dumb renderer taking its slice plus its `FormGroup`, wrapped in
`app-panel` for the card chrome. Splitting them keeps every file well under the
`anyComponentStyle` budget that `dashboard.scss` already breached once, and lets the router
lazy-render only the active one.

**`store-management.ts` / `.html` / `.css`** — structure copied from `orders.ts`:
`app-page-header` (title, `Store · domain` subtitle, published `app-badge`, *Preview storefront*
secondary button, *Save changes* primary) → the `load-error` block → `app-busy-overlay` wrapping
either `.first-load` or the content region → the `250px 1fr` grid of settings nav + active
section. `:host { display: contents }`; the shell's `.workspace` gap owns vertical rhythm.

All members `protected readonly`, mirroring the facade's signals; local UI-only state
(`storeMenuOpen`-style popovers) stays in the component. Unimplemented actions — upload,
reorder, delete store, rotate secret, home-page builder — call `ToastService.info('… is not
available yet.')`, the convention `orders.ts` documents, rather than failing silently. Buttons
follow the hand-rolled `.primary-action` CSS the shipped pages use, not `app-button`.

**`store-management.spec.ts`** — clone the `orders.spec.ts` harness: a hand-written
`FakeStoreSettingsApi` provided via `{provide: StoreSettingsApi, useValue: api}` with
`deferred` / `pending: Subject` / `failure` knobs, `fakeAsync` + `tick()`, DOM assertions, and
the "renders none of the console chrome" test. Assert: the branding section renders by
default, navigating to `/store-management/domain` swaps the section, Save is disabled until the
form is dirty and valid, an invalid custom domain blocks Save and shows the field error, the
toggles flip and mark the form dirty, verify walks the domain through its states, and retry
recovers from a load error.

### 5. Responsive and RTL

Same approach as Orders — `@container` queries, not viewport queries, so the page responds to
its own column as the shell's rail collapses at 900px:

- **wide** — `250px 1fr` as mocked; two-column field grids in Details, Social login, Payments
- **medium** — field grids collapse to one column; the settings nav stays
- **narrow (< ~700px)** — the settings nav becomes a horizontal scrolling `app-tab-switcher`
  above the section rather than a sticky rail; social-link rows stack label over field

Throughout: logical properties (`inline-size`, `padding-inline`, `margin-inline-start`), and
`[flip]="true"` on the directional chevrons and the *Home page builder* arrow, so the Arabic
locale mirrors. The mockup's `grid-column:1/-1` full-width fields become `grid-column: 1 / -1`
on named classes rather than an index check.

### 6. Wiring

- `src/app/app.routes.ts` — a `store-management` block copying the `orders` one: a lazy
  `loadComponent` `ConsoleShell` parent whose children are `{path: '', redirectTo: 'branding'}`
  and `{path: ':section', loadComponent: StoreManagement}`, with `title` via
  `$localize\`:@@route.storeManagement.title:Store management | cvhome\`` and
  `data: {breadcrumb: 'Store management'}` — the toolbar's `ConsoleShellFacade.pageLabel`
  requires the breadcrumb.
- `src/app/app.routes.server.ts` — `{path: 'store-management/**', renderMode: RenderMode.Client}`.
  Required, not optional: `SelectedStoreRequestContext.params()` throws during SSR.
- `src/app/mocks/console.fixture.ts` — the *Store management* nav item already exists in the
  `Organization` group with `icon: 'building'`; it only needs `route: '/store-management'` added,
  and `routerLinkActive` in `console-sidebar.ts` lights it with no further change.
- `src/locale/messages.xlf` — add the new route title; leave `.ar`/`.fr`/`.de` to the next
  `npm run extract:i18n`. Section labels and field copy stay plain text, matching `orders.html`.

Out of scope but worth noting: this route also gives the store switcher's dead *Manage all
stores* button and the toolbar's dead *Settings* entry somewhere to point. Leave them alone.

## Verification

1. `npm run lint` and `npm test` — `store-management.spec.ts` passes alongside the existing suites.
2. `npm start`, then open `/store-management`:
   - the sidebar's *Store management* item highlights; the toolbar breadcrumb reads
     `Dashboard › Store management`
   - clicking each of the eight sections swaps the card and updates the URL; reloading on
     `/store-management/payments` lands back on Payments; browser back walks the sections
   - edit a Details field: *Save changes* enables, saving raises a toast and the button
     disables again; navigating away with a dirty form warns
   - clear *Store name* → the field error shows and Save is blocked; type `https://foo/` into
     the custom domain → the format error shows
   - *Verify* walks the domain panel through checking → waiting/verified with the right tone
   - the toggles in Social login and Payments expand and collapse their credential grids;
     secret fields never show a value, only *Replace* / *Rotate*
   - the EN/AR/FR/DE track swaps the home-page copy and the character counter tracks the title
3. Compare against `store-core/console-template/Store Management.dc.html` side by side at
   1440px, 900px and 420px.
4. Switch themes (forest / midnight / daylight) from the toolbar and confirm no hardcoded colour
   survives; switch to Arabic and confirm the settings nav, chevrons and field grids mirror.
5. Set `FAILURE_RATE = 1` in `store-settings.api.service.ts` temporarily to exercise the error
   block and *Try again*, and to confirm a failed save keeps the form dirty rather than clearing it.
