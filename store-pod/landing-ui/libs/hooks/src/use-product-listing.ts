'use client'
import {useCallback, useEffect, useMemo, useRef, useState} from "react";
import {usePathname, useSearchParams} from "next/navigation";
import {
    ApiError, Category, defaultListingQuery, isApiError, LISTING_SORTS, ListingFacets, ListingQuery, ListingSort, listingQueryToSearchString,
    Product, ProductListingPage, StoreContext,
} from "@store-front/types";
import {ProductCategory} from "@store-front/services/product-category";

export type ListingStatus = 'ready' | 'loading' | 'empty' | 'error';

export interface UseProductListingOptions {
    /** SSR-loaded first page so the listing never first-paints empty. */
    initial?: ProductListingPage;
    initialQuery?: ListingQuery;
    facets?: ListingFacets;
    /** Mirror `page`/`sort`/`manufacturer` into the URL (shareable, back-button safe). Default true. */
    syncUrl?: boolean;
}

/**
 * Category listing with sort, pagination and facets. Replaces `useProductCategoryFilter`.
 * Exposes a `status` (ready/loading/empty/error) so themes render real states, never a silent empty grid.
 */
export const useProductListing = (storeContext: StoreContext, category: Category, options: UseProductListingOptions = {}) => {
    const {initial, initialQuery, syncUrl = true} = options;
    const pathname = usePathname();
    const searchParams = useSearchParams();

    const [query, setQueryState] = useState<ListingQuery>(initialQuery ?? defaultListingQuery());
    const [page, setPage] = useState<ProductListingPage | undefined>(initial);
    const [loading, setLoading] = useState<boolean>(!initial);
    const [error, setError] = useState<ApiError | null>(null);
    const [facets, setFacets] = useState<ListingFacets>(options.facets ?? {manufacturers: [], options: []});
    const requestId = useRef(0);
    const skipFirstFetch = useRef(!!initial);

    const load = useCallback(async (q: ListingQuery) => {
        const id = ++requestId.current;
        setLoading(true);
        setError(null);
        try {
            const result = await ProductCategory.getProducts(storeContext, q, category.id);
            if (id === requestId.current) setPage(result);
        } catch (e) {
            if (id === requestId.current) {
                setError(isApiError(e) ? e : new ApiError({code: 'CLIENT.UNKNOWN', category: 'UNKNOWN', status: 0, cause: e}));
            }
        } finally {
            if (id === requestId.current) setLoading(false);
        }
    }, [storeContext.store, storeContext.locale, category.id]);

    useEffect(() => {
        if (skipFirstFetch.current) {
            skipFirstFetch.current = false;
            return;
        }
        load(query).then();
    }, [query, load]);

    useEffect(() => {
        if (options.facets) return;
        ProductCategory.getFacets(storeContext, category.id).then(setFacets);
    }, [storeContext.store, storeContext.locale, category.id]);

    const queryRef = useRef(query);
    queryRef.current = query;

    const setQuery = useCallback((patch: Partial<ListingQuery>) => {
        const next: ListingQuery = {...queryRef.current, ...patch};
        if (!('page' in patch)) next.page = 0;  // any filter/sort change restarts paging
        queryRef.current = next;
        setQueryState(next);
        if (syncUrl && typeof window !== 'undefined') {
            // Native history update: Next syncs useSearchParams() with it and — unlike router.replace — does not
            // re-render the server page, so the listing is fetched once (here) and the URL stays shareable.
            window.history.replaceState(window.history.state, '', `${pathname}${listingQueryToSearchString(next)}`);
            if ('page' in patch) window.scrollTo({top: 0, behavior: 'smooth'});
        }
    }, [pathname, syncUrl]);

    const items: Product[] = page?.content ?? [];
    const status: ListingStatus = error ? 'error' : loading && !page ? 'loading' : items.length === 0 && !loading ? 'empty' : 'ready';

    return {
        items,
        page: query.page,
        totalPages: page?.totalPages ?? 0,
        total: page?.totalElements || page?.recordsTotal || page?.recordsFiltered || items.length,
        query,
        facets,
        status,
        loading,
        error,
        sorts: LISTING_SORTS,
        setSort: (sort: ListingSort) => setQuery({sort}),
        setPage: (p: number) => setQuery({page: Math.max(0, p)}),
        setManufacturer: (manufacturerId: number | undefined) => setQuery({manufacturerId}),
        toggleOptionValue: (id: number) => setQuery({
            optionValueIds: query.optionValueIds?.includes(id)
                ? query.optionValueIds.filter(x => x !== id)
                : [...(query.optionValueIds ?? []), id],
        }),
        clearFilters: () => setQuery({manufacturerId: undefined, optionValueIds: undefined, sort: 'relevance'}),
        hasActiveFilters: !!query.manufacturerId || !!query.optionValueIds?.length,
        retry: () => load(query),
        /** The URL-synced query as it stands — for the rare theme that needs it (share links). */
        searchString: useMemo(() => listingQueryToSearchString(query), [query]),
        _searchParams: searchParams,
    };
};
