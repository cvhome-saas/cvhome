package com.asrevo.cvhome.s2s.jwt;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "spring.security.oauth2.resourceserver.jwt")
@Getter
@Setter
public class IssuerUriSetConfigrationProperties {

    /**
     * The realm this service falls back to when only the legacy {@link #issuerUriSet} is configured. Unnamed
     * realms confer no authority ceiling, which is the behaviour that existed before realms did.
     */
    public static final String LEGACY_REALM = "default";

    /**
     * Every identity server this service accepts, keyed by realm name — {@code uaa} for staff and services,
     * {@code cua} for shoppers. Prefer this over {@link #issuerUriSet}: it is what lets a resource server tell
     * a shopper token from a staff one, and cap what each may confer.
     */
    private Map<String, Realm> issuers = new LinkedHashMap<>();

    /**
     * The flat, realm-blind trust list this replaced. Still honoured so the two forms can coexist during a
     * rollout, but it grants every issuer identical authority — which is exactly the property realms exist to
     * remove.
     *
     * @deprecated configure {@link #issuers} instead.
     */
    @Deprecated(since = "1.0.16")
    private Set<String> issuerUriSet;

    /** The configured realms, or a single unrestricted one synthesised from {@link #issuerUriSet}. */
    public IssuerRegistry toRegistry() {
        List<IssuerRealm> realms = new ArrayList<>();
        this.issuers.forEach((name, realm) -> realms
                .add(new IssuerRealm(name, Set.copyOf(realm.getUris()), realm.getJwkSetUri(), realm.getGrants())));
        if (realms.isEmpty() && this.issuerUriSet != null && !this.issuerUriSet.isEmpty()) {
            realms.add(new IssuerRealm(LEGACY_REALM, this.issuerUriSet, null, Set.of()));
        }
        return new IssuerRegistry(realms);
    }

    @Getter
    @Setter
    public static class Realm {

        /**
         * Every {@code iss} value this realm may present. A list rather than one URI because the same server is
         * reachable at several equivalent forms — an operator-entered default port or not, a stack's shifted
         * port, a path prefix. Entries are matched normalized, so listing both port forms is belt and braces.
         */
        private List<String> uris = new ArrayList<>();

        /**
         * Where to fetch this realm's signing keys. Configuring it skips OIDC discovery, which means no blocking
         * network call on the first authenticated request, and no dependency on the authorization server being
         * up before this one can verify anything. Leave unset to discover from {@link #uris}.
         */
        private String jwkSetUri;

        /**
         * The authorities this realm may confer, as an allow-list. Empty means unrestricted, which is right for
         * the staff realm whose clients carry arbitrary scopes. Setting {@code [ROLE_CUSTOMER]} on the shopper
         * realm is what makes a cua token structurally incapable of granting staff authority.
         */
        private Set<String> grants = Set.of();

    }

}
