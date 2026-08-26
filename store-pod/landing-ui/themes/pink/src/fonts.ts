import {Cairo, Dela_Gothic_One, M_PLUS_Rounded_1c} from 'next/font/google';
import type {ThemeFonts} from '@store-front/theme';

/**
 * The issue is set in two voices. Dela Gothic One is the masthead face: one ultra-heavy Japanese poster
 * gothic, the weight a girls' magazine cover line is printed at, used for the masthead, cover lines,
 * section openers, prices and every flag. M PLUS Rounded 1c (Latin, Latin-ext, Cyrillic) is the running
 * text of the issue — the rounded gothic of Japanese stationery and captions — for names, facts and prose.
 * Cairo is the Arabic companion on both roles: neither Latin cut carries Arabic, so Arabic falls through
 * to Cairo per glyph while Latin inside an Arabic page keeps its own voice. Both Latin cuts ship without
 * next/font's Arial-based metric fallback, which carries Arabic glyphs and would catch them before Cairo.
 */
const display = Dela_Gothic_One({
    weight: '400', subsets: ['latin', 'latin-ext'], display: 'swap',
    variable: '--font-pink-display', adjustFontFallback: false,
});
const sans = M_PLUS_Rounded_1c({
    weight: ['400', '500', '700', '800'], subsets: ['latin', 'latin-ext', 'cyrillic'], display: 'swap',
    variable: '--font-pink-sans', adjustFontFallback: false,
});
const arabic = Cairo({subsets: ['arabic', 'latin'], display: 'swap', variable: '--font-pink-arabic'});

export const fonts: ThemeFonts = {
    variables: [display.variable, sans.variable, arabic.variable].join(' '),
    roles: {sans: 'M PLUS Rounded 1c', display: 'Dela Gothic One'},
};
