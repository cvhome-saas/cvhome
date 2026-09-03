package com.asrevo.cvhome.sso.dto;

import java.time.Instant;
import java.util.UUID;

import com.asrevo.cvhome.sso.domain.SigningKeyStatus;

/** A signing key as the console lists it: never the private half, never even the public JWK. */
public record SigningKeyDto(UUID id, String kid, String algorithm, SigningKeyStatus status, Instant createdAt,
                            Instant activatedAt, Instant retireAfter, Instant retiredAt) {
}
