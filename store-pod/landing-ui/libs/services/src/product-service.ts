import {Product, ProductGroupPage} from "@store-front/types/product-groups";
import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {get, handleResponse} from "./http-utils";
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

    public static getRelatedProductGroup = async (storeContext: StoreContext, product: number): Promise<ProductGroup | undefined> => {
        return fetch(`${storeBaseServiceUrl('catalog', storeContext)}/api/v1/products/${product}/relationship?store=${storeContext.store}&lang=${storeContext.locale}`, get())
            .then(it => handleResponse<ProductGroup>(it))
    }

    public static getProductByGroup = async (storeContext: StoreContext, group: string): Promise<ProductGroup | undefined> => {
        return fetch(`${storeBaseServiceUrl('catalog', storeContext)}/api/v1/products/groups/${group}?store=${storeContext.store}&lang=${storeContext.locale}`, get())
            .then(it => handleResponse<ProductGroup>(it))
    }

    public static getProductByUrl = async (url: string, storeContext: StoreContext): Promise<Product | undefined> => {
        return fetch(`${storeBaseServiceUrl('catalog', storeContext)}/api/v2/product/name/${url}?store=${storeContext.store}&lang=${storeContext.locale}`, get())
            .then(it => handleResponse<Product>(it))
    }
}
