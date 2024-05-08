import {Category, CategoryPage} from "@/types/category";
import {storeBaseServiceUrl, StoreContext} from "@/types/store-context";

export class CategoryService {
    public static getCategories = async (storeContext: StoreContext): Promise<CategoryPage> => {
        return await fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/category?count=20&page=0&store=${storeContext.store}&lang=${storeContext.local}`)
            .then(it => it.json() as unknown as CategoryPage);
    }
    public static getCategory = async (storeContext: StoreContext,category:string): Promise<Category> => {
        return await fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/category/${category}?store=${storeContext.store}&lang=${storeContext.local}`)
            .then(it => it.json() as unknown as Category);
    }
}