package com.asrevo.cvhome.sso.domain;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserStatusTest {

    private static final Instant NOW = Instant.parse("2026-09-02T12:00:00Z");

    private static final String HASH = "{bcrypt}x";

    private static User user() {
        User user = new User();
        user.setPasswordHash(HASH);
        user.setActivatedAt(NOW.minusSeconds(3600));
        return user;
    }

    @Test
    void disabledWinsOverEverything() {
        User user = user();
        user.setEnabled(false);
        user.setLockedPermanently(true);

        assertThat(user.status(NOW)).isEqualTo(UserStatus.DISABLED);
    }

    @Test
    void aLockHoldsUntilItExpires() {
        User user = user();
        user.setLockedUntil(NOW.plusSeconds(60));
        assertThat(user.status(NOW)).isEqualTo(UserStatus.LOCKED);

        user.setLockedUntil(NOW.minusSeconds(1));
        assertThat(user.status(NOW)).isEqualTo(UserStatus.ACTIVE);

        user.setLockedPermanently(true);
        assertThat(user.status(NOW)).isEqualTo(UserStatus.LOCKED);
    }

    @Test
    void noPasswordAndNeverActivatedIsPending() {
        User user = new User();

        assertThat(user.status(NOW)).isEqualTo(UserStatus.PENDING);

        user.prePersist();
        assertThat(user.getActivatedAt()).isNull();
        user.setPasswordHash(HASH);
        user.setActivatedAt(NOW);
        assertThat(user.status(NOW)).isEqualTo(UserStatus.ACTIVE);
    }

}
