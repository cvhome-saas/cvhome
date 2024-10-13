package com.asrevo.cvhome.store.core.services.order.orderproduct;

import com.asrevo.cvhome.store.core.entity.order.orderproduct.OrderProductDownload;
import com.asrevo.cvhome.store.core.repositories.order.orderproduct.OrderProductDownloadRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("orderProductDownloadService")
@Slf4j
public class OrderProductDownloadServiceImpl
        extends SalesManagerEntityServiceImpl<Long, OrderProductDownload>
        implements OrderProductDownloadService {

    private final OrderProductDownloadRepository orderProductDownloadRepository;

    @Autowired
    public OrderProductDownloadServiceImpl(
            OrderProductDownloadRepository orderProductDownloadRepository) {
        super(orderProductDownloadRepository);
        this.orderProductDownloadRepository = orderProductDownloadRepository;
    }

    @Override
    public List<OrderProductDownload> getByOrderId(Long orderId) {
        return orderProductDownloadRepository.findByOrderId(orderId);
    }
}
