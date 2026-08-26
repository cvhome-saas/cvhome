import {Alexandria, Alumni_Sans, Geologica} from 'next/font/google';
import type {ThemeFonts} from '@store-front/theme';

/**
 * The Letterbox Menu sets in three faces, all declared at module scope (next/font requirement) and only
 * loaded when this theme renders. `variables` goes on <html>; tokens.css maps the roles onto them.
 *
 * Alumni Sans is the printed voice — a tall collegiate gothic for the masthead, the section bands, the
 * dish numbers and every price. Geologica sets what has to be read: dish names, descriptions, forms.
 * Alexandria leads both roles in Arabic (see the :lang(ar) block in tokens.css).
 */
const display = Alumni_Sans({
    subsets: ['latin', 'latin-ext', 'cyrillic'],
    weight: ['600', '700', '800', '900'],
    display: 'swap',
    variable: '--font-hunger-display',
});

const sans = Geologica({
    subsets: ['latin', 'latin-ext', 'cyrillic'],
    weight: ['300', '400', '500', '600', '700'],
    display: 'swap',
    variable: '--font-hunger-sans',
});

const arabic = Alexandria({
    subsets: ['arabic', 'latin'],
    weight: ['300', '400', '600', '700', '800'],
    display: 'swap',
    variable: '--font-hunger-arabic',
});

export const fonts: ThemeFonts = {
    variables: `${display.variable} ${sans.variable} ${arabic.variable}`,
    roles: {sans: 'Geologica', display: 'Alumni Sans'},
};
