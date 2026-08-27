package com.asrevo.cvhome.catalog.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.catalog.entity.ProductSearchIndex;
import com.asrevo.cvhome.catalog.entity.ProductSearchIndexId;

/**
 * The search index's write side. Every method here is a call into SQL that lives in {@code schema.sql}: the shape
 * of the searchable document — which fields, what weight, which stemmer, what normalisation — is defined once,
 * in the database, because the query side has to normalise text exactly the same way. A second implementation in
 * Java would not fail loudly when it drifted; it would just quietly stop matching.
 */
public interface ProductSearchIndexRepository extends JpaRepository<ProductSearchIndex, ProductSearchIndexId> {

    /**
     * Each of these calls a function that writes, but none is annotated {@code @Modifying}: they are invoked as
     * {@code select f(...)}, which returns a row, and a modifying query would refuse it. Each returns how many
     * index rows it touched.
     */
    @Query(value = "select catalog.refresh_product_search_index(:productId)", nativeQuery = true)
    int refresh(@Param("productId") Long productId);

    @Query(value = "select catalog.purge_product_search_index(:productId)", nativeQuery = true)
    int purge(@Param("productId") Long productId);

    @Query(value = "select catalog.rebuild_product_search_index(:store)", nativeQuery = true)
    int rebuildStore(@Param("store") String store);

    /**
     * The products carrying one brand, so a rename can walk them in batches instead of loading a whole
     * catalogue's worth of aggregates.
     */
    @Query(value = """
            select product_id
            from catalog.product
            where manufacturer_id = :manufacturerId and store_merchant_id = :store
            order by product_id
            """, nativeQuery = true)
    List<Long> productIdsForBrand(@Param("manufacturerId") Long manufacturerId, @Param("store") String store);

    /**
     * The autocomplete lookup, ranked, capped, and answered from the index alone.
     *
     * <p>
     * Two ways to match, because a shopper mid-word needs both. The trigram {@code ilike} catches a partial word
     * anywhere in the name — a prefix {@code tsquery} cannot, because the document holds stemmed lexemes and
     * "runn" is not a prefix of "run". The prefix tsquery earns its place anyway: it is what lets a half-typed
     * brand or sku surface, which the name match alone would miss.
     * </p>
     *
     * <p>
     * Native rather than Criteria because it is the hottest path in the feature and this is one index-backed
     * statement; there is no filter composition here to justify building it dynamically.
     * </p>
     */
    @Query(value = """
            select i.product_id
            from catalog.product_search_index i
                     join catalog.product p on p.product_id = i.product_id
            where i.store_merchant_id = :store
              and i.language_code = :language
              and p.available = true
              and (i.name_normalized ilike '%' || catalog.search_normalize(:q) || '%'
                or i.search_document @@ catalog.search_prefix_tsquery(:q, :language))
            order by public.word_similarity(catalog.search_normalize(:q), i.name_normalized) desc, i.product_id
            limit :limit
            """, nativeQuery = true)
    List<Long> suggestProductIds(@Param("store") String store, @Param("language") String language,
                                 @Param("q") String query, @Param("limit") int limit);

    /**
     * The closest product name to something that matched nothing — the "did you mean". Returns the name as the
     * merchant wrote it, not the normalised form the comparison ran on.
     *
     * <p>
     * {@code word_similarity}, not {@code similarity}: the latter compares the query against the whole name, so
     * two mistyped words against a five-word product name score near zero however good the match is. This
     * compares them against the best-matching run of words inside the name, which is the question actually
     * being asked.
     * </p>
     */
    @Query(value = """
            select pd.name
            from catalog.product_search_index i
                     join catalog.product p on p.product_id = i.product_id
                     join catalog.product_description pd
                          on pd.product_id = i.product_id and pd.language_code = i.language_code
            where i.store_merchant_id = :store
              and i.language_code = :language
              and p.available = true
              and public.word_similarity(catalog.search_normalize(:q), i.name_normalized) > :floor
            order by public.word_similarity(catalog.search_normalize(:q), i.name_normalized) desc
            limit 1
            """, nativeQuery = true)
    Optional<String> bestNearMiss(@Param("store") String store, @Param("language") String language,
                                  @Param("q") String query, @Param("floor") float floor);

    /**
     * The language this store has the most of, ignoring one.
     *
     * <p>
     * A shopper browsing in a language the merchant never wrote copy in would otherwise get an empty result for
     * every query, which reads as a broken shop rather than an untranslated one. Answered from the index instead
     * of from the merchant service on purpose: it is one indexed count, on a path that has already failed once,
     * and it needs no cross-service call.
     * </p>
     */
    @Query(value = """
            select i.language_code
            from catalog.product_search_index i
            where i.store_merchant_id = :store and i.language_code <> :exclude
            group by i.language_code
            order by count(*) desc, i.language_code
            limit 1
            """, nativeQuery = true)
    Optional<String> richestLanguageOtherThan(@Param("store") String store, @Param("exclude") String exclude);

    /**
     * Products the index has never seen.
     *
     * <p>
     * Non-empty in exactly two situations: a catalogue that predates this feature, and a database seeded by
     * {@code spring.sql.init}, whose data is inserted after {@code schema.sql} has run and so is not reached by
     * anything the schema itself could do. Both are fixed at startup rather than left for a shopper to discover.
     * </p>
     */
    @Query(value = """
            select p.product_id
            from catalog.product p
            where not exists (select 1 from catalog.product_search_index i where i.product_id = p.product_id)
            """, nativeQuery = true)
    List<Long> productIdsMissingFromIndex();
}
