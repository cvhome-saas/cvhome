###############################################
# Stage 1 — Build dependencies
###############################################
FROM node:20-alpine AS builder
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

# Create working directory
RUN mkdir -p /deployment
WORKDIR /deployment

# Initialize package.json
RUN npm init -y

# Install required dependencies
RUN npm i --omit=dev \
  express@5.1.0 \
  next@16.0.0 \
  react@19.2.0 \
  react-dom@19.2.0 \
  next-intl@4.4.0


###############################################
# Stage 2 — Distroless Runtime
###############################################
FROM gcr.io/distroless/nodejs20

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

# Copy the node_modules and package structure from builder
COPY --from=builder /deployment /deployment

# Copy local application directories
COPY app /deployment/app
COPY libs /deployment/libs
COPY templates /deployment/templates

WORKDIR /deployment

EXPOSE 8110

CMD ["app/dist/server.js"]