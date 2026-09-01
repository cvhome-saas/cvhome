#!/usr/bin/env node
/**
 * Every translation key written out in the source exists in both locales.
 *
 * This is the dangerous direction. An unused key is untidy; a *missing* one takes the page down,
 * because `app.config.ts` configures Transloco to **throw** on a missing key rather than fall back.
 * A throw during template evaluation aborts the change-detection pass, so the damage is not one
 * blank label — it is every binding after it in that pass. Store management's social-links section
 * shipped with one such key: the first row rendered, the next four came out with unbound ids and
 * empty labels, and the page's busy overlay never got the update that would have cleared it. It
 * looked like a hung request.
 *
 * **Keys are compared the way Transloco resolves them.** These files mix nested objects with
 * flattened keys — `auth` holds both a `signIn` string and a literal `"signIn.heading"` key — and a
 * checker that only walks the nesting reports two dozen live keys as missing.
 *
 * Only statically written keys can be checked. A key built at runtime is caught by `lint:i18n`
 * from the other side, and by the known-set discipline in `shared/i18n/status-label.ts`.
 */
import {readFileSync} from 'node:fs';
import {execSync} from 'node:child_process';

import {SOURCE_DIRS} from './sources.mjs';

/** Both spellings a key can have in these files: nested, and flattened at any depth. */
const resolvable = (tree) => {
  const keys = new Set();
  const walk = (node, prefix) => {
    for (const [key, value] of Object.entries(node)) {
      const path = prefix ? `${prefix}.${key}` : key;
      keys.add(path);
      if (value && typeof value === 'object') {
        walk(value, path);
      }
    }
  };
  walk(tree, '');
  return keys;
};

const locales = {
  en: resolvable(JSON.parse(readFileSync('src/locale/en.json', 'utf8'))),
  ar: resolvable(JSON.parse(readFileSync('src/locale/ar.json', 'utf8'))),
};

const files = execSync(
  `find ${SOURCE_DIRS.join(' ')} -type f \\( -name '*.ts' -o -name '*.html' \\) ! -name '*.spec.ts'`,
  {encoding: 'utf8'},
).trim().split('\n').filter(Boolean);

/** `t('x')`, `translate('x')`, `translateSignal('x')` — the three ways a key is written here. */
const CALL = /\b(?:t|translate|translateSignal)\(\s*'([A-Za-z][\w.]*)'/g;

const problems = new Map();

for (const file of files) {
  const source = readFileSync(file, 'utf8');
  source.split('\n').forEach((line, index) => {
    // Doc comments quote keys as examples; they are prose, not calls.
    const trimmed = line.trim();
    if (trimmed.startsWith('*') || trimmed.startsWith('//') || trimmed.startsWith('/*')) {
      return;
    }
    for (const match of line.matchAll(CALL)) {
      const key = match[1];
      // A trailing segment built at runtime — `t('legal.' + doc + '.title')` — is not static.
      if (!key.includes('.') || line.slice(match.index).includes("' +")) {
        continue;
      }
      const absent = Object.entries(locales)
        .filter(([, keys]) => !keys.has(key))
        .map(([locale]) => locale);
      if (absent.length > 0) {
        problems.set(`${key} [${absent.join(', ')}]`, `${file}:${index + 1}`);
      }
    }
  });
}

if (problems.size > 0) {
  console.error(`\n${problems.size} translation key(s) written in the source and missing from a locale:\n`);
  for (const [key, where] of problems) {
    console.error(`  ${key}\n    ${where}`);
  }
  console.error('\nTransloco throws on a missing key: this is a blank page, not a blank label.\n');
  process.exit(1);
}

console.log('i18n: every statically written key resolves in both locales.');
