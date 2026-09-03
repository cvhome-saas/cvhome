package com.asrevo.cvhome.uaa.web.admin;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.uaa.dto.KeyStatus;
import com.asrevo.cvhome.uaa.dto.SigningKeyDto;
import com.asrevo.cvhome.uaa.keys.KeyRotationService;

import lombok.RequiredArgsConstructor;

/** The signing keys: what exists, what signs, and the one action — rotate now. Never the key material. */
@RestController
@RequestMapping("/api/v1/admin/keys")
@RequiredArgsConstructor
public class AdminKeyController {

    private static final String ADMIN = "hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')";

    private final KeyRotationService keys;

    @PreAuthorize(ADMIN)
    @GetMapping
    public List<SigningKeyDto> list() {
        return keys.list();
    }

    @PreAuthorize(ADMIN)
    @GetMapping("/status")
    public KeyStatus status() {
        return keys.status();
    }

    @PreAuthorize(ADMIN)
    @PostMapping("/rotate")
    public SigningKeyDto rotate(Authentication authentication) {
        return keys.rotate(authentication == null ? "unknown" : authentication.getName());
    }

}
