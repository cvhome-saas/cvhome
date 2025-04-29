package com.asrevo.cvhome.manager.controller.statistic;

import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StatisticList;
import com.asrevo.cvhome.commons.domain.StatisticRange;
import com.asrevo.cvhome.manager.repository.ManagerOrgRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v2")
@AllArgsConstructor
@Tag(name = "Org statistic resource", description = "Org statistic")
@Slf4j
public class OrgStatisticApi {
    private final ManagerOrgRepository managerOrgRepository;

    @RequestMapping(value = {"/private/org-statistic"}, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @ConditionalOnApiStatus
    public StatisticList orgStatistic(@RequestBody StatisticRange range) {
        List<StatisticEntry> entries = managerOrgRepository.orgStatistic(range.fromDate().toInstant(), range.toDate().toInstant());
        return new StatisticList(entries);
    }

}

