package com.asrevo.cvhome.s2s.config.internal;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.asrevo.cvhome.errors.web.ErrorHandlingProperties;
import com.asrevo.cvhome.errors.web.ProblemDetailFactory;

import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Read-only means every unsafe method is refused for a token carrying {@code act_mode=read} — except the paths named
 * as reads that travel as {@code POST}. A token without the claim, or in write mode, is untouched.
 */
class ReadOnlyActorFilterTest {

    private static final String TOKEN = "token";

    private static final String ALG = "alg";

    private static final String NONE = "none";

    private static final String SAVE = "/api/v1/private/category";

    private static final String STATISTIC = "/api/v2/private/order-statistic";

    private static final String POST = "POST";

    private static final String READ = "read";

    private final FilterChain chain = mock(FilterChain.class);

    private final ReadOnlyActorFilter filter = new ReadOnlyActorFilter(
            new ReadOnlyActorProperties(List.of("/**/*-statistic", "/**/list")),
            new ProblemDetailFactory(new ErrorHandlingProperties(null, false)), new ObjectMapper());

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private static void principal(Map<String, Object> claims) {
        Jwt jwt = Jwt.withTokenValue(TOKEN).header(ALG, NONE).subject("merchant")
                .issuedAt(Instant.EPOCH).expiresAt(Instant.EPOCH.plusSeconds(3600))
                .claims(existing -> existing.putAll(claims)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    private MockHttpServletResponse run(String method, String path) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }

    @Test
    void aReadOnlyActorIsRefusedAwriteWithAproblemDetail() throws Exception {
        principal(Map.of(ReadOnlyActorFilter.ACT_MODE, READ));

        MockHttpServletResponse response = run(POST, SAVE);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(response.getContentAsString()).contains("COMMON.READ_ONLY_SESSION").contains(SAVE);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void aReadOnlyActorMayStillReadAndMayPostTheReadsTheListNames() throws Exception {
        principal(Map.of(ReadOnlyActorFilter.ACT_MODE, READ));

        assertThat(run("GET", SAVE).getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(run(POST, STATISTIC).getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(run(POST, "/api/v1/store-manager/list").getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(chain, org.mockito.Mockito.times(3)).doFilter(any(), any());
    }

    @Test
    void writeModeAndPlainTokensAreUntouched() throws Exception {
        principal(Map.of(ReadOnlyActorFilter.ACT_MODE, "write"));
        assertThat(run(POST, SAVE).getStatus()).isEqualTo(HttpStatus.OK.value());

        principal(Map.of());
        assertThat(run("DELETE", SAVE).getStatus()).isEqualTo(HttpStatus.OK.value());

        SecurityContextHolder.clearContext();
        assertThat(run(POST, SAVE).getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void theContextPathIsStrippedBeforeMatching() throws Exception {
        principal(Map.of(ReadOnlyActorFilter.ACT_MODE, READ));
        MockHttpServletRequest request = new MockHttpServletRequest(POST, "/uaa/api/v1/admin/users/list");
        request.setContextPath("/uaa");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

}
