package com.asrevo.cvhome.catalog.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.catalog.entity.Category;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("select c from Category c left join fetch c.descriptions where c.storeMerchantId = ?1 and c.id = ?2")
    Optional<Category> findByStoreAndId(StoreMerchantId store, Long id);

    @Query("select c from Category c left join fetch c.descriptions where c.storeMerchantId = ?1 and c.code = ?2")
    Optional<Category> findByStoreAndCode(StoreMerchantId store, String code);

    boolean existsByStoreMerchantIdAndCode(StoreMerchantId store, String code);

    @Query("""
            select c from Category c join fetch c.descriptions d
            where c.storeMerchantId = ?1 and d.seUrl = ?2 and d.languageCode = ?3""")
    Optional<Category> findByStoreAndFriendlyUrl(StoreMerchantId store, String friendlyUrl, LanguageCode language);

    /**
     * Every node whose lineage starts with {@code prefix} — a node's own lineage yields itself and its subtree.
     */
    @Query("""
            select distinct c from Category c left join fetch c.descriptions
            where c.storeMerchantId = ?1 and c.lineage like concat(?2, '%')
            order by c.lineage, c.sortOrder""")
    List<Category> findSubtree(StoreMerchantId store, String prefix);

    /**
     * All of a store's categories, optionally narrowed by a name fragment in any language. Paged, because the
     * console's hierarchy read is a page.
     */
    @Query(value = "select distinct c from Category c left join fetch c.descriptions where c.storeMerchantId = ?1",
            countQuery = "select count(c) from Category c where c.storeMerchantId = ?1")
    Page<Category> findByStore(StoreMerchantId store, Pageable pageable);

    @Query(value = """
            select distinct c from Category c left join fetch c.descriptions d
            where c.storeMerchantId = ?1 and lower(d.name) like lower(concat('%', ?2, '%'))""",
            countQuery = """
                    select count(distinct c) from Category c left join c.descriptions d
                    where c.storeMerchantId = ?1 and lower(d.name) like lower(concat('%', ?2, '%'))""")
    Page<Category> findByStoreAndName(StoreMerchantId store, String name, Pageable pageable);

    @Query("""
            select distinct c from Product p join p.categories c left join fetch c.descriptions
            where p.store = ?1 and p.id = ?2""")
    List<Category> findByProduct(StoreMerchantId store, Long productId);
}
