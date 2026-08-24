/**
 * Dev / QA request overrides. The proxy persists `?theme=<id>` and `?color=<ColorTheme|default>` as
 * cookies; `getTheme()` and `getColorThemeRequest()` honour them. An empty value (`?theme=`) clears one.
 */
export const THEME_OVERRIDE_COOKIE = 'storefront-theme';
export const COLOR_OVERRIDE_COOKIE = 'storefront-color';
export const themeOverrideEnabled = () => process.env.NODE_ENV !== 'production' || process.env.STOREFRONT_THEME_OVERRIDE === 'true';
