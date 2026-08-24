package com.asrevo.cvhome.content.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentStatusTest {

    @Test
    void draftCanPublishDirectlyOrGoThroughReview() {
        assertThat(ContentStatus.DRAFT.canTransitionTo(ContentStatus.PUBLISHED)).isTrue();
        assertThat(ContentStatus.DRAFT.canTransitionTo(ContentStatus.REVIEW)).isTrue();
        assertThat(ContentStatus.DRAFT.canTransitionTo(ContentStatus.SCHEDULED)).isTrue();
        assertThat(ContentStatus.REVIEW.canTransitionTo(ContentStatus.PUBLISHED)).isTrue();
    }

    @Test
    void publishedUnpublishesToDraftOrArchives() {
        assertThat(ContentStatus.PUBLISHED.canTransitionTo(ContentStatus.DRAFT)).isTrue();
        assertThat(ContentStatus.PUBLISHED.canTransitionTo(ContentStatus.ARCHIVED)).isTrue();
        assertThat(ContentStatus.PUBLISHED.canTransitionTo(ContentStatus.REVIEW)).isFalse();
    }

    @Test
    void archivedOnlyRestoresToDraft() {
        assertThat(ContentStatus.ARCHIVED.canTransitionTo(ContentStatus.DRAFT)).isTrue();
        assertThat(ContentStatus.ARCHIVED.canTransitionTo(ContentStatus.PUBLISHED)).isFalse();
        assertThat(ContentStatus.ARCHIVED.canTransitionTo(ContentStatus.SCHEDULED)).isFalse();
    }

    @Test
    void sameStatusIsNeverATransition() {
        for (ContentStatus s : ContentStatus.values()) {
            assertThat(s.canTransitionTo(s)).isFalse();
            assertThat(s.canTransitionTo(null)).isFalse();
        }
    }

}
