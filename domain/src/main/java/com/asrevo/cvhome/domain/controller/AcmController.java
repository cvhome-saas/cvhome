package com.asrevo.cvhome.domain.controller;

import com.asrevo.cvhome.commons.domain.DomainReferenceOrder;
import com.asrevo.cvhome.domain.service.DomainReferenceAcmService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/domain-reference-acm")
@AllArgsConstructor
@Slf4j
public class AcmController {
    private final DomainReferenceAcmService domainReferenceAcmService;

    @PostMapping(value = "order", params = {"domainId"})
    Mono<DomainReferenceOrder> order(@RequestParam("domainId") Long domainId) {
        return domainReferenceAcmService.order(domainId);
    }
}
