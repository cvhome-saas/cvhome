package com.asrevo.cvhome.inventory.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Bulk read behind the storefront, console and checkout. Ordered by id so a sku with several legacy rows always
     * resolves to the same one.
     */
    @Query("""
            select distinct i from Inventory i
            left join fetch i.prices
            where i.storeMerchantId = ?1 and i.sku in ?2
            order by i.id""")
    List<Inventory> findBySkus(StoreMerchantId store, Collection<String> skus);

    @Query("""
            select i from Inventory i
            left join fetch i.prices
            where i.storeMerchantId = ?1 and i.sku = ?2
            order by i.id limit 1""")
    Optional<Inventory> findBySku(StoreMerchantId store, String sku);

    /**
     * The reservation path's read: locked, so two orders cannot both take the last unit.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select i from Inventory i
            where i.storeMerchantId = ?1 and i.sku = ?2
            order by i.id limit 1""")
    Optional<Inventory> lockBySku(StoreMerchantId store, String sku);

    List<Inventory> findByStoreMerchantIdAndProductId(StoreMerchantId store, Long productId);
}
