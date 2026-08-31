const STOREFRONT_SCRIPT_RANGES = [
    [0x0041, 0x005a],
    [0x0061, 0x007a],
    [0x00c0, 0x024f],
    [0x0400, 0x052f],
    [0x0600, 0x06ff],
    [0x0750, 0x077f],
    [0x0870, 0x089f],
    [0x08a0, 0x08ff],
    [0x1c80, 0x1c8f],
    [0x1e00, 0x1eff],
    [0x2c60, 0x2c7f],
    [0x2de0, 0x2dff],
    [0xa640, 0xa69f],
    [0xa720, 0xa7ff],
    [0xfb50, 0xfdff],
    [0xfe70, 0xfeff],
];

const CJK_SCRIPT_RANGES = [
    [0x1100, 0x11ff],
    [0x2e80, 0x30ff],
    [0x3130, 0x318f],
    [0x31c0, 0x31ef],
    [0x31f0, 0x31ff],
    [0x3400, 0x4dbf],
    [0x4e00, 0x9fff],
    [0xac00, 0xd7af],
    [0xf900, 0xfaff],
    [0xff00, 0xffef],
    [0x20000, 0x323af],
];

function parsePoint(value, wildcardValue) {
    return Number.parseInt(value.replaceAll('?', wildcardValue), 16);
}

/** `U+0600-06FF, U+1F??` → codepoint intervals; `null` if anything fails to parse. */
export function parseUnicodeRange(value) {
    const ranges = [];
    for (const part of value.split(',')) {
        const match = /^U\+([0-9A-Fa-f?]+)(?:-([0-9A-Fa-f]+))?$/.exec(part.trim());
        if (!match || (match[1].includes('?') && match[2])) return null;
        const low = parsePoint(match[1], '0');
        const high = match[2] ? Number.parseInt(match[2], 16) : parsePoint(match[1], 'F');
        if (!Number.isInteger(low) || !Number.isInteger(high) || low > high) return null;
        ranges.push([low, high]);
    }
    return ranges.length ? ranges : null;
}

function overlapSize([low, high], [allowedLow, allowedHigh]) {
    return Math.max(0, Math.min(high, allowedHigh) - Math.max(low, allowedLow) + 1);
}

function coveredCodepoints(ranges, scripts) {
    return ranges.reduce((count, range) => count + scripts.reduce(
        (rangeCount, script) => rangeCount + overlapSize(range, script),
        0,
    ), 0);
}

/**
 * Google splits Japanese fonts into hundreds of shards. Most contain one punctuation or ASCII glyph,
 * which made the old U+3000 floor keep them despite their remaining glyphs being CJK. Keep the real
 * Latin/Cyrillic/Arabic subsets, but drop a CJK-heavy shard with fewer than eight storefront-script letters.
 */
export function isUnsupportedCjkShard(block) {
    const declared = /unicode-range:\s*([^;}]+)/.exec(block);
    if (!declared) return false;
    const ranges = parseUnicodeRange(declared[1]);
    if (!ranges) return false;
    const cjkCodepoints = coveredCodepoints(ranges, CJK_SCRIPT_RANGES);
    const storefrontCodepoints = coveredCodepoints(ranges, STOREFRONT_SCRIPT_RANGES);
    return cjkCodepoints > storefrontCodepoints && storefrontCodepoints < 8;
}
