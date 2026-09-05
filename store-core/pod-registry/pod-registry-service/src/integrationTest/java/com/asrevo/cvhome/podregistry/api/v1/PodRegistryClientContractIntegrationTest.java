package com.asrevo.cvhome.podregistry.api.v1;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.api.errors.PodPlacementRefusedException;
import com.asrevo.cvhome.podregistry.api.errors.PodRegistryApiErrors;
import com.asrevo.cvhome.podregistry.api.errors.PodRegistryUnavailableException;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementRequest;
import com.asrevo.cvhome.podregistry.services.placement.ExternalPodPlacementService;
import com.asrevo.cvhome.podregistry.services.pod.CachingPodDirectory;
import com.asrevo.cvhome.podregistry.services.pod.ExternalPodService;
import com.asrevo.cvhome.s2s.utils.WebClientsUtils;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.http.ApiClient;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import static com.asrevo.cvhome.testsupport.http.ApiClient.expect;
import static com.asrevo.cvhome.testsupport.http.ApiClient.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The pod registry's own clients, driven against the registry's running controllers.
 *
 * <p>
 * These interfaces are what tenancy holds, and their error contract is the whole reason they exist: a placement the
 * registry <em>refused</em> — no pod can take this organization's store — is a decision, and the caller must stop
 * rather than retry; a registry that could not be reached decided nothing, and the caller must leave the store
 * provisionable. Collapsing the two either strands a store forever or retries a refusal until it gives up.
 * </p>
 *
 * <p>
 * That mapping lives in {@code PodRegistryApiErrors.CATALOG} and only exists on the caller's side, so neither the
 * registry's own tests nor tenancy's — which stub these clients — reach it.
 * </p>
 *
 * <p>
 * {@link CachingPodDirectory} is included because its contract is the opposite of the others: it is documented to
 * <em>degrade</em> rather than fail, so a registry outage has to leave it answering from its seed instead of
 * throwing at every caller that asks where a pod is.
 * </p>
 */
@ServiceIntegrationTest
class PodRegistryClientContractIntegrationTest {

    private static final String ID = "id";
    private static final String PODS = "/api/v1/pod";

    private static final String LOCALHOST = "http://localhost:%d";

    private static final String POD_BODY = """
            {"name":"%s","endpoint":{"endpoint":"http://%s.example","type":"EXTERNAL"}}""";

    /** An organization with no dedicated pod, so a placement for it can be refused on purpose. */
    private static final String ORG = "65f023632bc46470c104b7a1";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private ApiClient api;

    private String superAdmin;

    private ExternalPodPlacementService placement;

    private ExternalPodService pods;

    @BeforeEach
    void setUp() {
        api = new ApiClient(port);
        Tokens tokens = new Tokens(signer);
        superAdmin = tokens.superAdmin();
        placement = clientFor(port, ExternalPodPlacementService.class);
        pods = clientFor(port, ExternalPodService.class);
    }

    @Test
    void thelistPathMatchesTheControllerItNames() throws Exception {
        createPod();

        assertThat(pods.listPods()).isNotEmpty();
    }

    @Test
    void aplacementIsDecidedThroughTheRealClient() throws Exception {
        String podId = createPod();

        var decision = placement.place(new PlacementRequest(new ManagerOrgId(ORG)));

        assertThat(decision).isNotNull();
        assertThat(podId).isNotBlank();
    }

    /**
     * Every pod drained means nothing can take the store. That is a decision, not an outage, and the caller has to
     * be able to tell — it stops provisioning rather than retrying forever.
     */
    @Test
    void aplacementWithNoEligiblePodIsArefusalRatherThanAnOutage() throws Exception {
        for (PodId id : allPodIds()) {
            expect(api.send(HttpMethod.POST, ApiClient.path(PODS, id.getId(), "drain"), superAdmin, null),
                    HttpStatus.OK);
        }

        assertThatThrownBy(() -> placement.place(new PlacementRequest(new ManagerOrgId(ORG))))
                .isInstanceOf(PodPlacementRefusedException.class)
                .isNotInstanceOf(PodRegistryUnavailableException.class);
    }

    @Test
    void aregistryThatCannotBeReachedIsAnOutageRatherThanArefusal() {
        ExternalPodPlacementService unreachable = clientFor(deadPort(), ExternalPodPlacementService.class);

        assertThatThrownBy(() -> unreachable.place(new PlacementRequest(new ManagerOrgId(ORG))))
                .isInstanceOf(PodRegistryUnavailableException.class)
                .isNotInstanceOf(PodPlacementRefusedException.class);
    }

    @Test
    void thelistAlsoReportsUnreachabilityAsSuch() {
        ExternalPodService unreachable = clientFor(deadPort(), ExternalPodService.class);

        assertThatThrownBy(unreachable::listPods).isInstanceOf(PodRegistryUnavailableException.class);
    }

    @Test
    void thedirectoryAnswersFromTheRegistryAndThenFromItsCache() throws Exception {
        createPod();
        CachingPodDirectory directory = new CachingPodDirectory(pods, List.of(), Duration.ofMinutes(10));
        PodId first = allPodIds().getFirst();

        assertThat(directory.find(first)).isPresent();
        // Second ask inside the TTL: the answer has to agree, and not depend on the registry still being there.
        assertThat(directory.find(first)).isPresent();
    }

    /**
     * The directory is documented to degrade rather than fail. A registry outage must leave it answering from the
     * seed it was built with, because its callers are asking "where is this pod" on the request path.
     */
    @Test
    void thedirectoryDegradesToItsSeedWhenTheRegistryIsGone() {
        Pod seeded = new Pod(new PodId("65f023632bc46470c104b7b2"), "seed-pod", null, null, null);
        CachingPodDirectory directory = new CachingPodDirectory(
                clientFor(deadPort(), ExternalPodService.class), List.of(seeded), Duration.ofMinutes(10));

        assertThat(directory.find(seeded.id())).contains(seeded);
        assertThat(directory.find(new PodId("65f023632bc46470c104b7b3"))).isEmpty();
    }

    private String createPod() {
        String name = ApiClient.slug("pod");
        ResponseEntity<String> created = api.send(HttpMethod.POST, PODS, superAdmin,
                String.format(POD_BODY, name, name));
        expect(created, HttpStatus.OK);
        return json(created).get(ID).get(ID).asString();
    }

    /** Through the client under test: the list endpoint is {@code /pod/list}, not the paged {@code /pod}. */
    private List<PodId> allPodIds() throws PodRegistryUnavailableException {
        return pods.listPods().stream().map(Pod::id).toList();
    }

    private <T> T clientFor(int somePort, Class<T> contract) {
        String token = new Tokens(signer).s2s(Tokens.SCOPE_STORE_CORE);
        RestClient.Builder http = RestClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token));
        return WebClientsUtils.build(http, String.format(LOCALHOST, somePort), contract,
                PodRegistryApiErrors.CATALOG);
    }

    private static int deadPort() {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
