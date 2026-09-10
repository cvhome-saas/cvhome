import {useCallback, useEffect, useState} from 'react';
import {OrderService} from '@store-front/services/order-service';
import {OrderStatusResult, StoreContext} from '@store-front/types';

/** `ref` is the order reference from the return URL; a guest cannot read the status without it. */
export function useOrderStatus(storeContext: StoreContext, orderId: number | undefined, ref?: string) {
    const [orderStatus, setOrderStatus] = useState<OrderStatusResult | undefined>(undefined);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<Error | null>(null);

    const fetchOrderStatus = useCallback(async () => {
        if (!orderId) return;
        setLoading(true);
        try {
            const result = await OrderService.getOrderStatus(storeContext, orderId, ref);
            setOrderStatus(result);
            return result;
        } catch (err) {
            setError(err as Error);
        } finally {
            setLoading(false);
        }
    }, [storeContext, orderId, ref]);

    useEffect(() => {
        fetchOrderStatus().then();
    }, [fetchOrderStatus]);

    return {
        orderStatus,
        loading,
        error,
        refetch: fetchOrderStatus,
    };
}
