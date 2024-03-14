package com.asrevo.cvhome.product.service;

import com.asrevo.cvhome.product.commons.domain.CategoryId;
import com.asrevo.cvhome.product.commons.domain.ImageLink;
import com.asrevo.cvhome.product.commons.domain.ProductAmount;
import com.asrevo.cvhome.product.commons.domain.ProductPrice;
import com.asrevo.cvhome.product.commons.dto.*;
import com.asrevo.cvhome.product.mappers.CategoryMapperImpl;
import com.asrevo.cvhome.product.mappers.ProductMapperImpl;
import com.asrevo.cvhome.product.service.impl.CategoryServiceImpl;
import com.asrevo.cvhome.product.service.impl.ProductServiceImpl;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JacksonAutoConfiguration.class, ProductServiceImpl.class, ProductMapperImpl.class, CategoryServiceImpl.class, CategoryMapperImpl.class})
@Testcontainers
@Tag("integration-test")
class ProductServiceIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

    @Test
    void findAll() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("swa"), 0)).id();
        IntStream.range(0, 20).forEach(it -> {
            CreateProductDto createProductDto = new CreateProductDto("p" + it, "d1" + it, new ProductPrice(50D * it, Currency.getInstance("USD")), new ImageLink("google.com"));
            productService.createProduct(storeId, categoryId, createProductDto);
        });
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<ProductDto> all = productService.findAll(storeId, pageRequest);
        assertThat(all.size(), is(pageRequest.getPageSize()));
    }

    @Test
    void createProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("swa"), 0)).id();
        CreateProductDto createProductDto = new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), new ImageLink("google.com"));
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, categoryId, createProductDto);
        assertThat(createProductResponseDto.published(), is(Boolean.FALSE));
        assertThat(createProductResponseDto.id(), notNullValue());
        assertThat(createProductResponseDto.price(), is(createProductDto.price()));
        assertThat(createProductResponseDto.imageLink(), is(createProductDto.imageLink()));
    }

    @Test
    void getProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("swa"), 0)).id();
        CreateProductDto createProductDto = new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), new ImageLink("google.com"));
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, categoryId, createProductDto);
        ProductDto productDto = productService.getProduct(storeId, createProductResponseDto.id());
        assertThat(createProductResponseDto.id(), is(productDto.id()));
        assertThat(createProductResponseDto.name(), is(createProductDto.name()));
        assertThat(createProductResponseDto.description(), is(createProductDto.description()));
        assertThat(createProductResponseDto.price(), is(createProductDto.price()));
        assertThat(createProductResponseDto.imageLink(), is(createProductDto.imageLink()));
    }

    @Test
    void deleteProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("swa"), 0)).id();
        CreateProductDto createProductDto = new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), new ImageLink("google.com"));
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, categoryId, createProductDto);
        DeleteProductResponseDto deleteProductResponseDto = productService.deleteProduct(storeId, createProductResponseDto.id());
        ProductDto productDto = productService.getProduct(storeId, createProductResponseDto.id());
        assertThat(deleteProductResponseDto.id(), is(createProductResponseDto.id()));
        assertThat(deleteProductResponseDto.deleted(), is(Boolean.TRUE));
        assertThat(productDto, nullValue());
    }

    @Test
    void publishProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("swa"), 0)).id();
        CreateProductDto createProductDto = new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), new ImageLink("google.com"));
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, categoryId, createProductDto);
        DetailedProductDto detailedProduct = productService.getDetailedProduct(storeId, createProductResponseDto.id());
        assertThat(detailedProduct, nullValue());
        PublishProductResponseDto publishProductResponseDto = productService.publishProduct(storeId, createProductResponseDto.id());
        DetailedProductDto publishedDetailedProduct = productService.getDetailedProduct(storeId, createProductResponseDto.id());
        assertThat(publishProductResponseDto.published(), is(Boolean.TRUE));
        assertThat(publishedDetailedProduct, notNullValue());
    }

    @Test
    void unPublishProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("swa"), 0)).id();
        CreateProductDto createProductDto = new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), new ImageLink("google.com"));
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, categoryId, createProductDto);
        DetailedProductDto detailedProduct = productService.getDetailedProduct(storeId, createProductResponseDto.id());
        assertThat(detailedProduct, nullValue());
        PublishProductResponseDto publishProductResponseDto = productService.unPublishProduct(storeId, createProductResponseDto.id());
        DetailedProductDto publishedDetailedProduct = productService.getDetailedProduct(storeId, createProductResponseDto.id());
        assertThat(publishProductResponseDto.published(), is(Boolean.FALSE));
        assertThat(publishedDetailedProduct, nullValue());
    }

    @Test
    void updateProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("swa"), 0)).id();
        CreateProductDto createProductDto = new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), new ImageLink("google.com"));
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, categoryId, createProductDto);
        UpdateProductDto updateProductDto = new UpdateProductDto("pnew", null, null, null);
        UpdateProductResponseDto updateProductResponseDto = productService.updateProduct(storeId, createProductResponseDto.id(), categoryId, updateProductDto);
        assertThat(updateProductDto.name(), is(updateProductDto.name()));
        assertThat(createProductDto.price(), is(updateProductResponseDto.price()));
        assertThat(createProductDto.imageLink(), is(updateProductResponseDto.imageLink()));
    }

    @Test
    void addImage() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("swa"), 0)).id();
        CreateProductDto createProductDto = new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), new ImageLink("google.com"));
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, categoryId, createProductDto);
        ImageLink imageLink = new ImageLink("new imageLink");
        AddProductImageResponseDto addProductImageResponseDto = productService.addImage(storeId, createProductResponseDto.id(), imageLink);
        assertThat(addProductImageResponseDto.imageLink(), equalTo(imageLink));
    }

    @Test
    void addVariant() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("swa"), 0)).id();
        CreateProductDto createProductDto = new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), new ImageLink("google.com"));
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, categoryId, createProductDto);
        AddProductVariantDto addProductVariantDto = new AddProductVariantDto(new ProductPrice(66D, Currency.getInstance("USD")), new ProductAmount(20), Map.of("COLOR", "RED"));
        AddProductVariantResponseDto addProductVariantResponseDto = productService.addVariant(storeId, createProductResponseDto.id(), addProductVariantDto);
        assertThat(addProductVariantResponseDto.id(), notNullValue());
        assertThat(addProductVariantResponseDto.amount(), is(addProductVariantDto.amount()));
        assertThat(addProductVariantResponseDto.price(), is(addProductVariantDto.price()));
        assertThat(addProductVariantResponseDto.features(), is(addProductVariantDto.features()));
    }

    @Test
    void getVariant() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("swa"), 0)).id();
        CreateProductDto createProductDto = new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), new ImageLink("google.com"));
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, categoryId, createProductDto);
        AddProductVariantDto addProductVariantDto = new AddProductVariantDto(new ProductPrice(66D, Currency.getInstance("USD")), new ProductAmount(20), Map.of("COLOR", "RED"));
        AddProductVariantResponseDto addProductVariantResponseDto = productService.addVariant(storeId, createProductResponseDto.id(), addProductVariantDto);
        ProductVariantDto productVariant = productService.getProductVariant(storeId, createProductResponseDto.id(), addProductVariantResponseDto.id());
        assertThat(productVariant, notNullValue());
    }
}
