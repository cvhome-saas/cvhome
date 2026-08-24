import 'server-only';
import {cache} from 'react';
import {cookies} from 'next/headers';
import {ColorTheme, type Store} from '@store-front/types';
import {deriveColorTokens, type MerchantTokens, type ThemeDefinition} from '@store-front/theme';
import {getStoreHeaders} from '@/shell/request/headers';
import {COLOR_OVERRIDE_COOKIE, themeOverrideEnabled} from '@/shell/theme/override';
import {resolveColorScheme} from './color-theme';

/**
 * The colour theme this request asks for, before validation: override cookie (dev/QA `?color=`) →
 * `Color-Theme` header (zero network) → the store record. `undefined` when none named one.
 */
export const getColorThemeRequest = cache(async (store: Store | undefined): Promise<string | undefined> => {
    const [h, c] = await Promise.all([getStoreHeaders(), cookies()]);
    const override = themeOverrideEnabled() ? c.get(COLOR_OVERRIDE_COOKIE)?.value : undefined;
    return override || h.colorTheme || store?.colorTheme || undefined;
});

/**
 * Requested colour theme → colour-role tokens. A fixed preset is rendered as-is (through the theme's
 * `mapMerchantColors` policy and the contrast guard); `DEFAULT` / unset / unknown render the theme's own
 * `tokens.defaultColors`. `preset` reports which one won (surfaced as `data-color-theme` on `<html>`).
 */
export function resolveMerchantTokens(theme: ThemeDefinition, requested: string | null | undefined): MerchantTokens & { preset: ColorTheme } {
    const {preset, schema} = resolveColorScheme(theme, requested);
    const tokens = deriveColorTokens(schema, {
        minContrast: theme.tokens.minContrast,
        mapMerchantColors: theme.tokens.mapMerchantColors,
    });
    return {...tokens, preset};
}
