package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.commons.domain.OrdersId;
import com.asrevo.cvhome.certificatemanager.commons.dto.OrdersResponseDto;
import com.asrevo.cvhome.certificatemanager.entity.OrdersEntity;

import java.util.List;
import java.util.Optional;

public interface OrdersService {

    Optional<OrdersEntity> findOneById(OrdersId ordersId);

    OrdersEntity save(OrdersEntity domainCertificateOrder);

    List<OrdersResponseDto> findAllOrderByIdIn(List<OrdersId> ordersIds);
}
