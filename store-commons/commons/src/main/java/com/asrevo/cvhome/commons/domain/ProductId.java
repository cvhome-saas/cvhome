package com.asrevo.cvhome.commons.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * The id of a product of a store's catalogue: what a product page, a relationship read and inventory's rows name.
 *
 * <p>
 * A positive number, carried as a JSON number and read back from one or from a digit string; bound from a path or
 * query parameter by {@link #of(String)}. A typed id rather than a {@code Long} so a cache key, a method signature and
 * a log line say what they hold, and so the wrong kind of id cannot be passed where another is expected.
 * </p>
 */
@JsonDeserialize(using = ProductId.Reader.class)
public record ProductId(Long value) implements Identifier, KeyPart, Comparable<ProductId> {

    private static final String KIND = "product id";

    public ProductId {
        LongIds.checked(value, KIND);
    }

    public static ProductId of(long value) {
        return new ProductId(value);
    }

    /** From a path or query parameter; Spring finds this factory when it binds a {@code String}. */
    public static ProductId of(String text) {
        return new ProductId(LongIds.parse(text, KIND));
    }

    @JsonValue
    @Override
    public Long value() {
        return value;
    }

    @Override
    public Object getId() {
        return value;
    }

    @Override
    public String cacheKeyPart() {
        return Long.toString(value);
    }

    @Override
    public int compareTo(ProductId other) {
        return Long.compare(value, other.value);
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    /** Reads the number, or a digit string, back through the constructor; anything else is a format error. */
    static final class Reader extends StdDeserializer<ProductId> {

        Reader() {
            super(ProductId.class);
        }

        @Override
        public ProductId deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            return LongIds.read(p, ctxt, ProductId.class, ProductId::new);
        }
    }
}
