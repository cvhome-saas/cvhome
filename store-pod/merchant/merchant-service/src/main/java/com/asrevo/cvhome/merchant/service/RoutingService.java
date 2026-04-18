package com.asrevo.cvhome.merchant.service;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainType;
import com.asrevo.cvhome.commons.domain.ManagerStoreDomain;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.repositories.merchant.MerchantRepository;
import com.asrevo.cvhome.merchant.services.merchant.MerchantStoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingService {

    private final MerchantRepository merchantRepository;

    private final MerchantStoreService merchantStoreService;

    public Set<ManagerStoreDomain> domains(StoreMerchantId store) {
        return merchantRepository.getReferenceById(store).getStoreDomains();
    }

    @Transactional
    public void addDomain(StoreMerchantId store, Domain domain) {
        MerchantStore merchantStore = merchantRepository.getReferenceById(store);
        merchantStore.getStoreDomains().add(new ManagerStoreDomain(domain.domain(), DomainType.CUSTOM_DOMAIN));
        merchantRepository.save(merchantStore);
    }

    @Transactional
    public void removeDomain(StoreMerchantId store, Domain domain) {
        MerchantStore merchantStore = merchantRepository.getReferenceById(store);
        merchantStore.getStoreDomains().remove(new ManagerStoreDomain(domain.domain(), DomainType.CUSTOM_DOMAIN));
        merchantStoreService.save(merchantStore);
    }

}
