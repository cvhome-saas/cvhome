import {useCallback, useEffect, useState} from 'react';
import {OrderService} from '@store-front/services/order-service';
import {OrderStatusResult, StoreContext} from '@store-front/types';

export function useOrderStatus(storeContext: StoreContext, orderId: number | undefined) {
    const [orderStatus, setOrderStatus] = useState<OrderStatusResult | undefined>(undefined);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<Error | null>(null);

    const fetchOrderStatus = useCallback(async () => {
        if (!orderId) return;
        setLoading(true);
        try {
            const result = await OrderService.getOrderStatus(storeContext, orderId);
            setOrderStatus(result);
            return result;
        } catch (err) {
            setError(err as Error);
        } finally {
            setLoading(false);
        }
    }, [storeContext, orderId]);

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
