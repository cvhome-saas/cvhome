import {test} from 'node:test';
import assert from 'node:assert/strict';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {analyzeTheme, themeDirs} from '../../scripts/theme-client-barrier.mjs';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..', '..');

// The guard behind the per-theme client barrier: a server file importing a 'use client' component directly
// puts that component — and with it the theme — back into the chunk group every storefront downloads. The
// build does not warn; this test does. Fix a failure with `node scripts/theme-client-barrier.mjs <id>`.
for (const dir of themeDirs(root)) {
    test(`theme ${path.basename(dir)} keeps its client components behind ./client`, () => {
        const {violations, allComponents} = analyzeTheme(dir);
        assert.deepEqual(violations, []);
        assert.ok(allComponents.length > 0, 'a theme has client components');
    });
}
