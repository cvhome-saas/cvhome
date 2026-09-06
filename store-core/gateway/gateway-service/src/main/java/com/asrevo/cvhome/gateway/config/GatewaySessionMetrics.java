package com.asrevo.cvhome.gateway.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.server.session.DefaultWebSessionManager;
import org.springframework.web.server.session.InMemoryWebSessionStore;
import org.springframework.web.server.session.WebSessionManager;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * How many seller sessions the gateway holds in memory, as {@code cvhome.gateway.sessions}.
 *
 * <p>
 * Sessions live in the JVM (no session store), so the count is both a capacity signal under a login-heavy load test
 * and the number of sellers a restart signs out. {@code NaN} when the session store is not the in-memory one.
 * </p>
 */
@Component
public class GatewaySessionMetrics implements MeterBinder {

    static final String METER = "cvhome.gateway.sessions";

    private final ObjectProvider<WebSessionManager> sessionManager;

    public GatewaySessionMetrics(ObjectProvider<WebSessionManager> sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder(METER, this, GatewaySessionMetrics::count)
                .description("Seller sessions held in the gateway's memory")
                .register(registry);
    }

    double count() {
        WebSessionManager manager = sessionManager.getIfAvailable();
        if (manager instanceof DefaultWebSessionManager defaultManager
                && defaultManager.getSessionStore() instanceof InMemoryWebSessionStore store) {
            return store.getSessions().size();
        }
        return Double.NaN;
    }

}
