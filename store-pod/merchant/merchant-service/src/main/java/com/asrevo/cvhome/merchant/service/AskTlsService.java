package com.asrevo.cvhome.merchant.service;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.merchant.repositories.merchant.MerchantRepository;
import com.asrevo.cvhome.s2s.model.PodInfoProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AskTlsService {

    private final MerchantRepository merchantRepository;

    private final PodInfoProperties podInfoProperties;

    public boolean ask(Domain domain) {
        log.info("Ask for TLS for domain: {}", domain);
        if (domain.matches(podInfoProperties.pod().domain())) {
            return true;
        }
        return merchantRepository.findByDomain(domain.domain(), podInfoProperties.pod().domain()).isPresent();
    }

}
