package com.asrevo.cvhome.sso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@code from} used to call itself instead of {@code valueOf}, so every parse recursed until the
 * stack ran out. Nothing noticed because {@code AdminClientController} had no caller in the
 * repository until uaa's console gained a clients screen: the list endpoint answers
 * {@code ClientSummary} and never parses an auth method, so only reading or saving one client hit
 * it — as a 500 carrying {@code StackOverflowError}, on an endpoint that looks simply broken.
 *
 * The sibling {@code OAuthGrantType.from} has always been correct, which is what makes this a
 * copy-paste slip rather than a design question.
 */
class ClientAuthMethodTest {

    /** A real OAuth2 method uaa does not support, so the failure is the interesting kind. */
    private static final String UNSUPPORTED = "private_key_jwt";

    @Test
    @DisplayName("parses a wire value in the case the server sends it")
    void parsesWireValue() {
        assertThat(ClientAuthMethod.from("client_secret_basic")).isEqualTo(ClientAuthMethod.CLIENT_SECRET_BASIC);
        assertThat(ClientAuthMethod.from("none")).isEqualTo(ClientAuthMethod.NONE);
    }

    @Test
    @DisplayName("parses the enum spelling too, and is not case sensitive")
    void parsesEnumSpelling() {
        assertThat(ClientAuthMethod.from("CLIENT_SECRET_POST")).isEqualTo(ClientAuthMethod.CLIENT_SECRET_POST);
        assertThat(ClientAuthMethod.from("Client_Secret_Post")).isEqualTo(ClientAuthMethod.CLIENT_SECRET_POST);
    }

    @Test
    @DisplayName("rejects an unknown method by name rather than by exhausting the stack")
    void rejectsUnknown() {
        assertThatThrownBy(() -> ClientAuthMethod.from(UNSUPPORTED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(UNSUPPORTED);
    }

    @Test
    @DisplayName("rejects null")
    void rejectsNull() {
        assertThatThrownBy(() -> ClientAuthMethod.from(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("round-trips through the wire value it serializes as")
    void roundTrips() {
        for (ClientAuthMethod method : ClientAuthMethod.values()) {
            assertThat(ClientAuthMethod.from(method.value())).isEqualTo(method);
        }
    }

    @Test
    @DisplayName("maps to Spring's own constant")
    void mapsToSpring() {
        assertThat(ClientAuthMethod.CLIENT_SECRET_BASIC.toSpring())
                .isEqualTo(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
    }
}
