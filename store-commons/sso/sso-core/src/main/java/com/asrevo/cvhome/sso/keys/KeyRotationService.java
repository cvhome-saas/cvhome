package com.asrevo.cvhome.sso.keys;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.errors.UncheckedBaseException;
import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.audit.AuditTargetType;
import com.asrevo.cvhome.sso.domain.SigningKey;
import com.asrevo.cvhome.sso.domain.SigningKeyStatus;
import com.asrevo.cvhome.sso.dto.KeyStatus;
import com.asrevo.cvhome.sso.dto.SigningKeyDto;
import com.asrevo.cvhome.sso.repo.SigningKeyRepository;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;
import com.asrevo.cvhome.uaa.errors.NoActiveSigningKeyException;
import com.asrevo.cvhome.uaa.errors.SigningKeyUnusableException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The signing keys' life: one ACTIVE key signs, rotated-out keys verify for the realm's retire window, then leave.
 *
 * <p>
 * <strong>Rotation never breaks an in-flight token.</strong> The old key goes to RETIRING and stays in the JWKS (public
 * half only) until {@code retireAfter}, so a resource server that fetched the set an hour ago still verifies, and one
 * that meets an unknown {@code kid} refetches and finds the new key.
 * </p>
 *
 * <p>
 * <strong>A key that cannot be read back is not a 500.</strong> If the crypto provider's key changed underneath a
 * stored row, its private half is unreadable: the key is excluded from signing, logged, and — when it was the active
 * one — replaced, so the realm keeps minting. Its public half still verifies what it signed.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeyRotationService {

    static final String RSA = "RSA";

    static final int RSA_BITS = 2048;

    private final SigningKeyRepository keys;

    private final SigningKeyMaterialMapper mapper;

    private final SettingsService settings;

    private final AuditService audit;

    private final JwkSetCache cache;

    private final Clock clock;

    // ---------------------------------------------------------------- the set the tokens use

    /**
     * The JWK set: the active key whole, every retiring key public-only. Bootstraps a key when none is active and
     * replaces one whose private half cannot be opened, in the same transaction.
     */
    @Transactional
    public JWKSet currentJwkSet() {
        SigningKey active = ensureActive();
        List<JWK> set = new ArrayList<>();
        try {
            set.add(mapper.toPrivateJwk(active));
        } catch (SigningKeyUnusableException e) {
            throw new UncheckedBaseException(e);
        }
        for (SigningKey retiring : keys.findByStatus(SigningKeyStatus.RETIRING)) {
            try {
                set.add(mapper.toPublicJwk(retiring));
            } catch (SigningKeyUnusableException e) {
                log.error("Retiring signing key {} is unreadable and left out of the JWKS", retiring.getKid(), e);
            }
        }
        return new JWKSet(set);
    }

    /** The {@code kid} tokens are signed under right now. */
    @Transactional
    public String activeKid() {
        return ensureActive().getKid();
    }

    /**
     * The active key, made when missing, replaced when unreadable. Locked so two threads bootstrapping at once cannot
     * both create one.
     */
    private SigningKey ensureActive() {
        List<SigningKey> active = keys.findActiveWithLock();
        for (SigningKey key : active) {
            if (readable(key)) {
                return key;
            }
            log.error("Active signing key {} cannot be decrypted; retiring it and generating a replacement", key.getKid());
            key.retiring(clock.instant().plus(settings.current().keys().retireDays(), ChronoUnit.DAYS));
            keys.save(key);
            cache.invalidate();
        }
        return generateActive();
    }

    private boolean readable(SigningKey key) {
        try {
            mapper.toPrivateJwk(key);
            return true;
        } catch (SigningKeyUnusableException e) {
            return false;
        }
    }

    private SigningKey generateActive() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA);
            generator.initialize(RSA_BITS);
            KeyPair pair = generator.generateKeyPair();
            RSAKey rsa = new RSAKey.Builder((RSAPublicKey) pair.getPublic()).privateKey(pair.getPrivate())
                    .keyUse(KeyUse.SIGNATURE).algorithm(JWSAlgorithm.RS256).keyIDFromThumbprint().build();
            SigningKey created = keys.save(mapper.toActiveEntity(rsa, clock.instant()));
            cache.invalidate();
            log.info("Signing key {} generated and activated", created.getKid());
            return created;
        } catch (NoSuchAlgorithmException | com.nimbusds.jose.JOSEException e) {
            throw new UncheckedBaseException(NoActiveSigningKeyException.create(e));
        }
    }

    // ---------------------------------------------------------------- administration

    /** A new active key; the previous one verifies for the realm's retire window and then leaves. */
    @Transactional
    public SigningKeyDto rotate(String actor) {
        Instant now = clock.instant();
        int retireDays = settings.current().keys().retireDays();
        List<SigningKey> previous = keys.findActiveWithLock();
        for (SigningKey key : previous) {
            key.retiring(now.plus(retireDays, ChronoUnit.DAYS));
            keys.save(key);
        }
        SigningKey created = generateActive();
        cache.invalidate();
        String replaced = previous.stream().map(SigningKey::getKid).findFirst().orElse("none");
        audit.record(AuditRecord.of(AuditEventType.KEY_ROTATED).target(AuditTargetType.KEY, created.getId().toString(),
                created.getKid()).detail(String.format("replaces %s, which verifies until %s", replaced,
                previous.isEmpty() ? "-" : previous.get(0).getRetireAfter())));
        log.info("Signing key rotated by {}: {} replaces {}", actor, created.getKid(), replaced);
        return toDto(created);
    }

    /** Every RETIRING key past its window becomes RETIRED and leaves the JWKS. */
    @Transactional
    public int retireDue() {
        Instant now = clock.instant();
        int retired = 0;
        for (SigningKey key : keys.findByStatus(SigningKeyStatus.RETIRING)) {
            if (key.getRetireAfter() != null && !key.getRetireAfter().isAfter(now)) {
                key.retire(now);
                keys.save(key);
                audit.record(AuditRecord.of(AuditEventType.KEY_RETIRED).target(AuditTargetType.KEY, key.getId().toString(),
                        key.getKid()));
                retired++;
            }
        }
        if (retired > 0) {
            cache.invalidate();
        }
        return retired;
    }

    /** Rotates when the active key is older than the realm's interval; a zero interval means manual only. */
    @Transactional
    public boolean rotateIfDue() {
        Optional<Instant> due = nextRotationAt();
        if (due.isPresent() && !due.get().isAfter(clock.instant())) {
            rotate("scheduler");
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<SigningKeyDto> list() {
        return keys.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    @Transactional
    public KeyStatus status() {
        SigningKey active = ensureActive();
        RealmSettings.Keys policy = settings.current().keys();
        int unusable = (int) keys.findByStatusIn(List.of(SigningKeyStatus.ACTIVE, SigningKeyStatus.RETIRING)).stream()
                .filter(key -> !readable(key)).count();
        return new KeyStatus(active.getKid(), active.getAlgorithm(), active.getActivatedAt(), policy.rotationDays(),
                nextRotationAt().orElse(null), policy.retireDays(), keys.findByStatus(SigningKeyStatus.RETIRING).size(),
                unusable);
    }

    private Optional<Instant> nextRotationAt() {
        int rotationDays = settings.current().keys().rotationDays();
        if (rotationDays <= 0) {
            return Optional.empty();
        }
        return keys.findByStatus(SigningKeyStatus.ACTIVE).stream().findFirst()
                .map(key -> key.getActivatedAt().plus(rotationDays, ChronoUnit.DAYS));
    }

    private SigningKeyDto toDto(SigningKey key) {
        return new SigningKeyDto(key.getId(), key.getKid(), key.getAlgorithm(), key.getStatus(), key.getCreatedAt(),
                key.getActivatedAt(), key.getRetireAfter(), key.getRetiredAt());
    }

}
