package com.asrevo.cvhome.cua.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.errors.DuplicateEmailException;
import com.asrevo.cvhome.cua.errors.DuplicateUsernameException;
import com.asrevo.cvhome.cua.service.UserService;
import com.asrevo.cvhome.cua.web.dto.RegistrationRequest;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private static final String FORM_STORE_KEY = "store";

    private static final String FORM_STORE_LOGO_KEY = "storeLogo";

    private static final String FORM_CLIENT_ID_KEY = "clientId";

    private static final String FORM_REGISTRATION_REQUEST_KEY = "registrationRequest";

    private static final String REQUEST_PARAM_CLIENT_ID_KEY = "client_id";

    private static final String REGISTER_PAGE = "register";

    private static final String REGISTRATION_ERROR_CODE = "error.registrationRequest";

    private final RequestCache requestCache;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    private final UserService userService;

    private final StoreLogoResolver storeLogo;

    /** The locale this page is rendering in, so the logo's alt text comes back in the same language. */
    private static LanguageCode language(HttpServletRequest request) {
        return new LanguageCode(RequestContextUtils.getLocale(request).getLanguage());
    }

    @GetMapping("/register")
    public String showRegistrationForm(HttpServletRequest request, HttpServletResponse response, Model model) {
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            String clientId = UriComponentsBuilder.fromUriString(savedRequest.getRedirectUrl())
                    .build()
                    .getQueryParams()
                    .getFirst(REQUEST_PARAM_CLIENT_ID_KEY);
            if (clientId != null) {

                StoreMerchantId storeId = new StoreMerchantId(clientId);
                ReadableMerchantStore store = externalMerchantStoreService.getStore(storeId);

                model.addAttribute(FORM_STORE_KEY, store);
                // The logo is the content service's now, not a field on the merchant record.
                model.addAttribute(FORM_STORE_LOGO_KEY, storeLogo.logoUrl(storeId, language(request)));
                model.addAttribute(FORM_CLIENT_ID_KEY, clientId);
                RegistrationRequest registrationRequest = new RegistrationRequest();
                model.addAttribute(FORM_REGISTRATION_REQUEST_KEY, registrationRequest);

            }
        }
        return REGISTER_PAGE;
    }

    @PostMapping("/register")
    public String registerUser(HttpServletRequest request, HttpServletResponse response,
                               @Valid @ModelAttribute("registrationRequest") RegistrationRequest registrationRequest,
                               BindingResult bindingResult, Model model) {

        StoreMerchantId store = null;
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            String clientId = UriComponentsBuilder.fromUriString(savedRequest.getRedirectUrl())
                    .build()
                    .getQueryParams()
                    .getFirst(REQUEST_PARAM_CLIENT_ID_KEY);
            if (clientId != null) {

                StoreMerchantId storeId = new StoreMerchantId(clientId);
                ReadableMerchantStore merchantStore = externalMerchantStoreService.getStore(storeId);

                model.addAttribute(FORM_STORE_KEY, merchantStore);
                // The logo is the content service's now, not a field on the merchant record.
                model.addAttribute(FORM_STORE_LOGO_KEY, storeLogo.logoUrl(storeId, language(request)));
                model.addAttribute(FORM_CLIENT_ID_KEY, clientId);
                store = storeId;
                model.addAttribute(FORM_REGISTRATION_REQUEST_KEY, registrationRequest);

            }
        }

        if (bindingResult.hasErrors() || store == null) {
            return REGISTER_PAGE;
        }

        try {
            userService.registerUser(store, registrationRequest);
            // Two catches rather than one: each collision belongs under the control the shopper has to change, and
            // the compiler is what says the set is complete.
        } catch (DuplicateUsernameException e) {
            // payload().detail(), not getMessage(): the latter is prefixed with the error code for logs, and the
            // shopper should see the sentence rather than CUA.REGISTRATION.USERNAME_TAKEN.
            bindingResult.rejectValue("username", REGISTRATION_ERROR_CODE, e.payload().detail());
            return REGISTER_PAGE;
        } catch (DuplicateEmailException e) {
            bindingResult.rejectValue("email", REGISTRATION_ERROR_CODE, e.payload().detail());
            return REGISTER_PAGE;
        } catch (Exception e) {
            log.error("Registration failed", e);
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            return REGISTER_PAGE;
        }

        return "redirect:/login";
    }

}
