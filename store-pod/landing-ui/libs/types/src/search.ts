/**
 * Search is an abstraction because the catalog has NO text-search endpoint today (`productName` /
 * `search` criteria are accepted and ignored by `ProductRepository.findAll`). Themes render search UI
 * only according to `SearchCapabilities`, so nothing promises a feature the platform cannot deliver.
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
