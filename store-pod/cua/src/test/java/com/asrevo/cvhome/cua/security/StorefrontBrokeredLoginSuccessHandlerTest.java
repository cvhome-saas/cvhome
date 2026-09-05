package com.asrevo.cvhome.cua.security;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import com.asrevo.cvhome.sso.idp.PendingLink;
import com.asrevo.cvhome.sso.security.BrokeredLoginSuccessHandler;
import com.asrevo.cvhome.sso.security.BrokeredPrincipal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * What happens after a shopper signs in through an identity provider.
 *
 * <p>
 * The federated authentication is exchanged for a local one before anything else, because everything downstream —
 * the session, the token, the audit row — is about the local account, not the Google or Apple identity that
 * proved it. The {@code IDP:} marker records which provider was used, so an account that later loses that
 * provider can still be traced to how it was created.
 * </p>
 *
 * <p>
 * The pending-link attribute is cleared on the way out. It is what carries a half-finished "link this provider to
 * an existing account" flow across the redirect to the provider; leaving it behind would make the next unrelated
 * federated login look like a continuation of it.
 * </p>
 */
class StorefrontBrokeredLoginSuccessHandlerTest {

    private static final String SHOPPER = "shopper@example.com";
    private static final String GOOGLE = "google";

    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
    private final BrokeredLoginSuccessHandler brokered = Mockito.mock(BrokeredLoginSuccessHandler.class);
    private final StorefrontBrokeredLoginSuccessHandler handler =
            new StorefrontBrokeredLoginSuccessHandler(requestCache, brokered);

    private static Authentication local() {
        return new UsernamePasswordAuthenticationToken(SHOPPER, null, List.of());
    }

    @Test
    void afederatedLoginIsExchangedForALocalOneMarkedWithItsProvider() throws Exception {
        BrokeredPrincipal principal = Mockito.mock(BrokeredPrincipal.class);
        when(principal.providerAlias()).thenReturn(GOOGLE);
        Authentication federated = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        when(brokered.establish(any(), any(), any(), any())).thenReturn(local());

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), new MockHttpServletResponse(), federated);

        // The marker is what matters here: it records which provider proved the identity.
        verify(brokered).establish(any(), any(), any(),
                eq("%s%s".formatted(BrokeredLoginSuccessHandler.VIA_PREFIX, GOOGLE)));
    }

    @Test
    void anAuthenticationThatIsNotBrokeredStillGetsTheBareMarker() throws Exception {
        when(brokered.establish(any(), any(), any(), any())).thenReturn(local());

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), new MockHttpServletResponse(), local());

        verify(brokered).establish(any(), any(), eq(SHOPPER), eq(BrokeredLoginSuccessHandler.VIA_PREFIX));
    }

    @Test
    void thePendingLinkIsClearedSoTheNextLoginIsNotTreatedAsAContinuation() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute(PendingLink.SESSION_KEY, "half-finished");
        when(brokered.establish(any(), any(), any(), any())).thenReturn(local());

        handler.onAuthenticationSuccess(request, new MockHttpServletResponse(), local());

        assertThat(request.getSession().getAttribute(PendingLink.SESSION_KEY)).isNull();
    }
}
