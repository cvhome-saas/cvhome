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
public class IssuerRealmProperties {

    /**
     * Every identity server this service accepts, keyed by realm name — {@code uaa} for staff and services,
     * {@code cua} for shoppers.
     */
    private Map<String, Realm> issuers = new LinkedHashMap<>();

    public IssuerRegistry toRegistry() {
        List<IssuerRealm> realms = new ArrayList<>();
        this.issuers.forEach((name, realm) -> realms
                .add(new IssuerRealm(name, Set.copyOf(realm.getUris()), realm.getJwkSetUri(), realm.getGrants())));
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
