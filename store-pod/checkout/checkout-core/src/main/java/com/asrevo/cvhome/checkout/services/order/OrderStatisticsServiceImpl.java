package com.asrevo.cvhome.checkout.services.order;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.checkout.repositories.OrderRepository;
import com.asrevo.cvhome.commons.domain.StatisticList;
import com.asrevo.cvhome.commons.domain.StatisticRange;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderStatisticsServiceImpl implements OrderStatisticsService {

    private final OrderRepository orders;

    @Override
    @Transactional(readOnly = true)
    public StatisticList orders(StoreMerchantId store, StatisticRange range) {
        return new StatisticList(orders.ordersPerDayAndStatus(store, from(range), to(range)));
    }

    @Override
    @Transactional(readOnly = true)
    public StatisticList customers(StoreMerchantId store, StatisticRange range) {
        return new StatisticList(orders.customersPerCountry(store, from(range), to(range)));
    }

    @Override
    @Transactional(readOnly = true)
    public StatisticList products(StoreMerchantId store, StatisticRange range) {
        return new StatisticList(orders.unitsPerSku(store, from(range), to(range)));
    }

    private static Instant from(StatisticRange range) {
        return range.fromDate() == null ? Instant.EPOCH : range.fromDate().toInstant();
    }

    private static Instant to(StatisticRange range) {
        return range.toDate() == null ? Instant.now() : range.toDate().toInstant();
    }
}
