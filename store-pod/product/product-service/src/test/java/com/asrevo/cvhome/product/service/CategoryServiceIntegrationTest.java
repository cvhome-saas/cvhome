package com.asrevo.cvhome.product.service;

import com.asrevo.cvhome.product.commons.domain.ImageLink;
import com.asrevo.cvhome.product.commons.dto.CategoryDto;
import com.asrevo.cvhome.product.commons.dto.CreateCategoryDto;
import com.asrevo.cvhome.product.commons.dto.CreateCategoryResponseDto;
import com.asrevo.cvhome.product.mappers.CategoryMapperImpl;
import com.asrevo.cvhome.product.service.impl.CategoryServiceImpl;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JacksonAutoConfiguration.class, CategoryServiceImpl.class, CategoryMapperImpl.class})
@Testcontainers
@Tag("integration-test")
class CategoryServiceIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");
    @Autowired
    private CategoryService categoryService;

    @Test
    void createCategory() {
        StoreId storeId = StoreId.newId();
        CreateCategoryDto createCategoryDto = new CreateCategoryDto("ssa", new ImageLink("swa"), 0);
        CreateCategoryResponseDto category = categoryService.createCategory(storeId, createCategoryDto);
        assertThat(category, notNullValue());
        assertThat(category.id(), notNullValue());
        assertThat(category.name(), is(createCategoryDto.name()));
        assertThat(category.imageLink(), is(createCategoryDto.imageLink()));
    }

    @Test
    void createCategoryUnderCategory() {
        StoreId storeId = StoreId.newId();
        CreateCategoryDto createCategoryDto = new CreateCategoryDto("ssa", new ImageLink("swa"), 0);
        CreateCategoryResponseDto parentCategory = categoryService.createCategory(storeId, createCategoryDto);
        CreateCategoryResponseDto subCategory = categoryService.createCategory(storeId, parentCategory.id(), createCategoryDto);

        assertThat(subCategory, notNullValue());
        assertThat(subCategory.id(), notNullValue());
        assertThat(subCategory.name(), is(createCategoryDto.name()));
        assertThat(subCategory.imageLink(), is(createCategoryDto.imageLink()));
        assertThat(subCategory.parent(), is(parentCategory.id()));
    }

    @Test
    void findCategoryInStore() {
        StoreId storeId = StoreId.newId();
        CreateCategoryDto createCategoryDto = new CreateCategoryDto("ssa", new ImageLink("swa"), 0);
        CreateCategoryResponseDto parentCategory = categoryService.createCategory(storeId, createCategoryDto);
        CreateCategoryResponseDto subCategory = categoryService.createCategory(storeId, parentCategory.id(), createCategoryDto);

        Optional<CategoryDto> findParent = categoryService.findCategory(parentCategory.id(), storeId);
        Optional<CategoryDto> findSub = categoryService.findCategory(subCategory.id(), storeId);

        assertThat(findParent.isPresent(), is(Boolean.TRUE));
        findParent.ifPresent(it -> {
            assertThat(it, notNullValue());
            assertThat(it.parent(), nullValue());
            assertThat(it.name(), notNullValue());
            assertThat(it.imageLink(), notNullValue());
        });

        assertThat(findSub.isPresent(), is(Boolean.TRUE));
        findSub.ifPresent(it -> {
            assertThat(it, notNullValue());
            assertThat(it.parent(), notNullValue());
            assertThat(it.name(), notNullValue());
            assertThat(it.imageLink(), notNullValue());
        });
    }
}