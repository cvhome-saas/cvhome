FROM quay.io/keycloak/keycloak:24.0

COPY keycloak/imports /opt/keycloak/data/import
COPY keycloak/providers /opt/keycloak/providers
EXPOSE 9999
ENTRYPOINT ["/opt/keycloak/bin/kc.sh","--verbose","start","--proxy=edge","--hostname-strict=false","--import-realm"]
