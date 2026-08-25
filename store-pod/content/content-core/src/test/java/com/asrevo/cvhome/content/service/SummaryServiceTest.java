package com.asrevo.cvhome.content.service;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.TranslationState;
import com.asrevo.cvhome.content.model.summary.ContentSummary;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.service.binding.PageBinding;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The hub's KPI cards. The "awaiting translation" figure counts items once but breaks the total down per locale,
 * which is why a two-locale item shows up in two buckets.
 */
class SummaryServiceTest {

    private static final String MEDIA_KEY = "media";

    private static final String ABOUT_SLUG = "about";

    private static final String ABOUT_TITLE = "About";

    private static final String ARABIC_TITLE = "عن";

    private static final String ARABIC_BODY = "<p>نص</p>";

    private static final String AR = "ar";

    private static final long QUOTA = 5000L;

    private ContentRepository repository;

    private SummaryService service;

    @BeforeEach
    void setUp() {
        repository = mock(ContentRepository.class);
        service = new SummaryService(repository, new BindingRegistry(List.of(new PageBinding())),
                ContentFixtures.clock(), List.of());
    }

    @Test
    void everyRailCountIsPresentAndTheQuotaIsCarried() {
        when(repository.findAll(any(Specification.class))).thenReturn(List.of());

        ContentSummary summary = service.summary(ContentFixtures.STORE, QUOTA);

        assertThat(summary.getCounts()).containsOnlyKeys("pages", "posts", "banners", "faq", "policies",
                "snippets", MEDIA_KEY, "menus");
        assertThat(summary.getMedia().getBytesQuota()).isEqualTo(QUOTA);
        assertThat(summary.getAwaitingTranslation().getTotal()).isZero();
    }

    @Test
    void anItemAwaitingTranslationIsCountedOncePerUntranslatedLocale() {
        Content item = ContentFixtures.published(1L, ContentType.PAGE, ABOUT_SLUG, ABOUT_TITLE);
        item.getDescriptions().getFirst().setState(TranslationState.STALE);
        item.getDescriptions().add(ContentFixtures.description(item, ContentFixtures.AR, ARABIC_TITLE, ARABIC_BODY));
        item.getDescriptions().getLast().setState(TranslationState.DRAFT);
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(item));

        ContentSummary summary = service.summary(ContentFixtures.STORE, QUOTA);

        assertThat(summary.getAwaitingTranslation().getTotal()).isEqualTo(1);
        assertThat(summary.getAwaitingTranslation().getByLocale()).containsEntry("en", 1L).containsEntry(AR, 1L);
    }

    @Test
    void aTranslatedLocaleIsNotCountedAsAwaiting() {
        Content item = ContentFixtures.published(1L, ContentType.PAGE, ABOUT_SLUG, ABOUT_TITLE);
        item.getDescriptions().add(ContentFixtures.description(item, ContentFixtures.AR, ARABIC_TITLE, ARABIC_BODY));
        item.getDescriptions().getLast().setState(TranslationState.DRAFT);
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(item));

        ContentSummary summary = service.summary(ContentFixtures.STORE, QUOTA);

        assertThat(summary.getAwaitingTranslation().getByLocale()).containsOnlyKeys(AR);
    }

    @Test
    void theKpiCardsComeFromTheRepositoryCounts() {
        when(repository.findAll(any(Specification.class))).thenReturn(List.of());
        when(repository.countByStoreMerchantIdAndContentTypeInAndStatus(eq(ContentFixtures.STORE), anyList(),
                eq(ContentStatus.PUBLISHED))).thenReturn(7L);
        when(repository.countByStoreMerchantIdAndContentTypeInAndStatus(eq(ContentFixtures.STORE), anyList(),
                eq(ContentStatus.DRAFT))).thenReturn(2L);
        when(repository.countStale(eq(ContentFixtures.STORE), eq(ContentStatus.DRAFT), anyList(), any()))
                .thenReturn(1L);

        ContentSummary summary = service.summary(ContentFixtures.STORE, QUOTA);

        assertThat(summary.getPublishedItems()).isEqualTo(7L);
        assertThat(summary.getDrafts().getTotal()).isEqualTo(2L);
        assertThat(summary.getDrafts().getStaleOver30Days()).isEqualTo(1L);
    }

    @Test
    void aContributorCanOverwriteTheZeroPlaceholders() {
        when(repository.findAll(any(Specification.class))).thenReturn(List.of());
        SummaryService withMedia = new SummaryService(repository,
                new BindingRegistry(List.of(new PageBinding())), ContentFixtures.clock(),
                List.of((store, summary, counts) -> counts.put(MEDIA_KEY, 42L)));

        assertThat(withMedia.summary(ContentFixtures.STORE, QUOTA).getCounts()).containsEntry(MEDIA_KEY, 42L);
    }

}
