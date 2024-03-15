package com.asrevo.cvhome.product.service;

import com.asrevo.cvhome.product.commons.domain.CategoryId;
import com.asrevo.cvhome.product.commons.domain.ProductDetails;
import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.dto.*;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminProductService {
    List<ProductDto> findAll(StoreId storeId, Pageable pageable);

    CreateProductResponseDto createProduct(StoreId storeId, CategoryId categoryId, CreateProductDto createProductDto);

    DeleteProductResponseDto deleteProduct(StoreId storeId, ProductId productId);

    ProductDetails addProductDetails(StoreId storeId, ProductId productId, ProductDetails productDetails);

    PublishProductResponseDto publishProduct(StoreId storeId, ProductId productId);

    PublishProductResponseDto unPublishProduct(StoreId storeId, ProductId productId);

    ProductDto getProduct(StoreId storeId, ProductId productId);

    UpdateProductResponseDto updateProduct(StoreId storeId, ProductId productId, CategoryId categoryId, UpdateProductDto updateProductDto);

}
