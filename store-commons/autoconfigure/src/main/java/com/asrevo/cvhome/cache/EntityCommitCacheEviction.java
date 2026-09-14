package com.asrevo.cvhome.cache;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManagerFactory;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostCollectionRecreateEvent;
import org.hibernate.event.spi.PostCollectionRecreateEventListener;
import org.hibernate.event.spi.PostCollectionRemoveEvent;
import org.hibernate.event.spi.PostCollectionRemoveEventListener;
import org.hibernate.event.spi.PostCollectionUpdateEvent;
import org.hibernate.event.spi.PostCollectionUpdateEventListener;
import org.hibernate.event.spi.PostCommitDeleteEventListener;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Clears a service's read caches whenever one of its entities is written and committed.
 *
 * <p>
 * The storefront reads that content and catalog cache are built from many tables that a dozen services write, and
 * neither emits an event for every change. One post-commit listener on the session factory sees every insert, update
 * and delete, so a merchant who saves and looks sees the change on the task that took it at once, and on any other
 * task once the cache's own time-to-live runs out. Writes are merchant actions, rare beside reads, so clearing
 * everything on each costs little. Only entities under {@code entityPackage} count: the outbox's rows are written every
 * few seconds and change nothing a shopper sees.
 * </p>
 *
 * <p>
 * A change to a collection alone (a product added to a group, a related product removed) fires a collection event at
 * flush rather than an entity event at commit; the caches are then cleared once the transaction commits. A bulk JPQL
 * update or delete fires neither; a cache it leaves stale lives out its time-to-live.
 * </p>
 */
public class EntityCommitCacheEviction
        implements PostCommitInsertEventListener, PostCommitUpdateEventListener, PostCommitDeleteEventListener,
        PostCollectionRecreateEventListener, PostCollectionUpdateEventListener, PostCollectionRemoveEventListener {

    private final transient CacheManager caches;

    private final String entityPackage;

    private final List<String> cacheNames;

    public EntityCommitCacheEviction(EntityManagerFactory entityManagerFactory, CacheManager caches,
                                     String entityPackage, Collection<String> cacheNames) {
        this.caches = caches;
        this.entityPackage = entityPackage;
        this.cacheNames = List.copyOf(cacheNames);
        EventListenerRegistry listeners = entityManagerFactory.unwrap(SessionFactoryImplementor.class)
                .getEventListenerRegistry();
        listeners.appendListeners(EventType.POST_COMMIT_INSERT, this);
        listeners.appendListeners(EventType.POST_COMMIT_UPDATE, this);
        listeners.appendListeners(EventType.POST_COMMIT_DELETE, this);
        listeners.appendListeners(EventType.POST_COLLECTION_RECREATE, this);
        listeners.appendListeners(EventType.POST_COLLECTION_UPDATE, this);
        listeners.appendListeners(EventType.POST_COLLECTION_REMOVE, this);
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

    @Override
    public void onPostRecreateCollection(PostCollectionRecreateEvent event) {
        evictAfterCommit(event.getAffectedOwnerEntityName());
    }

    @Override
    public void onPostUpdateCollection(PostCollectionUpdateEvent event) {
        evictAfterCommit(event.getAffectedOwnerEntityName());
    }

    @Override
    public void onPostRemoveCollection(PostCollectionRemoveEvent event) {
        evictAfterCommit(event.getAffectedOwnerEntityName());
    }

    private void evictFor(EntityPersister persister) {
        if (persister.getMappedClass().getPackageName().startsWith(entityPackage)) {
            clear();
        }
    }

    /** A collection event comes at flush, inside the transaction; clearing then could let a read re-cache the old rows. */
    private void evictAfterCommit(String ownerEntityName) {
        if (ownerEntityName == null || !ownerEntityName.startsWith(entityPackage)) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    clear();
                }
            });
        } else {
            clear();
        }
    }

    private void clear() {
        cacheNames.forEach(name -> Optional.ofNullable(caches.getCache(name)).ifPresent(Cache::clear));
    }
}
