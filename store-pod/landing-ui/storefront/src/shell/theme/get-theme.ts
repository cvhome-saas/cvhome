import 'server-only';
import {cache} from 'react';
import {cookies} from 'next/headers';
import type {ThemeDefinition} from '@store-front/theme';
import {getStoreHeaders} from '@/shell/request/headers';
import {isRegisteredTheme, type RegisteredThemeId, themeLoaders} from './registry';
import {FALLBACK_THEME_ID, LEGACY_THEME_MAP} from './legacy-theme-map';
import {THEME_OVERRIDE_COOKIE} from './override';

const overrideEnabled = process.env.NODE_ENV !== 'production' || process.env.STOREFRONT_THEME_OVERRIDE === 'true';

export function resolveThemeId(requested: string | undefined | null): RegisteredThemeId {
    const id = (requested ?? '').trim().toLowerCase();
    if (!id) return FALLBACK_THEME_ID;
    if (isRegisteredTheme(id)) return id;
    const legacy = LEGACY_THEME_MAP[id];
    if (legacy && isRegisteredTheme(legacy)) return legacy;
    return FALLBACK_THEME_ID;
}

/** Which theme id this request resolves to: override cookie (dev) → `theme` header → env → fallback. */
export const getThemeId = cache(async (): Promise<RegisteredThemeId> => {
    const [h, c] = await Promise.all([getStoreHeaders(), cookies()]);
    const override = overrideEnabled ? c.get(THEME_OVERRIDE_COOKIE)?.value : undefined;
    return resolveThemeId(override || h.theme || process.env.STOREFRONT_THEME);
});

/** The resolved ThemeDefinition for this request (memoised per request). */
export const getTheme = cache(async (): Promise<ThemeDefinition> => {
    const id = await getThemeId();
    const mod = await themeLoaders[id]();
    return mod.default;
});
