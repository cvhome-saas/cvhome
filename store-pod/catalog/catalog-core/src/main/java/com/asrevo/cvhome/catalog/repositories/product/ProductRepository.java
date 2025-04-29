package com.asrevo.cvhome.catalog.repositories.product;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    @Query(
            value =
                    """
                            SELECT
                            CASE WHEN COUNT(*) > 0 THEN true ELSE false END
                            FROM
                            Product p
                            LEFT JOIN ProductVariant pv ON pv.product.id = p.id
                            WHERE (pv.sku = ?1 OR p.sku = ?1) and p.store = ?2""")
    boolean existsBySku(String sku, StoreMerchantId storeMerchantId);

    @Query(
            value =
                    "select p.id from Product p left join p.variants pv where (p.sku=?1 or"
                            + " pv.sku=?1) and p.store=?2")
    List<Long> findBySku(String sku, StoreMerchantId merchantStoreId);
}
