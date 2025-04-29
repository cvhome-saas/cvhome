package com.asrevo.cvhome.order.repositories.shoppingcart;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.shoppingcart.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    @Query(
            """
                    select c from ShoppingCart c
                    left join fetch c.lineItems cl
                    where c.storeMerchantId = ?1 and c.id = ?2""")
    ShoppingCart findById(StoreMerchantId storeMerchantId, Long id);

    @Query(
            """
                    select c from ShoppingCart c
                    left join fetch c.lineItems cl
                    where c.storeMerchantId = ?1 and c.shoppingCartCode = ?2""")
    ShoppingCart findByCode(StoreMerchantId storeMerchantId, String code);
}
