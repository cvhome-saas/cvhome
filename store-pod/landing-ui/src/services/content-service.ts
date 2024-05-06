import {storeBaseServiceUrl, StoreContext} from "@/types/store-context";
import {ContentPage, Page} from "@/types/content";

export class ContentService {
    public static getContents = async (storeContext: StoreContext): Promise<ContentPage> => {
        return fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/content/pages?page=0&count=20&store=${storeContext.store}&lang=${storeContext.local}`)
            .then(it => it.json() as unknown as ContentPage)
    }
    public static getPage = async (storeContext: StoreContext, code: string): Promise<Page> => {
        return fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/content/pages/name/${code}?store=${storeContext.store}&lang=${storeContext.local}`)
            .then(it => it.json() as unknown as Page)
    }
}