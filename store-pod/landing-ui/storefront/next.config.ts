import path from 'path';
import {NextConfig} from 'next';
import createNextIntlPlugin from 'next-intl/plugin';

const monorepoRoot = path.join(__dirname, '..');

const nextConfig: NextConfig = {
    reactStrictMode: true,
    // Standalone output: the Dockerfile copies `.next/standalone` (server.js + traced node_modules).
    output: 'standalone',
    outputFileTracingRoot: monorepoRoot,
    images: {
        unoptimized: true,
    },
    turbopack: {
        root: monorepoRoot,
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
        // @themes:end
    ],
};

const withNextIntl = createNextIntlPlugin('./src/i18n/request.ts');
export default withNextIntl(nextConfig);
