package com.asrevo.cvhome.sso.token;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;

import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditRecords;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.audit.AuditTargetType;
import com.asrevo.cvhome.sso.domain.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Removing every authorization an account or a client holds.
 *
 * <p>
 * <strong>The lookup is keyed on the account id, not the username.</strong> {@code principal_name} holds the id
 * (see {@code JpaUserDetailsService}), and keying this on the username would revoke the tokens of every
 * same-named account on the deployment — which on cua, where every store is a realm, means an unrelated store's
 * shoppers.
 * </p>
 *
 * <p>
 * Rows the authorization service no longer knows are skipped rather than counted, so the audit line says how many
 * authorizations actually went; and nothing is audited when nothing was removed, because a revocation that
 * revoked nothing is not an event.
 * </p>
 */
class TokenRevocationServiceTest {

    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-0000000000f1");
    private static final String USERNAME = "someone";
    private static final String REGISTRATION_ID = "reg-1";
    private static final String CLIENT_ID = "console";
    private static final String A_1 = "a-1";
    private static final String A_2 = "a-2";
    private static final String S_1_AUTHORIZATION_S = "1 authorization(s)";
    private static final String GONE = "gone";

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final OAuth2AuthorizationService authorizations = mock(OAuth2AuthorizationService.class);
    private final AuditService audit = mock(AuditService.class);
    private final TokenRevocationService service = new TokenRevocationService(jdbc, authorizations, audit);

    @Test
    void anAccountsAuthorizationsAreFoundByItsIdRatherThanItsUsername() {
        givenAuthorizationsForPrincipal(ACCOUNT_ID.toString(), A_1, A_2);

        assertThat(service.revokeAllForUser(user())).isEqualTo(2);

        // On cua every store is a realm; keying this on a username would reach another store's shoppers.
        verify(jdbc).queryForList(any(String.class), eq(String.class), eq(ACCOUNT_ID.toString()));
    }

    @Test
    void everyFoundAuthorizationIsRemovedThroughTheServiceThatKnowsItsShape() {
        givenAuthorizationsForPrincipal(ACCOUNT_ID.toString(), A_1, A_2);

        service.revokeAllForUser(user());

        verify(authorizations, org.mockito.Mockito.times(2)).remove(any(OAuth2Authorization.class));
    }

    @Test
    void therevocationIsAuditedAgainstTheAccountWithTheCount() {
        givenAuthorizationsForPrincipal(ACCOUNT_ID.toString(), A_1);

        service.revokeAllForUser(user());

        AuditRecord record = recorded();
        assertThat(AuditRecords.typeOf(record)).isEqualTo(AuditEventType.TOKEN_REVOKED);
        assertThat(AuditRecords.targetTypeOf(record)).isEqualTo(AuditTargetType.USER);
        assertThat(AuditRecords.targetIdOf(record)).isEqualTo(ACCOUNT_ID.toString());
        assertThat(AuditRecords.targetNameOf(record)).isEqualTo(USERNAME);
        assertThat(AuditRecords.detailOf(record)).isEqualTo(S_1_AUTHORIZATION_S);
    }

    @Test
    void anAccountHoldingNoAuthorizationsIsNotAudited() {
        givenAuthorizationsForPrincipal(ACCOUNT_ID.toString());

        assertThat(service.revokeAllForUser(user())).isZero();

        // A revocation that revoked nothing is not an event.
        verify(audit, never()).record(any());
    }

    @Test
    void anIdTheAuthorizationServiceNoLongerKnowsIsSkippedRatherThanCounted() {
        when(jdbc.queryForList(any(String.class), eq(String.class), eq(ACCOUNT_ID.toString())))
                .thenReturn(List.of(A_1, GONE));
        when(authorizations.findById(A_1)).thenReturn(mock(OAuth2Authorization.class));
        when(authorizations.findById(GONE)).thenReturn(null);

        assertThat(service.revokeAllForUser(user())).isEqualTo(1);
        assertThat(AuditRecords.detailOf(recorded())).isEqualTo(S_1_AUTHORIZATION_S);
    }

    @Test
    void aclientsAuthorizationsAreFoundByItsRegistrationIdAndAuditedUnderItsClientId() {
        when(jdbc.queryForList(any(String.class), eq(String.class), eq(REGISTRATION_ID)))
                .thenReturn(List.of(A_1, A_2, "a-3"));
        when(authorizations.findById(any())).thenReturn(mock(OAuth2Authorization.class));

        assertThat(service.revokeAllForClient(REGISTRATION_ID, CLIENT_ID)).isEqualTo(3);

        AuditRecord record = recorded();
        assertThat(AuditRecords.clientIdOf(record)).isEqualTo(CLIENT_ID);
        assertThat(AuditRecords.targetTypeOf(record)).isEqualTo(AuditTargetType.CLIENT);
        assertThat(AuditRecords.targetIdOf(record)).isEqualTo(REGISTRATION_ID);
        assertThat(AuditRecords.detailOf(record)).isEqualTo("3 authorization(s)");
    }

    @Test
    void aclientHoldingNoAuthorizationsIsNotAudited() {
        when(jdbc.queryForList(any(String.class), eq(String.class), eq(REGISTRATION_ID))).thenReturn(List.of());

        assertThat(service.revokeAllForClient(REGISTRATION_ID, CLIENT_ID)).isZero();
        verify(audit, never()).record(any());
    }

    private void givenAuthorizationsForPrincipal(String principalName, String... ids) {
        when(jdbc.queryForList(any(String.class), eq(String.class), eq(principalName))).thenReturn(List.of(ids));
        when(authorizations.findById(any())).thenReturn(mock(OAuth2Authorization.class));
    }

    private AuditRecord recorded() {
        ArgumentCaptor<AuditRecord> captor = ArgumentCaptor.forClass(AuditRecord.class);
        verify(audit).record(captor.capture());
        return captor.getValue();
    }

    private static User user() {
        User user = new User();
        user.setId(ACCOUNT_ID);
        user.setUsername(USERNAME);
        user.setEmail("someone@example.com");
        return user;
    }

}
