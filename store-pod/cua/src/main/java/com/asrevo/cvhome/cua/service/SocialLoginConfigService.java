package com.asrevo.cvhome.cua.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.domain.SocialLoginConfigId;
import com.asrevo.cvhome.cua.repo.SocialLoginConfigRepository;
import com.asrevo.cvhome.cua.web.dto.PersistableSocialLoginConfig;
import com.asrevo.cvhome.cua.web.dto.ReadableSocialLogin;
import com.asrevo.cvhome.cua.web.dto.ReadableSocialLoginConfig;
import com.asrevo.cvhome.cua.web.mapper.SocialLoginConfigMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocialLoginConfigService {

    private final SocialLoginConfigRepository repository;
    private final SocialLoginConfigMapper mapper;

    public List<ReadableSocialLoginConfig> getConfigs(StoreMerchantId merchantStore) {
        return repository.findAllByIdStoreMerchantId(merchantStore).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public void saveConfigs(StoreMerchantId merchantStore, List<PersistableSocialLoginConfig> configs) {
        configs.forEach(dto -> {
            dto.setStoreMerchantId(merchantStore);
            repository.save(mapper.toEntity(dto));
        });
    }

    /** The providers switched on for a store, shaped for a login page: id, display name, registration id — no secrets. */
    public List<ReadableSocialLogin> enabledLogins(StoreMerchantId merchantStore) {
        return repository.findEnabledSocialLoginConfig(merchantStore).stream()
                .map(id -> new ReadableSocialLogin(id.providerId().getProviderId(), id.providerId().getClientName(),
                        id.toRegistrationId()))
                .toList();
    }

    public Optional<ReadableSocialLoginConfig> findById(SocialLoginConfigId socialLoginConfigId) {
        return repository.findById(socialLoginConfigId).map(mapper::toDTO);
    }
}
