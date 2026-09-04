package com.asrevo.cvhome.sso.security;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationFailureCredentialsExpiredEvent;
import org.springframework.security.authentication.event.AuthenticationFailureDisabledEvent;
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent;
import org.springframework.security.authentication.event.AuthenticationFailureServiceExceptionEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The audit trail for password sign-in, and the lockout counter that hangs off it.
 *
 * <p>
 * Only username-and-password events are handled. Federated and token authentications raise the same Spring events
 * and are counted elsewhere; counting them here would let a shopper's Google login trip a password lockout on an
 * unrelated account, or a client-credentials call reset one.
 * </p>
 *
 * <p>
 * Only a bad password advances the lockout counter. A locked or disabled account failing is not a guess at the
 * password — counting it would extend a lockout indefinitely from an attacker's point of view and lock out the
 * genuine user for as long as they keep trying.
 * </p>
 */
class AuthenticationAuditListenerTest {

    private static final String WRONG_PASSWORD = "no";

    private static final String LOCKED_MESSAGE = "locked";

    private static final String DISABLED_MESSAGE = "disabled";

    private static final String ACCOUNT_ID = "00000000-0000-0000-0000-000000000001";
    private static final String USERNAME = "someone";

    private final LockoutService lockout = mock(LockoutService.class);
    private final PrincipalNames principals = mock(PrincipalNames.class);
    private final AuditService audit = mock(AuditService.class);
    private final AuthenticationAuditListener listener =
            new AuthenticationAuditListener(lockout, principals, audit);

    private static Authentication password() {
        return new UsernamePasswordAuthenticationToken(ACCOUNT_ID, null, List.of());
    }

    private AuditRecord recorded() {
        ArgumentCaptor<AuditRecord> captor = ArgumentCaptor.forClass(AuditRecord.class);
        verify(audit).recordDetached(captor.capture());
        return captor.getValue();
    }

    @Test
    void asuccessfulPasswordSignInResetsTheCounterAndRecordsTheUsername() {
        when(principals.display(ACCOUNT_ID)).thenReturn(USERNAME);

        listener.onSuccess(new AuthenticationSuccessEvent(password()));

        // The principal name is the account id; both the counter and the row are keyed by the username.
        verify(lockout).succeeded(Mockito.eq(USERNAME), any(), Mockito.eq(LockoutService.VIA_PASSWORD));
        assertThat(recorded()).isNotNull();
    }

    @Test
    void anAuthenticationThatIsNotAPasswordOneIsIgnoredEntirely() {
        Authentication federated = new TestingAuthenticationToken(USERNAME, null, List.of());

        listener.onSuccess(new AuthenticationSuccessEvent(federated));
        listener.onFailure(new AuthenticationFailureBadCredentialsEvent(federated,
                new BadCredentialsException(WRONG_PASSWORD)));

        // Counting a federated login here would trip a password lockout on an unrelated account.
        Mockito.verifyNoInteractions(lockout);
        Mockito.verifyNoInteractions(audit);
    }

    @Test
    void onlyAbadPasswordAdvancesTheLockoutCounter() {
        listener.onFailure(new AuthenticationFailureBadCredentialsEvent(password(),
                new BadCredentialsException(WRONG_PASSWORD)));

        verify(lockout).failed(ACCOUNT_ID);
    }

    @Test
    void alockedOrDisabledAccountFailingIsNotAGuessAtThePassword() {
        listener.onFailure(new AuthenticationFailureLockedEvent(password(), new LockedException(LOCKED_MESSAGE)));
        listener.onFailure(new AuthenticationFailureDisabledEvent(password(), new DisabledException(DISABLED_MESSAGE)));

        // Counting these would extend a lockout indefinitely while the genuine user keeps trying.
        verify(lockout, never()).failed(anyString());
    }

    @Test
    void eachFailureKindIsRecordedUnderItsOwnReason() {
        listener.onFailure(new AuthenticationFailureBadCredentialsEvent(password(),
                new BadCredentialsException(WRONG_PASSWORD)));
        listener.onFailure(new AuthenticationFailureLockedEvent(password(), new LockedException(LOCKED_MESSAGE)));
        listener.onFailure(new AuthenticationFailureDisabledEvent(password(), new DisabledException(DISABLED_MESSAGE)));
        listener.onFailure(new AuthenticationFailureCredentialsExpiredEvent(password(),
                new org.springframework.security.authentication.CredentialsExpiredException("old")));
        listener.onFailure(new AuthenticationFailureServiceExceptionEvent(password(),
                new AuthenticationServiceException("boom")));

        // Five events, five rows: an operator reading the log can tell a wrong password from a locked account.
        verify(audit, Mockito.times(5)).recordDetached(any());
    }

    @Test
    void anUnknownUserFailureFallsIntoTheCatchAllReason() {
        listener.onFailure(new AuthenticationFailureBadCredentialsEvent(password(),
                new UsernameNotFoundException(USERNAME)));

        // Deliberately indistinguishable from a wrong password to the caller; the row still says which it was.
        verify(audit).recordDetached(any());
    }

    @Test
    void aLogoutIsRecordedAgainstWhoeverWasSignedIn() {
        when(principals.display(ACCOUNT_ID)).thenReturn(USERNAME);

        listener.onLogout(new LogoutSuccessEvent(password()));

        assertThat(recorded()).isNotNull();
    }

    @Test
    void aLogoutWithNoAuthenticationOnTheEventRecordsNothing() {
        // Spring's own constructor refuses a null authentication, so the guard is reached with a stubbed event --
        // it is defensive against a publisher that does not use that constructor.
        LogoutSuccessEvent event = mock(LogoutSuccessEvent.class);
        when(event.getAuthentication()).thenReturn(null);

        listener.onLogout(event);

        verify(audit, never()).recordDetached(any());
    }
}
