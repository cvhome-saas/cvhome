package com.asrevo.cvhome.product.repository;

import com.asrevo.cvhome.product.commons.domain.ImageId;
import com.asrevo.cvhome.product.entity.ImageEntity;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;

public interface ImageRepository extends ListCrudRepository<ImageEntity, ImageId> {
    Page<ImageEntity> findByStoreId(StoreId storeId, Pageable pageable);

    Page<ImageEntity> findByStoreIdAndNameIsContainingIgnoreCase(StoreId storeId, String name, Pageable pageable);
}
