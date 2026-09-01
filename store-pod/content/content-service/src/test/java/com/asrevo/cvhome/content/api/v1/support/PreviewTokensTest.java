package com.asrevo.cvhome.content.api.v1.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.testsupport.time.MutableClock;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The preview token's guarantees: bound to one store and one slug, expires after its TTL, and no part of it
 * survives tampering.
 */
class PreviewTokensTest {

    private static final Instant NOW = Instant.parse("2026-08-31T12:00:00Z");

    private static final StoreMerchantId STORE = new StoreMerchantId("store-a");

    private static final StoreMerchantId OTHER = new StoreMerchantId("store-b");

    private static final String SLUG = "layout:HOME";

    private final PreviewTokens tokens = new PreviewTokens(Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void aTokenIsValidForItsOwnStoreAndSlugOnly() {
        String token = tokens.issue(STORE, SLUG);
        assertThat(tokens.valid(token, STORE, SLUG)).isTrue();
        assertThat(tokens.valid(token, OTHER, SLUG)).isFalse();
        assertThat(tokens.valid(token, STORE, "layout:OTHER")).isFalse();
    }

    @Test
    void aTokenExpiresAfterItsTtl() {
        MutableClock clock = new MutableClock();
        clock.set(NOW);
        PreviewTokens timed = new PreviewTokens(clock);
        String token = timed.issue(STORE, SLUG);
        clock.advance(Duration.ofMinutes(29));
        assertThat(timed.valid(token, STORE, SLUG)).isTrue();
        clock.advance(Duration.ofMinutes(2));
        assertThat(timed.valid(token, STORE, SLUG)).isFalse();
    }

    @Test
    void tamperingInvalidatesTheToken() {
        String token = tokens.issue(STORE, SLUG);
        String zero = "0";
        String flipped = token.substring(0, token.length() - 1)
                + (token.endsWith(zero) ? "1" : zero);
        assertThat(tokens.valid(flipped, STORE, SLUG)).isFalse();
        // payload swapped for another store's, signature kept
        String otherPayload = tokens.issue(OTHER, SLUG);
        String franken = otherPayload.substring(0, otherPayload.lastIndexOf('.'))
                + token.substring(token.lastIndexOf('.'));
        assertThat(tokens.valid(franken, STORE, SLUG)).isFalse();
        assertThat(tokens.valid(franken, OTHER, SLUG)).isFalse();
    }

    @Test
    void malformedTokensAreRefusedNotExploded() {
        assertThat(tokens.valid(null, STORE, SLUG)).isFalse();
        assertThat(tokens.valid("", STORE, SLUG)).isFalse();
        assertThat(tokens.valid("no-dot", STORE, SLUG)).isFalse();
        assertThat(tokens.valid("not!base64.deadbeef", STORE, SLUG)).isFalse();
        assertThat(tokens.valid(".deadbeef", STORE, SLUG)).isFalse();
    }

}
