import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {Store} from "@store-front/types/store";
import {get, handleResponse} from "./http-utils";

export class StoreService {

    public static getStore = async (storeContext: StoreContext): Promise<Store | undefined> => {
        return fetch(`${storeBaseServiceUrl('merchant', storeContext)}/api/v1/store/${storeContext.store}?store=${storeContext.store}&lang=${storeContext.locale}`, get())
            .then(it => handleResponse<Store>(it))
    }
}
