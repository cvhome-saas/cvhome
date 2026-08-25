import {Category, CategoryPage} from "@store-front/types/category";
import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {apiFetch, get, orUndefined} from "./http-utils";

export class CategoryService {

    /**
     * Degrades: this is the navigation tree, fetched in the layout. Failing the whole page because the
     * menu could not load would take down a storefront that is otherwise perfectly renderable.
     */
    public static getCategories = async (storeContext: StoreContext): Promise<CategoryPage | undefined> => {
        return orUndefined(apiFetch<CategoryPage>(
            `${storeBaseServiceUrl('catalog', storeContext)}/api/v1/category-hierarchy?count=20&page=0&store=${storeContext.store}&lang=${storeContext.locale}`,
            get()));
    }

    /** Must fail: this is the category page's own subject, not decoration around it. */
    public static getCategory = async (storeContext: StoreContext, category: string): Promise<Category> => {
        return apiFetch<Category>(
            `${storeBaseServiceUrl('catalog', storeContext)}/api/v1/category/${category}?store=${storeContext.store}&lang=${storeContext.locale}`,
            get());
    }
}
