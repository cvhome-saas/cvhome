package com.asrevo.cvhome.sso.settings;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.realm.RealmContext;
import com.asrevo.cvhome.sso.realm.RealmMode;
import com.asrevo.cvhome.sso.realm.SsoRealmProperties;
import com.asrevo.cvhome.sso.realm.SsoTenantIdentifierResolver;
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

    private static final String REALM = "platform";

    private static final String STORE_A = "store-a";

    private static final String STORE_B = "store-b";

    /** The defaults; every case that cares about a ceiling states the one it is testing. */
    private static final SsoPlatformCeilings CEILINGS = new SsoPlatformCeilings(0, 0, 0, 0, 0, 0, 0, 0);

    private final SettingsRepository repository = mock(SettingsRepository.class);

    private final SettingsService service =
            new SettingsService(repository, mock(AuditService.class), new SsoTenantIdentifierResolver(single()),
                    CEILINGS);

    private Settings stored;

    @BeforeEach
    void setUp() {
        stored = new Settings(REALM);
        when(repository.findById(REALM)).thenReturn(Optional.of(stored));
        when(repository.saveAndFlush(any(Settings.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    /**
     * The leak the realm key exists to stop: with a constant key, whichever store loaded first imposed its
     * password policy, lockout thresholds and token lifetimes on every other store for the next thirty seconds.
     */
    @Test
    void oneRealmsPolicyIsNeverServedToAnother() {
        SsoRealmProperties multi = new SsoRealmProperties();
        multi.setMode(RealmMode.MULTI);
        SettingsService shared =
                new SettingsService(repository, mock(AuditService.class), new SsoTenantIdentifierResolver(multi),
                        CEILINGS);
        when(repository.findById(STORE_A)).thenReturn(Optional.of(new Settings(STORE_A)));
        when(repository.findById(STORE_B)).thenReturn(Optional.of(new Settings(STORE_B)));

        RealmContext.runIn(RealmId.of(STORE_A), shared::current);
        RealmContext.runIn(RealmId.of(STORE_B), shared::current);

        // Each realm loaded its own row rather than the other's cached one.
        verify(repository).findById(STORE_A);
        verify(repository).findById(STORE_B);
    }

    private static SsoRealmProperties single() {
        SsoRealmProperties properties = new SsoRealmProperties();
        properties.setMode(RealmMode.SINGLE);
        return properties;
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
        verify(repository, times(1)).findById(REALM);

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

    /**
     * A merchant edits their own realm, but the pod underneath is shared. Turning lockout off by setting a
     * threshold nobody reaches, or minting a token that outlives the store, is refused above the realm.
     */
    @Test
    void aRealmCannotSetAPolicyPastThePlatformsLimits() {
        RealmSettings base = service.current();

        assertThatThrownBy(() -> service.update(with(base, new RealmSettings.Lockout(1_000_000, 900, 5),
                base.version()), ACTOR))
                .isInstanceOf(SettingsInvalidException.class)
                .hasMessageContaining("lockout.threshold");
        assertThatThrownBy(() -> service.update(withTokens(base, new RealmSettings.Tokens(3600, 3600,
                Integer.MAX_VALUE, 365, 24)), ACTOR))
                .isInstanceOf(SettingsInvalidException.class)
                .hasMessageContaining("tokens.defaultRefreshTokenTtlSeconds");
    }

    /** And a policy inside them still goes through, so the ceiling is a ceiling and not a wall. */
    @Test
    void aPolicyWithinThemIsAccepted() throws Exception {
        RealmSettings base = service.current();

        RealmSettings after = service.update(with(base, new RealmSettings.Lockout(20, 60, 5), base.version()), ACTOR);

        assertThat(after.lockout().threshold()).isEqualTo(20);
    }

    private static RealmSettings withTokens(RealmSettings base, RealmSettings.Tokens tokens) {
        return new RealmSettings(base.displayName(), base.supportEmail(), base.defaultLocale(),
                base.selfRegistrationEnabled(), base.requireEmailVerification(), base.password(), base.lockout(),
                base.sessions(), tokens, base.keys(), base.auditRetentionDays(), base.updatedAt(),
                base.updatedBy(), base.version());
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
