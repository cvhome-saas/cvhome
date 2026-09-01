#!/usr/bin/env node
/**
 * The per-theme client barrier — analysis and (idempotent) codemod.
 *
 * Why it exists: Turbopack computes the browser chunk group of a route from the WHOLE server module graph,
 * dynamic `import()` included. The registry's per-theme `import()` therefore splits server chunks only; every
 * theme's `'use client'` components still land in the one chunk group each storefront downloads (twelve
 * themes, 30 requests, ~445 KB brotli, for a store that renders one). What Turbopack does split is an
 * `import()` issued from a *client* module. So each theme gets:
 *
 *   src/client-bundle.ts   plain barrel of every 'use client' component + tokens.css + ThemeFrame
 *   src/client.ts          'use client'; one next/dynamic per component, all loading ./client-bundle
 *   src/ThemeFrame.tsx     'use client'; puts the next/font variables on <html> from inside the chunk
 *
 * and every server file of the theme imports client components ONLY through `./client`. One async chunk group
 * per theme, loaded by the store that renders it and by nobody else.
 *
 *   node scripts/theme-client-barrier.mjs <id>...     rewrite those themes
 *   node scripts/theme-client-barrier.mjs --all       rewrite every theme
 *   node scripts/theme-client-barrier.mjs --check     report violations, exit 1 if any (the guard test uses this)
 */
import {existsSync, readdirSync, readFileSync, statSync, writeFileSync} from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

export const GENERATED = ['client-bundle.ts', 'client.ts', 'ThemeFrame.tsx'];
const CLIENT_DIRECTIVE = /^(?:\s*(?:\/\/[^\n]*|\/\*[\s\S]*?\*\/)\s*)*['"]use client['"]/;
const EXPORTED_COMPONENT = /^export (?:async )?(?:function|const) ([A-Z][A-Za-z0-9]*)\b/gm;
const IMPORT = /^import\s+(type\s+)?\{([^}]*)\}\s+from\s+'(\.[^']*)';?[ \t]*\n/gm;

const walk = dir => readdirSync(dir).flatMap(f => {
    const p = path.join(dir, f);
    return statSync(p).isDirectory() ? walk(p) : [p];
});

const resolveRelative = (from, spec) => {
    const base = path.resolve(path.dirname(from), spec);
    for (const c of [base, `${base}.ts`, `${base}.tsx`, path.join(base, 'index.ts'), path.join(base, 'index.tsx')]) {
        if (existsSync(c) && statSync(c).isFile()) return c;
    }
    return null;
};

/** Everything the codemod and the guard need to know about one theme's `src/`. */
export function analyzeTheme(themeDir) {
    const src = path.join(themeDir, 'src');
    const files = walk(src).filter(f => /\.tsx?$/.test(f));
    const read = f => readFileSync(f, 'utf8');
    const generated = new Set(GENERATED.map(g => path.join(src, g)));
    const clientFiles = files.filter(f => !generated.has(f) && CLIENT_DIRECTIVE.test(read(f)));
    const clientSet = new Set(clientFiles);
    // component exports per client file, in file order
    const components = new Map(clientFiles.map(f => [f, [...read(f).matchAll(EXPORTED_COMPONENT)].map(m => m[1])]));
    const allComponents = [...components.values()].flat();
    const dupes = allComponents.filter((c, i) => allComponents.indexOf(c) !== i);
    if (dupes.length) throw new Error(`${themeDir}: component names must be unique across client files: ${dupes.join(', ')}`);

    // server files importing client files directly (value imports; `import type` is erased and allowed)
    const directImports = [];
    for (const f of files) {
        if (clientSet.has(f) || generated.has(f)) continue;
        for (const m of read(f).matchAll(IMPORT)) {
            const target = resolveRelative(f, m[3]);
            if (!target || !clientSet.has(target)) continue;
            const specs = m[2].split(',').map(s => s.trim()).filter(Boolean);
            const values = specs.filter(s => !s.startsWith('type '));
            if (m[1] || values.length === 0) continue;
            const unknown = values.filter(v => !allComponents.includes(v));
            directImports.push({file: f, spec: m[3], target, values, types: specs.filter(s => s.startsWith('type ')), unknown, raw: m[0]});
        }
    }

    const violations = [];
    const rel = f => path.relative(themeDir, f);
    for (const d of directImports) {
        violations.push(`${rel(d.file)} imports {${d.values.join(', ')}} from '${d.spec}' (a 'use client' file) — import it from './client'`);
        for (const u of d.unknown) violations.push(`${rel(d.file)} imports ${u} from '${d.spec}': not a component export, it cannot cross the client barrier`);
    }
    const index = read(path.join(src, 'index.ts'));
    if (/^import ['"]\.\/tokens\.css['"]/m.test(index)) violations.push("index.ts imports ./tokens.css — the bundle owns it (it would land in the shared layout CSS)");
    if (/from ['"]\.\/fonts['"]/.test(index)) violations.push("index.ts imports ./fonts — ThemeFrame owns it (its CSS would land in the shared layout entry)");
    for (const g of GENERATED) if (!existsSync(path.join(src, g))) violations.push(`src/${g} is missing — run scripts/theme-client-barrier.mjs`);
    const bundle = existsSync(path.join(src, 'client-bundle.ts')) ? read(path.join(src, 'client-bundle.ts')) : '';
    const client = existsSync(path.join(src, 'client.ts')) ? read(path.join(src, 'client.ts')) : '';
    if (bundle) {
        for (const c of allComponents) {
            if (!new RegExp(`\\b${c}\\b`).test(bundle)) violations.push(`client-bundle.ts does not export ${c}`);
            if (!new RegExp(`export const ${c} = dynamic\\(`).test(client)) violations.push(`client.ts has no dynamic wrapper for ${c}`);
        }
        if (/loading:/.test(client)) {
            violations.push('client.ts passes a `loading` option — the Suspense boundary it adds turns notFound() into a streamed 200');
        }
        if (!/^import ['"]\.\/tokens\.css['"]/m.test(bundle)) violations.push('client-bundle.ts does not import ./tokens.css');
    }
    return {src, files, clientFiles, components, allComponents, directImports, violations};
}

const themeId = src => {
    const m = readFileSync(path.join(src, 'index.ts'), 'utf8').match(/^\s*id: '([a-z][a-z0-9-]*)'/m);
    if (!m) throw new Error(`${src}/index.ts: cannot find the theme id`);
    return m[1];
};

export function applyTheme(themeDir) {
    const a = analyzeTheme(themeDir);
    const {src} = a;
    const id = themeId(src);

    // 1. ThemeFrame (kept if it already exists — a theme may have edited it)
    const frame = path.join(src, 'ThemeFrame.tsx');
    if (!existsSync(frame)) {
        writeFileSync(frame, `'use client'
import {type ReactNode, useLayoutEffect} from 'react';
import {fonts} from './fonts';

const CLASSES = fonts.variables.split(' ').filter(Boolean);
// Runs while the HTML is parsed, before first paint: the next/font variable classes reach <html> without the
// font CSS ever entering the shared layout entry (it lives in this theme's chunk, with tokens.css).
const APPLY = \`document.documentElement.classList.add(\${CLASSES.map(c => JSON.stringify(c)).join(',')})\`;

/**
 * The theme's client frame — the one component of the lazily loaded theme chunk the shell always renders.
 * tokens.css says \`[data-theme="${id}"] { --font-body: var(--font-${id}-…) }\`: a custom property resolves on the
 * element that declares it, so the font classes must sit on <html>, next to data-theme, or every portal
 * (drawers, selects, toasts render into <body>) would fall back to the browser font. The inline script does it
 * before paint; the effect repeats it for a client-side mount. <html> carries suppressHydrationWarning for it.
 */
export function ThemeFrame({children}: { children: ReactNode }) {
    useLayoutEffect(() => {
        document.documentElement.classList.add(...CLASSES);
    }, []);
    return (
        <>
            <script dangerouslySetInnerHTML={{__html: APPLY}}/>
            {children}
        </>
    );
}
`);
    }

    // 2. client-bundle.ts — every client component, the tokens and the frame, in one module = one chunk
    const relImport = f => './' + path.relative(src, f).replace(/\\/g, '/').replace(/\.tsx?$/, '');
    const lines = [
        `// GENERATED by scripts/theme-client-barrier.mjs — the single import target of ./client.ts, so that every`,
        `// 'use client' component of this theme, its tokens and its fonts form ONE lazily loaded chunk. Do not import`,
        `// this module directly; server files import ./client, client files import each other.`,
        `import './tokens.css';`,
        `export {ThemeFrame} from './ThemeFrame';`,
    ];
    for (const [f, comps] of a.components) if (comps.length) lines.push(`export {${comps.join(', ')}} from '${relImport(f)}';`);
    writeFileSync(path.join(src, 'client-bundle.ts'), lines.join('\n') + '\n');

    // 3. client.ts — the barrier
    const wrappers = ['ThemeFrame', ...a.allComponents].map(c =>
        `export const ${c} = dynamic(() => import('./client-bundle').then(m => ({default: m.${c}})));`);
    writeFileSync(path.join(src, 'client.ts'), `'use client'
// GENERATED by scripts/theme-client-barrier.mjs — the client barrier of the ${id} theme.
//
// Server components import client components from HERE, never from their files: a direct import makes the
// component part of the route's shared chunk group (every store downloads it), while a next/dynamic issued from
// this client module makes ./client-bundle its own async chunk, fetched only by stores rendering this theme.
// The \`import()\` literal is what the next/dynamic transform records, so SSR preloads the chunk and its CSS.
// No \`loading\` option on purpose: it would wrap each component in a Suspense boundary, and the Frame's boundary
// above the page would flush the shell before a page's notFound()/error — every 404 would stream as a 200.
import dynamic from 'next/dynamic';

${wrappers.join('\n')}
`);

    // 4. server files: import from ./client; types keep pointing at the file
    for (const d of a.directImports) {
        let text = readFileSync(d.file, 'utf8');
        const toClient = path.relative(path.dirname(d.file), path.join(src, 'client')).replace(/\\/g, '/');
        const spec = toClient.startsWith('.') ? toClient : `./${toClient}`;
        let replacement = `import {${d.values.join(', ')}} from '${spec}';\n`;
        if (d.types.length) replacement = `import {${d.types.join(', ')}} from '${d.spec}';\n` + replacement;
        text = text.replace(d.raw, replacement);
        writeFileSync(d.file, text);
    }
    // merge duplicate ./client imports a file may now have
    for (const f of a.files) {
        if (a.clientFiles.includes(f)) continue;
        let text = readFileSync(f, 'utf8');
        const toClient = path.relative(path.dirname(f), path.join(src, 'client')).replace(/\\/g, '/');
        const spec = toClient.startsWith('.') ? toClient : `./${toClient}`;
        const re = new RegExp(`^import\\s+\\{([^}]*)\\}\\s+from\\s+'${spec.replace(/[.]/g, '\\.')}';?[ \\t]*\\n`, 'gm');
        const found = [...text.matchAll(re)];
        if (found.length > 1) {
            const specs = [...new Set(found.flatMap(m => m[1].split(',').map(s => s.trim()).filter(Boolean)))];
            let first = true;
            text = text.replace(re, () => first ? (first = false, `import {${specs.join(', ')}} from '${spec}';\n`) : '');
            writeFileSync(f, text);
        }
    }

    // 5. index.ts: no tokens/fonts, Frame on the layout
    const indexPath = path.join(src, 'index.ts');
    let index = readFileSync(indexPath, 'utf8');
    index = index.replace(/^import ['"]\.\/tokens\.css['"];?\n/m, '').replace(/^import \{fonts\} from ['"]\.\/fonts['"];?\n/m, '').replace(/^\s*fonts,\n/m, '');
    if (!/Frame: ThemeFrame/.test(index)) {
        if (!/layout: \{config: layoutConfig, Root\}/.test(index)) throw new Error(`${indexPath}: expected "layout: {config: layoutConfig, Root}"`);
        index = index.replace('layout: {config: layoutConfig, Root}', 'layout: {config: layoutConfig, Root, Frame: ThemeFrame}');
        index = index.replace(/^import \{([^}]*)\} from '\.\/client';\n/m, (m, specs) => `import {${[...new Set([...specs.split(',').map(s => s.trim()), 'ThemeFrame'])].join(', ')}} from './client';\n`);
        if (!/from '\.\/client'/.test(index)) index = index.replace(/^(import \{defineTheme\} from '@store-front\/theme';\n)/m, `$1import {ThemeFrame} from './client';\n`);
    }
    writeFileSync(indexPath, index);
    return analyzeTheme(themeDir);
}

export function themeDirs(root) {
    const themes = path.join(root, 'themes');
    return readdirSync(themes).map(d => path.join(themes, d)).filter(d => existsSync(path.join(d, 'src', 'index.ts')));
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
    const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
    const args = process.argv.slice(2);
    if (args.includes('--check')) {
        let bad = 0;
        for (const dir of themeDirs(root)) {
            const {violations} = analyzeTheme(dir);
            for (const v of violations) console.error(`${path.basename(dir)}: ${v}`);
            bad += violations.length;
        }
        process.exit(bad ? 1 : 0);
    }
    const dirs = args.includes('--all') ? themeDirs(root) : args.map(id => path.join(root, 'themes', id));
    if (!dirs.length) {
        console.error('Usage: node scripts/theme-client-barrier.mjs <id>... | --all | --check');
        process.exit(1);
    }
    for (const dir of dirs) {
        const {violations, allComponents} = applyTheme(dir);
        console.log(`${path.basename(dir)}: ${allComponents.length} components behind ./client${violations.length ? '\n  ' + violations.join('\n  ') : ''}`);
        if (violations.length) process.exitCode = 1;
    }
}
