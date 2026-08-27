import {LISTING_DEFAULT_COUNT, LISTING_SORTS, ListingSort} from "./listing";
import {Product} from "./product-groups";

/**
 * Search is an abstraction because not every deployment can answer every kind of query. Themes render search UI
 * according to `SearchCapabilities`, so nothing promises a feature the platform cannot deliver.
 *
 * The catalog now has a real full-text endpoint (`/api/v2/products/search` and `/api/v2/products/suggest`), so
 * `text` is true wherever that provider is wired. It stays a capability rather than an assumption: a store whose
 * catalogue has no copy in the shopper's language, or a deployment pointed at the suggestions-only provider,
 * still has to render honestly.
 */
export interface SearchCapabilities {
    /** Free-text product search over the catalog. */
    text: boolean;
    /** Instant suggestions (categories, pages) from already-loaded navigation data. */
    suggestions: boolean;
}

export const NO_SEARCH: SearchCapabilities = {text: false, suggestions: false};

export type SearchHitKind = 'product' | 'category' | 'page';

export interface SearchHit {
    kind: SearchHitKind;
    id: string;
    title: string;
    href: string;
    imageUrl?: string;
    subtitle?: string;
}

export interface SearchResult {
    query: string;
    hits: SearchHit[];
    /** True when the provider cannot answer this query kind (e.g. text search unavailable). */
    unavailable?: boolean;
}

/**
 * What a shopper asked the catalogue for.
 *
 * Deliberately not `ListingQuery` with a `q` bolted on: the category listing hits an endpoint that has no notion
 * of a search term or of multi-select facets, and widening its type would let a caller send filters that are
 * silently ignored. The two share `page`, `count` and `sort` because those genuinely are the same thing.
 */
export interface ProductSearchQuery {
    q: string;
    page: number;          // 0-based
    count: number;
    sort: ListingSort;
    categoryIds: number[];
    manufacturerIds: number[];
    productTypeIds: number[];
}

export const defaultSearchQuery = (): ProductSearchQuery => ({
    q: '',
    page: 0,
    count: LISTING_DEFAULT_COUNT,
    sort: 'relevance',
    categoryIds: [],
    manufacturerIds: [],
    productTypeIds: [],
});

export interface FacetBucket {
    id: number;
    name: string;
    count: number;
    selected: boolean;
}

/** The dimensions a shopper can narrow by. Named so a rail can be built by iterating rather than repeating. */
export type FacetDimension = 'categoryIds' | 'manufacturerIds' | 'productTypeIds';

export interface SearchFacets {
    categories: FacetBucket[];
    brands: FacetBucket[];
    types: FacetBucket[];
}

export const EMPTY_FACETS: SearchFacets = {categories: [], brands: [], types: []};

export interface ProductSearchPage {
    content: Product[];
    totalElements: number;
    totalPages: number;
    pageNumber: number;
    size: number;
    facets?: SearchFacets;
    /**
     * A near-miss product name, present only when the query matched nothing and a close one was found. The
     * results alongside it belong to the suggestion, not to what was typed.
     */
    didYouMean?: string;
    /** The language the results came from — not always the one asked for. */
    language?: string;
}

export const emptySearchPage = (): ProductSearchPage => ({
    content: [], totalElements: 0, totalPages: 0, pageNumber: 0, size: 0, facets: EMPTY_FACETS,
});

export interface ProductSuggestion {
    id: number;
    name: string;
    friendlyUrl: string;
    sku: string;
    imageUrl?: string;
    brand?: string;
}

const numbers = (raw: string | null): number[] =>
    raw ? raw.split(',').map(Number).filter(n => Number.isFinite(n) && n > 0) : [];

/** Parse `?q&page&sort&categories&brands&types` into a query (pure; usable from server and client). */
export function parseSearchQuery(params: URLSearchParams | null | undefined,
                                 base: ProductSearchQuery = defaultSearchQuery()): ProductSearchQuery {
    if (!params) return base;
    const page = Number(params.get('page'));
    const sort = params.get('sort') as ListingSort | null;
    return {
        ...base,
        q: params.get('q') ?? base.q,
        page: Number.isFinite(page) && page > 0 ? page : base.page,
        sort: sort && LISTING_SORTS.includes(sort) ? sort : base.sort,
        categoryIds: numbers(params.get('categories')),
        manufacturerIds: numbers(params.get('brands')),
        productTypeIds: numbers(params.get('types')),
    };
}

export function searchQueryToSearchString(query: ProductSearchQuery): string {
    const p = new URLSearchParams();
    if (query.q) p.set('q', query.q);
    if (query.page > 0) p.set('page', String(query.page));
    if (query.sort !== 'relevance') p.set('sort', query.sort);
    if (query.categoryIds.length) p.set('categories', query.categoryIds.join(','));
    if (query.manufacturerIds.length) p.set('brands', query.manufacturerIds.join(','));
    if (query.productTypeIds.length) p.set('types', query.productTypeIds.join(','));
    const s = p.toString();
    return s ? `?${s}` : '';
}

/** The href of the results page for a term, used by every theme's search box on submit. */
export const searchHref = (term: string): string =>
    `/search${term.trim() ? `?q=${encodeURIComponent(term.trim())}` : ''}`;
