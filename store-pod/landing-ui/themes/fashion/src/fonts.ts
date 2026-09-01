import {Anton, Changa, Rubik} from 'next/font/google';
import type {ThemeFonts} from '@store-front/theme';

/**
 * Display: Anton — the fly-poster face, one heavy condensed weight (Latin). Arabic display: Changa 800,
 * a compact heavy kufi-grotesk that keeps the poster voice in RTL locales. Body: Rubik (Latin, Cyrillic,
 * Arabic in one family) for everything that explains. Display elements are set at weight 400 so Anton
 * is never faux-bolded; Changa ships only its 800 face, so the same weight request resolves to it.
 *
 * `preload: false` on every face: this module is imported by ThemeFrame.tsx inside the theme's lazily loaded
 * client chunk (see scripts/theme-client-barrier.mjs), and next/font emits preload links from the layout
 * entry's font manifest only, so the flag has nothing to act on here. @font-face still fetches the faces the
 * page actually uses; `display: swap` covers the extra hop.
 */
const display = Anton({subsets: ['latin', 'latin-ext'], weight: '400', display: 'swap', preload: false, variable: '--font-fashion-display'});
const arabic = Changa({subsets: ['arabic', 'latin'], weight: ['800'], display: 'swap', preload: false, variable: '--font-fashion-arabic'});
const sans = Rubik({subsets: ['latin', 'latin-ext', 'cyrillic', 'arabic'], weight: ['400', '500', '700'], display: 'swap', preload: false, variable: '--font-fashion-sans'});

export const fonts: ThemeFonts = {
    variables: [display.variable, arabic.variable, sans.variable].join(' '),
    roles: {sans: 'Rubik', display: 'Anton'},
};
