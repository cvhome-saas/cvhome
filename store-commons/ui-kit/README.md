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

The controls need no Tailwind content-scanning: every component here styles itself with semantic
class names and `var(--token)` in its own stylesheet, and uses no Tailwind utility classes in its
template. A consumer needs Tailwind v4 to *emit the token layer*, not to style the controls.
