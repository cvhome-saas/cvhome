import {Cairo, Sofia_Sans, Sofia_Sans_Extra_Condensed} from 'next/font/google';
import type {ThemeFonts} from '@store-front/theme';

/**
 * One super-family for the catalogue: Sofia Sans (Latin, Latin-ext, Cyrillic) for everything that explains,
 * Sofia Sans Extra Condensed for everything that names and every price — the mail-order book's bold
 * condensed voice. Cairo is the Arabic companion on both roles: neither Sofia cut carries Arabic, so Arabic
 * text falls through to Cairo per glyph while Latin inside an Arabic page keeps the Sofia voice. All three
 * are variable, so weights are free and nothing is faux-bolded. The two Sofia cuts ship without next/font's
 * metric-adjusted local fallback: that fallback (Arial-based) carries Arabic glyphs and would catch them before
 * Cairo does, so Arabic would render in Arial instead of Cairo.
 *
 * `preload: false` on every face: all themes' font CSS lands in the SAME layout entry, so a preload
 * here fires on every storefront whatever theme is active. @font-face still fetches the faces the page
 * actually uses; `display: swap` covers the extra hop.
 */
const sans = Sofia_Sans({subsets: ['latin', 'latin-ext', 'cyrillic'], display: 'swap', preload: false, variable: '--font-basic-sans', adjustFontFallback: false});
const display = Sofia_Sans_Extra_Condensed({subsets: ['latin', 'latin-ext', 'cyrillic'], display: 'swap', preload: false, variable: '--font-basic-display', adjustFontFallback: false});
const arabic = Cairo({subsets: ['arabic', 'latin'], display: 'swap', preload: false, variable: '--font-basic-arabic'});

export const fonts: ThemeFonts = {
    variables: [sans.variable, display.variable, arabic.variable].join(' '),
    roles: {sans: 'Sofia Sans', display: 'Sofia Sans Extra Condensed'},
};
