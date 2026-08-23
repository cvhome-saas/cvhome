import {describe, it} from 'node:test';
import assert from 'node:assert/strict';
import {ColorTheme, getThemeColors} from '@store-front/types';
import {contrastRatio, deriveColorTokens, isDarkColor, parseColor} from '../src/index.ts';

const PAIRS: [string, string][] = [
    ['primary', 'primaryForeground'], ['secondary', 'secondaryForeground'], ['accent', 'accentForeground'],
    ['destructive', 'destructiveForeground'], ['success', 'successForeground'], ['warning', 'warningForeground'],
    ['info', 'infoForeground'], ['sale', 'saleForeground'], ['card', 'cardForeground'], ['popover', 'popoverForeground'],
    ['background', 'foreground'],
];

describe('merchant colour bridge', () => {
    for (const preset of Object.values(ColorTheme)) {
        it(`${preset}: every *-foreground pair reaches AA (4.5) and muted text is readable`, () => {
            const {tokens, style, scheme} = deriveColorTokens(getThemeColors(preset));
            for (const [bg, fg] of PAIRS) {
                const ratio = contrastRatio((tokens as never)[fg], (tokens as never)[bg]);
                assert.ok(ratio >= 4.5, `${preset} ${fg} on ${bg}: ${ratio.toFixed(2)}`);
            }
            assert.ok(contrastRatio(tokens.mutedForeground, tokens.background) >= 4.5, `${preset} muted-foreground on background`);
            assert.equal(style['--primary'], tokens.primary);
            assert.equal(style['--primary-foreground'], tokens.primaryForeground);
            assert.equal(scheme, isDarkColor(getThemeColors(preset).background) ? 'dark' : 'light');
            for (const v of Object.values(style)) assert.ok(parseColor(v), `${preset}: unparsable colour ${v}`);
        });
    }

    for (const preset of Object.values(ColorTheme)) {
        it(`${preset}: the bridge renders the preset's brand and semantic colours unmodified`, () => {
            const schema = getThemeColors(preset);
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
        assert.equal(deriveColorTokens(getThemeColors(ColorTheme.DARK)).scheme, 'dark');
        assert.equal(deriveColorTokens(getThemeColors(ColorTheme.MIDNIGHT)).scheme, 'dark');
        assert.equal(deriveColorTokens(getThemeColors(ColorTheme.LIGHT)).scheme, 'light');
    });

    it('lets a theme re-map roles and still guards contrast', () => {
        const {tokens} = deriveColorTokens(getThemeColors(ColorTheme.LIGHT), {
            mapMerchantColors: () => ({primary: '#ffff00', primaryForeground: '#ffffff'}),
        });
        assert.ok(contrastRatio(tokens.primaryForeground, tokens.primary) >= 4.5, 'bg nudged to keep the committed foreground readable');
    });
});
