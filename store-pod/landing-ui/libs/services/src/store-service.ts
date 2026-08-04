import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {Store} from "@store-front/types/store";
import {apiFetch, get} from "./http-utils";

export class StoreService {

    /**
     * Must fail: the whole storefront is rendered from the store record. Resolving with `undefined` here
     * is what let a merchant-pod outage render a blank page under a 200.
     */
    public static getStore = async (storeContext: StoreContext): Promise<Store> => {
        return apiFetch<Store>(
            `${storeBaseServiceUrl('merchant', storeContext)}/api/v1/store/${storeContext.store}?store=${storeContext.store}&lang=${storeContext.locale}`,
            get());
    }
}
