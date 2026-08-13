package com.asrevo.cvhome.content.service;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.TestcontainersConfiguration;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.ContentType;
import com.asrevo.cvhome.content.model.ContentView;
import com.asrevo.cvhome.content.model.ContentWriteRequest;
import com.asrevo.cvhome.content.model.LifecycleRequest;
import com.asrevo.cvhome.content.model.page.PageBlockSpec;
import com.asrevo.cvhome.content.model.page.PageView;
import com.asrevo.cvhome.content.model.page.PageWriteRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Tag("integration-test")
@Transactional
class StorefrontContentServiceIntegrationTest {
    private static final StoreMerchantId STORE = new StoreMerchantId("storefront-store");
    private static final StoreMerchantId OTHER_STORE = new StoreMerchantId("storefront-other-store");
    private static final LanguageCode ENGLISH = LanguageCode.defaultLanguage();
    private static final String ACTOR = "storefront-test";
    private static final String PAGE_NAME = "Shipping help";
    private static final String PAGE_SLUG = "shipping-help";
    private static final String PAGE_TEMPLATE = "standard";
    private static final String PAGE_REFERENCE_TYPE = "PAGE";
    private static final String PAGE_REFERENCE_ID = "shipping-details";
    private static final LanguageCode ARABIC = new LanguageCode("ar");

    @Autowired
    private PageService pageService;
    @Autowired
    private ContentV2Service contentService;
    @Autowired
    private StorefrontContentService storefrontService;

    @Test
    void storefrontExcludesDraftAndCrossStoreThenReturnsPublishedRouteSearchAndSummary() throws Exception {
        PageView page = pageService.create(STORE, ENGLISH, pageRequest(), ACTOR);

        assertThatThrownBy(() -> storefrontService.page(STORE, ENGLISH, PAGE_SLUG))
                .isInstanceOf(ContentNotFoundException.class);
        publish(page.content());

        assertThat(storefrontService.page(STORE, ENGLISH, PAGE_SLUG).content().id())
                .isEqualTo(page.content().id());
        assertThat(storefrontService.search(STORE, ENGLISH, "shipping", 25))
                .extracting(ContentView::id).containsExactly(page.content().id());
        assertThat(storefrontService.summary(STORE).byType()).containsEntry(ContentType.PAGE, 1L);
        assertThat(storefrontService.sitemap(STORE, ENGLISH)).contains(PAGE_SLUG);
        assertThatThrownBy(() -> storefrontService.page(OTHER_STORE, ENGLISH, PAGE_SLUG))
                .isInstanceOf(ContentNotFoundException.class);
    }

    @Test
    void pageReferenceRoundTripsThroughPersistence() throws Exception {
        PageWriteRequest request = new PageWriteRequest(pageRequest().content(), PAGE_TEMPLATE, true, null,
                List.of(new PageBlockSpec.Reference(PAGE_REFERENCE_TYPE, PAGE_REFERENCE_ID)));

        PageView created = pageService.create(STORE, ENGLISH, request, ACTOR);

        assertThat(pageService.find(STORE, created.content().id()).blocks())
                .containsExactly(new PageBlockSpec.Reference(PAGE_REFERENCE_TYPE, PAGE_REFERENCE_ID));
    }

    @Test
    void pageFallsBackWithMetadataAndSearchToleratesTypos() throws Exception {
        PageView page = pageService.create(STORE, ENGLISH, pageRequest(), ACTOR);
        publish(page.content());

        PageView fallback = storefrontService.page(STORE, ARABIC, PAGE_SLUG);

        assertThat(fallback.fallback()).isTrue();
        assertThat(fallback.requestedLanguage()).isEqualTo(ARABIC);
        assertThat(fallback.resolvedLanguage()).isEqualTo(ENGLISH);
        assertThat(storefrontService.search(STORE, ENGLISH, "shiping", 25))
                .extracting(ContentView::id).contains(page.content().id());
    }

    private void publish(ContentView content) throws Exception {
        contentService.transition(STORE, content.id(), content.version(), ContentStatus.PUBLISHED,
                new LifecycleRequest(null, null, "publish"), ACTOR);
    }

    private static PageWriteRequest pageRequest() {
        ContentWriteRequest content = new ContentWriteRequest(PAGE_SLUG, ContentType.PAGE,
                PAGE_NAME, PAGE_NAME, "Shipping delivery information", PAGE_SLUG,
                null, null, null, null, false, null, null);
        return new PageWriteRequest(content, PAGE_TEMPLATE, true, null, List.of());
    }
}
