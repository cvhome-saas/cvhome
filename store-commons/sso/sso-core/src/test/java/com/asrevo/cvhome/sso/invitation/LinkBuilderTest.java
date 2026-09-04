package com.asrevo.cvhome.sso.invitation;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import static org.assertj.core.api.Assertions.assertThat;

class LinkBuilderTest {

    private static final String RESET_PAGE = "/reset-password";

    private static final String TOKEN = "abc";

    private static final String AWKWARD_TOKEN = "a+b/c";

    private static final AuthorizationServerSettings ISSUER =
            AuthorizationServerSettings.builder().issuer("http://uaa.test:8001").build();

    private static LinksProperties pages(String baseUrl, String invitation, String reset) {
        return new LinksProperties(Duration.ofDays(7), Duration.ofHours(1), false, baseUrl, invitation, reset);
    }

    @Test
    void linksLandOnTheIssuerNotTheRequestHost() {
        LinkBuilder links = new LinkBuilder(ISSUER, pages("", "/accept-invitation", RESET_PAGE));

        assertThat(links.invitation(TOKEN)).isEqualTo("http://uaa.test:8001/accept-invitation?token=abc");
        assertThat(links.passwordReset(AWKWARD_TOKEN)).isEqualTo("http://uaa.test:8001/reset-password?token=a%2Bb%2Fc");
    }

    /**
     * uaa still mints the token and still redeems it; only the page that collects the new password moved. A
     * merchant who follows an invitation must land in the console, not on the identity server.
     */
    @Test
    void aConfiguredBaseUrlMovesThePagesToTheConsole() {
        LinkBuilder links = new LinkBuilder(ISSUER, pages("http://gateway.com:8000", "/invitation", RESET_PAGE));

        assertThat(links.invitation(TOKEN)).isEqualTo("http://gateway.com:8000/invitation?token=abc");
        assertThat(links.passwordReset(AWKWARD_TOKEN)).isEqualTo("http://gateway.com:8000/reset-password?token=a%2Bb%2Fc");
    }

}
