package com.asrevo.cvhome.aot;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every {@code lb://} name a service can call gets a child context generated at build time. A missing one fails the
 * native service on its first call to that peer, not at build time.
 */
class LoadBalancerClientsAotEnvironmentPostProcessorTest {

    private static final String KEY = "com.asrevo.cvhome.services.%s.%s";

    private static final String UAA = "uaa";

    private static final String MERCHANT = "merchant";

    private static final String CORE_GATEWAY = "store-core-gateway";

    private static final String CORE_NAMESPACE = "store-core.cvhome.lcl";

    private static final String CORE_GATEWAY_NAME = "store-core-gateway.store-core.cvhome.lcl";

    private static MockEnvironment withService(MockEnvironment environment, String service, String namespace,
                                               String gateway) {
        return environment.withProperty(KEY.formatted(service, "name"), service)
                .withProperty(KEY.formatted(service, "namespace"), namespace)
                .withProperty(KEY.formatted(service, "gateway-service-name"), gateway)
                // An entry's other fields hold placeholders; they must not be resolved to read these three.
                .withProperty(KEY.formatted(service, "domain"), "${an.unresolvable.placeholder}");
    }

    private static MockEnvironment services() {
        return withService(withService(new MockEnvironment(), UAA, CORE_NAMESPACE, CORE_GATEWAY),
                MERCHANT, "store-pod-507f1f77.cvhome.lcl", "spg");
    }

    @AfterEach
    void clearAotFlag() {
        System.clearProperty(LoadBalancerClientsAotEnvironmentPostProcessor.AOT_PROCESSING);
    }

    @Test
    void everyServiceNameAndEveryCrossNamespaceGatewayNameIsAClient() {
        assertThat(LoadBalancerClientsAotEnvironmentPostProcessor.clientNames(services()))
                .containsExactlyInAnyOrder(UAA, CORE_GATEWAY_NAME, MERCHANT, "spg.store-pod-507f1f77.cvhome.lcl");
    }

    @Test
    void theConfiguredListIsKeptAndExtendedNotReplaced() {
        MockEnvironment environment = services()
                .withProperty(LoadBalancerClientsAotEnvironmentPostProcessor.CLIENTS, "landing-ui,console-ui");

        assertThat(LoadBalancerClientsAotEnvironmentPostProcessor.clientNames(environment))
                .contains("landing-ui", "console-ui", UAA);
    }

    @Test
    void duringProcessAotTheListReachesTheEnvironment() {
        System.setProperty(LoadBalancerClientsAotEnvironmentPostProcessor.AOT_PROCESSING, "true");
        MockEnvironment environment = services();

        new LoadBalancerClientsAotEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty(LoadBalancerClientsAotEnvironmentPostProcessor.CLIENTS))
                .contains(CORE_GATEWAY_NAME);
    }

    @Test
    void atRunTimeNothingChanges() {
        // The generated contexts exist; the list keeps its configured meaning (eager loading) everywhere.
        MockEnvironment environment = services();

        new LoadBalancerClientsAotEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty(LoadBalancerClientsAotEnvironmentPostProcessor.CLIENTS)).isNull();
    }

}
