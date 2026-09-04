package com.asrevo.cvhome.sso.security;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditRecords;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.idp.PendingLink;
import com.asrevo.cvhome.sso.session.SessionAdminService;
import com.asrevo.cvhome.sso.session.SessionMetadata;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * What happens after a brokered login succeeds.
 *
 * <p>
 * The federated principal is replaced by a {@code UsernamePasswordAuthenticationToken} over the account's
 * {@code UserDetails} — the shape the authorization server's Jackson allow-list, the token customizer and every
 * {@code /me} caller already understand. A {@link BrokeredPrincipal} left in the context serialises into the
 * session as a type the allow-list rejects, which fails the request <em>after</em> the login has succeeded.
 * </p>
 *
 * <p>
 * The session is then stamped, given the realm's idle timeout, and — when the realm asks for one session per
 * person — every other session of that account is ended. The pending link is cleared so a second callback cannot
 * re-enter the confirmation flow, and the login is counted against the lockout as a success so a federated login
 * clears a partial lockout the same way a password one does.
 * </p>
 */
class BrokeredLoginSuccessHandlerTest {

    private static final String ACCOUNT_ID = "00000000-0000-0000-0000-0000000000e5";
    private static final String ALIAS = "corp";
    private static final int IDLE_SECONDS = 1800;
    private static final String ROLE_USER = "ROLE_USER";
    private static final String SUB_1 = "sub-1";

    private final JpaUserDetailsService userDetails = mock(JpaUserDetailsService.class);
    private final SettingsService settings = mock(SettingsService.class);
    private final SessionAdminService sessions = mock(SessionAdminService.class);
    private final LockoutService lockout = mock(LockoutService.class);
    private final AuditService audit = mock(AuditService.class);
    private final RequestCache requestCache = new HttpSessionRequestCache();
    private final BrokeredLoginSuccessHandler handler =
            new BrokeredLoginSuccessHandler(userDetails, settings, sessions, lockout, audit, requestCache);

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @BeforeEach
    void setUp() {
        when(userDetails.loadUserByUsername(ACCOUNT_ID)).thenReturn(details());
        givenSessionPolicy(false);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void thefederatedPrincipalIsReplacedByTheLocalUserDetailsToken() throws Exception {
        handler.onAuthenticationSuccess(request, response, brokered());

        Authentication local = SecurityContextHolder.getContext().getAuthentication();
        // A BrokeredPrincipal here serialises into the session as a type the authorization server rejects.
        assertThat(local.getPrincipal()).isInstanceOf(UserDetails.class);
        assertThat(local.getName()).isEqualTo(ACCOUNT_ID);
        assertThat(local.getAuthorities()).extracting("authority").containsExactly(ROLE_USER);
    }

    @Test
    void thelocalPrincipalIsSavedToTheSessionBecauseTheFilterSavedTheContextBeforeThisRan() throws Exception {
        handler.onAuthenticationSuccess(request, response, brokered());

        assertThat(request.getSession(false)).isNotNull();
        assertThat(request.getSession().getAttributeNames().hasMoreElements()).isTrue();
    }

    @Test
    void thesessionIsStampedAndGivenTheRealmsIdleTimeout() throws Exception {
        handler.onAuthenticationSuccess(request, response, brokered());

        assertThat(request.getSession().getAttribute(SessionMetadata.CREATED_AT)).isNotNull();
        assertThat(request.getSession().getMaxInactiveInterval()).isEqualTo(IDLE_SECONDS);
    }

    @Test
    void thependingLinkIsClearedSoAsecondCallbackCannotReEnterTheConfirmation() throws Exception {
        request.getSession().setAttribute(PendingLink.SESSION_KEY, new PendingLink(UUID.randomUUID(), ALIAS,
                "Corp", SUB_1, "someone@example.com", UUID.randomUUID(), ACCOUNT_ID));

        handler.onAuthenticationSuccess(request, response, brokered());

        assertThat(request.getSession().getAttribute(PendingLink.SESSION_KEY)).isNull();
    }

    @Test
    void asuccessfulFederatedLoginClearsThePartialLockoutTheSameWayApasswordOneDoes() throws Exception {
        handler.onAuthenticationSuccess(request, response, brokered());

        verify(lockout).succeeded(org.mockito.ArgumentMatchers.eq(ACCOUNT_ID), any(),
                org.mockito.ArgumentMatchers.eq(BrokeredLoginSuccessHandler.VIA_PREFIX + ALIAS));
    }

    @Test
    void theloginIsAuditedNamingTheProviderItCameThrough() throws Exception {
        handler.onAuthenticationSuccess(request, response, brokered());

        AuditRecord record = recorded();
        assertThat(AuditRecords.typeOf(record)).isEqualTo(AuditEventType.USER_LOGIN);
        assertThat(AuditRecords.detailOf(record)).isEqualTo(BrokeredLoginSuccessHandler.VIA_PREFIX + ALIAS);
        assertThat(AuditRecords.targetNameOf(record)).isEqualTo(ACCOUNT_ID);
    }

    @Test
    void anAuthenticationCarryingNoBrokeredPrincipalIsStillAttributedToAprovider() throws Exception {
        Authentication other = new org.springframework.security.authentication.TestingAuthenticationToken(
                ACCOUNT_ID, null, List.of());

        handler.onAuthenticationSuccess(request, response, other);

        // The bare prefix, not a null via: an unattributable federated login is still a federated login.
        assertThat(AuditRecords.detailOf(recorded())).isEqualTo(BrokeredLoginSuccessHandler.VIA_PREFIX);
    }

    @Test
    void arealmThatAllowsOneSessionPerPersonEndsTheOthers() throws Exception {
        givenSessionPolicy(true);

        handler.onAuthenticationSuccess(request, response, brokered());

        verify(sessions).revokeOthers(org.mockito.ArgumentMatchers.eq(ACCOUNT_ID), anyString());
    }

    @Test
    void arealmThatAllowsSeveralSessionsLeavesTheOthersAlone() throws Exception {
        handler.onAuthenticationSuccess(request, response, brokered());

        verify(sessions, never()).revokeOthers(anyString(), anyString());
    }

    @Test
    void theSharedEstablishCallSignsInTheSameWayForTheLinkConfirmation() {
        Authentication local = handler.establish(request, response, ACCOUNT_ID, "IDP:corp");

        assertThat(local.getName()).isEqualTo(ACCOUNT_ID);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(local);
        verify(audit).recordDetached(any());
    }

    private AuditRecord recorded() {
        org.mockito.ArgumentCaptor<AuditRecord> captor = org.mockito.ArgumentCaptor.forClass(AuditRecord.class);
        verify(audit).recordDetached(captor.capture());
        return captor.getValue();
    }

    private void givenSessionPolicy(boolean singleSessionPerUser) {
        RealmSettings.Sessions policy =
                new RealmSettings.Sessions(IDLE_SECONDS, 28800, false, 0, singleSessionPerUser);
        RealmSettings realm = mock(RealmSettings.class);
        when(realm.sessions()).thenReturn(policy);
        when(settings.current()).thenReturn(realm);
    }

    private static UserDetails details() {
        return User.withUsername(ACCOUNT_ID)
                .password("{noop}x")
                .authorities(List.of(new SimpleGrantedAuthority(ROLE_USER)))
                .build();
    }

    private static Authentication brokered() {
        BrokeredPrincipal principal = new BrokeredPrincipal(ACCOUNT_ID, ALIAS, Map.of("sub", SUB_1), null, null);
        return new org.springframework.security.authentication.TestingAuthenticationToken(principal, null, List.of());
    }

}
