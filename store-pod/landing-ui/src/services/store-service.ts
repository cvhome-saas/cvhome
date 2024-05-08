import {storeBaseServiceUrl, StoreContext} from "@/types/store-context";
import {Store} from "@/types/store";

export class StoreService {
    public static getStore = async (storeContext: StoreContext): Promise<Store> => {
        return fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/store/${storeContext.store}?store=${storeContext.store}&lang=${storeContext.local}`)
            .then(it => it.json() as unknown as Store);
    }
}