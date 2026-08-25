/**
 * Shared constants for the optional static-assets CDN mode (see start.mjs).
 *
 * The sentinel is baked into every production build as `assetPrefix` (next.config.ts) and
 * substituted at server start with STATIC_ASSETS_BASE_URL (CDN mode) or '' (origin mode).
 * RFC 2606 `.invalid` TLD: a syntactically valid URL Next accepts, guaranteed to never resolve.
 */
export const SENTINEL = 'https://storefront-static.invalid';

export const ENV = {
    SYNC_ENABLED: 'STATIC_ASSETS_SYNC_ENABLED',
    S3_BUCKET: 'STATIC_ASSETS_S3_BUCKET',
    S3_PREFIX: 'STATIC_ASSETS_S3_PREFIX',
    BASE_URL: 'STATIC_ASSETS_BASE_URL',
    S3_ENDPOINT: 'STATIC_ASSETS_S3_ENDPOINT',
    S3_FORCE_PATH_STYLE: 'STATIC_ASSETS_S3_FORCE_PATH_STYLE',
};

// File extensions whose content may carry the sentinel and must be rewritten as text.
export const TEXT_EXTENSIONS = new Set([
    'js', 'mjs', 'cjs', 'json', 'rsc', 'html', 'css', 'txt', 'map', 'body', 'meta',
]);

export const CONTENT_TYPES = {
    js: 'application/javascript',
    mjs: 'application/javascript',
    cjs: 'application/javascript',
    css: 'text/css',
    json: 'application/json',
    map: 'application/json',
    html: 'text/html; charset=utf-8',
    txt: 'text/plain; charset=utf-8',
    xml: 'application/xml',
    woff2: 'font/woff2',
    woff: 'font/woff',
    ttf: 'font/ttf',
    otf: 'font/otf',
    png: 'image/png',
    jpg: 'image/jpeg',
    jpeg: 'image/jpeg',
    gif: 'image/gif',
    webp: 'image/webp',
    avif: 'image/avif',
    svg: 'image/svg+xml',
    ico: 'image/x-icon',
};

export const DEFAULT_CONTENT_TYPE = 'application/octet-stream';
