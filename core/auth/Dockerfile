FROM quay.io/keycloak/keycloak:21.0

ENV KC_HTTP_PORT=9999

ENV KC_HTTP_ENABLED=true
ENV KC_HOSTNAME_STRICT_HTTPS=false

ENV KEYCLOAK_ADMIN=admin
ENV KEYCLOAK_ADMIN_PASSWORD=admin

COPY keycloak/imports /opt/keycloak/data/import
COPY keycloak/providers /opt/keycloak/providers
EXPOSE 9999
ENTRYPOINT ["/opt/keycloak/bin/kc.sh","--verbose","start","--proxy=edge","--hostname-strict=false","--import-realm"]
