/** Cookie the proxy sets from `?theme=<id>` (dev / QA only) and `getTheme()` honours. */
export const THEME_OVERRIDE_COOKIE = 'storefront-theme';
export const themeOverrideEnabled = () => process.env.NODE_ENV !== 'production' || process.env.STOREFRONT_THEME_OVERRIDE === 'true';
