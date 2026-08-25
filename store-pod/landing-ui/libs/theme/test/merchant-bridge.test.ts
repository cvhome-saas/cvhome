import {describe, it} from 'node:test';
import assert from 'node:assert/strict';
import {type ColorSchema, ColorTheme, getThemeColors} from '@store-front/types';
import {contrastRatio, deriveColorTokens, isDarkColor, parseColor} from '../src/index.ts';
import {DEFAULT_COLORS as STARTER_DEFAULT} from '../../../themes/starter/src/colors.ts';
import {DEFAULT_COLORS as BASIC_DEFAULT} from '../../../themes/basic/src/colors.ts';
import {DEFAULT_COLORS as BEAUTY_DEFAULT} from '../../../themes/beauty/src/colors.ts';
import {DEFAULT_COLORS as FASHION_DEFAULT} from '../../../themes/fashion/src/colors.ts';

/** Every palette the bridge must honour: the fixed presets plus each theme's generated default. */
const PALETTES: [string, ColorSchema][] = [
    ...Object.values(ColorTheme).filter(v => v !== ColorTheme.DEFAULT).map((p): [string, ColorSchema] => [p, getThemeColors(p)!]),
    ['theme default starter', STARTER_DEFAULT],
    ['theme default basic', BASIC_DEFAULT],
    ['theme default beauty', BEAUTY_DEFAULT],
    ['theme default fashion', FASHION_DEFAULT],
];

const PAIRS: [string, string][] = [
    ['primary', 'primaryForeground'], ['secondary', 'secondaryForeground'], ['accent', 'accentForeground'],
    ['destructive', 'destructiveForeground'], ['success', 'successForeground'], ['warning', 'warningForeground'],
    ['info', 'infoForeground'], ['sale', 'saleForeground'], ['card', 'cardForeground'], ['popover', 'popoverForeground'],
    ['background', 'foreground'],
];

describe('merchant colour bridge', () => {
    for (const [preset, schema] of PALETTES) {
        it(`${preset}: every *-foreground pair reaches AA (4.5) and muted text is readable`, () => {
            const {tokens, style, scheme} = deriveColorTokens(schema);
            for (const [bg, fg] of PAIRS) {
                const ratio = contrastRatio((tokens as never)[fg], (tokens as never)[bg]);
                assert.ok(ratio >= 4.5, `${preset} ${fg} on ${bg}: ${ratio.toFixed(2)}`);
            }
            assert.ok(contrastRatio(tokens.mutedForeground, tokens.background) >= 4.5, `${preset} muted-foreground on background`);
            assert.equal(style['--primary'], tokens.primary);
            assert.equal(style['--primary-foreground'], tokens.primaryForeground);
            assert.equal(scheme, isDarkColor(schema.background) ? 'dark' : 'light');
            for (const v of Object.values(style)) assert.ok(parseColor(v), `${preset}: unparsable colour ${v}`);
        });
    }

    for (const [preset, schema] of PALETTES) {
        it(`${preset}: the bridge renders the preset's brand and semantic colours unmodified`, () => {
            const {tokens} = deriveColorTokens(schema);
            // The presets are authored so every role already reads with white or #111; if the guard had
            // to nudge one of these, the merchant would not be seeing the colour they picked.
            assert.equal(tokens.background, schema.background);
            assert.equal(tokens.foreground, schema.foreground);
            assert.equal(tokens.primary, schema.primary);
            assert.equal(tokens.secondary, schema.secondary);
            assert.equal(tokens.accent, schema.accent);
            assert.equal(tokens.destructive, schema.error);
            assert.equal(tokens.warning, schema.warning);
            assert.equal(tokens.success, schema.success);
            assert.equal(tokens.info, schema.info);
            assert.ok(contrastRatio(tokens.primaryHover, tokens.primaryForeground) >= 4.5, `${preset} primary-foreground on primary-hover`);
            assert.ok(contrastRatio(tokens.ring, tokens.background) >= 3, `${preset} ring on background`);
            assert.ok(contrastRatio(tokens.foreground, tokens.background) >= 7, `${preset} AAA canvas/ink`);
        });
    }

    it('detects dark presets', () => {
        assert.equal(deriveColorTokens(getThemeColors(ColorTheme.DARK)!).scheme, 'dark');
        assert.equal(deriveColorTokens(getThemeColors(ColorTheme.MIDNIGHT)!).scheme, 'dark');
        assert.equal(deriveColorTokens(getThemeColors(ColorTheme.LIGHT)!).scheme, 'light');
    });

    it('DEFAULT and unknown names have no preset — the storefront theme supplies its own palette', () => {
        assert.equal(getThemeColors(ColorTheme.DEFAULT), undefined);
        assert.equal(getThemeColors('default'), undefined);
        assert.equal(getThemeColors('bogus'), undefined);
        assert.equal(getThemeColors(undefined), undefined);
        assert.equal(getThemeColors('midnight'), getThemeColors(ColorTheme.MIDNIGHT));
    });

    it('lets a theme re-map roles and still guards contrast', () => {
        const {tokens} = deriveColorTokens(getThemeColors(ColorTheme.LIGHT)!, {
            mapMerchantColors: () => ({primary: '#ffff00', primaryForeground: '#ffffff'}),
        });
        assert.ok(contrastRatio(tokens.primaryForeground, tokens.primary) >= 4.5, 'bg nudged to keep the committed foreground readable');
    });
});
