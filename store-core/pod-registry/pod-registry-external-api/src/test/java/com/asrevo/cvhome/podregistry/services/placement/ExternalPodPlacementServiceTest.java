package com.asrevo.cvhome.podregistry.services.placement;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementDecision;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementRequest;
import com.asrevo.cvhome.podregistry.commons.dto.RecordPlacementRequest;
import com.asrevo.cvhome.podregistry.services.pod.ExternalPodService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * The {@code @HttpExchange} paths are checked against the controller's mapping by nobody but this test: every
 * method must hit the URL {@code PodPlacementApi} / {@code PodApi} actually serve, with the body they parse.
 */
class ExternalPodPlacementServiceTest {

    private static final String BASE = "http://pod-registry";

    private static final String ORG = "21f023932bc66470c104b76f";

    private static final String POD = "507f1f77bcf86cd799439011";

    private static final String STORE = "65f023632bc46470c104b76f";

    private static final String OTHER_STORE = "65f023632bc46470c104b75f";

    private static final String REASON = "requested private pod";

    private static final String SPG = "http://spg";

    private MockRestServiceServer server;

    private ExternalPodPlacementService placement;

    private ExternalPodService pods;

    private static String url(String path) {
        return String.format("%s%s", BASE, path);
    }

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        server = MockRestServiceServer.bindTo(builder).build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(builder.build())).build();
        placement = factory.createClient(ExternalPodPlacementService.class);
        pods = factory.createClient(ExternalPodService.class);
    }

    @Test
    void placePostsTheOrgAndReadsTheDecision() throws Exception {
        server.expect(requestTo(url("/api/v1/pod/private/placement")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(ORG)))
                .andExpect(content().string(containsString(POD)))
                .andRespond(withSuccess(String.format(
                        "{\"podId\":\"%s\",\"dedicated\":true,\"reason\":\"%s\"}", POD, REASON),
                        MediaType.APPLICATION_JSON));

        PlacementDecision decision = placement.place(new PlacementRequest(new ManagerOrgId(ORG), new PodId(POD)));

        server.verify();
        assertThat(decision.podId()).isEqualTo(new PodId(POD));
        assertThat(decision.dedicated()).isTrue();
        assertThat(decision.reason()).isEqualTo(REASON);
    }

    @Test
    void recordPlacementPostsTheStoreAndPod() throws Exception {
        server.expect(requestTo(url("/api/v1/pod/private/placement-recorded")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(containsString(STORE)))
                .andExpect(content().string(containsString(POD)))
                .andRespond(withSuccess());

        placement.recordPlacement(new RecordPlacementRequest(new StoreMerchantId(STORE), new PodId(POD)));

        server.verify();
    }

    @Test
    void recordPlacementsPostsTheWholeList() throws Exception {
        server.expect(requestTo(url("/api/v1/pod/private/placements-recorded")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(content().string(containsString(OTHER_STORE)))
                .andRespond(withSuccess());

        placement.recordPlacements(List.of(
                new RecordPlacementRequest(new StoreMerchantId(STORE), new PodId(POD)),
                new RecordPlacementRequest(new StoreMerchantId(OTHER_STORE), new PodId(POD))));

        server.verify();
    }

    @Test
    void listPodsReadsTheRoutingLevelPod() throws Exception {
        server.expect(requestTo(url("/api/v1/pod/list")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(String.format(
                        "[{\"id\":\"%s\",\"name\":\"pod-a\",\"endpoint\":{\"endpoint\":\"%s\",\"type\":\"EXTERNAL\"}}]",
                        POD, SPG), MediaType.APPLICATION_JSON));

        List<Pod> listed = pods.listPods();

        server.verify();
        assertThat(listed).hasSize(1);
        assertThat(listed.getFirst().id()).isEqualTo(new PodId(POD));
        assertThat(listed.getFirst().endpoint().endpoint()).isEqualTo(SPG);
    }

}
