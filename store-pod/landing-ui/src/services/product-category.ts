import {storeBaseServiceUrl, StoreContext} from "@/types/store-context";
import {Manufacturer, ProductGroupPage} from "@/types/product-groups";

export class ProductCategory {
    public static getManufacturers = async (storeContext: StoreContext, categoryId: number): Promise<Manufacturer[]> => {
        return fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/category/${categoryId}/manufacturer?store=${storeContext.store}&lang=${storeContext.local}`)
            .then(it => it.json() as unknown as Manufacturer[]);
    }
    public static getProducts = async (storeContext: StoreContext, categoryId?: number, manufacturerId?: number): Promise<ProductGroupPage> => {
        const categoryParam = categoryId ? `&category=${categoryId}` : '';
        const manufacturerParam = manufacturerId ? `&manufacturer=${manufacturerId}` : '';
        return fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/products?store=${storeContext.store}${categoryParam}${manufacturerParam}&page=0&count=15&lang=${storeContext.local}`)
            .then(it => it.json() as unknown as ProductGroupPage);
    }

}