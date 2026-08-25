import {type ColorSchema, ColorTheme, getThemeColors, isColorTheme} from '@store-front/types';
import type {ThemeDefinition} from '@store-front/theme';

export interface ResolvedColorScheme {
    /** The `ColorTheme` that rendered — `DEFAULT` when the theme's own palette was used. */
    preset: ColorTheme;
    schema: ColorSchema;
}

/**
 * Which palette a request renders. A fixed preset the merchant (or a QA override) named wins; `DEFAULT`,
 * nothing, and unknown names all mean "the theme's own palette" (`tokens.defaultColors`). Pure — the
 * request plumbing lives in `merchant-tokens.ts`.
 */
export function resolveColorScheme(theme: ThemeDefinition, requested: string | null | undefined): ResolvedColorScheme {
    const name = (requested ?? '').trim().toUpperCase();
    const schema = isColorTheme(name) ? getThemeColors(name) : undefined;
    return schema ? {preset: name as ColorTheme, schema} : {preset: ColorTheme.DEFAULT, schema: theme.tokens.defaultColors};
}
