package com.asrevo.cvhome.product.service;

import com.asrevo.cvhome.product.commons.domain.*;
import com.asrevo.cvhome.product.commons.dto.*;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    List<ProductDto> findAll(StoreId storeId, Pageable pageable);

    CreateProductResponseDto createProduct(StoreId storeId, CategoryId categoryId, CreateProductDto createProductDto);

    DeleteProductResponseDto deleteProduct(StoreId storeId, ProductId productId);

    ProductDetails addProductDetails(StoreId storeId, ProductId productId, ProductDetails productDetails);

    PublishProductResponseDto publishProduct(StoreId storeId, ProductId productId);

    PublishProductResponseDto unPublishProduct(StoreId storeId, ProductId productId);

    ProductDto getProduct(StoreId storeId, ProductId productId);

    DetailedProductDto getDetailedProduct(StoreId storeId, ProductId productId);

    DetailedProductDto getDetailedProduct(StoreId storeId, ProductId productId, ProductVariantId variantId);

    UpdateProductResponseDto updateProduct(StoreId storeId, ProductId productId, CategoryId categoryId, UpdateProductDto updateProductDto);

    AddProductImageResponseDto addImage(StoreId storeId, ProductId productId, ImageLink imageLink);

    AddProductVariantResponseDto addVariant(StoreId storeId, ProductId productId, AddProductVariantDto addProductVariantDto);

    ProductVariantDto getProductVariant(StoreId storeId, ProductId productId, ProductVariantId variantId);
}
