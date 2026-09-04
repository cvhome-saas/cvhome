package com.asrevo.cvhome.sso.keys;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.domain.SigningKey;
import com.asrevo.cvhome.sso.domain.SigningKeyStatus;
import com.asrevo.cvhome.sso.dto.KeyStatus;
import com.asrevo.cvhome.sso.dto.SigningKeyDto;
import com.asrevo.cvhome.sso.repo.SigningKeyRepository;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;
import com.asrevo.cvhome.sso.support.FakeCrypto;
import com.nimbusds.jose.jwk.JWKSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Bootstrap, rotation with a retire window, retirement past it, and the unreadable-active-key recovery — over an
 * in-memory repository, because the behaviour is in the transitions, not in JPA.
 */
class KeyRotationServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-03T00:00:00Z");

    private static final int RETIRE_DAYS = 7;

    private static final int ROTATION_DAYS = 90;

    private static final String ADMIN = "admin";

    private final List<SigningKey> rows = new ArrayList<>();

    private final SigningKeyRepository repo = mock(SigningKeyRepository.class);

    private final FakeCrypto crypto = new FakeCrypto((byte) 0x33);

    private final SettingsService settings = mock(SettingsService.class);

    private final RealmSettings realm = mock(RealmSettings.class);

    private final JwkSetCache cache = new JwkSetCache();

    private final KeyRotationService service = new KeyRotationService(repo, new SigningKeyMaterialMapper(crypto), settings,
            mock(AuditService.class), cache, Clock.fixed(NOW, ZoneOffset.UTC));

    @BeforeEach
    void wire() {
        // platform(), not current(): one signing key serves the whole deployment, so its rotation interval
        // cannot come from whichever realm a background thread happened to be in.
        when(settings.platform()).thenReturn(realm);
        when(realm.keys()).thenReturn(new RealmSettings.Keys(ROTATION_DAYS, RETIRE_DAYS));
        when(repo.save(any())).thenAnswer(invocation -> {
            SigningKey key = invocation.getArgument(0);
            rows.removeIf(row -> row.getId().equals(key.getId()));
            rows.add(key);
            return key;
        });
        when(repo.findActiveWithLock()).thenAnswer(i -> byStatus(SigningKeyStatus.ACTIVE));
        when(repo.findByStatus(any())).thenAnswer(i -> byStatus(i.getArgument(0)));
        when(repo.findByStatusIn(any())).thenAnswer(i -> {
            List<SigningKeyStatus> statuses = i.getArgument(0);
            return rows.stream().filter(row -> statuses.contains(row.getStatus())).toList();
        });
        when(repo.findAllByOrderByCreatedAtDesc()).thenAnswer(i -> List.copyOf(rows));
    }

    private List<SigningKey> byStatus(SigningKeyStatus status) {
        return rows.stream().filter(row -> row.getStatus() == status).toList();
    }

    @Test
    void bootstrapsOneActiveKeyWithItsPrivateHalf() {
        JWKSet set = service.currentJwkSet();

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getStatus()).isEqualTo(SigningKeyStatus.ACTIVE);
        assertThat(set.getKeys()).hasSize(1);
        assertThat(set.getKeys().get(0).isPrivate()).isTrue();
        assertThat(service.activeKid()).isEqualTo(rows.get(0).getKid());
    }

    @Test
    void rotationRetiresThePreviousKeyForTheWindowAndKeepsItVerifying() {
        service.currentJwkSet();
        String first = rows.get(0).getKid();

        SigningKeyDto rotated = service.rotate(ADMIN);

        assertThat(rotated.status()).isEqualTo(SigningKeyStatus.ACTIVE);
        assertThat(rotated.kid()).isNotEqualTo(first);
        SigningKey previous = rows.stream().filter(row -> row.getKid().equals(first)).findFirst().orElseThrow();
        assertThat(previous.getStatus()).isEqualTo(SigningKeyStatus.RETIRING);
        assertThat(previous.getRetireAfter()).isEqualTo(NOW.plus(Duration.ofDays(RETIRE_DAYS)));
        JWKSet set = service.currentJwkSet();
        assertThat(set.getKeys()).hasSize(2);
        assertThat(set.getKeyByKeyId(first).isPrivate()).as("retiring key is public-only").isFalse();
        assertThat(set.getKeyByKeyId(rotated.kid()).isPrivate()).isTrue();
        assertThat(service.retireDue()).as("nothing past its window yet").isZero();
    }

    @Test
    void retirementHappensPastTheWindow() {
        service.currentJwkSet();
        service.rotate(ADMIN);
        SigningKey previous = byStatus(SigningKeyStatus.RETIRING).get(0);
        previous.setRetireAfter(NOW.minusSeconds(1));

        assertThat(service.retireDue()).isEqualTo(1);
        assertThat(previous.getStatus()).isEqualTo(SigningKeyStatus.RETIRED);
        assertThat(service.currentJwkSet().getKeys()).hasSize(1);
    }

    @Test
    void anUnreadableActiveKeyIsReplacedNotFatal() {
        service.currentJwkSet();
        String broken = rows.get(0).getKid();
        crypto.rekey((byte) 0x44);

        JWKSet set = service.currentJwkSet();

        assertThat(set.getKeys()).extracting(k -> k.getKeyID()).contains(broken);
        assertThat(set.getKeyByKeyId(broken).isPrivate()).as("still verifies, cannot sign").isFalse();
        assertThat(byStatus(SigningKeyStatus.ACTIVE)).hasSize(1);
        assertThat(byStatus(SigningKeyStatus.ACTIVE).get(0).getKid()).isNotEqualTo(broken);
        KeyStatus status = service.status();
        assertThat(status.unusableCount()).isEqualTo(1);
        assertThat(status.nextRotationAt()).isEqualTo(NOW.plus(Duration.ofDays(ROTATION_DAYS)));
    }

    @Test
    void scheduledRotationWaitsForTheInterval() {
        service.currentJwkSet();

        assertThat(service.rotateIfDue()).isFalse();
        rows.get(0).setActivatedAt(NOW.minus(Duration.ofDays(ROTATION_DAYS + 1)));
        assertThat(service.rotateIfDue()).isTrue();
        assertThat(byStatus(SigningKeyStatus.RETIRING)).hasSize(1);
    }

}
