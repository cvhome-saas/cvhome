package com.asrevo.cvhome.sso.realm;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.session.SessionMetadata;

import static org.assertj.core.api.Assertions.assertThat;

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

    private final SessionRealmFilter filter = new SessionRealmFilter();

    private MockHttpSession run(RealmId realm, MockHttpSession session) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/account/sessions");
        request.setSession(session);
        RealmContext.runIn(realm, () -> {
            try {
                filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        return session;
    }

    @Test
    void aSessionThatArrivesInAnotherRealmIsEnded() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionMetadata.REALM, STORE_A.getId());

        run(STORE_B, session);

        assertThat(session.isInvalid()).as("store B must not be served with store A's session").isTrue();
    }

    @Test
    void aSessionInItsOwnRealmIsLeftAlone() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionMetadata.REALM, STORE_A.getId());

        run(STORE_A, session);

        assertThat(session.isInvalid()).isFalse();
    }

    /** An unstamped session adopts the realm it is seen in; the stamp is written again at sign-in anyway. */
    @Test
    void anUnstampedSessionAdoptsTheRealmRatherThanBeingEnded() {
        MockHttpSession session = run(STORE_A, new MockHttpSession());

        assertThat(session.isInvalid()).isFalse();
        assertThat(session.getAttribute(SessionMetadata.REALM)).isEqualTo(STORE_A.getId());
    }
}
