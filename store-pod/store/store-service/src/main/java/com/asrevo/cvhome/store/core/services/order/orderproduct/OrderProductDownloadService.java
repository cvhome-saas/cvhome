package com.asrevo.cvhome.store.core.services.order.orderproduct;

import com.asrevo.cvhome.store.core.entity.order.orderproduct.OrderProductDownload;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

import java.util.List;


public interface OrderProductDownloadService extends SalesManagerEntityService<Long, OrderProductDownload> {

    /**
     * List {@link OrderProductDownload} by order id
     *
     * @param orderId
     * @return
     */
    List<OrderProductDownload> getByOrderId(Long orderId);

}
