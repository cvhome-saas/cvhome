package com.asrevo.cvhome.s2s.utils;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Issuer URIs compare normalized: case-insensitive scheme and host, the default port dropped, the path kept.
 */
class UrlNormalizeTest {

    private static final String ISSUER = "http://gateway.com:8000/uaa";

    private static final String NOT_A_URI = "not a uri at all";

    @Test
    void schemeAndHostAreLoweredAndDefaultPortsDropped() throws Exception {
        assertThat(UrlNormalize.normalizeUri("HTTP://Gateway.COM:80/uaa/")).isEqualTo("http://gateway.com/uaa/");
        assertThat(UrlNormalize.normalizeUri("https://gateway.com:443")).isEqualTo("https://gateway.com");
        assertThat(UrlNormalize.normalizeUri(ISSUER)).isEqualTo(ISSUER);
    }

    @Test
    void valuesThatAreNotIssuerUrisAreRefusedLoudlyOrKeptQuietly() {
        assertThatThrownBy(() -> UrlNormalize.normalizeUri(" ")).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> UrlNormalize.normalizeUri("gateway.com/uaa")).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> UrlNormalize.normalizeUri("http://[bad")).isInstanceOf(JwtException.class);
        assertThat(UrlNormalize.normalizeQuietly(NOT_A_URI)).isEqualTo(NOT_A_URI);
        assertThat(UrlNormalize.normalizeQuietly(ISSUER)).isEqualTo(ISSUER);
    }

}
