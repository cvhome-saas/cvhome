import {JetBrains_Mono, Noto_Kufi_Arabic, Oswald} from 'next/font/google';
import type {ThemeFonts} from '@store-front/theme';

/**
 * Display: a condensed grotesk in heavy caps (Oswald 600/700, Latin + Cyrillic) — the stencilled label voice.
 * Body/labels: a mono (JetBrains Mono, Latin + Cyrillic) — printed utility labels, exact numerals.
 * Arabic: Noto Kufi Arabic — a squared kufic that keeps the industrial register in RTL locales.
 */
const display = Oswald({subsets: ['latin', 'latin-ext', 'cyrillic'], weight: ['500', '600', '700'], display: 'swap', variable: '--font-beauty-display'});
const mono = JetBrains_Mono({subsets: ['latin', 'latin-ext', 'cyrillic'], weight: ['400', '500', '700'], display: 'swap', variable: '--font-beauty-mono'});
const arabic = Noto_Kufi_Arabic({subsets: ['arabic'], weight: ['400', '500', '700'], display: 'swap', variable: '--font-beauty-arabic'});

export const fonts: ThemeFonts = {
    variables: [display.variable, mono.variable, arabic.variable].join(' '),
    roles: {sans: 'JetBrains Mono', display: 'Oswald', mono: 'JetBrains Mono'},
};
