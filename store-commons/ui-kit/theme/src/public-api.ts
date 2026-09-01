/*
 * @cvhome-saas/ui-kit/theme — the design system's token layer.
 *
 * The tokens themselves are CSS, not TypeScript, and are shipped as assets beside this bundle:
 *
 *   @import '@cvhome-saas/ui-kit/theme/css/theme.css';
 *
 * They are Tailwind v4 `@theme` and `@utility` sources, so a consumer must `@import 'tailwindcss'`
 * ahead of them and must import them *inside* its own stylesheet — listing them in `angular.json`'s
 * `styles` array puts them in a different PostCSS pass, where `@theme` is not a known at-rule and
 * silently emits nothing.
 *
 * Load order is not cosmetic: theme-forest.css also claims bare `:root`, so it is the base every
 * other identity overrides. See the block comment in a consumer's `styles.css`.
 */

export {};
