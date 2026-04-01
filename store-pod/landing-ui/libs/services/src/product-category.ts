import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {Manufacturer, ProductGroupPage} from "@store-front/types/product-groups";
import {get, handleResponse} from "./http-utils";

export class ProductCategory {

    public static getManufacturers = async (storeContext: StoreContext, categoryId: number): Promise<Manufacturer[] | undefined> => {
        return fetch(`${storeBaseServiceUrl('catalog', storeContext)}/api/v1/category/${categoryId}/manufacturer?store=${storeContext.store}&lang=${storeContext.locale}`, get())
            .then(it => handleResponse<Manufacturer[]>(it))
    }

    public static getProducts = async (storeContext: StoreContext, categoryId?: number, manufacturerId?: number): Promise<ProductGroupPage | undefined> => {
        const categoryParam = categoryId ? `&categoryIds=${categoryId}` : '';
        const manufacturerParam = manufacturerId ? `&manufacturerId=${manufacturerId}` : '';
        return fetch(`${storeBaseServiceUrl('catalog', storeContext)}/api/v2/products?store=${storeContext.store}${categoryParam}${manufacturerParam}&page=0&count=15&lang=${storeContext.locale}`, get())
            .then(it => handleResponse<ProductGroupPage>(it))
    }

}
