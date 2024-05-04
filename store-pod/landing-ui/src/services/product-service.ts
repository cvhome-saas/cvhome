import {Product, ProductGroupPage} from "@/types/product-groups";
import {storeBaseServiceUrl, StoreContext} from "@/types/store-context";

export class ProductService {
    public static getFeaturedItemsProductGroup = async (storeContext: StoreContext): Promise<ProductGroupPage> => {
        return fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/products/group/FEATURED_ITEMS?store=${storeContext.store}&lang=${storeContext.local}`)
            .then((it: Response) => {
                return it.json() as unknown as ProductGroupPage
            });
    }
    public static getProductByFriendlyUrl = async (storeContext: StoreContext, friendlyUrl: string): Promise<Product> => {
        return fetch(`${storeBaseServiceUrl(storeContext)}/api/v2/product/name/${friendlyUrl}?store=${storeContext.store}&lang=${storeContext.local}`)
            .then((it: Response) => {
                return it.json() as unknown as Product
            });
    }
}