package com.asrevo.cvhome.merchant.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.repositories.merchant.MerchantRepository;
import com.asrevo.cvhome.s2s.model.PodInfoProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LookupDomainHeadersService {

    private final MerchantRepository merchantRepository;

    private final PodInfoProperties podInfoProperties;

    private static Map<String, String> mapHeaders(MerchantStore entity) {
        Map<String, String> headers = new HashMap<>();
        if (Objects.nonNull(entity.getId())) {
            headers.put("Store-Id", entity.getId().getId());
        }
        if (Objects.nonNull(entity.getTheme())) {
            headers.put("Theme", entity.getTheme().name());
        }
        if (Objects.nonNull(entity.getColorTheme())) {
            headers.put("Color-Theme", entity.getColorTheme().name());
        }
        if (Objects.nonNull(entity.getDefaultLanguageCode())) {
            headers.put("Default-Language", entity.getDefaultLanguageCode().code());
        }
        if (Objects.nonNull(entity.getLanguages())) {
            String supportedLanguages = entity.getLanguages()
                    .stream()
                    .map(LanguageCode::code)
                    .collect(Collectors.joining(","));
            headers.put("Supported-Languages", supportedLanguages);
        }
        return headers;
    }

    public Map<String, String> lookupHeaders(Domain domain) {
        return merchantRepository.findByDomain(domain.domain(), podInfoProperties.pod().domain())
                .map(LookupDomainHeadersService::mapHeaders)
                .orElseGet(Map::of);
    }

}
