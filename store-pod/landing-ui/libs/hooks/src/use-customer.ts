import {useCallback, useState} from 'react';
import {CustomerService} from '@store-front/services/customer-service';
import {StoreContext} from '@store-front/types';

export function useCustomer(storeContext: StoreContext) {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<Error | null>(null);

    const getCustomerInfo = useCallback(async () => {
        setLoading(true);
        try {
            return await CustomerService.getCustomerInfo(storeContext);
        } catch (err) {
            setError(err as Error);
        } finally {
            setLoading(false);
        }
    }, [storeContext]);

    const listOrders = useCallback(async (page: number, count: number) => {
        setLoading(true);
        try {
            return await CustomerService.listOrders(storeContext, page, count);
        } catch (err) {
            setError(err as Error);
        } finally {
            setLoading(false);
        }
    }, [storeContext]);

    const getOrderDetails = useCallback(async (id: number) => {
        setLoading(true);
        try {
            const order = await CustomerService.getOrder(storeContext, id);
            const history = await CustomerService.getOrderHistory(storeContext, id);
            return {order, history};
        } catch (err) {
            setError(err as Error);
        } finally {
            setLoading(false);
        }
    }, [storeContext]);

    return {
        loading,
        error,
        getCustomerInfo,
        listOrders,
        getOrderDetails,
    };
}
