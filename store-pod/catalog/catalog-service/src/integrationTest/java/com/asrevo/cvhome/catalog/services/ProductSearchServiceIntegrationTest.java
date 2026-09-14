package com.asrevo.cvhome.catalog.services;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import com.asrevo.cvhome.catalog.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.catalog.model.product.ProductSearchCriteria;
import com.asrevo.cvhome.catalog.model.product.ReadableProductSearchResult;
import com.asrevo.cvhome.catalog.model.product.SearchFacetGroup;
import com.asrevo.cvhome.catalog.services.product.ProductSearchService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.Tokens;
import com.asrevo.cvhome.testsupport.sql.SqlStatements;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The storefront's filter rail asks for its option facets alone ({@code rows=false&facetGroups=OPTIONS}), and gets
 * them without the page of products, its count, its hydration or the three facet blocks it never draws.
 *
 * <p>
 * The category page used to ask for one row and every facet block and throw most of it away: 17 statements on every
 * render. Same context as {@code ProductSearchApiIntegrationTest}.
 * </p>
 */
@StorageIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class ProductSearchServiceIntegrationTest {

    private static final StoreMerchantId STORE = new StoreMerchantId(Tokens.STORE_1);

    private static final LanguageCode EN = new LanguageCode("en");

    @Autowired
    private ProductSearchService search;

    @Test
    void theRailAloneReadsNoPageNoCountNoProductsAndOnlyItsOwnFacets() throws Exception {
        ProductSearchCriteria everything = new ProductSearchCriteria();
        SqlStatements.Recorded<ReadableProductSearchResult> asBefore = SqlStatements.during(
                () -> search.search(STORE, everything, EN, PageRequest.of(0, 1)));

        ProductSearchCriteria rail = new ProductSearchCriteria();
        rail.setRows(false);
        rail.setFacetGroups(Set.of(SearchFacetGroup.OPTIONS));
        SqlStatements.Recorded<ReadableProductSearchResult> railOnly = SqlStatements.during(
                () -> search.search(STORE, rail, EN, PageRequest.of(0, 1)));

        assertThat(railOnly.result().getContent()).isEmpty();
        assertThat(railOnly.result().getFacets().getOptions()).usingRecursiveComparison()
                .isEqualTo(asBefore.result().getFacets().getOptions());
        assertThat(railOnly.result().getFacets().getBrands()).isEmpty();
        assertThat(railOnly.count()).as(railOnly.toString()).isLessThanOrEqualTo(4).isLessThan(asBefore.count());
    }
}
