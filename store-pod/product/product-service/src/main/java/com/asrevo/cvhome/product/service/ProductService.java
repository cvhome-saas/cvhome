package com.asrevo.cvhome.product.service;

import com.asrevo.cvhome.store.commons.domain.StoreId;
import com.asrevo.cvhome.storepod.commons.domain.ProductId;
import com.asrevo.cvhome.storepod.commons.dto.DetailedProductDto;
import com.asrevo.cvhome.storepod.commons.dto.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductDto> findAll(StoreId storeId, Pageable pageable);

    DetailedProductDto getDetailedProduct(StoreId storeId, ProductId productId);
}
