package com.asrevo.cvhome.sso.service;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditRecords;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.password.PasswordService;
import com.asrevo.cvhome.sso.repo.UserRepository;
import com.asrevo.cvhome.sso.session.SessionAdminService;
import com.asrevo.cvhome.sso.token.TokenRevocationService;
import com.asrevo.cvhome.uaa.errors.CurrentPasswordMismatchException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Changing one's own password.
 *
 * <p>
 * The current password is what proves this is the person and not a hijacked session, so it is checked before
 * anything is written — and when it fails, nothing else happens at all: no save, no session revocation, no audit
 * row. A partial application here would let somebody with a stolen session end the real owner's sessions.
 * </p>
 *
 * <p>
 * On success every other session and every token ends, because a password change is what a person does after a
 * scare, and leaving a refresh token alive would leave the intruder in.
 * </p>
 */
class AccountServiceTest {

    private static final String CURRENT = "old-password";
    private static final String REPLACEMENT = "new-password";
    private static final String KEEP_SESSION = "session-1";
    private static final String USERNAME = "someone";

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordService passwords = mock(PasswordService.class);
    private final SessionAdminService sessions = mock(SessionAdminService.class);
    private final TokenRevocationService tokens = mock(TokenRevocationService.class);
    private final AuditService audit = mock(AuditService.class);
    private final AccountService service = new AccountService(users, passwords, sessions, tokens, audit);

    private final User user = user();

    @Test
    void therightCurrentPasswordSetsTheNewOneAndSavesTheAccount() throws Exception {
        when(passwords.matches(user, CURRENT)).thenReturn(true);

        service.changePassword(user, CURRENT, REPLACEMENT, KEEP_SESSION);

        InOrder order = Mockito.inOrder(passwords, users);
        order.verify(passwords).setPassword(user, REPLACEMENT);
        order.verify(users).save(user);
    }

    @Test
    void everyOtherSessionAndEveryTokenEndsButThisSessionSurvives() throws Exception {
        when(passwords.matches(user, CURRENT)).thenReturn(true);

        service.changePassword(user, CURRENT, REPLACEMENT, KEEP_SESSION);

        // A password change is what a person does after a scare; a live refresh token would leave the intruder in.
        verify(sessions).revokeAll(user, KEEP_SESSION);
        verify(tokens).revokeAllForUser(user);
    }

    @Test
    void thechangeIsAuditedAgainstTheAccount() throws Exception {
        when(passwords.matches(user, CURRENT)).thenReturn(true);

        service.changePassword(user, CURRENT, REPLACEMENT, KEEP_SESSION);

        org.mockito.ArgumentCaptor<AuditRecord> captor = org.mockito.ArgumentCaptor.forClass(AuditRecord.class);
        verify(audit).record(captor.capture());
        assertThat(AuditRecords.typeOf(captor.getValue())).isEqualTo(AuditEventType.USER_PASSWORD_CHANGED);
        assertThat(AuditRecords.targetNameOf(captor.getValue())).isEqualTo(USERNAME);
    }

    @Test
    void thewrongCurrentPasswordChangesNothingAtAll() throws Exception {
        when(passwords.matches(user, CURRENT)).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(user, CURRENT, REPLACEMENT, KEEP_SESSION))
                .isInstanceOf(CurrentPasswordMismatchException.class);

        // Otherwise a stolen session could end the real owner's sessions without knowing their password.
        verify(passwords, never()).setPassword(any(), any());
        verify(users, never()).save(any());
        verify(sessions, never()).revokeAll(any(), any());
        verify(tokens, never()).revokeAllForUser(any());
        verify(audit, never()).record(any());
    }

    private static User user() {
        User user = new User();
        user.setId(UUID.fromString("00000000-0000-0000-0000-0000000000c1"));
        user.setUsername(USERNAME);
        user.setEmail("someone@example.com");
        return user;
    }

}
