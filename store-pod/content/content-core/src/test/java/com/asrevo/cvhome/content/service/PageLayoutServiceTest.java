package com.asrevo.cvhome.content.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.PageLayout;
import com.asrevo.cvhome.content.entity.PageLayoutRevision;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentRuleException;
import com.asrevo.cvhome.content.model.MediaOwnerKind;
import com.asrevo.cvhome.content.model.layout.LayoutDocument;
import com.asrevo.cvhome.content.model.layout.LayoutSection;
import com.asrevo.cvhome.content.model.layout.PageKind;
import com.asrevo.cvhome.content.model.layout.PersistableLayout;
import com.asrevo.cvhome.content.model.layout.ReadableLayout;
import com.asrevo.cvhome.content.repository.PageLayoutRepository;
import com.asrevo.cvhome.content.repository.PageLayoutRevisionRepository;
import com.asrevo.cvhome.content.support.JsonCodec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The layout lifecycle: a row materializes from the starter default on first touch, saves are optimistic,
 * publish is an atomic copy plus a snapshot, and the storefront never sees an unpublished draft.
 */
class PageLayoutServiceTest {

    private static final String ACTOR = "tester";

    private PageLayoutRepository layouts;

    private PageLayoutRevisionRepository revisions;

    private MediaService media;

    private MediaUsageTracker usage;

    private PageLayoutService service;

    @BeforeEach
    void setUp() {
        layouts = mock(PageLayoutRepository.class);
        revisions = mock(PageLayoutRevisionRepository.class);
        media = mock(MediaService.class);
        usage = mock(MediaUsageTracker.class);
        when(layouts.save(any())).thenAnswer(inv -> inv.getArgument(0));
        service = new PageLayoutService(layouts, revisions, media, usage,
                Clock.fixed(Instant.parse("2026-08-31T12:00:00Z"), ZoneOffset.UTC));
    }

    private PageLayout existing(String draft, String published, int draftVersion) {
        PageLayout row = new PageLayout();
        row.setId(41L);
        row.setStoreMerchantId(ContentFixtures.STORE.getId());
        row.setPage(PageKind.HOME.name());
        row.setDraft(draft);
        row.setPublished(published);
        row.setDraftVersion(draftVersion);
        when(layouts.findByStoreMerchantIdAndPage(ContentFixtures.STORE.getId(), "HOME"))
                .thenReturn(Optional.of(row));
        return row;
    }

    private static LayoutDocument doc(String heading) {
        return new LayoutDocument(LayoutDocument.CURRENT_SCHEMA_VERSION, PageKind.HOME, List.of(
                new LayoutSection("sec-1", "hero", "minimal", Map.of(), null,
                        Map.of("heading", Map.of("en", heading)), null, null, null)));
    }

    @Test
    void firstTouchMaterializesTheStarterDefault() {
        when(layouts.findByStoreMerchantIdAndPage(ContentFixtures.STORE.getId(), "HOME"))
                .thenReturn(Optional.empty());

        ReadableLayout layout = service.get(ContentFixtures.STORE, PageKind.HOME);

        verify(layouts).save(any());
        assertThat(layout.draft().sections()).isNotEmpty();
        assertThat(layout.meta().draftVersion()).isEqualTo(1);
        assertThat(layout.meta().dirty()).isTrue();
    }

    @Test
    void aStaleSaveIsAConflictNotAClobber() {
        existing(JsonCodec.write(doc("live")), null, 4);

        assertThatThrownBy(() -> service.save(ContentFixtures.STORE, PageKind.HOME,
                new PersistableLayout(doc("mine"), 3), ACTOR))
                .isInstanceOf(ContentConflictException.class);
    }

    @Test
    void aSaveBumpsTheVersionAndReindexesMediaUsage() throws Exception {
        PageLayout row = existing(JsonCodec.write(doc("old")), null, 1);
        LayoutDocument next = new LayoutDocument(LayoutDocument.CURRENT_SCHEMA_VERSION, PageKind.HOME, List.of(
                new LayoutSection("sec-img", "image", "contained", Map.of("mediaId", 9), null, null, null,
                        null, null)));

        ReadableLayout saved = service.save(ContentFixtures.STORE, PageKind.HOME,
                new PersistableLayout(next, 1), ACTOR);

        assertThat(saved.meta().draftVersion()).isEqualTo(2);
        assertThat(row.getModifiedBy()).isEqualTo(ACTOR);
        verify(usage).replace(eq(ContentFixtures.STORE), eq(MediaOwnerKind.LAYOUT), eq("41"), any(), any(),
                any(), eq(Map.of("sec-img", 9L)));
    }

    @Test
    void publishCopiesTheDraftAndSnapshotsARevision() throws Exception {
        PageLayout row = existing(JsonCodec.write(doc("next")), JsonCodec.write(doc("live")), 3);
        when(media.urls(eq(ContentFixtures.STORE), anyList())).thenReturn(Map.of());

        var result = service.publish(ContentFixtures.STORE, PageKind.HOME, 3, ACTOR);

        assertThat(row.getPublished()).isEqualTo(row.getDraft());
        assertThat(row.getPublishedVersion()).isEqualTo(3);
        assertThat(result.meta().dirty()).isFalse();
        ArgumentCaptor<PageLayoutRevision> captor = ArgumentCaptor.forClass(PageLayoutRevision.class);
        verify(revisions).save(captor.capture());
        assertThat(captor.getValue().getVersion()).isEqualTo(3);
        assertThat(captor.getValue().getSnapshot()).isEqualTo(row.getPublished());
    }

    @Test
    void publishBlocksOnAMediaReferenceTheLibraryDoesNotHold() {
        LayoutDocument withMedia = new LayoutDocument(LayoutDocument.CURRENT_SCHEMA_VERSION, PageKind.HOME,
                List.of(new LayoutSection("sec-img", "image", null, Map.of("mediaId", 404), null, null, null,
                        null, null)));
        existing(JsonCodec.write(withMedia), null, 1);
        when(media.urls(eq(ContentFixtures.STORE), anyList())).thenReturn(Map.of());

        assertThatThrownBy(() -> service.publish(ContentFixtures.STORE, PageKind.HOME, 1, ACTOR))
                .isInstanceOf(ContentRuleException.class);
    }

    @Test
    void theStorefrontServesTheDefaultUntilFirstPublishAndNeverTheDraft() {
        existing(JsonCodec.write(doc("secret draft")), null, 2);

        LayoutDocument served = service.served(ContentFixtures.STORE, PageKind.HOME, false);

        assertThat(JsonCodec.write(served)).doesNotContain("secret draft");
        assertThat(served.sections()).isNotEmpty();
    }

    @Test
    void discardReturnsToThePublishedDocument() throws Exception {
        PageLayout row = existing(JsonCodec.write(doc("draft")), JsonCodec.write(doc("live")), 5);

        service.discard(ContentFixtures.STORE, PageKind.HOME, 5, ACTOR);

        assertThat(row.getDraft()).isEqualTo(row.getPublished());
        assertThat(row.getDraftVersion()).isEqualTo(6);
    }

}
