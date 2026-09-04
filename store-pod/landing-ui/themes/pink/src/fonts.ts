import {Cairo} from 'next/font/google';
import type {ThemeFonts} from '@store-front/theme';

/**
 * The issue is set in two voices. Dela Gothic One is the masthead face: one ultra-heavy Japanese poster
 * gothic, the weight a girls' magazine cover line is printed at, used for the masthead, cover lines,
 * section openers, prices and every flag. M PLUS Rounded 1c (Latin, Latin-ext, Cyrillic) is the running
 * text of the issue — the rounded gothic of Japanese stationery and captions — for names, facts and prose.
 * Cairo is the Arabic companion on both roles: neither Latin cut carries Arabic, so Arabic falls through
 * to Cairo per glyph while Latin inside an Arabic page keeps its own voice.
 *
 * The two Japanese families are self-hosted in self-hosted-fonts.css. Their Google stylesheets contain
 * hundreds of CJK shards per weight, which can exhaust Turbopack's concurrent font fetches and leave its
 * internal font module unresolved. The CSS keeps only the Latin, Latin-ext and Cyrillic files the storefront
 * locales use. Cairo stays on next/font because its small Arabic/Latin set does not trigger that failure.
 */
const arabic = Cairo({subsets: ['arabic', 'latin'], display: 'swap', preload: false, variable: '--font-pink-arabic'});

export const fonts: ThemeFonts = {
    variables: arabic.variable,
    roles: {sans: 'M PLUS Rounded 1c', display: 'Dela Gothic One'},
};
