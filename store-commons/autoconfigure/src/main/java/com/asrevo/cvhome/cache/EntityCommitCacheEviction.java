package com.asrevo.cvhome.cache;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import jakarta.persistence.EntityManagerFactory;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.TransactionCompletionCallbacks.AfterCompletionCallback;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.AbstractCollectionEvent;
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

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Drops a store's read caches whenever one of the service's entities of that store is written and committed.
 *
 * <p>
 * The storefront reads that content and catalog cache are built from many tables that a dozen services write, and
 * neither emits an event for every change. One post-commit listener on the session factory sees every insert, update
 * and delete, so a merchant who saves and looks sees the change on the task that took it at once, and on any other
 * task once the cache's own time-to-live runs out. Only entities under {@code entityPackage} count: the outbox's rows
 * are written every few seconds and change nothing a shopper sees.
 * </p>
 *
 * <p>
 * Every cached read is keyed by a {@link StoreScopedKey}, so a write evicts the entries of the store that wrote
 * ({@code storeOf} names it from the entity) and leaves every other tenant's warm: a pod hosts hundreds of stores, and
 * clearing them all on each merchant's save re-filled every cache against a three-connection pool. An entity the
 * resolver cannot place, or a key that is not store-scoped, clears the cache outright: stale is the one thing a
 * cache must not be.
 * </p>
 *
 * <p>
 * A change to a collection alone (a product added to a group, a related product removed) fires a collection event at
 * flush, inside the transaction; evicting then would let a concurrent read cache the old rows again for a whole
 * time-to-live, so the eviction is queued with the session and runs once its transaction has committed. A bulk JPQL
 * update or delete fires neither event: {@link #evictAfterCommit} is for the code that runs one.
 * </p>
 */
public class EntityCommitCacheEviction
        implements PostCommitInsertEventListener, PostCommitUpdateEventListener, PostCommitDeleteEventListener,
        PostCollectionRecreateEventListener, PostCollectionUpdateEventListener, PostCollectionRemoveEventListener {

    private final transient CacheManager caches;

    private final String entityPackage;

    private final List<String> cacheNames;

    private final transient Function<Object, StoreMerchantId> storeOf;

    /**
     * @param storeOf the store a written entity belongs to, or null when it cannot say; a description resolves
     *                through its owner, an entity of another kind returns null and clears everything
     */
    public EntityCommitCacheEviction(EntityManagerFactory entityManagerFactory, CacheManager caches,
                                     String entityPackage, Collection<String> cacheNames,
                                     Function<Object, StoreMerchantId> storeOf) {
        this.caches = caches;
        this.entityPackage = entityPackage;
        this.cacheNames = List.copyOf(cacheNames);
        this.storeOf = storeOf;
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
        evictFor(event.getPersister(), event.getEntity());
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        evictFor(event.getPersister(), event.getEntity());
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        evictFor(event.getPersister(), event.getEntity());
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

    /** Only the service's own entities are held back until commit; the outbox's rows need no post-commit call. */
    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return owns(persister.getMappedClass().getName());
    }

    @Override
    public void onPostRecreateCollection(PostCollectionRecreateEvent event) {
        evictWhenCommitted(event);
    }

    @Override
    public void onPostUpdateCollection(PostCollectionUpdateEvent event) {
        evictWhenCommitted(event);
    }

    @Override
    public void onPostRemoveCollection(PostCollectionRemoveEvent event) {
        evictWhenCommitted(event);
    }

    /**
     * Drops {@code store}'s entries once the surrounding Spring transaction commits, or at once outside one. For the
     * writes Hibernate reports no entity for: a bulk update, a native refresh of a derived table.
     */
    public void evictAfterCommit(StoreMerchantId store) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evict(store);
                }
            });
        } else {
            evict(store);
        }
    }

    /** Drops {@code store}'s entries from every cache, now; a null store, or a cache without scoped keys, is cleared. */
    public void evict(StoreMerchantId store) {
        for (String name : cacheNames) {
            Cache cache = caches.getCache(name);
            if (cache == null) {
                continue;
            }
            if (store != null && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache<?, ?> caffeine) {
                caffeine.asMap().keySet()
                        .removeIf(key -> !(key instanceof StoreScopedKey scoped) || store.equals(scoped.store()));
            } else {
                cache.clear();
            }
        }
    }

    private void evictFor(EntityPersister persister, Object entity) {
        if (owns(persister.getMappedClass().getName())) {
            evict(storeOf(entity));
        }
    }

    /** A collection event comes at flush; the eviction is queued on the session and runs after its commit. */
    private void evictWhenCommitted(AbstractCollectionEvent event) {
        String owner = event.getAffectedOwnerEntityName();
        if (owner == null || !owns(owner)) {
            return;
        }
        StoreMerchantId store = storeOf(event.getAffectedOwnerOrNull());
        AfterCompletionCallback afterCommit = (success, session) -> {
            if (success) {
                evict(store);
            }
        };
        event.getSession().getTransactionCompletionCallbacks().registerCallback(afterCommit);
    }

    private boolean owns(String entityClassName) {
        return entityClassName.startsWith(entityPackage);
    }

    private StoreMerchantId storeOf(Object entity) {
        return entity == null ? null : storeOf.apply(entity);
    }
}
