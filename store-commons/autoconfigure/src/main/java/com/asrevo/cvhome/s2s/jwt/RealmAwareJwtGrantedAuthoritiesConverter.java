package com.asrevo.cvhome.s2s.jwt;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import lombok.extern.slf4j.Slf4j;

/**
 * Reads a token's authorities the way {@link UaaJwtGrantedAuthoritiesConverter} always has, then caps them at
 * what the issuing realm is allowed to confer.
 *
 * <p>
 * The cap is the point. Both authorization servers write a {@code roles} claim in the same shape, and the
 * converter that reads it consults neither {@code iss} nor {@code aud} — so once decoded, a shopper token and a
 * staff token were indistinguishable to every resource server. The only thing stopping a cua token from carrying
 * {@code ROLE_ORG_ADMIN} was that cua's own token customizer hard-codes {@code CUSTOMER}. That put a trust
 * boundary in the keeping of a single line in a different service, where nothing enforced it and nothing tested
 * it. Here, a realm declaring {@code grants: [ROLE_CUSTOMER]} cannot confer anything else no matter what its
 * tokens claim.
 * </p>
 *
 * <p>
 * Every principal also gets a {@code REALM_<name>} authority naming where it came from, which is what lets
 * {@code StoreRoleAccessChecker} refuse a staff check for a shopper token and vice versa. It is granted after
 * the cap so a realm cannot filter away its own identity.
 * </p>
 *
 * <p>
 * A realm with no {@code grants} is unrestricted — the staff realm, whose clients carry arbitrary scopes, and
 * the legacy realm synthesised from a flat {@code issuer-uri-set}. An unknown issuer is unrestricted too, which
 * costs nothing: the decoder rejected it long before this converter ran.
 * </p>
 */
@Slf4j
public final class RealmAwareJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    public static final String REALM_AUTHORITY_PREFIX = "REALM_";

    private final IssuerRegistry registry;

    public RealmAwareJwtGrantedAuthoritiesConverter(IssuerRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry cannot be null");
    }

    @Override
    public @NonNull Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> claimed = UaaJwtGrantedAuthoritiesConverter.getGrantedAuthorities(jwt.getClaims());
        Optional<IssuerRealm> realm = this.registry.findByIssuer(jwt.getIssuer() == null ? null
                : jwt.getIssuer().toString());
        if (realm.isEmpty()) {
            return claimed;
        }
        return capped(claimed, realm.get());
    }

    private static Set<GrantedAuthority> capped(Set<GrantedAuthority> claimed, IssuerRealm realm) {
        Set<GrantedAuthority> granted = new LinkedHashSet<>();
        for (GrantedAuthority authority : claimed) {
            if (realm.permits(authority.getAuthority())) {
                granted.add(authority);
            } else {
                log.warn("Realm '{}' may not confer '{}'; dropping it. Its grants are {}.", realm.name(),
                        authority.getAuthority(), realm.grants());
            }
        }
        granted.add(new SimpleGrantedAuthority(REALM_AUTHORITY_PREFIX + realm.name()));
        return granted;
    }

}
