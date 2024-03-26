package com.asrevo.cvhome.store.repository;

import com.asrevo.cvhome.store.commons.domain.ProductDetailsId;
import com.asrevo.cvhome.store.entity.ProductDetailsEntity;
import com.asrevo.cvhome.storepod.commons.domain.ProductId;
import com.asrevo.cvhome.storepod.commons.domain.StoreId;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface ProductDetailsRepository extends ListCrudRepository<ProductDetailsEntity, ProductDetailsId> {
    Optional<ProductDetailsEntity> findByStoreIdAndAndProduct(StoreId storeId, ProductId productId);

    List<ProductDetailsEntity> findAllByStoreIdAndAndProductIn(StoreId storeId, List<ProductId> productIds);
}
