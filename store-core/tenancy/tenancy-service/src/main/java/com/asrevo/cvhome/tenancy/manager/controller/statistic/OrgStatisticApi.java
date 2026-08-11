package com.asrevo.cvhome.tenancy.manager.controller.statistic;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StatisticList;
import com.asrevo.cvhome.commons.domain.StatisticRange;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerOrgRepository;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v2")
@AllArgsConstructor
@Tag(name = "Org statistic resource", description = "Org statistic")
@Slf4j
public class OrgStatisticApi {

    private final ManagerOrgRepository managerOrgRepository;

    @PostMapping(value = {"/private/org-statistic"})
    @ResponseStatus(HttpStatus.OK)
    public StatisticList orgStatistic(@RequestBody StatisticRange range) {
        List<StatisticEntry> entries = managerOrgRepository.orgStatistic(range.fromDate().toInstant(),
                range.toDate().toInstant());
        return new StatisticList(entries);
    }

}
