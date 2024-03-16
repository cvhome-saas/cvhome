package com.asrevo.cvhome.product.repository;

import com.asrevo.cvhome.product.entity.CategoryEntity;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import com.asrevo.cvhome.storepod.commons.domain.CategoryId;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends ListCrudRepository<CategoryEntity, CategoryId> {
    List<CategoryEntity> findALlByStoreId(StoreId storeId);

    Optional<CategoryEntity> findByIdAndStoreId(CategoryId id, StoreId storeId);
}
