package com.asrevo.cvhome.sso.settings;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.uaa.errors.SettingsConflictException;
import com.asrevo.cvhome.uaa.errors.SettingsInvalidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Bounds are checked on the way in, the cache answers reads, and a stale version is refused.
 */
class SettingsServiceTest {

    private static final String ACTOR = "super-admin";

    private final SettingsRepository repository = mock(SettingsRepository.class);

    private final SettingsService service = new SettingsService(repository, mock(AuditService.class));

    private Settings stored;

    @BeforeEach
    void setUp() {
        stored = new Settings();
        when(repository.findById(Settings.SINGLETON_ID)).thenReturn(Optional.of(stored));
        when(repository.saveAndFlush(any(Settings.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static RealmSettings with(RealmSettings base, RealmSettings.Lockout lockout, long version) {
        return new RealmSettings(base.displayName(), base.supportEmail(), base.defaultLocale(),
                base.selfRegistrationEnabled(), base.requireEmailVerification(), base.password(), lockout,
                base.sessions(), base.tokens(), base.keys(), base.auditRetentionDays(), base.updatedAt(),
                base.updatedBy(), version);
    }

    @Test
    void readsAreCachedUntilAWrite() throws Exception {
        RealmSettings first = service.current();
        service.current();
        verify(repository, times(1)).findById(Settings.SINGLETON_ID);

        service.update(with(first, new RealmSettings.Lockout(3, 600, 0), first.version()), ACTOR);

        assertThat(service.current().lockout().threshold()).isEqualTo(3);
        assertThat(stored.getUpdatedBy()).isEqualTo(ACTOR);
    }

    @Test
    void aZeroLockoutThresholdIsRefused() {
        RealmSettings base = service.current();

        assertThatThrownBy(() -> service.update(with(base, new RealmSettings.Lockout(0, 600, 0), base.version()), ACTOR))
                .isInstanceOf(SettingsInvalidException.class);
    }

    @Test
    void aStaleVersionIsRefused() {
        RealmSettings base = service.current();

        assertThatThrownBy(() -> service.update(with(base, base.lockout(), base.version() + 1), ACTOR))
                .isInstanceOf(SettingsConflictException.class);
    }

    @Test
    void defaultsMatchTheSchema() {
        RealmSettings s = service.current();

        assertThat(s.password().minLength()).isEqualTo(12);
        assertThat(s.lockout().threshold()).isEqualTo(5);
        assertThat(s.sessions().idleSeconds()).isEqualTo(1800);
        assertThat(s.tokens().defaultAccessTokenTtlSeconds()).isEqualTo(900);
        assertThat(s.keys().rotationDays()).isEqualTo(90);
        assertThat(s.auditRetentionDays()).isEqualTo(365);
    }

}
