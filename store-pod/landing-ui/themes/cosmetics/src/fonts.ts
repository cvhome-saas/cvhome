import {Inter} from 'next/font/google';
import type {ThemeFonts} from '@store-front/theme';

/**
 * Fonts are declared at module scope (next/font requirement).
 * `variables` goes on <html>; tokens.css maps `--font-body` onto `--font-cosmetics-sans`.
 * Offline builds: swap for `next/font/local` with files under themes/<id>/fonts/.
 *
 * `preload: false` on every face: all themes' font CSS lands in the SAME layout entry, so a preload
 * here fires on every storefront whatever theme is active. @font-face still fetches the faces the page
 * actually uses; `display: swap` covers the extra hop.
 */
const sans = Inter({
    subsets: ['latin', 'latin-ext', 'cyrillic'],
    display: 'swap', preload: false,
    variable: '--font-cosmetics-sans',
});

export const fonts: ThemeFonts = {
    variables: sans.variable,
    roles: {sans: 'Inter'},
};
