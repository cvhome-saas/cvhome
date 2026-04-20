package com.asrevo.cvhome.catalog.repositories.product.type;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface PageableProductTypeRepository extends PagingAndSortingRepository<ProductType, Long> {

    Page<ProductType> findByStoreMerchantId(StoreMerchantId storeMerchantId, Pageable pageable);

}
