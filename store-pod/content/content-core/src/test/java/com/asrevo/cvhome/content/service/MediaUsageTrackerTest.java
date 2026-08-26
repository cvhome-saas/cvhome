package com.asrevo.cvhome.content.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.MediaUsageRow;
import com.asrevo.cvhome.content.model.MediaOwnerKind;
import com.asrevo.cvhome.content.repository.MediaUsageRepository;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * The reverse index behind "used on 3 pages": rebuilt for an item on every save, so a field cleared in the editor
 * must not leave a stale row behind.
 */
class MediaUsageTrackerTest {

    private static final String REF_ONE = "1";

    private static final String SLUG = "hello";

    private static final String HERO_FIELD = "hero";

    private MediaUsageRepository repository;

    private MediaUsageTracker tracker;

    @BeforeEach
    void setUp() {
        repository = mock(MediaUsageRepository.class);
        tracker = new MediaUsageTracker(repository);
    }

    @Test
    void recordingRebuildsTheItemsRowsAndSkipsUnsetFields() {
        Content item = ContentFixtures.content(1L, ContentType.POST, SLUG);
        Map<String, Long> refs = new LinkedHashMap<>();
        refs.put(HERO_FIELD, 5L);
        refs.put("og", null);

        tracker.record(item, refs);

        verify(repository).deleteByOwnerKindAndOwnerRef(MediaOwnerKind.CONTENT, REF_ONE);
        var captor = org.mockito.ArgumentCaptor.forClass(java.util.List.class);
        verify(repository).saveAll(captor.capture());
        assertThat((java.util.List<MediaUsageRow>) captor.getValue()).singleElement().satisfies(r -> {
            assertThat(r.getAssetId()).isEqualTo(5L);
            assertThat(r.getField()).isEqualTo(HERO_FIELD);
            assertThat(r.getContentType()).isEqualTo(ContentType.POST);
            assertThat(r.getOwnerKind()).isEqualTo(MediaOwnerKind.CONTENT);
            assertThat(r.getOwnerRef()).isEqualTo(REF_ONE);
        });
    }

    @Test
    void anItemThatReferencesNothingOnlyClears() {
        Content item = ContentFixtures.content(1L, ContentType.POST, SLUG);

        tracker.record(item, Map.of());

        verify(repository).deleteByOwnerKindAndOwnerRef(MediaOwnerKind.CONTENT, REF_ONE);
        verify(repository, never()).saveAll(any());
    }

    @Test
    void forgettingClearsTheItemsRows() {
        tracker.forget(ContentFixtures.content(1L, ContentType.POST, SLUG));

        verify(repository).deleteByOwnerKindAndOwnerRef(MediaOwnerKind.CONTENT, REF_ONE);
    }

}
