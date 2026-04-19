package com.asrevo.cvhome.cua.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.domain.SocialLoginConfig;
import com.asrevo.cvhome.cua.domain.SocialLoginConfigId;
import com.asrevo.cvhome.cua.repo.SocialLoginConfigRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocialLoginConfigService {

    private final SocialLoginConfigRepository repository;

    public List<SocialLoginConfig> getConfigs(StoreMerchantId merchantStore) {
        return repository.findAllByIdStoreMerchantId(merchantStore);
    }

    @Transactional
    public void saveConfigs(StoreMerchantId merchantStore, List<SocialLoginConfig> configs) {
        configs.forEach(config -> {
            config.setId(new SocialLoginConfigId(merchantStore, config.getId().providerId()));
            repository.save(config);
        });
    }

}
