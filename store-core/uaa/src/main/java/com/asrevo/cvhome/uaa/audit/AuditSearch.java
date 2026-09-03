package com.asrevo.cvhome.uaa.audit;

import java.time.Instant;
import java.util.List;

/**
 * The audit log's filters; every part is optional and they combine with AND.
 *
 * @param types      event types, as their wire names
 * @param categories whole groups, when a screen filters by tab rather than by type
 * @param q          a case-insensitive contains over the actor, the target and the detail
 */
public record AuditSearch(List<String> types, List<AuditEventType.AuditCategory> categories, String actor, String target,
                          String clientId, AuditOutcome outcome, String ip, String q, Instant from, Instant to) {

    public static AuditSearch none() {
        return new AuditSearch(List.of(), List.of(), null, null, null, null, null, null, null, null);
    }

}
