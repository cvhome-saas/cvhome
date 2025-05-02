package com.asrevo.cvhome.catalog.repositories.product.relationship;

import com.asrevo.cvhome.catalog.entity.product.relationship.ProductRelationship;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRelationshipRepository
        extends JpaRepository<ProductRelationship, Long>, ProductRelationshipRepositoryCustom {
    @Modifying
    @Query(
            "delete from ProductRelationship  pr where pr.relatedProduct.id=:relatedProduct  and"
                    + " pr.code=:code and pr.storeMerchantId=:storeMerchantId")
    int deleteAllByRelatedProduct_IdAndCodeAndStoreMerchantId(
            @Param("relatedProduct") Long relatedProduct,
            @Param("code") String code,
            @Param("storeMerchantId") StoreMerchantId storeMerchantId);

    @Modifying
    @Query(
            "delete from ProductRelationship  pr where pr.relatedProduct.id=:relatedProduct and"
                    + " pr.product.id=:productId and pr.code=:code and"
                    + " pr.storeMerchantId=:storeMerchantId")
    int deleteAllByRelatedProduct_IdAndProduct_IdAndCodeAndStoreMerchantId(
            @Param("relatedProduct") Long relatedProduct,
            @Param("productId") Long productId,
            @Param("code") String code,
            @Param("storeMerchantId") StoreMerchantId storeMerchantId);
}
