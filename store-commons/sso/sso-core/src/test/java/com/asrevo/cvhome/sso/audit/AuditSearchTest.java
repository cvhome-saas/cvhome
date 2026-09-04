package com.asrevo.cvhome.sso.audit;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.sso.dto.AuditQueryParams;

import static org.assertj.core.api.Assertions.assertThat;

/** The query string becomes a search: nulls become empty lists, not null lists. */
class AuditSearchTest {

    private static final String LOGIN = "user.login";

    private static final String IP = "10.0.0.1";

    private static final String TEXT = "reset";

    @Test
    void emptyQueryStringIsAnEmptySearch() {
        AuditSearch search = new AuditQueryParams(null, null, null, null, null, null, null, null, null, null).toSearch();
        assertThat(search.types()).isEmpty();
        assertThat(search.categories()).isEmpty();
        assertThat(search.actor()).isNull();
    }

    @Test
    void carriesEveryFilter() {
        AuditSearch search = new AuditQueryParams(List.of(LOGIN),
                List.of(AuditEventType.AuditCategory.SECURITY), "grace", "org1-admin", "web-app",
                AuditOutcome.FAILURE, IP, TEXT, null, null).toSearch();
        assertThat(search.types()).containsExactly(LOGIN);
        assertThat(search.categories()).containsExactly(AuditEventType.AuditCategory.SECURITY);
        assertThat(search.outcome()).isEqualTo(AuditOutcome.FAILURE);
        assertThat(search.ip()).isEqualTo(IP);
        assertThat(search.q()).isEqualTo(TEXT);
    }

    @Test
    void noneIsEmpty() {
        assertThat(AuditSearch.none().types()).isEmpty();
        assertThat(AuditSearch.none().to()).isNull();
    }

}
