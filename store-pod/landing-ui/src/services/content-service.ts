import {Box, ContentPage, Page} from "@/types/content";
import {storeBaseServiceUrl, StoreContext} from "@/types/store-context";
import {handleResponse} from "@/utils/http-utils";

export class ContentService {
    public static getContents = async (storeContext: StoreContext): Promise<ContentPage | undefined> => {
        return fetch(`${storeBaseServiceUrl('content')}/api/v1/content/pages?page=0&count=20&store=${storeContext.store}&lang=${storeContext.locale}`)
            .then(it => handleResponse(it))
    }
    public static getPage = async (storeContext: StoreContext, code: string): Promise<Page | undefined> => {
        return fetch(`${storeBaseServiceUrl('content')}/api/v1/content/pages/name/${code}?store=${storeContext.store}&lang=${storeContext.locale}`)
            .then(it => handleResponse(it))
    }

    public static getBox = async (storeContext: StoreContext, code: string): Promise<Box | undefined> => {
        return fetch(`${storeBaseServiceUrl('content')}/api/v1/content/boxes/${code}?store=${storeContext.store}&lang=${storeContext.locale}`)
            .then(it => handleResponse(it))
    }
}
