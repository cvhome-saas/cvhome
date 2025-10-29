package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.manager.utils.Defines;
import com.asrevo.cvhome.s2s.model.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AskTlsService {

	private final InternalStoreService internalStoreService;

	private final AppProperties appProperties;

	public boolean ask(Domain domain) {
		log.info("Ask for TLS for domain: {}", domain);
		String appDomain = appProperties.getDomain();
		if (domain.domain().endsWith(appDomain)) {
			if (domain.domain().startsWith(Defines.STORE_POD_PREFIX)
					|| domain.domain().startsWith(Defines.SAAS_POD_SUFFIX)) {
				return true;
			}
		}
		return internalStoreService.getReferenceByDomain(domain).isPresent();
	}

}
