package com.asrevo.cvhome.podregistry;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.EndpointType;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodEndpoint;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.api.errors.PodRegistryUnavailableException;
import com.asrevo.cvhome.podregistry.services.pod.CachingPodDirectory;
import com.asrevo.cvhome.podregistry.services.pod.ExternalPodService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The directory tenancy resolves a store's pod through, now that it has no pod table of its own.
 *
 * <p>
 * Its whole job is to keep a read working when the registry is not. These pin that it degrades rather than
 * propagates — the opposite of the placement client, which must fail closed.
 * </p>
 */
class CachingPodDirectoryTest {

    private static final PodId SEEDED = new PodId("507f1f77bcf86cd799439011");

    private static final PodId FETCHED = new PodId("607f1f77bcf86cd799439012");

    private static final Duration TTL = Duration.ofMinutes(1);

    private static PodRegistryUnavailableException down() {
        return PodRegistryUnavailableException.wrapping(new IllegalStateException("registry is down"));
    }

    private static Pod pod(PodId id) {
        return new Pod(id, String.format("pod-%s", id.shorten()),
                new PodEndpoint("http://pod.example", EndpointType.EXTERNAL), null, null);
    }

    @Test
    @DisplayName("a registry outage is survivable: the configuration seed still resolves")
    void seedAnswersWhenTheRegistryIsDown() throws PodRegistryUnavailableException {
        ExternalPodService client = mock(ExternalPodService.class);
        when(client.listPods()).thenThrow(down());
        CachingPodDirectory directory = new CachingPodDirectory(client, List.of(pod(SEEDED)), TTL);

        assertThat(directory.find(SEEDED)).isPresent();
    }

    @Test
    @DisplayName("a successful fetch replaces the seed")
    void fetchReplacesSeed() throws PodRegistryUnavailableException {
        ExternalPodService client = mock(ExternalPodService.class);
        when(client.listPods()).thenReturn(List.of(pod(FETCHED)));
        CachingPodDirectory directory = new CachingPodDirectory(client, List.of(pod(SEEDED)), TTL);

        assertThat(directory.find(FETCHED)).isPresent();
        assertThat(directory.find(SEEDED)).isEmpty();
    }

    @Test
    @DisplayName("a later outage keeps the last good map rather than falling back to the seed")
    void outageKeepsLastKnownGood() throws PodRegistryUnavailableException {
        ExternalPodService client = mock(ExternalPodService.class);
        when(client.listPods()).thenReturn(List.of(pod(FETCHED)))
                .thenThrow(down());
        CachingPodDirectory directory = new CachingPodDirectory(client, List.of(pod(SEEDED)), Duration.ZERO);

        assertThat(directory.find(FETCHED)).isPresent();
        // TTL zero forces a refresh, which now fails — the pod fetched a moment ago must still resolve.
        assertThat(directory.find(FETCHED)).isPresent();
    }

    @Test
    @DisplayName("within the TTL the registry is not called again")
    void ttlIsHonoured() throws PodRegistryUnavailableException {
        ExternalPodService client = mock(ExternalPodService.class);
        when(client.listPods()).thenReturn(List.of(pod(FETCHED)));
        CachingPodDirectory directory = new CachingPodDirectory(client, List.of(), TTL);

        directory.find(FETCHED);
        directory.find(FETCHED);
        directory.find(FETCHED);

        verify(client, times(1)).listPods();
    }

    @Test
    @DisplayName("an unknown pod is an empty Optional, so the caller decides — not a fabricated 404 in here")
    void unknownPodIsEmpty() throws PodRegistryUnavailableException {
        ExternalPodService client = mock(ExternalPodService.class);
        when(client.listPods()).thenReturn(List.of(pod(FETCHED)));
        CachingPodDirectory directory = new CachingPodDirectory(client, List.of(), TTL);

        assertThat(directory.find(SEEDED)).isEmpty();
    }

    @Test
    @DisplayName("a null id short-circuits without calling the registry")
    void nullIdIsEmpty() throws PodRegistryUnavailableException {
        ExternalPodService client = mock(ExternalPodService.class);
        CachingPodDirectory directory = new CachingPodDirectory(client, List.of(pod(SEEDED)), TTL);

        assertThat(directory.find(null)).isEmpty();
        verify(client, never()).listPods();
    }

}
