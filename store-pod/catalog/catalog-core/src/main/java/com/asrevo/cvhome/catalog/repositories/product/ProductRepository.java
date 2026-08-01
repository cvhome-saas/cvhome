package com.asrevo.cvhome.catalog.repositories.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;

public interface ProductRepository
        extends JpaRepository<Product, Long>, ProductRepositoryCustom, JpaSpecificationExecutor<Product> {

    String ID_FIELD = "id";

    @Query(value = """
            SELECT
            CASE WHEN COUNT(*) > 0 THEN true ELSE false END
            FROM
            Product p
            LEFT JOIN ProductVariant pv ON pv.product.id = p.id
            WHERE (pv.sku = ?1 OR p.sku = ?1) and p.store = ?2""")
    boolean existsBySku(String sku, StoreMerchantId storeMerchantId);

    @Query(value = "select p.id from Product p left join p.variants pv where (p.sku=?1 or"
            + " pv.sku=?1) and p.store=?2")
    List<Long> findBySku(String sku, StoreMerchantId merchantStoreId);

    default Page<Product> findAll(ProductCriteria productCriteria, StoreMerchantId storeMerchantId, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("store"), storeMerchantId));
            if (LanguageCode.isLanguage(productCriteria.getLanguage())) {
                predicates.add(cb.equal(root.get("descriptions").get("languageCode"), productCriteria.getLanguage()));
            }
            if (Objects.nonNull(productCriteria.getAvailable())) {
                predicates.add(cb.equal(root.get("available"), productCriteria.getAvailable()));
            }
            if (Objects.nonNull(productCriteria.getSku()) && !productCriteria.getSku().isEmpty()) {
                predicates.add(cb.like(root.get("sku"), Constants.PERCENT_SYMBOL + productCriteria.getSku() + Constants.PERCENT_SYMBOL));
            }
            if (Objects.nonNull(productCriteria.getManufacturerId())) {
                predicates.add(cb.equal(root.get("manufacturer").get(ID_FIELD), productCriteria.getManufacturerId()));
            }
            if (productCriteria.getCategoryIds() != null && !productCriteria.getCategoryIds().isEmpty()) {
                predicates.add(root.join("categories").get(ID_FIELD).in(productCriteria.getCategoryIds()));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return findAll(spec, pageable);
    }

    @EntityGraph(attributePaths = {"availabilities"})
    @Query("SELECT p FROM Product p WHERE p.sku = :sku AND p.store = :store")
    Product getByProductSkuFetchAvailabilities(@Param("sku") String sku,
                                               @Param("store") StoreMerchantId storeMerchantId);

}
