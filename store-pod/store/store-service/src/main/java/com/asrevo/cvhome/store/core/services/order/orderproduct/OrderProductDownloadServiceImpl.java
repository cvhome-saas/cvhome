package com.asrevo.cvhome.store.core.services.order.orderproduct;


import com.asrevo.cvhome.store.core.entity.order.orderproduct.OrderProductDownload;
import com.asrevo.cvhome.store.core.repositories.order.orderproduct.OrderProductDownloadRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("orderProductDownloadService")
public class OrderProductDownloadServiceImpl  extends SalesManagerEntityServiceImpl<Long, OrderProductDownload> implements OrderProductDownloadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderProductDownloadServiceImpl.class);


    private final OrderProductDownloadRepository orderProductDownloadRepository;

    @Autowired
    public OrderProductDownloadServiceImpl(OrderProductDownloadRepository orderProductDownloadRepository) {
        super(orderProductDownloadRepository);
        this.orderProductDownloadRepository = orderProductDownloadRepository;
    }
    
    @Override
    public List<OrderProductDownload> getByOrderId(Long orderId) {
    	return orderProductDownloadRepository.findByOrderId(orderId);
    }


}
