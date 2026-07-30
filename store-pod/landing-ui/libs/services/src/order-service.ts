import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {get, handleResponse} from "./http-utils";
import {OrderStatusResult} from "@store-front/types/order";

export class OrderService {

    public static getOrderStatus = async (storeContext: StoreContext, orderId: number): Promise<OrderStatusResult | undefined> => {
        return fetch(`${storeBaseServiceUrl('checkout', storeContext)}/api/v1/order/${orderId}/status?store=${storeContext.store}&lang=${storeContext.locale}`, get())
            .then(it => handleResponse<OrderStatusResult>(it))
    }

}
