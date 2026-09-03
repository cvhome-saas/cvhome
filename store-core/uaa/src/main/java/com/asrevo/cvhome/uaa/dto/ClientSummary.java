package com.asrevo.cvhome.uaa.dto;

import java.time.Instant;
import java.util.Set;

import com.asrevo.cvhome.uaa.client.ClientType;

/** A row of the client list: enough to draw the table without a read per row. */
public record ClientSummary(String id, String clientId, String clientName, ClientType type, boolean enabled,
                            Set<String> grantTypes, Instant clientSecretExpiresAt, Instant lastTokenIssuedAt) {
}
