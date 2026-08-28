/**
 * Small, dependency-free colour maths for the merchant bridge. Works in sRGB on hex / rgb() strings —
 * enough to derive foregrounds and guarantee WCAG contrast for the 30 presets.
 */

export interface Rgb { r: number; g: number; b: number }

const HEX = /^#?([0-9a-f]{3}|[0-9a-f]{4}|[0-9a-f]{6}|[0-9a-f]{8})$/i;
const RGB = /^rgba?\(\s*(\d+(?:\.\d+)?)\s*[, ]\s*(\d+(?:\.\d+)?)\s*[, ]\s*(\d+(?:\.\d+)?)/i;

export function parseColor(input: string): Rgb | undefined {
    const value = input.trim();
    const hex = HEX.exec(value);
    if (hex) {
        let h = hex[1];
        if (h.length === 3 || h.length === 4) h = h.split('').map(c => c + c).join('');
        return {
            r: parseInt(h.slice(0, 2), 16),
            g: parseInt(h.slice(2, 4), 16),
            b: parseInt(h.slice(4, 6), 16),
        };
    }
    const rgb = RGB.exec(value);
    if (rgb) {
        return {r: Number(rgb[1]), g: Number(rgb[2]), b: Number(rgb[3])};
    }
    return undefined;
}

export function toHex({r, g, b}: Rgb): string {
    const c = (n: number) => Math.max(0, Math.min(255, Math.round(n))).toString(16).padStart(2, '0');
    return `#${c(r)}${c(g)}${c(b)}`;
}

function channel(v: number): number {
    const s = v / 255;
    return s <= 0.03928 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4);
}

/** WCAG relative luminance, 0 (black) … 1 (white). */
export function luminance(color: string | Rgb): number {
    const rgb = typeof color === 'string' ? parseColor(color) : color;
    if (!rgb) return 0.5;
    return 0.2126 * channel(rgb.r) + 0.7152 * channel(rgb.g) + 0.0722 * channel(rgb.b);
}

/** WCAG contrast ratio, 1 … 21. Unknown colours contrast as 1 so callers treat them as failing. */
export function contrastRatio(a: string, b: string): number {
    const la = luminance(a);
    const lb = luminance(b);
    const [hi, lo] = la > lb ? [la, lb] : [lb, la];
    return (hi + 0.05) / (lo + 0.05);
}

/** Linear mix in sRGB: `mix(a, b, 0.1)` is 90 % a, 10 % b. */
export function mix(a: string, b: string, amount: number): string {
    const ca = parseColor(a);
    const cb = parseColor(b);
    if (!ca || !cb) return a;
    const t = Math.max(0, Math.min(1, amount));
    return toHex({
        r: ca.r + (cb.r - ca.r) * t,
        g: ca.g + (cb.g - ca.g) * t,
        b: ca.b + (cb.b - ca.b) * t,
    });
}

export const lighten = (color: string, amount: number) => mix(color, '#ffffff', amount);
export const darken = (color: string, amount: number) => mix(color, '#000000', amount);

export const isDarkColor = (color: string, threshold = 0.3) => luminance(color) < threshold;

/** White or near-black, whichever contrasts more with `background`. */
export function pickForeground(background: string, light = '#ffffff', dark = '#111111'): string {
    return contrastRatio(background, light) >= contrastRatio(background, dark) ? light : dark;
}

/**
 * Returns a (possibly adjusted) `background` such that `foreground` reaches `minContrast` on it, by
 * nudging the background away from the foreground in ≤ `steps` increments. The foreground is kept —
 * it is the value the theme committed to — and the background gives way.
 */
export function ensureContrast(foreground: string, background: string, minContrast = 4.5, steps = 12): string {
    let current = background;
    if (contrastRatio(foreground, current) >= minContrast) return current;
    const fgIsLight = luminance(foreground) > luminance(current);
    for (let i = 1; i <= steps; i++) {
        current = fgIsLight ? darken(current, 0.08) : lighten(current, 0.08);
        if (contrastRatio(foreground, current) >= minContrast) return current;
    }
    return current;
}
