package com.asrevo.cvhome.product.service;

import com.asrevo.cvhome.product.commons.domain.*;
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
import static org.junit.Assert.assertThrows;


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


    private static CreateProductDto getCreateProductDto(ProductType productType, SubProducts subProducts) {
        return new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), new ImageLink("https://google.com/product.png"), new ProductAmount(10), productType, subProducts);
    }

    private static CreateProductDto getCreateProductDto() {
        return new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), new ImageLink("https://google.com/product.png"), new ProductAmount(10), ProductType.SINGLE, SubProducts.empty());
    }

    @Test
    void findAll() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        IntStream.range(0, 20).forEach(it -> {
            CreateProductDto createProductDto = new CreateProductDto("p" + it, "d1" + it, new ProductPrice(50D * it, Currency.getInstance("USD")), new ImageLink("https://google.com/product.png"), new ProductAmount(10), ProductType.SINGLE, SubProducts.empty());
            productService.createProduct(storeId, categoryId, createProductDto);
        });
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<ProductDto> all = productService.findAll(storeId, pageRequest);
        assertThat(all.size(), is(pageRequest.getPageSize()));
    }

    @Test
    void createProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductDto createProductDto = getCreateProductDto();
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, categoryId, createProductDto);
        assertThat(createProductResponseDto.published(), is(Boolean.FALSE));
        assertThat(createProductResponseDto.id(), notNullValue());
        assertThat(createProductResponseDto.price(), is(createProductDto.price()));
        assertThat(createProductResponseDto.imageLink(), is(createProductDto.imageLink()));
    }

    @Test
    void createProductWithoutCategory() {
        assertThrows("category not exist", RuntimeException.class, () -> productService.createProduct(StoreId.newId(), CategoryId.newId(), getCreateProductDto()));
    }

    @Test
    void createGroupProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductResponseDto subProduct1 = productService.createProduct(storeId, categoryId, getCreateProductDto());
        CreateProductResponseDto subProduct2 = productService.createProduct(storeId, categoryId, getCreateProductDto());
        CreateProductResponseDto subProduct3 = productService.createProduct(storeId, categoryId, getCreateProductDto());

        SubProducts subProducts = new SubProducts(subProduct1.id(), subProduct2.id(), subProduct3.id());

        CreateProductResponseDto parentProduct = productService.createProduct(storeId, categoryId, getCreateProductDto(ProductType.GROUP, subProducts));
        assertThat(parentProduct.productType(), is(ProductType.GROUP));
        assertThat(parentProduct.subProducts().size(), is(equalTo(subProducts.size())));

    }

    @Test
    void createGroupProductWithDifferentStore() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        StoreId anotherstoreId = StoreId.newId();
        CategoryId anothercategoryId = categoryService.createCategory(anotherstoreId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductResponseDto subProduct1 = productService.createProduct(storeId, categoryId, getCreateProductDto());
        CreateProductResponseDto subProduct2 = productService.createProduct(storeId, categoryId, getCreateProductDto());
        CreateProductResponseDto subProduct3 = productService.createProduct(anotherstoreId, anothercategoryId, getCreateProductDto());
        SubProducts subProducts = new SubProducts(subProduct1.id(), subProduct2.id(), subProduct3.id());
        assertThrows("one or more sub product not from this store", RuntimeException.class, () -> productService.createProduct(storeId, categoryId, getCreateProductDto(ProductType.GROUP, subProducts)));
    }

    @Test
    void createGroupProductWithGroupOrVariantSubProducts() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductResponseDto subProduct1 = productService.createProduct(storeId, categoryId, getCreateProductDto());
        CreateProductResponseDto subProduct2 = productService.createProduct(storeId, categoryId, getCreateProductDto());
        CreateProductResponseDto subProduct3 = productService.createProduct(storeId, categoryId, getCreateProductDto(ProductType.GROUP, new SubProducts(subProduct2.id())));
        SubProducts subProducts = new SubProducts(subProduct1.id(), subProduct2.id(), subProduct3.id());
        assertThrows("all sub products should be single type not GROUP OR VARIANT", RuntimeException.class, () -> productService.createProduct(storeId, categoryId, getCreateProductDto(ProductType.GROUP, subProducts)));
    }

    @Test
    void createSingleWithMultipleProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductResponseDto subProduct1 = productService.createProduct(storeId, categoryId, getCreateProductDto());
        CreateProductResponseDto subProduct2 = productService.createProduct(storeId, categoryId, getCreateProductDto());
        CreateProductResponseDto subProduct3 = productService.createProduct(storeId, categoryId, getCreateProductDto());
        SubProducts subProducts = new SubProducts(subProduct1.id(), subProduct2.id(), subProduct3.id());
        assertThrows("single product should not have sub product", RuntimeException.class, () ->
                productService.createProduct(storeId, categoryId, getCreateProductDto(ProductType.SINGLE, subProducts)));

    }

    @Test
    void getProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductDto createProductDto = getCreateProductDto();
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
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductDto createProductDto = getCreateProductDto();
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
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductDto createProductDto = getCreateProductDto();
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, categoryId, createProductDto);
        ProductDetails details = new ProductDetails(Map.of(DetailsLanguage.EN, new ProductDetail("sa", "wa", List.of(), Map.of(), Boolean.FALSE)), new ImagesLink(List.of(new ImageLink("https://google.com/421.png"))));
        ProductDetails productDetails = productService.addProductDetails(storeId, createProductResponseDto.id(), details);
        assertThat(productDetails, notNullValue());
        assertThrows(RuntimeException.class, () -> productService.getDetailedProduct(storeId, createProductResponseDto.id()));
        PublishProductResponseDto publishProductResponseDto = productService.publishProduct(storeId, createProductResponseDto.id());
        DetailedProductDto publishedDetailedProduct = productService.getDetailedProduct(storeId, createProductResponseDto.id());
        assertThat(publishProductResponseDto.published(), is(Boolean.TRUE));
        assertThat(publishedDetailedProduct, notNullValue());
    }

    @Test
    void publishProductWithSubProductsOneOfThoseNotHaveProductDetails() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();

        CreateProductResponseDto subProduct1 = productService.createProduct(storeId, categoryId, getCreateProductDto());
        CreateProductResponseDto subProduct2 = productService.createProduct(storeId, categoryId, getCreateProductDto());
        CreateProductResponseDto subProduct3 = productService.createProduct(storeId, categoryId, getCreateProductDto());

        SubProducts subProducts = new SubProducts(subProduct1.id(), subProduct2.id(), subProduct3.id());

        CreateProductResponseDto parentProduct = productService.createProduct(storeId, categoryId, getCreateProductDto(ProductType.GROUP, subProducts));
        ProductDetails details = new ProductDetails(Map.of(DetailsLanguage.EN, new ProductDetail("sa", "wa", List.of(), Map.of(), Boolean.FALSE)), new ImagesLink(List.of(new ImageLink("https://google.com/421.png"))));
        productService.addProductDetails(storeId, parentProduct.id(), details);
        assertThrows("one of your sub products not have product details yet", RuntimeException.class, () -> productService.publishProduct(storeId, parentProduct.id()));
    }

    @Test
    void publishGroupProductWithSubProductsAllHaveDetails() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();

        CreateProductResponseDto subProduct1 = productService.createProduct(storeId, categoryId, getCreateProductDto());
        CreateProductResponseDto subProduct2 = productService.createProduct(storeId, categoryId, getCreateProductDto());
        CreateProductResponseDto subProduct3 = productService.createProduct(storeId, categoryId, getCreateProductDto());

        SubProducts subProducts = new SubProducts(subProduct1.id(), subProduct2.id(), subProduct3.id());

        CreateProductResponseDto parentProduct = productService.createProduct(storeId, categoryId, getCreateProductDto(ProductType.GROUP, subProducts));
        ProductDetails details = new ProductDetails(Map.of(DetailsLanguage.EN, new ProductDetail("sa", "wa", List.of(), Map.of(), Boolean.FALSE)), new ImagesLink(List.of(new ImageLink("https://google.com/421.png"))));

        productService.addProductDetails(storeId, subProduct1.id(), details);
        productService.addProductDetails(storeId, subProduct2.id(), details);
        productService.addProductDetails(storeId, subProduct3.id(), details);
        productService.addProductDetails(storeId, parentProduct.id(), details);
        productService.publishProduct(storeId, parentProduct.id());
    }

    @Test
    void publishVariantProductWithSubProductsAllHaveDetails() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();

        CreateProductResponseDto subProduct1 = productService.createProduct(storeId, categoryId, getCreateProductDto());
        CreateProductResponseDto subProduct2 = productService.createProduct(storeId, categoryId, getCreateProductDto());
        CreateProductResponseDto subProduct3 = productService.createProduct(storeId, categoryId, getCreateProductDto());

        SubProducts subProducts = new SubProducts(subProduct1.id(), subProduct2.id(), subProduct3.id());

        CreateProductResponseDto parentProduct = productService.createProduct(storeId, categoryId, getCreateProductDto(ProductType.GROUP, subProducts));
        ProductDetails details = new ProductDetails(Map.of(DetailsLanguage.EN, new ProductDetail("sa", "wa", List.of(), Map.of(), Boolean.FALSE)), new ImagesLink(List.of(new ImageLink("https://google.com/421.png"))));

        productService.addProductDetails(storeId, subProduct1.id(), details);
        productService.addProductDetails(storeId, subProduct2.id(), details);
        productService.addProductDetails(storeId, subProduct3.id(), details);
        productService.addProductDetails(storeId, parentProduct.id(), details);
        productService.publishProduct(storeId, parentProduct.id());
    }

    @Test
    void publishNotExistProduct() {
        assertThrows("product not exist", RuntimeException.class, () -> productService.publishProduct(StoreId.newId(), ProductId.newId()));
    }

    @Test
    void publishProductWithNoDetails() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductDto createProductDto = getCreateProductDto();
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, categoryId, createProductDto);
        assertThrows("product details not created yet", RuntimeException.class, () -> productService.publishProduct(storeId, createProductResponseDto.id()));
    }

    @Test
    void addProductDetails() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductDto createProductDto = getCreateProductDto();
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, categoryId, createProductDto);
        ProductDetails productDetails = new ProductDetails(Map.of(), new ImagesLink(List.of()));
        ProductDetails productDetailsResponse = productService.addProductDetails(storeId, createProductResponseDto.id(), productDetails);
        assertThat(productDetails, is(productDetailsResponse));
    }

    @Test
    void unPublishProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductDto createProductDto = getCreateProductDto();
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, categoryId, createProductDto);
        ProductDetails details = new ProductDetails(Map.of(DetailsLanguage.EN, new ProductDetail("sa", "wa", List.of(), Map.of(), Boolean.FALSE)), new ImagesLink(List.of(new ImageLink("https://google.com/421.png"))));
        productService.addProductDetails(storeId, createProductResponseDto.id(), details);
        productService.publishProduct(storeId, createProductResponseDto.id());
        DetailedProductDto detailedProduct = productService.getDetailedProduct(storeId, createProductResponseDto.id());
        assertThat(detailedProduct, notNullValue());
        PublishProductResponseDto publishProductResponseDto = productService.unPublishProduct(storeId, createProductResponseDto.id());
        assertThat(publishProductResponseDto.published(), is(Boolean.FALSE));
        assertThrows(RuntimeException.class, () -> productService.getDetailedProduct(storeId, createProductResponseDto.id()));
    }

    @Test
    void updateProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductDto createProductDto = getCreateProductDto();
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, categoryId, createProductDto);
        UpdateProductDto updateProductDto = new UpdateProductDto("pnew", null, null, null, new ProductAmount(10), ProductType.SINGLE, SubProducts.empty());
        UpdateProductResponseDto updateProductResponseDto = productService.updateProduct(storeId, createProductResponseDto.id(), categoryId, updateProductDto);
        assertThat(updateProductDto.name(), is(updateProductDto.name()));
        assertThat(createProductDto.price(), is(updateProductResponseDto.price()));
        assertThat(createProductDto.imageLink(), is(updateProductResponseDto.imageLink()));
    }

    @Test
    void updatePublishedProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductDto createProductDto = getCreateProductDto();
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, categoryId, createProductDto);
        ProductDetails details = new ProductDetails(Map.of(DetailsLanguage.EN, new ProductDetail("sa", "wa", List.of(), Map.of(), Boolean.FALSE)), new ImagesLink(List.of(new ImageLink("https://google.com/421.png"))));
        productService.addProductDetails(storeId, createProductResponseDto.id(), details);
        productService.publishProduct(storeId, createProductResponseDto.id());
        UpdateProductDto updateProductDto = new UpdateProductDto("pnew", null, null, null, new ProductAmount(10), ProductType.SINGLE, SubProducts.empty());
        UpdateProductResponseDto updateProductResponseDto = productService.updateProduct(storeId, createProductResponseDto.id(), categoryId, updateProductDto);
        assertThat(updateProductDto.name(), is(updateProductDto.name()));
        assertThat(createProductDto.price(), is(updateProductResponseDto.price()));

    }

    @Test
    void updateNonExistProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        UpdateProductDto updateProductDto = new UpdateProductDto("pnew", null, null, null, new ProductAmount(10), ProductType.SINGLE, SubProducts.empty());
        assertThrows("update published product that have details not created yet", RuntimeException.class, () -> productService.updateProduct(storeId, ProductId.newId(), categoryId, updateProductDto));
    }
}
