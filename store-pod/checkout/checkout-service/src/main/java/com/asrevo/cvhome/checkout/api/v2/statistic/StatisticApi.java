package com.asrevo.cvhome.checkout.api.v2.statistic;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.checkout.services.order.OrderStatisticsService;
import com.asrevo.cvhome.commons.domain.StatisticList;
import com.asrevo.cvhome.commons.domain.StatisticRange;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * The console dashboard's three checkout charts. POST because the range is a body, as the other statistic APIs do.
 */
@RestController
@RequestMapping("/api/v2")
@Tag(name = "Statistics")
@RequiredArgsConstructor
public class StatisticApi {

    private static final String MANAGE = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CHECKOUT.*')";

    private final OrderStatisticsService statistics;

    @PostMapping("/private/order-statistic")
    @PreAuthorize(MANAGE)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public StatisticList orders(@Valid @RequestBody StatisticRange range, StoreMerchantId merchantStore) {
        return statistics.orders(merchantStore, range);
    }

    @PostMapping("/private/customer-statistic")
    @PreAuthorize(MANAGE)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public StatisticList customers(@Valid @RequestBody StatisticRange range, StoreMerchantId merchantStore) {
        return statistics.customers(merchantStore, range);
    }

    @PostMapping("/private/product-statistic")
    @PreAuthorize(MANAGE)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public StatisticList products(@Valid @RequestBody StatisticRange range, StoreMerchantId merchantStore) {
        return statistics.products(merchantStore, range);
    }
}
