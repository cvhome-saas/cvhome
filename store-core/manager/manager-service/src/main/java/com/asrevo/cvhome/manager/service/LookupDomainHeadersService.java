package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.ManagerStorePreferences;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

		HashMap<String, String> headers = new HashMap<>();

		if (Objects.nonNull(entity.getId())) {
			headers.put("Store-Id", entity.getId().id().toString());
		}
		if (Objects.nonNull(entity.getPreferences())) {
			ManagerStorePreferences preferences = entity.getPreferences();
			if (Objects.nonNull(preferences.theme())) {
				headers.put("Theme", preferences.theme().name());
			}
			if (Objects.nonNull(preferences.defaultLanguage())) {
				headers.put("Default-Language", preferences.defaultLanguage().code());
			}
			if (Objects.nonNull(preferences.supportedLanguages())) {
				String supportedLanguages = preferences.supportedLanguages()
					.stream()
					.map(LanguageCode::code)
					.collect(Collectors.joining(","));
				headers.put("Supported-Languages", supportedLanguages);
			}
		}
		return headers;
	}

}
