import path from 'path';
import {NextConfig} from 'next';
import {PHASE_PRODUCTION_BUILD} from 'next/constants';
import createNextIntlPlugin from 'next-intl/plugin';
import {SENTINEL as STATIC_ASSETS_SENTINEL} from './scripts/static-assets/constants.mjs';

const monorepoRoot = path.join(__dirname, '..');

// `next dev` refuses cross-origin requests to its /_next resources, the HMR socket among them (blocked since Next 16.2,
// a warning before). Under lcl the browser reaches it through spg as `<store>.<pod host>`, the host INTERNAL_SPG names,
// so that host's subdomains are allowed. A production server ignores the option.
function devOrigins(): string[] {
    try {
        return process.env.INTERNAL_SPG ? [`*.${new URL(process.env.INTERNAL_SPG).hostname}`] : [];
    } catch {
        return [];
    }
}

const nextConfig: NextConfig = {
    reactStrictMode: true,
    allowedDevOrigins: devOrigins(),
    // Since 16.3, `next dev` writes its own AGENTS.md and CLAUDE.md into storefront/ whenever it detects a coding agent,
    // and re-creates them on every start. Agents run this app's lcl stacks all day; the repo's agent rules are its root
    // AGENTS.md and the project-structure skill, and a vendor copy in the app directory would be a second, conflicting one.
    agentRules: false,
    // Two lcl stacks can run this app from one checkout; each needs its own build directory or they
    // overwrite each other's dev output. Unset (a plain `npm run dev`) keeps Next's default `.next`.
    distDir: process.env.NEXT_DIST_DIR || '.next',
    // Standalone output: the Dockerfile copies `.next/standalone` (server.js + traced node_modules).
    output: 'standalone',
    outputFileTracingRoot: monorepoRoot,
    images: {
        unoptimized: true,
    },
    // spg's Caddy compresses the HTML (`encode zstd gzip` in store-pod/spg/Caddyfile), not this process: gzip
    // in Node cost 10-25 % of every render on a task of a quarter vCPU, while spg idles. Anything reaching
    // landing-ui without spg (its own port) gets uncompressed HTML.
    compress: false,
    // Stylesheets as files, not inlined. Inlining (turned on because Lighthouse flagged 15 render-blocking CSS
    // <link>s) put the CSS of every theme into every page twice: once as <style> and again as strings in the
    // RSC payload, because the registry imports all themes into the layout's entry. That was ~580 KiB of an
    // 864 KiB home page, serialised, escaped and compressed on every request: turning it off cut CPU per render by
    // a fifth to a third on a 0.25 vCPU task. The cost is back on the browser's side: a first visit waits on the
    // stylesheet links, which the CDN and the browser then cache.
    experimental: {
        inlineCss: false,
    },
    turbopack: {
        root: monorepoRoot,
    },
    // Loaded lazily by scripts/static-assets/sync-s3.mjs, outside the Next bundle. scripts/copy-instrumentation.mjs
    // copies every package listed here, with its dependency closure, into .next/standalone/node_modules, so the Docker
    // image can upload to S3 without an npm install. Not `outputFileTracingIncludes`: Turbopack matches those globs
    // anywhere in a path, and since 16.3 it hashes every match, so `node_modules/@aws-sdk/**` found the directory
    // symlinks `next dev` leaves in `.next-<stack>/dev/node_modules/` and failed the build (EISDIR).
    serverExternalPackages: ['@aws-sdk/client-s3'],
    // Source packages (no build step) compiled by Next. Themes are appended by scripts/new-theme.mjs.
    transpilePackages: [
        '@store-front/ui',
        '@store-front/i18n',
        '@store-front/theme',
        // @themes:start
        '@store-front/theme-starter',
        '@store-front/theme-beauty',
        '@store-front/theme-fashion',
        '@store-front/theme-basic',
        '@store-front/theme-grocery',
        '@store-front/theme-pink',
        '@store-front/theme-hunger',
        '@store-front/theme-furniture',
        '@store-front/theme-glasses',
        '@store-front/theme-cosmetics',
        '@store-front/theme-sports',
        '@store-front/theme-jewellery',
        // @themes:end
    ],
};

const withNextIntl = createNextIntlPlugin('./src/i18n/request.ts');

export default function configure(phase: string) {
    if (phase === PHASE_PRODUCTION_BUILD) {
        // Sentinel substituted at server start by start.mjs (CDN base URL or ''). Turbopack bakes
        // asset URLs into the output, so the prefix must exist at build time; dev stays untouched.
        nextConfig.assetPrefix = STATIC_ASSETS_SENTINEL;
    }
    return withNextIntl(nextConfig);
}
