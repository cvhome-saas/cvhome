###############################################
# Stage 1 — Build dependencies
###############################################
FROM node:20-alpine AS builder
WORKDIR /build
COPY node_modules ./node_modules
RUN apk add --no-cache findutils && \
    find node_modules -type d -name "test" -prune -exec rm -rf '{}' + && \
    find node_modules -type d -name "tests" -prune -exec rm -rf '{}' + && \
    find node_modules -type d -name "__tests__" -prune -exec rm -rf '{}' + && \
    find node_modules -type d -name "example" -prune -exec rm -rf '{}' + && \
    find node_modules -type d -name "examples" -prune -exec rm -rf '{}' + && \
    find node_modules -type d -name "docs" -prune -exec rm -rf '{}' + && \
    find node_modules -type d -name "doc" -prune -exec rm -rf '{}' + && \
    find node_modules -type f -name "*.md" -delete && \
    find node_modules -type f -name "*.map" -delete && \
    find node_modules -type f -name "*.ts" -delete && \
    find node_modules -type f -name "*.tsx" -delete && \
    rm -rf node_modules/.bin && \
    find node_modules -type f -name "*.coffee" -delete && \
    find node_modules -type f -name "*.markdown" -delete


###############################################
# Stage 2 — Distroless Runtime
###############################################
FROM gcr.io/distroless/nodejs20

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

# Copy the node_modules and package structure from builder
COPY --from=builder /build/node_modules ./node_modules/

# Copy local application directories
COPY app /deployment/app
COPY libs /deployment/libs
COPY templates /deployment/templates

EXPOSE 8110

CMD ["app/dist/server.js"]