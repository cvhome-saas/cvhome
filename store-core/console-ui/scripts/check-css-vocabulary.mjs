#!/usr/bin/env node
/**
 * Every shared layout class a template writes is defined by one of that component's own styleUrls.
 *
 * Angular scopes a stylesheet to the component that declares it. `.field-grid`, `.field-wide` and
 * `.split` live in `@shared/styles/field.css`, which is *not* global — a component gets them only
 * by listing that file in its own `styleUrls`. Write the class without the import and nothing
 * happens: no error, no warning, no failing spec. The class silently means nothing.
 *
 * That is not hypothetical. All three product-form steps used `.field-grid` and none imported
 * `field.css`, so the entire product form laid its fields out as a plain block with no gap and no
 * columns — every label sat flush against the field above it. It had been that way since the
 * refactor that moved those classes out of `editor-card.css`, which updated the catalogue's four
 * consumers and missed the product form's three. `editor-card.css` even documents where they went.
 *
 * The same shape hides a second bug: `.field-label` and `.required` are declared inside
 * `app-form-field`'s encapsulated stylesheet. Four features wrote `class="field-label"` in their
 * own markup, where that rule cannot reach, and got the body's 16px/400 beside their siblings'
 * 13px/600. `CLAUDE.md` had also promised `.field-hint` came from `field.css` for as long as the
 * file existed; it did not, so three more features styled nothing.
 *
 * A class is "defined" here if any stylesheet the component actually loads declares it. That is
 * deliberately generous — a feature is free to define `.field-label` itself and mean something
 * different by it, as `media-tab.css` does for its uppercase metadata captions. What this rejects
 * is the case with no definition anywhere in reach.
 */
import {readFileSync, existsSync} from 'node:fs';
import {execSync} from 'node:child_process';
import {dirname, resolve, relative} from 'node:path';

/** Shared vocabulary that is opt-in per component, and the file each is meant to come from. */
const VOCABULARY = {
  'field-grid': '@shared/styles/field.css',
  'field-wide': '@shared/styles/field.css',
  'field-hint': '@shared/styles/field.css',
  'group-label': '@shared/styles/field.css',
  'split': '@shared/styles/field.css',
  'cross-field-error': '@shared/styles/field.css',
  'field-label': 'app-form-field (use the component, or define your own)',
  'required': 'app-form-field (use the component, or define your own)',
};

const files = execSync("find src/app -name '*.ts' -not -name '*.spec.ts'", {encoding: 'utf8'})
  .trim().split('\n').filter(Boolean);

const problems = [];
let checked = 0;

for (const ts of files) {
  const src = readFileSync(ts, 'utf8');

  let html = '';
  const templateUrl = src.match(/templateUrl:\s*'([^']+)'/);
  if (templateUrl) {
    const p = resolve(dirname(ts), templateUrl[1]);
    if (existsSync(p)) html = readFileSync(p, 'utf8');
  }
  const inline = src.match(/template:\s*`([\s\S]*?)`\s*,\n\s*(?:styleUrls?|host|imports|changeDetection|encapsulation|providers|selector)\s*:/);
  if (inline) html += inline[1];
  if (!html && !/\[class\./.test(src)) continue;

  const urls = [];
  const block = src.match(/styleUrls?:\s*(\[[\s\S]*?\]|'[^']+')/);
  if (block) for (const m of block[1].matchAll(/'([^']+)'/g)) urls.push(m[1]);
  let css = '';
  for (const u of urls) {
    const p = resolve(dirname(ts), u);
    if (existsSync(p)) css += readFileSync(p, 'utf8');
  }

  // Every class token the template writes, from static `class="..."` attributes.
  const written = new Set();
  for (const m of html.matchAll(/class="([^"]*)"/g)) {
    for (const token of m[1].split(/\s+/)) if (token) written.add(token);
  }

  for (const [cls, home] of Object.entries(VOCABULARY)) {
    // A static class token, or a host/element binding `[class.field-grid]`.
    const inTemplate = written.has(cls) || new RegExp(`\\[class\\.${cls}\\]`).test(src);
    if (!inTemplate) continue;
    checked += 1;
    // A rule for it in any stylesheet this component actually loads — including `:host(.x)`.
    const defined = new RegExp(`\\.${cls}\\s*[,{:.)\\s]`).test(css);
    if (!defined) problems.push({file: relative('.', ts), cls, home, urls});
  }
}

if (problems.length > 0) {
  console.error(`\n${problems.length} shared class(es) used where nothing defines them:\n`);
  for (const {file, cls, home, urls} of problems) {
    console.error(`  ${file}`);
    console.error(`    .${cls} — comes from ${home}`);
    console.error(`    styleUrls: ${urls.length ? urls.join(', ') : '(none)'}\n`);
  }
  console.error('Add the stylesheet to this component\'s styleUrls, use the component that owns the\n' +
                'rule, or define the class yourself. Angular scopes styles per component: a class\n' +
                'written without its definition in reach silently does nothing.\n');
  process.exit(1);
}

console.log(`css vocabulary: ${checked} shared-class use(s) checked — all resolve.`);
