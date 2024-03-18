package com.asrevo.cvhome.store.repository;

import com.asrevo.cvhome.store.commons.domain.ImageId;
import com.asrevo.cvhome.store.entity.ImageEntity;
import com.asrevo.cvhome.manager.commons.domain.StoreId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;

public interface ImageRepository extends ListCrudRepository<ImageEntity, ImageId> {
    Page<ImageEntity> findByStoreId(StoreId storeId, Pageable pageable);

    Page<ImageEntity> findByStoreIdAndNameIsContainingIgnoreCase(StoreId storeId, String name, Pageable pageable);
}
