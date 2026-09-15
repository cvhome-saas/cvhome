package com.asrevo.cvhome.cache.eviction;

import java.util.Set;

import org.hibernate.engine.spi.TransactionCompletionCallbacks.AfterCompletionCallback;
import org.hibernate.event.spi.AbstractCollectionEvent;
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

import com.asrevo.cvhome.cache.CacheRegion;
import com.asrevo.cvhome.cache.CacheRegistry;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.StoreScoped;

/**
 * Drops a store's cached reads when one of the service's entities of that store is written and committed.
 *
 * <p>
 * The reads a service caches are built from many tables that a dozen services write, and none emits an event for
 * every change. One post-commit listener on the session factory sees every insert, update and delete, so a merchant
 * who saves and looks sees the change on the task that took it at once; the {@link EvictionRules} say which regions
 * a write to which entity drops, and the entity says its store ({@link StoreScoped}). A change to a collection alone
 * (a product added to a group) fires at flush, inside the transaction; evicting then would let a concurrent read
 * cache the old rows again, so that eviction is queued with the session and runs once its transaction has committed.
 * </p>
 */
public final class CommitEvictionListener
        implements PostCommitInsertEventListener, PostCommitUpdateEventListener, PostCommitDeleteEventListener,
        PostCollectionRecreateEventListener, PostCollectionUpdateEventListener, PostCollectionRemoveEventListener {

    private final transient EvictionRules rules;

    private final transient CacheRegistry registry;

    public CommitEvictionListener(EvictionRules rules, CacheRegistry registry) {
        this.rules = rules;
        this.registry = registry;
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        evictFor(event.getEntity());
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        evictFor(event.getEntity());
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        evictFor(event.getEntity());
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
        return rules.owns(persister.getMappedClass().getName());
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

    /** Drops the store's entries from the regions the rules name for {@code entity}'s class, now. */
    public void evictFor(Object entity) {
        if (entity == null) {
            return;
        }
        Set<CacheRegion> regions = rules.regionsFor(entity.getClass());
        StoreMerchantId store = entity instanceof StoreScoped scoped ? scoped.scopedStore() : null;
        if (!regions.isEmpty() && store != null) {
            registry.evictStore(store, regions);
        }
    }

    private void evictWhenCommitted(AbstractCollectionEvent event) {
        Object owner = event.getAffectedOwnerOrNull();
        if (owner == null || rules.regionsFor(owner.getClass()).isEmpty()) {
            return;
        }
        AfterCompletionCallback afterCommit = (success, session) -> {
            if (success) {
                evictFor(owner);
            }
        };
        event.getSession().getTransactionCompletionCallbacks().registerCallback(afterCommit);
    }
}
