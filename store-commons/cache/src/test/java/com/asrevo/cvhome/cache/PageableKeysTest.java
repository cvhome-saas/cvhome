package com.asrevo.cvhome.cache;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class PageableKeysTest {

    private static final String ALL = "all";

    @Test
    void aPageRequestReadsAsPageSizeAndSort() {
        assertThat(PageableKeys.of(PageRequest.of(2, 20)).cacheKeyPart()).isEqualTo("p2s20");
        assertThat(PageableKeys.of(PageRequest.of(0, 10, Sort.by("name").ascending().and(Sort.by("id").descending())))
                .cacheKeyPart()).isEqualTo("p0s10:name+id-");
        assertThat(PageableKeys.of(Pageable.unpaged()).cacheKeyPart()).isEqualTo(ALL);
        assertThat(PageableKeys.of(null).cacheKeyPart()).isEqualTo(ALL);
        assertThat(CacheKey.of(Stores.A, Stores.EN).with(PageableKeys.of(PageRequest.of(1, 5))).render())
                .endsWith("|en|p1s5");
    }
}
