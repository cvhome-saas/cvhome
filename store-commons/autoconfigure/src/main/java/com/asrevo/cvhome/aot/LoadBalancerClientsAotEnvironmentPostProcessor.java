package com.asrevo.cvhome.aot;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

/**
 * Tells Spring Cloud LoadBalancer's build-time processing every {@code lb://} name a service can call.
 *
 * <p>
 * Each {@code lb://} name gets a child application context. On the JVM it is built the first time the name is
 * called; in a native image it can only be one generated ahead of time, and Spring Cloud generates them for the names
 * in {@code spring.cloud.loadbalancer.eager-load.clients} as the build sees it. That list lives in
 * {@code fargate-config.yml}, which a profile-free build never reads, so the native gateway's first call to
 * {@code lb://pod-registry} failed: "GenericApplicationContext must be an instance of AnnotationConfigRegistry".
 * </p>
 *
 * <p>
 * The names come from {@code com.asrevo.cvhome.services} in {@code common-config.yml}, where every service already
 * is: each service's own name (a call inside its namespace, {@code lb://merchant}) and each
 * {@code <gateway-service-name>.<namespace>} ({@code ServiceUrlBuilder}'s call across namespaces,
 * {@code lb://store-core-gateway.store-core.cvhome.lcl/billing}), added to whatever the list already holds. A new
 * service is covered by being in that map. Only during {@code processAot}: at run time the generated contexts exist
 * and the list keeps its configured meaning (eager loading), so no deployment starts differently.
 * </p>
 *
 * <p>
 * Not covered: a pod registered with an {@code INTERNAL} endpoint, which is called as {@code lb://spg.<its
 * namespace>} — a name that exists only in pod-registry's data. Every deployment registers pods as {@code EXTERNAL}.
 * </p>
 */
public class LoadBalancerClientsAotEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String CLIENTS = "spring.cloud.loadbalancer.eager-load.clients";

    static final String SERVICES = "com.asrevo.cvhome.services";

    /** Set by Spring's AOT processor for the duration of {@code processAot}. */
    static final String AOT_PROCESSING = "spring.aot.processing";

    static final String SOURCE_NAME = "cvhomeLoadBalancerClients";

    /** Only the three fields of a service entry this needs; the rest are never bound, so never resolved. */
    record ServiceEntry(String name, String namespace, String gatewayServiceName) {
    }

    static Set<String> clientNames(Environment environment) {
        Binder binder = Binder.get(environment);
        Set<String> names = new TreeSet<>(binder.bind(CLIENTS, Bindable.setOf(String.class)).orElse(Set.of()));
        Map<String, ServiceEntry> services =
                binder.bind(SERVICES, Bindable.mapOf(String.class, ServiceEntry.class)).orElse(Map.of());
        services.forEach((key, service) -> {
            names.add(StringUtils.hasText(service.name()) ? service.name() : key);
            if (StringUtils.hasText(service.gatewayServiceName()) && StringUtils.hasText(service.namespace())) {
                names.add(String.format("%s.%s", service.gatewayServiceName(), service.namespace()));
            }
        });
        return names;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!Boolean.getBoolean(AOT_PROCESSING)) {
            return;
        }
        Set<String> names = clientNames(environment);
        if (!names.isEmpty()) {
            environment.getPropertySources()
                    .addFirst(new MapPropertySource(SOURCE_NAME, Map.of(CLIENTS, String.join(",", names))));
        }
    }

    /** After the config files are loaded: the services map comes from them. */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
