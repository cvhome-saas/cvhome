package com.asrevo.cvhome.merchant.service;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.merchant.repositories.merchant.MerchantRepository;
import com.asrevo.cvhome.merchant.utils.Defines;
import com.asrevo.cvhome.s2s.model.AppProperties;
import com.asrevo.cvhome.s2s.model.PodInfoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AskTlsService {

	private final MerchantRepository merchantRepository;

	private final PodInfoProperties podInfoProperties;

	public boolean ask(Domain domain) {
		log.info("Ask for TLS for domain: {}", domain);
		if (domain.equals(podInfoProperties.pod().domain())) {
			return true;
		}
		return merchantRepository.findByDomain(domain.domain(), podInfoProperties.pod().domain()).isPresent();
	}

}
