package com.asrevo.cvhome.billing.commons;

import java.io.Serializable;

/**
 * What one {@link EntitlementKey} is worth on a given plan.
 *
 * <p>
 * Exactly one of the two fields is set, or neither: a null {@code limitValue} on a numeric key means <em>unlimited</em>
 * rather than zero. That distinction is the whole point of the type — a plan that omits a ceiling must not read as a
 * plan that forbids the feature.
 * </p>
 *
 * @param key         what is being granted
 * @param limitValue  the ceiling for a numeric key, or {@code null} for unlimited
 * @param flagValue   whether a capability key is granted, or {@code null} for a numeric key
 */
public record EntitlementValue(EntitlementKey key, Integer limitValue, Boolean flagValue) implements Serializable {

    public static EntitlementValue limit(EntitlementKey key, Integer limitValue) {
        return new EntitlementValue(key, limitValue, null);
    }

    public static EntitlementValue flag(EntitlementKey key, boolean flagValue) {
        return new EntitlementValue(key, null, flagValue);
    }

    public static EntitlementValue unlimited(EntitlementKey key) {
        return new EntitlementValue(key, null, null);
    }

    /**
     * Whether no ceiling applies. A capability key is never "unlimited"; it is granted or it is not.
     */
    public boolean unlimited() {
        return key.numeric() && limitValue == null;
    }

    /**
     * Whether a capability is granted. Numeric keys answer {@code false} — ask {@link #exceeded(int)} instead.
     */
    public boolean granted() {
        return Boolean.TRUE.equals(flagValue);
    }

    /**
     * Whether {@code current} has already reached the ceiling, i.e. whether one more would be too many.
     *
     * <p>
     * An unlimited or non-numeric key is never exceeded, so a plan that simply does not mention a key does not
     * accidentally block the feature it never meant to cap.
     * </p>
     */
    public boolean exceeded(int current) {
        return !unlimited() && key.numeric() && current >= limitValue;
    }

}
