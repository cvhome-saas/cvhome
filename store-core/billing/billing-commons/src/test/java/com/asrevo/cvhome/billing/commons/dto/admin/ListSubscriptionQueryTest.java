package com.asrevo.cvhome.billing.commons.dto.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.billing.commons.SubscriptionStatus;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The platform register's filter, read the way the web layer reads it.
 *
 * <p>
 * Every field here is optional and absent means "do not narrow on it". {@code blockedOnly} was the one exception,
 * and only because it was a primitive: Jackson refuses to map an absent or null value onto one, so a body that left
 * the flag out was rejected as unreadable — a 400 naming nothing, on the operator's default screen.
 * </p>
 */
class ListSubscriptionQueryTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    @DisplayName("a body with no filters at all reads as every subscription")
    void anEmptyBodyReads() {
        // The regression. `{}` was a 400 before blockedOnly was boxed, which is what the platform console's
        // "show me everything" call would have been had it not happened to send the flag on every request.
        ListSubscriptionQuery query = JSON.readValue("{}", ListSubscriptionQuery.class);

        assertThat(query.org()).isNull();
        assertThat(query.status()).isNull();
        assertThat(query.planCode()).isNull();
        assertThat(query.term()).isNull();
        assertThat(query.blockedOnly()).isFalse();
    }

    @Test
    @DisplayName("a partial body reads, keeping the filter it names")
    void aPartialBodyReads() {
        ListSubscriptionQuery query = JSON.readValue("{\"status\":\"ACTIVE\"}", ListSubscriptionQuery.class);

        assertThat(query.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(query.blockedOnly()).isFalse();
    }

    @Test
    @DisplayName("an explicit null flag is the same as an absent one")
    void anExplicitNullFlagReads() {
        assertThat(JSON.readValue("{\"blockedOnly\":null}", ListSubscriptionQuery.class).blockedOnly()).isFalse();
    }

    @Test
    @DisplayName("the flag still means what it says when it is sent")
    void theFlagIsHonoured() {
        assertThat(JSON.readValue("{\"blockedOnly\":true}", ListSubscriptionQuery.class).blockedOnly()).isTrue();
        assertThat(JSON.readValue("{\"blockedOnly\":false}", ListSubscriptionQuery.class).blockedOnly()).isFalse();
    }

    @Test
    @DisplayName("a filter built in Java with a null flag is false rather than an unboxing failure")
    void aNullFlagInJavaIsFalse() {
        // PlatformBillingServiceImpl builds one of these when the whole body is absent; the compact constructor is
        // what keeps `filter.blockedOnly()` safe to unbox at the repository call.
        assertThat(new ListSubscriptionQuery(null, null, null, null, null).blockedOnly()).isFalse();
    }

}
