###############################################
# Stage 1 — Build dependencies
###############################################
FROM node:20-slim AS builder
WORKDIR /build

RUN npm init -y

RUN npm i --omit=dev \
  express@5.1.0 \
  next@16.0.0 \
  react@19.2.0 \
  react-dom@19.2.0 \
  next-intl@4.4.0 \
  typescript@5.9.3


RUN apt-get update && apt-get install -y findutils && \
    find node_modules -type d \( \
        -name "test" -o \
        -name "tests" -o \
        -name "__tests__" -o \
        -name "bench" -o \
        -name "benchmark" -o \
        -name "coverage" \
    \) -prune -exec rm -rf '{}' + && \
    find node_modules -type f \( \
        -name "*.coffee" -o \
        -name "*.markdown" -o \
        -name "*.log" -o \
        -name "*.orig" \
    \) -delete


###############################################
# Stage 2 — Distroless Runtime
###############################################
FROM gcr.io/distroless/nodejs20

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1


COPY --from=builder /build/node_modules ./node_modules
COPY app ./app
COPY libs ./libs
COPY templates ./templates

EXPOSE 8110

CMD ["./app/dist/server.js"]