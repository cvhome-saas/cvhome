package com.asrevo.cvhome.product.repository;

import com.asrevo.cvhome.product.commons.domain.ProductVariantId;
import com.asrevo.cvhome.product.entity.ProductVariantEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface ProductVariantRepository extends ListCrudRepository<ProductVariantEntity, ProductVariantId> {
}
