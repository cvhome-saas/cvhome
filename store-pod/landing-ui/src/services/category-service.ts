import {CategoryPage} from "@/types/category";
import {storeBaseServiceUrl, StoreContext} from "@/types/store-context";

export class CategoryService {
    public static getCategories = async (storeContext: StoreContext): Promise<CategoryPage> => {
        return await fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/category?count=20&page=0&store=${storeContext.store}&lang=${storeContext.local}`)
            .then(it => it.json() as unknown as CategoryPage);
    }
}