import {Category, CategoryPage} from "@/types/category";
import {storeBaseServiceUrl, StoreContext} from "@/types/store-context";
import {handleResponse} from "@/utils/http-utils";

export class CategoryService {
    public static getCategories = async (storeContext: StoreContext): Promise<CategoryPage | undefined> => {
        return await fetch(`${storeBaseServiceUrl('catalog')}/api/v1/category-hierarchy?count=20&page=0&store=${storeContext.store}&lang=${storeContext.locale}`)
            .then(it => handleResponse(it))
    }
    public static getCategory = async (storeContext: StoreContext, category: string): Promise<Category | undefined> => {
        return await fetch(`${storeBaseServiceUrl('catalog')}/api/v1/category/${category}?store=${storeContext.store}&lang=${storeContext.locale}`)
            .then(it => handleResponse(it))
    }
}