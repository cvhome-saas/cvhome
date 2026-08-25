package com.asrevo.cvhome.tenancy.manager.controller;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.services.pod.CachingPodDirectory;
import com.asrevo.cvhome.tenancy.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_A;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_B;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_SUSPENDED;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.POD_ID;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.SUSPENDED_ORG_STORE;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.SUSPENDED_STORE;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.expect;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.json;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Which pod hosts a store — the call the console makes to enter one.
 *
 * <p>
 * Guarded twice, and the second guard is the one that holds: the permission gate admits any org admin to any store,
 * because the shared {@code isOrgAdmin} ignores the store it is asked about, so the org check inside
 * {@code InternalStoreService.getStorePod} is what keeps one tenant out of another's routing. This is also where
 * "suspended" has to bite — reading a suspended store's own record stays allowed, or the console could not show why
 * it is closed, but entering it must not be.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class RouterApiIntegrationTest {

    private static final String ROUTE = "/api/v1/router/store-pod-by-store-id";

    private static final String STORE_PARAM = "store";

    private static final String STORE = Tokens.STORE_1;

    private static final String UNKNOWN_STORE = "0000000000000000000000ff";

    private static final String POD_NAME = "pod-1";

    private static final String POD_DOMAIN = "pod1.example.com";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private CachingPodDirectory podDirectory;

    private TenancyApiTestSupport api;

    @BeforeEach
    void setUp() {
        api = new TenancyApiTestSupport(port, signer);
        Mockito.reset(podDirectory);
    }

    private void registryKnowsThePod() {
        when(podDirectory.find(any()))
                .thenReturn(Optional.of(new Pod(new PodId(POD_ID), POD_NAME, null, null, POD_DOMAIN)));
    }

    private static String forStore(String store) {
        return with(ROUTE, STORE_PARAM, store);
    }

    @Test
    void aStoresOwnAdminIsToldWhichPodHostsIt() {
        registryKnowsThePod();

        JsonNode pod = json(api.get(forStore(STORE), api.storeAdmin(ORG_A, STORE)));

        assertThat(pod.get("name").asString()).isEqualTo(POD_NAME);
        assertThat(pod.get("domain").asString()).isEqualTo(POD_DOMAIN);
    }

    /** A store of another organization is a 404, not a 403 — the id must not be confirmed by the status code. */
    @Test
    void aSecondOrganizationsAdminCannotRouteToTheFirstsStore() {
        registryKnowsThePod();

        expect(api.get(forStore(STORE), api.orgAdmin(ORG_B)), HttpStatus.NOT_FOUND);
    }

    @Test
    void aStoreThatDoesNotExistIsAFourOhFour() {
        registryKnowsThePod();

        expect(api.get(forStore(UNKNOWN_STORE), api.superAdmin()), HttpStatus.NOT_FOUND);
    }

    @Test
    void aSuspendedStoreCannotBeEntered() {
        registryKnowsThePod();

        expect(api.get(forStore(SUSPENDED_STORE), api.superAdmin()), HttpStatus.UNPROCESSABLE_CONTENT);
    }

    /**
     * Suspending an organization closes its stores without writing to any of them, so an ACTIVE store owned by a
     * SUSPENDED organization has to be refused here too.
     */
    @Test
    void aStoreOfASuspendedOrganizationCannotBeEntered() {
        registryKnowsThePod();

        expect(api.get(forStore(SUSPENDED_ORG_STORE), api.orgAdmin(ORG_SUSPENDED)),
                HttpStatus.UNPROCESSABLE_CONTENT);
    }

    /**
     * The binding outlived the pod. A real inconsistency, so it is an error rather than the {@code null} body this
     * used to answer with through {@code PodRepository.orElse(null)}.
     */
    @Test
    void aStoreBoundToAPodTheRegistryDoesNotKnowIsAnError() {
        when(podDirectory.find(any())).thenReturn(Optional.empty());

        expect(api.get(forStore(STORE), api.superAdmin()), HttpStatus.NOT_FOUND);
    }

    @Test
    void anUnauthenticatedCallerIsRefused() {
        expect(api.get(forStore(STORE), null), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void aCustomerMayNotAskWhereAStoreLives() {
        expect(api.get(forStore(STORE), api.customer(ORG_A, STORE)), HttpStatus.FORBIDDEN);
    }

}
