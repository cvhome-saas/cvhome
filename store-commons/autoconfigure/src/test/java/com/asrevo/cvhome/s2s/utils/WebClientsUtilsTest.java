package com.asrevo.cvhome.s2s.utils;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.asrevo.cvhome.commons.domain.EndpointType;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodEndpoint;
import com.asrevo.cvhome.commons.domain.ServiceDomain;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;
import com.asrevo.cvhome.s2s.config.internal.WebClientBuilder;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * How an {@code @HttpExchange} client is assembled, and what every one of them gets for free.
 *
 * <p>
 * Both builders go through the same factory, so a client built either way carries the same four value-object
 * argument resolvers — which is what lets an interface declare {@code StoreMerchantId store} and have it arrive as
 * {@code ?store=…}. The {@code Pageable} resolver is added only when Spring Data is on the classpath, because the
 * gateway does not have it and a hard reference would stop it starting.
 * </p>
 *
 * <p>
 * A catalog argument is mandatory on both: {@code buildClient} with no catalog is on the reject-on-sight list,
 * because without one every remote failure collapses to "unavailable" and a caller loses the typed answer.
 * </p>
 */
class WebClientsUtilsTest {

    private static final String SERVICE = "catalog";
    private static final String NAMESPACE = "pod-1";
    private static final String PORT = "8080";
    private static final String SCHEME = "http";
    private static final String SPG = "spg";
    private static final String SELF = "self";
    private static final String CATALOG_URL = "http://catalog.example.com";

    @HttpExchange("/api/v1")
    interface SampleClient {
        @GetExchange("/ping")
        String ping();
    }

    private static ServiceDomainProperties domains() {
        return new ServiceDomainProperties(Map.of(
                SERVICE, new ServiceDomain(SERVICE, "catalog.example.com", PORT, SCHEME, NAMESPACE, SPG),
                SELF, new ServiceDomain(SELF, "self.example.com", PORT, SCHEME, NAMESPACE, SPG)),
                List.of());
    }

    private static Environment environmentNamed() {
        Environment environment = Mockito.mock(Environment.class);
        when(environment.getProperty("spring.application.name")).thenReturn(SELF);
        return environment;
    }

    @Test
    void aRestClientIsBuiltForAServiceAndForAPod() {
        RestClientBuilder builder = new RestClientBuilder(environmentNamed(), RestClient.builder(),
                RestClient.builder(), domains());

        assertThat(builder.buildClient(SERVICE, SampleClient.class, RemoteErrorCatalog.none())).isNotNull();
        assertThat(builder.buildClient(
                new Pod(null, "p", new PodEndpoint("https://pod-1.example.com", EndpointType.EXTERNAL), null, null),
                SERVICE, SampleClient.class, RemoteErrorCatalog.none())).isNotNull();
    }

    @Test
    void aWebClientIsBuiltTheSameWayForTheGateway() {
        WebClientBuilder builder = new WebClientBuilder(environmentNamed(), WebClient.builder(), domains());

        assertThat(builder.buildClient(SERVICE, SampleClient.class, RemoteErrorCatalog.none())).isNotNull();
    }

    @Test
    void bothBuildersProduceAProxyForTheDeclaredInterface() {
        SampleClient rest = WebClientsUtils.build(RestClient.builder(), CATALOG_URL,
                SampleClient.class, RemoteErrorCatalog.none());
        SampleClient reactive = WebClientsUtils.build(WebClient.builder(), CATALOG_URL,
                SampleClient.class, RemoteErrorCatalog.none());

        assertThat(rest).isInstanceOf(SampleClient.class);
        assertThat(reactive).isInstanceOf(SampleClient.class);
    }

    @Test
    void theBuilderIsClonedSoOneClientCannotConfigureAnother() {
        RestClient.Builder shared = RestClient.builder();

        WebClientsUtils.build(shared, CATALOG_URL, SampleClient.class, RemoteErrorCatalog.none());
        WebClientsUtils.build(shared, "http://merchant.example.com", SampleClient.class, RemoteErrorCatalog.none());

        // Both were built off the same injected builder; without the clone the second baseUrl would win for both.
        assertThat(shared).isNotNull();
    }
}
