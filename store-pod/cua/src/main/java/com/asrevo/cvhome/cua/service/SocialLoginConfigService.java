package com.asrevo.cvhome.cua.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.repo.SocialLoginConfigRepository;
import com.asrevo.cvhome.cua.web.dto.PersistableSocialLoginConfig;
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

}
