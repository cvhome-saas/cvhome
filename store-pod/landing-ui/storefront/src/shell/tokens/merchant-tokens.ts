import 'server-only';
import {cookies} from 'next/headers';
import {ColorTheme, getThemeColors, type Store} from '@store-front/types';
import {deriveColorTokens, type MerchantTokens, type ThemeDefinition} from '@store-front/theme';
import type {StoreHeaders} from '@/shell/request/headers';
import {COLOR_OVERRIDE_COOKIE, themeOverrideEnabled} from '@/shell/theme/override';

/**
 * Merchant preset → colour-role tokens. The override cookie (dev/QA, set from `?color=`) wins, then the
 * `Color-Theme` header (zero network), then the store record; a store that never picked a palette gets LIGHT.
 */
export async function resolveMerchantTokens(theme: ThemeDefinition, headers: StoreHeaders, store: Store | undefined): Promise<MerchantTokens> {
    const override = themeOverrideEnabled() ? (await cookies()).get(COLOR_OVERRIDE_COOKIE)?.value : undefined;
    const name = (override ?? headers.colorTheme ?? store?.colorTheme ?? '').toUpperCase();
    const preset = (Object.values(ColorTheme) as string[]).includes(name) ? (name as ColorTheme) : ColorTheme.LIGHT;
    return deriveColorTokens(getThemeColors(preset), {
        minContrast: theme.tokens.minContrast,
        mapMerchantColors: theme.tokens.mapMerchantColors,
    });
}
