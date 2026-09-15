package com.asrevo.cvhome.aot;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.aot.AotServices;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A native service can build and call every {@code -external-api} client. Missing any of these is not a build error:
 * the first call to that peer fails at run time, inside a request.
 */
class HttpServiceClientRuntimeHintsTest {

    private static RuntimeHints registered() {
        RuntimeHints hints = new RuntimeHints();
        new HttpServiceClientRuntimeHints().registerHints(hints, HttpServiceClientRuntimeHintsTest.class.getClassLoader());
        return hints;
    }

    @Test
    void anInterfaceWithExchangeMethodsIsFoundAndOneWithoutIsNot() {
        assertThat(HttpServiceClientRuntimeHints.clientInterfaces(getClass().getClassLoader()))
                .contains(PeerApi.class)
                .doesNotContain(NotAClient.class);
    }

    @Test
    void bothProxiesAClientBecomesAreRegistered() {
        RuntimeHints hints = registered();

        // The typed-error wrapper, then the proxy HttpServiceProxyFactory creates.
        assertThat(RuntimeHintsPredicates.proxies().forInterfaces(PeerApi.class)).accepts(hints);
        assertThat(RuntimeHintsPredicates.proxies().forInterfaces(AopProxyUtils.completeJdkProxyInterfaces(PeerApi.class)))
                .accepts(hints);
    }

    @Test
    void theWrapperMayInvokeEachMethodAndTheBodiesAreBindable() throws NoSuchMethodException {
        RuntimeHints hints = registered();

        assertThat(RuntimeHintsPredicates.reflection().onMethodInvocation(PeerApi.class.getMethod("find", String.class)))
                .accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection().onType(TypeReference.of(PeerView.class))).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection().onType(TypeReference.of(PeerRequest.class))).accepts(hints);
    }

    @Test
    void theRegistrarIsDiscoveredByAotProcessingWithoutABean() {
        assertThat(AotServices.factories().load(RuntimeHintsRegistrar.class))
                .anyMatch(HttpServiceClientRuntimeHints.class::isInstance);
    }

    @HttpExchange("/api/v1/peer")
    interface PeerApi {

        @GetExchange("/{id}")
        PeerView find(@PathVariable String id);

        @PostExchange
        List<PeerView> create(@RequestBody PeerRequest request);

    }

    interface NotAClient {

        PeerView find(String id);

    }

    record PeerView(String id, String name) {
    }

    record PeerRequest(String name) {
    }

}
