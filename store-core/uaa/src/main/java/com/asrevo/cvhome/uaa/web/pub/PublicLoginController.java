package com.asrevo.cvhome.uaa.web.pub;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.uaa.settings.RealmSettings;
import com.asrevo.cvhome.uaa.settings.SettingsService;

import lombok.RequiredArgsConstructor;

/**
 * What the sign-in page needs before anyone is signed in. Public and stateless; carries nothing a stranger should
 * not see — the display name, whether remember-me is offered, and the lockout policy the page explains.
 */
@RestController
@RequestMapping("/api/v1/public/login")
@RequiredArgsConstructor
public class PublicLoginController {

    private final SettingsService settings;

    @GetMapping("settings")
    public LoginSettings settings() {
        RealmSettings current = settings.current();
        return new LoginSettings(current.displayName(), current.defaultLocale(), current.sessions().rememberMeEnabled(),
                current.lockout().threshold(), current.lockout().durationSeconds() / 60);
    }

    public record LoginSettings(String displayName, String defaultLocale, boolean rememberMeEnabled,
                                int lockoutThreshold, int lockoutMinutes) {
    }

}
