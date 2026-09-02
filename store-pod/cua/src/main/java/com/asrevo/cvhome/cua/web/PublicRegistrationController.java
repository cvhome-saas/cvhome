package com.asrevo.cvhome.cua.web;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.errors.DuplicateEmailException;
import com.asrevo.cvhome.cua.errors.DuplicateUsernameException;
import com.asrevo.cvhome.cua.service.UserService;
import com.asrevo.cvhome.cua.web.dto.RegistrationRequest;

import lombok.RequiredArgsConstructor;

/**
 * Shopper self-registration, as JSON for the storefront's own registration page.
 *
 * <p>
 * Public and stateless: it needs no authorize flow behind it, only the store — which comes from {@code ?store=}
 * like every other endpoint, and is what scopes the uniqueness of the username and email. Success is an empty
 * {@code 201}; the storefront then starts the normal login flow. A collision surfaces as the typed conflict the
 * storefront already knows how to name.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/public/registration")
@RequiredArgsConstructor
public class PublicRegistrationController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegistrationRequest request, StoreMerchantId merchantStore,
                         LanguageCode language) throws DuplicateUsernameException, DuplicateEmailException {
        userService.registerUser(merchantStore, request);
    }

}
