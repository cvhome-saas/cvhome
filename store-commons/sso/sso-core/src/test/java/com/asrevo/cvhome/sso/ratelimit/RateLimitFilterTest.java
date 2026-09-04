package com.asrevo.cvhome.sso.ratelimit;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.errors.web.ProblemDetailFactory;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.realm.RealmContext;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * One deployment serves every store on the pod, so an attempt has to be counted against the store it was aimed at
 * as well as against the address it came from.
 */
class RateLimitFilterTest {

    private static final int LIMIT = 2;

    private static final int SPREAD = 3;

    private static final String ADDRESS = "10.0.0.1";

    private static final String LOGIN = "/login";

    private static final RealmId STORE_A = RealmId.of("65f023632bc46470c104b76f");

    private static final RealmId STORE_B = RealmId.of("65f023632bc46470c104b75f");

    private final ProblemDetailFactory problems = mock(ProblemDetailFactory.class);

    private final RateLimitFilter filter = new RateLimitFilter(
            new RateLimitProperties(true, new RateLimitProperties.Rule(LIMIT, Duration.ofMinutes(1)), null, null,
                    SPREAD),
            problems, JsonMapper.builder().build(), mock(AuditService.class));

    RateLimitFilterTest() {
        when(problems.create(any(), any(), any(), any(), any()))
                .thenReturn(ProblemDetail.forStatus(429));
    }

    @Test
    void aStoresBudgetIsItsOwn() throws Exception {
        assertThat(post(STORE_A)).isEqualTo(200);
        assertThat(post(STORE_A)).isEqualTo(200);
        assertThat(post(STORE_A)).as("store A has spent its budget").isEqualTo(429);

        assertThat(post(STORE_B)).as("store B's shoppers are not refused for it").isEqualTo(200);
    }

    /** And the address is still counted on its own, so spreading the same burst across stores does not evade it. */
    @Test
    void anAddressSprayingEveryStoreIsStillCaught() throws Exception {
        for (int realm = 0; realm < SPREAD; realm++) {
            RealmId store = RealmId.of(String.format("realm-%d", realm));
            assertThat(post(store)).isEqualTo(200);
            assertThat(post(store)).isEqualTo(200);
        }

        assertThat(post(RealmId.of("realm-fresh"))).as("the address has spent %d attempts", LIMIT * SPREAD)
                .isEqualTo(429);
    }

    private int post(RealmId realm) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", LOGIN);
        request.setServletPath(LOGIN);
        request.setRemoteAddr(ADDRESS);
        MockHttpServletResponse response = new MockHttpServletResponse();
        RealmContext.runIn(realm, () -> {
            try {
                filter.doFilter(request, response, new MockFilterChain());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        return response.getStatus();
    }

}
