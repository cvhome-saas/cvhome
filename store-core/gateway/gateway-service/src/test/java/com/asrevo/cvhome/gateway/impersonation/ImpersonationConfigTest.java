package com.asrevo.cvhome.gateway.impersonation;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.mock.env.MockEnvironment;

import com.asrevo.cvhome.s2s.config.internal.ServiceUrlBuilder;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** The beans are built as declared: a UTC clock, the shared URL resolution, and two plain clients. */
class ImpersonationConfigTest {

    private final ImpersonationConfig config = new ImpersonationConfig();

    @Test
    void theClockIsUtc() {
        assertThat(config.impersonationClock().getZone()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    void theUrlBuilderIsTheSharedResolutionOverTheSameProperties() {
        ServiceDomainProperties services = new ServiceDomainProperties(Map.of(), List.of());
        MockEnvironment environment = new MockEnvironment();

        assertThat(config.impersonationUrls(services, environment)).isEqualTo(new ServiceUrlBuilder(services, environment));
    }

    @Test
    void bothClientsAreBuilt() {
        assertThat(config.impersonationUaaClient()).isNotNull();
        assertThat(config.impersonationTenancyClient(mock(LoadBalancedExchangeFilterFunction.class))).isNotNull();
    }

}
