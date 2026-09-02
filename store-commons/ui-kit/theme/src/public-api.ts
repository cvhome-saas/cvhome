/*
 * @cvhome-saas/ui-kit/theme — the design system's token layer.
 *
 * The tokens themselves are CSS, shipped as assets beside this bundle:
 *
 *   @import '@cvhome-saas/ui-kit/theme/css/theme.css';
 *
 * They are Tailwind v4 `@theme` and `@utility` sources, so a consumer must `@import 'tailwindcss'`
 * ahead of them and must import them from *inside* its own stylesheet — listing them in
 * `angular.json`'s `styles` array puts them in a different PostCSS pass, where `@theme` is not a
 * known at-rule and silently emits nothing.
 *
 * Load order is not cosmetic: theme-forest.css also claims bare `:root`, so it is the base every
 * other identity overrides.
 *
 * What is here in TypeScript is the small part that cannot be CSS: `THEME` reads a resolved token
 * value back off the document for echarts and jsPDF, which paint outside the DOM and cannot use a
 * custom property.
 */
export * from './lib/theme.provider';
export * from './lib/theme.service';
