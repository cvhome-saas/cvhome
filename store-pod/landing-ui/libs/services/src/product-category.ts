import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {Manufacturer, ProductGroupPage} from "@store-front/types/product-groups";
import {apiFetch, get, orUndefined} from "./http-utils";

export class ProductCategory {

    /** Degrades: a filter facet on the category page. */
    public static getManufacturers = async (storeContext: StoreContext, categoryId: number): Promise<Manufacturer[] | undefined> => {
        return orUndefined(apiFetch<Manufacturer[]>(
            `${storeBaseServiceUrl('catalog', storeContext)}/api/v1/category/${categoryId}/manufacturer?store=${storeContext.store}&lang=${storeContext.locale}`,
            get()));
    }

    /**
     * Degrades for now. Both callers are `useProductCategoryFilter`, a client hook with no error state,
     * so throwing here would only produce an unhandled rejection and an empty grid. Once that hook
     * surfaces an error this should become a must-fail call — the listing is the point of the page.
     */
    public static getProducts = async (storeContext: StoreContext, categoryId?: number, manufacturerId?: number): Promise<ProductGroupPage | undefined> => {
        const categoryParam = categoryId ? `&categoryIds=${categoryId}` : '';
        const manufacturerParam = manufacturerId ? `&manufacturerId=${manufacturerId}` : '';
        return orUndefined(apiFetch<ProductGroupPage>(
            `${storeBaseServiceUrl('catalog', storeContext)}/api/v2/products?store=${storeContext.store}${categoryParam}${manufacturerParam}&page=0&count=15&lang=${storeContext.locale}`,
            get()));
    }

}
