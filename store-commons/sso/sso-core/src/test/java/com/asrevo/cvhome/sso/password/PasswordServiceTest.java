package com.asrevo.cvhome.sso.password;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;
import com.asrevo.cvhome.uaa.errors.PasswordReusedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * History and expiry: the two rules that need the clock and the previous hashes.
 */
class PasswordServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-02T12:00:00Z");

    private static final String HASH_PREFIX = "{h}";

    private static final String CONCAT = "%s%s";

    private static final String FIRST = "first-one-9";

    private static final String SECOND = "second-one-9";

    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    private final PasswordHistoryRepository history = mock(PasswordHistoryRepository.class);

    private final CompromisedPasswordGate breached = mock(CompromisedPasswordGate.class);

    private final SettingsService settings = mock(SettingsService.class);

    private final PasswordService service = new PasswordService(encoder, history, new PasswordPolicyValidator(), breached,
            settings, Clock.fixed(NOW, ZoneOffset.UTC));

    private final List<PasswordHistory> stored = new ArrayList<>();

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("someone");
        user.setEmail("someone@example.com");
        when(encoder.encode(anyString())).thenAnswer(i -> String.format(CONCAT, HASH_PREFIX, i.getArgument(0)));
        when(encoder.matches(anyString(), anyString()))
                .thenAnswer(i -> String.format(CONCAT, HASH_PREFIX, i.getArgument(0)).equals(i.getArgument(1)));
        when(history.findByUserIdOrderByCreatedAtDesc(user.getId())).thenAnswer(i -> new ArrayList<>(stored));
        when(history.save(any(PasswordHistory.class))).thenAnswer(i -> {
            stored.addFirst(i.getArgument(0));
            return i.getArgument(0);
        });
        RealmSettings realm = mock(RealmSettings.class);
        when(realm.password()).thenReturn(new RealmSettings.PasswordPolicy(8, false, false, false, false, 2, 30, false));
        when(settings.current()).thenReturn(realm);
    }

    @Test
    void aRecentPasswordCannotComeBack() throws Exception {
        service.setPassword(user, FIRST);
        service.setPassword(user, SECOND);

        assertThatThrownBy(() -> service.setPassword(user, FIRST)).isInstanceOf(PasswordReusedException.class);
        assertThatThrownBy(() -> service.setPassword(user, SECOND)).isInstanceOf(PasswordReusedException.class);
        assertThat(user.getPasswordChangedAt()).isEqualTo(NOW);
        assertThat(user.getActivatedAt()).isEqualTo(NOW);
        verify(breached, never()).check(anyString());
    }

    @Test
    void expiryFollowsTheChangeDate() {
        user.setPasswordChangedAt(NOW.minusSeconds(31L * 24 * 3600));
        assertThat(service.expired(user)).isTrue();

        user.setPasswordChangedAt(NOW.minusSeconds(29L * 24 * 3600));
        assertThat(service.expired(user)).isFalse();
    }

}
