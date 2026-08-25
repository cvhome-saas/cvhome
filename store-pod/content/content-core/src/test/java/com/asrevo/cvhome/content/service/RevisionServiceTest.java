package com.asrevo.cvhome.content.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentRevision;
import com.asrevo.cvhome.content.model.page.PersistablePage;
import com.asrevo.cvhome.content.model.page.ReadablePage;
import com.asrevo.cvhome.content.repository.ContentRevisionRepository;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Per-version snapshots.
 *
 * <p>
 * The delete-then-flush is the point: Hibernate orders inserts before deletes, so re-recording the same version
 * (a restore, or a write that did not move the version) would hit {@code content_revision_unique} unless the
 * old row is flushed away first.
 * </p>
 */
class RevisionServiceTest {

    private static final String ACTOR = "ada";

    private static final String SLUG = "about-us";

    private ContentRevisionRepository repository;

    private RevisionService service;

    @BeforeEach
    void setUp() {
        repository = mock(ContentRevisionRepository.class);
        service = new RevisionService(repository);
    }

    private static Content item() {
        Content c = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        c.setVersion(3);
        return c;
    }

    private static ContentRevision revision(int version) {
        ContentRevision r = new ContentRevision();
        r.setContentId(1L);
        r.setVersion(version);
        r.setAuthor(ACTOR);
        r.setCreatedAt(ContentFixtures.NOW);
        ReadablePage page = new ReadablePage();
        page.setSlug(SLUG);
        r.setSnapshot(JsonCodec.write(page));
        return r;
    }

    @Test
    void aFirstSnapshotIsJustSaved() {
        when(repository.findByContentIdAndVersion(1L, 3)).thenReturn(Optional.empty());
        when(repository.findByContentIdOrderByVersionDesc(1L)).thenReturn(List.of());

        service.record(item(), new ReadablePage(), ACTOR);

        verify(repository, never()).delete(any());
        verify(repository).save(any());
    }

    @Test
    void rewritingTheSameVersionDeletesAndFlushesBeforeInserting() {
        ContentRevision existing = revision(3);
        when(repository.findByContentIdAndVersion(1L, 3)).thenReturn(Optional.of(existing));
        when(repository.findByContentIdOrderByVersionDesc(1L)).thenReturn(List.of());

        service.record(item(), new ReadablePage(), ACTOR);

        var order = inOrder(repository);
        order.verify(repository).delete(existing);
        order.verify(repository).flush();
        order.verify(repository).save(any());
    }

    @Test
    void onlyTheFiftyNewestSnapshotsAreKept() {
        List<ContentRevision> all = new ArrayList<>();
        for (int i = 60; i > 0; i--) {
            all.add(revision(i));
        }
        when(repository.findByContentIdAndVersion(1L, 3)).thenReturn(Optional.empty());
        when(repository.findByContentIdOrderByVersionDesc(1L)).thenReturn(all);

        service.record(item(), new ReadablePage(), ACTOR);

        verify(repository).deleteAll(all.subList(50, 60));
    }

    @Test
    void theListIsTheVersionAuthorAndWhen() {
        when(repository.findByContentIdOrderByVersionDesc(1L)).thenReturn(List.of(revision(2), revision(1)));

        assertThat(service.list(1L)).extracting("version", "author")
                .containsExactly(org.assertj.core.groups.Tuple.tuple(2, ACTOR),
                        org.assertj.core.groups.Tuple.tuple(1, ACTOR));
    }

    @Test
    void aSnapshotReplaysThroughTheSameMapperAPutUses() {
        when(repository.findByContentIdAndVersion(1L, 2)).thenReturn(Optional.of(revision(2)));

        Optional<PersistablePage> out = service.snapshot(1L, 2, PersistablePage.class);

        assertThat(out).isPresent().get().extracting(PersistablePage::getSlug).isEqualTo(SLUG);
    }

    @Test
    void aMissingSnapshotIsEmpty() {
        when(repository.findByContentIdAndVersion(1L, 9)).thenReturn(Optional.empty());

        assertThat(service.snapshot(1L, 9, PersistablePage.class)).isEmpty();
    }

    @Test
    void forgettingDropsEveryRevisionOfTheItem() {
        service.forget(1L);

        verify(repository).deleteByContentId(1L);
    }

}
