package com.asrevo.cvhome.merchant.api.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.api.MerchantStoreOrgOwner;
import com.asrevo.cvhome.s2s.utils.WebClientsUtils;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Merchant's own store client, and the org-owner lookup built on it, against merchant's running controller.
 *
 * <p>
 * {@link MerchantStoreOrgOwner} is how every other service answers "which organization owns this store", and it is
 * consulted on authorization paths — so its two quiet fallbacks matter more than its happy path. A store with no
 * org, and a merchant service that cannot be reached, both have to yield {@code null} rather than throw: the
 * caller treats "unknown owner" as "not an org admin", and an exception here would turn a merchant outage into a
 * 500 on every request that asks the question.
 * </p>
 *
 * <p>
 * The answer is cached for thirty minutes, so the lookup is also asserted to survive a second ask — a cache that
 * stored {@code null} as a positive answer, or missed on every call, would each be a different production
 * problem.
 * </p>
 */
@StorageIntegrationTest
@TestPropertySource(properties = {
        "com.asrevo.cvhome.pod-info.pod.name=pod-507f1f77",
        "com.asrevo.cvhome.pod-info.pod.domain=spg-507f1f77.gateway.com"})
class MerchantStoreClientContractIntegrationTest {

    private static final String LOCALHOST = "http://localhost:%d";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private ExternalMerchantStoreService stores;

    @BeforeEach
    void setUp() {
        stores = clientFor(port);
    }

    @Test
    void theStoreReadPathMatchesTheControllerItNames() {
        var store = stores.getStore(new StoreMerchantId(Tokens.STORE_1));

        assertThat(store).isNotNull();
        assertThat(store.getId()).isNotBlank();
    }

    @Test
    void thelanguageOverloadIsTheSameCallBecauseTheStoreReadIsNotLocalised() {
        StoreMerchantId store = new StoreMerchantId(Tokens.STORE_1);

        assertThat(stores.getStore(store, new LanguageCode("en")).getId())
                .isEqualTo(stores.getStore(store).getId());
    }

    @Test
    void theOrgOwnerLookupAnswersTheOrganizationThatOwnsTheStore() {
        MerchantStoreOrgOwner owners = new MerchantStoreOrgOwner(stores);

        ManagerOrgId owner = owners.owner(new StoreMerchantId(Tokens.STORE_1));

        assertThat(owner).isNotNull();
        // Asked twice: the second answer comes from the cache and has to agree with the first.
        assertThat(owners.owner(new StoreMerchantId(Tokens.STORE_1))).isEqualTo(owner);
    }

    @Test
    void anullStoreIsAnullOwnerRatherThanAlookup() {
        assertThat(new MerchantStoreOrgOwner(stores).owner(null)).isNull();
    }

    /**
     * A merchant service that cannot be reached yields "owner unknown", not an exception. Callers read that as
     * "not an org admin"; throwing would turn a merchant outage into a 500 on every authorization check.
     */
    @Test
    void amerchantServiceThatCannotBeReachedYieldsNoOwnerRatherThanThrowing() {
        MerchantStoreOrgOwner owners = new MerchantStoreOrgOwner(clientFor(deadPort()));

        assertThat(owners.owner(new StoreMerchantId(Tokens.STORE_1))).isNull();
    }

    @Test
    void astoreThatDoesNotExistYieldsNoOwner() {
        MerchantStoreOrgOwner owners = new MerchantStoreOrgOwner(stores);

        assertThat(owners.owner(new StoreMerchantId("65f023632bc46470c104b999"))).isNull();
    }

    private ExternalMerchantStoreService clientFor(int somePort) {
        String token = new Tokens(signer).s2s(Tokens.SCOPE_STORE_CORE);
        RestClient.Builder http = RestClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token));
        return WebClientsUtils.build(http, String.format(LOCALHOST, somePort),
                ExternalMerchantStoreService.class, null);
    }

    /** A port nothing is listening on. */
    private static int deadPort() {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
