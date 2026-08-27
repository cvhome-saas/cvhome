package com.asrevo.cvhome.catalog.config;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.catalog.repositories.ProductSearchIndexRepository;
import com.asrevo.cvhome.catalog.services.product.ProductSearchIndexer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The startup backfill.
 *
 * <p>
 * It has to be additive. Reindexing everything on every restart would re-read every catalogue in the pod for
 * nothing; indexing nothing would leave a seeded database unsearchable, because {@code spring.sql.init}
 * inserts its rows after {@code schema.sql} has already run.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class ProductSearchIndexInitializerTest {

    private static final int BATCH = 200;

    @Mock
    private ProductSearchIndexRepository searchIndexRepository;

    @Mock
    private ProductSearchIndexer indexer;

    @InjectMocks
    private ProductSearchIndexInitializer initializer;

    @Test
    void aCatalogueTheIndexHasNeverSeenIsIndexed() {
        when(searchIndexRepository.productIdsMissingFromIndex()).thenReturn(List.of(1L, 2L, 3L));

        initializer.indexWhatIsMissing();

        verify(indexer).reindexBatch(List.of(1L, 2L, 3L));
    }

    @Test
    void aRunningSystemFindsNothingToDo() {
        when(searchIndexRepository.productIdsMissingFromIndex()).thenReturn(List.of());

        initializer.indexWhatIsMissing();

        verify(indexer, never()).reindexBatch(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void aLargeBackfillIsSplitIntoBatchesCoveringEveryProduct() {
        List<Long> missing = IntStream.rangeClosed(1, BATCH * 2 + 5).mapToObj(Long::valueOf).toList();
        when(searchIndexRepository.productIdsMissingFromIndex()).thenReturn(missing);

        initializer.indexWhatIsMissing();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Long>> batches = ArgumentCaptor.forClass(List.class);
        verify(indexer, org.mockito.Mockito.times(3)).reindexBatch(batches.capture());
        assertThat(batches.getAllValues()).extracting(List::size).containsExactly(BATCH, BATCH, 5);
        assertThat(batches.getAllValues().stream().flatMap(List::stream).toList())
                .containsExactlyElementsOf(missing);
    }
}
