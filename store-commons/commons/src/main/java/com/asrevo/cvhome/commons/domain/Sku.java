package com.asrevo.cvhome.commons.domain;

import java.io.Serializable;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A stock-keeping unit: the key a sellable variant is known by in every pod. Catalog mints it (one per
 * {@code product_variant}), inventory keys stock and price by it, checkout keys cart and order lines by it.
 *
 * <p>
 * The format is enforced here and nowhere else: letters, digits, {@code _} and {@code -}, at most 255 characters —
 * the width of every {@code sku} column. It is compared exactly. Case is kept and nothing is trimmed, because each
 * per-store unique key compares exactly and a normalised value would stop matching the rows already written.
 * </p>
 *
 * <p>
 * On the wire it is a bare JSON string, so no consumer — storefront, console, load tests — sees the type, and its
 * {@link #toString()} is that string, which is how an {@code @HttpExchange} client writes it into a query parameter. A
 * request <em>body</em> keeps a {@code String} annotated {@code @Pattern(regexp = Sku.FORMAT)} instead: bean validation
 * names the offending field in its 400, a failing JSON creator cannot.
 * </p>
 *
 * <p>
 * Ordered as its string is: inventory locks rows in sku order so two overlapping reservations cannot deadlock, and
 * that order must not change under it.
 * </p>
 */
public record Sku(String value) implements Serializable, Comparable<Sku> {

    /**
     * The whole rule, for {@code @Pattern} on a request body.
     */
    public static final String FORMAT = "^[A-Za-z0-9_-]{1,255}$";

    private static final Pattern RULE = Pattern.compile(FORMAT);

    public Sku {
        if (value == null || !RULE.matcher(value).matches()) {
            throw new IllegalArgumentException(String.format("not a sku: '%s' (expected %s)", value, FORMAT));
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Sku of(String value) {
        return new Sku(value);
    }

    @JsonValue
    @Override
    public String value() {
        return value;
    }

    @Override
    public int compareTo(Sku other) {
        return value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
