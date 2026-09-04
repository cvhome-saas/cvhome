package com.asrevo.cvhome.sso.realm;


import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.web.ProblemDetailFactory;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Entering the request's realm before anything else runs.
 *
 * <p>
 * Three behaviours, each of which has a failure mode worth naming. The realm is <em>left</em> in a
 * {@code finally}, so a request that throws does not leave its realm on a pooled thread for the next request to
 * inherit — which would serve one tenant's data to another.
 * </p>
 *
 * <p>
 * A request the resolver places in no realm proceeds with none entered rather than a default. The JWKS document
 * and the discovery endpoint are identical for every realm; entering a default would make anything realm-scoped
 * that reached them answer quietly for the wrong tenant instead of failing loudly.
 * </p>
 *
 * <p>
 * And a refusal renders its own problem body, because a filter throws outside the reach of the
 * {@code @ControllerAdvice} — without it, a cross-store request reached the client as a bare 500.
 * </p>
 */
class RealmFilterTest {

    private static final RealmId REALM = RealmId.of("store-1");
    private static final String REFUSED = "No such store.";

    private final RealmResolver resolver = mock(RealmResolver.class);
    private final ProblemDetailFactory problems = mock(ProblemDetailFactory.class);
    private final RealmFilter filter = new RealmFilter(resolver, problems, JsonMapper.builder().build());

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    void therealmIsEnteredForTheDurationOfTheRequest() throws Exception {
        when(resolver.resolve(request)).thenReturn(REALM);
        java.util.Optional<RealmId>[] seen = asArray();
        FilterChain chain = (req, res) -> seen[0] = RealmContext.current();

        filter.doFilter(request, response, chain);

        assertThat(seen[0]).contains(REALM);
    }

    @Test
    void therealmIsLeftEvenWhenTheRequestThrows() {
        when(resolver.resolve(request)).thenReturn(REALM);
        FilterChain chain = (req, res) -> {
            throw new IllegalStateException("boom");
        };

        try {
            filter.doFilter(request, response, chain);
        } catch (Exception expected) {
            // The chain's failure is not what is under test.
        }

        // Left behind on a pooled thread, this realm would be inherited by the next request on it.
        assertThat(RealmContext.current()).isEmpty();
    }

    @Test
    void arequestInNoRealmProceedsWithNoneEnteredRatherThanAdefault() throws Exception {
        when(resolver.resolve(request)).thenReturn(null);
        java.util.Optional<RealmId>[] seen = asArray();
        seen[0] = java.util.Optional.of(REALM);
        FilterChain chain = (req, res) -> seen[0] = RealmContext.current();

        filter.doFilter(request, response, chain);

        // JWKS and discovery are identical for every realm; a default would answer quietly for the wrong one.
        assertThat(seen[0]).isEmpty();
    }

    @Test
    void arefusalRendersItsOwnProblemBodyBecauseAfilterIsOutsideTheControllerAdvice() throws Exception {
        givenAproblem();
        when(resolver.resolve(request))
                .thenThrow(new RealmResolutionException(CommonErrors.RESOURCE_NOT_FOUND, REFUSED));
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(CommonErrors.RESOURCE_NOT_FOUND.category().httpStatus());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(response.getContentAsString()).contains(REFUSED);
        // The chain never runs: a request in no realm must not reach a controller.
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void therefusalCarriesTheCodeTheResolverChose() throws Exception {
        givenAproblem();
        when(resolver.resolve(request))
                .thenThrow(new RealmResolutionException(CommonErrors.ACCESS_DENIED, REFUSED));

        filter.doFilter(request, response, new MockFilterChain());

        verify(problems).create(eq(CommonErrors.ACCESS_DENIED), anyString(), anyMap(), anyList(), any());
        assertThat(response.getStatus()).isEqualTo(CommonErrors.ACCESS_DENIED.category().httpStatus());
    }

    @SuppressWarnings("unchecked")
    private static java.util.Optional<RealmId>[] asArray() {
        return new java.util.Optional[] {java.util.Optional.empty()};
    }

    private void givenAproblem() {
        when(problems.traceId()).thenReturn("trace-1");
        when(problems.create(any(com.asrevo.cvhome.errors.ErrorCode.class), anyString(), anyMap(), anyList(), any()))
                .thenAnswer(invocation -> ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                        invocation.getArgument(1)));
    }

}
