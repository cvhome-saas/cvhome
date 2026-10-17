package com.asrevo.cvhome.catalog.reads;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SuggestQueryTest {

    private static final String X = "x";

    @Test
    void whatTheShopperTypedIsLoweredTrimmedCutAndTheLimitClamped() {
        assertThat(SuggestQuery.of("  Sho ", 8)).isEqualTo(SuggestQuery.of("sho", 8));
        assertThat(SuggestQuery.of(null, 0)).isEqualTo(new SuggestQuery("", 1));
        assertThat(SuggestQuery.of(X.repeat(100), 99).text()).hasSize(SuggestQuery.MAX_LENGTH);
        assertThat(SuggestQuery.of(X, 99).limit()).isEqualTo(SuggestQuery.MAX_SUGGESTIONS);
        assertThat(SuggestQuery.of("Shoes", 5).cacheKeyPart()).isEqualTo("shoes:5");
    }
}
