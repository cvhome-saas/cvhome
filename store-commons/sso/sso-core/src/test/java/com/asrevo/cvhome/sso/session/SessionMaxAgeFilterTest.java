package com.asrevo.cvhome.sso.session;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The absolute session ceiling — the one the idle timeout cannot extend.
 *
 * <p>
 * Idle timeout is Spring Session's and resets on every request, so a session that is used continuously never
 * expires under it. This filter is the "however active you have been" limit, which is what makes a stolen session
 * cookie expire on a schedule rather than never.
 * </p>
 */
class SessionMaxAgeFilterTest {

    private static final int MAX_SECONDS = 3600;

    private final SettingsService settings = mock(SettingsService.class);
    private final SessionMaxAgeFilter filter = new SessionMaxAgeFilter(settings);

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final MockFilterChain chain = new MockFilterChain();

    @Test
    void asessionOlderThanTheCeilingIsInvalidated() throws Exception {
        givenMaxSeconds(MAX_SECONDS);
        MockHttpSession session = sessionCreated(Duration.ofHours(2));

        filter.doFilter(request, response, chain);

        assertThat(session.isInvalid()).isTrue();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void asessionInsideTheCeilingSurvivesHoweverActiveItHasBeen() throws Exception {
        givenMaxSeconds(MAX_SECONDS);
        MockHttpSession session = sessionCreated(Duration.ofMinutes(30));

        filter.doFilter(request, response, chain);

        assertThat(session.isInvalid()).isFalse();
    }

    @Test
    void arequestWithNoSessionPassesStraightThroughWithoutCreatingOne() throws Exception {
        filter.doFilter(request, response, chain);

        assertThat(request.getSession(false)).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void asessionThatWasNeverStampedIsLeftAloneRatherThanTreatedAsInfinitelyOld() throws Exception {
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);

        filter.doFilter(request, response, chain);

        // No stamp means the session predates the metadata, not that it is expired.
        assertThat(session.isInvalid()).isFalse();
    }

    @Test
    void theChainRunsWhetherOrNotTheSessionWasInvalidated() throws Exception {
        givenMaxSeconds(1);
        sessionCreated(Duration.ofHours(1));

        filter.doFilter(request, response, chain);

        assertThat(chain.getResponse()).isSameAs(response);
    }

    private void givenMaxSeconds(int maxSeconds) {
        RealmSettings.Sessions sessions = new RealmSettings.Sessions(1800, maxSeconds, false, 0, false);
        RealmSettings realm = mock(RealmSettings.class);
        when(realm.sessions()).thenReturn(sessions);
        when(settings.current()).thenReturn(realm);
    }

    private MockHttpSession sessionCreated(Duration ago) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionMetadata.CREATED_AT, Instant.now().minus(ago).toEpochMilli());
        request.setSession(session);
        return session;
    }

}
