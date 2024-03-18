package com.asrevo.cvhome.store.repository;

import com.asrevo.cvhome.store.entity.CategoryEntity;
import com.asrevo.cvhome.manager.commons.domain.StoreId;
import com.asrevo.cvhome.storepod.commons.domain.CategoryId;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends ListCrudRepository<CategoryEntity, CategoryId> {
    List<CategoryEntity> findALlByStoreId(StoreId storeId);

    Optional<CategoryEntity> findByIdAndStoreId(CategoryId id, StoreId storeId);
}
