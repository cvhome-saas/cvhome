package com.asrevo.cvhome.cache;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryKeyTest {

    private static final String TEXT = "q=shoes;page=1";

    private static final String SHOES = "shoes";

    @Test
    void twoWrappersWithTheSameTextAreOneKeyWhateverTheObjects() {
        QueryKey<List<String>> first = QueryKey.of(TEXT, List.of(SHOES));
        QueryKey<List<String>> second = QueryKey.of(TEXT, List.of("other object, same text"));

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second).hasToString(first.hash().value());
        assertThat(first.cacheKeyPart()).isEqualTo(QueryHash.of(TEXT).value());
        assertThat(first.criteria()).containsExactly(SHOES);
        assertThat(first).isNotEqualTo(QueryKey.of("q=boots", List.of())).isNotEqualTo(TEXT);
        assertThat(CacheKey.of(Stores.A, Stores.EN).with(first))
                .isEqualTo(CacheKey.of(Stores.A, Stores.EN).with(second));
        assertThatThrownBy(() -> QueryKey.of(TEXT, null)).isInstanceOf(IllegalArgumentException.class);
    }
}
