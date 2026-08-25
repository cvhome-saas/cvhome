# landing-ui: optional CDN for static assets (S3 sync on server start + CDN URL rewrite)

## Context

The storefront (`store-pod/landing-ui/storefront`, Next 16, `output: 'standalone'`, Turbopack build) serves its
own `/_next/static/**` (2.8 MB, 82 files) and `public/` from the Node container behind spg/Caddy. Goal: an
**optional** mode where, when the Next server starts, the build's static assets are pushed to S3 (only if that
build isn't there yet) and every asset reference in the rendered HTML/JS points at a CDN in front of the bucket:

```
STATIC_ASSETS_SYNC_ENABLED=true
STATIC_ASSETS_S3_BUCKET=my-storefront-assets
STATIC_ASSETS_S3_PREFIX=storefront
STATIC_ASSETS_BASE_URL=https://dxxx.cloudfront.net
AWS_REGION=eu-central-1
```

Everything in JS, run at Next server start (the user's two clarifications). Off by default → identical to today.

### Why not just set `assetPrefix` at runtime
Verified empirically against the current standalone build (patched `required-server-files.json` config →
`startServer`): CSS, fonts and entry scripts get the prefix, **but** Turbopack bakes absolute
`"/_next/static/chunks/…"` paths into `.next/server/app/**/page_client-reference-manifest.js` / `*.rsc` and
bakes `"/_next/"` as the chunk base inside `.next/static/chunks/turbopack-*.js`. Those stay origin-relative →
"rewrite all references" is impossible at runtime alone. So the prefix must be in the build, and the runtime
value substituted in.

## Design

**Build-time sentinel, start-time substitution.** `next build` always uses
`assetPrefix = 'https://storefront-static.invalid'` (RFC 2606 `.invalid`, a valid URL so Next accepts it).
A JS entrypoint (`storefront/start.mjs`) replaces that sentinel in the build output before starting Next:
with `STATIC_ASSETS_BASE_URL` when sync is on and the upload (or "already uploaded" check) succeeded, with
`''` (→ plain `/_next/...`) otherwise. Then it starts Next exactly as the generated `server.js` does.

Start sequence (`start.mjs`, ESM, no build step, ~150 lines + 2 helper modules):
1. Resolve dirs: `.next/` next to the entrypoint (works for standalone and plain `next build` layouts), read
   `.next/BUILD_ID`, `.next/required-server-files.json` (`config`), and `.next/static-assets.state.json`
   (what the files currently contain: `{prefix}` — absent ⇒ sentinel untouched).
2. If `STATIC_ASSETS_SYNC_ENABLED=true` → `syncToS3()`:
   - marker key `${PREFIX}/_builds/${BUILD_ID}` — `HeadObject`; exists ⇒ "already synced, skip upload"
     (the "S3 version hash ≠ our build hash" check; per-build markers, never delete old builds — rolling
     deploys keep serving old tasks whose chunks are content-hashed anyway).
   - else upload `.next/static/**` → `${PREFIX}/_next/static/**` (`Cache-Control: public, max-age=31536000,
     immutable`) and `public/**` → `${PREFIX}/**` (`public, max-age=3600`), content-type by extension
     (js/css/json/map/txt/woff2/woff/ttf/png/jpg/webp/svg/ico), concurrency 16, **after** rewriting the
     sentinel → base URL inside text files (the turbopack runtime chunk carries it). Then `PutObject` the marker
     (body = BUILD_ID + timestamp). Log one summary line (`uploaded N files / skipped (marker present)`).
   - `@aws-sdk/client-s3` with the default credential chain (task role on Fargate, env keys / MinIO locally);
     optional `STATIC_ASSETS_S3_ENDPOINT` + `STATIC_ASSETS_S3_FORCE_PATH_STYLE=true` for local MinIO QA.
   - Any failure ⇒ `console.error`, fall back to origin serving (prefix `''`) — a CDN hiccup must never take
     the storefront down.
3. `applyAssetPrefix(to)` — walk `.next/server/**`, `.next/static/**`, `.next/*.json` and replace
   `state.prefix ?? SENTINEL` → `to` in text files only (extensions: js, mjs, json, rsc, html, css, txt, map,
   body, meta); write `.next/static-assets.state.json = {prefix: to, buildId}`. Idempotent; if the state
   prefix is `''` and a non-empty prefix is now wanted, warn (cannot re-prefix relative paths; fresh
   container needed) and continue.
4. Patch `config.assetPrefix = to` on the loaded config, `process.env.__NEXT_PRIVATE_STANDALONE_CONFIG =
   JSON.stringify(config)`, then `require('next')` + `startServer({dir, isDev:false, config, hostname, port,
   keepAliveTimeout})` — the same calls the generated `server.js` makes (`storefront/.next/standalone/storefront/server.js`).

Getting the SDK into the image without `npm install` at image build (Dockerfile principle):
`@aws-sdk/client-s3` in `storefront/package.json` + `serverExternalPackages: ['@aws-sdk/client-s3']` +
`src/instrumentation.ts` whose `register()` does a guarded `await import('@aws-sdk/client-s3')` that never
runs (env-gated) — Next's file tracing then copies the package into `.next/standalone/node_modules`, where
`start.mjs`'s `createRequire` finds it. Verify after build: `ls .next/standalone/node_modules/@aws-sdk/client-s3`.
Fallback if tracing doesn't pick it up: `outputFileTracingIncludes` globs for `@aws-sdk/**`, `@smithy/**`,
`@aws-crypto/**`, `tslib`, `fast-xml-parser`, `strnum`, `bowser`, `uuid`.

## Files

- `storefront/next.config.ts` — `assetPrefix: STATIC_ASSETS_SENTINEL` for `PHASE_PRODUCTION_BUILD` only
  (dev untouched); `serverExternalPackages`. Import the sentinel from `storefront/scripts/static-assets/constants.mjs`.
- `storefront/scripts/static-assets/constants.mjs` — `SENTINEL`, env names, `TEXT_EXTENSIONS`, `CONTENT_TYPES`.
- `storefront/scripts/static-assets/apply-prefix.mjs` — `applyAssetPrefix(nextDir, to)` + `readState`.
- `storefront/scripts/static-assets/sync-s3.mjs` — `syncStaticAssets({nextDir, publicDir, buildId, env})`.
- `storefront/start.mjs` — the entrypoint (steps 1–4). Also `npm run start` → `node start.mjs` so a local
  `next build && npm start` gets the sentinel removed too.
- `storefront/src/instrumentation.ts` — trace hook for the SDK (guarded import).
- `storefront/package.json` — dep `@aws-sdk/client-s3`; `start` script.
- `Dockerfile` — `COPY storefront/start.mjs storefront/scripts/static-assets ./storefront/…`;
  `CMD ["storefront/start.mjs"]`; document the env block in a comment. `docker.sh`: pass the STATIC_ASSETS_*
  env through.
- Docs: `.agents/skills/project-structure/references/new-landing-ui-template.md` (request flow + env table),
  `themes/README.md` not needed. Add a short "CDN" section to `store-pod/landing-ui/storefront/README.md` if
  one exists, else the guide.

## Out of scope / noted
- `public/` references in app code (`/placeholder.png` via `PLACEHOLDER_IMAGE` in `libs/services`) stay
  origin-relative — Next's `assetPrefix` only covers `/_next/`. The files are still uploaded under
  `${PREFIX}/` so a CDN behaviour can route them; switching the reference is a one-line follow-up.
- No CloudFront/bucket provisioning here (infra side); bucket must allow the task role `s3:PutObject`,
  `s3:GetObject`/`HeadObject` on the prefix; CDN origin = bucket, origin path = `/${PREFIX}`.

## Verification
1. `cd store-pod/landing-ui && npm run build` → sentinel present: `grep -rl storefront-static.invalid storefront/.next/server | wc -l` > 0; `ls storefront/.next/standalone/node_modules/@aws-sdk/client-s3`.
2. Sync off: `node storefront/start.mjs` (from the standalone dir copy, as the Dockerfile lays it out) → HTML has only `/_next/static/...` refs, no sentinel anywhere in `.next`; second start is a no-op.
3. Sync on against local MinIO (`docker-compose-lcl`): `STATIC_ASSETS_SYNC_ENABLED=true STATIC_ASSETS_S3_BUCKET=<bucket> STATIC_ASSETS_S3_PREFIX=storefront STATIC_ASSETS_BASE_URL=http://localhost:9000/<bucket>/storefront STATIC_ASSETS_S3_ENDPOINT=http://localhost:9000 STATIC_ASSETS_S3_FORCE_PATH_STYLE=true AWS_REGION=eu-central-1 AWS_ACCESS_KEY_ID=minioadmin AWS_SECRET_ACCESS_KEY=minioadmin node start.mjs` → log "uploaded N files"; HTML: every `_next/static` ref (script/link/font/`__next_f` manifest chunks) starts with the base URL, `grep -c '"/_next/static' page.html` = 0; the turbopack runtime chunk fetched from MinIO contains the base URL; page loads and hydrates in the browser (chrome-devtools: no 404s in network). Restart → "already synced" and no uploads.
4. Failure path: wrong bucket → error logged, server still serves from origin.
5. `npm run lint && npm run typecheck`; `docker.sh` builds and runs the image with `CMD start.mjs`.
