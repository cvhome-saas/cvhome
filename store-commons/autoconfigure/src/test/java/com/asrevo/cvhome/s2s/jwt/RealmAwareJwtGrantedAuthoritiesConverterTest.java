package com.asrevo.cvhome.s2s.jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a token is allowed to confer, given who signed it.
 *
 * <p>
 * Both authorization servers write their roles into the same {@code roles} claim, and the converter that reads
 * it consulted neither {@code iss} nor {@code aud} — so a shopper token asking for {@code ORG_ADMIN} would have
 * been granted it. The only thing preventing that was cua's own token customizer hard-coding {@code CUSTOMER},
 * one line in a different service that nothing here enforced. These tests are that enforcement.
 * </p>
 */
class RealmAwareJwtGrantedAuthoritiesConverterTest {

    private static final String CUA_ISSUER = "http://spg.gateway.com/cua";

    private static final String UAA_ISSUER = "http://uaa.gateway.com:8001";

    private static final String ROLES = "roles";

    private static final String ORG_ADMIN = "ROLE_ORG_ADMIN";

    private static final String CUSTOMER = "ROLE_CUSTOMER";

    private static final String CUSTOMER_CLAIM = "CUSTOMER";

    private static final String ORG_ADMIN_CLAIM = "ORG_ADMIN";

    private static final String CUA = "cua";

    private static final String UAA = "uaa";

    private static final String REALM_CUA = RealmAwareJwtGrantedAuthoritiesConverter.REALM_AUTHORITY_PREFIX + CUA;

    private static final String REALM_UAA = RealmAwareJwtGrantedAuthoritiesConverter.REALM_AUTHORITY_PREFIX + UAA;

    private static final IssuerRegistry REGISTRY = new IssuerRegistry(List.of(
            new IssuerRealm(UAA, Set.of(UAA_ISSUER), null, Set.of()),
            new IssuerRealm(CUA, Set.of(CUA_ISSUER), null, Set.of(CUSTOMER, "SCOPE_OPENID"))));

    private static Jwt tokenFrom(String issuer, Object rolesClaim) {
        return tokenFrom(issuer, rolesClaim, null);
    }

    private static Jwt tokenFrom(String issuer, Object rolesClaim, String scopeClaim) {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuer(issuer)
                .claim(ROLES, rolesClaim)
                .claim("scope", scopeClaim == null ? "" : scopeClaim)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
    }

    private static Collection<String> authorities(Jwt jwt) {
        Collection<GrantedAuthority> granted = new RealmAwareJwtGrantedAuthoritiesConverter(REGISTRY).convert(jwt);
        return granted.stream().map(GrantedAuthority::getAuthority).toList();
    }

    @Test
    void capsAShopperTokenAtWhatTheShopperRealmMayConfer() {
        Collection<String> granted = authorities(tokenFrom(CUA_ISSUER, List.of(ORG_ADMIN_CLAIM, "SUPER_ADMIN")));

        assertThat(granted).doesNotContain(ORG_ADMIN, "ROLE_SUPER_ADMIN");
        assertThat(granted).containsExactly(REALM_CUA);
    }

    @Test
    void letsAShopperTokenKeepTheOneRoleItIsFor() {
        assertThat(authorities(tokenFrom(CUA_ISSUER, List.of(CUSTOMER_CLAIM)))).contains(CUSTOMER, REALM_CUA);
    }

    @Test
    void leavesTheStaffRealmUncapped() {
        Collection<String> granted = authorities(tokenFrom(UAA_ISSUER, List.of(ORG_ADMIN_CLAIM)));

        assertThat(granted).contains(ORG_ADMIN, REALM_UAA);
    }

    /**
     * A realm the registry does not know cannot be capped, and does not need to be: the decoder refused such a
     * token long before any of this ran. Falling back to the realm-blind mapping is what keeps the legacy flat
     * trust list, and Boot's own single-issuer support, working unchanged.
     */
    @Test
    void leavesAnUnknownIssuerAlone() {
        Collection<String> granted = authorities(tokenFrom("http://somewhere.else", List.of(ORG_ADMIN_CLAIM)));

        assertThat(granted).contains(ORG_ADMIN);
        assertThat(granted).noneMatch(it -> it.startsWith(RealmAwareJwtGrantedAuthoritiesConverter.REALM_AUTHORITY_PREFIX));
    }

    /**
     * The scope every shopper token carries. It is in the realm's grants so that a dropped-authority warning
     * means something is actually wrong, rather than firing on every storefront request.
     */
    @Test
    void letsTheShopperRealmKeepTheScopeItIsIssuedWith() {
        Jwt token = tokenFrom(CUA_ISSUER, List.of(CUSTOMER_CLAIM), "openid");

        assertThat(authorities(token)).contains("SCOPE_openid", CUSTOMER);
    }

    @Test
    void readsASpaceDelimitedRolesClaimTheSameWay() {
        assertThat(authorities(tokenFrom(CUA_ISSUER, "%s %s".formatted(CUSTOMER_CLAIM, ORG_ADMIN_CLAIM))))
                .contains(CUSTOMER)
                .doesNotContain(ORG_ADMIN);
    }

    @Test
    void namesTheRealmSoAccessChecksCanTellStaffFromShopper() {
        Map<String, Collection<String>> byIssuer = Map.of(
                CUA_ISSUER, authorities(tokenFrom(CUA_ISSUER, List.of(CUSTOMER_CLAIM))),
                UAA_ISSUER, authorities(tokenFrom(UAA_ISSUER, List.of("STORE_ADMIN"))));

        assertThat(byIssuer.get(CUA_ISSUER)).contains(REALM_CUA).doesNotContain(REALM_UAA);
        assertThat(byIssuer.get(UAA_ISSUER)).contains(REALM_UAA).doesNotContain(REALM_CUA);
    }

}
