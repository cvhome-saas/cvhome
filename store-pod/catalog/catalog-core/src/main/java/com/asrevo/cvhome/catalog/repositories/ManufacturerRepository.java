package com.asrevo.cvhome.catalog.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.catalog.entity.Manufacturer;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {

    @Query("select m from Manufacturer m left join fetch m.descriptions where m.storeMerchantId = ?1 and m.id = ?2")
    Optional<Manufacturer> findByStoreAndId(StoreMerchantId store, Long id);

    @Query("select m from Manufacturer m left join fetch m.descriptions where m.storeMerchantId = ?1 and m.code = ?2")
    Optional<Manufacturer> findByStoreAndCode(StoreMerchantId store, String code);

    boolean existsByStoreMerchantIdAndCode(StoreMerchantId store, String code);

    @Query(value = "select distinct m from Manufacturer m left join fetch m.descriptions where m.storeMerchantId = ?1",
            countQuery = "select count(m) from Manufacturer m where m.storeMerchantId = ?1")
    Page<Manufacturer> findByStore(StoreMerchantId store, Pageable pageable);

    @Query(value = """
            select distinct m from Manufacturer m left join fetch m.descriptions d
            where m.storeMerchantId = ?1 and lower(d.name) like lower(concat('%', ?2, '%'))""",
            countQuery = """
                    select count(distinct m) from Manufacturer m left join m.descriptions d
                    where m.storeMerchantId = ?1 and lower(d.name) like lower(concat('%', ?2, '%'))""")
    Page<Manufacturer> findByStoreAndName(StoreMerchantId store, String name, Pageable pageable);

    /**
     * The brands of the products in a category subtree — the storefront's brand facet.
     */
    @Query("""
            select distinct m from Product p join p.manufacturer m left join fetch m.descriptions
            join p.categories c
            where p.store = ?1 and p.available = true and c.lineage like concat(?2, '%')
            order by m.code""")
    List<Manufacturer> findByCategorySubtree(StoreMerchantId store, String lineagePrefix);
}
