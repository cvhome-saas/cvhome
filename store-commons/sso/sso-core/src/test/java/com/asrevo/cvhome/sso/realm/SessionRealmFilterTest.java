package com.asrevo.cvhome.sso.realm;

import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.errors.web.ProblemDetailFactory;
import com.asrevo.cvhome.sso.session.SessionMetadata;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The second lock on a boundary the browser is supposed to hold on its own.
 *
 * <p>
 * A store's session cookie is host-scoped, so it is never sent to another store — until someone sets a
 * {@code Domain} on the shared parent, which is one line of configuration and would hand every store's sessions
 * to every other store. This is the check that would catch it, and it is only worth having if it is tested.
 * </p>
 */
class SessionRealmFilterTest {

    private static final RealmId STORE_A = RealmId.of("65f023632bc46470c104b76f");

    private static final RealmId STORE_B = RealmId.of("65f023632bc46470c104b75f");

    private final ProblemDetailFactory problems = mock(ProblemDetailFactory.class);

    private final SessionRealmFilter filter = new SessionRealmFilter(problems, JsonMapper.builder().build());

    SessionRealmFilterTest() {
        when(problems.create(any(), any(), any(), any(), any())).thenReturn(ProblemDetail.forStatus(403));
    }

    private MockHttpServletResponse run(RealmId realm, MockHttpSession session) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/account/sessions");
        if (session != null) {
            request.setSession(session);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        RealmContext.runIn(realm, () -> {
            try {
                filter.doFilter(request, response, new MockFilterChain());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        return response;
    }

    private static MockHttpSession signedInTo(RealmId realm) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionMetadata.REALM, realm.getId());
        return session;
    }

    @Test
    void aSessionThatArrivesInAnotherRealmIsRefused() {
        MockHttpSession session = signedInTo(STORE_A);

        MockHttpServletResponse response = run(STORE_B, session);

        assertThat(response.getStatus()).as("store B must not be served with store A's session").isEqualTo(403);
    }

    /**
     * And the session survives it. A session that any request can destroy by naming another store in a query
     * parameter is a forced-logout button for anyone who can make the browser follow a link.
     */
    @Test
    void refusingDoesNotEndTheSession() {
        MockHttpSession session = signedInTo(STORE_A);

        run(STORE_B, session);

        assertThat(session.isInvalid()).isFalse();
    }

    @Test
    void aSessionInItsOwnRealmIsServed() {
        assertThat(run(STORE_A, signedInTo(STORE_A)).getStatus()).isEqualTo(200);
    }

    /**
     * The hand-off session {@code /oauth2/authorize} creates before anyone has signed in carries no stamp and no
     * tenant data. Checking it would break the hand-off itself, where the realm is resolved from the form the
     * browser is about to post.
     */
    @Test
    void anAnonymousSessionIsNotTheFiltersBusiness() {
        assertThat(run(STORE_B, new MockHttpSession()).getStatus()).isEqualTo(200);
        assertThat(run(STORE_B, null).getStatus()).isEqualTo(200);
    }

}
