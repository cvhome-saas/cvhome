package com.asrevo.cvhome.sso.dto;

import java.time.Instant;

import com.asrevo.cvhome.sso.client.ClientType;

/**
 * What uaa adds to a registration beyond Spring's own settings. Everything but {@code description} is read-only on
 * input: enabling and disabling and the secret's lifetime have their own endpoints. {@code enabled} is boxed so a
 * request that sends only a description parses.
 *
 * @param previousSecretUntil when the rotated-out secret stops authenticating, or {@code null} when none is in grace
 */
public record ClientStatus(String description, Boolean enabled, ClientType type, Instant clientIdIssuedAt,
                           Instant clientSecretExpiresAt, Instant lastTokenIssuedAt, Instant disabledAt, String disabledBy,
                           Instant previousSecretUntil) {
}
