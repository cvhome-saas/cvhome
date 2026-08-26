import {Inter} from 'next/font/google';
import type {ThemeFonts} from '@store-front/theme';

/**
 * Fonts are declared at module scope (next/font requirement) and only loaded when this theme renders.
 * `variables` goes on <html>; tokens.css maps `--font-body` onto `--font-glasses-sans`.
 * Offline builds: swap for `next/font/local` with files under themes/<id>/fonts/.
 */
const sans = Inter({
    subsets: ['latin', 'latin-ext', 'cyrillic'],
    display: 'swap',
    variable: '--font-glasses-sans',
});

export const fonts: ThemeFonts = {
    variables: sans.variable,
    roles: {sans: 'Inter'},
};
