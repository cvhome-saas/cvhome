package com.asrevo.cvhome.domaincertificatemanager.service.impl;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.OrdersId;
import com.asrevo.cvhome.domaincertificatemanager.commons.dto.OrdersResponseDto;
import com.asrevo.cvhome.domaincertificatemanager.entity.OrdersEntity;
import com.asrevo.cvhome.domaincertificatemanager.repository.OrdersRepository;
import com.asrevo.cvhome.domaincertificatemanager.service.OrdersService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
}
