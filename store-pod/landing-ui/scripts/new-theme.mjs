#!/usr/bin/env node
/**
 * Scaffold a new storefront theme from `themes/starter`.
 *
 *   npm run new-theme <id>          (kebab-case, e.g. atelier)
 *
 * What it does — and what you then do by hand is printed at the end:
 *  1. copies themes/starter → themes/<id> (fresh DESIGN.md placeholder, README stub)
 *  2. renames package name / theme id / tokens.css selector
 *  3. registers the theme: storefront registry, themes.css import, next.config transpilePackages,
 *     legacy-theme-map entry, Theme enum value in libs/types (if absent), a default-palette seed in
 *     libs/types/scripts/build-color-schemas.mjs (THEME_DEFAULTS — regenerates themes/<id>/src/colors.ts)
 *  4. runs `npm install` so the workspace link exists
 */
import {cpSync, existsSync, mkdirSync, readdirSync, readFileSync, statSync, writeFileSync} from 'node:fs';
import {execSync} from 'node:child_process';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const id = (process.argv[2] ?? '').trim();

if (!/^[a-z][a-z0-9-]*$/.test(id)) {
    console.error('Usage: npm run new-theme <id>   — id must be kebab-case (a-z, 0-9, -), e.g. "atelier"');
    process.exit(1);
}
if (id === 'starter') {
    console.error('"starter" is the template itself.');
    process.exit(1);
}
const src = path.join(root, 'themes', 'starter');
const dst = path.join(root, 'themes', id);
if (existsSync(dst)) {
    console.error(`themes/${id} already exists.`);
    process.exit(1);
}

// 1. copy
cpSync(src, dst, {recursive: true, filter: p => !/node_modules|\.next|\.DS_Store/.test(p)});

// 2. rename inside the copy
const titleCase = id.split('-').map(s => s[0].toUpperCase() + s.slice(1)).join(' ');
const walk = dir => readdirSync(dir).flatMap(f => {
    const p = path.join(dir, f);
    return statSync(p).isDirectory() ? walk(p) : [p];
});
for (const file of walk(dst)) {
    if (!/\.(ts|tsx|css|json|md)$/.test(file)) continue;
    let text = readFileSync(file, 'utf8');
    text = text
        .replaceAll('@store-front/theme-starter', `@store-front/theme-${id}`)
        .replaceAll('[data-theme="starter"]', `[data-theme="${id}"]`)
        .replaceAll("id: 'starter'", `id: '${id}'`)
        .replaceAll("name: 'Starter'", `name: '${titleCase}'`)
        .replaceAll('--font-starter-', `--font-${id}-`)
        .replaceAll('starter-announcement', `${id}-announcement`)
        .replaceAll('starter-hero', `${id}-hero`)
        .replaceAll('starter-rail', `${id}-rail`);
    writeFileSync(file, text);
}
writeFileSync(path.join(dst, 'DESIGN.md'), `# ${titleCase} — DESIGN.md\n\n_Not written yet._ DESIGN.md is produced at finish by the impeccable documenter from the built theme.\nUntil then this theme is a copy of \`starter\` and has no visual world of its own.\n`);
writeFileSync(path.join(dst, 'README.md'), `# ${id} theme\n\nScaffolded from \`starter\` on ${new Date().toISOString().slice(0, 10)}.\nSee \`themes/README.md\` (direction catalog) and the theme guide in\n\`.agents/skills/project-structure/references/new-landing-ui-template.md\`.\n`);
const pkgPath = path.join(dst, 'package.json');
const pkg = JSON.parse(readFileSync(pkgPath, 'utf8'));
pkg.name = `@store-front/theme-${id}`;
pkg.version = '0.1.0';
pkg.description = `${titleCase} storefront theme.`;
writeFileSync(pkgPath, JSON.stringify(pkg, null, 2) + '\n');

// 3. register
function insertBefore(file, marker, line) {
    const p = path.join(root, file);
    const text = readFileSync(p, 'utf8');
    if (text.includes(line.trim())) return;
    if (!text.includes(marker)) throw new Error(`${file}: marker "${marker}" not found`);
    writeFileSync(p, text.replace(marker, `${line}\n${marker}`));
}
insertBefore('storefront/src/shell/theme/registry.ts', '    // @themes:end', `    '${id}': () => import('@store-front/theme-${id}'),`);
insertBefore('storefront/src/app/themes.css', '/* @themes:end */', `@source "../../../themes/${id}/src";`);
insertBefore('storefront/next.config.ts', '        // @themes:end', `        '@store-front/theme-${id}',`);
{
    // Legacy map: a same-name enum value (e.g. `beauty`) already has an entry — repoint it instead of duplicating the key.
    const p = path.join(root, 'storefront/src/shell/theme/legacy-theme-map.ts');
    const text = readFileSync(p, 'utf8');
    const keyRe = new RegExp(`^(\\s*)(['"]?)${id.replaceAll('-', '\\-')}\\2:\\s*'[^']*',\\s*$`, 'm');
    if (keyRe.test(text)) writeFileSync(p, text.replace(keyRe, `$1$2${id}$2: '${id}',`));
    else insertBefore('storefront/src/shell/theme/legacy-theme-map.ts', '    // @legacy-themes:end', `    '${id}': '${id}',`);
}

const sfPkgPath = path.join(root, 'storefront', 'package.json');
const sfPkg = JSON.parse(readFileSync(sfPkgPath, 'utf8'));
sfPkg.dependencies[`@store-front/theme-${id}`] = '*';
sfPkg.dependencies = Object.fromEntries(Object.entries(sfPkg.dependencies).sort(([a], [b]) => a.localeCompare(b)));
writeFileSync(sfPkgPath, JSON.stringify(sfPkg, null, 2) + '\n');

const enumPath = path.join(root, 'libs', 'types', 'src', 'store.ts');
const enumName = id.toUpperCase().replaceAll('-', '_');
let enumSrc = readFileSync(enumPath, 'utf8');
if (!new RegExp(`^\\s*${enumName}\\s*=`, 'm').test(enumSrc)) {
    enumSrc = enumSrc.replace(/(export enum Theme \{[\s\S]*?)(\n\})/, `$1\n    ${enumName} = '${enumName}',$2`);
    writeFileSync(enumPath, enumSrc);
}

// Default palette: seed a copy of starter's under THEME_DEFAULTS, then regenerate themes/<id>/src/colors.ts.
{
    const genPath = path.join(root, 'libs', 'types', 'scripts', 'build-color-schemas.mjs');
    const gen = readFileSync(genPath, 'utf8');
    const keyRe = new RegExp(`^\\s*(['"]?)${id.replaceAll('-', '\\-')}\\1:\\s*\\{`, 'm');
    if (!keyRe.test(gen)) {
        const starterSeed = gen.match(/^    starter: \{\n([\s\S]*?)^    \},\n/m);
        if (!starterSeed) throw new Error('build-color-schemas.mjs: THEME_DEFAULTS.starter seed not found');
        const body = starterSeed[1].replace(/note: '[^']*'/, `note: 'TODO ${titleCase}: replace with the palette of the chosen visual world (copied from starter).'`);
        insertBefore('libs/types/scripts/build-color-schemas.mjs', '    // @theme-defaults:end', `    '${id}': {\n${body}    },`);
    }
    execSync('node scripts/build-color-schemas.mjs', {cwd: path.join(root, 'libs', 'types'), stdio: 'inherit'});
}

// 4. install
execSync('npm install', {cwd: root, stdio: 'inherit'});

console.log(`
✅ themes/${id} scaffolded and registered.

Next steps
  1. Design it — the impeccable flow (see the guide):
       cd store-pod/landing-ui
       node ~/.claude/skills/impeccable/scripts/context.mjs --target themes/${id}
     PRODUCT.md is inherited from store-pod/landing-ui/PRODUCT.md; the direction is chosen by
     new-work + concept-seed (the roll is mandatory), then build page by page, finish review,
     documenter writes themes/${id}/DESIGN.md.
  2. Edit themes/${id}/src/{tokens.css,fonts.ts,config.ts,layout,pages,sections,components,states}.
     Every page in ThemePages is required except Search: a theme without pages/Search.tsx gets the
     shell's plain results page, built from tokens so it still looks like this theme. Copy
     themes/basic/src/{pages/Search.tsx,sections/SearchResults.tsx,states/skeletons/SearchSkeleton.tsx}
     when you want a designed one, and register `search` in states.PageSkeleton alongside it.
     Its default palette (ColorTheme DEFAULT) is generated into themes/${id}/src/colors.ts — edit the
     '${id}' seed in libs/types/scripts/build-color-schemas.mjs (THEME_DEFAULTS), then
     \`npm run gen:colors -w libs/types\`; never hand-edit colors.ts.
  3. Run:  npm run dev   →  http://localhost:8110/en?theme=${id}&color=default   (or STOREFRONT_THEME=${id};
     \`?color=<PRESET>\` previews a merchant preset on top)
  4. Backend (out of scope here): add ${enumName} to the Java Theme enum with implemented=true so
     merchants can pick it; until then map existing enum values to '${id}' in
     storefront/src/shell/theme/legacy-theme-map.ts.
`);
