package com.asrevo.cvhome.sso.token;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import static org.assertj.core.api.Assertions.assertThat;

/** The context survives the round trip through an authorization's attributes, and a plain authorization has none. */
class ImpersonationContextTest {

    private static final String OPERATOR = "super-admin";

    private static final String MERCHANT = "org1-store1-admin";

    private static final String REASON = "ticket 42";

    private static final Instant NOT_AFTER = Instant.parse("2026-04-01T09:45:00Z");

    private static RegisteredClient client() {
        return RegisteredClient.withId("id").clientId("console-impersonation")
                .authorizationGrantType(AuthorizationGrantType.TOKEN_EXCHANGE).build();
    }

    @Test
    void roundTripsThroughTheAuthorizationAttributes() {
        UUID operator = UUID.randomUUID();
        UUID merchant = UUID.randomUUID();
        ImpersonationContext context = new ImpersonationContext(operator, OPERATOR, merchant, MERCHANT, REASON, NOT_AFTER);
        OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(client())
                .principalName(merchant.toString()).authorizationGrantType(AuthorizationGrantType.TOKEN_EXCHANGE);
        context.writeTo(builder);

        assertThat(ImpersonationContext.from(builder.build())).contains(context);
    }

    @Test
    void anOrdinaryAuthorizationIsNotAnImpersonation() {
        OAuth2Authorization plain = OAuth2Authorization.withRegisteredClient(client()).principalName(MERCHANT)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).build();

        assertThat(ImpersonationContext.from(plain)).isEmpty();
        assertThat(ImpersonationContext.from(null)).isEmpty();
    }

}
