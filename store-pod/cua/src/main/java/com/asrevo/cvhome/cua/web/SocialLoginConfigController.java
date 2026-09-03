package com.asrevo.cvhome.cua.web;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.service.SocialLoginConfigService;
import com.asrevo.cvhome.cua.web.dto.PersistableSocialLoginConfig;
import com.asrevo.cvhome.cua.web.dto.ReadableSocialLoginConfig;
import com.asrevo.cvhome.uaa.errors.IdpAliasTakenException;
import com.asrevo.cvhome.uaa.errors.IdpConfigInvalidException;
import com.asrevo.cvhome.uaa.errors.IdpNotFoundException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/private/social-login-config")
@RequiredArgsConstructor
public class SocialLoginConfigController {

    private final SocialLoginConfigService service;

    @GetMapping
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CUA.*')")
    public List<ReadableSocialLoginConfig> getConfigs(StoreMerchantId merchantStore) {
        return service.getConfigs();
    }

    @PostMapping
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CUA.*')")
    public void saveConfigs(StoreMerchantId merchantStore, @RequestBody List<PersistableSocialLoginConfig> configs)
            throws IdpAliasTakenException, IdpConfigInvalidException, IdpNotFoundException {
        service.saveConfigs(configs);
    }

    @GetMapping("/supported-social-providers")
    public java.util.List<String> getSupportedProviders() {
        return service.supportedProviders();
    }

}
