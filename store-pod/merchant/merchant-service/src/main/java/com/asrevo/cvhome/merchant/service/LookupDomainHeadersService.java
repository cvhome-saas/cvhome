package com.asrevo.cvhome.merchant.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.merchant.services.merchant.MerchantRoutingService;
import com.asrevo.cvhome.s2s.model.PodInfoProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LookupDomainHeadersService {

    private final MerchantRoutingService merchantRoutingService;

    private final PodInfoProperties podInfoProperties;

    public Map<String, String> lookupHeaders(Domain domain) {
        return merchantRoutingService.lookupHeaders(domain, podInfoProperties.pod().domain());
    }

}
