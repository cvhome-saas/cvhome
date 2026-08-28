#!/usr/bin/env node
/**
 * Post-build: drop the CJK halves of the Google fonts next/font refuses to subset.
 *
 * `next/font/google`'s `subsets` option only decides which files get a <link rel="preload"> — the CSS
 * it emits is Google's response verbatim (see next/dist/compiled/@next/font/dist/google/loader.js).
 * For fonts with Japanese/Chinese/Korean coverage Google splits the family into ~120 numbered
 * unicode-range slices *per weight*, so asking for `subsets: ['latin', 'latin-ext', 'cyrillic']` still
 * ships every kanji slice. The pink theme's two Japanese faces alone produced 629 @font-face rules —
 * 472 KB of CSS, 161 KB gzipped — in the layout entry that every storefront loads, whatever theme is
 * active, plus ~3.7 MB of woff2 that no en/fr/ar/es/ru page can ever render.
 *
 * The rule here is deliberately conservative: a @font-face is removed only when EVERY range in its
 * `unicode-range` starts at or above U+3000 — i.e. the slice is purely CJK, kana, hangul, CJK-compat
 * or emoji. Anything reaching below U+3000 stays, so Latin, Greek, Cyrillic, Hebrew, Vietnamese and
 * Arabic (whose subset spans U+0600 as well as the presentation forms at U+FB50) are untouched, and a
 * face with no `unicode-range` at all (next/font's metric fallbacks) is always kept.
 *
 * Then any woff2 under static/media that no stylesheet references any more is deleted — the file set
 * shrinks for both the Docker image and the S3/CDN sync.
 *
 * Idempotent: a second run finds nothing to remove. Runs on `npm run build`, before the Dockerfile
 * copies .next/static and before start.mjs uploads it.
 */
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

// U+3000 IDEOGRAPHIC SPACE: the first codepoint of the CJK block run. Nothing an en/fr/ar/es/ru
// storefront renders lives at or above it except the Arabic presentation forms, and those slices
// also cover U+0600, so they never trip the "every range" test below.
const CJK_FLOOR = 0x3000;

const storefrontDir = path.join(path.dirname(fileURLToPath(import.meta.url)), '..');
const distDir = path.join(storefrontDir, process.env.NEXT_DIST_DIR || '.next');
const staticDir = path.join(distDir, 'static');
const mediaDir = path.join(staticDir, 'media');

/** `U+0600-06FF, U+FB50-FDFF` → `[[0x600, 0x6ff], [0xfb50, 0xfdff]]`; `null` if anything fails to parse. */
function parseUnicodeRange(value) {
    const ranges = [];
    for (const part of value.split(',')) {
        const m = /^U\+([0-9A-Fa-f]+)(?:-([0-9A-Fa-f]+))?$/.exec(part.trim());
        if (!m) return null;
        const low = parseInt(m[1], 16);
        ranges.push([low, m[2] ? parseInt(m[2], 16) : low]);
    }
    return ranges.length ? ranges : null;
}

function isCjkOnly(block) {
    const declared = /unicode-range:\s*([^;}]+)/.exec(block);
    if (!declared) return false;
    const ranges = parseUnicodeRange(declared[1]);
    // Unparseable range: keep the face. Being wrong here means a missing glyph, so never guess.
    return ranges !== null && ranges.every(([low]) => low >= CJK_FLOOR);
}

function walkFiles(root) {
    if (!fs.existsSync(root)) return [];
    return fs.readdirSync(root, {withFileTypes: true, recursive: true})
        .filter(entry => entry.isFile())
        .map(entry => path.join(entry.parentPath, entry.name));
}

if (!fs.existsSync(staticDir)) {
    console.error(`[fonts] ${path.relative(storefrontDir, staticDir)} not found — run \`next build\` first.`);
    process.exit(1);
}

let removedFaces = 0;
let cssBefore = 0;
let cssAfter = 0;

for (const file of walkFiles(staticDir)) {
    if (!file.endsWith('.css')) continue;
    const css = fs.readFileSync(file, 'utf8');
    if (!css.includes('@font-face')) continue;
    let removedHere = 0;
    const pruned = css.replace(/@font-face\s*\{[^}]*\}/g, block => {
        if (!isCjkOnly(block)) return block;
        removedHere++;
        return '';
    });
    if (!removedHere) continue;
    removedFaces += removedHere;
    cssBefore += css.length;
    cssAfter += pruned.length;
    fs.writeFileSync(file, pruned);
}

// Sweep orphaned font files. The reference set is built from every text asset the build emitted —
// stylesheets, JS chunks, manifests, prerendered HTML — so a file referenced from anywhere survives.
let removedFiles = 0;
let removedBytes = 0;
if (removedFaces && fs.existsSync(mediaDir)) {
    const referenced = new Set();
    for (const file of [...walkFiles(staticDir), ...walkFiles(path.join(distDir, 'server'))]) {
        if (file.startsWith(mediaDir)) continue;
        if (!/\.(css|js|mjs|cjs|json|rsc|html|txt|body|meta|map)$/.test(file)) continue;
        for (const m of fs.readFileSync(file, 'utf8').matchAll(/media\/([\w.-]+\.(?:woff2?|ttf|otf|eot))/g)) {
            referenced.add(m[1]);
        }
    }
    for (const name of fs.readdirSync(mediaDir)) {
        if (referenced.has(name)) continue;
        const full = path.join(mediaDir, name);
        removedBytes += fs.statSync(full).size;
        fs.unlinkSync(full);
        removedFiles++;
    }
}

const kb = bytes => `${(bytes / 1024).toFixed(0)} KB`;
if (removedFaces) {
    console.log(
        `[fonts] pruned ${removedFaces} CJK-only @font-face rules — stylesheets ${kb(cssBefore)} → ${kb(cssAfter)}; ` +
        `deleted ${removedFiles} unreferenced font files (${kb(removedBytes)})`,
    );
} else {
    console.log('[fonts] no CJK-only @font-face rules found — nothing to prune');
}
