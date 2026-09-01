# `@cvhome-saas/ui-kit`

The console design system and application infrastructure, shared by `store-core/console-ui` and the
Angular app embedded in `store-core/uaa`. Architecture and the rules a consumer follows:
[`ARCHITECTURE.md`](../../store-core/console-ui/ARCHITECTURE.md); the visual system:
[`DESIGN.md`](./DESIGN.md).

## Entry points

| Import | Holds |
|---|---|
| `@cvhome-saas/ui-kit` | Config, the HTTP client, the error stack, auth, platform access, table types, routing helpers |
| `@cvhome-saas/ui-kit/ui` | The control catalogue — `app-text-field`, `app-form-field`, `app-data-table`, … |
| `@cvhome-saas/ui-kit/theme` | The four-theme token layer (CSS) and the theme service |
| `@cvhome-saas/ui-kit/i18n` | Transloco plumbing and the kit's own dictionaries |
| `@cvhome-saas/ui-kit/forms` | Validators and form-error helpers |
| `@cvhome-saas/ui-kit/uaa` | Clients for uaa's admin API, which both consoles call |

## Build it before you install a consumer

```bash
npm install && npm run build      # → dist/ui-kit
```

Consumers link the **built** package (`"@cvhome-saas/ui-kit": "file:…/dist/ui-kit"`).

**npm does not complain when that directory is missing.** Verified against npm 10.9.2: `npm install`
exits 0, reports "added 868 packages", and writes a **dangling symlink**. The failure surfaces much
later, from the build:

```
✘ [ERROR] Could not resolve "@cvhome-saas/ui-kit"
✘ [ERROR] TS2307: Cannot find module '@cvhome-saas/ui-kit' or its corresponding type declarations.
```

which reads like a broken import in the app rather than a library nobody built. If you ever see that
pair, run `npm run kit`. Three things stop it happening, and a fourth is on you:

1. each consumer has an `npm run kit` script that builds this module, and its `build` and `start`
   scripts chain it;
2. each consumer's `npmInstall` Gradle task `dependsOn ':store-commons:ui-kit:build'`;
3. `lcl.yml` runs the same build as a `prepare` step;
4. if you clone and reach straight for `ng serve`, run `npm run kit` in the app first.

After the first install nothing needs repeating: `file:` makes npm write a **symlink**, so a
rebuild here is visible to a running `ng serve` with no reinstall.

## `preserveSymlinks` is load-bearing

Every consumer sets `"preserveSymlinks": true` on its `build`, `test` and `server` targets. Without
it Angular resolves through the symlink to this directory and then looks for `@angular/core`
relative to *here*, where the app's `node_modules` is not. The failure is a wall of
`Cannot find module '@angular/core'` pointing at library files, which reads like a broken library
rather than a resolution setting.

## Styles

The theme CSS ships as assets at `dist/ui-kit/theme/css/`. It is Tailwind v4 `@theme`/`@utility`
source, so a consumer `@import`s it from inside its own stylesheet, after `@import 'tailwindcss'` —
never through `angular.json`'s `styles` array, which is a separate PostCSS pass where `@theme`
quietly emits nothing.

**A consumer must also add the kit to Tailwind's source scanning**, next to that import:

```css
@source '../node_modules/@cvhome-saas/ui-kit';
```

Most controls do style themselves with semantic class names over `var(--token)`, but not all of
them: badges, the tag input, the tree and the chart legends use real utilities — `inline-flex`,
`items-center`, `gap-1`, `ring-1`, `bg-chart-3-wash`, `group-aria-expanded:rotate-180`. Component
CSS is compiled by ng-packagr, which does not run Tailwind, so those utilities can only be emitted
by the *consumer's* Tailwind build, and it only emits what it has seen.

Leave the `@source` out and nothing fails: the build is green, the specs pass, and roughly 2.6 kB of
utilities are silently missing from the stylesheet, which shows up as controls that are subtly
mislaid rather than as an error. Scan the installed package rather than the kit's source tree — that
is the path a consumer actually has, and it turns out to be a superset.
