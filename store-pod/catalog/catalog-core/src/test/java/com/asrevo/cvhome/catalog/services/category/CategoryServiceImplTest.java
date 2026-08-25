package com.asrevo.cvhome.catalog.services.category;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.entity.Category;
import com.asrevo.cvhome.catalog.entity.CategoryDescription;
import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.errors.CategoryFriendlyUrlNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.category.CategoryReference;
import com.asrevo.cvhome.catalog.model.category.PersistableCategory;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.repositories.CategoryRepository;
import com.asrevo.cvhome.catalog.repositories.ProductRepository;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The tree operations: how a page of nodes becomes a hierarchy, what a move does to the descendants' paths, and
 * what deleting a branch does to the products that lived only in it.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final LanguageCode EN = new LanguageCode("en");

    private static final String MEN = "MEN";

    private static final String ROOT_LINEAGE = "/1/";

    private static final String MEN_SHOES = "MEN_SHOES";

    private static final String ORPHAN = "ORPHAN";

    private static final String MEN_SLUG = "men";

    private static final String ACCESSORIES = "ACCESSORIES";

    private static final String NO_SUCH = "NO_SUCH";

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CategoryServiceImpl service;

    private static Category category(long id, String code, Category parent) {
        Category category = new Category();
        category.setId(id);
        category.setCode(code);
        category.setStoreMerchantId(STORE);
        category.placeUnder(parent);
        CategoryDescription description = new CategoryDescription(category);
        description.setLanguageCode(EN);
        description.setName(code);
        category.getDescriptions().add(description);
        return category;
    }

    private static Product product(long id, Category... categories) {
        Product product = new Product();
        product.setId(id);
        product.setStore(STORE);
        product.setCategories(new HashSet<>(Set.of(categories)));
        return product;
    }

    // ------------------------------------------------------------------------------------------------- reading

    @Test
    void aParentOutsideThePageBecomesARootOfWhatWasRead() {
        // The hierarchy is assembled from one page, so a node whose parent did not fit on it has to be shown
        // somewhere: it becomes a root rather than disappearing.
        Category root = category(1L, MEN, null);
        Category child = category(7L, MEN_SHOES, root);
        Category orphan = category(9L, ORPHAN, category(4L, "ELSEWHERE", null));
        when(categoryRepository.findByStore(eq(STORE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(root, child, orphan)));

        ReadableEntityList<ReadableCategory> tree = service.hierarchy(STORE, null, EN, true,
                PageRequest.of(0, 10));

        assertThat(tree.getContent()).hasSize(2);
        assertThat(tree.getContent().getFirst().getChildren()).hasSize(1);
        assertThat(tree.getContent().getLast().getCode()).isEqualTo(ORPHAN);
        assertThat(tree.getTotalElements()).isEqualTo(3);
    }

    @Test
    void aNameNarrowsTheQueryAndBlankDoesNot() {
        when(categoryRepository.findByStoreAndName(eq(STORE), eq("Men"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(category(1L, MEN, null))));
        when(categoryRepository.findByStore(eq(STORE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertThat(service.list(STORE, "  Men  ", EN, false, PageRequest.of(0, 10)).getContent()).hasSize(1);
        assertThat(service.list(STORE, "   ", EN, false, PageRequest.of(0, 10)).getContent()).isEmpty();
    }

    @Test
    void readingOneCategoryAnswersItsSubtreeWithoutItself() throws Exception {
        Category root = category(1L, MEN, null);
        Category child = category(7L, MEN_SHOES, root);
        when(categoryRepository.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(root));
        when(categoryRepository.findSubtree(STORE, ROOT_LINEAGE)).thenReturn(List.of(root, child));

        ReadableCategory readable = service.get(STORE, 1L, EN);

        assertThat(readable.getChildren()).hasSize(1);
        assertThat(readable.getChildren().getFirst().getCode()).isEqualTo(MEN_SHOES);
    }

    @Test
    void anUnknownIdOrSlugIsNotFound() {
        when(categoryRepository.findByStoreAndId(STORE, 1L)).thenReturn(Optional.empty());
        when(categoryRepository.findByStoreAndFriendlyUrl(STORE, MEN_SLUG, EN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(STORE, 1L, EN)).isInstanceOf(CategoryNotFoundException.class);
        assertThatThrownBy(() -> service.getByFriendlyUrl(STORE, MEN_SLUG, EN))
                .isInstanceOf(CategoryFriendlyUrlNotFoundException.class);
    }

    // ------------------------------------------------------------------------------------------------- writing

    @Test
    void aNewCategoryIsFlushedBeforeItsPathIsComputed() throws Exception {
        // The lineage is built from the node's own id, so a create has to reach the database for one before it can
        // place the node anywhere.
        PersistableCategory source = new PersistableCategory();
        source.setCode(MEN);
        when(categoryRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(11L);
            return category;
        });

        PersistableCategory saved = service.save(STORE, source);

        assertThat(saved.getId()).isEqualTo(11L);
        verify(categoryRepository).saveAndFlush(any());
        verify(categoryRepository).save(any());
    }

    @Test
    void anEditReplacesTheDescendantsPathsRatherThanFlushingAgain() throws Exception {
        Category root = category(1L, MEN, null);
        Category child = category(7L, MEN_SHOES, root);
        Category newParent = category(4L, ACCESSORIES, null);
        PersistableCategory source = new PersistableCategory();
        source.setId(1L);
        source.setCode(MEN);
        source.setParent(new CategoryReference(4L, null));
        when(categoryRepository.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(root));
        when(categoryRepository.findByStoreAndId(STORE, 4L)).thenReturn(Optional.of(newParent));
        // the descendants are looked up under the path they still hold, not the one the node is moving to
        when(categoryRepository.findSubtree(STORE, ROOT_LINEAGE)).thenReturn(List.of(root, child));

        service.save(STORE, source);

        assertThat(root.getLineage()).isEqualTo("/4/1/");
        assertThat(child.getLineage()).isEqualTo("/4/1/7/");
        assertThat(child.getDepth()).isEqualTo(2);
        verify(categoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void aParentReferenceWithNeitherIdNorCodeMeansTheRoot() throws Exception {
        PersistableCategory source = new PersistableCategory();
        source.setId(0L);
        source.setCode(MEN);
        source.setParent(new CategoryReference(null, "  "));
        when(categoryRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(12L);
            return category;
        });

        service.save(STORE, source);

        // an id of zero is the console's "not saved yet", so this is a create, not an edit of category 0
        verify(categoryRepository).saveAndFlush(any());
        verify(categoryRepository, never()).findByStoreAndCode(any(), any());
    }

    @Test
    void aParentThatDoesNotResolveIsRefused() {
        PersistableCategory source = new PersistableCategory();
        source.setCode(MEN);
        source.setParent(new CategoryReference(null, NO_SUCH));
        when(categoryRepository.findByStoreAndCode(STORE, NO_SUCH)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.save(STORE, source))
                .isInstanceOf(CategoryReferenceUnresolvableException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void movingToTheRootSentinelDetachesTheNode() throws Exception {
        Category root = category(1L, MEN, null);
        Category child = category(7L, MEN_SHOES, root);
        when(categoryRepository.findByStoreAndId(STORE, 7L)).thenReturn(Optional.of(child));
        when(categoryRepository.findSubtree(STORE, "/1/7/")).thenReturn(List.of(child));

        service.move(STORE, 7L, -1L);

        assertThat(child.getParent()).isNull();
        assertThat(child.getLineage()).isEqualTo("/7/");
    }

    @Test
    void hidingACategoryOnlyFlipsTheFlag() throws Exception {
        Category root = category(1L, MEN, null);
        when(categoryRepository.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(root));

        service.setVisible(STORE, 1L, true);

        assertThat(root.isVisible()).isTrue();
    }

    // ------------------------------------------------------------------------------------------------ deleting

    @Test
    void deletingABranchDeletesOnlyTheProductsLeftWithNoCategory() throws Exception {
        // A product that also lives elsewhere keeps that membership and survives; one that lived only in the
        // branch has nowhere left to be shown, so it goes with it.
        Category root = category(1L, MEN, null);
        Category child = category(7L, MEN_SHOES, root);
        Category elsewhere = category(4L, ACCESSORIES, null);
        Product only = product(30L, child);
        Product shared = product(31L, child, elsewhere);
        when(categoryRepository.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(root));
        when(categoryRepository.findSubtree(STORE, ROOT_LINEAGE)).thenReturn(List.of(root, child));
        when(productRepository.findByStoreAndCategories(eq(STORE), any())).thenReturn(List.of(only, shared));

        service.delete(STORE, 1L);

        verify(productService).delete(STORE, only);
        verify(productService, never()).delete(STORE, shared);
        assertThat(shared.getCategories()).containsExactly(elsewhere);
        // deepest first, so no child still points at a parent being removed
        verify(categoryRepository).deleteAll(List.of(child, root));
    }

}
