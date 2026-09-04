package com.asrevo.cvhome.sso.registration;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.password.PasswordService;
import com.asrevo.cvhome.sso.repo.UserRepository;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;
import com.asrevo.cvhome.uaa.errors.EmailTakenException;
import com.asrevo.cvhome.uaa.errors.SelfRegistrationDisabledException;
import com.asrevo.cvhome.uaa.errors.UsernameTakenException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Someone signing themselves up: allowed only where the realm says so, and never over an existing account. */
class SelfRegistrationServiceTest {

    private static final RegistrationRequest REQUEST =
            new RegistrationRequest("ahmed", "ahmed@example.test", "Ahmed", "Nour", "Str0ng-Passphrase!");

    private final UserRepository users = mock(UserRepository.class);

    private final PasswordService passwords = mock(PasswordService.class);

    private final SettingsService settings = mock(SettingsService.class);

    private final AuditService audit = mock(AuditService.class);

    private final SelfRegistrationService service =
            new SelfRegistrationService(users, passwords, settings, audit);

    @Test
    void createsTheAccountWhenTheRealmAllowsIt() throws Exception {
        allowRegistration(true);

        service.register(REQUEST);

        verify(users).save(any(User.class));
    }

    /** The platform realm keeps this off; a merchant may turn it off for their store without affecting others. */
    @Test
    void refusesWhenTheRealmHasRegistrationOff() {
        allowRegistration(false);

        assertThatThrownBy(() -> service.register(REQUEST))
                .isInstanceOf(SelfRegistrationDisabledException.class);

        verify(users, never()).save(any(User.class));
    }

    @Test
    void refusesAUsernameAlreadyUsedInThisRealm() {
        allowRegistration(true);
        when(users.existsByUsernameIgnoreCase(anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.register(REQUEST)).isInstanceOf(UsernameTakenException.class);
    }

    @Test
    void refusesAnEmailAlreadyUsedInThisRealm() {
        allowRegistration(true);
        when(users.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.register(REQUEST)).isInstanceOf(EmailTakenException.class);
    }

    /**
     * The password must go through PasswordService rather than a bare encoder: that is what applies the realm's
     * policy, its history and its breach check. Shoppers previously got none of them.
     */
    @Test
    void putsThePasswordThroughTheRealmsPolicy() throws Exception {
        allowRegistration(true);

        service.register(REQUEST);

        verify(passwords).setPassword(any(User.class), anyString());
    }

    private void allowRegistration(boolean allowed) {
        RealmSettings s = mock(RealmSettings.class);
        when(s.selfRegistrationEnabled()).thenReturn(allowed);
        when(settings.current()).thenReturn(s);
    }

}
