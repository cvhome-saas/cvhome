package com.asrevo.cvhome.sso.password;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * One previous hash an account may not reuse.
 *
 * <p>
 * The {@code @PrePersist} fills in an id and a stamp only when they are absent, so a row written by the password
 * service — which sets them itself, and orders the history by that stamp — keeps its own values. Overwriting them
 * would reorder the history and let the oldest hash be mistaken for the newest.
 * </p>
 */
class PasswordHistoryTest {

    private static final String HASH = "{bcrypt}$2a$10$abcdefghijklmnopqrstuv";
    private static final String PLATFORM = "platform";

    @Test
    void arowWrittenWithoutAnIdOrAstampGetsBothOnInsert() {
        PasswordHistory history = new PasswordHistory();
        history.setUserId(UUID.randomUUID());
        history.setPasswordHash(HASH);

        history.prePersist();

        assertThat(history.getId()).isNotNull();
        assertThat(history.getCreatedAt()).isNotNull();
    }

    @Test
    void arowThatBroughtItsOwnIdAndStampKeepsThem() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        PasswordHistory history = new PasswordHistory();
        history.setId(id);
        history.setUserId(UUID.randomUUID());
        history.setPasswordHash(HASH);
        history.setCreatedAt(createdAt);

        history.prePersist();

        // The history is ordered by this stamp; overwriting it would make the oldest hash look like the newest.
        assertThat(history.getId()).isEqualTo(id);
        assertThat(history.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void therowCarriesTheAccountItBelongsToAndTheHashItRetires() {
        UUID userId = UUID.randomUUID();
        PasswordHistory history = new PasswordHistory();
        history.setUserId(userId);
        history.setPasswordHash(HASH);
        history.setRealmId(PLATFORM);

        assertThat(history.getUserId()).isEqualTo(userId);
        assertThat(history.getPasswordHash()).isEqualTo(HASH);
        assertThat(history.getRealmId()).isEqualTo(PLATFORM);
    }

}
