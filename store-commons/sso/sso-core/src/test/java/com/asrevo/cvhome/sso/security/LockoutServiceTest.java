package com.asrevo.cvhome.sso.security;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.repo.UserRepository;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LockoutServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-02T12:00:00Z");

    private static final String USERNAME = "someone";

    private static final String IP = "10.0.0.1";

    private final UserRepository users = mock(UserRepository.class);

    private final SettingsService settings = mock(SettingsService.class);

    private final LockoutService service = new LockoutService(users, settings, mock(AuditService.class),
            Clock.fixed(NOW, ZoneOffset.UTC));

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(USERNAME);
        when(users.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        RealmSettings realm = mock(RealmSettings.class);
        when(realm.lockout()).thenReturn(new RealmSettings.Lockout(3, 600, 2));
        when(settings.current()).thenReturn(realm);
    }

    @Test
    void locksAtTheThresholdAndPermanentlyAfterEnoughLockouts() {
        assertThat(service.failed(USERNAME).attemptsLeft()).isEqualTo(2);
        assertThat(service.failed(USERNAME).attemptsLeft()).isEqualTo(1);

        LockoutService.Outcome third = service.failed(USERNAME);

        assertThat(third.locked()).isTrue();
        assertThat(third.permanent()).isFalse();
        assertThat(user.getLockedUntil()).isEqualTo(NOW.plusSeconds(600));
        assertThat(user.isLocked(NOW)).isTrue();
        assertThat(user.isLocked(NOW.plusSeconds(601))).isFalse();
        assertThat(user.getFailedLoginAttempts()).isZero();

        service.failed(USERNAME);
        service.failed(USERNAME);
        LockoutService.Outcome sixth = service.failed(USERNAME);
        assertThat(sixth.permanent()).isTrue();
        assertThat(user.isLocked(NOW.plusSeconds(100_000))).isTrue();
    }

    @Test
    void successResetsTheCounterAndStampsTheSignIn() {
        service.failed(USERNAME);

        service.succeeded(USERNAME, IP, LockoutService.VIA_PASSWORD);

        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLastSignInAt()).isEqualTo(NOW);
        assertThat(user.getLastSignInIp()).isEqualTo(IP);
        assertThat(service.attemptsLeft(USERNAME)).isEqualTo(3);
    }

    @Test
    void unlockClearsEverything() {
        service.failed(USERNAME);
        service.failed(USERNAME);
        service.failed(USERNAME);

        service.unlock(user);

        assertThat(user.isLocked(NOW)).isFalse();
        assertThat(user.getLockoutCount()).isZero();
        assertThat(service.failed("nobody")).isEqualTo(LockoutService.Outcome.UNKNOWN_USER);
    }

}
