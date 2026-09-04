package com.asrevo.cvhome.sso.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Making sure the {@code XSRF-TOKEN} cookie is written on every response.
 *
 * <p>
 * {@code CookieCsrfTokenRepository} only writes the cookie when something reads the token, and a single-page app's
 * first request is for {@code index.html}, which reads nothing — so without this touch the console's first
 * {@code fetch} and the sign-in form's first post go out with no cookie to copy into {@code X-XSRF-TOKEN}, and are
 * refused. Reading the token is the entire point, so that is what is asserted.
 * </p>
 */
class CsrfCookieFilterTest {
    private static final String THE_VALUE = "the-value";

    private final CsrfCookieFilter filter = new CsrfCookieFilter();
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final MockFilterChain chain = new MockFilterChain();

    @Test
    void thetokenIsReadSoTheRepositoryWritesTheCookie() throws Exception {
        CsrfToken token = mock(CsrfToken.class);
        when(token.getToken()).thenReturn("token-value");
        request.setAttribute(CsrfToken.class.getName(), token);

        filter.doFilter(request, response, chain);

        // Reading is what makes the deferred repository emit Set-Cookie.
        verify(token).getToken();
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void arequestWithNoTokenAttributePassesThroughUntouched() throws Exception {
        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
        assertThat(chain.getResponse()).isSameAs(response);
    }

    @Test
    void arealTokenIsNotAlteredOnTheWayThrough() throws Exception {
        CsrfToken token = new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", THE_VALUE);
        request.setAttribute(CsrfToken.class.getName(), token);

        filter.doFilter(request, response, chain);

        assertThat(token.getToken()).isEqualTo(THE_VALUE);
    }

}
