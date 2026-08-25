package com.asrevo.cvhome.content.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.model.BulkAction;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.TranslationState;
import com.asrevo.cvhome.content.model.common.BulkRequest;
import com.asrevo.cvhome.content.model.common.BulkResult;
import com.asrevo.cvhome.content.model.common.ContentTranslation;
import com.asrevo.cvhome.content.model.common.ReadableContentRowList;
import com.asrevo.cvhome.content.model.common.SavedContent;
import com.asrevo.cvhome.content.model.page.PersistablePage;
import com.asrevo.cvhome.content.model.page.ReadablePage;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.repository.ContentStatusAuditRepository;
import com.asrevo.cvhome.content.service.binding.PageBinding;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Everything a workflow item does that does not depend on its type. The rules pinned here: a slug is unique per
 * store, a stale version is a 409, an item of another type (or store) reads as missing, a published item whose
 * slug moved leaves a redirect behind, and bulk never fails the batch on one bad id.
 */
class ContentItemServiceTest {

    private static final String MODIFIED = "auditSection.dateModified";

    private static final String CODE_COLUMN = "code";

    private static final String STATUS = "status";

    private static final String ARABIC_TITLE = "عن";

    private static final String ARABIC_BODY = "<p>نص</p>";

    private static final String STATE = "state";

    private static final String SLUG = "about-us";

    private static final String NEW_SLUG = "about";

    private static final String TITLE = "About us";

    private static final String BODY = "<p>body</p>";

    private static final String ACTOR = "ada";

    private ContentRepository repository;

    private ContentStatusAuditRepository audits;

    private PublishingService publishing;

    private RevisionService revisions;

    private RedirectService redirects;

    private MediaUsageTracker mediaUsage;

    private PageBinding binding;

    private ContentItemService service;

    @BeforeEach
    void setUp() {
        repository = mock(ContentRepository.class);
        audits = mock(ContentStatusAuditRepository.class);
        publishing = mock(PublishingService.class);
        revisions = mock(RevisionService.class);
        redirects = mock(RedirectService.class);
        mediaUsage = mock(MediaUsageTracker.class);
        binding = new PageBinding();
        service = new ContentItemService(repository, audits, publishing, revisions, redirects, mediaUsage,
                ContentFixtures.clock());
    }

    private static PersistablePage dto(String slug) {
        PersistablePage p = new PersistablePage();
        p.setSlug(slug);
        p.setTranslations(List.of(ContentFixtures.translation(ContentFixtures.EN, TITLE, BODY)));
        return p;
    }

    private void savesInPlace() {
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(repository.findById(anyLong())).thenAnswer(i -> Optional.empty());
    }

    @Test
    void theClockIsTheOneItWasBuiltWith() {
        assertThat(service.clock().instant()).isEqualTo(ContentFixtures.NOW);
    }

    @Nested
    class Sorting {

        @Test
        void anUnsortedRequestFallsBackToMostRecentlyModified() {
            Pageable mapped = ContentItemService.mapSort(PageRequest.of(1, 20));

            assertThat(mapped.getSort()).containsExactly(
                    new Sort.Order(Sort.Direction.DESC, MODIFIED));
            assertThat(mapped.getPageNumber()).isEqualTo(1);
        }

        @Test
        void titleAndSlugBothSortByTheSlugColumn() {
            assertThat(ContentItemService.mapSort(PageRequest.of(0, 20, Sort.by("title"))).getSort())
                    .containsExactly(new Sort.Order(Sort.Direction.ASC, CODE_COLUMN));
            assertThat(ContentItemService.mapSort(PageRequest.of(0, 20, Sort.by("slug"))).getSort())
                    .containsExactly(new Sort.Order(Sort.Direction.ASC, CODE_COLUMN));
        }

        @Test
        void anUnknownSortKeyFallsBackRatherThanFailing() {
            assertThat(ContentItemService.mapSort(PageRequest.of(0, 20, Sort.by("colour"))).getSort())
                    .containsExactly(new Sort.Order(Sort.Direction.ASC, MODIFIED));
        }

        @Test
        void thePassThroughKeysKeepTheirName() {
            assertThat(ContentItemService.mapSort(PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, STATUS)))
                    .getSort()).containsExactly(new Sort.Order(Sort.Direction.DESC, STATUS));
            assertThat(ContentItemService.mapSort(PageRequest.of(0, 20, Sort.by("createdAt"))).getSort())
                    .containsExactly(new Sort.Order(Sort.Direction.ASC, "auditSection.dateCreated"));
        }

    }

    @Nested
    class Reads {

        @Test
        void theListCarriesThePagingFiguresAndOneRowPerItem() {
            Content item = ContentFixtures.published(1L, ContentType.PAGE, SLUG, TITLE);
            Page<Content> page = new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1);
            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            ReadableContentRowList out = service.list(binding, ContentFixtures.STORE, ContentFixtures.EN,
                    ListQuery.none(), PageRequest.of(0, 20));

            assertThat(out.getTotalElements()).isEqualTo(1);
            assertThat(out.getTotalPages()).isEqualTo(1);
            assertThat(out.getSize()).isEqualTo(1);
            assertThat(out.getPageNumber()).isZero();
            assertThat(out.getContent()).singleElement()
                    .satisfies(r -> assertThat(r.getSubtitle()).isEqualTo("/about-us"));
        }

        @Test
        void anItemOfAnotherStoreOrAnotherTypeReadsAsMissing() {
            when(repository.findByIdAndStore(1L, ContentFixtures.STORE)).thenReturn(Optional.empty());
            when(repository.findByIdAndStore(2L, ContentFixtures.STORE))
                    .thenReturn(Optional.of(ContentFixtures.published(2L, ContentType.POST, SLUG, TITLE)));

            assertThatThrownBy(() -> service.get(binding, 1L, ContentFixtures.STORE))
                    .isInstanceOf(ContentNotFoundException.class);
            assertThatThrownBy(() -> service.get(binding, 2L, ContentFixtures.STORE))
                    .isInstanceOf(ContentNotFoundException.class);
        }

        @Test
        void aReadPopulatesBothTheCommonAndTheTypeSpecificHalf() throws Exception {
            Content item = ContentFixtures.published(1L, ContentType.PAGE, SLUG, TITLE);
            item.setShowInFooter(true);
            when(repository.findByIdAndStore(1L, ContentFixtures.STORE)).thenReturn(Optional.of(item));

            ReadablePage out = service.get(binding, 1L, ContentFixtures.STORE);

            assertThat(out.getSlug()).isEqualTo(SLUG);
            assertThat(out.isShowInFooter()).isTrue();
            assertThat(out.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
            assertThat(out.getTranslations()).hasSize(1);
        }

        @Test
        void slugAvailabilityAsksTheRightQuestionForCreateAndForEdit() {
            when(repository.existsByStoreMerchantIdAndCode(ContentFixtures.STORE, SLUG)).thenReturn(true);
            when(repository.existsByStoreMerchantIdAndCodeAndIdNot(ContentFixtures.STORE, SLUG, 1L))
                    .thenReturn(false);

            assertThat(service.slugAvailable(ContentFixtures.STORE, SLUG, null)).isFalse();
            assertThat(service.slugAvailable(ContentFixtures.STORE, SLUG, 1L)).isTrue();
        }

        @Test
        void revisionsAreOnlyListedForAnItemThisStoreOwns() throws Exception {
            when(repository.findByIdAndStore(1L, ContentFixtures.STORE))
                    .thenReturn(Optional.of(ContentFixtures.published(1L, ContentType.PAGE, SLUG, TITLE)));
            when(revisions.list(1L)).thenReturn(List.of());

            assertThat(service.revisions(binding, 1L, ContentFixtures.STORE)).isEmpty();
            verify(revisions).list(1L);
        }

    }

    @Nested
    class Writes {

        @Test
        void aDuplicateSlugIsRefusedOnCreate() {
            when(repository.existsByStoreMerchantIdAndCode(ContentFixtures.STORE, SLUG)).thenReturn(true);

            assertThatThrownBy(() -> service.create(binding, dto(SLUG), ContentFixtures.STORE, ContentFixtures.EN,
                    ACTOR)).isInstanceOf(ContentConflictException.class);
        }

        @Test
        void aNewItemStartsAsAnInvisibleDraftAndIsSnapshotted() throws Exception {
            when(repository.existsByStoreMerchantIdAndCode(ContentFixtures.STORE, SLUG)).thenReturn(false);
            when(repository.saveAndFlush(any())).thenAnswer(i -> {
                Content c = i.getArgument(0);
                c.setId(1L);
                return c;
            });

            SavedContent saved = service.create(binding, dto(SLUG), ContentFixtures.STORE, ContentFixtures.EN,
                    ACTOR);

            assertThat(saved.getStatus()).isEqualTo(ContentStatus.DRAFT);
            assertThat(saved.getId()).isEqualTo(1L);
            verify(revisions).record(any(), any(), eq(ACTOR));
            verify(mediaUsage).record(any(), any());
        }

        @Test
        void theOgImageJoinsTheMediaUsageIndex() throws Exception {
            when(repository.existsByStoreMerchantIdAndCode(ContentFixtures.STORE, SLUG)).thenReturn(false);
            when(repository.saveAndFlush(any())).thenAnswer(i -> {
                Content c = i.getArgument(0);
                c.setId(1L);
                return c;
            });
            PersistablePage body = dto(SLUG);
            body.setOgMediaId(9L);

            service.create(binding, body, ContentFixtures.STORE, ContentFixtures.EN, ACTOR);

            var refs = org.mockito.ArgumentCaptor.forClass(java.util.Map.class);
            verify(mediaUsage).record(any(), refs.capture());
            assertThat((java.util.Map<String, Long>) refs.getValue()).containsEntry("og", 9L);
        }

        @Test
        void aStaleVersionIsAConflict() {
            Content item = ContentFixtures.published(1L, ContentType.PAGE, SLUG, TITLE);
            item.setVersion(3);
            when(repository.findByIdAndStore(1L, ContentFixtures.STORE)).thenReturn(Optional.of(item));
            PersistablePage body = dto(SLUG);
            body.setVersion(2);

            assertThatThrownBy(() -> service.update(binding, 1L, body, ContentFixtures.STORE, ContentFixtures.EN,
                    ACTOR)).isInstanceOf(ContentConflictException.class);
        }

        @Test
        void movingOntoAnotherItemsSlugIsAConflict() {
            Content item = ContentFixtures.published(1L, ContentType.PAGE, SLUG, TITLE);
            when(repository.findByIdAndStore(1L, ContentFixtures.STORE)).thenReturn(Optional.of(item));
            when(repository.existsByStoreMerchantIdAndCodeAndIdNot(ContentFixtures.STORE, NEW_SLUG, 1L))
                    .thenReturn(true);

            assertThatThrownBy(() -> service.update(binding, 1L, dto(NEW_SLUG), ContentFixtures.STORE,
                    ContentFixtures.EN, ACTOR)).isInstanceOf(ContentConflictException.class);
        }

        @Test
        void aPublishedItemThatMovesLeavesARedirectBehind() throws Exception {
            Content item = ContentFixtures.published(1L, ContentType.PAGE, SLUG, TITLE);
            when(repository.findByIdAndStore(1L, ContentFixtures.STORE)).thenReturn(Optional.of(item));
            when(repository.existsByStoreMerchantIdAndCodeAndIdNot(ContentFixtures.STORE, NEW_SLUG, 1L))
                    .thenReturn(false);
            savesInPlace();

            service.update(binding, 1L, dto(NEW_SLUG), ContentFixtures.STORE, ContentFixtures.EN, ACTOR);

            verify(redirects).moved(ContentFixtures.STORE, "/content/about-us", "/content/about");
            verify(repository).touch(eq(1L), eq(ContentFixtures.NOW), eq(ACTOR));
        }

        @Test
        void aDraftThatMovesLeavesNoRedirect() throws Exception {
            Content item = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
            when(repository.findByIdAndStore(1L, ContentFixtures.STORE)).thenReturn(Optional.of(item));
            savesInPlace();

            service.update(binding, 1L, dto(NEW_SLUG), ContentFixtures.STORE, ContentFixtures.EN, ACTOR);

            verify(redirects, never()).moved(any(), anyString(), anyString());
        }

        @Test
        void rewritingAPublishedSourceLocaleMarksTheOthersStale() throws Exception {
            Content item = ContentFixtures.published(1L, ContentType.PAGE, SLUG, TITLE);
            item.getDescriptions().getFirst().setId(10L);
            item.getDescriptions().add(ContentFixtures.description(item, ContentFixtures.AR, ARABIC_TITLE, ARABIC_BODY));
            item.getDescriptions().getLast().setId(11L);
            when(repository.findByIdAndStore(1L, ContentFixtures.STORE)).thenReturn(Optional.of(item));
            savesInPlace();
            PersistablePage body = dto(SLUG);
            body.setTranslations(List.of(ContentFixtures.translation(ContentFixtures.EN, TITLE, "<p>rewritten</p>"),
                    ContentFixtures.translation(ContentFixtures.AR, ARABIC_TITLE, ARABIC_BODY)));

            service.update(binding, 1L, body, ContentFixtures.STORE, ContentFixtures.EN, ACTOR);

            assertThat(item.description(ContentFixtures.AR)).get()
                    .extracting(STATE).isEqualTo(TranslationState.STALE);
            assertThat(item.description(ContentFixtures.EN)).get()
                    .extracting(STATE).isEqualTo(TranslationState.TRANSLATED);
        }

        @Test
        void oneLocaleCanBeWrittenWithoutTouchingTheOthers() throws Exception {
            Content item = ContentFixtures.published(1L, ContentType.PAGE, SLUG, TITLE);
            when(repository.findByIdAndStore(1L, ContentFixtures.STORE)).thenReturn(Optional.of(item));
            savesInPlace();
            ContentTranslation arabic = ContentFixtures.translation(null, ARABIC_TITLE, ARABIC_BODY);

            service.updateTranslation(binding, 1L, ContentFixtures.AR, arabic, ContentFixtures.STORE, ACTOR);

            assertThat(item.getDescriptions()).extracting("languageCode")
                    .containsExactlyInAnyOrder(ContentFixtures.EN, ContentFixtures.AR);
        }

        @Test
        void aTransitionDelegatesToThePublishingService() throws Exception {
            Content item = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
            when(repository.findByIdAndStore(1L, ContentFixtures.STORE)).thenReturn(Optional.of(item));
            savesInPlace();

            service.transition(binding, 1L, ContentFixtures.STORE, ContentStatus.PUBLISHED, null,
                    ContentFixtures.EN, ACTOR);

            verify(publishing).transition(eq(item), eq(ContentStatus.PUBLISHED), eq(null), eq(ContentFixtures.EN),
                    eq(binding), eq(ACTOR), eq(null));
        }

        @Test
        void restoringAVersionThatWasNeverRecordedReadsAsMissing() {
            Content item = ContentFixtures.published(1L, ContentType.PAGE, SLUG, TITLE);
            when(repository.findByIdAndStore(1L, ContentFixtures.STORE)).thenReturn(Optional.of(item));
            when(revisions.snapshot(1L, 2, PersistablePage.class)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.restore(binding, 1L, 2, ContentFixtures.STORE, ContentFixtures.EN,
                    ACTOR)).isInstanceOf(ContentNotFoundException.class);
        }

        @Test
        void restoringReplaysTheSnapshotAtTheCurrentVersion() throws Exception {
            Content item = ContentFixtures.published(1L, ContentType.PAGE, SLUG, TITLE);
            item.setVersion(5);
            when(repository.findByIdAndStore(1L, ContentFixtures.STORE)).thenReturn(Optional.of(item));
            PersistablePage snapshot = dto(SLUG);
            snapshot.setVersion(2);
            when(revisions.snapshot(1L, 2, PersistablePage.class)).thenReturn(Optional.of(snapshot));
            savesInPlace();

            service.restore(binding, 1L, 2, ContentFixtures.STORE, ContentFixtures.EN, ACTOR);

            assertThat(snapshot.getVersion()).isEqualTo(5);
        }

        @Test
        void deletingAnItemClearsItsRevisionsAuditRowsAndMediaUsage() throws Exception {
            Content item = ContentFixtures.published(1L, ContentType.PAGE, SLUG, TITLE);
            when(repository.findByIdAndStore(1L, ContentFixtures.STORE)).thenReturn(Optional.of(item));

            service.delete(binding, 1L, ContentFixtures.STORE, false);

            verify(revisions).forget(1L);
            verify(audits).deleteByContentId(1L);
            verify(mediaUsage).forget(item);
            verify(repository).delete(item);
        }

    }

    @Nested
    class Bulk {

        private BulkRequest request(BulkAction action, List<Long> ids) {
            BulkRequest r = new BulkRequest();
            r.setAction(action);
            r.setIds(ids);
            return r;
        }

        @Test
        void tooManyIdsIsRejectedOutright() {
            List<Long> ids = java.util.stream.LongStream.rangeClosed(1, BulkRequest.MAX_IDS + 1L)
                    .boxed().toList();

            assertThatThrownBy(() -> service.bulk(binding, request(BulkAction.PUBLISH, ids), ContentFixtures.STORE,
                    ContentFixtures.EN, ACTOR)).isInstanceOf(InvalidContentRequestException.class);
        }

        @Test
        void oneBadIdDoesNotFailTheBatch() throws Exception {
            Content ok = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
            when(repository.findByIdAndStore(1L, ContentFixtures.STORE)).thenReturn(Optional.of(ok));
            when(repository.findByIdAndStore(2L, ContentFixtures.STORE)).thenReturn(Optional.empty());
            savesInPlace();

            List<BulkResult> results = service.bulk(binding, request(BulkAction.PUBLISH, List.of(1L, 2L)),
                    ContentFixtures.STORE, ContentFixtures.EN, ACTOR);

            assertThat(results).extracting(BulkResult::isOk).containsExactly(true, false);
            assertThat(results.getLast().getErrorCode()).isEqualTo("CONTENT.NOT_FOUND");
        }

        @Test
        void everyBulkActionMapsToItsTransitionOrDelete() throws Exception {
            Content item = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
            when(repository.findByIdAndStore(1L, ContentFixtures.STORE)).thenReturn(Optional.of(item));
            savesInPlace();

            service.bulk(binding, request(BulkAction.UNPUBLISH, List.of(1L)), ContentFixtures.STORE,
                    ContentFixtures.EN, ACTOR);
            service.bulk(binding, request(BulkAction.ARCHIVE, List.of(1L)), ContentFixtures.STORE,
                    ContentFixtures.EN, ACTOR);
            service.bulk(binding, request(BulkAction.DELETE, List.of(1L)), ContentFixtures.STORE,
                    ContentFixtures.EN, ACTOR);

            verify(publishing).transition(eq(item), eq(ContentStatus.DRAFT), any(), any(), any(), any(), any());
            verify(publishing).transition(eq(item), eq(ContentStatus.ARCHIVED), any(), any(), any(), any(), any());
            verify(repository).delete(item);
        }

    }

}
