package com.asrevo.cvhome.cache;

import java.util.List;
import java.util.function.Function;

import jakarta.persistence.EntityManagerFactory;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.TransactionCompletionCallbacks;
import org.hibernate.engine.spi.TransactionCompletionCallbacks.AfterCompletionCallback;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostCollectionRecreateEvent;
import org.hibernate.event.spi.PostCollectionRemoveEvent;
import org.hibernate.event.spi.PostCollectionUpdateEvent;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A committed write to one of the service's own entities drops its store's cached reads; another store's stay, and
 * anything else leaves them alone.
 *
 * <p>
 * Content and catalog cache what the storefront reads and neither emits an event for every change, so this listener is
 * what keeps a merchant's save from being hidden behind a cached copy on the task that took it. A pod hosts hundreds
 * of stores, so it drops only the store that wrote.
 * </p>
 */
class EntityCommitCacheEvictionTest {

    private static final String CACHE = "READS";

    private static final StoreMerchantId STORE_A = new StoreMerchantId("store-a");

    private static final StoreMerchantId STORE_B = new StoreMerchantId("store-b");

    private static final List<Object> ARGUMENTS = List.of("group");

    private static final StoreScopedKey KEY_A = new StoreScopedKey(STORE_A, ARGUMENTS);

    private static final StoreScopedKey KEY_B = new StoreScopedKey(STORE_B, ARGUMENTS);

    private static final String UNSCOPED_KEY = "unscoped";

    private static final String OWN_ENTITY = Owned.class.getName();

    private final EventListenerRegistry registry = mock(EventListenerRegistry.class);

    private final CaffeineCacheManager caches = new CaffeineCacheManager(CACHE);

    private EntityCommitCacheEviction eviction;

    /** Stands in for an entity of the service: it lives under the package the listener watches, and names its store. */
    record Owned(StoreMerchantId store) {
    }

    /** An entity of the service whose store the resolver cannot name. */
    static final class Placeless {
    }

    @BeforeEach
    void setUp() {
        EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);
        SessionFactoryImplementor sessionFactory = mock(SessionFactoryImplementor.class);
        when(entityManagerFactory.unwrap(SessionFactoryImplementor.class)).thenReturn(sessionFactory);
        when(sessionFactory.getEventListenerRegistry()).thenReturn(registry);
        Function<Object, StoreMerchantId> storeOf = entity -> entity instanceof Owned owned ? owned.store() : null;
        eviction = new EntityCommitCacheEviction(entityManagerFactory, caches, "com.asrevo.cvhome.cache",
                List.of(CACHE), storeOf);
        cached();
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private void cached() {
        caches.getCache(CACHE).put(KEY_A, "a");
        caches.getCache(CACHE).put(KEY_B, "b");
        caches.getCache(CACHE).put(UNSCOPED_KEY, "?");
    }

    private boolean stillCached(Object key) {
        return caches.getCache(CACHE).get(key) != null;
    }

    private static EntityPersister persisterOf(Class<?> type) {
        EntityPersister persister = mock(EntityPersister.class);
        when(persister.getMappedClass()).thenAnswer(invocation -> type);
        return persister;
    }

    private static PostInsertEvent insertOf(Object entity) {
        PostInsertEvent insert = mock(PostInsertEvent.class);
        when(insert.getPersister()).thenAnswer(invocation -> persisterOf(entity.getClass()));
        when(insert.getEntity()).thenReturn(entity);
        return insert;
    }

    @Test
    void itListensForEveryCommittedWriteAndEveryCollectionChangeOfItsOwnEntities() {
        verify(registry).appendListeners(EventType.POST_COMMIT_INSERT, eviction);
        verify(registry).appendListeners(EventType.POST_COMMIT_UPDATE, eviction);
        verify(registry).appendListeners(EventType.POST_COMMIT_DELETE, eviction);
        verify(registry).appendListeners(EventType.POST_COLLECTION_RECREATE, eviction);
        verify(registry).appendListeners(EventType.POST_COLLECTION_UPDATE, eviction);
        verify(registry).appendListeners(EventType.POST_COLLECTION_REMOVE, eviction);
        assertThat(eviction.requiresPostCommitHandling(persisterOf(Owned.class))).isTrue();
        assertThat(eviction.requiresPostCommitHandling(persisterOf(String.class)))
                .as("the outbox's rows need no post-commit call").isFalse();
    }

    @Test
    void aWriteOfOneStoresEntityDropsThatStoresEntriesAndKeepsTheOthers() {
        eviction.onPostInsert(insertOf(new Owned(STORE_A)));
        assertThat(stillCached(KEY_A)).isFalse();
        assertThat(stillCached(KEY_B)).as("another tenant's entries stay warm").isTrue();
        assertThat(stillCached(UNSCOPED_KEY)).as("a key of unknown scope may belong to the store").isFalse();

        cached();
        PostUpdateEvent update = mock(PostUpdateEvent.class);
        when(update.getPersister()).thenAnswer(invocation -> persisterOf(Owned.class));
        when(update.getEntity()).thenReturn(new Owned(STORE_B));
        eviction.onPostUpdate(update);
        assertThat(stillCached(KEY_B)).isFalse();
        assertThat(stillCached(KEY_A)).isTrue();

        cached();
        PostDeleteEvent delete = mock(PostDeleteEvent.class);
        when(delete.getPersister()).thenAnswer(invocation -> persisterOf(Owned.class));
        when(delete.getEntity()).thenReturn(new Owned(STORE_A));
        eviction.onPostDelete(delete);
        assertThat(stillCached(KEY_A)).isFalse();
        assertThat(stillCached(KEY_B)).isTrue();
    }

    @Test
    void anEntityWhoseStoreIsUnknownClearsEveryStore() {
        eviction.onPostInsert(insertOf(new Placeless()));
        assertThat(stillCached(KEY_A)).isFalse();
        assertThat(stillCached(KEY_B)).isFalse();
    }

    @Test
    void anotherPackagesEntityAndAFailedCommitLeaveTheCachesAlone() {
        eviction.onPostInsert(insertOf("an outbox row"));

        eviction.onPostInsertCommitFailed(mock(PostInsertEvent.class));
        eviction.onPostUpdateCommitFailed(mock(PostUpdateEvent.class));
        eviction.onPostDeleteCommitFailed(mock(PostDeleteEvent.class));

        assertThat(stillCached(KEY_A)).isTrue();
        assertThat(stillCached(KEY_B)).isTrue();
    }

    @Test
    void aCollectionChangeDropsTheStoresEntriesOnlyOnceItsTransactionCommits() {
        EventSource session = mock(EventSource.class);
        TransactionCompletionCallbacks callbacks = mock(TransactionCompletionCallbacks.class);
        when(session.getTransactionCompletionCallbacks()).thenReturn(callbacks);
        PostCollectionUpdateEvent update = mock(PostCollectionUpdateEvent.class);
        when(update.getAffectedOwnerEntityName()).thenReturn(OWN_ENTITY);
        when(update.getAffectedOwnerOrNull()).thenReturn(new Owned(STORE_A));
        when(update.getSession()).thenReturn(session);

        eviction.onPostUpdateCollection(update);
        assertThat(stillCached(KEY_A)).as("dropped at flush, a read could cache the old rows again").isTrue();

        ArgumentCaptor<AfterCompletionCallback> queued = ArgumentCaptor.forClass(AfterCompletionCallback.class);
        verify(callbacks).registerCallback(queued.capture());
        queued.getValue().doAfterTransactionCompletion(false, session);
        assertThat(stillCached(KEY_A)).as("a rolled-back change left nothing stale").isTrue();

        queued.getValue().doAfterTransactionCompletion(true, session);
        assertThat(stillCached(KEY_A)).isFalse();
        assertThat(stillCached(KEY_B)).isTrue();
    }

    @Test
    void aRecreatedOrRemovedCollectionQueuesTheSameEviction() {
        EventSource session = mock(EventSource.class);
        TransactionCompletionCallbacks callbacks = mock(TransactionCompletionCallbacks.class);
        when(session.getTransactionCompletionCallbacks()).thenReturn(callbacks);

        PostCollectionRecreateEvent recreate = mock(PostCollectionRecreateEvent.class);
        when(recreate.getAffectedOwnerEntityName()).thenReturn(OWN_ENTITY);
        when(recreate.getSession()).thenReturn(session);
        eviction.onPostRecreateCollection(recreate);

        PostCollectionRemoveEvent remove = mock(PostCollectionRemoveEvent.class);
        when(remove.getAffectedOwnerEntityName()).thenReturn(OWN_ENTITY);
        when(remove.getSession()).thenReturn(session);
        eviction.onPostRemoveCollection(remove);

        ArgumentCaptor<AfterCompletionCallback> queued = ArgumentCaptor.forClass(AfterCompletionCallback.class);
        verify(callbacks, times(2)).registerCallback(queued.capture());
        queued.getAllValues().forEach(callback -> callback.doAfterTransactionCompletion(true, session));
        assertThat(stillCached(KEY_A)).as("an owner without a store clears every store").isFalse();
        assertThat(stillCached(KEY_B)).isFalse();
    }

    @Test
    void aCollectionOfAnotherPackagesEntityLeavesTheCachesAlone() {
        PostCollectionUpdateEvent update = mock(PostCollectionUpdateEvent.class);
        when(update.getAffectedOwnerEntityName()).thenReturn("io.namastack.outbox.OutboxRecord");
        eviction.onPostUpdateCollection(update);

        PostCollectionUpdateEvent ownerless = mock(PostCollectionUpdateEvent.class);
        eviction.onPostUpdateCollection(ownerless);

        assertThat(stillCached(KEY_A)).isTrue();
        assertThat(stillCached(KEY_B)).isTrue();
    }

    @Test
    void anExplicitEvictionWaitsForTheSpringTransactionItRunsIn() {
        TransactionSynchronizationManager.initSynchronization();
        eviction.evictAfterCommit(STORE_A);
        assertThat(stillCached(KEY_A)).isTrue();

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        assertThat(stillCached(KEY_A)).isFalse();
        assertThat(stillCached(KEY_B)).isTrue();

        TransactionSynchronizationManager.clearSynchronization();
        cached();
        eviction.evictAfterCommit(STORE_B);
        assertThat(stillCached(KEY_B)).as("outside a transaction it is immediate").isFalse();
        assertThat(stillCached(KEY_A)).isTrue();
    }
}
