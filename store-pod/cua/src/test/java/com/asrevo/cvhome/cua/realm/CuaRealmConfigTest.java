package com.asrevo.cvhome.cua.realm;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;

import com.asrevo.cvhome.cua.config.StorefrontClientRepository;
import com.asrevo.cvhome.sso.realm.RealmRegistry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * cua's realm seam: one realm per store, resolved from the request.
 *
 * <p>
 * The filter runs at {@code HIGHEST_PRECEDENCE + 1} because every later filter — the session realm filter, the
 * authorization server's own chain, the client repository — reads the realm this one established. Ordered after
 * any of them, a request would be authorized against whichever realm was left over from the last one.
 * </p>
 */
class CuaRealmConfigTest {

    private final CuaRealmConfig config = new CuaRealmConfig();
    private final RealmRegistry realms = Mockito.mock(RealmRegistry.class);

    @Test
    void theRealmIsResolvedFromTheStoreRatherThanPinnedToOne() {
        // uaa's deployment uses a FixedRealmResolver; cua cannot, because it serves every store.
        assertThat(config.realmResolver()).isInstanceOf(StoreRealmResolver.class);
    }

    @Test
    void theEdgeVerifiedFilterRunsBeforeAnythingThatReadsARealm() {
        assertThat(config.edgeVerifiedRealmFilter(realms).getOrder())
                .isEqualTo(Ordered.HIGHEST_PRECEDENCE + 1);
        assertThat(config.edgeVerifiedRealmFilter(realms).getFilter())
                .isInstanceOf(EdgeVerifiedRealmFilter.class);
    }

    @Test
    void theClientRepositoryIsWrappedSoRedirectsFollowTheAskingStorefront() {
        assertThat(config.registeredClientRepository(Mockito.mock(JdbcTemplate.class), realms))
                .isInstanceOf(StorefrontClientRepository.class);
    }
}
