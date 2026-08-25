package com.asrevo.cloud.ecs.discovery;

import java.util.Map;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.servicediscovery.model.HttpInstanceSummary;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * One Cloud Map registration seen as a Spring {@code ServiceInstance}, which is what the load balancer routes on.
 *
 * <p>
 * The attributes are strings written by ECS, and every one of them is optional. The port is the interesting case:
 * a task that registered its own port must win over the configured default, because that default is a per-service
 * guess and getting it wrong sends traffic to a closed port.
 * </p>
 */
class CloudMapServiceInstanceTest {

    private static final String IPV4 = "AWS_INSTANCE_IPV4";

    private static final String PORT = "AWS_INSTANCE_PORT";

    private static final String SECURE = "SECURE";

    private static final String HOST = "10.0.1.7";

    private static final Integer DEFAULT_PORT = 8080;

    private static final String REGISTERED_PORT = "9091";

    private static final String SERVICE = "catalog";

    private static final String NAMESPACE = "pods.local";

    private static final String INSTANCE_ID = "i-123";

    private static HttpInstanceSummary registration(Map<String, String> attributes) {
        return HttpInstanceSummary.builder()
                .serviceName(SERVICE)
                .namespaceName(NAMESPACE)
                .instanceId(INSTANCE_ID)
                .attributes(attributes)
                .build();
    }

    @Test
    void theHostIsTheAddressEcsRegistered() {
        CloudMapServiceInstance instance = new CloudMapServiceInstance(registration(Map.of(IPV4, HOST)), DEFAULT_PORT);

        assertThat(instance.getHost()).isEqualTo(HOST);
    }

    @Test
    void aRegistrationWithoutItsOwnPortFallsBackToTheConfiguredOne() {
        CloudMapServiceInstance instance = new CloudMapServiceInstance(registration(Map.of(IPV4, HOST)), DEFAULT_PORT);

        assertThat(instance.getPort()).isEqualTo(DEFAULT_PORT);
    }

    @Test
    void aRegistrationCarryingItsOwnPortOverridesTheConfiguredOne() {
        CloudMapServiceInstance instance = new CloudMapServiceInstance(
                registration(Map.of(IPV4, HOST, PORT, REGISTERED_PORT)), DEFAULT_PORT);

        assertThat(instance.getPort()).isEqualTo(9091);
    }

    @Test
    void anInstanceIsPlainHttpUnlessItSaysOtherwise() {
        CloudMapServiceInstance instance = new CloudMapServiceInstance(registration(Map.of(IPV4, HOST)), DEFAULT_PORT);

        assertThat(instance.isSecure()).isFalse();
        assertThat(instance.getScheme()).isEqualTo("http");
    }

    @Test
    void anInstanceMarkedSecureIsAddressedOverHttps() {
        CloudMapServiceInstance instance = new CloudMapServiceInstance(
                registration(Map.of(IPV4, HOST, SECURE, "true")), DEFAULT_PORT);

        assertThat(instance.isSecure()).isTrue();
        assertThat(instance.getScheme()).isEqualTo("https");
    }

    @Test
    void theUriCombinesTheSchemeHostAndResolvedPort() {
        CloudMapServiceInstance instance = new CloudMapServiceInstance(
                registration(Map.of(IPV4, HOST, PORT, REGISTERED_PORT)), DEFAULT_PORT);

        assertThat(instance.getUri()).hasToString("http://10.0.1.7:9091");
    }

    @Test
    void theServiceNamespaceAndInstanceIdComeStraightFromTheRegistration() {
        CloudMapServiceInstance instance = new CloudMapServiceInstance(registration(Map.of(IPV4, HOST)), DEFAULT_PORT);

        assertThat(instance.getServiceId()).isEqualTo(SERVICE);
        assertThat(instance.getNamespace()).isEqualTo(NAMESPACE);
        assertThat(instance.getInstanceId()).isEqualTo(INSTANCE_ID);
        assertThat(instance.getMetadata()).containsEntry(IPV4, HOST);
    }
}
