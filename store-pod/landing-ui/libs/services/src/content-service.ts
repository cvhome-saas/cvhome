import { Box, ContentPage, Page } from "@store-front/types/content";
import { storeBaseServiceUrl, StoreContext } from "@store-front/types/store-context";
import { handleResponse } from "./http-utils";

export class ContentService {
    public static getContents = async (storeContext: StoreContext): Promise<ContentPage | undefined> => {
        return fetch(`${storeBaseServiceUrl('merchant', storeContext)}/api/v1/content/pages?page=0&count=20&store=${storeContext.store}&lang=${storeContext.locale}`)
            .then(it => handleResponse<ContentPage>(it))
    }
    public static getPage = async (storeContext: StoreContext, code: string): Promise<Page | undefined> => {
        return fetch(`${storeBaseServiceUrl('merchant', storeContext)}/api/v1/content/pages/name/${code}?store=${storeContext.store}&lang=${storeContext.locale}`)
            .then(it => handleResponse<Page>(it))
    }

    public static getBox = async (storeContext: StoreContext, code: string): Promise<Box | undefined> => {
        return fetch(`${storeBaseServiceUrl('merchant', storeContext)}/api/v1/content/boxes/${code}?store=${storeContext.store}&lang=${storeContext.locale}`)
            .then(it => handleResponse<Box>(it))
    }
}