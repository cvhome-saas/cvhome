package com.asrevo.cvhome.catalog.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.catalog.entity.ProductType;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {

    @Query("select t from ProductType t left join fetch t.descriptions where t.storeMerchantId = ?1 and t.id = ?2")
    Optional<ProductType> findByStoreAndId(StoreMerchantId store, Long id);

    @Query("select t from ProductType t left join fetch t.descriptions where t.storeMerchantId = ?1 and t.code = ?2")
    Optional<ProductType> findByStoreAndCode(StoreMerchantId store, String code);

    boolean existsByStoreMerchantIdAndCode(StoreMerchantId store, String code);

    Page<ProductType> findByStoreMerchantId(StoreMerchantId store, Pageable pageable);
}
