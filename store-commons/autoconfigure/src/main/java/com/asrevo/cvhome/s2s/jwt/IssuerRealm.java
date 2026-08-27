package com.asrevo.cvhome.s2s.jwt;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.asrevo.cvhome.s2s.utils.UrlNormalize;

/**
 * One identity server this service accepts tokens from — {@code uaa} for staff and services, {@code cua} for
 * shoppers — together with the authority it is allowed to confer.
 *
 * <p>
 * A realm is deliberately <em>not</em> a URL. The same server answers on several: an operator-entered pod
 * endpoint may or may not carry an explicit default port, a stack shifts ports, a scheme changes, cua sits
 * behind a {@code /cua} path prefix. {@link #issuerUris} therefore holds every form we know, all normalized,
 * and identity is the realm name. Topology may move underneath a realm without breaking authentication, which
 * is precisely what a flat set of issuer URIs could not express.
 * </p>
 *
 * <p>
 * {@link #grants} is the authority ceiling. An empty set means "unrestricted", which is what the staff realm
 * uses; naming it — {@code [ROLE_CUSTOMER]} for cua — makes the shopper boundary something the resource server
 * enforces rather than something it assumes. Before this existed, the only thing
 * stopping a cua token from carrying {@code ROLE_ORG_ADMIN} was that cua's own token customizer hard-codes
 * {@code CUSTOMER}: an unwritten invariant holding a trust boundary together across two services.
 * </p>
 *
 * @param name       the realm's name, used in logs and as the {@code REALM_} authority
 * @param issuerUris every normalized {@code iss} value this realm may present; never empty
 * @param jwkSetUri  where to fetch its signing keys, or {@code null} to discover them from the issuer
 * @param grants     the authorities it may confer, uppercased; empty means unrestricted
 */
public record IssuerRealm(String name, Set<String> issuerUris, String jwkSetUri, Set<String> grants) {

    public IssuerRealm {
        Objects.requireNonNull(name, "realm name cannot be null");
        Objects.requireNonNull(issuerUris, "issuerUris cannot be null");
        Set<String> normalized = issuerUris.stream()
                .map(UrlNormalize::normalizeUri)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Realm '%s' declares no issuer URIs".formatted(name));
        }
        issuerUris = Collections.unmodifiableSet(normalized);
        grants = grants == null ? Set.of()
                : grants.stream().map(it -> it.toUpperCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * The URI to point OIDC discovery at when no {@link #jwkSetUri} is configured. The first declared form wins,
     * because discovery asserts that the document's own {@code issuer} equals the location requested — so the
     * alternates, which exist for token matching, are not interchangeable here.
     */
    public String issuerLocation() {
        return issuerUris.iterator().next();
    }

    /** Whether this realm may confer {@code authority}. Case-insensitive: the claim converter emits both cases. */
    public boolean permits(String authority) {
        return grants.isEmpty() || grants.contains(authority.toUpperCase(Locale.ROOT));
    }

}
