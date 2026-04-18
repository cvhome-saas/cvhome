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
import org.springframework.web.util.UriComponentsBuilder;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
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

    private static final String FORM_CLIENT_ID_KEY = "clientId";

    private static final String FORM_REGISTRATION_REQUEST_KEY = "registrationRequest";

    private static final String REQUEST_PARAM_CLIENT_ID_KEY = "client_id";

    private static final String REGISTER_PAGE = "register";

    private final RequestCache requestCache;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    private final UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(HttpServletRequest request, HttpServletResponse response, Model model) {
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            String clientId = UriComponentsBuilder.fromUriString(savedRequest.getRedirectUrl())
                    .build()
                    .getQueryParams()
                    .getFirst(REQUEST_PARAM_CLIENT_ID_KEY);
            if (clientId != null) {

                ReadableMerchantStore store = externalMerchantStoreService.getStore(new StoreMerchantId(clientId));

                model.addAttribute(FORM_STORE_KEY, store);
                model.addAttribute(FORM_CLIENT_ID_KEY, clientId);
                RegistrationRequest registrationRequest = new RegistrationRequest();
                registrationRequest.setClientId(clientId);
                model.addAttribute(FORM_REGISTRATION_REQUEST_KEY, registrationRequest);

            }
        }
        return REGISTER_PAGE;
    }

    @PostMapping("/register")
    public String registerUser(HttpServletRequest request, HttpServletResponse response,
                               @Valid @ModelAttribute("registrationRequest") RegistrationRequest registrationRequest,
                               BindingResult bindingResult, Model model) {

        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            String clientId = UriComponentsBuilder.fromUriString(savedRequest.getRedirectUrl())
                    .build()
                    .getQueryParams()
                    .getFirst(REQUEST_PARAM_CLIENT_ID_KEY);
            if (clientId != null) {

                ReadableMerchantStore store = externalMerchantStoreService.getStore(new StoreMerchantId(clientId));

                model.addAttribute(FORM_STORE_KEY, store);
                model.addAttribute(FORM_CLIENT_ID_KEY, clientId);
                registrationRequest.setClientId(clientId);
                model.addAttribute(FORM_REGISTRATION_REQUEST_KEY, registrationRequest);

            }
        }

        if (bindingResult.hasErrors()) {
            return REGISTER_PAGE;
        }

        try {
            userService.registerUser(registrationRequest);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("username", "error.registrationRequest", e.getMessage());
            return REGISTER_PAGE;
        } catch (Exception e) {
            log.error("Registration failed", e);
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            return REGISTER_PAGE;
        }

        return "redirect:/login";
    }

}
