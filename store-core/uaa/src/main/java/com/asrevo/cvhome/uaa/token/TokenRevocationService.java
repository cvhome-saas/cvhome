package com.asrevo.cvhome.uaa.token;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.uaa.audit.AuditEventType;
import com.asrevo.cvhome.uaa.audit.AuditRecord;
import com.asrevo.cvhome.uaa.audit.AuditService;
import com.asrevo.cvhome.uaa.audit.AuditTargetType;

import lombok.RequiredArgsConstructor;

/**
 * Removes every authorization — access, refresh and ID tokens — an account or a client holds.
 *
 * <p>
 * The authorization service has no "find by principal", so the ids are read with JDBC and each authorization is
 * removed through the service, which is the one thing that knows the row's shape. A removed authorization makes its
 * refresh token unusable at once and its access token fail introspection; a self-contained access token that a
 * resource server validates locally lives until its (fifteen-minute) expiry.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private static final String IDS_BY_PRINCIPAL = "select id from uaa.oauth2_authorization where principal_name = ?";

    private static final String IDS_BY_CLIENT = "select id from uaa.oauth2_authorization where registered_client_id = ?";

    private static final String COUNT = "%d authorization(s)";

    private final JdbcTemplate jdbc;

    private final OAuth2AuthorizationService authorizations;

    private final AuditService audit;

    @Transactional
    public int revokeAllForUser(String username) {
        int removed = removeAll(jdbc.queryForList(IDS_BY_PRINCIPAL, String.class, username));
        if (removed > 0) {
            audit.record(AuditRecord.of(AuditEventType.TOKEN_REVOKED).target(AuditTargetType.USER, null, username)
                    .detail(String.format(COUNT, removed)));
        }
        return removed;
    }

    @Transactional
    public int revokeAllForClient(String registeredClientId, String clientId) {
        int removed = removeAll(jdbc.queryForList(IDS_BY_CLIENT, String.class, registeredClientId));
        if (removed > 0) {
            audit.record(AuditRecord.of(AuditEventType.TOKEN_REVOKED).client(clientId)
                    .target(AuditTargetType.CLIENT, registeredClientId, clientId)
                    .detail(String.format(COUNT, removed)));
        }
        return removed;
    }

    private int removeAll(List<String> ids) {
        int removed = 0;
        for (String id : ids) {
            OAuth2Authorization authorization = authorizations.findById(id);
            if (authorization != null) {
                authorizations.remove(authorization);
                removed++;
            }
        }
        return removed;
    }

}
