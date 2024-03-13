package com.asrevo.cvhome.product.repository;

import com.asrevo.cvhome.product.commons.domain.ProductImageId;
import com.asrevo.cvhome.product.entity.ProductImageEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface ProductImageRepository extends ListCrudRepository<ProductImageEntity, ProductImageId> {
}
