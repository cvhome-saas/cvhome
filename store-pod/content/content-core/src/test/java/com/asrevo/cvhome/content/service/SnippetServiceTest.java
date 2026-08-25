package com.asrevo.cvhome.content.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.snippet.Snippet;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The legacy BOX rows as snippets: no workflow, a PUT upserts by code, and {@code status} mirrors visibility so
 * the list predicates stay uniform across types.
 */
class SnippetServiceTest {

    private static final String EXAMPLE_TITLE = "Example";

    private static final String CODE = "meta-title";

    private static final String ACTOR = "ada";

    private ContentRepository repository;

    private SnippetService service;

    @BeforeEach
    void setUp() {
        repository = mock(ContentRepository.class);
        service = new SnippetService(repository);
    }

    @Test
    void snippetsAreListedForTheStore() {
        Content box = ContentFixtures.published(1L, ContentType.BOX, CODE, EXAMPLE_TITLE);
        when(repository.findAllByType(ContentFixtures.STORE, ContentType.BOX)).thenReturn(List.of(box));

        assertThat(service.list(ContentFixtures.STORE)).singleElement().satisfies(s -> {
            assertThat(s.getCode()).isEqualTo(CODE);
            assertThat(s.isVisible()).isTrue();
            assertThat(s.getTranslations()).hasSize(1);
        });
    }

    @Test
    void anUnknownCodeReadsAsMissing() {
        when(repository.findByCodeAndType(CODE, ContentType.BOX, ContentFixtures.STORE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(ContentFixtures.STORE, CODE))
                .isInstanceOf(ContentNotFoundException.class);
        assertThatThrownBy(() -> service.delete(ContentFixtures.STORE, CODE))
                .isInstanceOf(ContentNotFoundException.class);
    }

    @Test
    void aPutOnAnUnknownCodeCreatesTheRow() {
        when(repository.findByCodeAndType(CODE, ContentType.BOX, ContentFixtures.STORE))
                .thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        Snippet body = new Snippet();
        body.setVisible(true);
        body.setTranslations(List.of(ContentFixtures.translation(ContentFixtures.EN, EXAMPLE_TITLE, null)));

        Snippet out = service.put(ContentFixtures.STORE, CODE, body, ACTOR);

        assertThat(out.getCode()).isEqualTo(CODE);
        assertThat(out.isVisible()).isTrue();
        assertThat(out.getTranslations()).hasSize(1);
    }

    @Test
    void hidingASnippetDropsItsStatusToDraft() {
        Content box = ContentFixtures.published(1L, ContentType.BOX, CODE, EXAMPLE_TITLE);
        when(repository.findByCodeAndType(CODE, ContentType.BOX, ContentFixtures.STORE))
                .thenReturn(Optional.of(box));
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        Snippet body = new Snippet();
        body.setVisible(false);
        body.setTranslations(List.of(ContentFixtures.translation(ContentFixtures.EN, EXAMPLE_TITLE, null)));

        service.put(ContentFixtures.STORE, CODE, body, ACTOR);

        assertThat(box.getStatus()).isEqualTo(ContentStatus.DRAFT);
        assertThat(box.isVisible()).isFalse();
        assertThat(box.getUpdatedBy()).isEqualTo(ACTOR);
    }

    @Test
    void deletingRemovesTheRow() throws Exception {
        Content box = ContentFixtures.published(1L, ContentType.BOX, CODE, EXAMPLE_TITLE);
        when(repository.findByCodeAndType(CODE, ContentType.BOX, ContentFixtures.STORE))
                .thenReturn(Optional.of(box));

        service.delete(ContentFixtures.STORE, CODE);

        verify(repository).delete(box);
    }

}
