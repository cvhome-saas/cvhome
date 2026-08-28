package com.asrevo.cvhome.podregistry.service;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.commons.domain.EndpointType;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.commons.PodHealthStatus;
import com.asrevo.cvhome.podregistry.domain.PodEntity;
import com.asrevo.cvhome.podregistry.domain.PodHealthCheckEntity;
import com.asrevo.cvhome.podregistry.repository.PodHealthCheckRepository;
import com.asrevo.cvhome.podregistry.repository.PodRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * The probe is a reachability check, not a health check: any HTTP answer is GREEN, only a transport failure is RED.
 * A pod that answers 404 must not be pulled from placement, because 404 proves its edge is up.
 */
class PodHealthProbeTest {

    private static final String ENDPOINT = "http://spg-a.example";

    private static final String OTHER_ENDPOINT = "http://spg-b.example";

    private PodRepository podRepository;

    private PodHealthCheckRepository healthCheckRepository;

    private MockRestServiceServer server;

    private PodHealthProbe probe;

    private static PodEntity pod(String endpoint, PodHealthStatus lastStatus) {
        PodEntity pod = new PodEntity();
        pod.setId(PodId.newId());
        pod.setName(endpoint);
        pod.setEndpoint(endpoint);
        pod.setEndpointType(EndpointType.EXTERNAL);
        pod.setLastHealthStatus(lastStatus);
        return pod;
    }

    @BeforeEach
    void setUp() {
        podRepository = mock(PodRepository.class);
        healthCheckRepository = mock(PodHealthCheckRepository.class);
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        probe = new PodHealthProbe(podRepository, healthCheckRepository, builder.build());
    }

    private PodHealthCheckEntity recorded() {
        ArgumentCaptor<PodHealthCheckEntity> captor = ArgumentCaptor.forClass(PodHealthCheckEntity.class);
        verify(healthCheckRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void anOkAnswerIsGreenWithNoDetail() {
        server.expect(requestTo(ENDPOINT)).andExpect(method(HttpMethod.GET)).andRespond(withSuccess());
        PodEntity pod = pod(ENDPOINT, PodHealthStatus.RED);

        probe.probe(pod);

        server.verify();
        assertThat(pod.getLastHealthStatus()).isEqualTo(PodHealthStatus.GREEN);
        assertThat(pod.getLastHealthAt()).isNotNull();
        verify(podRepository).save(pod);
        PodHealthCheckEntity check = recorded();
        assertThat(check.getPodId()).isEqualTo(pod.getId());
        assertThat(check.getStatus()).isEqualTo(PodHealthStatus.GREEN);
        assertThat(check.getDetail()).isNull();
        assertThat(check.getLatencyMs()).isNotNegative();
        assertThat(check.getCheckedAt()).isNotNull();
    }

    @Test
    @DisplayName("an error status still proves the pod's edge is up, so it is GREEN with the status as detail")
    void anErrorStatusIsStillGreen() {
        server.expect(requestTo(ENDPOINT)).andRespond(withStatus(HttpStatus.NOT_FOUND));
        PodEntity pod = pod(ENDPOINT, PodHealthStatus.GREEN);

        probe.probe(pod);

        assertThat(pod.getLastHealthStatus()).isEqualTo(PodHealthStatus.GREEN);
        PodHealthCheckEntity check = recorded();
        assertThat(check.getStatus()).isEqualTo(PodHealthStatus.GREEN);
        assertThat(check.getDetail()).contains("404");
    }

    @Test
    void aTransportFailureIsRedNamingTheFailure() {
        server.expect(requestTo(ENDPOINT)).andRespond(withException(new IOException("refused")));
        PodEntity pod = pod(ENDPOINT, null);

        probe.probe(pod);

        assertThat(pod.getLastHealthStatus()).isEqualTo(PodHealthStatus.RED);
        PodHealthCheckEntity check = recorded();
        assertThat(check.getStatus()).isEqualTo(PodHealthStatus.RED);
        assertThat(check.getDetail()).isNotBlank();
    }

    @Test
    void theSweepProbesEveryPodAndOneFailureDoesNotStopTheRest() {
        PodEntity down = pod(ENDPOINT, PodHealthStatus.GREEN);
        PodEntity up = pod(OTHER_ENDPOINT, PodHealthStatus.GREEN);
        when(podRepository.findAll()).thenReturn(List.of(down, up));
        server.expect(requestTo(ENDPOINT)).andRespond(withException(new IOException("timeout")));
        server.expect(requestTo(OTHER_ENDPOINT)).andRespond(withSuccess());

        probe.probeAll();

        server.verify();
        assertThat(down.getLastHealthStatus()).isEqualTo(PodHealthStatus.RED);
        assertThat(up.getLastHealthStatus()).isEqualTo(PodHealthStatus.GREEN);
        verify(podRepository).save(down);
        verify(podRepository).save(up);
    }

}
