package com.asrevo.cvhome.tenancy.manager.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.tenancy.errors.DuplicateSignupEmailException;
import com.asrevo.cvhome.tenancy.manager.dto.CreateOrgRequest;
import com.asrevo.cvhome.tenancy.manager.service.SignupService;
import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Public signup: creates an organization and its first administrator.
 *
 * <p>
 * Moved off {@code api/v1/user-account}, which it shared with {@code UserAccountApi}. Two controllers on one base
 * path is legal and confusing: the shared prefix implied a shared audience, when in fact everything on the other
 * one requires a session and a store-scoped permission while this is the one endpoint on the service that anyone
 * on the internet may call.
 * </p>
 *
 * <p>
 * Which is also why {@code @Valid} matters more here than anywhere else on the service. Without it this endpoint
 * accepted empty names, {@code not-an-email} as an address, a one-character password and a mismatched
 * confirmation — 200, tenant created — and the console's form was the entire gate. The rules it enforces now are
 * on {@code SignUpUser}; the form still applies them so it can say so without a round trip, but it is no longer
 * the only thing that does.
 * </p>
 */
@RestController
@RequestMapping("api/v1/signup")
@AllArgsConstructor
@Slf4j
public class SignUpApi {

    private final SignupService signupService;

    @PostMapping("public/create")
    public ReadableUser create(@Valid @RequestBody CreateOrgRequest request)
            throws DuplicateSignupEmailException, UaaApiUnavailableException {
        return signupService.createOrgUser(request);
    }

}
