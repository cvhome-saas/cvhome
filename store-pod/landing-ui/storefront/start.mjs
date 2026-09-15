/**
 * Production entrypoint (replaces the generated .next/standalone/storefront/server.js as CMD).
 *
 * Every production build bakes a sentinel assetPrefix (see next.config.ts). Before starting Next
 * this script substitutes it with the runtime value:
 *   - STATIC_ASSETS_SYNC_ENABLED=true → upload .next/static + public to S3 (skipped when the
 *     build's marker already exists in the bucket), then prefix = STATIC_ASSETS_BASE_URL (CDN).
 *   - otherwise (or on any sync failure) → prefix = '' → Next serves /_next/static itself,
 *     byte-for-byte today's behavior. A CDN hiccup must never take the storefront down.
 *
 * All STATIC_ASSETS_* env is read here at start — the image is environment-agnostic.
 * Works from both layouts: the standalone image (this file next to .next/) and a plain
 * `next build` tree via `npm start`.
 */
performance.mark('next-start');
import path from 'node:path';
import fs from 'node:fs';
import http from 'node:http';
import {fileURLToPath} from 'node:url';
import module from 'node:module';
import {ENV, SENTINEL} from './scripts/static-assets/constants.mjs';
import {applyAssetPrefix, readState} from './scripts/static-assets/apply-prefix.mjs';
import {syncStaticAssets} from './scripts/static-assets/sync-s3.mjs';
import {installRequestSignal, isDegraded, withRequestSignal} from './scripts/server/request-scope.mjs';
import {createPageCache, loopbackRevalidator, pageCacheSettings} from './scripts/server/page-cache.mjs';

const require = module.createRequire(import.meta.url);
const dir = fileURLToPath(new URL('.', import.meta.url));

process.env.NODE_ENV = 'production';
process.chdir(dir);

const nextDir = path.join(dir, '.next');
const publicDir = path.join(dir, 'public');
const buildId = fs.readFileSync(path.join(nextDir, 'BUILD_ID'), 'utf8').trim();
const {config} = JSON.parse(fs.readFileSync(path.join(nextDir, 'required-server-files.json'), 'utf8'));

let prefix = '';
if (process.env[ENV.SYNC_ENABLED] === 'true') {
    const state = readState(nextDir);
    if (state?.prefix === '') {
        console.warn(
            '[static-assets] sync enabled but this build output was already rewritten to ' +
            'origin-relative URLs — start from a fresh container/image to enable the CDN. Serving from origin.',
        );
    } else {
        try {
            await syncStaticAssets({nextDir, publicDir, buildId, env: process.env});
            prefix = (process.env[ENV.BASE_URL] || '').replace(/\/+$/, '');
        } catch (err) {
            console.error('[static-assets] sync failed — serving assets from origin:', err);
        }
    }
}

const appliedPrefix = applyAssetPrefix(nextDir, prefix, buildId);
config.assetPrefix = appliedPrefix;
// The build auto-adds the sentinel host to images.remotePatterns; swap it for the CDN host
// (or drop it in origin mode) and keep the on-disk config in step with what we serve.
if (Array.isArray(config.images?.remotePatterns)) {
    const sentinelHost = new URL(SENTINEL).hostname;
    const cdnHost = appliedPrefix ? new URL(appliedPrefix).hostname : null;
    config.images.remotePatterns = config.images.remotePatterns
        .map((p) => (p.hostname === sentinelHost ? (cdnHost ? {...p, hostname: cdnHost} : null) : p))
        .filter(Boolean);
    const serverFilesPath = path.join(nextDir, 'required-server-files.json');
    const serverFiles = JSON.parse(fs.readFileSync(serverFilesPath, 'utf8'));
    serverFiles.config = config;
    fs.writeFileSync(serverFilesPath, JSON.stringify(serverFiles));
}
process.env.__NEXT_PRIVATE_STANDALONE_CONFIG = JSON.stringify(config);

// From here on: identical to the generated standalone server.js.
const currentPort = parseInt(process.env.PORT, 10) || 8110;
const hostname = process.env.HOSTNAME || '0.0.0.0';
let keepAliveTimeout = parseInt(process.env.KEEP_ALIVE_TIMEOUT, 10);
if (Number.isNaN(keepAliveTimeout) || !Number.isFinite(keepAliveTimeout) || keepAliveTimeout < 0) {
    keepAliveTimeout = undefined;
}

require('next');
const {startServer} = require('next/dist/server/lib/start-server');

// An anonymous page is served from memory when another shopper asked for it a few seconds ago, and refreshed by a
// request of the cache's own to this port once it is stale (scripts/server/page-cache.mjs;
// STOREFRONT_PAGE_CACHE_TTL_SECONDS=0 turns it off). What is rendered runs with an abort signal that its backend calls
// carry, and a render with a read that gave up is not kept (scripts/server/request-scope.mjs).
installRequestSignal();
const pages = createPageCache({...pageCacheSettings(process.env), revalidate: loopbackRevalidator(() => currentPort),
    degraded: isDegraded});
const serve = (req, res, next) => pages.serve(req, res, keepAlive => withRequestSignal(res, next, keepAlive));

// startServer builds its own http.Server and keeps it; its listener is the one thing we wrap. It calls
// http.createServer(listener) synchronously, once, before its first await, so the wrapper is installed for exactly that
// call and removes itself. Everything else startServer does — graceful shutdown, keep-alive, the upgrade handler — is
// Next's own, unchanged.
const createServer = http.createServer;
http.createServer = (...args) => {
    http.createServer = createServer;
    const at = args.findIndex(arg => typeof arg === 'function');
    if (at >= 0) {
        const listener = args[at];
        args[at] = (req, res) => serve(req, res, () => listener(req, res));
    }
    return createServer.apply(http, args);
};

startServer({
    dir,
    isDev: false,
    config,
    hostname,
    port: currentPort,
    allowRetry: false,
    keepAliveTimeout,
}).catch((err) => {
    console.error(err);
    process.exit(1);
});
