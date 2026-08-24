/** Cookies the proxy sets from `?theme=<id>` / `?color=<preset>` (dev / QA only); honoured by getTheme() / resolveMerchantTokens(). */
export const THEME_OVERRIDE_COOKIE = 'storefront-theme';
export const COLOR_OVERRIDE_COOKIE = 'storefront-color';
export const themeOverrideEnabled = () => process.env.NODE_ENV !== 'production' || process.env.STOREFRONT_THEME_OVERRIDE === 'true';
