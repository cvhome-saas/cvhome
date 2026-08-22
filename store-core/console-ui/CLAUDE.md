# console-ui

The seller console. Angular 20 standalone + SSR, Tailwind v4 on a three-theme token layer,
Transloco (en/ar, RTL), served on port 8011 behind `console-ui.gateway.com`.

## Read first

- **[`ARCHITECTURE.md`](./ARCHITECTURE.md)** — the tiers, the shape of a feature, the shared control
  catalogue, and the rules a new page is expected to follow. Start here.
- **[`DESIGN.md`](./DESIGN.md)** — the visual system: tokens, the three themes, the named rules.
- **[`lessons.md`](./lessons.md)** — every capability the backend does not have yet, and what the
  console does instead. Append-only.

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

`run-lcl.sh` supervises every service and records a pid per service. Use `restart console-ui`;
killing the process and starting your own `ng serve` leaves the gateway routing to an instance it no
longer knows, which reads as "the app does not compile".
