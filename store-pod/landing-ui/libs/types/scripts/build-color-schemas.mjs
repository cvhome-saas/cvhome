#!/usr/bin/env node
/**
 * Builds `src/color-schema.ts` from the preset seeds below.
 *
 *   node scripts/build-color-schemas.mjs          # rewrite src/color-schema.ts
 *   node scripts/build-color-schemas.mjs --check  # verify only (exit 1 on a rule violation)
 *
 * Why a generator: the 30 `ColorTheme` presets are the merchant's brand palettes. Hand-picked hex drifted
 * into inconsistent hover steps, off-meaning semantic colours and primaries the contrast bridge had to
 * repaint. Here every preset is a handful of OKLCH seeds (perceptual lightness/chroma/hue); the derived
 * roles follow one rule set, and the rules below are enforced before anything is written:
 *
 *   - canvas/ink (`background`/`foreground`)      ≥ 7:1   (AAA body text, natively)
 *   - primary, secondary, accent, error, warning,
 *     success, info                               ≥ 4.5:1 against white OR #111 — the bridge's
 *                                                 `pickForeground` finds a readable text colour without
 *                                                 nudging the brand colour itself
 *   - every hover / pressed step                  keeps ≥ 4.5:1 with the base colour's text colour
 *   - `ring`, `outline`                           ≥ 3:1 against the canvas (WCAG non-text contrast)
 *   - `border`                                    visibly distinct from the canvas (≥ 1.2:1)
 *   - semantic hues stay in their families        error ≈ red, warning ≈ amber, success ≈ green, info ≈ blue
 *
 * Key names are fixed (they are the public `ColorSchema` contract and the backend `ColorTheme` enum is the
 * preset list), so only values are produced here.
 *
 * The enum also carries `DEFAULT` — not a preset but "use the storefront theme's own palette". Those
 * per-theme palettes are seeded in `THEME_DEFAULTS` below, built and verified by the same rules, and
 * written to `themes/<id>/src/colors.ts` (`DEFAULT_COLORS`, wired as `tokens.defaultColors`).
 */
import {writeFileSync, readFileSync} from 'node:fs';
import {fileURLToPath} from 'node:url';
import {dirname, resolve} from 'node:path';

/* ------------------------------------------------------------------------------------------------ */
/* Seeds                                                                                             */
/* ------------------------------------------------------------------------------------------------ */

/** OKLCH triplet: lightness 0–1, chroma 0–0.37, hue degrees. */
const o = (l, c, h) => ({l, c, h});

// One semantic set per scheme so meaning is identical across the catalogue. Light schemes use deep
// tones for white text (warning is the exception: amber with dark text, as platform conventions do);
// dark schemes use bright tones with dark text.
const SEMANTIC = {
    light: {error: o(0.50, 0.19, 27), warning: o(0.76, 0.16, 72), success: o(0.49, 0.14, 150), info: o(0.49, 0.14, 245)},
    dark: {error: o(0.72, 0.17, 25), warning: o(0.84, 0.15, 82), success: o(0.79, 0.15, 152), info: o(0.79, 0.11, 235)},
};

/**
 * Each preset: a short intent line (kept in the generated file), canvas + ink, the three brand roles.
 *  - `primary`   the one action colour (CTAs, active states); the theme may demote it, never repaint it
 *  - `secondary` the supporting tone — usually a tonal "wash" of the canvas family for chips, secondary
 *                actions and soft surfaces, sometimes a second committed hue
 *  - `accent`    the statement colour — one trend/brand hue for badges, sale tags and feature marks
 * Palette direction is fashion/brand-led: trend hues (butter yellow, mocha mousse, digital lavender,
 * cherry, cobalt, pistachio, chocolate), black or bone CTAs where the world is monochrome.
 * Lightness bands: ≤ 0.49 reads with white text, ≥ 0.62 with #111 text; the 0.50–0.61 band fails both
 * and is avoided on purpose.
 */
const PRESETS = {
    LIGHT: {
        note: 'Gallery: warm white, soft-black ink and a black CTA; bone wash support; cherry-red statement.',
        background: o(0.99, 0.004, 90), foreground: o(0.20, 0.010, 60),
        primary: o(0.22, 0.010, 60), secondary: o(0.88, 0.012, 80), accent: o(0.48, 0.21, 25),
    },
    DARK: {
        note: 'After hours: warm graphite, bone ink and a bone CTA; elevated-charcoal support; chartreuse statement.',
        background: o(0.17, 0.008, 60), foreground: o(0.92, 0.012, 85),
        primary: o(0.90, 0.030, 85), secondary: o(0.32, 0.010, 60), accent: o(0.88, 0.20, 120),
    },
    NATURE: {
        note: 'Pistachio: oat canvas, deep-olive ink and action; pistachio wash support; tomato statement.',
        background: o(0.96, 0.018, 90), foreground: o(0.28, 0.050, 120),
        primary: o(0.42, 0.10, 125), secondary: o(0.85, 0.09, 125), accent: o(0.48, 0.20, 30),
    },
    OCEAN: {
        note: 'Riviera: sea-salt white, deep-marine ink and action; sky-wash support; tangerine statement.',
        background: o(0.97, 0.012, 220), foreground: o(0.24, 0.070, 260),
        primary: o(0.30, 0.12, 262), secondary: o(0.86, 0.06, 220), accent: o(0.68, 0.17, 50),
    },
    MIDNIGHT: {
        note: 'Velvet: aubergine-black canvas, digital-lavender CTA with dark text; plum support; champagne statement.',
        background: o(0.16, 0.040, 290), foreground: o(0.93, 0.010, 300),
        primary: o(0.80, 0.10, 300), secondary: o(0.30, 0.06, 290), accent: o(0.86, 0.10, 85),
    },
    FOREST_WHISPER: {
        note: 'Sage: sage-mist canvas, eucalyptus action; tonal sage support; rust statement.',
        background: o(0.955, 0.012, 130), foreground: o(0.25, 0.040, 150),
        primary: o(0.38, 0.07, 150), secondary: o(0.88, 0.04, 130), accent: o(0.48, 0.13, 40),
    },
    DESERT_MIRAGE: {
        note: 'Terracotta: sand canvas, clay ink, terracotta action; dune wash support; turquoise statement.',
        background: o(0.95, 0.025, 70), foreground: o(0.30, 0.060, 40),
        primary: o(0.48, 0.15, 38), secondary: o(0.86, 0.05, 70), accent: o(0.74, 0.12, 195),
    },
    MIDNIGHT_DUSK: {
        note: 'Cobalt: cool-white canvas, ink-blue text, cobalt action; periwinkle wash support; butter-yellow statement.',
        background: o(0.96, 0.010, 270), foreground: o(0.22, 0.060, 270),
        primary: o(0.45, 0.23, 264), secondary: o(0.88, 0.05, 275), accent: o(0.92, 0.12, 95),
    },
    ROSE: {
        note: 'Cherry: powder canvas, oxblood ink, cherry action; powder-pink wash support; chocolate statement.',
        background: o(0.97, 0.012, 20), foreground: o(0.26, 0.080, 20),
        primary: o(0.48, 0.20, 22), secondary: o(0.88, 0.05, 10), accent: o(0.36, 0.06, 50),
    },
    LAVENDER: {
        note: 'Digital lavender: lilac canvas, violet action; lavender wash support; pistachio statement.',
        background: o(0.96, 0.020, 300), foreground: o(0.25, 0.070, 300),
        primary: o(0.48, 0.17, 305), secondary: o(0.85, 0.07, 300), accent: o(0.86, 0.10, 130),
    },
    AURORA_LIGHTS: {
        note: 'Holographic: cool-pearl canvas, hot-magenta action; iridescent-mint support; electric-lilac statement.',
        background: o(0.97, 0.010, 250), foreground: o(0.22, 0.050, 280),
        primary: o(0.48, 0.22, 345), secondary: o(0.88, 0.08, 175), accent: o(0.78, 0.12, 300),
    },
    CYBERPUNK: {
        note: 'Cyberpunk: violet-black canvas, hot-pink CTA with dark text; electric-cyan support; acid-yellow statement.',
        background: o(0.15, 0.020, 290), foreground: o(0.93, 0.008, 300),
        primary: o(0.72, 0.22, 350), secondary: o(0.84, 0.13, 200), accent: o(0.90, 0.18, 100),
    },
    AUTUMN_HARVEST: {
        note: 'Mocha: oat canvas, espresso ink, mocha-mousse action; caramel support; burnt-orange statement.',
        background: o(0.95, 0.020, 75), foreground: o(0.25, 0.040, 50),
        primary: o(0.46, 0.06, 50), secondary: o(0.78, 0.10, 70), accent: o(0.62, 0.17, 48),
    },
    CYBER_NEON: {
        note: 'Neon on black: neon-lime CTA with dark text; graphite support; neon-magenta statement.',
        background: o(0.12, 0.000, 0), foreground: o(0.96, 0.000, 0),
        primary: o(0.90, 0.24, 130), secondary: o(0.30, 0.010, 290), accent: o(0.70, 0.24, 340),
    },
    SUNSET: {
        note: 'Tomato: apricot-cream canvas, brick ink, tomato CTA with dark text; blush support; plum statement.',
        background: o(0.965, 0.020, 55), foreground: o(0.26, 0.070, 30),
        primary: o(0.62, 0.20, 35), secondary: o(0.86, 0.07, 30), accent: o(0.40, 0.12, 340),
    },
    FOREST: {
        note: 'Bottle green: paper canvas, bottle-green ink and action; moss wash support; cherry statement.',
        background: o(0.96, 0.010, 110), foreground: o(0.22, 0.040, 160),
        primary: o(0.35, 0.09, 160), secondary: o(0.85, 0.05, 120), accent: o(0.48, 0.20, 25),
    },
    DESERT: {
        note: 'Saffron: sand canvas, saffron CTA with dark text; camel support; cactus statement.',
        background: o(0.955, 0.020, 85), foreground: o(0.30, 0.050, 55),
        primary: o(0.76, 0.16, 70), secondary: o(0.46, 0.09, 60), accent: o(0.42, 0.09, 150),
    },
    SKY: {
        note: 'Powder blue: pale canvas, cobalt-sky action; powder-blue wash support; butter-yellow statement.',
        background: o(0.97, 0.012, 230), foreground: o(0.24, 0.050, 250),
        primary: o(0.48, 0.17, 250), secondary: o(0.88, 0.06, 230), accent: o(0.92, 0.12, 95),
    },
    EARTH: {
        note: 'Chocolate: ecru canvas, chocolate ink and action; greige wash support; olive statement.',
        background: o(0.95, 0.012, 70), foreground: o(0.22, 0.030, 50),
        primary: o(0.32, 0.06, 50), secondary: o(0.86, 0.015, 70), accent: o(0.48, 0.10, 110),
    },
    FIRE: {
        note: 'Scarlet: warm-white canvas, near-black ink, scarlet action; charcoal support; chartreuse statement.',
        background: o(0.97, 0.010, 30), foreground: o(0.22, 0.030, 30),
        primary: o(0.48, 0.21, 28), secondary: o(0.30, 0.010, 30), accent: o(0.88, 0.20, 115),
    },
    ICE: {
        note: 'Glacier: frost canvas, steel-blue action; glacier-tint support; silver-lilac statement.',
        background: o(0.975, 0.008, 210), foreground: o(0.25, 0.040, 230),
        primary: o(0.42, 0.08, 230), secondary: o(0.90, 0.03, 205), accent: o(0.76, 0.06, 290),
    },
    BLOSSOM: {
        note: 'Ballet: petal canvas, ballet-pink CTA with dark text; petal wash support; deep-green statement.',
        background: o(0.97, 0.014, 350), foreground: o(0.25, 0.060, 350),
        primary: o(0.64, 0.14, 355), secondary: o(0.90, 0.04, 350), accent: o(0.38, 0.09, 155),
    },
    GOLDEN: {
        note: 'Butter: cream canvas, butter-yellow CTA with dark text; bronze support; cobalt statement.',
        background: o(0.975, 0.018, 95), foreground: o(0.28, 0.050, 70),
        primary: o(0.90, 0.14, 95), secondary: o(0.42, 0.08, 65), accent: o(0.45, 0.20, 262),
    },
    GRAPE: {
        note: 'Aubergine: lilac-white canvas, aubergine ink and action; mauve wash support; lime statement.',
        background: o(0.965, 0.012, 320), foreground: o(0.22, 0.070, 320),
        primary: o(0.34, 0.12, 320), secondary: o(0.86, 0.05, 320), accent: o(0.86, 0.18, 125),
    },
    PEACH: {
        note: 'Peach fuzz: peach canvas, peach CTA with dark text; coral support; sage statement.',
        background: o(0.97, 0.016, 55), foreground: o(0.27, 0.050, 40),
        primary: o(0.78, 0.12, 45), secondary: o(0.48, 0.16, 28), accent: o(0.74, 0.06, 140),
    },
    MINT: {
        note: 'Matcha: mint-white canvas, matcha action; mint wash support; chocolate statement.',
        background: o(0.96, 0.015, 150), foreground: o(0.25, 0.040, 160),
        primary: o(0.45, 0.10, 145), secondary: o(0.88, 0.07, 160), accent: o(0.36, 0.06, 50),
    },
    SAND: {
        note: 'Ecru: sand canvas, warm near-black ink, khaki action; ecru wash support; sea-blue statement.',
        background: o(0.95, 0.012, 85), foreground: o(0.24, 0.020, 70),
        primary: o(0.44, 0.06, 80), secondary: o(0.88, 0.02, 85), accent: o(0.45, 0.10, 220),
    },
    RAINBOW: {
        note: 'Colour-block: gallery white, cobalt action; cherry support; butter-yellow statement.',
        background: o(0.98, 0.004, 90), foreground: o(0.20, 0.010, 60),
        primary: o(0.45, 0.22, 262), secondary: o(0.48, 0.20, 25), accent: o(0.92, 0.13, 95),
    },
    NEON: {
        note: 'Neon in daylight: cool-white canvas, electric-violet action; near-black support; neon-lime statement.',
        background: o(0.98, 0.005, 200), foreground: o(0.18, 0.030, 280),
        primary: o(0.45, 0.25, 300), secondary: o(0.22, 0.020, 280), accent: o(0.90, 0.22, 130),
    },
    PASTEL: {
        note: 'Sorbet: pink-white canvas, sorbet-pink CTA with dark text; lilac wash support; lemon statement.',
        background: o(0.975, 0.010, 340), foreground: o(0.28, 0.050, 340),
        primary: o(0.80, 0.09, 10), secondary: o(0.86, 0.06, 300), accent: o(0.92, 0.10, 95),
    },
};

/**
 * Per-theme default palettes — what a theme looks like when the merchant's colour theme is `DEFAULT`
 * (or unset). Each is the palette of the theme's own visual world (see `themes/<id>/DESIGN.md` and the
 * decision record in `.impeccable/`), not a generic light preset. Same seed shape and rules as a preset.
 * Keyed by theme id (`themes/<id>`); `scripts/new-theme.mjs` appends a starter copy for new themes.
 */
const THEME_DEFAULTS = {
    // @theme-defaults:start
    starter: {
        note: 'Neutral reference (the LIGHT preset): warm white, soft-black ink and a black CTA; bone wash support; cherry-red statement.',
        background: o(0.99, 0.004, 90), foreground: o(0.20, 0.010, 60),
        primary: o(0.22, 0.010, 60), secondary: o(0.88, 0.012, 80), accent: o(0.48, 0.21, 25),
    },
    basic: {
        note: 'The Catalogue Page: white stock, black ink, cobalt title fields and tabs; catalogue-grey wash; print-red price flash.',
        background: o(1.0, 0, 0), foreground: o(0.21, 0, 0),
        primary: o(0.50, 0.16, 257), secondary: o(0.90, 0, 0), accent: o(0.50, 0.19, 27),
    },
    beauty: {
        note: 'Industrial Quote Grammar: white stockroom cotton, ink plates, the safety-orange zip-tie tag with dark text; hazard-yellow statement.',
        background: o(1.0, 0, 0), foreground: o(0.17, 0, 0),
        primary: o(0.70, 0.20, 45), secondary: o(0.93, 0.004, 90), accent: o(0.85, 0.17, 90),
    },
    fashion: {
        note: 'The Wheatpaste Wall: poster paper, ink, day-glo pink paper with dark text on every primary action; acid-green statement.',
        background: o(0.96, 0.012, 90), foreground: o(0.18, 0, 0),
        primary: o(0.68, 0.24, 350), secondary: o(0.18, 0, 0), accent: o(0.90, 0.20, 115),
    },
    // @theme-defaults:end
};

/* ------------------------------------------------------------------------------------------------ */
/* Colour maths (OKLCH → sRGB, WCAG contrast)                                                        */
/* ------------------------------------------------------------------------------------------------ */

function oklchToLinearSrgb({l, c, h}) {
    const hr = (h * Math.PI) / 180;
    const a = c * Math.cos(hr);
    const b = c * Math.sin(hr);
    const l_ = l + 0.3963377774 * a + 0.2158037573 * b;
    const m_ = l - 0.1055613458 * a - 0.0638541728 * b;
    const s_ = l - 0.0894841775 * a - 1.2914855480 * b;
    const L = l_ ** 3, M = m_ ** 3, S = s_ ** 3;
    return {
        r: +4.0767416621 * L - 3.3077115913 * M + 0.2309699292 * S,
        g: -1.2684380046 * L + 2.6097574011 * M - 0.3413193965 * S,
        b: -0.0041960863 * L - 0.7034186147 * M + 1.7076147010 * S,
    };
}

const inGamut = ({r, g, b}) => [r, g, b].every(v => v >= -1e-4 && v <= 1 + 1e-4);
const gamma = v => (v <= 0.0031308 ? 12.92 * v : 1.055 * Math.pow(v, 1 / 2.4) - 0.055);

/** OKLCH → `#rrggbb`, reducing chroma (never lightness) until the colour fits sRGB. */
function toHex(color) {
    let {l, c, h} = color;
    let lin = oklchToLinearSrgb({l, c, h});
    if (!inGamut(lin)) {
        let lo = 0, hi = c;
        for (let i = 0; i < 24; i++) {
            const mid = (lo + hi) / 2;
            if (inGamut(oklchToLinearSrgb({l, c: mid, h}))) lo = mid; else hi = mid;
        }
        lin = oklchToLinearSrgb({l, c: lo, h});
    }
    const ch = v => Math.max(0, Math.min(255, Math.round(gamma(Math.max(0, Math.min(1, v))) * 255)))
        .toString(16).padStart(2, '0');
    return `#${ch(lin.r)}${ch(lin.g)}${ch(lin.b)}`.toUpperCase();
}

function luminance(hex) {
    const n = parseInt(hex.slice(1), 16);
    const ch = v => {
        const s = v / 255;
        return s <= 0.03928 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4);
    };
    return 0.2126 * ch(n >> 16) + 0.7152 * ch((n >> 8) & 255) + 0.0722 * ch(n & 255);
}

function contrast(a, b) {
    const la = luminance(a), lb = luminance(b);
    return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
}

/** Same choice the bridge makes (`pickForeground`): white or near-black, whichever reads better. */
const textOn = hex => (contrast(hex, '#FFFFFF') >= contrast(hex, '#111111') ? '#FFFFFF' : '#111111');

/* ------------------------------------------------------------------------------------------------ */
/* Derivation rules                                                                                  */
/* ------------------------------------------------------------------------------------------------ */

const HOVER_STEP = 0.07;
const PRESSED_STEP = 0.13;

/**
 * Hover darkens on light canvases and lightens on dark ones — unless that would drop the role's own text
 * below AA, in which case the step flips direction (a bright-with-dark-text role always darkens, etc.).
 */
function interactiveSteps(base, scheme) {
    const baseHex = toHex(base);
    const text = textOn(baseHex);
    const sign = scheme === 'light' ? -1 : 1;
    const step = (dir, amount) => ({...base, l: Math.max(0.05, Math.min(0.98, base.l + dir * amount))});
    let hover = step(sign, HOVER_STEP);
    let pressed = step(sign, PRESSED_STEP);
    if (contrast(toHex(hover), text) < 4.5 || contrast(toHex(pressed), text) < 4.5) {
        hover = step(-sign, HOVER_STEP);
        pressed = step(-sign, PRESSED_STEP);
    }
    return {hover: toHex(hover), pressed: toHex(pressed)};
}

/** Lightness-walk a brand colour until it reaches `min` contrast with the canvas (for ring/outline). */
function ensureAgainstCanvas(color, canvasHex, min, scheme) {
    let {l, c, h} = color;
    const dir = scheme === 'light' ? -1 : 1;
    for (let i = 0; i < 30 && contrast(toHex({l, c, h}), canvasHex) < min; i++) l += dir * 0.02;
    return toHex({l, c, h});
}

function buildPreset(name, seed) {
    const scheme = seed.background.l < 0.5 ? 'dark' : 'light';
    const semantic = SEMANTIC[scheme];
    const bg = seed.background, fg = seed.foreground;
    const background = toHex(bg);
    const foreground = toHex(fg);

    const roles = {
        primary: seed.primary, secondary: seed.secondary, accent: seed.accent,
        error: semantic.error, warning: semantic.warning, success: semantic.success, info: semantic.info,
        // neutral: a mid-tone in the ink's hue for disabled surfaces and placeholders
        neutral: scheme === 'light' ? o(0.74, Math.min(0.02, fg.c), fg.h) : o(0.50, Math.min(0.02, fg.c), fg.h),
    };

    const out = {
        primary: toHex(roles.primary),
        secondary: toHex(roles.secondary),
        accent: toHex(roles.accent),
        background,
        foreground,
        error: toHex(roles.error),
        warning: toHex(roles.warning),
        success: toHex(roles.success),
        info: toHex(roles.info),
        neutral: toHex(roles.neutral),
        // hairline one step off the canvas, in the canvas hue
        border: toHex(scheme === 'light'
            ? o(bg.l - 0.10, bg.c === 0 ? 0 : Math.min(0.03, bg.c + 0.012), bg.h)
            : o(bg.l + 0.13, bg.c === 0 ? 0 : Math.min(0.03, bg.c + 0.010), bg.h)),
        // focus ring: the brand hue, guaranteed ≥ 3:1 on the canvas
        ring: ensureAgainstCanvas(roles.primary, background, 3, scheme),
        // outlined-control border: the brand hue at reduced chroma, also ≥ 3:1 on the canvas
        outline: ensureAgainstCanvas({...roles.primary, c: roles.primary.c * 0.6}, background, 3, scheme),
    };

    for (const role of ['primary', 'secondary', 'accent', 'error', 'warning', 'success', 'info', 'neutral']) {
        const {hover, pressed} = interactiveSteps(roles[role], scheme);
        const Cap = role[0].toUpperCase() + role.slice(1);
        out[`hover${Cap}`] = hover;
        out[`focus${Cap}`] = pressed;
    }
    return {scheme, note: seed.note, colors: out};
}

/* ------------------------------------------------------------------------------------------------ */
/* Rules                                                                                             */
/* ------------------------------------------------------------------------------------------------ */

const HUE_FAMILIES = {error: [10, 40], warning: [60, 95], success: [135, 165], info: [225, 260]};

function verify(name, preset) {
    const c = preset.colors;
    const problems = [];
    const need = (ok, msg) => { if (!ok) problems.push(`${name}: ${msg}`); };

    need(contrast(c.foreground, c.background) >= 7, `foreground on background ${contrast(c.foreground, c.background).toFixed(2)} < 7`);
    for (const role of ['primary', 'secondary', 'accent', 'error', 'warning', 'success', 'info']) {
        const text = textOn(c[role]);
        need(contrast(c[role], text) >= 4.5, `${role} ${c[role]} reads ${contrast(c[role], text).toFixed(2)} with ${text}`);
        const Cap = role[0].toUpperCase() + role.slice(1);
        for (const step of [`hover${Cap}`, `focus${Cap}`]) {
            need(c[step] !== c[role], `${step} equals ${role}`);
            need(contrast(c[step], text) >= 4.5, `${step} ${c[step]} reads ${contrast(c[step], text).toFixed(2)} with ${role}'s text ${text}`);
        }
    }
    need(contrast(c.ring, c.background) >= 3, `ring on background ${contrast(c.ring, c.background).toFixed(2)} < 3`);
    need(contrast(c.outline, c.background) >= 3, `outline on background ${contrast(c.outline, c.background).toFixed(2)} < 3`);
    need(contrast(c.border, c.background) >= 1.2, `border on background ${contrast(c.border, c.background).toFixed(2)} < 1.2`);
    const scheme = preset.scheme;
    for (const [role, [lo, hi]] of Object.entries(HUE_FAMILIES)) {
        const h = SEMANTIC[scheme][role].h;
        need(h >= lo && h <= hi, `${role} hue ${h} outside ${lo}–${hi}`);
    }
    for (const v of Object.values(c)) need(/^#[0-9A-F]{6}$/.test(v), `unparsable ${v}`);
    return problems;
}

/* ------------------------------------------------------------------------------------------------ */
/* Emit                                                                                              */
/* ------------------------------------------------------------------------------------------------ */

const KEYS = [
    'primary', 'secondary', 'accent', 'background', 'foreground', 'error', 'warning', 'success', 'info',
    'neutral', 'border', 'ring', 'outline',
    'hoverPrimary', 'focusPrimary', 'hoverSecondary', 'focusSecondary', 'hoverAccent', 'focusAccent',
    'hoverError', 'focusError', 'hoverWarning', 'focusWarning', 'hoverSuccess', 'focusSuccess',
    'hoverInfo', 'focusInfo', 'hoverNeutral', 'focusNeutral',
];

function emit(presets) {
    const names = Object.keys(presets);
    const lines = [];
    lines.push('// GENERATED by scripts/build-color-schemas.mjs — edit the seeds there, then `npm run gen:colors`.');
    lines.push('// Rules the generator enforces: canvas/ink ≥ 7:1; every brand and semantic role reads ≥ 4.5:1 with');
    lines.push('// white or #111 (so the contrast bridge never repaints a merchant colour); hover/pressed steps keep');
    lines.push('// that text readable; ring and outline ≥ 3:1 on the canvas; semantic hues stay in their families.');
    lines.push('');
    lines.push('/**');
    lines.push(' * A merchant colour preset. Roles, not swatches:');
    lines.push(' *  - `background` / `foreground`   canvas and ink (≥ 7:1)');
    lines.push(' *  - `primary`                     the action colour — CTAs, active states, focus');
    lines.push(' *  - `secondary`                   supporting hue — secondary actions, links, chips');
    lines.push(' *  - `accent`                      highlight — badges, sale tags, feature marks');
    lines.push(' *  - `error` `warning` `success` `info`  status, always red / amber / green / blue');
    lines.push(' *  - `neutral`                     mid-tone for disabled surfaces and placeholders');
    lines.push(' *  - `border`                      hairline one step off the canvas');
    lines.push(' *  - `ring`                        focus indicator, brand hue, ≥ 3:1 on the canvas');
    lines.push(' *  - `outline`                     outlined-control border, quieter brand hue, ≥ 3:1 on the canvas');
    lines.push(' *  - `hover*` / `focus*`           hover and pressed steps of each role (one and two lightness');
    lines.push(' *                                  steps away from the canvas, flipped when that would hurt legibility)');
    lines.push(' * Text on any role: white or #111 by contrast — see `libs/theme` `pickForeground`.');
    lines.push(' */');
    lines.push('export interface ColorSchema {');
    for (const k of KEYS) lines.push(`    ${k}: string;`);
    lines.push('}');
    lines.push('');
    for (const name of names) {
        const p = presets[name];
        lines.push(`/** ${p.note} (${p.scheme}) */`);
        lines.push(`const ${name}_COLOR_SCHEMA: ColorSchema = {`);
        for (const k of KEYS) lines.push(`    ${k}: "${p.colors[k]}",`);
        lines.push('};');
    }
    lines.push('');
    lines.push('/**');
    lines.push(' * Colour theme ids — mirrors the backend `ColorTheme` enum; the merchant picks one in the seller console.');
    lines.push(' * `DEFAULT` is not a preset: it means "the storefront theme\'s own palette" (`ThemeDefinition.tokens.defaultColors`).');
    lines.push(' */');
    lines.push('export enum ColorTheme {');
    lines.push('    DEFAULT = "DEFAULT",');
    for (const name of names) lines.push(`    ${name} = "${name}",`);
    lines.push('}');
    lines.push('');
    lines.push('/** The fixed presets — every `ColorTheme` except `DEFAULT`. */');
    lines.push('export type ColorPreset = Exclude<ColorTheme, ColorTheme.DEFAULT>;');
    lines.push('');
    lines.push('const colorThemeData: Record<ColorPreset, ColorSchema> = {');
    for (const name of names) lines.push(`    [ColorTheme.${name}]: ${name}_COLOR_SCHEMA,`);
    lines.push('};');
    lines.push('');
    lines.push('/** Whether `value` is exactly a `ColorTheme` member name (`DEFAULT` included); normalise case first. */');
    lines.push('export function isColorTheme(value: string | null | undefined): value is ColorTheme {');
    lines.push('    return !!value && Object.hasOwn(ColorTheme, value);');
    lines.push('}');
    lines.push('');
    lines.push('/**');
    lines.push(' * Retrieves the ColorSchema of a fixed preset.');
    lines.push(' * @param theme The ColorTheme member or its name (any case).');
    lines.push(' * @returns The preset, or `undefined` for `DEFAULT` and unknown values — the caller falls back to the');
    lines.push(' *          storefront theme\'s `tokens.defaultColors`.');
    lines.push(' */');
    lines.push('export function getThemeColors(theme: ColorTheme | string | null | undefined): ColorSchema | undefined {');
    lines.push('    const name = (theme ?? \'\').trim().toUpperCase();');
    lines.push('    return name === ColorTheme.DEFAULT ? undefined : colorThemeData[name as ColorPreset];');
    lines.push('}');
    lines.push('');
    return lines.join('\n');
}

function emitThemeDefault(id, p) {
    const lines = [];
    lines.push(`// GENERATED by libs/types/scripts/build-color-schemas.mjs (THEME_DEFAULTS.${id}) — edit the seed there, then`);
    lines.push('// `npm run gen:colors` in libs/types. Same rules as the merchant presets (see that file\'s header).');
    lines.push("import type {ColorSchema} from '@store-front/types';");
    lines.push('');
    lines.push('/**');
    lines.push(` * ${p.note} (${p.scheme})`);
    lines.push(' *');
    lines.push(" * The theme's own palette — what renders when the merchant's colour theme is `DEFAULT` or unset. Wired as");
    lines.push(' * `tokens.defaultColors` in `./index.ts`; any fixed preset the merchant picks replaces it whole.');
    lines.push(' */');
    lines.push('export const DEFAULT_COLORS: ColorSchema = {');
    for (const k of KEYS) lines.push(`    ${k}: "${p.colors[k]}",`);
    lines.push('};');
    lines.push('');
    return lines.join('\n');
}

/* ------------------------------------------------------------------------------------------------ */
/* Main                                                                                              */
/* ------------------------------------------------------------------------------------------------ */

const built = Object.fromEntries(Object.entries(PRESETS).map(([name, seed]) => [name, buildPreset(name, seed)]));
const builtDefaults = Object.fromEntries(Object.entries(THEME_DEFAULTS).map(([id, seed]) => [id, buildPreset(id, seed)]));
const problems = [
    ...Object.entries(built).flatMap(([name, p]) => verify(name, p)),
    ...Object.entries(builtDefaults).flatMap(([id, p]) => verify(`theme default ${id}`, p)),
];
if (problems.length) {
    console.error(`color presets: ${problems.length} rule violation(s)\n  ` + problems.join('\n  '));
    process.exit(1);
}

const here = dirname(fileURLToPath(import.meta.url));
const outputs = [
    {target: resolve(here, '../src/color-schema.ts'), source: emit(built)},
    ...Object.entries(builtDefaults).map(([id, p]) => ({target: resolve(here, `../../../themes/${id}/src/colors.ts`), source: emitThemeDefault(id, p)})),
];
const check = process.argv.includes('--check');
let stale = 0;
for (const {target, source} of outputs) {
    if (check) {
        let current = '';
        try { current = readFileSync(target, 'utf8'); } catch { /* missing counts as stale */ }
        if (current !== source) { console.error(`color presets: ${target} is out of date — run \`npm run gen:colors\` in libs/types`); stale++; }
    } else {
        writeFileSync(target, source);
    }
}
if (check) {
    if (stale) process.exit(1);
    console.log(`color presets: ${Object.keys(built).length} presets + ${Object.keys(builtDefaults).length} theme defaults verified, files up to date`);
} else {
    console.log(`color presets: wrote ${Object.keys(built).length} presets to src/color-schema.ts and ${Object.keys(builtDefaults).length} theme defaults to themes/<id>/src/colors.ts`);
}
