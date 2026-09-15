package com.asrevo.cvhome.content.reads;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostsFilterTest {

    private static final String NEWS = "news";

    @Test
    void absentAndBlankFiltersReadAsDashesAndPresentOnesTrimmed() {
        assertThat(PostsFilter.of(null, null).cacheKeyPart()).isEqualTo("-:-");
        assertThat(PostsFilter.of(" news ", "")).isEqualTo(new PostsFilter(NEWS, null));
        assertThat(PostsFilter.of(NEWS, "spring").cacheKeyPart()).isEqualTo("news:spring");
    }
}
