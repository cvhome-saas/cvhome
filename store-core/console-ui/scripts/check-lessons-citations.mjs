#!/usr/bin/env node
/**
 * Every `lessons.md, "..."` citation in the source names a heading that exists.
 *
 * Two had rotted before this ran: `features/billing/billing.html` quoted a heading that had never
 * been written, and `core/reference/reference-data.service.ts` quoted one that had been renamed.
 * Both were silent — nothing in lint or the specs looks at prose.
 *
 * **The citations wrap.** Thirty-one of them are inside JSDoc blocks and break across lines with a
 * leading ` * `, so a naive single-line regex finds barely half and reports the rest as passing.
 * The source is flattened before matching, which is the whole trick.
 */
import {readFileSync} from 'node:fs';
import {execSync} from 'node:child_process';

import {SOURCE_ROOTS} from './sources.mjs';

const LESSONS = 'lessons.md';
const SOURCE_GLOB = SOURCE_ROOTS.join(' ');

const headings = new Set(
  readFileSync(LESSONS, 'utf8')
    .split('\n')
    .filter((line) => line.startsWith('## '))
    .map((line) => normalise(line.slice(3))),
);

/** Collapses comment leaders and runs of whitespace, so a wrapped quote matches a one-line heading. */
function normalise(text) {
  return text
    .replace(/\n\s*\*?\s*/g, ' ')
    .replace(/\s+/g, ' ')
    .replace(/[`"]/g, '')
    .trim()
    .toLowerCase();
}

const files = execSync(
  `find ${SOURCE_GLOB} -type f \\( -name '*.ts' -o -name '*.html' -o -name '*.css' \\)`,
  {encoding: 'utf8'},
).trim().split('\n').filter(Boolean);

const problems = [];
let checked = 0;

for (const file of files) {
  const raw = readFileSync(file, 'utf8');
  // Flatten comment leaders first, then find every quoted citation in the flattened text.
  const flat = raw.replace(/\n\s*\*\s?/g, ' ').replace(/\n\s*\/\/\s?/g, ' ');
  for (const match of flat.matchAll(/lessons\.md[,:]?\s*["“]([^"”]{8,200})["”]/g)) {
    checked += 1;
    const quoted = normalise(match[1]);
    if (!headings.has(quoted)) {
      problems.push({file, quoted: match[1].replace(/\s+/g, ' ').trim()});
    }
  }
}

if (problems.length > 0) {
  console.error(`\n${problems.length} lessons.md citation(s) name no heading:\n`);
  for (const {file, quoted} of problems) {
    console.error(`  ${file}\n    "${quoted}"`);
  }
  console.error(`\n${headings.size} headings in ${LESSONS}. Fix the quote, or write the entry.\n`);
  process.exit(1);
}

console.log(`lessons.md: ${checked} citation(s) checked against ${headings.size} headings — all resolve.`);
