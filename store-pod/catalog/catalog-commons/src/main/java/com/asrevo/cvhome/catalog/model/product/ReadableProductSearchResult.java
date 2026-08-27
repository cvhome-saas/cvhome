package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;

import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import lombok.Getter;
import lombok.Setter;

/**
 * A page of search results, plus what the theme needs to draw the rest of the page around them.
 */
@Getter
@Setter
public class ReadableProductSearchResult extends ReadableEntityList<ReadableProduct> {

    @Serial
    private static final long serialVersionUID = 1L;

    private ReadableSearchFacets facets;

    /**
     * A near-miss product name, set only when the query itself matched nothing and a trigram lookup found
     * something close. This is what a theme renders as "did you mean" — and the results alongside it are that
     * suggestion's results, not the original query's, which had none.
     */
    private String didYouMean;

    /**
     * The language the results actually came from. Usually the one asked for; the store's default when the
     * requested language had nothing indexed, so a theme can say so rather than showing an unexplained mix.
     */
    private String language;
}
