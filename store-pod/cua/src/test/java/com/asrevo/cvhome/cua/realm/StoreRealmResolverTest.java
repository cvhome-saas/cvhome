package com.asrevo.cvhome.cua.realm;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.asrevo.cvhome.commons.domain.RealmId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Which store a request belongs to: the host decides, and a request that disagrees with it is refused. */
class StoreRealmResolverTest {

    private static final String STORE_A = "aaaaaaaaaaaaaaaaaaaaaaaa";

    private static final String STORE_B = "bbbbbbbbbbbbbbbbbbbbbbbb";

    private static final String LOGIN = "/cua/login";

    private static final String CLIENT_ID = "client_id";

    private final StoreRealmResolver resolver = new StoreRealmResolver();

    @Test
    void theEdgeHeaderIsTheRealm() {
        MockHttpServletRequest request = get("/cua/oauth2/authorize");
        request.addHeader(StoreRealmResolver.STORE_HEADER, STORE_A);

        assertThat(resolver.resolve(request)).isEqualTo(RealmId.of(STORE_A));
    }

    /** Not every caller arrives through the edge, and the JSON APIs are addressed by ?store= across the repo. */
    @Test
    void withoutTheEdgeHeaderTheRequestMaySayWhichStore() {
        MockHttpServletRequest request = get("/cua/api/v1/public/registration");
        request.setParameter("store", STORE_A);

        assertThat(resolver.resolve(request)).isEqualTo(RealmId.of(STORE_A));
    }

    @Test
    void theFormsClientIdIsReadWhenNothingElseSaysWhichStore() {
        MockHttpServletRequest request = get(LOGIN);
        request.setParameter(CLIENT_ID, STORE_A);

        assertThat(resolver.resolve(request)).isEqualTo(RealmId.of(STORE_A));
    }

    /**
     * The point of the change. cua used to take the tenant straight out of the login form, so a form on one
     * store's page could name another. Where the edge has spoken, a request that disagrees is refused rather than
     * resolved to either one — quietly picking a winner is what would make the attempt work.
     */
    @Test
    void aRequestThatNamesAnotherStoreThanItsHostIsRefused() {
        MockHttpServletRequest request = get(LOGIN);
        request.addHeader(StoreRealmResolver.STORE_HEADER, STORE_A);
        request.setParameter(CLIENT_ID, STORE_B);

        assertThatThrownBy(() -> resolver.resolve(request))
                .isInstanceOf(CrossStoreRequestException.class)
                .hasMessageContaining(STORE_A)
                .hasMessageContaining(STORE_B);
    }

    @Test
    void agreeingIsFine() {
        MockHttpServletRequest request = get(LOGIN);
        request.addHeader(StoreRealmResolver.STORE_HEADER, STORE_A);
        request.setParameter(CLIENT_ID, STORE_A);

        assertThat(resolver.resolve(request)).isEqualTo(RealmId.of(STORE_A));
    }

    /** One document per pod, identical for every store on it; nothing realm-scoped may run under a guess. */
    @Test
    void theJwksAndDiscoveryDocumentsBelongToNoRealm() {
        assertThat(resolver.resolve(get("/cua/oauth2/jwks"))).isNull();
        assertThat(resolver.resolve(get("/cua/.well-known/openid-configuration"))).isNull();
        assertThat(resolver.resolve(get("/cua/actuator/health"))).isNull();
    }

    @Test
    void aRequestThatNamesNoStoreAtAllHasNoRealm() {
        assertThat(resolver.resolve(get("/cua/oauth2/token"))).isNull();
    }

    private static MockHttpServletRequest get(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRequestURI(uri);
        return request;
    }

}
