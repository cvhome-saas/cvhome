package com.asrevo.cvhome.cache;

import java.util.Objects;

import com.asrevo.cvhome.commons.domain.KeyPart;

/**
 * A criteria object as a cache key part: its identity is the hash of its canonical text, and the object rides
 * along for the loader.
 *
 * <p>
 * A listing or search is asked for with a mutable, request-bound object that must never sit in a key. The read's
 * owner renders it once ({@code criteria.normalised()}) and wraps it here; the key generator sees a part that
 * reads as the hash, the {@code @Cacheable} method unwraps {@link #criteria()} for the service behind it. Two
 * wrappers are equal when their texts are, whatever the objects.
 * </p>
 *
 * @param <T> the criteria type
 */
public final class QueryKey<T> implements KeyPart {

    private final QueryHash hash;

    private final transient T criteria;

    private QueryKey(QueryHash hash, T criteria) {
        this.hash = hash;
        this.criteria = criteria;
    }

    /** Wraps {@code criteria}, whose canonical text is {@code normalised}. */
    public static <T> QueryKey<T> of(String normalised, T criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("a query key carries its criteria");
        }
        return new QueryKey<>(QueryHash.of(normalised), criteria);
    }

    public T criteria() {
        return criteria;
    }

    public QueryHash hash() {
        return hash;
    }

    @Override
    public String cacheKeyPart() {
        return hash.value();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof QueryKey<?> that && hash.equals(that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return hash.value();
    }
}
