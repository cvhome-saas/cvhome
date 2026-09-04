package com.asrevo.cvhome.sso.realm;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.RealmId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Which realms a deployment serves, and how a new one comes into being.
 *
 * <p>
 * <strong>Only positive answers are cached.</strong> A realm that does not exist yet may exist a moment later —
 * that is the whole point of {@link RealmRegistry#ensure} — and caching "no" would keep a newly provisioned store
 * broken for the length of the cache with nothing to show for it.
 * </p>
 *
 * <p>
 * {@code all()} always includes the platform realm even when no store row names it, because the schedulers run
 * with no request and so with no realm to take from one, and the audit rows for their work are written there. A
 * list that omitted it would drop those rows on the floor.
 * </p>
 */
class RealmRegistryTest {

    private static final RealmId STORE = RealmId.of("store-1");
    private static final String FIXED = "platform";

    private final RealmRepository realms = mock(RealmRepository.class);
    private final SsoRealmProperties properties = new SsoRealmProperties();
    private final RealmRegistry registry = new RealmRegistry(realms, properties);

    @Test
    void asingleRealmDeploymentServesItsOneFixedRealmAndAsksNoQuestions() {
        properties.setMode(RealmMode.SINGLE);
        properties.setFixed(FIXED);

        assertThat(registry.all()).containsExactly(RealmId.of(FIXED));
        verify(realms, never()).findAllByEnabledTrue();
    }

    @Test
    void amultiRealmDeploymentServesEveryEnabledStoreAndAlwaysThePlatformRealm() {
        properties.setMode(RealmMode.MULTI);
        when(realms.findAllByEnabledTrue()).thenReturn(List.of(new Realm(STORE.getId(), "Store One")));

        // The schedulers run with no request, and their audit rows are written in the platform realm.
        assertThat(registry.all()).containsExactly(RealmId.PLATFORM, STORE);
    }

    @Test
    void aplatformRowInTheTableIsNotListedTwice() {
        properties.setMode(RealmMode.MULTI);
        when(realms.findAllByEnabledTrue())
                .thenReturn(List.of(new Realm(RealmId.PLATFORM.getId(), "Platform")));

        assertThat(registry.all()).containsExactly(RealmId.PLATFORM);
    }

    @Test
    void anExistingRealmIsAnsweredFromTheCacheOnTheSecondAsk() {
        when(realms.existsByIdAndEnabledTrue(STORE.getId())).thenReturn(true);

        assertThat(registry.exists(STORE)).isTrue();
        assertThat(registry.exists(STORE)).isTrue();

        // Asked on the way in to every request; the answer changes about as often as a store is created.
        verify(realms, times(1)).existsByIdAndEnabledTrue(STORE.getId());
    }

    @Test
    void amissingRealmIsNeverCachedSoAnewStoreIsNotKeptBrokenUntilItExpires() {
        when(realms.existsByIdAndEnabledTrue(STORE.getId())).thenReturn(false);

        assertThat(registry.exists(STORE)).isFalse();
        assertThat(registry.exists(STORE)).isFalse();

        verify(realms, times(2)).existsByIdAndEnabledTrue(STORE.getId());
    }

    @Test
    void ensureCreatesTheRealmTheFirstTimeAndThenLeavesItAlone() {
        when(realms.existsByIdAndEnabledTrue(STORE.getId())).thenReturn(false);

        registry.ensure(STORE);

        verify(realms).save(org.mockito.ArgumentMatchers.argThat(realm -> STORE.getId().equals(realm.getId())));
    }

    @Test
    void ensureIsAnoOpForArealmThatAlreadyExists() {
        when(realms.existsByIdAndEnabledTrue(STORE.getId())).thenReturn(true);

        registry.ensure(STORE);

        verify(realms, never()).save(org.mockito.ArgumentMatchers.any(Realm.class));
    }

    @Test
    void arealmCreatedByEnsureIsCachedSoTheNextRequestDoesNotHitTheTable() {
        when(realms.existsByIdAndEnabledTrue(STORE.getId())).thenReturn(false);
        registry.ensure(STORE);

        assertThat(registry.exists(STORE)).isTrue();

        // One lookup from ensure itself; the second answer comes from the cache the save populated.
        verify(realms, times(1)).existsByIdAndEnabledTrue(STORE.getId());
    }

}
