package com.asrevo.cvhome.uaa.config;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.ServiceDomain;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import com.asrevo.cvhome.sso.config.IssuerPin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * uaa's half of the issuer seam.
 *
 * <p>
 * The issuer is pinned to a configured address rather than derived from the request host, because every token uaa
 * signs names it and a resource server checks it. Deriving it per request would mint tokens that name whichever
 * host the browser happened to use — one issuer behind the gateway, another on uaa's own address — and half of
 * them would fail validation. The pin is therefore configuration or nothing: a partial address fails at startup
 * rather than producing tokens nobody can verify.
 * </p>
 */
class ServiceDomainIssuerPinTest {

    private static final String UAA = "uaa";
    private static final String HOST = "uaa.gateway.com";
    private static final String PORT = "9002";
    private static final String SCHEME = "http";
    private static final String NAMESPACE = "ns";
    private static final String GATEWAY = "gw";

    private static IssuerPin pinFor(ServiceDomain domain) {
        return new ServiceDomainIssuerPin().issuerPin(
                new ServiceDomainProperties(domain == null ? Map.of() : Map.of(UAA, domain), List.of()));
    }

    @Test
    void aFullyConfiguredServiceDomainBecomesTheIssuer() {
        IssuerPin pin = pinFor(new ServiceDomain(UAA, HOST, PORT, SCHEME, NAMESPACE, GATEWAY));

        assertThat(pin.issuer()).isEqualTo("%s://%s:%s".formatted(SCHEME, HOST, PORT));
    }

    @Test
    void anUnconfiguredServiceFailsLoudlyRatherThanMintingAnUnverifiableIssuer() {
        assertThatThrownBy(() -> pinFor(null).issuer())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("issuer is not configured");
    }

    @Test
    void aPartiallyConfiguredServiceFailsTheSameWayOnEveryMissingPiece() {
        assertThatThrownBy(() -> pinFor(new ServiceDomain(UAA, null, PORT, SCHEME, NAMESPACE, GATEWAY)).issuer())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> pinFor(new ServiceDomain(UAA, HOST, null, SCHEME, NAMESPACE, GATEWAY)).issuer())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> pinFor(new ServiceDomain(UAA, HOST, PORT, null, NAMESPACE, GATEWAY)).issuer())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void thePinIsEvaluatedLazilySoAMisconfigurationSurfacesWhereItIsRead() {
        // Building the bean must not throw; the message has to reach whoever asked for an issuer.
        assertThat(pinFor(null)).isNotNull();
    }
}
