package com.asrevo.cvhome.sso.realm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.RealmId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/** The realm a thread is working in: entered explicitly, restored on the way out, never defaulted. */
class RealmContextTest {

    private static final RealmId STORE_A = RealmId.of("aaaaaaaaaaaaaaaaaaaaaaaa");

    private static final RealmId STORE_B = RealmId.of("bbbbbbbbbbbbbbbbbbbbbbbb");

    @AfterEach
    void clear() {
        // A leaked realm would make the next test pass for the wrong reason.
        assertThat(RealmContext.current()).isEmpty();
    }

    @Test
    void hasNoRealmUntilOneIsEntered() {
        assertThat(RealmContext.current()).isEmpty();
    }

    /**
     * The whole point of the class: no default. A resolver that guesses turns a wiring mistake into a
     * cross-tenant read.
     */
    @Test
    void requireRefusesToGuessARealm() {
        assertThatIllegalStateException().isThrownBy(RealmContext::require)
                .withMessageContaining("No realm has been entered");
    }

    @Test
    void exposesTheRealmInsideTheScope() {
        RealmContext.runIn(STORE_A, () -> assertThat(RealmContext.require()).isEqualTo(STORE_A));
    }

    @Test
    void callInReturnsTheValue() {
        String id = RealmContext.callIn(STORE_A, () -> RealmContext.require().getId());
        assertThat(id).isEqualTo(STORE_A.getId());
    }

    @Test
    void restoresTheOuterRealmRatherThanClearingIt() {
        RealmContext.runIn(STORE_A, () -> {
            RealmContext.runIn(STORE_B, () -> assertThat(RealmContext.require()).isEqualTo(STORE_B));
            assertThat(RealmContext.require()).isEqualTo(STORE_A);
        });
    }

    @Test
    void clearsTheRealmWhenTheScopeThrows() {
        try {
            RealmContext.runIn(STORE_A, () -> {
                throw new IllegalArgumentException("boom");
            });
        } catch (IllegalArgumentException expected) {
            // the finally block is what is under test, not the exception
        }
        assertThat(RealmContext.current()).isEmpty();
    }

    /** A realm entered on one thread must not be visible on another — it is per-thread state, not global. */
    @Test
    void doesNotLeakToAnotherThread() {
        RealmId[] seen = new RealmId[1];
        RealmContext.runIn(STORE_A, () -> {
            Thread other = new Thread(() -> seen[0] = RealmContext.current().orElse(null));
            other.start();
            try {
                other.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertThat(seen[0]).isNull();
    }

}
