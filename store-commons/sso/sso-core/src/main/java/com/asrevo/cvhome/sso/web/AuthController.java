package com.asrevo.cvhome.sso.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.sso.dto.MeResponse;
import com.asrevo.cvhome.sso.security.CurrentUserResolver;
import com.asrevo.cvhome.uaa.errors.NotAUserPrincipalException;

import lombok.RequiredArgsConstructor;

/**
 * Who is signed in. Any authenticated caller; a service client gets a 403 rather than a description of nobody.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CurrentUserResolver currentUser;

    @GetMapping("me")
    public MeResponse me(Authentication authentication) throws NotAUserPrincipalException {
        return currentUser.describe(authentication);
    }

}
