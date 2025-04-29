import {storeBaseServiceUrl, StoreContext} from "@/types/store-context";
import {Store} from "@/types/store";
import {handleResponse} from "@/utils/http-utils";

export class StoreService {

    public static getStore = async (storeContext: StoreContext): Promise<Store | undefined> => {
        return fetch(`${storeBaseServiceUrl('merchant')}/api/v1/store/${storeContext.store}?store=${storeContext.store}&lang=${storeContext.locale}`)
            .then(it => handleResponse(it))
    }
}