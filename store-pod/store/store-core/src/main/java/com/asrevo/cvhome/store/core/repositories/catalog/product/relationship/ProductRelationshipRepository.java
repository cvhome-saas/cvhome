package com.asrevo.cvhome.store.core.repositories.catalog.product.relationship;

import com.asrevo.cvhome.store.core.entity.catalog.product.relationship.ProductRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRelationshipRepository
        extends JpaRepository<ProductRelationship, Long>, ProductRelationshipRepositoryCustom {}
