import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';
import {fileURLToPath} from 'node:url';

const themeDir = path.join(path.dirname(fileURLToPath(import.meta.url)), '..');
const fontsSource = fs.readFileSync(path.join(themeDir, 'src/fonts.ts'), 'utf8');
const selfHostedCss = fs.readFileSync(path.join(themeDir, 'src/self-hosted-fonts.css'), 'utf8');
const tokensCss = fs.readFileSync(path.join(themeDir, 'src/tokens.css'), 'utf8');

test('CJK fonts are self-hosted with only storefront script subsets', () => {
    assert.doesNotMatch(fontsSource, /M_PLUS_Rounded_1c|Dela_Gothic_One/);
    assert.match(fontsSource, /import \{Cairo} from 'next\/font\/google'/);
    assert.match(tokensCss, /@import '\.\/self-hosted-fonts\.css'/);

    const faces = [...selfHostedCss.matchAll(/@font-face\s*\{[^}]+}/g)].map(match => match[0]);
    assert.equal(faces.filter(face => face.includes("font-family: 'Dela Gothic One'")).length, 2);
    assert.equal(faces.filter(face => face.includes("font-family: 'M PLUS Rounded 1c'")).length, 12);

    for (const weight of [400, 500, 700, 800]) {
        const weightedFaces = faces.filter(face => face.includes(`font-weight: ${weight}`));
        for (const subset of ['cyrillic', 'latin-ext', 'latin']) {
            assert.ok(weightedFaces.some(face => face.includes(`m-plus-rounded-1c-${subset}-${weight}-normal.woff2`)));
        }
    }

    assert.doesNotMatch(selfHostedCss, /japanese|m-plus-rounded-1c-\d+-/);
});
