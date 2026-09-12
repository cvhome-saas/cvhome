package com.asrevo.cvhome.commons.domain;

import java.io.Serializable;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonValue;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;

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
 * names the offending field in its 400, a failing JSON reader cannot.
 * </p>
 *
 * <p>
 * Ordered as its string is: inventory locks rows in sku order so two overlapping reservations cannot deadlock, and
 * that order must not change under it.
 * </p>
 */
@JsonDeserialize(using = Sku.Reader.class)
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

    /**
     * Reads the bare string back through the constructor.
     *
     * <p>
     * A deserializer rather than a delegating {@code @JsonCreator}, like {@code StoreMerchantId.Reader}: the creator's
     * {@code JsonCreator.Mode} enum is compiled into every class that reads this one, and a module without Jackson's
     * annotations on its classpath warned about it on every build. A value that is not a sku is a format error
     * Jackson reports as one, so a request body carrying it is a 400, never a 500.
     * </p>
     */
    static final class Reader extends StdDeserializer<Sku> {

        Reader() {
            super(Sku.class);
        }

        @Override
        public Sku deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            if (p.currentToken() != JsonToken.VALUE_STRING) {
                return (Sku) ctxt.handleUnexpectedToken(Sku.class, p);
            }
            String text = p.getString();
            try {
                return new Sku(text);
            } catch (IllegalArgumentException e) {
                throw ctxt.weirdStringException(text, Sku.class, e.getMessage());
            }
        }
    }
}
