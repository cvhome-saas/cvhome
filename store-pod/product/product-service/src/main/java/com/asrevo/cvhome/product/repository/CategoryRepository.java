package com.asrevo.cvhome.product.repository;

import com.asrevo.cvhome.product.commons.domain.CategoryId;
import com.asrevo.cvhome.product.entity.CategoryEntity;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends ListCrudRepository<CategoryEntity, CategoryId> {
    List<CategoryEntity> findALlByStoreId(StoreId storeId);

    Optional<CategoryEntity> findByIdAndStoreId(CategoryId id, StoreId storeId);
}
