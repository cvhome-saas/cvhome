package com.asrevo.cvhome.catalog.repositories.product.relationship;

import com.asrevo.cvhome.catalog.entity.product.relationship.ProductRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRelationshipRepository
        extends JpaRepository<ProductRelationship, Long>, ProductRelationshipRepositoryCustom {}
