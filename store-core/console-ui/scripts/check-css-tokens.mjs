#!/usr/bin/env node
/**
 * Every `var(--x)` written without a fallback resolves to a property something actually defines.
 *
 * This is the `--surface-sunken` class of bug: `branding-tab.css` styled two panels with tokens no
 * theme declared, so the declarations resolved to nothing and the tab simply looked wrong. Nothing
 * failed — not `ng build`, not the specs, not stylelint, which has no opinion about whether a
 * custom property exists. Two more were found the same week: `order-details.css` sized a heading
 * from `--text-operations-title`, which is an `@utility` rather than a property, and
 * `plan-banner.css` painted its warn state with `--warning-veil`, which no theme defined — so the
 * warn state was painted exactly like the default.
 *
 * **Why not `stylelint-value-no-unknown-custom-properties`.** It reports 29 names here and 22 of
 * them are correct code, because a property in this app can legitimately come from three places
 * that a CSS-only resolver cannot see:
 *
 *   1. Tailwind's own default theme — `--color-slate-100`, `--font-weight-medium`, `--font-mono`.
 *      Emitted at build time, in no file here. Read out of `tailwindcss/theme.css` rather than
 *      prefix-matched: `--text-*` is a Tailwind namespace, and prefix-matching it waves through
 *      `--text-muted`, which is one of the two names this was written to catch.
 *   2. A TypeScript host binding or a template — `[style.--table-columns]` in `data-table.ts`,
 *      `[style.--depth]` in `tree.html`. Set per row, at runtime.
 *   3. The component's own stylesheet, which is the ordinary case.
 *
 * So this scans all three, and reports only a `var()` written *without* a fallback. One with a
 * fallback is a deliberate customisation hook and cannot silently resolve to nothing — which is
 * precisely the failure being hunted.
 */
import {readFileSync} from 'node:fs';
import {execSync} from 'node:child_process';

const SOURCE = 'src';

/**
 * The exact set Tailwind emits from its own default theme, read from the installed package so it
 * tracks the dependency instead of drifting from a hand-copied list. ~419 names.
 */
const TAILWIND_DEFAULTS = new Set(
  [...readFileSync('node_modules/tailwindcss/theme.css', 'utf8').matchAll(/^\s*(--[a-z0-9-]+)\s*:/gim)]
    .map((m) => m[1]),
);

/** `--spacing-<name>` and the like: Tailwind resolves any key in a namespace it owns. */
const TAILWIND_DYNAMIC = /^--(?:spacing|container|breakpoint)$/;

const files = execSync(
  `find ${SOURCE} -type f \\( -name '*.css' -o -name '*.ts' -o -name '*.html' \\)`,
  {encoding: 'utf8'},
).trim().split('\n').filter(Boolean);

const defined = new Set();
const uses = [];

for (const file of files) {
  const raw = readFileSync(file, 'utf8');

  // Declared in a stylesheet: `--x: value`, anywhere a declaration can sit.
  for (const m of raw.matchAll(/(--[a-z0-9-]+)\s*:/gi)) defined.add(m[1]);

  // Set from a template or a host binding: [style.--x], '[style.--x]', style.setProperty('--x').
  for (const m of raw.matchAll(/style\.(--[a-z0-9-]+)/gi)) defined.add(m[1]);
  for (const m of raw.matchAll(/setProperty\(\s*['"`](--[a-z0-9-]+)/gi)) defined.add(m[1]);

  if (!file.endsWith('.css')) continue;
  // Read without a fallback: `var(--x)` and not `var(--x, …)`.
  for (const m of raw.matchAll(/var\(\s*(--[a-z0-9-]+)\s*\)/gi)) {
    const line = raw.slice(0, m.index).split('\n').length;
    uses.push({file, line, name: m[1]});
  }
}

const fromTailwind = (name) => TAILWIND_DEFAULTS.has(name) || TAILWIND_DYNAMIC.test(name);
const problems = uses.filter(({name}) => !defined.has(name) && !fromTailwind(name));

if (problems.length > 0) {
  console.error(`\n${problems.length} custom propert(ies) are read with no fallback and defined nowhere:\n`);
  for (const {file, line, name} of problems) console.error(`  ${file}:${line}\n    var(${name})`);
  console.error(
    '\nDefine it in a theme, set it from the component, or give the var() a fallback if it is\n' +
    'meant to be an optional hook. A property that resolves to nothing fails silently.\n',
  );
  process.exit(1);
}

console.log(
  `css tokens: ${uses.length} fallback-free var() read(s) checked against ${defined.size} definitions — all resolve.`,
);
