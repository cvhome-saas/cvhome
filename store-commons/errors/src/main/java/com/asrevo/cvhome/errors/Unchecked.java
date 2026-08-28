package com.asrevo.cvhome.errors;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Bridges checked {@link BaseException}s across APIs that cannot declare them — chiefly {@code java.util.function}
 * types used in stream pipelines.
 *
 * <p>
 * Wrap the lambda, then restore the checked type once at the enclosing method:
 * </p>
 *
 * <pre>{@code
 * public List<ReadableProduct> readAll(List<Product> products) throws BaseException {
 *     return Unchecked.rethrow(() -> products.stream()
 *             .map(Unchecked.fn(mapper::map))
 *             .toList());
 * }
 * }</pre>
 *
 * <p>
 * Forgetting the outer {@code rethrow} is not a correctness bug: the carrier reaching the web layer is unwrapped and
 * rendered with the right status. It only costs the compile-time signature, which is the point of the checked style.
 * </p>
 */
public final class Unchecked {

    private Unchecked() {
    }

    /**
     * Adapts a mapping function that throws, for use in {@code map}, {@code flatMap} and similar.
     */
    public static <T, R> Function<T, R> fn(ThrowingFunction<T, R> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (BaseException e) {
                throw new UncheckedBaseException(e);
            }
        };
    }

    /**
     * Adapts a consumer that throws, for use in {@code forEach} and similar.
     */
    public static <T> Consumer<T> consumer(ThrowingConsumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (BaseException e) {
                throw new UncheckedBaseException(e);
            }
        };
    }

    /**
     * Adapts a predicate that throws, for use in {@code filter} and similar.
     */
    public static <T> Predicate<T> predicate(ThrowingPredicate<T> predicate) {
        return t -> {
            try {
                return predicate.test(t);
            } catch (BaseException e) {
                throw new UncheckedBaseException(e);
            }
        };
    }

    /**
     * Runs a block and converts any {@link UncheckedBaseException} raised inside it back into the checked exception it
     * was carrying. Use this at the method that declares {@code throws BaseException}.
     */
    public static <T> T rethrow(Supplier<T> block) throws BaseException {
        try {
            return block.get();
        } catch (UncheckedBaseException e) {
            throw e.getCause();
        }
    }

    /**
     * Void variant of {@link #rethrow(Supplier)}.
     */
    public static void rethrow(Runnable block) throws BaseException {
        try {
            block.run();
        } catch (UncheckedBaseException e) {
            throw e.getCause();
        }
    }

    /**
     * A {@link Function} that is allowed to fail.
     *
     * @param <T> input type
     * @param <R> result type
     */
    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        R apply(T t) throws BaseException;
    }

    /**
     * A {@link Consumer} that is allowed to fail.
     *
     * @param <T> input type
     */
    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T t) throws BaseException;
    }

    /**
     * A {@link Predicate} that is allowed to fail.
     *
     * @param <T> input type
     */
    @FunctionalInterface
    public interface ThrowingPredicate<T> {
        boolean test(T t) throws BaseException;
    }

}
