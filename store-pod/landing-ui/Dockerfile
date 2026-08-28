###############################################
# Build happens on the host / CI (see docker.sh, build.gradle): `npm ci && npm run build` produces
# storefront/.next/standalone — a self-contained server with only the traced node_modules.
# This image just copies that output. No npm install at image-build time.
#
# Static assets / CDN (optional, all env read at container start — the image is env-agnostic):
#   STATIC_ASSETS_SYNC_ENABLED=true          push .next/static + public to S3 on start (skipped
#                                            when the build is already in the bucket) and serve
#                                            every asset reference from the CDN base URL
#   STATIC_ASSETS_S3_BUCKET=my-bucket        target bucket
#   STATIC_ASSETS_S3_PREFIX=storefront       key prefix inside the bucket
#   STATIC_ASSETS_BASE_URL=https://d…net     CDN URL in front of the bucket/prefix
#   AWS_REGION=eu-central-1                  region (credentials via default chain / task role)
#   (local QA: STATIC_ASSETS_S3_ENDPOINT + STATIC_ASSETS_S3_FORCE_PATH_STYLE=true for MinIO)
# Unset / not "true" → identical to serving everything from this container.
# start.mjs substitutes the build-time sentinel assetPrefix, then starts Next exactly like the
# generated server.js. Do not run storefront/server.js directly — it would serve the sentinel.
###############################################
FROM public.ecr.aws/b2i4h4k9/nodejs20:latest

ENV NODE_ENV=production \
    PORT=8110 \
    HOSTNAME=0.0.0.0

# standalone output is rooted at the monorepo (outputFileTracingRoot = landing-ui/)
COPY storefront/.next/standalone ./
COPY storefront/.next/static ./storefront/.next/static
COPY storefront/public ./storefront/public
COPY storefront/start.mjs ./storefront/start.mjs
COPY storefront/scripts/static-assets ./storefront/scripts/static-assets

EXPOSE 8110

CMD ["storefront/start.mjs"]
