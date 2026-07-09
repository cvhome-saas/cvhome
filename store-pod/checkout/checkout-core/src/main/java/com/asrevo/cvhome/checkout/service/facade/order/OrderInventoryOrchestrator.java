package com.asrevo.cvhome.checkout.service.facade.order;

import com.asrevo.cvhome.catalog.model.product.ProductReservationResult;
import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;

public interface OrderInventoryOrchestrator {

    ProductReservationResult reserveProduct(StoreMerchantId store, Order order);

    void updateOrderStatusWithReservationCommit(Long orderId, StoreMerchantId store, OrderStatus successOrder, PaymentStatus successPay);

    void updateOrderStatusWithReservationRelease(Long orderId, StoreMerchantId store, OrderStatus successOrder, PaymentStatus successPay);
}
