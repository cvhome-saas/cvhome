FROM quay.io/keycloak/keycloak:25.0

COPY keycloak/imports /opt/keycloak/data/import
COPY keycloak/providers /opt/keycloak/providers
COPY keycloak/themes/json-mail-theme /opt/keycloak/themes/json-mail-theme


EXPOSE 9999
EXPOSE 9000
ENTRYPOINT ["/opt/keycloak/bin/kc.sh","--verbose","start","--proxy=edge","--hostname-strict=false","--import-realm","--health-enabled=true"]
