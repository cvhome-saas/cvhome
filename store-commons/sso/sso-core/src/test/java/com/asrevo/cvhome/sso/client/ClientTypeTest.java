package com.asrevo.cvhome.sso.client;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import static org.assertj.core.api.Assertions.assertThat;

/** The type is derived from how a client authenticates and what it may ask for; it is never stored. */
class ClientTypeTest {

    @Test
    void noneAloneIsPublic() {
        assertThat(ClientType.of(Set.of(ClientAuthenticationMethod.NONE), Set.of(AuthorizationGrantType.AUTHORIZATION_CODE)))
                .isEqualTo(ClientType.PUBLIC);
        assertThat(ClientType.PUBLIC.holdsSecret()).isFalse();
    }

    @Test
    void clientCredentialsOnlyIsMachine() {
        assertThat(ClientType.of(Set.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC),
                Set.of(AuthorizationGrantType.CLIENT_CREDENTIALS))).isEqualTo(ClientType.MACHINE);
    }

    @Test
    void aSecretWithUserGrantsIsConfidential() {
        assertThat(ClientType.of(Set.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC),
                Set.of(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN)))
                .isEqualTo(ClientType.CONFIDENTIAL);
        assertThat(ClientType.of(Set.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC, ClientAuthenticationMethod.NONE),
                Set.of(AuthorizationGrantType.CLIENT_CREDENTIALS))).isEqualTo(ClientType.MACHINE);
    }

}
