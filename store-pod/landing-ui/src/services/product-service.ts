import {Product, ProductGroupPage} from "@/types/product-groups";
import {storeBaseServiceUrl, StoreContext} from "@/types/store-context";
import {handleResponse} from "@/utils/http-utils";

export class ProductService {

    public static getHomePageProductGroup = async (storeContext: StoreContext): Promise<ProductGroupPage | undefined> => {
        return this.getProductByGroup(storeContext, 'HOME_PAGE');
    }

    public static getRecommendedProductGroup = async (storeContext: StoreContext): Promise<ProductGroupPage | undefined> => {
        return this.getProductByGroup(storeContext, 'RECOMMENDED');
    }

    public static getNewlyAddedProductGroup = async (storeContext: StoreContext): Promise<ProductGroupPage | undefined> => {
        return this.getProductByGroup(storeContext, 'NEWLY_ADDED');
    }

    public static getFeaturedItemsProductGroup = async (storeContext: StoreContext): Promise<ProductGroupPage | undefined> => {
        return this.getProductByGroup(storeContext, 'FEATURED_ITEMS');
    }

    public static getRelatedProductGroup = async (storeContext: StoreContext, product: number): Promise<ProductGroupPage | undefined> => {
        return fetch(`${storeBaseServiceUrl('catalog', storeContext)}/api/v1/products/${product}/related?store=${storeContext.store}&lang=${storeContext.locale}`)
            .then(it => handleResponse(it))
    }

    public static getProductByGroup = async (storeContext: StoreContext, group: string): Promise<ProductGroupPage | undefined> => {
        return fetch(`${storeBaseServiceUrl('catalog', storeContext)}/api/v1/products/group/${group}?store=${storeContext.store}&lang=${storeContext.locale}`)
            .then(it => handleResponse(it))
    }

    public static getProductByUrl = async (url: string, storeContext: StoreContext): Promise<Product | undefined> => {
        return fetch(`${storeBaseServiceUrl('catalog', storeContext)}/api/v2/product/name/${url}?store=${storeContext.store}&lang=${storeContext.locale}`)
            .then(it => handleResponse(it))
    }
}