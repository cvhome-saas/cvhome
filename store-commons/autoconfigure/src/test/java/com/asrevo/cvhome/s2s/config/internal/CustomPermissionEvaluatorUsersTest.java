package com.asrevo.cvhome.s2s.config.internal;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.s2s.services.StoreOrgOwnerRetriever;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The {@code STORE-CORE.USERS.*} tokens, and in particular {@code RESET_PASSWORD}.
 *
 * <p>
 * That one was declared on {@code UserAccountApi.resetPassword} and matched by no {@code case} here, so it fell
 * through every switch to {@code default -> false} and the endpoint was 403 for every caller — including a super
 * admin — from the day it was written. Nothing failed, because an unmapped token is indistinguishable from a
 * refused one. These tests exist so that the next one fails here rather than in a QA pass.
 * </p>
 */
class CustomPermissionEvaluatorUsersTest {

    private static final String TARGET_TYPE = "StoreMerchantId";

    private static final String ORG_CLAIM = "org";

    private static final String STORE_CLAIM = "store";

    private static final String LIST = "STORE-CORE.USERS.LIST";

    private static final String ORG = "21f023932bc66470c104b76f";

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    /** Every token the users API declares. `RESET_PASSWORD` is the one that was missing. */
    private static final List<String> USER_TOKENS = List.of(LIST, "STORE-CORE.USERS.CREATE",
            "STORE-CORE.USERS.UPDATE", "STORE-CORE.USERS.DELETE", "STORE-CORE.USERS.ENABLE",
            "STORE-CORE.USERS.DISABLE", "STORE-CORE.USERS.RESET_PASSWORD");

    /**
     * A context that knows STORE belongs to ORG.
     *
     * <p>
     * An org admin's token names the organization, never the store, so the evaluator has to be told who owns the
     * store before it can admit one — and a context that cannot answer refuses, which is what makes a service
     * missing this lookup fail closed rather than open.
     * </p>
     */
    private final CustomPermissionEvaluator evaluator = new CustomPermissionEvaluator(contextKnowing(STORE, ORG));

    private static StaticApplicationContext contextKnowing(StoreMerchantId store, String org) {
        StaticApplicationContext context = new StaticApplicationContext();
        StoreOrgOwnerRetriever owners = asked -> store.equals(asked) ? new ManagerOrgId(org) : null;
        context.getBeanFactory().registerSingleton("storeOrgOwnerRetriever", owners);
        return context;
    }

    @ParameterizedTest(name = "an org admin may {0}")
    @DisplayName("an org admin holds every users token")
    @ValueSource(strings = {"STORE-CORE.USERS.LIST", "STORE-CORE.USERS.CREATE", "STORE-CORE.USERS.UPDATE",
        "STORE-CORE.USERS.DELETE", "STORE-CORE.USERS.ENABLE", "STORE-CORE.USERS.DISABLE",
        "STORE-CORE.USERS.RESET_PASSWORD"})
    void orgAdminHoldsEveryUsersToken(String token) {
        assertThat(evaluator.hasPermission(orgAdmin(), STORE, TARGET_TYPE, token)).isTrue();
    }

    @ParameterizedTest(name = "a store admin may {0}")
    @DisplayName("a store admin holds every users token for its own store")
    @ValueSource(strings = {"STORE-CORE.USERS.LIST", "STORE-CORE.USERS.CREATE", "STORE-CORE.USERS.UPDATE",
        "STORE-CORE.USERS.DELETE", "STORE-CORE.USERS.ENABLE", "STORE-CORE.USERS.DISABLE",
        "STORE-CORE.USERS.RESET_PASSWORD"})
    void storeAdminHoldsEveryUsersToken(String token) {
        assertThat(evaluator.hasPermission(storeAdmin(), STORE, TARGET_TYPE, token)).isTrue();
    }

    @Test
    @DisplayName("a moderator may read the list and change nothing")
    void moderatorReadsButDoesNotMaintain() {
        Authentication moderator = moderator();

        assertThat(evaluator.hasPermission(moderator, STORE, TARGET_TYPE, LIST)).isTrue();

        assertThat(USER_TOKENS.stream()
                .filter(token -> !LIST.equals(token))
                .filter(token -> evaluator.hasPermission(moderator, STORE, TARGET_TYPE, token)))
                .as("a moderator can see who has access to a store without being able to take it over")
                .isEmpty();
    }

    @Test
    @DisplayName("a store admin holds nothing on another store")
    void storeAdminIsConfinedToItsOwnStore() {
        StoreMerchantId otherStore = new StoreMerchantId("65f023632bc46470c104b75f");

        assertThat(USER_TOKENS.stream()
                .filter(token -> evaluator.hasPermission(storeAdmin(), otherStore, TARGET_TYPE, token)))
                .isEmpty();
    }

    @Test
    @DisplayName("an unrecognised token is refused rather than assumed")
    void anUnknownTokenIsRefused() {
        assertThat(evaluator.hasPermission(orgAdmin(), STORE, TARGET_TYPE, "STORE-CORE.USERS.INVENTED")).isFalse();
    }

    private static Authentication orgAdmin() {
        return principal(Map.of(ORG_CLAIM, ORG), Roles.ROLE_ORG_ADMIN);
    }

    private static Authentication storeAdmin() {
        return principal(Map.of(ORG_CLAIM, ORG, STORE_CLAIM, STORE.storeMerchantId()), Roles.ROLE_STORE_ADMIN);
    }

    private static Authentication moderator() {
        return principal(Map.of(ORG_CLAIM, ORG, STORE_CLAIM, STORE.storeMerchantId()), Roles.ROLE_STORE_MODERATOR);
    }

    /**
     * A real {@link JwtAuthenticationToken} rather than a mock: the code under test reads both the authorities and
     * the token's own claims, so a stub that answers only one of them would pass while the production path failed.
     */
    private static Authentication principal(Map<String, Object> claims, Roles... roles) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("org1-admin")
                .issuedAt(Instant.EPOCH)
                .expiresAt(Instant.EPOCH.plusSeconds(3600))
                .claims(existing -> existing.putAll(claims))
                .claim("roles", Set.of(roles))
                .build();
        return new JwtAuthenticationToken(jwt,
                List.of(roles).stream().map(role -> new SimpleGrantedAuthority(role.name())).toList());
    }

}
