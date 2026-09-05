package com.asrevo.cvhome.tenancy.manager.service;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.asrevo.cvhome.commons.domain.EndpointType;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodEndpoint;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.merchant.api.MerchantStorePodClient;
import com.asrevo.cvhome.podregistry.services.pod.CachingPodDirectory;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The per-pod client cache the store outbox writes through.
 *
 * <p>
 * Two things it has to get right. It resolves pods through the directory rather than the configuration seed,
 * because placement may pick a pod an operator registered through the API that configuration has never heard of.
 * And it caches per pod id, because building a client per outbox attempt would rebuild the whole HTTP stack on
 * every store event.
 * </p>
 */
class StorePodClientFactoryTest {

    private static final PodId POD = new PodId("507f1f77bcf86cd799439011");
    private static final PodId UNKNOWN = new PodId("507f1f77bcf86cd799439012");
    private static final String MERCHANT = "merchant";

    private final CachingPodDirectory directory = Mockito.mock(CachingPodDirectory.class);
    private final RestClientBuilder restClientBuilder = Mockito.mock(RestClientBuilder.class);
    private final StorePodClientFactory factory = new StorePodClientFactory(directory, restClientBuilder);

    private static Pod pod() {
        return new Pod(POD, "pod-a", new PodEndpoint("http://pod.example", EndpointType.EXTERNAL), null, null);
    }

    @Test
    void aClientIsBuiltOncePerPodAndReusedAfterwards() {
        MerchantStorePodClient client = Mockito.mock(MerchantStorePodClient.class);
        when(directory.find(POD)).thenReturn(Optional.of(pod()));
        when(restClientBuilder.buildClient(any(), eq(MERCHANT), eq(MerchantStorePodClient.class), any()))
                .thenReturn(client);

        assertThat(factory.getMerchantStorePodClient(POD))
                .isSameAs(client)
                .isSameAs(factory.getMerchantStorePodClient(POD));

        // Rebuilding per call would rebuild the HTTP stack on every store event.
        verify(restClientBuilder).buildClient(any(), eq(MERCHANT), eq(MerchantStorePodClient.class), any());
    }

    @Test
    void aPodTheDirectoryDoesNotKnowFailsThisAttemptRatherThanCachingAMiss() {
        when(directory.find(UNKNOWN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> factory.getMerchantStorePodClient(UNKNOWN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(UNKNOWN.toString());

        // The directory refreshes within its TTL, so the outbox retry has to be able to resolve it later.
        assertThatThrownBy(() -> factory.getMerchantStorePodClient(UNKNOWN))
                .isInstanceOf(IllegalArgumentException.class);
        verify(directory, Mockito.times(2)).find(UNKNOWN);
    }
}
