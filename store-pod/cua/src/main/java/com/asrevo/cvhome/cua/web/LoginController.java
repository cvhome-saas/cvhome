package com.asrevo.cvhome.cua.web;

import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.UriComponentsBuilder;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.domain.SocialLoginConfigId;
import com.asrevo.cvhome.cua.repo.SocialLoginConfigRepository;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final RequestCache requestCache;

    private final SocialLoginConfigRepository socialLoginConfigRepository;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    private final StoreLogoResolver storeLogo;

    @GetMapping("/login")
    public String login(HttpServletRequest request, Locale locale, HttpServletResponse response, Model model) {
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            String clientId = UriComponentsBuilder.fromUriString(savedRequest.getRedirectUrl())
                    .build()
                    .getQueryParams()
                    .getFirst("client_id");
            if (clientId != null) {

                StoreMerchantId storeId = new StoreMerchantId(clientId);
                ReadableMerchantStore store = externalMerchantStoreService.getStore(storeId);
                model.addAttribute("store", store);
                // The logo is the content service's now, not a field on the merchant record.
                model.addAttribute("storeLogo",
                        storeLogo.logoUrl(storeId, new LanguageCode(locale.getLanguage())));
                model.addAttribute("clientId", clientId);
                List<SocialLoginConfigId> configs = socialLoginConfigRepository
                        .findEnabledSocialLoginConfig(storeId);
                model.addAttribute("socialLogins", configs);
            }
        }
        return "login";
    }

}