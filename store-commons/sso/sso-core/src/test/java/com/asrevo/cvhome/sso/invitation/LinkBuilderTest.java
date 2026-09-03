package com.asrevo.cvhome.sso.invitation;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import static org.assertj.core.api.Assertions.assertThat;

class LinkBuilderTest {

    private final LinkBuilder links = new LinkBuilder(AuthorizationServerSettings.builder().issuer("http://uaa.test:8001").build());

    @Test
    void linksLandOnTheIssuerNotTheRequestHost() {
        assertThat(links.invitation("abc")).isEqualTo("http://uaa.test:8001/accept-invitation?token=abc");
        assertThat(links.passwordReset("a+b/c")).isEqualTo("http://uaa.test:8001/reset-password?token=a%2Bb%2Fc");
    }

}
