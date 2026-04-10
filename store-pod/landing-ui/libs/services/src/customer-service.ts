import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {get, handleResponse} from "./http-utils";
import {Customer, Order, OrderHistoryList, OrderPage} from "@store-front/types";

export class CustomerService {

    public static getCustomerInfo = async (storeContext: StoreContext): Promise<Customer | undefined> => {
        return fetch(`${storeBaseServiceUrl('checkout', storeContext)}/api/v1/private/customer/info?&store=${storeContext.store}&lang=${storeContext.locale}`, get())
            .then(it => handleResponse<Customer>(it))
    }

    public static listOrders = async (storeContext: StoreContext, page: number, count: number): Promise<OrderPage | undefined> => {
        return fetch(`${storeBaseServiceUrl('checkout', storeContext)}/api/v1/private/customer/orders?page=${page}&count=${count}&store=${storeContext.store}&lang=${storeContext.locale}`, get())
            .then(it => handleResponse<OrderPage>(it))
    }

    public static getOrder = async (storeContext: StoreContext, id: number): Promise<Order | undefined> => {
        return fetch(`${storeBaseServiceUrl('checkout', storeContext)}/api/v1/private/customer/${id}/order?&store=${storeContext.store}&lang=${storeContext.locale}`, get())
            .then(it => handleResponse<Order>(it))
    }

    public static getOrderHistory = async (storeContext: StoreContext, id: number): Promise<OrderHistoryList | undefined> => {
        return fetch(`${storeBaseServiceUrl('checkout', storeContext)}/api/v1/private/customer/${id}/order/history?&store=${storeContext.store}&lang=${storeContext.locale}`, get())
            .then(it => handleResponse<OrderHistoryList>(it))
    }

}