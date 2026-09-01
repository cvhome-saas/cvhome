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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The layout lifecycle: a row materializes from the starter default on first touch, saves are optimistic,
 * publish is an atomic copy plus a snapshot, and the storefront never sees an unpublished draft.
 */
class PageLayoutServiceTest {
    private static final String HOME = "HOME";
    private static final String LIVE = "live";
    private static final String SEC_IMG = "sec-img";
    private static final String IMAGE = "image";
    private static final String MEDIA_ID = "mediaId";
    private static final String SECRET_DRAFT = "secret draft";
    private static final String OWNER_REF = "41";
    private static final String BODY = "body";
    private static final String EN = "en";
    private static final String OLD = "old";
    private static final String THE_PAST = "the past";


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
        when(layouts.findByStoreMerchantIdAndPage(ContentFixtures.STORE.getId(), HOME))
                .thenReturn(Optional.of(row));
        return row;
    }

    private static LayoutDocument doc(String heading) {
        return new LayoutDocument(LayoutDocument.CURRENT_SCHEMA_VERSION, PageKind.HOME, List.of(
                new LayoutSection("sec-1", "hero", "minimal", Map.of(), null,
                        Map.of("heading", Map.of(EN, heading)), null, null, null, null)));
    }

    @Test
    void firstTouchMaterializesTheStarterDefault() {
        when(layouts.findByStoreMerchantIdAndPage(ContentFixtures.STORE.getId(), HOME))
                .thenReturn(Optional.empty());

        ReadableLayout layout = service.get(ContentFixtures.STORE, PageKind.HOME);

        verify(layouts).save(any());
        assertThat(layout.draft().sections()).isNotEmpty();
        assertThat(layout.meta().draftVersion()).isEqualTo(1);
        assertThat(layout.meta().dirty()).isTrue();
    }

    @Test
    void aStaleSaveIsAConflictNotAClobber() {
        existing(JsonCodec.write(doc(LIVE)), null, 4);

        assertThatThrownBy(() -> service.save(ContentFixtures.STORE, PageKind.HOME,
                new PersistableLayout(doc("mine"), 3), ACTOR))
                .isInstanceOf(ContentConflictException.class);
    }

    @Test
    void aSaveBumpsTheVersionAndReindexesMediaUsage() throws Exception {
        PageLayout row = existing(JsonCodec.write(doc(OLD)), null, 1);
        LayoutDocument next = new LayoutDocument(LayoutDocument.CURRENT_SCHEMA_VERSION, PageKind.HOME, List.of(
                new LayoutSection(SEC_IMG, IMAGE, "contained", Map.of(MEDIA_ID, 9), null, null, null,
                        null, null, null)));

        ReadableLayout saved = service.save(ContentFixtures.STORE, PageKind.HOME,
                new PersistableLayout(next, 1), ACTOR);

        assertThat(saved.meta().draftVersion()).isEqualTo(2);
        assertThat(row.getModifiedBy()).isEqualTo(ACTOR);
        verify(usage).replace(eq(ContentFixtures.STORE), eq(MediaOwnerKind.LAYOUT), eq(OWNER_REF), any(), any(),
                any(), eq(Map.of(SEC_IMG, 9L)));
    }

    @Test
    void publishCopiesTheDraftAndSnapshotsARevision() throws Exception {
        PageLayout row = existing(JsonCodec.write(doc("next")), JsonCodec.write(doc(LIVE)), 3);
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
                List.of(new LayoutSection(SEC_IMG, IMAGE, null, Map.of(MEDIA_ID, 404), null, null, null,
                        null, null, null)));
        existing(JsonCodec.write(withMedia), null, 1);
        when(media.urls(eq(ContentFixtures.STORE), anyList())).thenReturn(Map.of());

        assertThatThrownBy(() -> service.publish(ContentFixtures.STORE, PageKind.HOME, 1, ACTOR))
                .isInstanceOf(ContentRuleException.class);
    }

    @Test
    void aRepeatPublishOfTheSameDraftIsANoOpNotADuplicateRevision() throws Exception {
        PageLayout row = existing(JsonCodec.write(doc(LIVE)), JsonCodec.write(doc(LIVE)), 3);
        row.setPublishedVersion(3);

        var result = service.publish(ContentFixtures.STORE, PageKind.HOME, 3, ACTOR);

        assertThat(result.meta().publishedVersion()).isEqualTo(3);
        verify(revisions, never()).save(any());
    }

    @Test
    void theUsageIndexGuardsThePublishedDocumentTooNotJustTheDraft() throws Exception {
        LayoutDocument live = new LayoutDocument(LayoutDocument.CURRENT_SCHEMA_VERSION, PageKind.HOME,
                List.of(new LayoutSection(SEC_IMG, IMAGE, null, Map.of(MEDIA_ID, 7), null, null, null,
                        null, null, null)));
        existing(JsonCodec.write(doc(OLD)), JsonCodec.write(live), 1);

        service.save(ContentFixtures.STORE, PageKind.HOME, new PersistableLayout(doc("no media"), 1), ACTOR);

        // the draft dropped the image, but the live page still renders it — its reference must survive
        verify(usage).replace(eq(ContentFixtures.STORE), eq(MediaOwnerKind.LAYOUT), eq(OWNER_REF), any(), any(),
                any(), eq(Map.of(String.format("published/%s", SEC_IMG), 7L)));
    }

    @Test
    void aRichtextBodyIsSanitizedOnSave() throws Exception {
        existing(JsonCodec.write(doc(OLD)), null, 1);
        LayoutDocument dirty = new LayoutDocument(LayoutDocument.CURRENT_SCHEMA_VERSION, PageKind.HOME, List.of(
                new LayoutSection("sec-story", "richtext", "centered", Map.of(), null,
                        Map.of(BODY, Map.of(EN, "<p>fine</p><script>alert(1)</script>")),
                        null, null, null, null)));

        ReadableLayout saved = service.save(ContentFixtures.STORE, PageKind.HOME,
                new PersistableLayout(dirty, 1), ACTOR);

        String body = saved.draft().sections().getFirst().text().get(BODY).get(EN);
        assertThat(body).contains("<p>fine</p>").doesNotContain("script");
    }

    @Test
    void theStorefrontServesTheDefaultUntilFirstPublishAndNeverTheDraft() {
        existing(JsonCodec.write(doc(SECRET_DRAFT)), null, 2);

        LayoutDocument served = service.served(ContentFixtures.STORE, PageKind.HOME, false);

        assertThat(JsonCodec.write(served)).doesNotContain(SECRET_DRAFT);
        assertThat(served.sections()).isNotEmpty();
    }

    @Test
    void restoreMaterializesTheRevisionIntoTheDraft() throws Exception {
        PageLayout row = existing(JsonCodec.write(doc("current")), JsonCodec.write(doc(LIVE)), 4);
        PageLayoutRevision revision = new PageLayoutRevision();
        revision.setLayoutId(41L);
        revision.setVersion(2);
        revision.setSnapshot(JsonCodec.write(doc(THE_PAST)));
        when(revisions.findByLayoutIdAndVersion(41L, 2)).thenReturn(Optional.of(revision));

        ReadableLayout restored = service.restore(ContentFixtures.STORE, PageKind.HOME, 2, ACTOR);

        assertThat(restored.meta().draftVersion()).isEqualTo(5);
        assertThat(row.getDraft()).contains(THE_PAST);
        assertThat(restored.meta().dirty()).isTrue();
    }

    @Test
    void restoreOfAMissingRevisionIsNotFound() {
        existing(JsonCodec.write(doc(LIVE)), null, 1);
        when(revisions.findByLayoutIdAndVersion(41L, 9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.restore(ContentFixtures.STORE, PageKind.HOME, 9, ACTOR))
                .isInstanceOf(com.asrevo.cvhome.content.errors.ContentNotFoundException.class);
    }

    @Test
    void theBuilderPreviewServesTheDraftAndTheRevisionListReadsNewestFirst() {
        existing(JsonCodec.write(doc(SECRET_DRAFT)), JsonCodec.write(doc(LIVE)), 2);
        when(revisions.findByLayoutIdOrderByVersionDesc(41L)).thenReturn(List.of());

        LayoutDocument draft = service.served(ContentFixtures.STORE, PageKind.HOME, true);
        assertThat(JsonCodec.write(draft)).contains(SECRET_DRAFT);
        assertThat(service.revisions(ContentFixtures.STORE, PageKind.HOME)).isEmpty();
    }

    @Test
    void discardReturnsToThePublishedDocument() throws Exception {
        PageLayout row = existing(JsonCodec.write(doc("draft")), JsonCodec.write(doc(LIVE)), 5);

        service.discard(ContentFixtures.STORE, PageKind.HOME, 5, ACTOR);

        assertThat(row.getDraft()).isEqualTo(row.getPublished());
        assertThat(row.getDraftVersion()).isEqualTo(6);
    }

}
