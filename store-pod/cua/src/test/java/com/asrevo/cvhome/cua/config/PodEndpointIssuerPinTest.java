package com.asrevo.cvhome.cua.config;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.EndpointType;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodEndpoint;
import com.asrevo.cvhome.s2s.model.PodInfoProperties;
import com.asrevo.cvhome.sso.config.IssuerPin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * cua's half of the issuer seam — the same shape as uaa's, pinned to the pod's endpoint instead of a service host.
 *
 * <p>
 * Every token cua mints names this issuer, and the pods that consume those tokens check it. Deriving it from the
 * request host would mint one issuer per storefront domain, and every one of them would be rejected downstream,
 * so a missing endpoint fails loudly rather than falling back.
 * </p>
 */
class PodEndpointIssuerPinTest {

    private static final String ENDPOINT = "https://pod-1.example.com";
    private static final String POD_NAME = "pod-1";
    private static final String CUA_ISSUER = "%s/cua";

    private static IssuerPin pinFor(Pod pod) {
        return new PodEndpointIssuerPin().issuerPin(new PodInfoProperties(pod));
    }

    @Test
    void theIssuerIsThePodsEndpointWithCuasOwnPathBeneathIt() {
        Pod pod = new Pod(null, POD_NAME, new PodEndpoint(ENDPOINT, EndpointType.EXTERNAL), null, null);

        assertThat(pinFor(pod).issuer()).isEqualTo(CUA_ISSUER.formatted(ENDPOINT));
    }

    @Test
    void aTrailingSlashOnTheEndpointIsNormalisedAway() {
        Pod pod = new Pod(null, POD_NAME, new PodEndpoint("%s/".formatted(ENDPOINT), EndpointType.EXTERNAL),
                null, null);

        // Two issuers differing only by a slash are two issuers to a resource server.
        assertThat(pinFor(pod).issuer()).isEqualTo(CUA_ISSUER.formatted(ENDPOINT));
    }

    @Test
    void anUnconfiguredPodFailsLoudlyRatherThanDerivingAnIssuerFromTheRequest() {
        assertThatThrownBy(() -> pinFor(null).issuer())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot pin its OAuth2 issuer");
        assertThatThrownBy(() -> pinFor(new Pod(null, POD_NAME, null, null, null)).issuer())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> pinFor(new Pod(null, POD_NAME,
                new PodEndpoint(null, EndpointType.EXTERNAL), null, null)).issuer())
                .isInstanceOf(IllegalStateException.class);
    }
}
