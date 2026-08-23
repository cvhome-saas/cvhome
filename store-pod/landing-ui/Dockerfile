###############################################
# Build happens on the host / CI (see docker.sh, build.gradle): `npm ci && npm run build` produces
# storefront/.next/standalone — a self-contained server with only the traced node_modules.
# This image just copies that output. No npm install at image-build time.
###############################################
FROM public.ecr.aws/b2i4h4k9/nodejs20:latest

ENV NODE_ENV=production \
    PORT=8110 \
    HOSTNAME=0.0.0.0

# standalone output is rooted at the monorepo (outputFileTracingRoot = landing-ui/)
COPY storefront/.next/standalone ./
COPY storefront/.next/static ./storefront/.next/static
COPY storefront/public ./storefront/public

EXPOSE 8110

CMD ["storefront/server.js"]
