package com.asrevo.cvhome.catalog.services.product;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.model.product.ProductSearchCriteria;
import com.asrevo.cvhome.catalog.model.product.ReadableProductSearchResult;
import com.asrevo.cvhome.catalog.model.product.ReadableProductSuggestion;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Product search.
 *
 * <p>
 * An interface with one Postgres implementation, and the seam a different engine would enter through. Postgres
 * full-text is the right answer while a pod holds a few million product-language rows and relevance is static;
 * past that, or the day search needs synonyms, learned ranking or semantic matching, this is the boundary that
 * moves rather than every caller.
 * </p>
 */
public interface ProductSearchService {

    ReadableProductSearchResult search(StoreMerchantId store, ProductSearchCriteria criteria, LanguageCode language,
                                       Pageable pageable);

    /**
     * The autocomplete path: no facets, no total count, capped hard.
     */
    List<ReadableProductSuggestion> suggest(StoreMerchantId store, String query, LanguageCode language, int limit);

    /**
     * Rebuild a store's index from the catalogue as it stands. For after the document's shape changes, or when
     * something has clearly drifted.
     */
    void rebuildIndex(StoreMerchantId store);
}
