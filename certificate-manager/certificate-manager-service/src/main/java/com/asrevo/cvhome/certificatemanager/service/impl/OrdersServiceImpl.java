package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.certificatemanager.entity.OrdersEntity;
import com.asrevo.cvhome.certificatemanager.repository.OrdersRepository;
import com.asrevo.cvhome.certificatemanager.service.OrdersService;
import com.asrevo.cvhome.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.commons.domain.OrdersId;
import com.asrevo.cvhome.commons.dto.OrdersResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class OrdersServiceImpl implements OrdersService {
    private final OrdersRepository ordersRepository;

    @Override
    public Optional<OrdersEntity> findOneById(OrdersId ordersId) {
        return ordersRepository.findById(ordersId);
    }

    @Transactional
    @Override
    public OrdersEntity save(OrdersEntity orders) {
        return ordersRepository.save(orders);
    }

    @Override
    public List<OrdersResponseDto> findAllOrderByIdIn(List<OrdersId> ordersIds) {
        // @TODO check the map
        return StreamSupport.stream(ordersRepository.findAllById(ordersIds).spliterator(), false)
                .map(it -> new OrdersResponseDto())
                .toList();
    }

    @Override
    public List<OrdersEntity> findAllSinceValidation(Set<CertificateOrderStatus> statuses, Instant from, int limit) {
        return ordersRepository.findByCertificateOrderStatusInAndValidatedDateLessThanOrderByIdAsc(statuses, from, PageRequest.of(0, limit));
    }

    @Override
    public List<OrdersEntity> findAllSinceCreation(Set<CertificateOrderStatus> statuses, Instant from, int limit) {
        return ordersRepository.findByCertificateOrderStatusInAndCreatedDateLessThanOrderByIdAsc(statuses, from, PageRequest.of(0, limit));
    }

}
