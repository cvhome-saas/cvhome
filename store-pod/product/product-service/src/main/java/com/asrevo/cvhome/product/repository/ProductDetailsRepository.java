package com.asrevo.cvhome.product.repository;

import com.asrevo.cvhome.product.commons.domain.ProductDetailsId;
import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.entity.ProductDetailsEntity;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface ProductDetailsRepository extends ListCrudRepository<ProductDetailsEntity, ProductDetailsId> {
    Optional<ProductDetailsEntity> findByStoreIdAndAndProduct(StoreId storeId, ProductId productId);
}
