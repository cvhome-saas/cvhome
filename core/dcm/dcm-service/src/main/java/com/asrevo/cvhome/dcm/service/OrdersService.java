package com.asrevo.cvhome.dcm.service;

import com.asrevo.cvhome.dcm.commons.domain.OrdersId;
import com.asrevo.cvhome.dcm.commons.dto.OrdersResponseDto;
import com.asrevo.cvhome.dcm.entity.OrdersEntity;

import java.util.List;
import java.util.Optional;

public interface OrdersService {

    Optional<OrdersEntity> findOneById(OrdersId ordersId);

    OrdersEntity save(OrdersEntity domainCertificateOrder);

    List<OrdersResponseDto> findAllOrderByIdIn(List<OrdersId> ordersIds);
}
