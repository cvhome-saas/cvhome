package com.asrevo.cvhome.content.config;

import java.util.Optional;

import jakarta.persistence.EntityManagerFactory;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostCommitDeleteEventListener;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.content.facade.CachedStorefront;

/**
 * Clears the storefront caches whenever a content entity is written and committed.
 *
 * <p>
 * Content has no change events, and the writes the storefront reads — pages, policies, menus, layouts, banners, site
 * settings — are spread over a dozen services. One post-commit listener on the session factory sees them all, so an
 * editor who publishes and opens the storefront sees the change on this task at once, and on any other within
 * {@link CachedStorefront#TTL}. Writes are editor actions, so clearing everything on each is cheap. Only this service's
 * own entities count: the outbox's rows are written every few seconds and change nothing a shopper sees.
 * </p>
 */
@Component
public class StorefrontCacheEviction
        implements PostCommitInsertEventListener, PostCommitUpdateEventListener, PostCommitDeleteEventListener {

    private static final String CONTENT_ENTITIES = "com.asrevo.cvhome.content.entity";

    private final transient CacheManager caches;

    public StorefrontCacheEviction(EntityManagerFactory entityManagerFactory, CacheManager caches) {
        this.caches = caches;
        EventListenerRegistry listeners = entityManagerFactory.unwrap(SessionFactoryImplementor.class)
                .getEventListenerRegistry();
        listeners.appendListeners(EventType.POST_COMMIT_INSERT, this);
        listeners.appendListeners(EventType.POST_COMMIT_UPDATE, this);
        listeners.appendListeners(EventType.POST_COMMIT_DELETE, this);
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        evictFor(event.getPersister());
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        evictFor(event.getPersister());
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        evictFor(event.getPersister());
    }

    @Override
    public void onPostInsertCommitFailed(PostInsertEvent event) {
        // Nothing was written, so nothing a cache holds went stale.
    }

    @Override
    public void onPostUpdateCommitFailed(PostUpdateEvent event) {
        // Nothing was written, so nothing a cache holds went stale.
    }

    @Override
    public void onPostDeleteCommitFailed(PostDeleteEvent event) {
        // Nothing was written, so nothing a cache holds went stale.
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return true;
    }

    private void evictFor(EntityPersister persister) {
        if (persister.getMappedClass().getPackageName().startsWith(CONTENT_ENTITIES)) {
            CachedStorefront.CACHES.forEach(name -> Optional.ofNullable(caches.getCache(name)).ifPresent(Cache::clear));
        }
    }
}
