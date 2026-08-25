/**
 * Pushes the build's static assets to S3 at server start (CDN mode).
 *
 * Skip check: a per-build marker object `${PREFIX}/_builds/${BUILD_ID}` — if it exists the build
 * is already in the bucket and nothing is uploaded. Old builds are never deleted: rolling deploys
 * keep old tasks alive whose content-hashed chunks must stay fetchable.
 *
 * `@aws-sdk/client-s3` reaches the standalone image via Next file tracing (see instrumentation.ts)
 * and is loaded lazily here so origin-mode starts never touch it.
 */
import fs from 'node:fs';
import path from 'node:path';
import module from 'node:module';
import {CONTENT_TYPES, DEFAULT_CONTENT_TYPE, ENV, SENTINEL, TEXT_EXTENSIONS} from './constants.mjs';

const UPLOAD_CONCURRENCY = 16;

function* walkFiles(root) {
    if (!fs.existsSync(root)) return;
    for (const entry of fs.readdirSync(root, {withFileTypes: true, recursive: true})) {
        if (entry.isFile()) yield path.join(entry.parentPath, entry.name);
    }
}

function toPosixKey(root, file) {
    return path.relative(root, file).split(path.sep).join('/');
}

function extOf(file) {
    return path.extname(file).slice(1).toLowerCase();
}

/**
 * Uploads `.next/static/**` → `${PREFIX}/_next/static/**` (immutable) and `public/**` →
 * `${PREFIX}/**` (1h). Text files get the sentinel rewritten to the CDN base URL before upload
 * (the turbopack runtime chunk carries it). Throws on any failure — the caller falls back to
 * origin serving.
 */
export async function syncStaticAssets({nextDir, publicDir, buildId, env}) {
    const bucket = env[ENV.S3_BUCKET];
    const baseUrl = (env[ENV.BASE_URL] || '').replace(/\/+$/, '');
    const keyPrefix = (env[ENV.S3_PREFIX] || '').replace(/^\/+|\/+$/g, '');
    if (!bucket) throw new Error(`${ENV.S3_BUCKET} is required when ${ENV.SYNC_ENABLED}=true`);
    if (!baseUrl) throw new Error(`${ENV.BASE_URL} is required when ${ENV.SYNC_ENABLED}=true`);

    const require = module.createRequire(import.meta.url);
    const {S3Client, HeadObjectCommand, PutObjectCommand} = require('@aws-sdk/client-s3');
    const client = new S3Client({
        ...(env.AWS_REGION ? {region: env.AWS_REGION} : {}),
        ...(env[ENV.S3_ENDPOINT] ? {endpoint: env[ENV.S3_ENDPOINT]} : {}),
        ...(env[ENV.S3_FORCE_PATH_STYLE] === 'true' ? {forcePathStyle: true} : {}),
    });
    const key = (...parts) => [keyPrefix, ...parts].filter(Boolean).join('/');
    const markerKey = key('_builds', buildId);

    try {
        await client.send(new HeadObjectCommand({Bucket: bucket, Key: markerKey}));
        console.log(`[static-assets] build ${buildId} already synced (s3://${bucket}/${markerKey}) — skipping upload`);
        return {uploaded: 0, skipped: true};
    } catch (err) {
        const notFound = err?.name === 'NotFound' || err?.$metadata?.httpStatusCode === 404;
        if (!notFound) throw err;
    }

    const staticRoot = path.join(nextDir, 'static');
    const uploads = [
        ...[...walkFiles(staticRoot)].map((file) => ({
            file,
            key: key('_next/static', toPosixKey(staticRoot, file)),
            cacheControl: 'public, max-age=31536000, immutable',
        })),
        ...[...walkFiles(publicDir)].map((file) => ({
            file,
            key: key(toPosixKey(publicDir, file)),
            cacheControl: 'public, max-age=3600',
        })),
    ];

    let next = 0;
    const worker = async () => {
        while (next < uploads.length) {
            const {file, key: objectKey, cacheControl} = uploads[next++];
            const ext = extOf(file);
            let body = fs.readFileSync(file);
            if (TEXT_EXTENSIONS.has(ext)) {
                // replaceAll is a no-op when the sentinel was already substituted on disk
                body = Buffer.from(body.toString('utf8').replaceAll(SENTINEL, baseUrl));
            }
            await client.send(new PutObjectCommand({
                Bucket: bucket,
                Key: objectKey,
                Body: body,
                CacheControl: cacheControl,
                ContentType: CONTENT_TYPES[ext] ?? DEFAULT_CONTENT_TYPE,
            }));
        }
    };
    await Promise.all(Array.from({length: Math.min(UPLOAD_CONCURRENCY, uploads.length)}, worker));

    await client.send(new PutObjectCommand({
        Bucket: bucket,
        Key: markerKey,
        Body: `${buildId} ${new Date().toISOString()}\n`,
        ContentType: 'text/plain',
    }));
    console.log(`[static-assets] uploaded ${uploads.length} files to s3://${bucket}/${keyPrefix || ''} (build ${buildId})`);
    return {uploaded: uploads.length, skipped: false};
}
