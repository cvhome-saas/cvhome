package com.asrevo.cvhome.content.entity.content;

import java.time.Instant;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.errors.IllegalContentTransitionException;
import com.asrevo.cvhome.content.model.ContentStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit-test")
class ContentLifecycleTest {
    private static final Instant NOW = Instant.parse("2026-08-13T00:00:00Z");
    private static final String ACTOR = "actor";

    @Test
    void supportsReviewPublishUnpublishArchiveAndRestore() throws IllegalContentTransitionException {
        Content content = new Content();

        content.transition(ContentStatus.IN_REVIEW, null, null, ACTOR, NOW);
        content.transition(ContentStatus.PUBLISHED, null, null, ACTOR, NOW);
        content.transition(ContentStatus.UNPUBLISHED, null, null, ACTOR, NOW);
        content.transition(ContentStatus.ARCHIVED, null, null, ACTOR, NOW);
        content.transition(ContentStatus.DRAFT, null, null, ACTOR, NOW);

        assertThat(content.getStatus()).isEqualTo(ContentStatus.DRAFT);
    }

    @Test
    void refusesInvalidTransitionAndPastSchedule() {
        Content content = new Content();

        assertThatThrownBy(() -> content.transition(ContentStatus.ARCHIVED, null, null, ACTOR, NOW))
                .isInstanceOf(IllegalContentTransitionException.class);
        assertThatThrownBy(() -> content.transition(ContentStatus.SCHEDULED, NOW, null, ACTOR, NOW))
                .isInstanceOf(IllegalContentTransitionException.class);
    }
}
