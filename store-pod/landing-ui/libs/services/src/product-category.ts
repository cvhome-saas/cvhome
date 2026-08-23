import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {Manufacturer, ProductVariant} from "@store-front/types/product-groups";
import {ListingFacets, ListingQuery, ListingSort, ProductListingPage} from "@store-front/types/listing";
import {apiFetch, get, orUndefined} from "./http-utils";

/**
 * `sort=` is forwarded verbatim to Spring's Pageable on the Product entity. Only direct columns are
 * safe (`dateAvailable`, `id`, `sortOrder`); anything in a joined table (price, description.name) 500s.
 * `relevance` = the backend's default ordering.
 */
export const SORT_MAP: Readonly<Record<ListingSort, string | undefined>> = {
    relevance: undefined,
    newest: 'dateAvailable,desc',
    oldest: 'dateAvailable,asc',
};

export function listingQueryToParams(query: ListingQuery, categoryId?: number): string {
    const p = new URLSearchParams();
    if (categoryId) p.set('categoryIds', String(categoryId));
    if (query.manufacturerId) p.set('manufacturerId', String(query.manufacturerId));
    if (query.optionValueIds?.length) p.set('optionValueIds', query.optionValueIds.join(','));
    p.set('page', String(Math.max(0, query.page)));
    p.set('count', String(query.count));
    const sort = SORT_MAP[query.sort];
    if (sort) p.set('sort', sort);
    return p.toString();
}

export class ProductCategory {

    /** Degrades: a filter facet on the category page. */
    public static getManufacturers = async (storeContext: StoreContext, categoryId: number): Promise<Manufacturer[] | undefined> => {
        return orUndefined(apiFetch<Manufacturer[]>(
            `${storeBaseServiceUrl('catalog', storeContext)}/api/v1/category/${categoryId}/manufacturer?store=${storeContext.store}&lang=${storeContext.locale}`,
            get()));
    }

    /** Degrades: option/variant facets across a category. */
    public static getCategoryVariants = async (storeContext: StoreContext, categoryId: number): Promise<ProductVariant[] | undefined> => {
        return orUndefined(apiFetch<ProductVariant[]>(
            `${storeBaseServiceUrl('catalog', storeContext)}/api/v2/category/${categoryId}/variations?store=${storeContext.store}&lang=${storeContext.locale}`,
            get()));
    }

    /** Both facets at once; each degrades independently. */
    public static getFacets = async (storeContext: StoreContext, categoryId: number): Promise<ListingFacets> => {
        const [manufacturers, variants] = await Promise.all([
            ProductCategory.getManufacturers(storeContext, categoryId),
            ProductCategory.getCategoryVariants(storeContext, categoryId),
        ]);
        return {manufacturers: manufacturers ?? [], variants: variants ?? []};
    }

    /**
     * Must fail: the listing is the point of the category page. Callers (the server loader and
     * `useProductListing`) surface the error as a state instead of an empty grid.
     */
    public static getProducts = async (storeContext: StoreContext, query: ListingQuery, categoryId?: number): Promise<ProductListingPage> => {
        return apiFetch<ProductListingPage>(
            `${storeBaseServiceUrl('catalog', storeContext)}/api/v2/products?store=${storeContext.store}&lang=${storeContext.locale}&${listingQueryToParams(query, categoryId)}`,
            get());
    }
}
