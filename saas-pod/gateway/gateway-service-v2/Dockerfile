FROM caddy:2.7.6
FROM caddy:2.7.6-builder AS builder

RUN xcaddy build \
    --with github.com/caddyserver/nginx-adapter \
    --with github.com/hairyhenderson/caddy-teapot-module@v0.0.3-0 \
    --with github.com/ss098/certmagic-s3

FROM caddy:2.7.6

COPY --from=builder /usr/bin/caddy /usr/bin/caddy
COPY Caddyfile /etc/caddy/Caddyfile
ENV HOST 0.0.0.0
EXPOSE 80
EXPOSE 443