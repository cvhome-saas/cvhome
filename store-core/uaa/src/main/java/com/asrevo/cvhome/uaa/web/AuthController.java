package com.asrevo.cvhome.uaa.web;

import java.util.Objects;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @GetMapping("me")
    public Object me() {
        return Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }

}
