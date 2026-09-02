package com.asrevo.cvhome.cua.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.service.SocialLoginConfigService;
import com.asrevo.cvhome.cua.web.dto.ReadableSocialLogin;

import lombok.RequiredArgsConstructor;

/**
 * The social providers a store has switched on, for the storefront's login page to offer. Public: the list is what
 * the old server-rendered page showed to anyone, and it carries no credential.
 */
@RestController
@RequestMapping("/api/v1/public/social-logins")
@RequiredArgsConstructor
public class PublicSocialLoginController {

    private final SocialLoginConfigService service;

    @GetMapping
    public List<ReadableSocialLogin> enabledLogins(StoreMerchantId merchantStore, LanguageCode language) {
        return service.enabledLogins(merchantStore);
    }

}
