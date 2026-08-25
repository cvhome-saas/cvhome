package com.asrevo.cvhome.merchant.services.merchant;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainType;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.ManagerStoreDomain;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.errors.MerchantStoreNotFoundException;
import com.asrevo.cvhome.merchant.repositories.merchant.MerchantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MerchantRoutingService {

    private final MerchantRepository merchantRepository;

    public boolean containsDomain(Domain domain, String podDomain) {
        return merchantRepository.findByDomain(domain.domain(), podDomain).isPresent();
    }

    public Map<String, String> lookupHeaders(Domain domain, String podDomain) {
        return merchantRepository.findByDomain(domain.domain(), podDomain)
                .map(MerchantRoutingService::mapHeaders)
                .orElseGet(Map::of);
    }

    @Transactional(readOnly = true)
    public Set<ManagerStoreDomain> domains(StoreMerchantId store) throws MerchantStoreNotFoundException {
        return findStore(store).getStoreDomains();
    }

    @Transactional
    public void addDomain(StoreMerchantId store, Domain domain) throws MerchantStoreNotFoundException {
        MerchantStore merchantStore = findStore(store);
        merchantStore.getStoreDomains().add(new ManagerStoreDomain(domain.domain(), DomainType.CUSTOM_DOMAIN));
        merchantRepository.save(merchantStore);
    }

    @Transactional
    public void removeDomain(StoreMerchantId store, Domain domain) throws MerchantStoreNotFoundException {
        MerchantStore merchantStore = findStore(store);
        merchantStore.getStoreDomains().remove(new ManagerStoreDomain(domain.domain(), DomainType.CUSTOM_DOMAIN));
        merchantRepository.save(merchantStore);
    }

    private MerchantStore findStore(StoreMerchantId store) throws MerchantStoreNotFoundException {
        return merchantRepository.findById(store).orElseThrow(() -> MerchantStoreNotFoundException.of(store));
    }

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

}
