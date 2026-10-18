package com.asrevo.cvhome.cache;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryHashTest {

    private static final String QUERY = "categories=1,2;q=shoes";

    @Test
    void theSameNormalisedTextHashesTheSameAndReadsAsSixtyFourHexCharacters() {
        QueryHash hash = QueryHash.of(QUERY);

        assertThat(hash).isEqualTo(QueryHash.of(QUERY));
        assertThat(hash.cacheKeyPart()).isEqualTo(hash.value()).hasSize(64).matches("[0-9a-f]{64}");
        assertThat(hash).isNotEqualTo(QueryHash.of("q=shoes;categories=1,2"));
    }

    @Test
    void blankAndNullAreRefused() {
        assertThatThrownBy(() -> QueryHash.of(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new QueryHash(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new QueryHash(null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(QueryHash.of("").value()).hasSize(64);
    }
}
