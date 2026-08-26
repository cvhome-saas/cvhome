package com.asrevo.cvhome.content.entity;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code servable} is the one predicate the storefront trusts: a legacy BOX row goes by {@code visible}, a
 * workflow row must be PUBLISHED and inside its window.
 */
class ContentTest {

    private static final String ABOUT_TITLE = "About";

    private static final String SLUG = "about-us";

    /**
     * The visible flag used to be the whole answer for the legacy BOX rows, so a draft that happened to carry
     * it leaked to the storefront. Status is the only gate now, whatever the row's type.
     */
    @Test
    void theVisibleFlagAloneNoLongerServesARow() {
        Content untyped = ContentFixtures.content(1L, null, SLUG);
        untyped.setVisible(true);

        assertThat(untyped.servable(ContentFixtures.NOW)).isFalse();
    }

    @Test
    void anUnpublishedWorkflowRowIsNeverServable() {
        assertThat(ContentFixtures.content(1L, ContentType.PAGE, SLUG).servable(ContentFixtures.NOW)).isFalse();
    }

    @Test
    void aWorkflowRowIsServableOnlyInsideItsWindow() {
        Content page = ContentFixtures.published(1L, ContentType.PAGE, SLUG, ABOUT_TITLE);
        assertThat(page.servable(ContentFixtures.NOW)).isTrue();

        page.setPublishAt(ContentFixtures.NOW.plusSeconds(60));
        assertThat(page.servable(ContentFixtures.NOW)).isFalse();

        page.setPublishAt(ContentFixtures.NOW.minusSeconds(60));
        page.setUnpublishAt(ContentFixtures.NOW.minusSeconds(1));
        assertThat(page.servable(ContentFixtures.NOW)).isFalse();

        page.setUnpublishAt(ContentFixtures.NOW.plusSeconds(1));
        assertThat(page.servable(ContentFixtures.NOW)).isTrue();
    }

    @Test
    void statusIsWhatDecidesEvenWhenTheLegacyFlagDisagrees() {
        Content page = ContentFixtures.published(1L, ContentType.PAGE, SLUG, ABOUT_TITLE);
        page.setStatus(ContentStatus.ARCHIVED);

        assertThat(page.servable(ContentFixtures.NOW)).isFalse();
    }

    @Test
    void theFirstDescriptionAnswersWhenNoLocaleIsAsked() {
        Content page = ContentFixtures.published(1L, ContentType.PAGE, SLUG, ABOUT_TITLE);

        assertThat(page.getDescription().getName()).isEqualTo(ABOUT_TITLE);
        assertThat(page.description(ContentFixtures.EN)).isPresent();
        assertThat(page.description(ContentFixtures.AR)).isEmpty();
        assertThat(page.description(null)).isEmpty();
    }

    @Test
    void aRowWithNoDescriptionsHasNoneToOffer() {
        assertThat(ContentFixtures.content(1L, ContentType.PAGE, SLUG).getDescription()).isNull();
    }

}
