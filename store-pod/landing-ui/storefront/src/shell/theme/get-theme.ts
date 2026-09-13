import 'server-only';
import {cache} from 'react';
import {cookies} from 'next/headers';
import type {ThemeDefinition} from '@store-front/theme';
import {getStoreHeaders} from '@/shell/request/headers';
import {themeLoaders} from './registry';
import {resolveThemeId, type RegisteredThemeId} from './theme-id';
import {THEME_OVERRIDE_COOKIE, themeOverrideEnabled} from './override';

/** Which theme id this request resolves to: override cookie (dev) → `theme` header → env → fallback. */
export const getThemeId = cache(async (): Promise<RegisteredThemeId> => {
    const [h, c] = await Promise.all([getStoreHeaders(), cookies()]);
    const override = themeOverrideEnabled() ? c.get(THEME_OVERRIDE_COOKIE)?.value : undefined;
    return resolveThemeId(override || h.theme || process.env.STOREFRONT_THEME);
});

/** The resolved ThemeDefinition for this request (memoised per request). */
export const getTheme = cache(async (): Promise<ThemeDefinition> => {
    const id = await getThemeId();
    const mod = await themeLoaders[id]();
    return mod.default;
});
