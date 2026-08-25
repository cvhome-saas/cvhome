import path from 'path';
import {NextConfig} from 'next';
import {PHASE_PRODUCTION_BUILD} from 'next/constants';
import createNextIntlPlugin from 'next-intl/plugin';
import {SENTINEL as STATIC_ASSETS_SENTINEL} from './scripts/static-assets/constants.mjs';

const monorepoRoot = path.join(__dirname, '..');

const nextConfig: NextConfig = {
    reactStrictMode: true,
    // extra/lcl sets NEXT_DIST_DIR per stack so two dev servers from one checkout do not share `.next`.
    ...(process.env.NEXT_DIST_DIR ? { distDir: process.env.NEXT_DIST_DIR } : {}),
    // Standalone output: the Dockerfile copies `.next/standalone` (server.js + traced node_modules).
    output: 'standalone',
    outputFileTracingRoot: monorepoRoot,
    images: {
        unoptimized: true,
    },
    turbopack: {
        root: monorepoRoot,
    },
    // Loaded lazily by scripts/static-assets/sync-s3.mjs, outside the Next bundle. The tracing
    // includes force the SDK (and its runtime deps) into .next/standalone/node_modules so the
    // Docker image can upload to S3 without an npm install.
    serverExternalPackages: ['@aws-sdk/client-s3'],
    outputFileTracingIncludes: {
        '/**': [
            '../node_modules/@aws-sdk/**',
            '../node_modules/@aws/**',
            '../node_modules/@smithy/**',
            '../node_modules/tslib/**',
            '../node_modules/bowser/**',
            '../node_modules/uuid/**',
        ],
    },
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
