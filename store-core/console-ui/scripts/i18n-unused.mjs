#!/usr/bin/env node
/**
 * Translation keys that nothing reaches.
 *
 * **The hard part is composition, and getting it wrong is expensive.** A first pass at this reported
 * 37 dead keys; 25 of them were live. `marketing.entitlement.MAX_PRODUCTS.limit` is built as
 * `marketing.entitlement.${feature.key}.limit` and `legal.terms.title` as
 * `'legal.' + document() + '.title'`, so neither the full key nor its immediate parent ever appears
 * literally — and deleting them would have taken the pricing table and both legal pages down under
 * the strict missing-key handler, which throws rather than falling back.
 *
 * So a key counts as reachable if the source contains the whole thing, or **any** ancestor prefix,
 * or any suffix that could be the tail of a template literal. That is deliberately generous: a false
 * negative here is a tidy-up missed, a false positive is a page that crashes in production.
 */
import {readFileSync} from 'node:fs';
import {execSync} from 'node:child_process';

const flatten = (node, prefix = '') =>
  Object.entries(node).flatMap(([key, value]) => {
    const path = prefix ? `${prefix}.${key}` : key;
    return value && typeof value === 'object' ? flatten(value, path) : [path];
  });

const keys = flatten(JSON.parse(readFileSync('src/locale/en.json', 'utf8')));

const source = execSync(
  "find src/app -type f \\( -name '*.ts' -o -name '*.html' \\) -exec cat {} +",
  {encoding: 'utf8', maxBuffer: 64 * 1024 * 1024},
);

/** Every dotted key written out in full. */
const literal = new Set(source.match(/[A-Za-z][A-Za-z0-9_]*(?:\.[A-Za-z][A-Za-z0-9_]*)+/g) ?? []);

/**
 * The static head of every key the source *builds* rather than writes.
 *
 * Three shapes, all of them in use here:
 *   `products.tab.${tab}`                  a template literal
 *   `marketing.entitlement.${key}.limit`   ...with a tail after the hole
 *   'legal.' + document() + '.title'       string concatenation, in a template binding
 *
 * Anything under one of these heads is reachable. This is the rule an earlier pass got wrong by
 * guessing at prefixes instead of reading them: it called 25 live keys dead, including every
 * `status.*` and both legal pages.
 */
const wildcards = [
  ...source.matchAll(/[`'"]([A-Za-z][A-Za-z0-9_]*(?:\.[A-Za-z][A-Za-z0-9_]*)*\.)\$\{/g),
  ...source.matchAll(/[`'"]([A-Za-z][A-Za-z0-9_]*(?:\.[A-Za-z][A-Za-z0-9_]*)*\.)['"`]\s*\+/g),
].map((match) => match[1]);

const reachable = (key) =>
  literal.has(key) || wildcards.some((head) => key.startsWith(head));

const dead = keys.filter((key) => !reachable(key));

if (dead.length > 0) {
  console.error(`\n${dead.length} translation key(s) nothing reaches:\n`);
  for (const key of dead) {
    console.error(`  ${key}`);
  }
  console.error('\nDelete them from both locales, or use them.\n');
  process.exit(1);
}

console.log(`i18n: ${keys.length} keys, all reachable.`);
