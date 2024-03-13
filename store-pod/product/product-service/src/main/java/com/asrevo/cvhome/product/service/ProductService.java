package com.asrevo.cvhome.product.service;

import com.asrevo.cvhome.product.commons.domain.ImageLink;
import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.domain.ProductVariantId;
import com.asrevo.cvhome.product.commons.dto.*;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    List<ProductDto> findAll(StoreId storeId, Pageable pageable);

    CreateProductResponseDto createProduct(StoreId storeId, CreateProductDto createProductDto);
    DeleteProductResponseDto deleteProduct(StoreId storeId, ProductId productId);
    ProductDto getProduct(StoreId storeId, ProductId productId);

    UpdateProductResponseDto updateProduct(StoreId storeId, ProductId productId, UpdateProductDto updateProductDto);

    AddProductImageResponseDto addImage(StoreId storeId, ProductId productId, ImageLink imageLink);

    AddProductVariantResponseDto addVariant(StoreId storeId, ProductId productId, AddProductVariantDto addProductVariantDto);

    ProductVariantDto getProductVariant(StoreId storeId, ProductId productId, ProductVariantId variantId);
}
