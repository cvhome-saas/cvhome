package com.asrevo.cvhome.uaa.sdk.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The two OAuth2 enums the admin SDK sends over the wire.
 *
 * <p>
 * Their {@code value()} is the spec's own spelling — {@code authorization_code}, {@code client_secret_basic} — and
 * it is what uaa reads, so a Java-side rename that did not update it would silently register a client with a grant
 * the server does not recognise. Reading is deliberately case-insensitive, because these values arrive from
 * hand-written configuration as often as from a server, and an unknown one fails with the offending text rather
 * than a bare {@code IllegalArgumentException} from {@code valueOf}.
 * </p>
 */
class OAuthEnumsTest {

    private static final String SPACE = " ";
    private static final String UNKNOWN_GRANT = "password";
    private static final String UNKNOWN_METHOD = "private_key_jwt";
    private static final String NULL_MESSAGE = "cannot be null";

    @ParameterizedTest
    @EnumSource(OAuthGrantType.class)
    void everyGrantTypeRoundTripsThroughItsWireValue(OAuthGrantType grant) {
        assertThat(OAuthGrantType.from(grant.value())).isEqualTo(grant);
        assertThat(grant.value()).isLowerCase().doesNotContain(SPACE);
    }

    @ParameterizedTest
    @EnumSource(ClientAuthMethod.class)
    void everyAuthMethodRoundTripsThroughItsWireValue(ClientAuthMethod method) {
        assertThat(ClientAuthMethod.from(method.value())).isEqualTo(method);
        assertThat(method.value()).isLowerCase().doesNotContain(SPACE);
    }

    @Test
    void theWireValuesAreTheSpecsOwnSpellings() {
        assertThat(OAuthGrantType.AUTHORIZATION_CODE.value()).isEqualTo("authorization_code");
        assertThat(OAuthGrantType.CLIENT_CREDENTIALS.value()).isEqualTo("client_credentials");
        assertThat(OAuthGrantType.REFRESH_TOKEN.value()).isEqualTo("refresh_token");
        assertThat(ClientAuthMethod.CLIENT_SECRET_BASIC.value()).isEqualTo("client_secret_basic");
        assertThat(ClientAuthMethod.CLIENT_SECRET_POST.value()).isEqualTo("client_secret_post");
        assertThat(ClientAuthMethod.NONE.value()).isEqualTo("none");
    }

    @Test
    void readingIsCaseInsensitiveBecauseTheseArriveFromHandWrittenConfigurationToo() {
        assertThat(OAuthGrantType.from("AUTHORIZATION_CODE")).isEqualTo(OAuthGrantType.AUTHORIZATION_CODE);
        assertThat(ClientAuthMethod.from("Client_Secret_Basic")).isEqualTo(ClientAuthMethod.CLIENT_SECRET_BASIC);
    }

    @Test
    void anUnknownValueFailsWithTheOffendingTextRatherThanABareValueOfError() {
        assertThatThrownBy(() -> OAuthGrantType.from(UNKNOWN_GRANT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(UNKNOWN_GRANT);
        assertThatThrownBy(() -> ClientAuthMethod.from(UNKNOWN_METHOD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(UNKNOWN_METHOD);
    }

    @Test
    void aNullValueIsNamedForWhatItIsRatherThanBecomingANullPointer() {
        assertThatThrownBy(() -> OAuthGrantType.from(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(NULL_MESSAGE);
        assertThatThrownBy(() -> ClientAuthMethod.from(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(NULL_MESSAGE);
    }
}
