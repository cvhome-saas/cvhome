import {Inter} from 'next/font/google';
import type {ThemeFonts} from '@store-front/theme';

/**
 * Fonts are declared at module scope (next/font requirement).
 * `variables` goes on <html>; tokens.css maps `--font-body` onto `--font-sports-sans`.
 * Offline builds: swap for `next/font/local` with files under themes/<id>/fonts/.
 *
 * `preload: false` on every face. It dates from when every theme's font CSS shared one layout entry and a
 * preload fired on every storefront whatever theme was active; each theme now has its own route tree, so
 * preloading is this theme's call, left for a measured change. @font-face still fetches the faces the page
 * actually uses; `display: swap` covers the extra hop.
 */
const sans = Inter({
    subsets: ['latin', 'latin-ext', 'cyrillic'],
    display: 'swap', preload: false,
    variable: '--font-sports-sans',
});

export const fonts: ThemeFonts = {
    variables: sans.variable,
    roles: {sans: 'Inter'},
};
