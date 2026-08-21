import 'server-only';
import {ColorTheme, getThemeColors, type Store} from '@store-front/types';
import {deriveColorTokens, type MerchantTokens, type ThemeDefinition} from '@store-front/theme';
import type {StoreHeaders} from '@/shell/request/headers';

/**
 * Merchant preset → colour-role tokens. The `Color-Theme` header is preferred (zero network); the store
 * record is the fallback; a store that never picked a palette gets LIGHT.
 */
export function resolveMerchantTokens(theme: ThemeDefinition, headers: StoreHeaders, store: Store | undefined): MerchantTokens {
    const name = (headers.colorTheme ?? store?.colorTheme ?? '').toUpperCase();
    const preset = (Object.values(ColorTheme) as string[]).includes(name) ? (name as ColorTheme) : ColorTheme.LIGHT;
    return deriveColorTokens(getThemeColors(preset), {
        minContrast: theme.tokens.minContrast,
        mapMerchantColors: theme.tokens.mapMerchantColors,
    });
}
