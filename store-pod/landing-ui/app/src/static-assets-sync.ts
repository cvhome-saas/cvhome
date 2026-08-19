import {
    HeadObjectCommand,
    PutObjectCommand,
    S3Client,
} from "@aws-sdk/client-s3";

import { createReadStream } from "node:fs";

import {
    access,
    readFile,
    readdir,
    stat,
    writeFile,
} from "node:fs/promises";

import path from "node:path";


/**
 * ============================================================================
 * Configuration
 * ============================================================================
 */

const SYNC_ENABLED =
    process.env.STATIC_ASSETS_SYNC_ENABLED?.toLowerCase() === "true";

const S3_BUCKET =
    process.env.STATIC_ASSETS_S3_BUCKET;

const S3_PREFIX =
    normalizePath(
        process.env.STATIC_ASSETS_S3_PREFIX ?? "storefront",
    );

const BASE_URL =
    removeTrailingSlash(
        process.env.STATIC_ASSETS_BASE_URL ?? "",
    );

const AWS_REGION =
    process.env.AWS_REGION;

/**
 * Maximum number of simultaneous S3 uploads.
 *
 * Avoid:
 *
 *     Promise.all(5000 files)
 *
 * because that could create thousands of concurrent
 * requests/sockets.
 */
const UPLOAD_CONCURRENCY =
    parsePositiveInteger(
        process.env.STATIC_ASSETS_UPLOAD_CONCURRENCY,
        20,
    );


/**
 * ============================================================================
 * Directories
 * ============================================================================
 */

const ROOT_DIR = process.cwd();

const TEMPLATES_DIR =
    path.join(ROOT_DIR, "templates");


/**
 * ============================================================================
 * AWS
 * ============================================================================
 */

const s3 = new S3Client({
    region: AWS_REGION,
});


/**
 * ============================================================================
 * Models
 * ============================================================================
 */

interface TemplateBuild {
    template: string;

    templateDir: string;

    nextDir: string;

    staticDir: string;

    buildId: string;
}


interface SyncResult {
    template: string;

    buildId: string;

    uploaded: boolean;

    rewritten: boolean;

    uploadedFiles: number;
}


/**
 * ============================================================================
 * Public entry point
 * ============================================================================
 */

export async function syncStaticAssets(): Promise<void> {

    if (!SYNC_ENABLED) {
        console.log(
            "[static-assets] synchronization disabled",
        );

        return;
    }

    validateConfiguration();

    console.log(
        "[static-assets] synchronization enabled",
    );

    console.log(
        `[static-assets] bucket=${S3_BUCKET}`,
    );

    console.log(
        `[static-assets] prefix=${S3_PREFIX}`,
    );

    console.log(
        `[static-assets] baseUrl=${BASE_URL}`,
    );

    console.log(
        `[static-assets] uploadConcurrency=${UPLOAD_CONCURRENCY}`,
    );

    const templates =
        await discoverTemplates();

    if (templates.length === 0) {
        console.warn(
            `[static-assets] no Next.js templates found under ${TEMPLATES_DIR}`,
        );

        return;
    }

    console.log(
        `[static-assets] discovered ${templates.length} templates`,
    );

    const results: SyncResult[] = [];

    /*
     * Templates are intentionally synchronized sequentially.
     *
     * File uploads inside each template are parallelized.
     *
     * This prevents:
     *
     * 10 templates × 20 uploads
     *
     * suddenly creating 200 concurrent S3 requests.
     */
    for (const template of templates) {
        const result =
            await synchronizeTemplate(template);

        results.push(result);
    }

    logSummary(results);
}


/**
 * ============================================================================
 * Configuration validation
 * ============================================================================
 */

function validateConfiguration(): void {

    const missing: string[] = [];

    if (!S3_BUCKET) {
        missing.push(
            "STATIC_ASSETS_S3_BUCKET",
        );
    }

    if (!BASE_URL) {
        missing.push(
            "STATIC_ASSETS_BASE_URL",
        );
    }

    if (!AWS_REGION) {
        missing.push(
            "AWS_REGION",
        );
    }

    if (missing.length > 0) {
        throw new Error(
            "Static asset synchronization is enabled but " +
            `required environment variables are missing: ${missing.join(", ")}`,
        );
    }
}


/**
 * ============================================================================
 * Template discovery
 * ============================================================================
 */

async function discoverTemplates(): Promise<TemplateBuild[]> {

    const entries =
        await readdir(
            TEMPLATES_DIR,
            {
                withFileTypes: true,
            },
        );

    const templates: TemplateBuild[] = [];

    for (const entry of entries) {

        if (!entry.isDirectory()) {
            continue;
        }

        const template =
            entry.name;

        const templateDir =
            path.join(
                TEMPLATES_DIR,
                template,
            );

        const nextDir =
            path.join(
                templateDir,
                ".next",
            );

        const staticDir =
            path.join(
                nextDir,
                "static",
            );

        const buildIdFile =
            path.join(
                nextDir,
                "BUILD_ID",
            );


        if (!(await exists(buildIdFile))) {

            console.debug(
                `[static-assets] skipping template=${template}: BUILD_ID not found`,
            );

            continue;
        }


        if (!(await exists(staticDir))) {

            console.debug(
                `[static-assets] skipping template=${template}: .next/static not found`,
            );

            continue;
        }


        const buildId =
            (
                await readFile(
                    buildIdFile,
                    "utf8",
                )
            ).trim();


        if (!buildId) {
            throw new Error(
                `Template '${template}' has an empty BUILD_ID`,
            );
        }


        templates.push({
            template,
            templateDir,
            nextDir,
            staticDir,
            buildId,
        });
    }

    return templates;
}


/**
 * ============================================================================
 * Template synchronization
 * ============================================================================
 */

async function synchronizeTemplate(
    template: TemplateBuild,
): Promise<SyncResult> {

    console.log(
        `[static-assets] processing template=${template.template} buildId=${template.buildId}`,
    );


    /*
     * IMPORTANT:
     *
     * Every container must rewrite its own local Next.js server output,
     * regardless of whether S3 already contains this build.
     *
     * The S3 marker only tells us that static assets have already been
     * uploaded.
     */

    const rewritten =
        await rewriteTemplateUrls(template);


    const markerKey =
        getMarkerKey(template);


    const alreadyUploaded =
        await objectExists(markerKey);


    if (alreadyUploaded) {

        console.log(
            `[static-assets] template=${template.template} build=${template.buildId} already synchronized`,
        );

        return {
            template: template.template,
            buildId: template.buildId,
            uploaded: false,
            rewritten,
            uploadedFiles: 0,
        };
    }


    console.log(
        `[static-assets] uploading template=${template.template} build=${template.buildId}`,
    );


    /*
     * Upload ORIGINAL .next/static files.
     *
     * rewriteTemplateUrls() explicitly excludes this directory.
     */

    const uploadedFiles =
        await uploadStaticDirectory(template);


    /*
     * IMPORTANT:
     *
     * Marker MUST be written last.
     *
     * If the process crashes halfway through uploading, the marker
     * doesn't exist.
     *
     * Another container will therefore retry the upload.
     */

    await writeCompletionMarker(template);


    console.log(
        `[static-assets] synchronized template=${template.template} build=${template.buildId}`,
    );


    return {
        template: template.template,
        buildId: template.buildId,
        uploaded: true,
        rewritten,
        uploadedFiles,
    };
}


/**
 * ============================================================================
 * Runtime Next.js URL rewriting
 * ============================================================================
 */

async function rewriteTemplateUrls(
    template: TemplateBuild,
): Promise<boolean> {

    /*
     * Example:
     *
     * BASE_URL
     * https://d123.cloudfront.net
     *
     * S3_PREFIX
     * storefront
     *
     * template
     * basis
     *
     * BUILD_ID
     * abc123
     *
     *
     * Result:
     *
     * https://d123.cloudfront.net/storefront/basis/abc123
     */

    const assetBaseUrl =
        buildAssetBaseUrl(template);


    console.log(
        `[static-assets] rewriting template=${template.template}`,
    );

    console.log(
        `[static-assets] assetBaseUrl=${assetBaseUrl}`,
    );


    /*
     * IMPORTANT:
     *
     * Do NOT rewrite .next/static.
     *
     * Those files are immutable Next.js build artifacts and are
     * uploaded to S3 unchanged.
     *
     * We only rewrite server/manifests/runtime metadata outside
     * .next/static.
     */

    const files =
        await collectFiles(
            template.nextDir,
            template.staticDir,
        );


    let changedFiles = 0;


    for (const file of files) {

        if (!isRewritableFile(file)) {
            continue;
        }


        const changed =
            await rewriteFile(
                file,
                assetBaseUrl,
            );


        if (changed) {
            changedFiles++;
        }
    }


    console.log(
        `[static-assets] template=${template.template} rewrittenFiles=${changedFiles}`,
    );


    return changedFiles > 0;
}


/**
 * Rewrites ONLY:
 *
 *     /_next/static/
 *
 * We intentionally DO NOT rewrite:
 *
 *     /_next/image
 *     /_next/data
 *     /_next/...
 *
 * because those can represent dynamic Next.js endpoints that
 * must continue going to the Next.js server.
 */
async function rewriteFile(
    file: string,
    assetBaseUrl: string,
): Promise<boolean> {

    let content: string;


    try {
        content =
            await readFile(
                file,
                "utf8",
            );
    } catch {
        return false;
    }


    const source =
        "/_next/static/";


    const replacement =
        `${assetBaseUrl}/_next/static/`;


    /*
     * Make rewriting idempotent.
     *
     * If this function somehow executes twice in the same process
     * or against an already rewritten file, the generated URL won't
     * be rewritten again because it no longer contains the original
     * /_next/static/ sequence in isolation.
     */

    const rewritten =
        content.replaceAll(
            source,
            replacement,
        );


    if (rewritten === content) {
        return false;
    }


    await writeFile(
        file,
        rewritten,
        "utf8",
    );


    return true;
}


function isRewritableFile(
    file: string,
): boolean {

    const extension =
        path.extname(file)
            .toLowerCase();


    return [
        ".js",
        ".json",
        ".html",
        ".css",
        ".txt",
    ].includes(extension);
}


/**
 * ============================================================================
 * S3 upload
 * ============================================================================
 */

async function uploadStaticDirectory(
    template: TemplateBuild,
): Promise<number> {

    const files =
        await collectFiles(
            template.staticDir,
        );


    console.log(
        `[static-assets] template=${template.template} files=${files.length}`,
    );


    if (files.length === 0) {
        console.warn(
            `[static-assets] template=${template.template} has no static files`,
        );

        return 0;
    }


    let uploaded = 0;


    /*
     * Upload in bounded batches.
     *
     * Example:
     *
     * 1000 files
     * concurrency = 20
     *
     * => max 20 simultaneous S3 PUT requests.
     */

    for (
        let index = 0;
        index < files.length;
        index += UPLOAD_CONCURRENCY
    ) {

        const batch =
            files.slice(
                index,
                index + UPLOAD_CONCURRENCY,
            );


        await Promise.all(
            batch.map(
                async file => {

                    await uploadStaticFile(
                        template,
                        file,
                    );

                    uploaded++;
                },
            ),
        );


        console.log(
            `[static-assets] template=${template.template} uploaded=${uploaded}/${files.length}`,
        );
    }


    return uploaded;
}


async function uploadStaticFile(
    template: TemplateBuild,
    file: string,
): Promise<void> {

    const relativePath =
        path
            .relative(
                template.staticDir,
                file,
            )
            .split(path.sep)
            .join("/");


    /*
     * IMPORTANT:
     *
     * BUILD_ID is part of the S3 namespace.
     *
     * Example:
     *
     * storefront/
     *   basis/
     *     abc123/
     *       _next/
     *         static/
     *           chunks/...
     */

    const key =
        buildS3StaticKey(
            template,
            relativePath,
        );


    const metadata =
        await stat(file);


    await s3.send(
        new PutObjectCommand({
            Bucket: S3_BUCKET!,
            Key: key,

            Body:
                createReadStream(file),

            ContentLength:
            metadata.size,

            ContentType:
                getContentType(file),

            /*
             * Safe because BUILD_ID makes the URL immutable.
             *
             * A new deployment gets another URL:
             *
             * basis/AAA/...
             * basis/BBB/...
             */

            CacheControl:
                "public, max-age=31536000, immutable",
        }),
    );
}


/**
 * ============================================================================
 * Completion marker
 * ============================================================================
 */

async function writeCompletionMarker(
    template: TemplateBuild,
): Promise<void> {

    const key =
        getMarkerKey(template);


    const marker = {
        template:
        template.template,

        buildId:
        template.buildId,

        synchronizedAt:
            new Date().toISOString(),

        assetBaseUrl:
            buildAssetBaseUrl(template),
    };


    await s3.send(
        new PutObjectCommand({
            Bucket:
                S3_BUCKET!,

            Key:
            key,

            Body:
                JSON.stringify(
                    marker,
                    null,
                    2,
                ),

            ContentType:
                "application/json",

            /*
             * Marker is operational metadata.
             * Don't cache it aggressively.
             */

            CacheControl:
                "no-store",
        }),
    );
}


/**
 * Marker location:
 *
 * storefront/
 *   basis/
 *     .builds/
 *       abc123.complete.json
 */
function getMarkerKey(
    template: TemplateBuild,
): string {

    return joinS3Path(
        S3_PREFIX,
        template.template,
        ".builds",
        `${template.buildId}.complete.json`,
    );
}


/**
 * ============================================================================
 * S3 path generation
 * ============================================================================
 */

function buildS3StaticKey(
    template: TemplateBuild,
    relativePath: string,
): string {

    return joinS3Path(
        S3_PREFIX,
        template.template,
        template.buildId,
        "_next",
        "static",
        relativePath,
    );
}


/**
 * CDN URL:
 *
 * https://cdn.example.com
 *      /storefront
 *      /basis
 *      /abc123
 */
function buildAssetBaseUrl(
    template: TemplateBuild,
): string {

    const pathPart =
        joinS3Path(
            S3_PREFIX,
            template.template,
            template.buildId,
        );


    return `${BASE_URL}/${pathPart}`;
}


/**
 * ============================================================================
 * S3 existence
 * ============================================================================
 */

async function objectExists(
    key: string,
): Promise<boolean> {

    try {

        await s3.send(
            new HeadObjectCommand({
                Bucket:
                    S3_BUCKET!,

                Key:
                key,
            }),
        );


        return true;

    } catch (error: unknown) {

        if (isNotFoundError(error)) {
            return false;
        }


        /*
         * Don't treat authorization/network errors as
         * "object doesn't exist".
         *
         * Otherwise we might start uploading when S3 itself
         * is unavailable or IAM is incorrectly configured.
         */

        throw error;
    }
}


function isNotFoundError(
    error: unknown,
): boolean {

    if (
        typeof error !== "object" ||
        error === null
    ) {
        return false;
    }


    const candidate =
        error as {
            name?: string;

            $metadata?: {
                httpStatusCode?: number;
            };
        };


    return (
        candidate.name === "NotFound" ||
        candidate.name === "NoSuchKey" ||
        candidate.$metadata?.httpStatusCode === 404
    );
}


/**
 * ============================================================================
 * Files
 * ============================================================================
 */

/**
 * Recursively collects files.
 *
 * excludedDirectory can be supplied when scanning .next so
 * .next/static is not modified.
 */
async function collectFiles(
    directory: string,
    excludedDirectory?: string,
): Promise<string[]> {

    const result: string[] = [];


    const entries =
        await readdir(
            directory,
            {
                withFileTypes: true,
            },
        );


    for (const entry of entries) {

        const absolutePath =
            path.join(
                directory,
                entry.name,
            );


        /*
         * Do not descend into the excluded directory.
         */

        if (
            excludedDirectory &&
            path.resolve(absolutePath) ===
            path.resolve(excludedDirectory)
        ) {
            continue;
        }


        if (entry.isDirectory()) {

            const nested =
                await collectFiles(
                    absolutePath,
                    excludedDirectory,
                );


            result.push(...nested);

        } else if (entry.isFile()) {

            result.push(
                absolutePath,
            );
        }
    }


    return result;
}


async function exists(
    file: string,
): Promise<boolean> {

    try {
        await access(file);

        return true;

    } catch {

        return false;
    }
}


/**
 * ============================================================================
 * Content types
 * ============================================================================
 */

function getContentType(
    file: string,
): string {

    switch (
        path.extname(file)
            .toLowerCase()
        ) {

        case ".js":
            return "application/javascript; charset=utf-8";

        case ".mjs":
            return "application/javascript; charset=utf-8";

        case ".css":
            return "text/css; charset=utf-8";

        case ".json":
            return "application/json; charset=utf-8";

        case ".map":
            return "application/json; charset=utf-8";

        case ".txt":
            return "text/plain; charset=utf-8";

        case ".svg":
            return "image/svg+xml";

        case ".png":
            return "image/png";

        case ".jpg":
        case ".jpeg":
            return "image/jpeg";

        case ".webp":
            return "image/webp";

        case ".avif":
            return "image/avif";

        case ".gif":
            return "image/gif";

        case ".ico":
            return "image/x-icon";

        case ".woff":
            return "font/woff";

        case ".woff2":
            return "font/woff2";

        case ".ttf":
            return "font/ttf";

        case ".otf":
            return "font/otf";

        default:
            return "application/octet-stream";
    }
}


/**
 * ============================================================================
 * Helpers
 * ============================================================================
 */

function joinS3Path(
    ...parts: string[]
): string {

    return parts
        .filter(Boolean)
        .map(part =>
            part
                .replace(/^\/+/, "")
                .replace(/\/+$/, ""),
        )
        .filter(Boolean)
        .join("/");
}


function normalizePath(
    value: string,
): string {

    return value
        .replace(/^\/+/, "")
        .replace(/\/+$/, "");
}


function removeTrailingSlash(
    value: string,
): string {

    return value.replace(
        /\/+$/,
        "",
    );
}


function parsePositiveInteger(
    value: string | undefined,
    defaultValue: number,
): number {

    if (!value) {
        return defaultValue;
    }


    const parsed =
        Number.parseInt(
            value,
            10,
        );


    if (
        !Number.isInteger(parsed) ||
        parsed <= 0
    ) {
        throw new Error(
            `Expected positive integer but received '${value}'`,
        );
    }


    return parsed;
}


/**
 * ============================================================================
 * Logging
 * ============================================================================
 */

function logSummary(
    results: SyncResult[],
): void {

    console.log(
        "[static-assets] synchronization completed",
    );


    for (const result of results) {

        console.log(
            [
                "[static-assets]",
                `template=${result.template}`,
                `build=${result.buildId}`,
                `uploaded=${result.uploaded}`,
                `uploadedFiles=${result.uploadedFiles}`,
                `rewritten=${result.rewritten}`,
            ].join(" "),
        );
    }
}