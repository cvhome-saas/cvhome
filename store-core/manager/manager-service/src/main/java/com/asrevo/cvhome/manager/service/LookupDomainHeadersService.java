package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.utils.OperationExecution;
import com.asrevo.cvhome.manager.entity.ManagerStoreEntity;
import com.asrevo.cvhome.manager.repository.ManagerStoreRepository;
import com.asrevo.cvhome.manager.utils.Defines;
import com.asrevo.cvhome.manager.utils.ErrorCodes;
import com.asrevo.cvhome.s2s.model.AppProperties;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LookupDomainHeadersService {

	private final ManagerStoreRepository storeRepository;

	private final AppProperties appProperties;

	public Map<String, String> lookupHeaders(Domain domain) {
		ManagerStoreEntity entity = storeRepository
			.findByDomain(domain.domain(), appProperties.getDomain(), Defines.SAAS_POD_SUFFIX)
			.orElseThrow(() -> new OperationExecution(ErrorCodes.store_not_found));

		return Map.of("Store-Id", entity.getId().id().toString(), "Theme", entity.getPreferences().theme().name(),
				"Default-Language", entity.getPreferences().defaultLanguage().code(), "Supported-Languages",
				entity.getPreferences()
					.supportedLanguages()
					.stream()
					.map(LanguageCode::code)
					.collect(Collectors.joining(",")));
	}

}
