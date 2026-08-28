package com.asrevo.cvhome.errors;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Carrying a checked {@link BaseException} through a stream pipeline and restoring it on the far side.
 *
 * <p>
 * The property that matters is that the exception which comes back out is the <em>same instance</em> that was
 * thrown, not a copy or a wrapper: the web layer renders it by its concrete type and payload, so a carrier that
 * leaked through would be rendered as an unclassified 500 instead of the status the code names.
 * </p>
 */
class UncheckedTest {

    private static final String BOOM = "boom";

    private static final String UNCHANGED = "unchanged";

    private static NoSuchProductException failure() {
        return new ErrorBuilder<>(CommonErrors.INTERNAL_ERROR, NoSuchProductException::new).detail(BOOM).build();
    }

    @Test
    void aMappingFunctionThatSucceedsIsTransparent() {
        assertThat(List.of(1, 2).stream().map(Unchecked.fn(value -> value * 2)).toList()).containsExactly(2, 4);
    }

    @Test
    void aConsumerThatSucceedsIsTransparent() {
        List<Integer> seen = new ArrayList<>();

        List.of(1, 2).forEach(Unchecked.consumer(seen::add));

        assertThat(seen).containsExactly(1, 2);
    }

    @Test
    void aPredicateThatSucceedsIsTransparent() {
        assertThat(List.of(1, 2, 3).stream().filter(Unchecked.predicate(value -> value > 1)).toList())
                .containsExactly(2, 3);
    }

    @Test
    void theCheckedExceptionComesBackOutAsItselfNotAsACarrier() throws BaseException {
        NoSuchProductException thrown = failure();

        assertThatThrownBy(() -> Unchecked.rethrow(() -> List.of(1).stream()
                .map(Unchecked.<Integer, Integer>fn(value -> {
                    throw thrown;
                }))
                .toList()))
                .isSameAs(thrown);

        assertThat(Unchecked.rethrow(() -> UNCHANGED)).isEqualTo(UNCHANGED);
    }

    @Test
    void aConsumerFailureIsRestoredToo() {
        NoSuchProductException thrown = failure();

        assertThatThrownBy(() -> Unchecked.rethrow(() -> List.of(1).forEach(Unchecked.consumer(value -> {
            throw thrown;
        })))).isSameAs(thrown);
    }

    @Test
    void aPredicateFailureIsRestoredToo() {
        NoSuchProductException thrown = failure();

        assertThatThrownBy(() -> Unchecked.rethrow(() -> List.of(1).stream()
                .filter(Unchecked.<Integer>predicate(value -> {
                    throw thrown;
                }))
                .toList())).isSameAs(thrown);
    }

    @Test
    void anOrdinaryRuntimeFailureIsLeftAloneRatherThanUnwrapped() {
        IllegalStateException other = new IllegalStateException(BOOM);

        assertThatThrownBy(() -> Unchecked.rethrow(() -> {
            throw other;
        })).isSameAs(other);
    }

    @Test
    void theVoidVariantRunsItsBlock() throws BaseException {
        List<Integer> seen = new ArrayList<>();

        Unchecked.rethrow(() -> seen.add(1));

        assertThat(seen).containsExactly(1);
    }

    /**
     * The category bases are abstract on purpose — the codebase forbids throwing one directly — so a test needs a
     * named condition of its own, exactly as a bounded context declares one.
     */
    private static final class NoSuchProductException extends ResourceNotFoundException {

        private NoSuchProductException(ErrorPayload payload, Throwable cause) {
            super(payload, cause);
        }
    }
}
