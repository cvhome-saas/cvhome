package com.asrevo.cvhome.cua.realm;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.realm.RealmContext;
import com.asrevo.cvhome.sso.realm.RealmRegistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** A store's realm is created by the edge vouching for it, and by nothing else. */
class EdgeVerifiedRealmFilterTest {

    private static final RealmId STORE = RealmId.of("aaaaaaaaaaaaaaaaaaaaaaaa");

    private final RealmRegistry registry = mock(RealmRegistry.class);

    private final EdgeVerifiedRealmFilter filter = new EdgeVerifiedRealmFilter(registry);

    @Test
    void aStoreTheEdgeVouchedForGetsItsRealm() throws Exception {
        MockHttpServletRequest request = login();
        request.addHeader(StoreRealmResolver.STORE_HEADER, STORE.getId());

        RealmContext.runIn(STORE, () -> doFilter(request));

        verify(registry).ensure(STORE);
    }

    /**
     * The rule that keeps this safe. Without the edge header the realm came from a query parameter or a login
     * form — whatever the caller typed — and creating a realm from one of those would let anybody mint tenants.
     */
    @Test
    void aStoreIdTheCallerSuppliedCreatesNothing() throws Exception {
        MockHttpServletRequest request = login();
        request.setParameter("client_id", STORE.getId());

        RealmContext.runIn(STORE, () -> doFilter(request));

        verify(registry, never()).ensure(any());
    }

    private static MockHttpServletRequest login() {
        return new MockHttpServletRequest("GET", "/cua/login");
    }

    private void doFilter(MockHttpServletRequest request) {
        try {
            filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
