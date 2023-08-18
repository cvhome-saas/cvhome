package com.asrevo.cvhome.user.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("api/v1/auth")
@Slf4j
@AllArgsConstructor
public class AuthController {
    @GetMapping("current")
    public Principal current(@AuthenticationPrincipal Principal principal) {
        return principal;
    }
}
