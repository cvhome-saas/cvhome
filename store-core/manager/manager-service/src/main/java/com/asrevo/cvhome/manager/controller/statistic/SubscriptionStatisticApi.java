package com.asrevo.cvhome.manager.controller.statistic;

import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StatisticList;
import com.asrevo.cvhome.commons.domain.StatisticRange;
import com.asrevo.cvhome.manager.repository.ManagerOrgRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2")
@AllArgsConstructor
@Tag(name = "Subscription statistic resource", description = "Subscription statistic")
@Slf4j
public class SubscriptionStatisticApi {
    private final ManagerOrgRepository managerOrgRepository;

    @RequestMapping(
            value = {"/private/subscription-statistic"},
            method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @ConditionalOnApiStatus
    public StatisticList subscriptionStatistic(@RequestBody StatisticRange range) {
        List<StatisticEntry> entries =
                managerOrgRepository.subscriptionStatistic(
                        range.fromDate().toInstant(), range.toDate().toInstant());
        return new StatisticList(entries);
    }
}
