package com.asrevo.cvhome.store.service;

import com.asrevo.cvhome.storepod.commons.domain.CategoryId;
import com.asrevo.cvhome.storepod.commons.domain.ImageLink;
import com.asrevo.cvhome.storepod.commons.dto.CategoriesView;
import com.asrevo.cvhome.store.entity.CategoryEntity;
import com.asrevo.cvhome.store.repository.CategoryRepository;
import com.asrevo.cvhome.store.service.impl.CategoryServiceImpl;
import com.asrevo.cvhome.manager.commons.domain.StoreId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@ExtendWith(MockitoExtension.class)
@Tag("unit-test")
class AdminCategoryServiceUnitTest {
    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void getStoreCategoriesView() {
        StoreId storeId = StoreId.newId();

        CategoryEntity p1 = new CategoryEntity();
        p1.setId(CategoryId.newId());
        p1.setStoreId(storeId);
        p1.setName("p1");
        p1.setImageLink( new ImageLink("https://google.com/product.png"));

        CategoryEntity p1s1 = new CategoryEntity();
        p1s1.setId(CategoryId.newId());
        p1s1.setStoreId(storeId);
        p1s1.setName("p1s1");
        p1s1.setImageLink( new ImageLink("https://google.com/product.png"));
        p1s1.setParent(AggregateReference.to(p1.getId()));

        CategoryEntity p1s2 = new CategoryEntity();
        p1s2.setId(CategoryId.newId());
        p1s2.setStoreId(storeId);
        p1s2.setName("p1s2");
        p1s2.setImageLink( new ImageLink("https://google.com/product.png"));
        p1s2.setParent(AggregateReference.to(p1.getId()));


        CategoryEntity p2 = new CategoryEntity();
        p2.setId(CategoryId.newId());
        p2.setStoreId(storeId);
        p2.setName("p2");
        p2.setImageLink( new ImageLink("https://google.com/product.png"));

        CategoryEntity p2s1 = new CategoryEntity();
        p2s1.setId(CategoryId.newId());
        p2s1.setStoreId(storeId);
        p2s1.setName("p2s1");
        p2s1.setImageLink( new ImageLink("https://google.com/product.png"));
        p2s1.setParent(AggregateReference.to(p2.getId()));

        CategoryEntity p2s2 = new CategoryEntity();
        p2s2.setId(CategoryId.newId());
        p2s2.setStoreId(storeId);
        p2s2.setName("p2s2");
        p2s2.setImageLink( new ImageLink("https://google.com/product.png"));
        p2s2.setParent(AggregateReference.to(p2.getId()));


        List<CategoryEntity> entities = List.of(p1, p2, p1s1, p1s2, p2s1, p2s2);
        Mockito.when(categoryRepository.findALlByStoreId(storeId)).thenReturn(entities);
        List<CategoriesView> storeCategoriesView = categoryService.getStoreCategoriesView(storeId);
        assertThat(storeCategoriesView, hasSize(2));
        for (CategoriesView categoriesView : storeCategoriesView) {
            assertThat(categoriesView.subCategories(), hasSize(2));
        }
    }

}