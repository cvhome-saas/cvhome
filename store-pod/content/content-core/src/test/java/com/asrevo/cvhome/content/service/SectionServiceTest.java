package com.asrevo.cvhome.content.service;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The home page's order.
 *
 * <p>
 * The whole order arrives in one request and every section is renumbered {@code 0..n}. Accepting one move at a
 * time would be the smaller API and the wrong one: two concurrent moves, or one request lost in the middle of a
 * drag, leave gaps and ties, and a tie is resolved by whatever the storefront's sort happens to do — which is to
 * say, arbitrarily, and differently on different reads.
 * </p>
 */
class SectionServiceTest {

    private ContentRepository contents;

    private SectionService service;

    @BeforeEach
    void setUp() {
        contents = mock(ContentRepository.class);
        service = new SectionService(contents);
    }

    private static Content section(long id, int sortOrder) {
        Content c = ContentFixtures.published(id, ContentType.SECTION, String.format("s-%d", id), "Section");
        c.setSortOrder(sortOrder);
        return c;
    }

    private void stored(Content... sections) {
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.SECTION)).thenReturn(List.of(sections));
    }

    @Test
    void theNamedOrderIsWrittenAsZeroToN() throws ContentNotFoundException {
        Content first = section(1L, 0);
        Content second = section(2L, 1);
        Content third = section(3L, 2);
        stored(first, second, third);

        service.reorder(ContentFixtures.STORE, List.of(3L, 1L, 2L));

        assertThat(third.getSortOrder()).isZero();
        assertThat(first.getSortOrder()).isEqualTo(1);
        assertThat(second.getSortOrder()).isEqualTo(2);
    }

    /**
     * Positions are rewritten even when the sequence is unchanged, which is what repairs a page that already had
     * gaps or ties in it — from a seed, or from a half-applied reorder written before this endpoint existed.
     */
    @Test
    void reorderingClosesGapsLeftBehindByAnythingElse() throws ContentNotFoundException {
        Content first = section(1L, 5);
        Content second = section(2L, 5);
        stored(first, second);

        service.reorder(ContentFixtures.STORE, List.of(1L, 2L));

        assertThat(first.getSortOrder()).isZero();
        assertThat(second.getSortOrder()).isEqualTo(1);
    }

    /**
     * A caller that names only part of the page — an older console, or a request built while a section was being
     * created in another tab — does not silently drop the rest. They keep their relative order behind what was
     * named, which is the reading that loses the least.
     */
    @Test
    void sectionsTheCallerDidNotNameKeepTheirOrderAfterTheOnesItDid() throws ContentNotFoundException {
        Content named = section(1L, 0);
        Content firstUnnamed = section(2L, 1);
        Content secondUnnamed = section(3L, 2);
        stored(named, firstUnnamed, secondUnnamed);

        service.reorder(ContentFixtures.STORE, List.of(1L));

        assertThat(named.getSortOrder()).isZero();
        assertThat(firstUnnamed.getSortOrder()).isEqualTo(1);
        assertThat(secondUnnamed.getSortOrder()).isEqualTo(2);
    }

    @Test
    void anIdFromAnotherStoreOrAlreadyDeletedIsRefused() {
        stored(section(1L, 0));

        assertThatThrownBy(() -> service.reorder(ContentFixtures.STORE, List.of(1L, 99L)))
                .isInstanceOf(ContentNotFoundException.class);
    }

    /**
     * The same id twice would otherwise renumber one section into two positions and quietly consume the slot the
     * next one wanted. The second mention finds nothing left under that id and is refused.
     */
    @Test
    void theSameSectionNamedTwiceIsRefused() {
        stored(section(1L, 0), section(2L, 1));

        assertThatThrownBy(() -> service.reorder(ContentFixtures.STORE, List.of(1L, 1L)))
                .isInstanceOf(ContentNotFoundException.class);
    }

    @Test
    void anEmptyOrderStillRenumbersWhatIsThere() throws ContentNotFoundException {
        Content only = section(1L, 9);
        stored(only);

        service.reorder(ContentFixtures.STORE, List.of());

        assertThat(only.getSortOrder()).isZero();
        verify(contents).save(only);
    }

    @Test
    void aPageWithNoSectionsSavesNothing() throws ContentNotFoundException {
        stored();

        service.reorder(ContentFixtures.STORE, List.of());

        verify(contents, never()).save(any());
    }

}
