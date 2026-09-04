package com.asrevo.cvhome.sso.keys;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.realm.RealmContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The hourly signing-key tick.
 *
 * <p>
 * It runs <em>in the platform realm</em>, not in none. The keys themselves are deployment-wide and carry no realm,
 * but the work audits itself, and an audit row belongs to a realm — written from a thread with no realm it would
 * land under the sentinel realm, which is to say somewhere nobody reads. That is the whole reason this class is
 * not a bare method call.
 * </p>
 *
 * <p>
 * Retiring runs before rotating so a key past its window is gone before a new one is minted, and the realm is left
 * behind afterwards rather than leaking onto the pooled scheduler thread.
 * </p>
 */
class KeyRotationSchedulerTest {

    private final KeyRotationService keys = mock(KeyRotationService.class);
    private final KeyRotationScheduler scheduler = new KeyRotationScheduler(keys);

    @Test
    void theTickRunsInThePlatformRealmSoItsAuditRowsAreSomewhereReadable() {
        Optional<RealmId>[] seen = asArray();
        when(keys.retireDue()).thenAnswer(invocation -> {
            seen[0] = RealmContext.current();
            return 0;
        });

        scheduler.tick();

        assertThat(seen[0]).contains(RealmId.PLATFORM);
    }

    @Test
    void retiringHappensBeforeRotatingSoAkeyPastItsWindowIsGoneFirst() {
        when(keys.retireDue()).thenReturn(1);
        when(keys.rotateIfDue()).thenReturn(true);

        scheduler.tick();

        org.mockito.InOrder order = org.mockito.Mockito.inOrder(keys);
        order.verify(keys).retireDue();
        order.verify(keys).rotateIfDue();
    }

    @Test
    void aquietTickStillLeavesNoRealmBehindOnTheSchedulerThread() {
        when(keys.retireDue()).thenReturn(0);
        when(keys.rotateIfDue()).thenReturn(false);

        scheduler.tick();

        assertThat(RealmContext.current()).isEmpty();
        verify(keys).rotateIfDue();
    }

    @SuppressWarnings("unchecked")
    private static Optional<RealmId>[] asArray() {
        return new Optional[] {Optional.empty()};
    }

}
