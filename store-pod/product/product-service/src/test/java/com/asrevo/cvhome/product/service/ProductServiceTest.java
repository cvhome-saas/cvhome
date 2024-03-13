package com.asrevo.cvhome.product.service;

import com.asrevo.cvhome.product.commons.domain.ProductAmount;
import com.asrevo.cvhome.product.commons.domain.ProductPrice;
import com.asrevo.cvhome.product.commons.dto.*;
import com.asrevo.cvhome.product.mappers.ProductMapperImpl;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JacksonAutoConfiguration.class, ProductServiceImpl.class, ProductMapperImpl.class})
@Testcontainers
@Tag("integration-test")
class ProductServiceTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");
    @Autowired
    private ProductService productService;

    @Test
    void findAll() {
        List<ProductDto> all = productService.findAll(StoreId.newId(), PageRequest.of(0, 10));
        System.out.println(all);
    }

    @Test
    void createProduct() {
        StoreId storeId = StoreId.newId();
        CreateProductDto createProductDto = new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), Boolean.TRUE);
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, createProductDto);
        assertThat(createProductResponseDto.published(), is(createProductDto.published()));
        assertThat(createProductResponseDto.id(), notNullValue());
        assertThat(createProductResponseDto.price(), is(createProductDto.price()));
    }

    @Test
    void updateProduct() {
        StoreId storeId = StoreId.newId();
        CreateProductDto createProductDto = new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), Boolean.TRUE);
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, createProductDto);


        UpdateProductDto updateProductDto = new UpdateProductDto("pnew", null, null, Boolean.FALSE);
        UpdateProductResponseDto updateProductResponseDto = productService.updateProduct(storeId, createProductResponseDto.id(), updateProductDto);


        assertThat(updateProductDto.published(), is(updateProductResponseDto.published()));
        assertThat(updateProductDto.name(), is(updateProductDto.name()));
        assertThat(createProductDto.price(), is(updateProductResponseDto.price()));

    }

    @Test
    void addImage() {
        StoreId storeId = StoreId.newId();
        CreateProductDto createProductDto = new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), Boolean.TRUE);
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, createProductDto);
        String newLink = "new link";
        ProductDto product = productService.addImage(storeId, createProductResponseDto.id(), newLink);
        assertThat(product.images(), hasItem(newLink));

    }

    @Test
    void addVariant() {
        StoreId storeId = StoreId.newId();
        CreateProductDto createProductDto = new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), Boolean.TRUE);
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, createProductDto);

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
        CreateProductDto createProductDto = new CreateProductDto("p1", "d1", new ProductPrice(50D, Currency.getInstance("USD")), Boolean.TRUE);
        CreateProductResponseDto createProductResponseDto = productService.createProduct(storeId, createProductDto);

        AddProductVariantDto addProductVariantDto = new AddProductVariantDto(new ProductPrice(66D, Currency.getInstance("USD")), new ProductAmount(20), Map.of("COLOR", "RED"));
        AddProductVariantResponseDto addProductVariantResponseDto = productService.addVariant(storeId, createProductResponseDto.id(), addProductVariantDto);

        ProductVariantDto productVariant = productService.getProductVariant(storeId, createProductResponseDto.id(), addProductVariantResponseDto.id());
        assertThat(productVariant, notNullValue());
        System.out.println("aaaaaaaaaa");

    }
}
