/**
 * Substitutes the build-time asset-prefix sentinel across the .next output.
 *
 * A runtime-only `assetPrefix` is not enough: Turbopack bakes absolute "/_next/static/…" paths
 * into .next/server/app/**\/*_client-reference-manifest.js / *.rsc and "/_next/" as the chunk
 * base inside .next/static/chunks/turbopack-*.js — so the build carries a sentinel URL and this
 * module rewrites it in place to the runtime value before the server starts.
 */
import fs from 'node:fs';
import path from 'node:path';
import {SENTINEL, TEXT_EXTENSIONS} from './constants.mjs';

const STATE_FILE = 'static-assets.state.json';

/** What the .next files currently contain: {prefix, buildId}, or null if the sentinel is untouched. */
export function readState(nextDir) {
    try {
        return JSON.parse(fs.readFileSync(path.join(nextDir, STATE_FILE), 'utf8'));
    } catch {
        return null;
    }
}

function isTextFile(filePath) {
    return TEXT_EXTENSIONS.has(path.extname(filePath).slice(1).toLowerCase());
}

function* walkFiles(root) {
    if (!fs.existsSync(root)) return;
    for (const entry of fs.readdirSync(root, {withFileTypes: true, recursive: true})) {
        if (entry.isFile()) yield path.join(entry.parentPath, entry.name);
    }
}

/**
 * Rewrites `state.prefix ?? SENTINEL` → `to` in every text file of the build output and records
 * the result in .next/static-assets.state.json. Idempotent. Returns the prefix actually applied —
 * '' when a non-empty prefix was requested but the files no longer carry a substitutable value.
 */
export function applyAssetPrefix(nextDir, to, buildId) {
    const state = readState(nextDir);
    const from = state?.prefix ?? SENTINEL;
    if (from === to) return to;
    if (from === '') {
        // Origin-relative "/_next/..." can't be re-prefixed by string replacement; a fresh
        // container (untouched sentinel) is needed to switch to a CDN URL.
        console.warn(
            '[static-assets] build output was already rewritten to origin-relative URLs; ' +
            'cannot apply a CDN prefix now — restart from a fresh container/image. Serving from origin.',
        );
        return '';
    }

    const roots = [
        path.join(nextDir, 'server'),
        path.join(nextDir, 'static'),
        ...fs.readdirSync(nextDir, {withFileTypes: true})
            .filter((e) => e.isFile() && e.name.endsWith('.json'))
            .map((e) => path.join(nextDir, e.name)),
    ];
    let rewritten = 0;
    for (const root of roots) {
        const files = fs.statSync(root).isDirectory() ? walkFiles(root) : [root];
        for (const file of files) {
            if (!isTextFile(file)) continue;
            const content = fs.readFileSync(file, 'utf8');
            if (!content.includes(from)) continue;
            fs.writeFileSync(file, content.replaceAll(from, to));
            rewritten++;
        }
    }
    fs.writeFileSync(path.join(nextDir, STATE_FILE), JSON.stringify({prefix: to, buildId}, null, 2));
    console.log(`[static-assets] asset prefix set to ${to === '' ? "'' (origin)" : to} — ${rewritten} files rewritten`);
    return to;
}
