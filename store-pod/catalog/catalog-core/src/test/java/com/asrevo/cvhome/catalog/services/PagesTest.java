package com.asrevo.cvhome.catalog.services;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The list envelope every catalog list endpoint answers with. {@code size} is the size of <em>this</em> page, not the
 * requested page size — a paginated client that confuses the two pages forever.
 */
class PagesTest {

    private static final String A = "a";

    private static final String B = "b";

    @Test
    void readableEnvelopeCarriesThePageCoordinatesNotTheRequestedSize() {
        var page = new PageImpl<>(List.of(1, 2, 3), PageRequest.of(1, 3), 8);

        ReadableEntityList<String> list = Pages.toReadable(page, String::valueOf);

        assertThat(list.getContent()).containsExactly("1", "2", "3");
        assertThat(list.getSize()).isEqualTo(3);
        assertThat(list.getTotalElements()).isEqualTo(8);
        assertThat(list.getTotalPages()).isEqualTo(3);
        assertThat(list.getPageNumber()).isEqualTo(1);
    }

    @Test
    void emptyPageStillReportsItsTotals() {
        var page = new PageImpl<Integer>(List.of(), PageRequest.of(0, 10), 0);

        ReadableEntityList<String> list = Pages.toReadable(page, String::valueOf);

        assertThat(list.getContent()).isEmpty();
        assertThat(list.getSize()).isZero();
        assertThat(list.getTotalElements()).isZero();
    }

    @Test
    void singleTreatsTheWholeListAsOnePage() {
        ReadableEntityList<String> list = Pages.single(List.of(A, B));

        assertThat(list.getContent()).containsExactly(A, B);
        assertThat(list.getTotalElements()).isEqualTo(2);
        assertThat(list.getTotalPages()).isEqualTo(1);
        assertThat(list.getPageNumber()).isZero();
    }

}
