import {Manufacturer, ProductGroupPage} from "./product-groups";

/**
 * Sort keys the catalog can honour today. `sort=` is a raw Spring `Pageable` sort on the Product entity,
 * so only direct entity columns are safe; price and name live in joined tables and would 500.
 * Keep this list and `SORT_MAP` in `@store-front/services/product-category` in step.
 */
export type ListingSort = 'relevance' | 'newest' | 'oldest';

export const LISTING_SORTS: readonly ListingSort[] = ['relevance', 'newest', 'oldest'];
export const LISTING_DEFAULT_COUNT = 24;

export interface ListingQuery {
    page: number;          // 0-based
    count: number;
    sort: ListingSort;
    manufacturerId?: number;
    optionValueIds?: number[];
}

export const defaultListingQuery = (): ListingQuery => ({page: 0, count: LISTING_DEFAULT_COUNT, sort: 'relevance'});

export type ProductListingPage = ProductGroupPage;

export interface ListingFacets {
    manufacturers: Manufacturer[];
    /**
     * The option-value filter groups — Color with Red (12) / Blue (8), counted against the current
     * results. Value ids are store-wide, so a toggled value round-trips straight back as
     * `ListingQuery.optionValueIds`.
     */
    options: OptionFacet[];
}

/** One option's slice of the filter rail. */
export interface OptionFacet {
    id: number;
    code?: string;
    name: string;
    values: OptionFacetValue[];
}

export interface OptionFacetValue {
    id: number;
    name: string;
    count: number;
    selected?: boolean;
}

/** Parse `?page&sort&manufacturer&options` into a ListingQuery (pure; usable from server and client). */
export function parseListingQuery(params: URLSearchParams | null | undefined, base: ListingQuery = defaultListingQuery()): ListingQuery {
    if (!params) return base;
    const page = Number(params.get('page'));
    const sort = params.get('sort') as ListingSort | null;
    const manufacturer = Number(params.get('manufacturer'));
    const options = params.get('options');
    return {
        ...base,
        page: Number.isFinite(page) && page > 0 ? page : base.page,
        sort: sort && LISTING_SORTS.includes(sort) ? sort : base.sort,
        manufacturerId: Number.isFinite(manufacturer) && manufacturer > 0 ? manufacturer : base.manufacturerId,
        optionValueIds: options ? options.split(',').map(Number).filter(n => Number.isFinite(n) && n > 0) : base.optionValueIds,
    };
}

export function listingQueryToSearchString(query: ListingQuery): string {
    const p = new URLSearchParams();
    if (query.page > 0) p.set('page', String(query.page));
    if (query.sort !== 'relevance') p.set('sort', query.sort);
    if (query.manufacturerId) p.set('manufacturer', String(query.manufacturerId));
    if (query.optionValueIds?.length) p.set('options', query.optionValueIds.join(','));
    const s = p.toString();
    return s ? `?${s}` : '';
}
