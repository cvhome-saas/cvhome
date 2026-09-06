/**
 * Puts the OpenTelemetry instrumentation hook, and everything it needs at runtime, into the standalone output.
 *
 * Two gaps in `next build --output standalone`, both silent:
 *
 *  1. `<distDir>/server/instrumentation.js` is not copied. The server requires exactly that path
 *     (`getInstrumentationModule` in next/dist/server/lib/router-utils/instrumentation-globals.external) and
 *     swallows `MODULE_NOT_FOUND`, so the container starts with telemetry off and says nothing about it.
 *  2. Next keeps every `@opentelemetry` package except `api` external and never bundles it, and file tracing does
 *     not follow the dynamic `import('./src/shell/telemetry')` in instrumentation.ts. Even with the hook in place
 *     the SDK is not there — and the first missing module is only reported once (1) is fixed.
 *
 * The result was a deployed storefront with no page-render spans, no upstream fetch spans and no entry in the
 * service graph, while `next start` from the full build directory traced perfectly, because there both the hook and
 * `node_modules` are where the server expects them. It survived every local check and surfaced only when the image
 * itself was put under load.
 *
 * So: copy the hook plus the chunks it references (transitively — the whole `server/chunks` directory is ~31 MB
 * against ~4 KB of instrumentation), and copy the telemetry packages plus their dependency closure. Nothing is
 * hand-listed: the seeds are the app's own `@opentelemetry` dependencies and the closure comes from each package's
 * `dependencies`, so a version bump or a new instrumentation needs no change here.
 */
import fs from 'node:fs';
import path from 'node:path';
import module from 'node:module';

const require = module.createRequire(import.meta.url);
const dist = process.env.NEXT_DIST_DIR || '.next';
const app = path.basename(process.cwd());
const buildServer = path.join(dist, 'server');
const standalone = path.join(dist, 'standalone');
const standaloneServer = path.join(standalone, app, dist, 'server');
const standaloneModules = path.join(standalone, 'node_modules');
const hook = 'instrumentation.js';

if (!fs.existsSync(path.join(buildServer, hook))) {
    // No instrumentation.ts in this app, or the build produced none: nothing to do, and not an error.
    process.exit(0);
}
if (!fs.existsSync(standaloneServer)) {
    console.error(`[instrumentation] no standalone output at ${standaloneServer}; is output: 'standalone' still set?`);
    process.exit(1);
}

// ── the hook and its chunks ──────────────────────────────────────────────────────────────────────────────────────
/**
 * Chunk paths a compiled file pulls in, relative to `<distDir>/server`. Turbopack emits two forms: a plain
 * `require("./chunks/…")` for the runtime, and `R.c("server/chunks/…")` — a runtime call, not a require — for the
 * hook's own code. Matching only the first copies the runtime and leaves the module behind.
 */
const chunksReferencedBy = (file) => {
    const source = fs.readFileSync(file, 'utf8');
    return [
        ...[...source.matchAll(/require\("\.\/(chunks\/[^"]+\.js)"\)/g)].map((m) => m[1]),
        ...[...source.matchAll(/["'`]server\/(chunks\/[^"'`]+\.js)["'`]/g)].map((m) => m[1]),
    ];
};

const copyFile = (relative) => {
    const from = path.join(buildServer, relative);
    if (!fs.existsSync(from)) return false;
    const to = path.join(standaloneServer, relative);
    fs.mkdirSync(path.dirname(to), {recursive: true});
    fs.copyFileSync(from, to);
    return true;
};

const chunks = [];
const pendingChunks = [hook];
const seenChunks = new Set();
while (pendingChunks.length > 0) {
    const relative = pendingChunks.pop();
    if (seenChunks.has(relative)) continue;
    seenChunks.add(relative);
    if (copyFile(relative)) {
        chunks.push(relative);
        pendingChunks.push(...chunksReferencedBy(path.join(buildServer, relative)));
    }
}

// ── the telemetry packages and their dependency closure ──────────────────────────────────────────────────────────
/** Where a package lives, resolved from this app (npm hoists workspace dependencies to the workspace root). */
const packageDirOf = (name) => {
    try {
        return path.dirname(require.resolve(`${name}/package.json`, {paths: [process.cwd()]}));
    } catch {
        try {
            // Packages with no exports map for ./package.json: resolve the entry point and walk up to its root.
            let dir = path.dirname(require.resolve(name, {paths: [process.cwd()]}));
            while (dir !== path.dirname(dir) && !fs.existsSync(path.join(dir, 'package.json'))) dir = path.dirname(dir);
            return fs.existsSync(path.join(dir, 'package.json')) ? dir : null;
        } catch {
            return null;
        }
    }
};

const manifest = JSON.parse(fs.readFileSync('package.json', 'utf8'));
const seeds = Object.keys(manifest.dependencies ?? {}).filter((name) => name.startsWith('@opentelemetry/'));

const packages = [];
const pendingPackages = [...seeds];
const seenPackages = new Set();
while (pendingPackages.length > 0) {
    const name = pendingPackages.pop();
    if (seenPackages.has(name)) continue;
    seenPackages.add(name);
    const dir = packageDirOf(name);
    if (!dir) continue;
    const target = path.join(standaloneModules, name);
    if (!fs.existsSync(target)) {
        fs.cpSync(dir, target, {recursive: true, dereference: true});
        packages.push(name);
    }
    const own = JSON.parse(fs.readFileSync(path.join(dir, 'package.json'), 'utf8'));
    pendingPackages.push(...Object.keys(own.dependencies ?? {}));
}

console.log(`[instrumentation] standalone: ${chunks.length} build file(s), ${packages.length} package(s) copied `
    + `(${seenPackages.size} in the telemetry closure)`);
