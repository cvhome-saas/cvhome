package com.asrevo.cvhome.store.core.repositories.shipping;

import com.asrevo.cvhome.store.core.entity.shipping.ShippingOrigin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShippingOriginRepository extends JpaRepository<ShippingOrigin, Long> {

    @Query("select s from ShippingOrigin as s join fetch s.merchantStore sm where sm.id = ?1")
    ShippingOrigin findByStore(Integer storeId);
}
