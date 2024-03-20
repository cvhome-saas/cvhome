package com.asrevo.cvhome.store.service;

import com.asrevo.cvhome.storepod.commons.domain.StoreId;
import com.asrevo.cvhome.store.commons.dto.*;
import com.asrevo.cvhome.storepod.commons.domain.ProductId;
import com.asrevo.cvhome.storepod.commons.dto.DetailedProductDto;
import com.asrevo.cvhome.storepod.commons.dto.FindAllProductDto;
import com.asrevo.cvhome.storepod.commons.dto.ProductDto;
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
