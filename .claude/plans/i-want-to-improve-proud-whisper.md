# landing-ui: optional CDN for static assets (S3 sync on server start + CDN URL rewrite)

> Adopted from the pre-existing plan `.claude/plans/r-store-pod-landing-ui-themes-we-alwyas-eager-key.md`,
> which covers this exact request (same env vars) and includes an empirically verified design constraint.

## Context

The storefront (`store-pod/landing-ui/storefront`, Next 16, `output: 'standalone'`, Turbopack build) serves its
own `/_next/static/**` (~2.8 MB, 82 files) and `public/` from the Node container behind spg/Caddy. Goal: an
**optional** mode where, when the Next server starts in Docker, the build's static assets are pushed to S3
(only if that build isn't there yet — S3-stored build hash vs current BUILD_ID) and every asset reference
points at a CDN in front of the bucket:

```
STATIC_ASSETS_SYNC_ENABLED=true
STATIC_ASSETS_S3_BUCKET=my-storefront-assets
STATIC_ASSETS_S3_PREFIX=storefront
STATIC_ASSETS_BASE_URL=https://dxxx.cloudfront.net
AWS_REGION=eu-central-1
```

Off by default → behavior identical to today (Next serves everything itself on :8110).

### Why not just set `assetPrefix` at runtime
Verified empirically against the current standalone build: CSS, fonts and entry scripts get the prefix,
**but** Turbopack bakes absolute `"/_next/static/chunks/…"` paths into
`.next/server/app/**/page_client-reference-manifest.js` / `*.rsc` and bakes `"/_next/"` as the chunk base
inside `.next/static/chunks/turbopack-*.js`. Those stay origin-relative → "rewrite all references" is
impossible at runtime alone. So the prefix must be in the build, and the runtime value substituted in.

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
   keepAliveTimeout})` — the same calls the generated `server.js` makes.

Getting the SDK into the image without `npm install` at image build (Dockerfile principle):
`@aws-sdk/client-s3` in `storefront/package.json` + `serverExternalPackages: ['@aws-sdk/client-s3']` +
a guarded `await import('@aws-sdk/client-s3')` in the **existing** `storefront/instrumentation.ts`
(env-gated, never actually runs) — Next's file tracing then copies the package into
`.next/standalone/node_modules`, where `start.mjs`'s `createRequire` finds it. Verify after build:
`ls .next/standalone/node_modules/@aws-sdk/client-s3`. Fallback if tracing doesn't pick it up:
`outputFileTracingIncludes` globs for `@aws-sdk/**`, `@smithy/**`, `@aws-crypto/**`, `tslib`,
`fast-xml-parser`, `strnum`, `bowser`, `uuid`.

## Backward compatibility: sync off / env absent ⇒ exactly today's behavior

When `STATIC_ASSETS_SYNC_ENABLED` is unset, empty, or anything other than `true`, `start.mjs` skips S3
entirely and substitutes the sentinel with `''` before boot — the served HTML/JS contains plain
`/_next/static/...` URLs and Next serves them itself on :8110, byte-for-byte the same as today. No AWS
SDK is loaded, no network calls are made, no credentials are needed. Concretely per start path:

- **Docker image** (`docker.sh`, gradle `bootBuildImage`, compose/ECS): `CMD ["storefront/start.mjs"]`
  replaces `CMD ["storefront/server.js"]`; with sync off, start.mjs strips the sentinel then calls the same
  `startServer(...)` the generated `server.js` calls — same port, hostname, and serving behavior.
- **Local dev** (`npm run dev` via `run-lcl.sh`): the sentinel `assetPrefix` is applied only under
  `PHASE_PRODUCTION_BUILD` — dev config is untouched, dev works exactly as now.
- **Local prod run**: `npm run start` becomes `node start.mjs`, so `next build && npm start` also gets the
  sentinel stripped. The one path that would break is bypassing both and running `node server.js` /
  `next start` directly against a fresh production build (sentinel left in place) — not used by any repo
  script; noted in the Dockerfile comment.
- The substitution is idempotent and recorded in `.next/static-assets.state.json`, so restarts with sync
  off are no-ops.

This "sync off ⇒ unchanged" property is verification step 2 below and will be tested explicitly (both
`node start.mjs` locally and the built Docker image with no STATIC_ASSETS_* env) before anything else.

## One image, env at deploy time only

No `STATIC_ASSETS_*` variable is read at build time. The build is environment-agnostic: it always bakes the
same fixed sentinel (`https://storefront-static.invalid`) regardless of env, so the produced image is
identical whether or not CDN will be used and for every target environment. All five env vars are read by
`start.mjs` at container start:

- The **same image** deployed to dev with `STATIC_ASSETS_BASE_URL=https://dev.cdn.com` and to prod with
  `https://prod.cdn.com` (different bucket/region too) works: every container starts from the image layers,
  which still contain the untouched sentinel, and substitutes its own environment's URL on first boot.
- The S3 upload also happens per environment at start: files are sentinel-rewritten with that environment's
  base URL and pushed to that environment's bucket/prefix, marker-keyed by BUILD_ID — so dev and prod
  buckets each get a correct copy from the same image.
- Changing the URL for an existing environment is a redeploy (new container ⇒ fresh sentinel), not a
  rebuild. Only re-prefixing *inside one long-lived container that already substituted a different value*
  needs the container restarted from the image (the warn case in step 3); normal orchestrators (ECS/k8s/
  `docker run`) always start fresh from the image, so this never occurs in practice.

Verification will include: build once, then boot the same standalone output twice with two different
`STATIC_ASSETS_BASE_URL` values (fresh copy each time, as a container would) and confirm each serves its
own URL.

## Files

- `storefront/next.config.ts` — `assetPrefix: STATIC_ASSETS_SENTINEL` for `PHASE_PRODUCTION_BUILD` only
  (dev untouched); `serverExternalPackages`. Import the sentinel from `storefront/scripts/static-assets/constants.mjs`.
- `storefront/scripts/static-assets/constants.mjs` — `SENTINEL`, env names, `TEXT_EXTENSIONS`, `CONTENT_TYPES`.
- `storefront/scripts/static-assets/apply-prefix.mjs` — `applyAssetPrefix(nextDir, to)` + `readState`.
- `storefront/scripts/static-assets/sync-s3.mjs` — `syncStaticAssets({nextDir, publicDir, buildId, env})`.
- `storefront/start.mjs` — the entrypoint (steps 1–4). Also `npm run start` → `node start.mjs` so a local
  `next build && npm start` gets the sentinel removed too.
- `storefront/instrumentation.ts` — add the guarded SDK trace-hook import (file already exists; holds OTEL setup).
- `storefront/package.json` — dep `@aws-sdk/client-s3`; `start` script.
- `Dockerfile` — `COPY storefront/start.mjs …` and `COPY storefront/scripts/static-assets …`;
  `CMD ["storefront/start.mjs"]`; document the env block in a comment. `docker.sh`: pass the STATIC_ASSETS_*
  env through.
- Docs: `.agents/skills/project-structure/references/new-landing-ui-template.md` (request flow + env table).

## Out of scope / noted
- `public/` references in app code (`/placeholder.png` via `PLACEHOLDER_IMAGE` in `libs/services`) stay
  origin-relative — Next's `assetPrefix` only covers `/_next/`. The files are still uploaded under
  `${PREFIX}/` so a CDN behaviour can route them; switching the reference is a one-line follow-up.
- No CloudFront/bucket provisioning here (infra side); bucket must allow the task role `s3:PutObject`,
  `s3:GetObject`/`HeadObject` on the prefix; CDN origin = bucket, origin path = `/${PREFIX}`.

## Verification
1. `cd store-pod/landing-ui && npm run build` → sentinel present: `grep -rl storefront-static.invalid storefront/.next/server | wc -l` > 0; `ls storefront/.next/standalone/node_modules/@aws-sdk/client-s3`.
2. Sync off: `node storefront/start.mjs` (from the standalone dir copy, as the Dockerfile lays it out) → HTML has only `/_next/static/...` refs, no sentinel anywhere in `.next`; second start is a no-op.
3. Sync on against local MinIO (`docker-compose-lcl`): `STATIC_ASSETS_SYNC_ENABLED=true STATIC_ASSETS_S3_BUCKET=<bucket> STATIC_ASSETS_S3_PREFIX=storefront STATIC_ASSETS_BASE_URL=http://localhost:9000/<bucket>/storefront STATIC_ASSETS_S3_ENDPOINT=http://localhost:9000 STATIC_ASSETS_S3_FORCE_PATH_STYLE=true AWS_REGION=eu-central-1 AWS_ACCESS_KEY_ID=minioadmin AWS_SECRET_ACCESS_KEY=minioadmin node start.mjs` → log "uploaded N files"; HTML: every `_next/static` ref starts with the base URL, `grep -c '"/_next/static' page.html` = 0; the turbopack runtime chunk fetched from MinIO contains the base URL; page loads and hydrates (no 404s in network). Restart → "already synced", no uploads.
4. Failure path: wrong bucket → error logged, server still serves from origin.
5. `npm run lint && npm run typecheck`; `docker.sh` builds and runs the image with `CMD start.mjs`.
