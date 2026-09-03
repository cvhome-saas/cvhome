package com.asrevo.cvhome.sso.dto;

import java.time.Instant;
import java.util.List;

/**
 * The overview screen in one read.
 *
 * @param signIns   one bucket per hour or per day over the range, successes and failures side by side
 * @param posture   what the realm's own data says about how it is configured; each check names what to do
 * @param counts    the rail's badges: users, roles, clients, providers
 */
public record Dashboard(String range, Instant from, Instant to, List<Bucket> signIns, long signInsTotal,
                        long signInFailures, long tokensIssued, long activeSessions, UserCounts users,
                        List<Ranked> topClients, List<AuditEventDto> recentFailures, List<PostureCheck> posture,
                        RailCounts counts) {

    /** One column of the sign-in chart. */
    public record Bucket(Instant at, long success, long failure) {
    }

    /** One row of a "top" list: a name, a number, and the share of the largest. */
    public record Ranked(String label, long value) {
    }

    /**
     * One line of the security posture, computed from data rather than declared.
     *
     * @param level {@code OK}, {@code WARN} or {@code RISK}
     */
    public record PostureCheck(String id, String level, String detail) {
    }

    public record RailCounts(long users, long roles, long clients, long identityProviders) {
    }

}
