import {Almarai, Fira_Sans_Extra_Condensed, Manrope} from 'next/font/google';
import type {ThemeFonts} from '@store-front/theme';

/**
 * The warehouse voices: Fira Sans Extra Condensed (700/800, Latin + Cyrillic) is the printed signage —
 * aisle boards, prices, running heads; Manrope carries everything that explains. Almarai is the Arabic
 * companion on both roles: blunt geometric Arabic that keeps the signage weight; neither Latin face
 * carries Arabic, so Arabic falls through per glyph while Latin inside an Arabic page keeps its voice.
 * The Latin faces ship without next/font's metric-adjusted fallback: that fallback (Arial-based) carries
 * Arabic glyphs and would catch them before Almarai does.
 */
const sans = Manrope({subsets: ['latin', 'latin-ext', 'cyrillic'], display: 'swap', variable: '--font-grocery-sans', adjustFontFallback: false});
const display = Fira_Sans_Extra_Condensed({weight: ['700', '800'], subsets: ['latin', 'latin-ext', 'cyrillic', 'cyrillic-ext'], display: 'swap', variable: '--font-grocery-display', adjustFontFallback: false});
const arabic = Almarai({weight: ['400', '700', '800'], subsets: ['arabic'], display: 'swap', variable: '--font-grocery-arabic'});

export const fonts: ThemeFonts = {
    variables: [sans.variable, display.variable, arabic.variable].join(' '),
    roles: {sans: 'Manrope', display: 'Fira Sans Extra Condensed'},
};
