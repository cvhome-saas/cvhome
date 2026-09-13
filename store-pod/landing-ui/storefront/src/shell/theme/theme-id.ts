import {FALLBACK_THEME_ID, LEGACY_THEME_MAP} from './legacy-theme-map';

/**
 * The registered theme ids and how a requested id resolves to one. No theme package is imported here, on purpose:
 * `proxy.ts` picks each request's route tree with this module, and whatever it imports lands in the middleware
 * bundle. Managed by `scripts/new-theme.mjs` — one id per line between the markers.
 */
export const THEME_IDS = [
    // @themes:start
    'starter',
    'beauty',
    'fashion',
    'basic',
    'grocery',
    'pink',
    'hunger',
    'furniture',
    'glasses',
    'cosmetics',
    'sports',
    'jewellery',
    // @themes:end
] as const;

export type RegisteredThemeId = (typeof THEME_IDS)[number];

export const isRegisteredTheme = (id: string): id is RegisteredThemeId => (THEME_IDS as readonly string[]).includes(id);

/** A requested id (override, `Theme` header, env), lowercased: a registered theme, else its legacy mapping, else the fallback. */
export function resolveThemeId(requested: string | undefined | null): RegisteredThemeId {
    const id = (requested ?? '').trim().toLowerCase();
    if (!id) return FALLBACK_THEME_ID;
    if (isRegisteredTheme(id)) return id;
    const legacy = LEGACY_THEME_MAP[id];
    if (legacy && isRegisteredTheme(legacy)) return legacy;
    return FALLBACK_THEME_ID;
}
