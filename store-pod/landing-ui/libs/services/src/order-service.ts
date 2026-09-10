import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {apiFetch, get} from "./http-utils";
import {OrderStatusResult} from "@store-front/types/order";

export class OrderService {

    /**
     * Must fail. This backs the post-payment result page, where `undefined` was rendered as "we couldn't
     * find this order" — so a checkout-service blip told a shopper who had just paid that their order did
     * not exist. A 404 and an outage have to stay distinguishable all the way to the screen.
     *
     * `ref` is the order reference the payment redirect carried back (`?orderId=…&ref=…`). A signed-in shopper is
     * matched to the order by their session; a guest is matched only by the ref, and without it the service
     * answers 404 for every id — a numeric id alone must not read somebody else's order.
     */
    public static getOrderStatus = async (storeContext: StoreContext, orderId: number, ref?: string): Promise<OrderStatusResult> => {
        const refParam = ref ? `&ref=${encodeURIComponent(ref)}` : '';
        return apiFetch<OrderStatusResult>(
            `${storeBaseServiceUrl('checkout', storeContext)}/api/v1/order/${orderId}/status?store=${storeContext.store}&lang=${storeContext.locale}${refParam}`,
            get({auth: true}));
    }

}
