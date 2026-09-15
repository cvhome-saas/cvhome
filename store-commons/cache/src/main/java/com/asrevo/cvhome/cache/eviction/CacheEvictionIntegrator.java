package com.asrevo.cvhome.cache.eviction;

import java.util.List;
import java.util.Map;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.spi.IntegratorProvider;

/**
 * Installs the {@link CommitEvictionListener} while the session factory is built, through Hibernate's own
 * integrator hook, so the listener is in place before the first session rather than added by a bean after the fact.
 */
public final class CacheEvictionIntegrator implements Integrator {

    /** Hibernate's property naming the {@link IntegratorProvider}; {@code JpaSettings.INTEGRATOR_PROVIDER}. */
    public static final String PROPERTY = "hibernate.integrator_provider";

    private final CommitEvictionListener listener;

    public CacheEvictionIntegrator(CommitEvictionListener listener) {
        this.listener = listener;
    }

    /** The JPA property that makes Hibernate call this integrator. */
    public Map<String, Object> asProperty() {
        IntegratorProvider provider = () -> List.of(this);
        return Map.of(PROPERTY, provider);
    }

    @Override
    public void integrate(Metadata metadata, BootstrapContext bootstrapContext, SessionFactoryImplementor factory) {
        EventListenerRegistry listeners = factory.getEventListenerRegistry();
        listeners.appendListeners(EventType.POST_COMMIT_INSERT, listener);
        listeners.appendListeners(EventType.POST_COMMIT_UPDATE, listener);
        listeners.appendListeners(EventType.POST_COMMIT_DELETE, listener);
        listeners.appendListeners(EventType.POST_COLLECTION_RECREATE, listener);
        listeners.appendListeners(EventType.POST_COLLECTION_UPDATE, listener);
        listeners.appendListeners(EventType.POST_COLLECTION_REMOVE, listener);
    }
}
