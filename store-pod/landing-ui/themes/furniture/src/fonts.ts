import {Archivo, Golos_Text, Tajawal} from 'next/font/google';
import type {ThemeFonts} from '@store-front/theme';

/**
 * The Home Floor Directory speaks in two voices and one companion.
 *
 *  - Archivo (variable, with the `wdth` axis) is the enamel sign: department names and floor numerals are
 *    set expanded and tracked, the way a directory board is lettered. Latin only — Cyrillic falls through
 *    to Golos Text below, which is why `:lang(ru)` leads with Golos in tokens.css.
 *  - Golos Text is the workhorse: body copy, form fields, tables, and every Cyrillic glyph on the site.
 *  - Tajawal carries Arabic on both roles; `:lang(ar)` leads with it, because next/font's metric fallback
 *    would otherwise swallow the Arabic glyphs before the stack ever reaches it.
 *
 * `preload: false` on every face: this module is imported by ThemeFrame.tsx inside the theme's lazily loaded
 * client chunk (see scripts/theme-client-barrier.mjs), and next/font emits preload links from the layout
 * entry's font manifest only, so the flag has nothing to act on here. @font-face still fetches the faces the
 * page actually uses; `display: swap` covers the extra hop.
 */
const sign = Archivo({
    subsets: ['latin', 'latin-ext'],
    axes: ['wdth'],
    display: 'swap', preload: false,
    variable: '--font-furniture-sign',
});

const text = Golos_Text({
    subsets: ['latin', 'latin-ext', 'cyrillic'],
    display: 'swap', preload: false,
    variable: '--font-furniture-text',
});

const arabic = Tajawal({
    subsets: ['arabic', 'latin'],
    weight: ['400', '500', '700', '900'],
    display: 'swap', preload: false,
    variable: '--font-furniture-arabic',
});

export const fonts: ThemeFonts = {
    variables: `${sign.variable} ${text.variable} ${arabic.variable}`,
    roles: {sans: 'Golos Text', display: 'Archivo (wdth)', mono: 'Golos Text (tabular)'},
};
