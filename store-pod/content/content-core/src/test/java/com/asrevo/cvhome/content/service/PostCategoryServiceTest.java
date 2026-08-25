package com.asrevo.cvhome.content.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.PostCategory;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.repository.PostCategoryRepository;
import com.asrevo.cvhome.content.support.JsonCodec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Blog categories. Slugs are unique per store and a rename onto a taken slug is a 409, but keeping your own slug
 * is not a duplicate of yourself.
 */
class PostCategoryServiceTest {

    private static final String EN = "en";

    private static final String NEWS_NAME = "News";

    private static final String STORE_ID = ContentFixtures.STORE.getId();

    private static final String NEWS = "news";

    private static final String GUIDES = "guides";

    private PostCategoryRepository repository;

    private PostCategoryService service;

    @BeforeEach
    void setUp() {
        repository = mock(PostCategoryRepository.class);
        service = new PostCategoryService(repository);
    }

    private static PostCategory category(Long id, String slug) {
        PostCategory c = new PostCategory();
        c.setId(id);
        c.setStoreMerchantId(STORE_ID);
        c.setSlug(slug);
        c.setPosition(1);
        c.setNames(JsonCodec.write(Map.of(EN, slug)));
        return c;
    }

    @Test
    void categoriesComeBackInPositionOrderAndKeyedById() {
        when(repository.findByStoreMerchantIdOrderByPositionAscIdAsc(STORE_ID))
                .thenReturn(List.of(category(1L, NEWS), category(2L, GUIDES)));

        assertThat(service.list(ContentFixtures.STORE)).extracting("slug").containsExactly(NEWS, GUIDES);
        assertThat(service.byIds(ContentFixtures.STORE)).containsOnlyKeys(1L, 2L);
    }

    @Test
    void aDuplicateSlugIsAConflict() {
        when(repository.findByStoreMerchantIdAndSlug(STORE_ID, NEWS)).thenReturn(Optional.of(category(1L, NEWS)));
        var body = new com.asrevo.cvhome.content.model.post.PostCategory();
        body.setSlug(NEWS);

        assertThatThrownBy(() -> service.create(ContentFixtures.STORE, body))
                .isInstanceOf(ContentConflictException.class);
    }

    @Test
    void aNewCategoryWithoutAPositionSortsFirst() throws Exception {
        when(repository.findByStoreMerchantIdAndSlug(STORE_ID, NEWS)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenAnswer(i -> {
            PostCategory c = i.getArgument(0);
            c.setId(4L);
            return c;
        });
        var body = new com.asrevo.cvhome.content.model.post.PostCategory();
        body.setSlug(NEWS);
        body.setNames(Map.of(EN, NEWS_NAME));

        var out = service.create(ContentFixtures.STORE, body);

        assertThat(out.getId()).isEqualTo(4L);
        assertThat(out.getPosition()).isZero();
        assertThat(out.getNames()).containsEntry(EN, NEWS_NAME);
    }

    @Test
    void updatingACategoryOfAnotherStoreReadsAsMissing() {
        when(repository.findByIdAndStoreMerchantId(1L, STORE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(ContentFixtures.STORE, 1L,
                new com.asrevo.cvhome.content.model.post.PostCategory()))
                .isInstanceOf(ContentNotFoundException.class);
        assertThatThrownBy(() -> service.delete(ContentFixtures.STORE, 1L))
                .isInstanceOf(ContentNotFoundException.class);
    }

    @Test
    void renamingOntoATakenSlugIsAConflict() {
        when(repository.findByIdAndStoreMerchantId(1L, STORE_ID)).thenReturn(Optional.of(category(1L, NEWS)));
        when(repository.findByStoreMerchantIdAndSlug(STORE_ID, GUIDES)).thenReturn(Optional.of(category(2L, GUIDES)));
        var body = new com.asrevo.cvhome.content.model.post.PostCategory();
        body.setSlug(GUIDES);

        assertThatThrownBy(() -> service.update(ContentFixtures.STORE, 1L, body))
                .isInstanceOf(ContentConflictException.class);
    }

    @Test
    void keepingYourOwnSlugIsNotADuplicate() throws Exception {
        when(repository.findByIdAndStoreMerchantId(1L, STORE_ID)).thenReturn(Optional.of(category(1L, NEWS)));
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        var body = new com.asrevo.cvhome.content.model.post.PostCategory();
        body.setSlug(NEWS);
        body.setNames(Map.of(EN, "Newsroom"));
        body.setPosition(5);

        assertThat(service.update(ContentFixtures.STORE, 1L, body).getPosition()).isEqualTo(5);
    }

    @Test
    void deletingRemovesTheRow() throws Exception {
        PostCategory c = category(1L, NEWS);
        when(repository.findByIdAndStoreMerchantId(1L, STORE_ID)).thenReturn(Optional.of(c));

        service.delete(ContentFixtures.STORE, 1L);

        verify(repository).delete(c);
    }

    @Test
    void aCategoryWithoutStoredNamesReadsAsAnEmptyMap() {
        PostCategory c = category(1L, NEWS);
        c.setNames(null);

        assertThat(PostCategoryService.names(c)).isEmpty();
        assertThat(PostCategoryService.names(category(1L, NEWS))).containsEntry(EN, NEWS);
    }

}
