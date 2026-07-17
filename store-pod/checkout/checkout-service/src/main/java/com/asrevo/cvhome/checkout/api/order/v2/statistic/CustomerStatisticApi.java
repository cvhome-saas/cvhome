package com.asrevo.cvhome.checkout.api.order.v2.statistic;

import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.checkout.repositories.order.OrderRepository;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StatisticList;
import com.asrevo.cvhome.commons.domain.StatisticRange;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping("/api/v2")
@AllArgsConstructor
@Tag(name = "Customer statistic resource", description = "Customer statistic")
@Slf4j
public class CustomerStatisticApi {

    private final OrderRepository orderRepository;

    @PostMapping(value = {"/private/customer-statistic"})
    @ResponseStatus(HttpStatus.OK)
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CHECKOUT.*')")
    public StatisticList customerStatistic(StoreMerchantId merchantStore, LanguageCode language,
                                           @RequestBody StatisticRange range) {
        List<StatisticEntry> entries = orderRepository.customerStatistic(Date.from(range.fromDate().toInstant()),
                Date.from(range.toDate().toInstant()), merchantStore);
        return new StatisticList(entries);
    }

}
