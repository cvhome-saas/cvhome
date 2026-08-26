import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {apiFetch, get} from "./http-utils";
import {OrderStatusResult} from "@store-front/types/order";

export class OrderService {

    /**
     * Must fail. This backs the post-payment result page, where `undefined` was rendered as "we couldn't
     * find this order" — so a checkout-service blip told a shopper who had just paid that their order did
     * not exist. A 404 and an outage have to stay distinguishable all the way to the screen.
     */
    public static getOrderStatus = async (storeContext: StoreContext, orderId: number): Promise<OrderStatusResult> => {
        return apiFetch<OrderStatusResult>(
            `${storeBaseServiceUrl('checkout', storeContext)}/api/v1/order/${orderId}/status?store=${storeContext.store}&lang=${storeContext.locale}`,
            get({auth: true}));
    }

}
