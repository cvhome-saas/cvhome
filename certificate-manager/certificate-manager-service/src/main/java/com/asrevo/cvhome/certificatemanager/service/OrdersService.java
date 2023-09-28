package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.certificatemanager.commons.domain.OrdersId;
import com.asrevo.cvhome.certificatemanager.commons.dto.OrdersResponseDto;
import com.asrevo.cvhome.certificatemanager.entity.OrdersEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrdersService {

    Optional<OrdersEntity> findOneById(OrdersId ordersId);

    OrdersEntity save(OrdersEntity domainCertificateOrder);

    List<OrdersResponseDto> findAllOrderByIdIn(List<OrdersId> ordersIds);

    List<OrdersEntity> findAllSinceValidation(Set<CertificateOrderStatus> statuses, Instant from, int limit);

    List<OrdersEntity> findAllSinceCreation(Set<CertificateOrderStatus> statuses, Instant from, int limit);
}
