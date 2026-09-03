package com.asrevo.cvhome.sso.dto;

import java.time.Instant;
import java.util.List;

import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditOutcome;
import com.asrevo.cvhome.sso.audit.AuditSearch;

/**
 * The audit log's query string, bound as one object.
 *
 * <p>
 * A record rather than ten method parameters: the same filters serve the page and the export, and binding them once
 * means the two cannot drift apart.
 * </p>
 */
public record AuditQueryParams(List<String> type, List<AuditEventType.AuditCategory> category, String actor,
                               String target, String clientId, AuditOutcome outcome, String ip, String q,
                               Instant from, Instant to) {

    public AuditSearch toSearch() {
        return new AuditSearch(type == null ? List.of() : type, category == null ? List.of() : category, actor, target,
                clientId, outcome, ip, q, from, to);
    }

}
