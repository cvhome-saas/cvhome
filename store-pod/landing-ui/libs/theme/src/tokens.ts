/**
 * Design-token schema shared by every theme.
 *
 * Everything is a plain CSS custom property. `storefront/src/app/globals.css` is the ONLY place that maps
 * these into Tailwind (`@theme inline`), so a theme never writes utility classes — it sets variables in
 * its `tokens.css` under `[data-theme="<id>"]` and the utilities (`bg-primary`, `rounded-card`,
 * `max-w-content`, `py-section`, `ease-standard`, …) follow. Token names deliberately avoid Tailwind's
 * own namespaces (`--text-*`, `--radius-*`, `--shadow-*`, `--font-*` roles …) so the mapping never cycles.
 *
 * Two owners:
 *  - COLOR ROLES are produced per request by the merchant bridge (`merchant-bridge.ts`) from the store's
 *    `ColorTheme` preset and written as an inline `style` on `<html>`. A theme may re-map them through
 *    `ThemeDefinition.tokens.mapMerchantColors` but never hard-codes them in CSS.
 *  - Everything else (fonts, type scale, spacing/density, radius, borders, shadows, containers, motion,
 *    layout) is the theme author's decision in `tokens.css`.
 */

/** Colour roles — values are CSS colours (hex from the presets, but any CSS colour is valid). */
export interface ColorRoleTokens {
    background: string;
    foreground: string;
    card: string;
    cardForeground: string;
    popover: string;
    popoverForeground: string;
    primary: string;
    primaryForeground: string;
    primaryHover: string;
    secondary: string;
    secondaryForeground: string;
    muted: string;
    mutedForeground: string;
    accent: string;
    accentForeground: string;
    destructive: string;
    destructiveForeground: string;
    success: string;
    successForeground: string;
    warning: string;
    warningForeground: string;
    info: string;
    infoForeground: string;
    sale: string;
    saleForeground: string;
    border: string;
    input: string;
    ring: string;
}

export const COLOR_ROLE_KEYS = [
    'background', 'foreground', 'card', 'cardForeground', 'popover', 'popoverForeground',
    'primary', 'primaryForeground', 'primaryHover', 'secondary', 'secondaryForeground',
    'muted', 'mutedForeground', 'accent', 'accentForeground',
    'destructive', 'destructiveForeground', 'success', 'successForeground',
    'warning', 'warningForeground', 'info', 'infoForeground', 'sale', 'saleForeground',
    'border', 'input', 'ring',
] as const satisfies readonly (keyof ColorRoleTokens)[];

/** `primaryForeground` → `--primary-foreground` */
export function toCssVarName(key: string): string {
    return '--' + key.replace(/([a-z0-9])([A-Z])/g, '$1-$2').toLowerCase();
}

/**
 * Theme-owned tokens (set in `tokens.css`). Listed so the starter theme, the docs and the scaffold checker
 * agree on the exact names. Every theme MUST define each of these under its `[data-theme]` selector.
 */
export const THEME_OWNED_TOKENS = [
    // fonts — point at the next/font variables exported by the theme's fonts.ts
    '--font-body', '--font-heading', '--font-code',
    // type scale (mapped to Tailwind text-*/leading-*/tracking-*)
    '--type-xs', '--type-sm', '--type-base', '--type-lg', '--type-xl', '--type-2xl', '--type-3xl', '--type-4xl', '--type-5xl', '--type-6xl',
    '--line-tight', '--line-snug', '--line-normal', '--line-relaxed',
    '--track-tight', '--track-normal', '--track-wide',
    // spacing & density (--space-unit feeds Tailwind's --spacing scale)
    '--space-unit', '--density', '--section-y', '--gutter',
    // radius (rounded-control / rounded-card / rounded-image / rounded-badge / rounded-overlay; also rounded-sm/md/lg/xl)
    '--r-control', '--r-card', '--r-image', '--r-badge', '--r-overlay',
    // borders & elevation (shadow-sm/md/lg/overlay)
    '--border-width', '--elev-sm', '--elev-md', '--elev-lg', '--elev-overlay',
    // containers (max-w-narrow / max-w-content / max-w-wide)
    '--width-narrow', '--width-content', '--width-wide',
    // motion (duration-(--motion-*) / ease-standard / ease-emphasized)
    '--motion-fast', '--motion-base', '--motion-slow', '--easing-standard', '--easing-emphasized',
    // layout (h-header / top-header; aspect-product)
    '--header-h', '--header-h-lg', '--product-aspect',
] as const;

export type ThemeOwnedToken = typeof THEME_OWNED_TOKENS[number];
