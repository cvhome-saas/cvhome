import {Product} from "@store-front/types/product-groups";
import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {apiFetch, get, orUndefined} from "./http-utils";
import {ProductGroup} from "@store-front/types";
import {InventoryService} from "./inventory-service";

export class ProductService {

    /** Degrades: a strip below the product. Its absence is not worth losing the product page over. */
    public static getRelatedProductGroup = async (storeContext: StoreContext, product: number): Promise<ProductGroup | undefined> => {
        const group = await orUndefined(apiFetch<ProductGroup>(
            `${storeBaseServiceUrl('catalog', storeContext)}/api/v1/products/${product}/relationship?store=${storeContext.store}&lang=${storeContext.locale}`,
            get()));
        await InventoryService.enrichProducts(storeContext, group?.products);
        return group;
    }

    /**
     * Degrades: the home page renders four of these strips side by side. One catalog hiccup should cost
     * that strip, not the landing page every shopper arrives on.
     */
    public static getProductByGroup = async (storeContext: StoreContext, group: string): Promise<ProductGroup | undefined> => {
        const productGroup = await orUndefined(apiFetch<ProductGroup>(
            `${storeBaseServiceUrl('catalog', storeContext)}/api/v1/products/groups/${group}?store=${storeContext.store}&lang=${storeContext.locale}`,
            get()));
        await InventoryService.enrichProducts(storeContext, productGroup?.products);
        return productGroup;
    }

    /** Must fail: this is the product page's subject. A 404 here is a real 404. */
    public static getProductByUrl = async (url: string, storeContext: StoreContext): Promise<Product> => {
        const product = await apiFetch<Product>(
            `${storeBaseServiceUrl('catalog', storeContext)}/api/v2/product/name/${url}?store=${storeContext.store}&lang=${storeContext.locale}`,
            get());
        // Stock and price live in the inventory service since the split; the enrichment degrades,
        // the product itself must not.
        await InventoryService.enrichProduct(storeContext, product);
        return product;
    }
}
