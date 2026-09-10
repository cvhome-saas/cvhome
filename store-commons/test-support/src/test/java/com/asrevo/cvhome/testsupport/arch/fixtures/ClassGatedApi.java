package com.asrevo.cvhome.testsupport.arch.fixtures;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** The gate on the class, no class-level mapping — sso's {@code AccountController} shape. */
@RestController
@PreAuthorize("isAuthenticated()")
public class ClassGatedApi {

    @GetMapping("/account")
    public String account() {
        return "account";
    }

}
