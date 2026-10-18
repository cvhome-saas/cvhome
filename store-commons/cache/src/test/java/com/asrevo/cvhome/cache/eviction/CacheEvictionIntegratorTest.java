package com.asrevo.cvhome.cache.eviction;

import java.util.List;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.cache.CacheRegions;
import com.asrevo.cvhome.cache.CacheRegistry;
import com.asrevo.cvhome.cache.config.CacheProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CacheEvictionIntegratorTest {

    @Test
    void theIntegratorRegistersTheListenerForTheSixEventsAndIsHandedToHibernateAsAProperty() {
        CommitEvictionListener listener = new CommitEvictionListener(EvictionRules.none(),
                new CacheRegistry(CacheRegions.none(), List.of(), CacheProperties.defaults()));
        CacheEvictionIntegrator integrator = new CacheEvictionIntegrator(listener);
        SessionFactoryImplementor factory = mock(SessionFactoryImplementor.class);
        EventListenerRegistry registry = mock(EventListenerRegistry.class);
        when(factory.getEventListenerRegistry()).thenReturn(registry);

        integrator.integrate(null, null, factory);

        verify(registry).appendListeners(EventType.POST_COMMIT_INSERT, listener);
        verify(registry).appendListeners(EventType.POST_COMMIT_UPDATE, listener);
        verify(registry).appendListeners(EventType.POST_COMMIT_DELETE, listener);
        verify(registry).appendListeners(EventType.POST_COLLECTION_RECREATE, listener);
        verify(registry).appendListeners(EventType.POST_COLLECTION_UPDATE, listener);
        verify(registry).appendListeners(EventType.POST_COLLECTION_REMOVE, listener);
        Object provider = integrator.asProperty().get("hibernate.integrator_provider");
        assertThat(provider).isInstanceOf(IntegratorProvider.class);
        assertThat(((IntegratorProvider) provider).getIntegrators()).containsExactly(integrator);
    }
}
