package com.asrevo.cvhome.sso.security;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import com.asrevo.cvhome.sso.audit.AuditActorType;
import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditOutcome;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditRecords;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.idp.BrokerRefusedException;
import com.asrevo.cvhome.sso.idp.PendingLink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Where a brokered login that did not complete lands.
 *
 * <p>
 * Two behaviours here have bitten before and are pinned deliberately. {@code link_required} is <em>not</em> a
 * failure — it is the password step — so it must not be audited as one, or a person completing a first federated
 * login would leave a failed-login row and, through the lockout counter, eventually a locked account.
 * </p>
 *
 * <p>
 * And the exception must never reach the session. Sessions are Spring Session JDBC rows, so every attribute is
 * serialised, and an {@link OAuth2AuthenticationException} carries the non-serialisable HTTP response that caused
 * it — saving one turned a refusal into a 500. Only the {@link PendingLink} goes in; the code travels in the query
 * string.
 * </p>
 */
class IdpLoginFailureHandlerTest {

    private static final String REDIRECT_PREFIX = "/login?error=";
    private static final String GENERIC = "idp";
    private static final String NOPE = "nope";

    private final AuditService audit = mock(AuditService.class);
    private final IdpLoginFailureHandler handler = new IdpLoginFailureHandler(audit);

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    void arefusalRedirectsToTheSignInPageCarryingTheBrokersOwnCode() throws Exception {
        handler.onAuthenticationFailure(request, response,
                new BrokerRefusedException(BrokerRefusedException.UNKNOWN_USER, "no such account"));

        assertThat(response.getRedirectedUrl()).isEqualTo(REDIRECT_PREFIX + BrokerRefusedException.UNKNOWN_USER);
    }

    @Test
    void arefusalIsAuditedAsAfailedLoginUnderTheUppercasedCode() throws Exception {
        handler.onAuthenticationFailure(request, response,
                new BrokerRefusedException(BrokerRefusedException.LOCKED, "locked out"));

        AuditRecord record = recorded();
        assertThat(AuditRecords.typeOf(record)).isEqualTo(AuditEventType.USER_LOGIN_FAILED);
        assertThat(AuditRecords.outcomeOf(record)).isEqualTo(AuditOutcome.FAILURE);
        assertThat(AuditRecords.reasonCodeOf(record)).isEqualTo("ACCOUNT_LOCKED");
        assertThat(AuditRecords.actorOf(record).type()).isEqualTo(AuditActorType.ANONYMOUS);
    }

    @Test
    void alinkRequiredIsThePasswordStepRatherThanAfailureAndIsNotAudited() throws Exception {
        handler.onAuthenticationFailure(request, response, new BrokerRefusedException(pendingLink()));

        // Auditing it would leave a failed-login row — and feed the lockout counter — for a normal first login.
        verify(audit, never()).recordDetached(any());
        assertThat(response.getRedirectedUrl()).isEqualTo(REDIRECT_PREFIX + BrokerRefusedException.LINK_REQUIRED);
    }

    @Test
    void thePendingLinkIsParkedInTheSessionSoTheConfirmationCallCanFinishIt() throws Exception {
        PendingLink pending = pendingLink();

        handler.onAuthenticationFailure(request, response, new BrokerRefusedException(pending));

        assertThat(request.getSession().getAttribute(PendingLink.SESSION_KEY)).isSameAs(pending);
    }

    @Test
    void theExceptionItselfIsNeverPutInTheSession() throws Exception {
        OAuth2AuthenticationException oauth =
                new OAuth2AuthenticationException(new OAuth2Error("invalid_token", "bad token", null));

        handler.onAuthenticationFailure(request, response, oauth);

        // Spring Session JDBC serialises every attribute; this exception carries a non-serialisable response.
        assertThat(java.util.Collections.list(request.getSession().getAttributeNames())).isEmpty();
    }

    @Test
    void arefusalCarryingNoPendingLinkLeavesTheSessionAlone() throws Exception {
        handler.onAuthenticationFailure(request, response,
                new BrokerRefusedException(BrokerRefusedException.NO_EMAIL, "no address"));

        assertThat(request.getSession(false)).isNull();
    }

    @Test
    void anOauthFailureFallsBackToTheGenericCode() throws Exception {
        handler.onAuthenticationFailure(request, response,
                new OAuth2AuthenticationException(new OAuth2Error("server_error", "upstream fell over", null)));

        assertThat(response.getRedirectedUrl()).isEqualTo(REDIRECT_PREFIX + GENERIC);
        assertThat(AuditRecords.reasonCodeOf(recorded())).isEqualTo("IDP");
    }

    @Test
    void anyOtherAuthenticationFailureIsStillRecordedAndRedirected() throws Exception {
        handler.onAuthenticationFailure(request, response, new BadCredentialsException(NOPE));

        assertThat(response.getRedirectedUrl()).isEqualTo(REDIRECT_PREFIX + GENERIC);
        assertThat(AuditRecords.detailOf(recorded())).isEqualTo(NOPE);
    }

    private AuditRecord recorded() {
        ArgumentCaptor<AuditRecord> captor = ArgumentCaptor.forClass(AuditRecord.class);
        verify(audit, Mockito.atLeastOnce()).recordDetached(captor.capture());
        return captor.getValue();
    }

    private static PendingLink pendingLink() {
        return new PendingLink(UUID.randomUUID(), "google", "Google", "sub-1", "someone@example.com",
                UUID.randomUUID(), "someone");
    }

}
