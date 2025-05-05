package com.asrevo.cvhome.catalog.repositories.product.relationship;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.relationship.ProductRelationship;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRelationshipRepository
        extends JpaRepository<ProductRelationship, Long>,
                ProductRelationshipRepositoryCustom,
                JpaSpecificationExecutor<ProductRelationship> {
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

    default List<ProductRelationship> ss(
            StoreMerchantId storeMerchantId,
            String type,
            Product relatedProduct,
            LanguageCode languageCode) {
        return findAll(
                (Specification<ProductRelationship>)
                        (root, query, cb) -> {
                            List<Predicate> predicates = new ArrayList<>();
                            predicates.add(cb.equal(root.get("storeMerchantId"), storeMerchantId));
                            if (LanguageCode.isLanguage(languageCode)) {
                                predicates.add(
                                        cb.equal(
                                                root.get("relatedProduct")
                                                        .get("descriptions")
                                                        .get("languageCode"),
                                                languageCode));
                            }
                            predicates.add(cb.equal(root.get("code"), type));
                            if (Objects.nonNull(relatedProduct)) {
                                predicates.add(
                                        cb.equal(
                                                root.get("relatedProduct").get("id"),
                                                relatedProduct.getId()));
                            }
                            return cb.and(predicates.toArray(Predicate[]::new));
                        });
    }
}
