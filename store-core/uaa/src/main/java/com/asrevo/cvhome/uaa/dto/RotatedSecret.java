package com.asrevo.cvhome.uaa.dto;

import java.time.Instant;

/**
 * A freshly issued client secret, shown once.
 *
 * @param clientSecretExpiresAt when the new secret stops working, or {@code null} for never
 * @param previousSecretUntil   when the secret it replaced stops working, or {@code null} when there was none
 */
public record RotatedSecret(String id, String clientId, String clientSecret, Instant clientSecretExpiresAt,
                            Instant previousSecretUntil) {
}
