package com.asrevo.cvhome.product.service;

import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.dto.*;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminProductService {
    Page<ProductDto> findAll(StoreId storeId, FindAllProductDto findAllProductDto, Pageable pageable);

    DetailedProductDto createProduct(StoreId storeId, CreateDetailedProductDto detailedProductDto);

    DeleteProductResponseDto deleteProduct(StoreId storeId, ProductId productId);

    PublishProductResponseDto publishProduct(StoreId storeId, ProductId productId);

    PublishProductResponseDto unPublishProduct(StoreId storeId, ProductId productId);

    ProductDto getProduct(StoreId storeId, ProductId productId);

    DetailedProductDto getDetailedProduct(StoreId storeId, ProductId productId);
}
