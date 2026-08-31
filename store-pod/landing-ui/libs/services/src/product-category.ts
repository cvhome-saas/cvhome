import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {Manufacturer} from "@store-front/types/product-groups";
import {ListingFacets, ListingQuery, ListingSort, OptionFacet, ProductListingPage} from "@store-front/types/listing";
import {apiFetch, get, orUndefined, publicGet} from "./http-utils";
import {InventoryService} from "./inventory-service";

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

    /**
     * The filter rail's facets: manufacturers from the category's own endpoint, and the counted
     * option-value groups from the search endpoint's facet block — the one place the catalog counts
     * them. The search runs with the category filter and no query (`count=1`, results discarded);
     * value ids are store-wide, so a toggled value round-trips as `ListingQuery.optionValueIds`.
     * Both degrade: a rail without a group beats a listing page that fails on its filters.
     */
    public static getFacets = async (storeContext: StoreContext, categoryId: number): Promise<ListingFacets> => {
        const [manufacturers, options] = await Promise.all([
            ProductCategory.getManufacturers(storeContext, categoryId),
            ProductCategory.getOptionFacets(storeContext, categoryId),
        ]);
        return {manufacturers: manufacturers ?? [], options: options ?? []};
    }

    /** Degrades: the option groups of the filter rail. */
    private static getOptionFacets = async (storeContext: StoreContext, categoryId: number): Promise<OptionFacet[] | undefined> => {
        interface FacetsPayload {
            facets?: {
                options?: {
                    optionId: number; code?: string; name?: string;
                    values?: {id: number; name?: string; count: number; selected?: boolean}[];
                }[];
            };
        }
        const result = await orUndefined(apiFetch<FacetsPayload>(
            `${storeBaseServiceUrl('catalog', storeContext)}/api/v2/products/search?store=${storeContext.store}&lang=${storeContext.locale}&categoryIds=${categoryId}&page=0&count=1&facets=true`,
            publicGet()));
        return result?.facets?.options?.map(option => ({
            id: option.optionId,
            code: option.code,
            name: option.name ?? option.code ?? '',
            values: (option.values ?? []).map(value => ({
                id: value.id,
                name: value.name ?? '',
                count: value.count,
                selected: value.selected,
            })),
        }));
    }

    /**
     * Must fail: the listing is the point of the category page. Callers (the server loader and
     * `useProductListing`) surface the error as a state instead of an empty grid.
     */
    public static getProducts = async (storeContext: StoreContext, query: ListingQuery, categoryId?: number): Promise<ProductListingPage> => {
        const page = await apiFetch<ProductListingPage>(
            `${storeBaseServiceUrl('catalog', storeContext)}/api/v2/products?store=${storeContext.store}&lang=${storeContext.locale}&${listingQueryToParams(query, categoryId)}`,
            get());
        // Stock and price live in the inventory service since the split. The merge degrades — a
        // listing without prices still lists — the page itself must not.
        await InventoryService.enrichProducts(storeContext, page.content);
        return page;
    }
}
