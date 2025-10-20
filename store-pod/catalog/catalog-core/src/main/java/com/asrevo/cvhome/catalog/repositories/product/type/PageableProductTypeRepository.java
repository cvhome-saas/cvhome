package com.asrevo.cvhome.catalog.repositories.product.type;

import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PageableProductTypeRepository extends PagingAndSortingRepository<ProductType, Long> {

	Page<ProductType> findByStoreMerchantId(StoreMerchantId storeMerchantId, Pageable pageable);

}
