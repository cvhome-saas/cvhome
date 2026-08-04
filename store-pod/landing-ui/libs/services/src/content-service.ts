import {Box, ContentPage, Page} from "@store-front/types/content";
import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {apiFetch, get, orUndefined} from "./http-utils";

export class ContentService {

    /** Degrades: the footer page list. Missing links beat a missing storefront. */
    public static getContents = async (storeContext: StoreContext): Promise<ContentPage | undefined> => {
        return orUndefined(apiFetch<ContentPage>(
            `${storeBaseServiceUrl('merchant', storeContext)}/api/v1/content/pages?page=0&count=20&store=${storeContext.store}&lang=${storeContext.locale}`,
            get()));
    }

    /** Must fail: the CMS page is what the route is for. */
    public static getPage = async (storeContext: StoreContext, code: string): Promise<Page> => {
        return apiFetch<Page>(
            `${storeBaseServiceUrl('merchant', storeContext)}/api/v1/content/pages/name/${code}?store=${storeContext.store}&lang=${storeContext.locale}`,
            get());
    }

    /**
     * Degrades: boxes are meta titles, meta descriptions and the header banner, all fetched in the
     * layout. A store that never configured one already 404s here, so an absent box is routine.
     */
    public static getBox = async (storeContext: StoreContext, code: string): Promise<Box | undefined> => {
        return orUndefined(apiFetch<Box>(
            `${storeBaseServiceUrl('merchant', storeContext)}/api/v1/content/boxes/${code}?store=${storeContext.store}&lang=${storeContext.locale}`,
            get()));
    }
}
