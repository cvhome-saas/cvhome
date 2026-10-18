package com.asrevo.cvhome.cache.eviction;

import java.util.List;

import org.hibernate.engine.spi.TransactionCompletionCallbacks;
import org.hibernate.engine.spi.TransactionCompletionCallbacks.AfterCompletionCallback;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PostCollectionRecreateEvent;
import org.hibernate.event.spi.PostCollectionRemoveEvent;
import org.hibernate.event.spi.PostCollectionUpdateEvent;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.cache.CacheKey;
import com.asrevo.cvhome.cache.CacheRegions;
import com.asrevo.cvhome.cache.CacheRegistry;
import com.asrevo.cvhome.cache.RegionCache;
import com.asrevo.cvhome.cache.Stores;
import com.asrevo.cvhome.cache.TestRegions;
import com.asrevo.cvhome.cache.config.CacheProperties;
import com.asrevo.cvhome.cache.provider.caffeine.CaffeineCacheProvider;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.StoreScoped;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A committed write to an entity with a rule drops its store's entries from the rule's regions and no other
 * store's; an entity without a rule, one that names no store, a failed commit and a collection change before its
 * commit leave everything warm.
 */
class CommitEvictionListenerTest {

    private static final String VALUE = "v";

    private static final CacheKey A = CacheKey.of(Stores.A, Stores.EN);

    private static final CacheKey B = CacheKey.of(Stores.B, Stores.EN);

    private CacheRegistry registry;

    private CommitEvictionListener listener;

    /** An entity of the service with a rule, naming its store. */
    record Product(StoreMerchantId store) implements StoreScoped {

        @Override
        public StoreMerchantId scopedStore() {
            return store;
        }
    }

    /** An entity of the service with a rule that cannot name its store. */
    record Placeless() implements StoreScoped {

        @Override
        public StoreMerchantId scopedStore() {
            return null;
        }
    }

    /** An entity of the service nobody wrote a rule for. */
    record Audit(StoreMerchantId store) implements StoreScoped {

        @Override
        public StoreMerchantId scopedStore() {
            return store;
        }
    }

    @BeforeEach
    void setUp() {
        registry = new CacheRegistry(CacheRegions.of(TestRegions.values()), List.of(new CaffeineCacheProvider()),
                CacheProperties.defaults());
        EvictionRules rules = EvictionRules.in("com.asrevo.cvhome.cache.eviction")
                .on(Product.class, Placeless.class).evict(TestRegions.PRODUCT).build();
        listener = new CommitEvictionListener(rules, registry);
        for (TestRegions region : List.of(TestRegions.PRODUCT, TestRegions.LISTING)) {
            RegionCache<String> cache = registry.region(region, String.class);
            cache.put(A, VALUE);
            cache.put(B, VALUE);
        }
    }

    private boolean warm(TestRegions region, CacheKey key) {
        return registry.region(region, String.class).getIfPresent(key).isPresent();
    }

    private static EntityPersister persisterOf(Class<?> type) {
        EntityPersister persister = mock(EntityPersister.class);
        when(persister.getMappedClass()).thenAnswer(invocation -> type);
        return persister;
    }

    @Test
    void aCommittedInsertUpdateOrDeleteDropsTheStoresEntriesOfTheRulesRegions() {
        PostInsertEvent insert = mock(PostInsertEvent.class);
        when(insert.getEntity()).thenReturn(new Product(Stores.A));

        listener.onPostInsert(insert);

        assertThat(warm(TestRegions.PRODUCT, A)).isFalse();
        assertThat(warm(TestRegions.PRODUCT, B)).as("the other store stays warm").isTrue();
        assertThat(warm(TestRegions.LISTING, A)).as("a region the rule does not name stays warm").isTrue();

        registry.region(TestRegions.PRODUCT, String.class).put(A, VALUE);
        PostUpdateEvent update = mock(PostUpdateEvent.class);
        when(update.getEntity()).thenReturn(new Product(Stores.A));
        listener.onPostUpdate(update);
        assertThat(warm(TestRegions.PRODUCT, A)).isFalse();

        registry.region(TestRegions.PRODUCT, String.class).put(A, VALUE);
        PostDeleteEvent delete = mock(PostDeleteEvent.class);
        when(delete.getEntity()).thenReturn(new Product(Stores.A));
        listener.onPostDelete(delete);
        assertThat(warm(TestRegions.PRODUCT, A)).isFalse();
    }

    @Test
    void anEntityWithoutARuleOrAStoreAndAFailedCommitLeaveEverythingWarm() {
        PostInsertEvent audit = mock(PostInsertEvent.class);
        when(audit.getEntity()).thenReturn(new Audit(Stores.A));
        PostUpdateEvent placeless = mock(PostUpdateEvent.class);
        when(placeless.getEntity()).thenReturn(new Placeless());
        PostDeleteEvent failed = mock(PostDeleteEvent.class);
        when(failed.getEntity()).thenReturn(new Product(Stores.A));

        listener.onPostInsert(audit);
        listener.onPostUpdate(placeless);
        listener.onPostInsertCommitFailed(audit);
        listener.onPostUpdateCommitFailed(placeless);
        listener.onPostDeleteCommitFailed(failed);
        listener.evictFor(null);

        assertThat(warm(TestRegions.PRODUCT, A)).isTrue();
        assertThat(warm(TestRegions.PRODUCT, B)).isTrue();
        assertThat(listener.requiresPostCommitHandling(persisterOf(Product.class))).isTrue();
        assertThat(listener.requiresPostCommitHandling(persisterOf(String.class))).isFalse();
    }

    @Test
    void aCollectionChangeEvictsOnlyOnceItsTransactionCommits() {
        TransactionCompletionCallbacks callbacks = mock(TransactionCompletionCallbacks.class);
        EventSource session = mock(EventSource.class);
        when(session.getTransactionCompletionCallbacks()).thenReturn(callbacks);
        PostCollectionUpdateEvent update = mock(PostCollectionUpdateEvent.class);
        when(update.getAffectedOwnerOrNull()).thenReturn(new Product(Stores.A));
        when(update.getSession()).thenReturn(session);

        listener.onPostUpdateCollection(update);

        assertThat(warm(TestRegions.PRODUCT, A)).as("not before the commit").isTrue();
        ArgumentCaptor<AfterCompletionCallback> captor = ArgumentCaptor.forClass(AfterCompletionCallback.class);
        verify(callbacks).registerCallback(captor.capture());
        captor.getValue().doAfterTransactionCompletion(false, session);
        assertThat(warm(TestRegions.PRODUCT, A)).as("a rolled-back transaction changed nothing").isTrue();
        captor.getValue().doAfterTransactionCompletion(true, session);
        assertThat(warm(TestRegions.PRODUCT, A)).isFalse();
        assertThat(warm(TestRegions.PRODUCT, B)).isTrue();

        PostCollectionRecreateEvent recreate = mock(PostCollectionRecreateEvent.class);
        when(recreate.getAffectedOwnerOrNull()).thenReturn(new Audit(Stores.A));
        PostCollectionRemoveEvent remove = mock(PostCollectionRemoveEvent.class);
        when(remove.getAffectedOwnerOrNull()).thenReturn(null);
        listener.onPostRecreateCollection(recreate);
        listener.onPostRemoveCollection(remove);
        verify(recreate, never()).getSession();
        verify(remove, never()).getSession();
    }
}
