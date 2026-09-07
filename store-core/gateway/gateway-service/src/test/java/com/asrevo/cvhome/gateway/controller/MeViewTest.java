package com.asrevo.cvhome.gateway.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import com.asrevo.cvhome.gateway.impersonation.ImpersonationView;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * One shape for both kinds of principal: an OIDC login answers from its ID token, an impersonated session — a plain
 * {@code OAuth2User} built from the exchanged token — from its attributes.
 */
class MeViewTest {

    private static final String SUB = "sub";

    private static final String OPERATOR_ID = "65d8419c-8765-4b8b-a15f-910dce959931";

    private static final String MERCHANT_ID = "60ab49a5-7f06-4b5a-be81-9b30bb6559ae";

    private static final String SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    private static final String STORE_ADMIN = "ROLE_STORE_ADMIN";

    private static final String UAA = "uaa";

    private static final String PREFERRED_USERNAME = "preferred_username";

    private static final String EMAIL = "email";

    private static final String OPERATOR = "super-admin";

    private static final String GIVEN = "Super";

    private static final String FAMILY = "Admin";

    private static final String OPERATOR_EMAIL = "super-admin@mail.com";

    private static final String MERCHANT = "org1-store1-admin";

    private static final String MERCHANT_EMAIL = "org1-store1-admin@mail.com";

    private static final String PRINCIPAL_KEY = "cvhome_principal";

    private static final String COMPOSITE = String.format("%s/%s", MERCHANT_ID, OPERATOR_ID);

    @Test
    void anOidcLoginAnswersFromItsIdToken() {
        OidcIdToken idToken = new OidcIdToken("id", Instant.EPOCH, Instant.EPOCH.plusSeconds(60), Map.of(
                SUB, OPERATOR_ID, PREFERRED_USERNAME, OPERATOR, "given_name", GIVEN, "family_name", FAMILY,
                EMAIL, OPERATOR_EMAIL));
        DefaultOidcUser user = new DefaultOidcUser(List.of(new SimpleGrantedAuthority(SUPER_ADMIN)), idToken);
        OAuth2AuthenticationToken login = new OAuth2AuthenticationToken(user, user.getAuthorities(), UAA);

        MeView view = MeView.of(login, null);

        assertThat(view.principal().claims()).containsEntry(SUB, OPERATOR_ID);
        assertThat(view.principal().name()).isEqualTo(OPERATOR_ID);
        assertThat(view.principal().preferredUsername()).isEqualTo(OPERATOR);
        assertThat(view.principal().givenName()).isEqualTo(GIVEN);
        assertThat(view.principal().familyName()).isEqualTo(FAMILY);
        assertThat(view.principal().email()).isEqualTo(OPERATOR_EMAIL);
        assertThat(view.authorities()).extracting(MeView.AuthorityView::authority).containsExactly(SUPER_ADMIN);
        assertThat(view.impersonation()).isNull();
    }

    @Test
    void anImpersonatedSessionAnswersFromTheExchangedTokensAttributes() {
        DefaultOAuth2User merchant = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(STORE_ADMIN)),
                Map.of(SUB, MERCHANT_ID, PREFERRED_USERNAME, MERCHANT, EMAIL, MERCHANT_EMAIL, PRINCIPAL_KEY, COMPOSITE),
                PRINCIPAL_KEY);
        OAuth2AuthenticationToken login = new OAuth2AuthenticationToken(merchant, merchant.getAuthorities(), UAA);
        ImpersonationView acting = new ImpersonationView(MERCHANT, MERCHANT_ID, "ticket", Instant.EPOCH.plusSeconds(900));

        MeView view = MeView.of(login, acting);

        assertThat(view.principal().claims()).containsEntry(SUB, MERCHANT_ID);
        assertThat(view.principal().name()).isEqualTo(COMPOSITE);
        assertThat(view.principal().preferredUsername()).isEqualTo(MERCHANT);
        assertThat(view.principal().givenName()).isNull();
        assertThat(view.principal().email()).isEqualTo(MERCHANT_EMAIL);
        assertThat(view.authorities()).extracting(MeView.AuthorityView::authority).containsExactly(STORE_ADMIN);
        assertThat(view.impersonation()).isEqualTo(acting);
    }

}
