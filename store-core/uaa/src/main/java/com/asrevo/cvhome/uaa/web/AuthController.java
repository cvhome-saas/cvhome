package com.asrevo.cvhome.uaa.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.uaa.dto.MeResponse;
import com.asrevo.cvhome.uaa.errors.NotAUserPrincipalException;
import com.asrevo.cvhome.uaa.security.CurrentUserResolver;

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
