import {Inter} from 'next/font/google';
import type {ThemeFonts} from '@store-front/theme';

/**
 * Fonts are declared at module scope (next/font requirement).
 * `variables` goes on <html>; tokens.css maps `--font-body` onto `--font-sports-sans`.
 * Offline builds: swap for `next/font/local` with files under themes/<id>/fonts/.
 *
 * `preload: false` on every face: this module is imported by ThemeFrame.tsx inside the theme's lazily loaded
 * client chunk (see scripts/theme-client-barrier.mjs), and next/font emits preload links from the layout
 * entry's font manifest only, so the flag has nothing to act on here. @font-face still fetches the faces the
 * page actually uses; `display: swap` covers the extra hop.
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
