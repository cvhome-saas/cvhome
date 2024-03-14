package com.asrevo.cvhome.product.repository;

import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.entity.ProductEntity;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface ProductRepository extends ListCrudRepository<ProductEntity, ProductId> {
    Page<ProductEntity> findAllByStoreId(StoreId storeId, Pageable pageable);

    Optional<ProductEntity> findOneByStoreIdAndIdAndDeletedIsFalse(StoreId storeId, ProductId productId);

    Optional<ProductEntity> findOneByStoreIdAndIdAndDeletedIsFalseAndPublishedIsTrue(StoreId storeId, ProductId productId);
}
