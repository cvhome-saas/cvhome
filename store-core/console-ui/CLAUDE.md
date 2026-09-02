# console-ui

The seller console. Angular 20 standalone + SSR, Tailwind v4 on a four-theme token layer,
Transloco (en/ar, RTL), served on port 8011 behind `console-ui.gateway.com`.

Its design system, error stack, HTTP client and theme layer live in **`@cvhome-saas/ui-kit`**
(`../../store-commons/ui-kit`), shared with the console uaa serves. `npm run build` builds the library first;
after a fresh clone, `npm run kit` once is what stops `Could not resolve "@cvhome-saas/ui-kit"`.

## Read first

- **[`ARCHITECTURE.md`](./ARCHITECTURE.md)** — the tiers, the shape of a feature, the shared control
  catalogue, and the rules a new page is expected to follow. Start here.
- **[`../../store-commons/ui-kit/README.md`](../../store-commons/ui-kit/README.md)** — the library: its six
  entry points, and the three settings that make a `file:`-linked Angular package work.
- **[`DESIGN.md`](./DESIGN.md)** — the visual system: tokens, the four themes, the named rules.
- **[`lessons.md`](./lessons.md)** — every capability the backend does not have yet, and what the
  console does instead. Append-only.
- **[`Todos.md`](./Todos.md)** — known debt that is agreed and not yet done. Check it before starting
  anything that touches the design system; the open items are there because they keep recurring.

## Commands

```bash
npm run dev        # ng serve
npm run build      # AOT. Run it before committing — strict template checking lives here
npm run lint       # eslint + stylelint + lessons citations + unused i18n keys
npm run test:ci    # non-interactive
```

## The rules easiest to break

Each of these has already cost someone an afternoon.

- **No raw `<input>`, `<select>`, `<textarea>` or checkbox** in a feature. Use the shared controls —
  §4 of `ARCHITECTURE.md` says which. A native select cannot be themed and a native checkbox reads
  as checked when it is not.
- **No `@features/*` import from another feature.** What two features need belongs one tier down.
- **The controls are in the kit, not in `@shared/ui`.** `app-text-field`, `app-panel`, `app-data-table` and
  the rest import from `@cvhome-saas/ui-kit/ui`; the error stack, `CrudService` and `snapshot()` from
  `@cvhome-saas/ui-kit`. What is left in `@shared/` is what only this console needs.
- **A kit control that uses a Tailwind utility needs `@source`.** `src/styles.css` scans the installed
  package for exactly this reason. Removing that line compiles cleanly and silently drops ~2.6 kB of
  utilities — badges, tag inputs and chart legends come out subtly mislaid rather than broken.
- **`@shared/styles/field.css` is not global** — `.field-grid`, `.field-wide`, `.split`, `.field-hint`
  and `.group-label` come from it, and every feature that uses them lists it in its own `styleUrls`
  beside its stylesheet. Omit it and `.split` is a plain block: the panes stack at every width, while
  the component's *own* rules apply normally, so it reads as a layout puzzle rather than a missing
  import. `npm run lint` checks this now — all three product-form steps had been missing it, which
  laid the whole product form out with no grid and no gap.
- **`.field-label` and `.required` belong to `app-form-field`** and are scoped to it. Writing either
  in a feature's own markup styles nothing. Use the component; for a label that names a *group* of
  controls, `.group-label` from `field.css` is the one that exists.
- **No literal hex, and no physical direction property** (`margin-left`, `left`, `text-align: left`).
  Stylelint fails on both.
- **No fixture standing in for a real answer.** If the platform cannot do it, the control says so
  and `lessons.md` records why, with a `TODO(lessons.md):` marker citing the exact heading.
- **`applyToForm` and `clearServerErrorsOnChange` always together**, or the form stays permanently
  invalid with the field looking fixed.
- **A backtick inside an inline `template:`** closes the template literal. It fails as a run of
  `',' expected` errors pointing at the *next* declaration, which is baffling until you have seen
  it once. Write component names in prose, or use an external `.html`.
- **`npm run build`, not just `npm test`.** Template type errors — a bare boolean attribute, an
  unknown element — appear only in the AOT build.

## Local stack

`lcl` supervises every service and records a pid per service. Use `lcl restart console-ui`;
killing the process and starting your own `ng serve` leaves the gateway routing to an instance it no
longer knows, which reads as "the app does not compile".
