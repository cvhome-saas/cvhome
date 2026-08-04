import {Product} from "@store-front/types/product-groups";
import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {apiFetch, get, orUndefined} from "./http-utils";
import {ProductGroup} from "@store-front/types";

export class ProductService {

    public static getHomePageProductGroup = async (storeContext: StoreContext): Promise<ProductGroup | undefined> => {
        return this.getProductByGroup(storeContext, 'HOME_PAGE');
    }

    public static getRecommendedProductGroup = async (storeContext: StoreContext): Promise<ProductGroup | undefined> => {
        return this.getProductByGroup(storeContext, 'RECOMMENDED');
    }

    public static getNewlyAddedProductGroup = async (storeContext: StoreContext): Promise<ProductGroup | undefined> => {
        return this.getProductByGroup(storeContext, 'NEWLY_ADDED');
    }

    public static getFeaturedItemsProductGroup = async (storeContext: StoreContext): Promise<ProductGroup | undefined> => {
        return this.getProductByGroup(storeContext, 'FEATURED_ITEMS');
    }

    /** Degrades: a strip below the product. Its absence is not worth losing the product page over. */
    public static getRelatedProductGroup = async (storeContext: StoreContext, product: number): Promise<ProductGroup | undefined> => {
        return orUndefined(apiFetch<ProductGroup>(
            `${storeBaseServiceUrl('catalog', storeContext)}/api/v1/products/${product}/relationship?store=${storeContext.store}&lang=${storeContext.locale}`,
            get()));
    }

    /**
     * Degrades: the home page renders four of these strips side by side. One catalog hiccup should cost
     * that strip, not the landing page every shopper arrives on.
     */
    public static getProductByGroup = async (storeContext: StoreContext, group: string): Promise<ProductGroup | undefined> => {
        return orUndefined(apiFetch<ProductGroup>(
            `${storeBaseServiceUrl('catalog', storeContext)}/api/v1/products/groups/${group}?store=${storeContext.store}&lang=${storeContext.locale}`,
            get()));
    }

    /** Must fail: this is the product page's subject. A 404 here is a real 404. */
    public static getProductByUrl = async (url: string, storeContext: StoreContext): Promise<Product> => {
        return apiFetch<Product>(
            `${storeBaseServiceUrl('catalog', storeContext)}/api/v2/product/name/${url}?store=${storeContext.store}&lang=${storeContext.locale}`,
            get());
    }
}
