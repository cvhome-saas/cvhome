package com.asrevo.cvhome.product.repository;

import com.asrevo.cvhome.storepod.commons.domain.ProductId;
import com.asrevo.cvhome.product.entity.ProductEntity;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends ListCrudRepository<ProductEntity, ProductId>, QueryByExampleExecutor<ProductEntity> {
    Page<ProductEntity> findAllByStoreId(StoreId storeId, Pageable pageable);

    Optional<ProductEntity> findOneByStoreIdAndIdAndDeletedIsFalse(StoreId storeId, ProductId productId);

    List<ProductEntity> findOneByStoreIdAndIdInAndDeletedIsFalse(StoreId storeId, List<ProductId> productIds);

}
