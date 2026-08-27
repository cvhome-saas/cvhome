'use client'
import {useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {
    EMPTY_FACETS,
    type ListingSort,
    type ProductSearchPage,
    type FacetDimension,
    type ProductSearchQuery,
    type SearchFacets,
    type StoreContext,
    searchQueryToSearchString,
} from '@store-front/types';
import {ProductSearchService} from '@store-front/services/product-search-service';

export type SearchStatus = 'ready' | 'loading' | 'empty' | 'error';

export interface UseProductSearchOptions {
    initial: ProductSearchPage;
    initialQuery: ProductSearchQuery;
    facets: SearchFacets;
    /** Mirror the query into the address bar, so a filtered result set is a shareable link. */
    syncUrl?: boolean;
}

/**
 * The behaviour behind a results page: paging, sorting, facet selection, and keeping the URL in step.
 *
 * A sibling of `useProductListing` rather than a widening of it — that hook is bound to a category and to the
 * listing endpoint, which has neither a search term nor multi-select facets. They share their shape, and a
 * theme uses whichever page it is rendering.
 */
export function useProductSearch(storeContext: StoreContext, options: UseProductSearchOptions) {
    const {initial, initialQuery, facets: initialFacets, syncUrl = true} = options;

    const [query, setQuery] = useState<ProductSearchQuery>(initialQuery);
    const [page, setPage] = useState<ProductSearchPage>(initial);
    const [facets, setFacets] = useState<SearchFacets>(initialFacets);
    const [status, setStatus] = useState<SearchStatus>(
        initial.totalElements > 0 ? 'ready' : 'empty');
    const abort = useRef<AbortController | null>(null);
    // The server already fetched exactly this query; refetching it on mount would be a wasted round trip and
    // a visible flicker.
    const serverRendered = useRef(true);

    const run = useCallback(async (next: ProductSearchQuery) => {
        abort.current?.abort();
        const controller = new AbortController();
        abort.current = controller;
        setStatus('loading');
        try {
            const result = await ProductSearchService.search(storeContext, next);
            if (controller.signal.aborted) return;
            setPage(result);
            if (result.facets) setFacets(result.facets);
            setStatus(result.totalElements > 0 ? 'ready' : 'empty');
        } catch {
            if (!controller.signal.aborted) setStatus('error');
        }
    }, [storeContext]);

    useEffect(() => {
        if (serverRendered.current) {
            serverRendered.current = false;
            return;
        }
        void run(query);
        if (syncUrl && typeof window !== 'undefined') {
            // replaceState, not the router: the results are fetched here, and a router navigation would send
            // the whole page back to the server to render what is already on screen.
            window.history.replaceState(null, '', `${window.location.pathname}${searchQueryToSearchString(query)}`);
        }
    }, [query, run, syncUrl]);

    useEffect(() => () => abort.current?.abort(), []);

    /** Any change other than paging puts the shopper back on the first page — page 4 of a new filter is nowhere. */
    const amend = useCallback((patch: Partial<ProductSearchQuery>) => {
        setQuery(current => ({...current, ...patch, page: patch.page ?? 0}));
    }, []);

    const toggle = useCallback((dimension: FacetDimension, id: number) => {
        setQuery(current => {
            const selected = current[dimension];
            return {
                ...current,
                [dimension]: selected.includes(id) ? selected.filter(x => x !== id) : [...selected, id],
                page: 0,
            };
        });
    }, []);

    const clearFilters = useCallback(() => {
        amend({categoryIds: [], manufacturerIds: [], productTypeIds: []});
    }, [amend]);

    const hasFilters = query.categoryIds.length > 0 || query.manufacturerIds.length > 0
        || query.productTypeIds.length > 0;

    const products = useMemo(() => page.content ?? [], [page]);

    return {
        query,
        products,
        page,
        facets,
        status,
        hasFilters,
        didYouMean: page.didYouMean,
        setTerm: (q: string) => amend({q}),
        setSort: (sort: ListingSort) => amend({sort}),
        goToPage: (n: number) => setQuery(current => ({...current, page: Math.max(0, n)})),
        toggle,
        clearFilters,
        retry: () => void run(query),
    };
}
