package com.asrevo.cvhome.sso.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The grant vocabulary's parse, both ways.
 *
 * <p>
 * {@code ClientClientDetailsMapper} reads a stored client's grants back through {@code from(value)}, and the
 * token-exchange grant's wire value is a URN — {@code urn:ietf:params:oauth:grant-type:token-exchange} — which no
 * upper-casing turns into an enum name. Before the value was matched first, listing the impersonation client in the
 * admin console was a 500.
 * </p>
 */
class OAuthGrantTypeTest {

    private static final String UNKNOWN = "device_code";

    @Test
    void roundTripsEveryValueThroughItsWireForm() {
        for (OAuthGrantType type : OAuthGrantType.values()) {
            assertThat(OAuthGrantType.from(type.value())).isEqualTo(type);
            assertThat(OAuthGrantType.from(type.name())).isEqualTo(type);
        }
    }

    @Test
    void theTokenExchangeGrantIsSpringsUrn() {
        assertThat(OAuthGrantType.TOKEN_EXCHANGE.value()).isEqualTo(AuthorizationGrantType.TOKEN_EXCHANGE.getValue());
        assertThat(OAuthGrantType.from(AuthorizationGrantType.TOKEN_EXCHANGE.getValue()).toSpring())
                .isEqualTo(AuthorizationGrantType.TOKEN_EXCHANGE);
    }

    @Test
    void rejectsAnUnknownGrantByName() {
        assertThatThrownBy(() -> OAuthGrantType.from(UNKNOWN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(UNKNOWN);
        assertThatThrownBy(() -> OAuthGrantType.from(null)).isInstanceOf(IllegalArgumentException.class);
    }

}
