package com.asrevo.cvhome.store.service;

import com.asrevo.cvhome.store.commons.dto.CategoryDto;
import com.asrevo.cvhome.store.commons.dto.CreateCategoryDto;
import com.asrevo.cvhome.store.commons.dto.CreateCategoryResponseDto;
import com.asrevo.cvhome.store.mappers.CategoryMapperImpl;
import com.asrevo.cvhome.store.service.impl.AdminCategoryServiceImpl;
import com.asrevo.cvhome.storepod.commons.domain.CategoryId;
import com.asrevo.cvhome.storepod.commons.domain.ImageLink;
import com.asrevo.cvhome.storepod.commons.domain.StoreId;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JacksonAutoConfiguration.class, AdminCategoryServiceImpl.class, CategoryMapperImpl.class})
@Testcontainers
@Tag("integration-test")
class AdminCategoryServiceIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");
    @Autowired
    private AdminCategoryService categoryService;

    @Test
    void createCategory() {
        StoreId storeId = StoreId.newId();
        CreateCategoryDto createCategoryDto = new CreateCategoryDto("ssa", new ImageLink("https://google.com/product.png"), 0);
        CreateCategoryResponseDto category = categoryService.createCategory(storeId, createCategoryDto);
        assertThat(category, notNullValue());
        assertThat(category.id(), notNullValue());
        assertThat(category.name(), is(createCategoryDto.name()));
        assertThat(category.imageLink(), is(createCategoryDto.imageLink()));
    }

    @Test
    void createCategoryUnderCategory() {
        StoreId storeId = StoreId.newId();
        CreateCategoryDto createCategoryDto = new CreateCategoryDto("ssa", new ImageLink("https://google.com/product.png"), 0);
        CreateCategoryResponseDto parentCategory = categoryService.createCategory(storeId, createCategoryDto);
        CreateCategoryResponseDto subCategory = categoryService.createCategory(storeId, parentCategory.id(), createCategoryDto);

        assertThat(subCategory, notNullValue());
        assertThat(subCategory.id(), notNullValue());
        assertThat(subCategory.name(), is(createCategoryDto.name()));
        assertThat(subCategory.imageLink(), is(createCategoryDto.imageLink()));
        assertThat(subCategory.parent(), is(parentCategory.id()));
    }

    @Test
    void createCategoryUnderCategoryNotExist() {
        StoreId storeId = StoreId.newId();
        CreateCategoryDto createCategoryDto = new CreateCategoryDto("ssa", new ImageLink("https://google.com/product.png"), 0);
        assertThrows("parent category not exist", RuntimeException.class, () -> categoryService.createCategory(storeId, CategoryId.newId(), createCategoryDto));
    }

    @Test
    void findCategoryInStore() {
        StoreId storeId = StoreId.newId();
        CreateCategoryDto createCategoryDto = new CreateCategoryDto("ssa", new ImageLink("https://google.com/product.png"), 0);
        CreateCategoryResponseDto parentCategory = categoryService.createCategory(storeId, createCategoryDto);
        CreateCategoryResponseDto subCategory = categoryService.createCategory(storeId, parentCategory.id(), createCategoryDto);


        CategoryDto findParent = categoryService.findCategory(parentCategory.id(), storeId);
        assertThat(findParent, notNullValue());
        assertThat(findParent.parent(), nullValue());
        assertThat(findParent.name(), notNullValue());
        assertThat(findParent.imageLink(), notNullValue());

        CategoryDto findSub = categoryService.findCategory(subCategory.id(), storeId);
        assertThat(findSub, notNullValue());
        assertThat(findSub.parent(), notNullValue());
        assertThat(findSub.name(), notNullValue());
        assertThat(findSub.imageLink(), notNullValue());
    }
}