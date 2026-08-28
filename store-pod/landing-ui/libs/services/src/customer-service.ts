import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {apiFetch, get} from "./http-utils";
import {Customer, Order, OrderHistoryList, OrderPage} from "@store-front/types";

/**
 * All must-fail. These are a shopper's own account and order records: showing an empty list because the
 * checkout service is down reads as "you have no orders", which is a worse lie than an error.
 */
export class CustomerService {

    public static getCustomerInfo = async (storeContext: StoreContext): Promise<Customer> => {
        return apiFetch<Customer>(
            `${storeBaseServiceUrl('checkout', storeContext)}/api/v1/private/customer/info?&store=${storeContext.store}&lang=${storeContext.locale}`,
            get({auth: true}));
    }

    public static listOrders = async (storeContext: StoreContext, page: number, count: number): Promise<OrderPage> => {
        return apiFetch<OrderPage>(
            `${storeBaseServiceUrl('checkout', storeContext)}/api/v1/private/customer/orders?page=${page}&count=${count}&store=${storeContext.store}&lang=${storeContext.locale}`,
            get({auth: true}));
    }

    public static getOrder = async (storeContext: StoreContext, id: number): Promise<Order> => {
        return apiFetch<Order>(
            `${storeBaseServiceUrl('checkout', storeContext)}/api/v1/private/customer/${id}/order?&store=${storeContext.store}&lang=${storeContext.locale}`,
            get({auth: true}));
    }

    public static getOrderHistory = async (storeContext: StoreContext, id: number): Promise<OrderHistoryList> => {
        return apiFetch<OrderHistoryList>(
            `${storeBaseServiceUrl('checkout', storeContext)}/api/v1/private/customer/${id}/order/history?&store=${storeContext.store}&lang=${storeContext.locale}`,
            get({auth: true}));
    }

}
