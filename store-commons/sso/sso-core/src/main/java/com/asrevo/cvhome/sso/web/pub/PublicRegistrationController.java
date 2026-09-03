package com.asrevo.cvhome.sso.web.pub;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.sso.registration.RegistrationRequest;
import com.asrevo.cvhome.sso.registration.SelfRegistrationService;
import com.asrevo.cvhome.uaa.errors.EmailTakenException;
import com.asrevo.cvhome.uaa.errors.PasswordCompromisedException;
import com.asrevo.cvhome.uaa.errors.PasswordPolicyViolationException;
import com.asrevo.cvhome.uaa.errors.PasswordReusedException;
import com.asrevo.cvhome.uaa.errors.SelfRegistrationDisabledException;
import com.asrevo.cvhome.uaa.errors.UsernameTakenException;

import lombok.RequiredArgsConstructor;

/**
 * Where someone signs themselves up. Public, stateless and rate limited, on the chain that never touches the
 * session — a registration must not become the request a later login resumes.
 *
 * <p>
 * There is no store or realm in the signature. The realm is the one the request arrived in, and on cua that is
 * decided by the host the shopper is on; a body that could name it would let one form create accounts in any
 * store the deployment serves.
 * </p>
 *
 * <p>
 * Success is an empty {@code 201} and the storefront then starts the normal login flow, which is the contract
 * cua already had.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/public/registration")
@RequiredArgsConstructor
public class PublicRegistrationController {

    private final SelfRegistrationService registrations;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegistrationRequest request)
            throws SelfRegistrationDisabledException, UsernameTakenException, EmailTakenException,
            PasswordPolicyViolationException, PasswordReusedException, PasswordCompromisedException {
        registrations.register(request);
    }

}
