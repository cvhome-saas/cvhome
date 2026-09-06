package com.asrevo.cvhome.checkout.services.order;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.checkout.entity.Orders;
import com.asrevo.cvhome.checkout.repositories.OrderRepository;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StatisticRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderStatisticsServiceImplTest {

    private static final String CONFIRMED_2 = "CONFIRMED";

    private static final ZonedDateTime FROM = ZonedDateTime.of(2026, 9, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private static final ZonedDateTime TO = ZonedDateTime.of(2026, 9, 5, 0, 0, 0, 0, ZoneOffset.UTC);

    @Mock
    private OrderRepository orders;

    @InjectMocks
    private OrderStatisticsServiceImpl service;

    @Test
    void eachChartIsItsOwnQueryOverTheRange() {
        StatisticRange range = new StatisticRange(FROM, TO);
        when(orders.ordersPerDayAndStatus(Orders.STORE, FROM.toInstant(), TO.toInstant()))
                .thenReturn(List.of(StatisticEntry.of("2026-09-01", CONFIRMED_2, 3)));
        when(orders.customersPerCountry(Orders.STORE, FROM.toInstant(), TO.toInstant()))
                .thenReturn(List.of(StatisticEntry.of("GB", 2)));
        when(orders.unitsPerSku(Orders.STORE, FROM.toInstant(), TO.toInstant()))
                .thenReturn(List.of(StatisticEntry.of("SKU-1", 10L)));

        assertThat(service.orders(Orders.STORE, range).entries()).singleElement()
                .satisfies(e -> assertThat(e.name()).isEqualTo(CONFIRMED_2));
        assertThat(service.customers(Orders.STORE, range).entries()).singleElement()
                .satisfies(e -> assertThat(e.value()).isEqualTo(2));
        assertThat(service.products(Orders.STORE, range).entries()).singleElement()
                .satisfies(e -> assertThat(e.value()).isEqualTo(10L));
    }

    @Test
    void anOpenRangeMeansFromTheEpochUntilNow() {
        when(orders.ordersPerDayAndStatus(eq(Orders.STORE), any(), any())).thenReturn(List.of());

        service.orders(Orders.STORE, new StatisticRange(null, null));

        verify(orders).ordersPerDayAndStatus(eq(Orders.STORE), eq(Instant.EPOCH), any());
    }
}
