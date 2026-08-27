import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {ProductSearchPage, ProductSearchQuery, ProductSuggestion} from "@store-front/types/search";
import {apiFetch, get, orUndefined} from "./http-utils";
import {InventoryService} from "./inventory-service";
import {SORT_MAP} from "./product-category";

const SUGGEST_LIMIT = 8;

export function searchQueryToParams(query: ProductSearchQuery, facets: boolean): string {
    const p = new URLSearchParams();
    if (query.q.trim()) p.set('q', query.q.trim());
    if (query.categoryIds.length) p.set('categoryIds', query.categoryIds.join(','));
    if (query.manufacturerIds.length) p.set('manufacturerIds', query.manufacturerIds.join(','));
    if (query.productTypeIds.length) p.set('productTypeIds', query.productTypeIds.join(','));
    p.set('page', String(Math.max(0, query.page)));
    p.set('count', String(query.count));
    p.set('facets', String(facets));
    // The backend takes a named sort rather than a raw Pageable here, because relevance is a ranking function
    // and not a column. `SORT_MAP` still describes the listing endpoint; the names happen to line up.
    p.set('sort', query.sort.toUpperCase());
    return p.toString();
}

export class ProductSearchService {

    /**
     * Must fail: the results are the point of the search page. Callers surface the error as a state rather
     * than as an empty grid, which would read as "no such product".
     */
    public static search = async (storeContext: StoreContext, query: ProductSearchQuery,
                                  facets = true): Promise<ProductSearchPage> => {
        const page = await apiFetch<ProductSearchPage>(
            `${storeBaseServiceUrl('catalog', storeContext)}/api/v2/products/search?store=${storeContext.store}&lang=${storeContext.locale}&${searchQueryToParams(query, facets)}`,
            get());
        // Price and stock live in the inventory service, keyed by sku. The merge degrades — results without
        // prices still list — the results themselves must not.
        await InventoryService.enrichProducts(storeContext, page.content);
        return page;
    }

    /**
     * Degrades: the dropdown under a search box. It is answered on every keystroke and a shopper who gets no
     * suggestions can still press Enter, so a failure here costs a convenience, not the feature.
     */
    public static suggest = async (storeContext: StoreContext, term: string, signal?: AbortSignal,
                                   limit = SUGGEST_LIMIT): Promise<ProductSuggestion[]> => {
        if (!term.trim()) return [];
        const suggestions = await orUndefined(apiFetch<ProductSuggestion[]>(
            `${storeBaseServiceUrl('catalog', storeContext)}/api/v2/products/suggest?store=${storeContext.store}&lang=${storeContext.locale}&q=${encodeURIComponent(term.trim())}&limit=${limit}`,
            {...get(), signal}));
        return suggestions ?? [];
    }
}

export {SORT_MAP};
