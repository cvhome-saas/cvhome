package com.asrevo.cvhome.sso.audit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * One realm's audit trim.
 *
 * <p>
 * The cut-off comes from the realm's own retention setting rather than a constant, because each realm decides how
 * long its own history lives — and the clock is injected, so a trim never depends on when the test happens to run.
 * A wrong cut-off here deletes audit history, which is the one table nothing can reconstruct.
 * </p>
 */
class AuditRetentionTest {

    private static final Instant NOW = Instant.parse("2026-08-01T00:00:00Z");
    private static final RealmId REALM = RealmId.of("store-1");

    private final AuditEventRepository repository = mock(AuditEventRepository.class);
    private final SettingsService settings = mock(SettingsService.class);
    private final AuditRetention retention =
            new AuditRetention(repository, settings, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void thecutOffIsTheRealmsOwnRetentionSettingCountedBackFromNow() {
        givenRetentionDays(90);

        retention.trim(REALM);

        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        verify(repository).deleteOlderThan(captor.capture());
        assertThat(captor.getValue()).isEqualTo(NOW.minus(Duration.ofDays(90)));
    }

    @Test
    void arealmThatKeepsLessHistoryTrimsMore() {
        givenRetentionDays(7);

        retention.trim(REALM);

        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        verify(repository).deleteOlderThan(captor.capture());
        assertThat(captor.getValue()).isEqualTo(NOW.minus(Duration.ofDays(7)));
    }

    @Test
    void arealmWithNothingToTrimStillCompletes() {
        givenRetentionDays(90);
        when(repository.deleteOlderThan(org.mockito.ArgumentMatchers.any())).thenReturn(0);

        retention.trim(REALM);

        verify(repository).deleteOlderThan(org.mockito.ArgumentMatchers.any());
    }

    private void givenRetentionDays(int days) {
        RealmSettings realm = mock(RealmSettings.class);
        when(realm.auditRetentionDays()).thenReturn(days);
        when(settings.current()).thenReturn(realm);
        when(repository.deleteOlderThan(org.mockito.ArgumentMatchers.any())).thenReturn(3);
    }

}
