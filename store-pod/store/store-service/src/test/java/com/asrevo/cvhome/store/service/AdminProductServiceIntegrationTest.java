package com.asrevo.cvhome.store.service;

import com.asrevo.cvhome.store.commons.domain.*;
import com.asrevo.cvhome.store.commons.dto.*;
import com.asrevo.cvhome.store.mappers.CategoryMapperImpl;
import com.asrevo.cvhome.store.mappers.ProductMapperImpl;
import com.asrevo.cvhome.store.service.impl.AdminCategoryServiceImpl;
import com.asrevo.cvhome.store.service.impl.AdminProductServiceImpl;
import com.asrevo.cvhome.store.utils.ErrorCodes;
import com.asrevo.cvhome.manager.commons.domain.StoreId;
import com.asrevo.cvhome.storepod.commons.domain.*;
import com.asrevo.cvhome.storepod.commons.dto.DetailedProductDto;
import com.asrevo.cvhome.storepod.commons.dto.FindAllProductDto;
import com.asrevo.cvhome.storepod.commons.dto.ProductDto;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
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
@Import({JacksonAutoConfiguration.class, AdminProductServiceImpl.class, ProductMapperImpl.class, AdminCategoryServiceImpl.class, CategoryMapperImpl.class})
@Testcontainers
@Tag("integration-test")
class AdminProductServiceIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");
    @Autowired
    private AdminProductService productService;
    @Autowired
    private AdminCategoryService categoryService;


    private static CreateProductDto getCreateProductDto(CategoryId categoryId, ProductType productType) {
        return new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), new ImageLink("https://google.com/product.png"), new ProductAmount(10), productType, ImagesLink.empty(), categoryId);
    }

    private static CreateProductDto getCreateProductDto(CategoryId categoryId) {
        return new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), new ImageLink("https://google.com/product.png"), new ProductAmount(10), ProductType.SINGLE, ImagesLink.empty(), categoryId);
    }

    @Test
    void findAll() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        IntStream.range(0, 20).forEach(it -> {
            CreateProductDto createProductDto = new CreateProductDto("p" + it, "d1" + it, new ProductPrice(50D * it, Currency.getInstance("USD")), new ImageLink("https://google.com/product.png"), new ProductAmount(10), ProductType.SINGLE, ImagesLink.empty(), categoryId);
            productService.createProduct(storeId, new CreateDetailedProductDto(createProductDto));
        });
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<ProductDto> page = productService.findAll(storeId, new FindAllProductDto(CategoryId.newId(), Boolean.TRUE, "new product", ProductType.GROUP), pageRequest);
        assertThat(page.getSize(), is(pageRequest.getPageSize()));
    }

    @Test
    void createProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductDto createProductDto = getCreateProductDto(categoryId);
        DetailedProductDto detailedProductDto = productService.createProduct(storeId, new CreateDetailedProductDto(createProductDto));
        ProductDto dto = detailedProductDto.dto();
        assertThat(dto.published(), is(Boolean.FALSE));
        assertThat(dto.id(), notNullValue());
        assertThat(dto.price(), is(createProductDto.price()));
        assertThat(dto.imageLink(), is(createProductDto.imageLink()));
    }

    @Test
    void createProductWithoutCategory() {
        assertThrows(ErrorCodes.category_not_exist.message(), RuntimeException.class, () -> productService.createProduct(StoreId.newId(), new CreateDetailedProductDto(getCreateProductDto(CategoryId.newId()))));
    }


    @Test
    void getProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductDto createProductDto = getCreateProductDto(categoryId);
        DetailedProductDto detailedProductDto = productService.createProduct(storeId, new CreateDetailedProductDto(createProductDto));
        ProductDto dto = detailedProductDto.dto();
        ProductDto productDto = productService.getProduct(storeId, dto.id());
        assertThat(dto.id(), is(productDto.id()));
        assertThat(dto.name(), is(createProductDto.name()));
        assertThat(dto.description(), is(createProductDto.description()));
        assertThat(dto.price(), is(createProductDto.price()));
        assertThat(dto.imageLink(), is(createProductDto.imageLink()));
    }

    @Test
    void deleteProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductDto createProductDto = getCreateProductDto(categoryId);
        DetailedProductDto detailedProductDto = productService.createProduct(storeId, new CreateDetailedProductDto(createProductDto));
        DeleteProductResponseDto deleteProductResponseDto = productService.deleteProduct(storeId, detailedProductDto.id());

        assertThat(deleteProductResponseDto.deleted(), is(Boolean.TRUE));
        assertThat(deleteProductResponseDto.id(), is(detailedProductDto.id()));
        assertThrows(ErrorCodes.product_not_exist.message(), RuntimeException.class, () -> productService.getProduct(storeId, detailedProductDto.id()));
    }

    @Test
    void publishProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductDto createProductDto = getCreateProductDto(categoryId);
        ProductDetails details = new ProductDetails(new ProductDetail("sa", "wa", List.of(), Map.of(), Boolean.FALSE), new ImagesLink(List.of(new ImageLink("https://google.com/421.png"))));
        DetailedProductDto detailedProductDto = productService.createProduct(storeId, new CreateDetailedProductDto(createProductDto, details, null));
        assertThat(detailedProductDto, notNullValue());
        assertThat(detailedProductDto.productDetails(), notNullValue());
        PublishProductResponseDto publishProductResponseDto = productService.publishProduct(storeId, detailedProductDto.id());
        assertThat(publishProductResponseDto.published(), is(Boolean.TRUE));
    }


    @Test
    void publishMultiProductWithSubProductsAllHaveDetails() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();

        ProductDetails details = new ProductDetails(new ProductDetail("sa", "wa", List.of(), Map.of(), Boolean.FALSE), new ImagesLink(List.of(new ImageLink("https://google.com/421.png"))));

        DetailedProductDto subProduct1 = productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId), details, null));
        DetailedProductDto subProduct2 = productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId), details, null));
        DetailedProductDto subProduct3 = productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId), details, null));


        List<DetailedProductDto> subProducts = new SubProducts(subProduct1.id(), subProduct2.id(), subProduct3.id()).productIds()
                .stream()
                .map(it -> new DetailedProductDto(it, null, null, null))
                .toList();

        DetailedProductDto parentProduct = productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId, ProductType.GROUP), details, subProducts));

        productService.publishProduct(storeId, parentProduct.id());
    }

    @Test
    void publishNotExistProduct() {
        assertThrows(ErrorCodes.product_not_exist.message(), RuntimeException.class, () -> productService.publishProduct(StoreId.newId(), ProductId.newId()));
    }

    @Test
    void publishProductWithNoDetails() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductDto createProductDto = getCreateProductDto(categoryId);
        DetailedProductDto detailedProductDto = productService.createProduct(storeId, new CreateDetailedProductDto(createProductDto));
        assertThrows(ErrorCodes.product_details_not_created_yet.message(), RuntimeException.class, () -> productService.publishProduct(storeId, detailedProductDto.id()));
    }

 /*   @Test
    void addProductDetails() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductDto createProductDto = getCreateProductDto(categoryId);
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, createProductDto);
        ProductDetails productDetails = new ProductDetails(new ProductDetail("sa", "wa", List.of(), Map.of(), Boolean.FALSE), new ImagesLink(List.of(new ImageLink("https://google.com/421.png"))));
        ProductDetails productDetailsResponse = productService.addProductDetails(storeId, createProductResponseDto.id(), productDetails);
        assertThat(productDetails, is(productDetailsResponse));
    }*/

    @Test
    void unPublishProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        CreateProductDto createProductDto = getCreateProductDto(categoryId);
        ProductDetails details = new ProductDetails(new ProductDetail("sa", "wa", List.of(), Map.of(), Boolean.FALSE), new ImagesLink(List.of(new ImageLink("https://google.com/421.png"))));
        DetailedProductDto detailedProductDto = productService.createProduct(storeId, new CreateDetailedProductDto(createProductDto, details, null));
        productService.publishProduct(storeId, detailedProductDto.id());
        PublishProductResponseDto publishProductResponseDto = productService.unPublishProduct(storeId, detailedProductDto.id());
        assertThat(publishProductResponseDto.published(), is(Boolean.FALSE));
    }

    @Test
    void createGroupProductWithDifferentStore() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        StoreId anotherstoreId = StoreId.newId();
        CategoryId anothercategoryId = categoryService.createCategory(anotherstoreId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        DetailedProductDto subProduct1 = productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId)));
        DetailedProductDto subProduct2 = productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId)));
        DetailedProductDto subProduct3 = productService.createProduct(anotherstoreId, new CreateDetailedProductDto(getCreateProductDto(anothercategoryId)));

        List<DetailedProductDto> subProducts = new SubProducts(subProduct1.id(), subProduct2.id(), subProduct3.id()).productIds()
                .stream()
                .map(it -> new DetailedProductDto(it, null, null, null))
                .toList();

        assertThrows(ErrorCodes.one_or_more_sub_product_not_from_this_store.message(), RuntimeException.class, () -> {
            productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId, ProductType.GROUP), null, subProducts)/*getCreateProductDto(categoryId, ProductType.GROUP, subProducts)*/);
        });
    }

    @Test
    void createSingleWithMultipleProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        DetailedProductDto subProduct1 = productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId)));
        DetailedProductDto subProduct2 = productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId)));
        DetailedProductDto subProduct3 = productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId)));

        List<DetailedProductDto> subProducts = new SubProducts(subProduct1.id(), subProduct2.id(), subProduct3.id()).productIds()
                .stream()
                .map(it -> new DetailedProductDto(it, null, null, null))
                .toList();

        assertThrows(ErrorCodes.single_product_should_not_have_sub_product.message(), RuntimeException.class, () -> {
            productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId, ProductType.SINGLE), null, subProducts)/*getCreateProductDto(categoryId, ProductType.GROUP, subProducts)*/);
        });

    }

    @Test
    void createGroupProduct() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();
        DetailedProductDto subProduct1 = productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId)));
        DetailedProductDto subProduct2 = productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId)));
        DetailedProductDto subProduct3 = productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId)));


        List<DetailedProductDto> subProducts = new SubProducts(subProduct1.id(), subProduct2.id(), subProduct3.id()).productIds()
                .stream()
                .map(it -> new DetailedProductDto(it, null, null, null))
                .toList();

        DetailedProductDto pProduct = productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId, ProductType.GROUP), null, subProducts)/*getCreateProductDto(categoryId, ProductType.GROUP, subProducts)*/);
        assertThat(pProduct.dto().productType(), is(ProductType.GROUP));
        assertThat(pProduct.subProducts().size(), is(equalTo(subProducts.size())));

    }

    @Test
    void publishProductWithSubProductsOneOfThoseNotHaveProductDetails() {
        StoreId storeId = StoreId.newId();
        CategoryId categoryId = categoryService.createCategory(storeId, new CreateCategoryDto("ssa", new ImageLink("https://google.com/image.jpg"), 0)).id();

        DetailedProductDto subProduct1 = productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId)));
        DetailedProductDto subProduct2 = productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId)));
        DetailedProductDto subProduct3 = productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId)));

        List<DetailedProductDto> subProducts = new SubProducts(subProduct1.id(), subProduct2.id(), subProduct3.id()).productIds()
                .stream()
                .map(it -> new DetailedProductDto(it, null, null, null))
                .toList();
        ProductDetails details = new ProductDetails(new ProductDetail("sa", "wa", List.of(), Map.of(), Boolean.FALSE), new ImagesLink(List.of(new ImageLink("https://google.com/421.png"))));

        DetailedProductDto parentProduct = productService.createProduct(storeId, new CreateDetailedProductDto(getCreateProductDto(categoryId, ProductType.GROUP), details, subProducts));
        assertThrows(ErrorCodes.one_of_your_sub_products_not_have_product_details_yet.message(), RuntimeException.class, () -> productService.publishProduct(storeId, parentProduct.id()));
    }

}
