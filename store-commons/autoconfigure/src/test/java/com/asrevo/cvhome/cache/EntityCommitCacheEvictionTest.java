package com.asrevo.cvhome.cache;

import java.util.List;

import jakarta.persistence.EntityManagerFactory;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
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
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A committed write to one of the service's own entities clears its read caches; anything else leaves them alone.
 *
 * <p>
 * Content and catalog cache what the storefront reads and neither emits an event for every change, so this listener is
 * what keeps a merchant's save from being hidden behind a cached copy on the task that took it.
 * </p>
 */
class EntityCommitCacheEvictionTest {

    private static final String CACHE = "READS";

    private static final String KEY = "key";

    private static final String OWN_ENTITY = Owned.class.getName();

    private final EventListenerRegistry registry = mock(EventListenerRegistry.class);

    private final ConcurrentMapCacheManager caches = new ConcurrentMapCacheManager(CACHE);

    private EntityCommitCacheEviction eviction;

    /** Stands in for an entity of the service: it lives under the package the listener watches. */
    static final class Owned {
    }

    @BeforeEach
    void setUp() {
        EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);
        SessionFactoryImplementor sessionFactory = mock(SessionFactoryImplementor.class);
        when(entityManagerFactory.unwrap(SessionFactoryImplementor.class)).thenReturn(sessionFactory);
        when(sessionFactory.getEventListenerRegistry()).thenReturn(registry);
        eviction = new EntityCommitCacheEviction(entityManagerFactory, caches, "com.asrevo.cvhome.cache", List.of(CACHE));
        cached();
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private void cached() {
        caches.getCache(CACHE).put(KEY, "value");
    }

    private boolean stillCached() {
        return caches.getCache(CACHE).get(KEY) != null;
    }

    private static EntityPersister persisterOf(Class<?> type) {
        EntityPersister persister = mock(EntityPersister.class);
        when(persister.getMappedClass()).thenAnswer(invocation -> type);
        return persister;
    }

    @Test
    void itListensForEveryCommittedWriteAndEveryCollectionChange() {
        verify(registry).appendListeners(EventType.POST_COMMIT_INSERT, eviction);
        verify(registry).appendListeners(EventType.POST_COMMIT_UPDATE, eviction);
        verify(registry).appendListeners(EventType.POST_COMMIT_DELETE, eviction);
        verify(registry).appendListeners(EventType.POST_COLLECTION_RECREATE, eviction);
        verify(registry).appendListeners(EventType.POST_COLLECTION_UPDATE, eviction);
        verify(registry).appendListeners(EventType.POST_COLLECTION_REMOVE, eviction);
        assertThat(eviction.requiresPostCommitHandling(persisterOf(Owned.class))).isTrue();
    }

    @Test
    void anInsertUpdateOrDeleteOfItsOwnEntityClearsTheCaches() {
        PostInsertEvent insert = mock(PostInsertEvent.class);
        when(insert.getPersister()).thenAnswer(invocation -> persisterOf(Owned.class));
        eviction.onPostInsert(insert);
        assertThat(stillCached()).isFalse();

        cached();
        PostUpdateEvent update = mock(PostUpdateEvent.class);
        when(update.getPersister()).thenAnswer(invocation -> persisterOf(Owned.class));
        eviction.onPostUpdate(update);
        assertThat(stillCached()).isFalse();

        cached();
        PostDeleteEvent delete = mock(PostDeleteEvent.class);
        when(delete.getPersister()).thenAnswer(invocation -> persisterOf(Owned.class));
        eviction.onPostDelete(delete);
        assertThat(stillCached()).isFalse();
    }

    @Test
    void anotherPackagesEntityAndAFailedCommitLeaveTheCachesAlone() {
        PostInsertEvent outbox = mock(PostInsertEvent.class);
        when(outbox.getPersister()).thenAnswer(invocation -> persisterOf(String.class));
        eviction.onPostInsert(outbox);

        eviction.onPostInsertCommitFailed(mock(PostInsertEvent.class));
        eviction.onPostUpdateCommitFailed(mock(PostUpdateEvent.class));
        eviction.onPostDeleteCommitFailed(mock(PostDeleteEvent.class));

        assertThat(stillCached()).isTrue();
    }

    @Test
    void aCollectionChangeInsideATransactionClearsTheCachesOnlyOnceItCommits() {
        TransactionSynchronizationManager.initSynchronization();
        PostCollectionUpdateEvent update = mock(PostCollectionUpdateEvent.class);
        when(update.getAffectedOwnerEntityName()).thenReturn(OWN_ENTITY);

        eviction.onPostUpdateCollection(update);
        assertThat(stillCached()).as("cleared at flush, a read could cache the old rows again").isTrue();

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        assertThat(stillCached()).isFalse();
    }

    @Test
    void aCollectionChangeOutsideATransactionClearsTheCachesAtOnce() {
        PostCollectionRecreateEvent recreate = mock(PostCollectionRecreateEvent.class);
        when(recreate.getAffectedOwnerEntityName()).thenReturn(OWN_ENTITY);
        eviction.onPostRecreateCollection(recreate);
        assertThat(stillCached()).isFalse();

        cached();
        PostCollectionRemoveEvent remove = mock(PostCollectionRemoveEvent.class);
        when(remove.getAffectedOwnerEntityName()).thenReturn(OWN_ENTITY);
        eviction.onPostRemoveCollection(remove);
        assertThat(stillCached()).isFalse();
    }

    @Test
    void aCollectionOfAnotherPackagesEntityLeavesTheCachesAlone() {
        PostCollectionUpdateEvent update = mock(PostCollectionUpdateEvent.class);
        when(update.getAffectedOwnerEntityName()).thenReturn("io.namastack.outbox.OutboxRecord");
        eviction.onPostUpdateCollection(update);

        PostCollectionUpdateEvent ownerless = mock(PostCollectionUpdateEvent.class);
        eviction.onPostUpdateCollection(ownerless);

        assertThat(stillCached()).isTrue();
    }
}
