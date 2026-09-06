package com.asrevo.cvhome.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.server.session.DefaultWebSessionManager;
import org.springframework.web.server.session.InMemoryWebSessionStore;
import org.springframework.web.server.session.WebSessionManager;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The gauge reads the in-memory store's size and reports NaN for any other store.
 */
class GatewaySessionMetricsTest {

    @SuppressWarnings("unchecked")
    private static ObjectProvider<WebSessionManager> provider(WebSessionManager manager) {
        ObjectProvider<WebSessionManager> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(manager);
        return provider;
    }

    @Test
    void countsTheSessionsInTheInMemoryStore() {
        InMemoryWebSessionStore store = new InMemoryWebSessionStore();
        DefaultWebSessionManager manager = new DefaultWebSessionManager();
        manager.setSessionStore(store);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        new GatewaySessionMetrics(provider(manager)).bindTo(registry);

        assertThat(registry.get(GatewaySessionMetrics.METER).gauge().value()).isZero();

        store.createWebSession().flatMap(session -> {
            session.getAttributes().put("k", "v");
            return session.save();
        }).block();
        assertThat(registry.get(GatewaySessionMetrics.METER).gauge().value()).isEqualTo(1);
    }

    @Test
    void reportsNanForAnotherStore() {
        assertThat(new GatewaySessionMetrics(provider(mock(WebSessionManager.class))).count()).isNaN();
    }

}
