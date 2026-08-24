# console-ui — the architecture

What this application agrees on, so that a new page looks like the pages beside it and a new module
plan can say "as usual" instead of re-deciding. `DESIGN.md` is the visual system; `lessons.md` is
what the backend cannot do yet. This is the shape of the code.

Every rule here is either enforced by a check or written down because it cannot be. Where a rule
exists because something went wrong, the entry says what.

---

## 1. Tiers

```
features → layouts → shared → api → core → models
```

A tier may use anything below it and nothing above it. Enforced by `no-restricted-imports` blocks in
`eslint.config.js`, one per tier.

| Tier | Holds | May not |
|---|---|---|
| `features/` | One folder per route: the page, its facade, its api service, its form service, its components. | Import another feature. |
| `layouts/` | Chrome every feature sits inside: the console shell, the auth shell, the marketing shell. | Import a feature. |
| `shared/` | Anything two features need: `ui/` components, `forms/` helpers, `validators/`, `state/`, `i18n/`, `styles/`. | Import `api/`, `layouts/` or `features/`. |
| `api/` | The HTTP tier, ported from seller-core. One directory per bounded context. | Import any UI tier. |
| `core/` | Infrastructure: the HTTP client, the error stack, auth, theming, i18n, routing helpers, the export service. | Import `shared/` or above. |
| `models/` | Wire DTOs and view models. The floor. | Import **anything**. |

**Two rules that were learned rather than designed.**

*A feature that needs another feature's code has found something that belongs one tier down.* Three
did — create-store borrowed store management's validators, the catalogue borrowed the product form's
search, the product form borrowed the product list's cache stamp — and all three were data or
validation concerns, not feature concerns.

*`models/` is the floor, and that includes presentational vocabulary.* Eight model files imported
`Tone`, `IconName` and `PageT` upward, and `models/orders.ts` imported `KpiDatum` from a *component
file*. Those types are fine to share; the direction was not. They live in `@models/ui`,
`@models/page` and `@models/locale`, and the UI tier re-exports them so call sites read naturally.

---

## 2. A feature

```
features/orders/
  orders.ts  orders.html  orders.css      the page
  components/                             parts of it, if it has any
  facades/orders.facade.ts                what the page reads and calls
  services/orders.api.service.ts          the seam onto @api/*, and the view-model mapping
  services/orders-form.service.ts         the reactive form, if it has one
```

**page → facade → api service → `@api/*`.** The api service is where a wire DTO becomes a view
model, and it is the seam the whole seller-ui migration turned over; keeping it means facades, pages
and specs do not move when an endpoint does. HTTP goes through it — a facade may inject a non-HTTP
collaborator from `@api/` (a cache stamp, the selected store) but not make a request itself.

**A page facade is provided by the page**, not `providedIn: 'root'`:

```ts
@Component({providers: [OrdersFacade], /* … */})
```

A root-provided facade with an `rxResource` starts fetching the moment anything injects it. The
product form held `ProductsFacade` purely to call `invalidate()` after a save and paid for a page of
the products list on every visit. Inject the smallest thing that does the job.

**Naming, across every facade:**

| Name | Is |
|---|---|
| `isLoading` / `error` / `isEmpty` / `reload` | from `snapshot()` — never hand-rolled |
| `busy` | one in-flight write. Additional named flags (`uploading`, `deleting`) only where two writes genuinely overlap |
| `toast` | the injected `ToastService`. Never `toasts` |

**Toasts are raised by facades, not components.** A page reports what it *did*; the facade is what
did it.

**Writes reload rather than echo.** Most endpoints answer `void`, so a save re-reads: the page shows
what the server normalised, not what the operator typed.

---

## 3. Loading, failing, and being empty

Every list and every page uses the same three, in the same order:

```html
@if (facade.error(); as failure) {
  <app-load-error [message]="failure.message" (retry)="facade.reload()" />
}

<app-busy-overlay reserve="page" [busy]="facade.isLoading()" [label]="t('orders.loading')">
  @if (!facade.isEmpty()) {
    …
  }
</app-busy-overlay>
```

- **`snapshot(params, stream)`** (`@shared/state`) is the resource. It keeps the last good value
  while the next one loads, so a table does not blank on a filter change, and it treats `params`
  returning `undefined` as "not ready" — a resource keyed on signals that settle at different times
  otherwise runs once per signal.
- **`reserve`** holds the first-load height. `page` for a route, `panel` for a widget. Nothing
  declares its own placeholder slab.
- **`app-empty-state`** for "nothing here". Project an action only when there *is* one: "nothing
  yet" is a state, "nothing matched" is a filter to clear.
- **`optionalOne()` / `optionalList()`** (`@core/http`) wrap every leg of a wide `forkJoin` *except*
  the one that is the page, each with a comment naming why that leg may fail. A select that falls
  back to its current value is still a working page; a failed `forkJoin` is a blank one.

---

## 4. Controls

**There are no raw `<input>`, `<select>`, `<textarea>` or `<button type=checkbox>` elements in
`features/` or `layouts/`.** Five stylesheets each defined a `.field`/`.control` vocabulary and they
had drifted — two paddings, two textarea heights, one invalid state between them.

| Need | Use |
|---|---|
| Any labelled field | `app-form-field` wrapping the control |
| One line of text | `app-text-field` — `type`, `prefix`/`suffix`, `icon`, `maxLength`, `latin`, `mono`, `check` |
| Several lines | `app-textarea` |
| A seller's prose (HTML) | `app-rich-text` |
| A number | `app-number-field` |
| One of a known set | `app-select` |
| Several of a set | `app-checkbox` |
| An independent setting | `app-toggle` |
| A credential | `app-secret-field` |
| A date | `app-date-picker`, `app-date-range-picker` |
| Free-form labels | `app-tag-input` |
| Picking a record by typing | `app-autocomplete` |
| Filtering a list on screen | `app-search-box` |
| Which language you are editing | `app-locale-switcher` |
| An image | `app-image-picker` |

**Chrome:** `app-page-header`, `app-panel` (`padded` for a body that is not a table),
`app-data-table` + `app-table-row`, `app-pagination`, `app-kpi-grid`, `app-badge`,
`app-notice-bar`, `app-progress-track`, `app-section-nav` (a rail with routed sections),
`app-tab-switcher` (views of one thing), `app-stepper` (a linear task), `app-tree`,
`app-confirm-dialog`, `app-image-preview`, `app-video-dialog`, `app-toast-host`.

**Actions are classes, not components**, because they decorate `<button>`, `<a>` and occasionally
`<span>`. Defined once in `src/styles.css`: `.primary-action` (exactly one filled emerald action per
view), `.secondary-action`, `.ghost-action`, `.danger-action` (the hue in the text, not a red slab),
`.icon-action` (square, icon-only, always with an `aria-label`).

**Three traps these components exist to close**, all of them found in production code:

- A native `<select>` cannot be themed — `appearance: none` restyles the closed control and the
  operating system draws the open list. In two of three themes it opened a white sheet.
- A native checkbox tinted with `accent-color` colours only its *checked* state; unchecked against a
  dark theme is a solid dark square, indistinguishable from selected.
- A `[value]` or `[checked]` binding only writes when the expression differs from what it last
  *wrote*. A click changes the DOM behind it, so a model reset to its previous value writes nothing
  and the control stays visibly wrong. Controls drive the DOM from the signal instead.

---

## 5. Forms

- The reactive form is built in a `*-form.service.ts`, not in the component or the facade.
- Validators that two features need live in `@shared/validators`. Patterns too.
- `uniqueAsync(check, errorKey, {when, debounceMs})` is the only "is this taken?" validator. Its
  `control.enabled` guard fires when the answer *lands*, not when it is asked for — without that,
  every existing record is marked a duplicate of itself the moment its code field is disabled.
- `formDirty(form)` is the only dirty signal. It reads `form.events`, so a form reset to pristine
  after a save actually settles; `valueChanges` never fires for that.
- **`applyToForm` and `clearServerErrorsOnChange` always appear together.** A server error is not a
  validator, so nothing else will ever remove it: bind without clearing and the form stays
  permanently invalid with the field looking fixed.
- Messages come from `shared.validation.*` via `app-field-error`, keyed on the validator that
  failed. A field with something specific to say passes `fallback`, which wins — the map removes
  boilerplate, it does not overrule intent.
- The required marker is `aria-hidden`; the control's own `required` is what a reader needs.

---

## 6. Pages

- `:host { display: contents; }` — the shell's `.workspace` owns the page's padding and rhythm. A
  page that keeps its own box does not get them.
- `app-page-header` is the first element, fed from the facade's `heading()`.
- `.page-body` / `.split` (`@shared/styles/field.css`) for the stack and the two-pane layout.
- One entry animation: `animate.enter="rise"`, with `--rise-from` for the distance. Seven keyframes
  said this before, from 8px to 20px.
- Tab and filter state belongs in the URL, so a filtered list can be linked and survives a reload.
- A route param is validated *before* it reaches a facade — `positiveIntParam`, `enumParam`
  (`@core/routing`). `/orders/abc` used to reach the server as `orders/NaN` and come back a 500 that
  read as "load failed".
- Every route carries `titleKey`, console routes carry `breadcrumbKey`, and every top-level branch
  has an entry in `app.routes.server.ts`. A spec fails if one does not.

---

## 7. Words and numbers

- `*transloco="let t"` in templates; `translateSignal` or `transloco.translate` in a facade, always
  with `transloco.activeLang()` read so it re-resolves on a language change.
- Transloco is configured to **throw** on a missing key. A value that comes from the server — a
  status, a payment type, a unit — is translated only if it is in a known set and humanized
  otherwise (`@shared/i18n/status-label`). A new enum value must not be able to take a page down.
- Money through `@shared/i18n/money`, dates and counts through `TranslocoLocaleService`. Never bare
  `Intl` (it resolves to the *browser's* locale) and never `DatePipe` (pinned to `LOCALE_ID`).
- Latin data inside a page that may be right-to-left — a SKU, a slug, a domain, an email — gets
  `latin` on the control, which is `unicode-bidi: plaintext`.
- Cross-feature copy lives in `shared.*`. `npm run lint:i18n` fails on a key nothing reaches, and
  reads the *actual* dynamic patterns rather than guessing at prefixes.

---

## 8. Styling

- Every colour, radius, shadow and type step is a token. A literal hex in a stylesheet is a defect,
  and `stylelint` fails on one. The single exception is the invoice's `--paper-*` set, which is
  light in all three themes because it is a document rather than a surface — and it lives in
  `theme.css`, named, so it can be told apart from an oversight.
- **Logical properties only.** No `margin-left`, `left`, `right`, `text-align: left`. Also enforced.
- **`@shared/styles/field.css` is included per component, not globally.** A feature that uses
  `.page-body`, `.split` or `.field-hint` lists it in `styleUrls` ahead of its own sheet. Two pages
  shipped without it and their two-pane layouts stacked at every width — the component's own rules
  applied, so the missing import looked like a broken breakpoint.
- A page cannot style anything inside a child component's template: Angular scopes styles by the
  *defining* component. The two ways out are a stylesheet the child itself includes, or a custom
  property the child exposes. A selector written from outside is not one of them.
- Motion honours `prefers-reduced-motion`, handled globally in `styles.css`.

---

## 9. Tests

- `npm run test:ci` — non-interactive.
- **A spec that asserts presence proves nothing about behaviour.** `expect(querySelector('x')).not.toBeNull()`
  passes whether or not the thing is wired, visible or the right size. Where a person would look at
  something, measure it; where a person would use something, drive it.
- Address a control by what identifies it — `formControlName`, a role, its label — not by a DOM id
  that the markup happens to carry today.
- Every api service has an `HttpTestingController` spec asserting URL, verb, params and body. That
  tier's knowledge is all strings, and none of it is checked by the compiler.
- `@testing/` holds the harnesses: `api-harness`, `transloco-testing`, `console-api.fake`.
- A test fake must not import the production constant it stands in for. `console-api.fake` did, so a
  spec asserting on the nav could not fail however wrong that constant became.

---

## 10. Backend gaps

Anything the design asks for and the platform cannot answer gets:

1. a `TODO(lessons.md): <capability> — see lessons.md, "<heading>".` at the call site,
2. an entry in `lessons.md` under that exact heading, and
3. an honest UI: the control omitted, or present and saying it is not available yet.

**Never a fixture standing in for a real answer.** `npm run lint:lessons` fails if a citation names
a heading that does not exist — two had already rotted before it was written, silently, because
nothing in lint or the specs looks at prose.

---

## 11. Checks

```bash
npm run build      # AOT: catches template type errors that tsc alone does not
npm run lint       # eslint + stylelint + lessons citations + unused i18n keys
npm run test:ci
```

`npm run build` is not optional before a commit: strict template checking lives there, and several
classes of error — a bare boolean attribute on a component input, an unknown element, a mistyped
binding — appear nowhere else.

## 12. QA

Against the local stack (`run-lcl.sh`; use `restart console-ui`, never `kill` — the supervisor
tracks the pid and will otherwise route to an instance it no longer knows):

- Every page in **English and Arabic**, in **Forest, Midnight and Daylight**, at **1440 / 900 / 420**.
- Network tab: `?store=` on every private call, and no request fired twice on one page load.
- Write paths round-tripped, not just rendered.
