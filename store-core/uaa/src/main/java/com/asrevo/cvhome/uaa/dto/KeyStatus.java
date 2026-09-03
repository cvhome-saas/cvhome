package com.asrevo.cvhome.uaa.dto;

import java.time.Instant;

/**
 * The signing-key posture in one read.
 *
 * @param nextRotationAt when the scheduler will rotate on its own, or {@code null} when rotation is manual
 * @param retiringCount  keys still verifying but no longer signing
 * @param unusableCount  stored keys whose private half cannot be read back
 */
public record KeyStatus(String activeKid, String algorithm, Instant activatedAt, int rotationDays, Instant nextRotationAt,
                        int retireDays, int retiringCount, int unusableCount) {
}
