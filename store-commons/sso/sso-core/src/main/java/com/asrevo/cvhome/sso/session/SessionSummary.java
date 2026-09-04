package com.asrevo.cvhome.sso.session;

import java.time.Instant;

/** One live session of an account, as the console lists it. */
public record SessionSummary(String id, Instant createdAt, Instant lastAccessedAt, Instant expiresAt, String ip,
                             String userAgent, String via, boolean current) {
}
