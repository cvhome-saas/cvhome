import type {ColorSchema} from '@store-front/types';
import {COLOR_ROLE_KEYS, type ColorRoleTokens, toCssVarName} from './tokens';
import {contrastRatio, ensureContrast, isDarkColor, lighten, mix, pickForeground} from './color-math';

export type ColorScheme = 'light' | 'dark';

export interface DeriveOptions {
    /** Minimum contrast every `*-foreground` pair must reach against its background. Default 4.5 (AA). */
    minContrast?: number;
    /** Theme hook: override any role after the defaults are derived. */
    mapMerchantColors?: (schema: ColorSchema, meta: { isDark: boolean }) => Partial<ColorRoleTokens>;
}

export interface MerchantTokens {
    scheme: ColorScheme;
    tokens: ColorRoleTokens;
    /** `{'--primary': '#1565C0', …}` — spread onto the `<html>` `style` attribute. */
    style: Record<string, string>;
}

const FG_PAIRS: readonly (readonly [keyof ColorRoleTokens, keyof ColorRoleTokens])[] = [
    ['primary', 'primaryForeground'], ['secondary', 'secondaryForeground'], ['accent', 'accentForeground'],
    ['destructive', 'destructiveForeground'], ['success', 'successForeground'], ['warning', 'warningForeground'],
    ['info', 'infoForeground'], ['sale', 'saleForeground'], ['card', 'cardForeground'], ['popover', 'popoverForeground'],
    ['muted', 'mutedForeground'], ['background', 'foreground'],
];

/**
 * Maps the merchant's 29-colour `ColorSchema` preset into the full colour-role token set.
 *
 * The presets were written for a world without `*-foreground` pairs, so this is where contrast is made
 * a guarantee rather than luck: every foreground is chosen white/near-black by contrast, and the
 * background side is nudged until the pair reaches `minContrast`.
 */
export function deriveColorTokens(schema: ColorSchema, options: DeriveOptions = {}): MerchantTokens {
    const minContrast = options.minContrast ?? 4.5;
    const isDark = isDarkColor(schema.background);

    // 1. page pair first — everything else derives from it
    let foreground = contrastRatio(schema.foreground, schema.background) >= minContrast
        ? schema.foreground
        : pickForeground(schema.background);
    const background = ensureContrast(foreground, schema.background, minContrast);

    const card = isDark ? lighten(background, 0.04) : background;
    const muted = mix(background, foreground, 0.06);

    const base: ColorRoleTokens = {
        background,
        foreground,
        card,
        cardForeground: foreground,
        popover: card,
        popoverForeground: foreground,
        primary: schema.primary,
        primaryForeground: pickForeground(schema.primary),
        primaryHover: schema.hoverPrimary,
        secondary: schema.secondary,
        secondaryForeground: pickForeground(schema.secondary),
        muted,
        mutedForeground: mix(foreground, background, 0.4),
        accent: schema.accent,
        accentForeground: pickForeground(schema.accent),
        destructive: schema.error,
        destructiveForeground: pickForeground(schema.error),
        success: schema.success,
        successForeground: pickForeground(schema.success),
        warning: schema.warning,
        warningForeground: pickForeground(schema.warning),
        info: schema.info,
        infoForeground: pickForeground(schema.info),
        sale: schema.error,
        saleForeground: pickForeground(schema.error),
        border: schema.border,
        input: schema.border,
        ring: schema.ring,
    };

    // 2. theme policy
    const themed: ColorRoleTokens = {...base, ...(options.mapMerchantColors?.(schema, {isDark}) ?? {})};

    // 3. contrast guard: keep each committed foreground, give way on its background
    for (const [bgKey, fgKey] of FG_PAIRS) {
        if (bgKey === 'muted') continue;
        themed[bgKey] = ensureContrast(themed[fgKey], themed[bgKey], minContrast);
    }
    // muted text sits on the page background: walk it toward the foreground until it reads
    foreground = themed.foreground;
    for (let i = 0; i < 10 && contrastRatio(themed.mutedForeground, themed.background) < minContrast; i++) {
        themed.mutedForeground = mix(themed.mutedForeground, foreground, 0.25);
    }
    if (contrastRatio(themed.mutedForeground, themed.background) < minContrast) themed.mutedForeground = foreground;

    return {
        scheme: isDark ? 'dark' : 'light',
        tokens: themed,
        style: toCssVarStyle(themed),
    };
}

export function toCssVarStyle(tokens: ColorRoleTokens): Record<string, string> {
    const style: Record<string, string> = {};
    for (const key of COLOR_ROLE_KEYS) {
        style[toCssVarName(key)] = tokens[key];
    }
    return style;
}
