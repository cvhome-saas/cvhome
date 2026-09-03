package com.asrevo.cvhome.uaa.web.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.uaa.errors.SettingsConflictException;
import com.asrevo.cvhome.uaa.errors.SettingsInvalidException;
import com.asrevo.cvhome.uaa.settings.RealmSettings;
import com.asrevo.cvhome.uaa.settings.SettingsService;

import lombok.RequiredArgsConstructor;

/**
 * The realm's policy: one document, read whole, written whole with its version.
 */
@RestController
@RequestMapping("/api/v1/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final SettingsService settings;

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @GetMapping
    public RealmSettings get() {
        return settings.current();
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @PutMapping
    public RealmSettings update(@RequestBody RealmSettings requested, Authentication authentication)
            throws SettingsInvalidException, SettingsConflictException {
        return settings.update(requested, authentication.getName());
    }

}
