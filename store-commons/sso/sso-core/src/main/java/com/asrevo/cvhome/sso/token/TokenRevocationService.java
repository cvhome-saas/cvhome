package com.asrevo.cvhome.sso.token;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.audit.AuditTargetType;
import com.asrevo.cvhome.sso.domain.User;

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
/*
 * The table is unqualified on purpose. Hikari sets the connection's schema to the service's own name
 * (`spring.datasource.hikari.schema`), so `oauth2_authorization` resolves to uaa's copy in uaa and cua's in cua.
 * Naming the schema here was a bug carried in with the extraction: the two deployments share one database, so a
 * cua revocation was reading — and deleting from — uaa's table.
 */
@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private static final String IDS_BY_PRINCIPAL = "select id from oauth2_authorization where principal_name = ?";

    private static final String IDS_BY_CLIENT = "select id from oauth2_authorization where registered_client_id = ?";

    private static final String COUNT = "%d authorization(s)";

    private final JdbcTemplate jdbc;

    private final OAuth2AuthorizationService authorizations;

    private final AuditService audit;

    /**
     * {@code principal_name} is the account id, not the username — see {@code JpaUserDetailsService}. Keying this
     * on the username would have revoked the tokens of every same-named account on the deployment, which on cua
     * means every store on the pod.
     */
    @Transactional
    public int revokeAllForUser(User user) {
        String principalName = user.getId().toString();
        int removed = removeAll(jdbc.queryForList(IDS_BY_PRINCIPAL, String.class, principalName));
        if (removed > 0) {
            audit.record(AuditRecord.of(AuditEventType.TOKEN_REVOKED)
                    .target(AuditTargetType.USER, principalName, user.getUsername())
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
