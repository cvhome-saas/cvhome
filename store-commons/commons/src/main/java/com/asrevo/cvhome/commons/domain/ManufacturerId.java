package com.asrevo.cvhome.commons.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * The id of a brand of a store's catalogue: what a listing's filter names.
 *
 * <p>
 * A positive number, carried as a JSON number and read back from one or from a digit string; bound from a path or
 * query parameter by {@link #of(String)}. A typed id rather than a {@code Long} so a cache key, a method signature and
 * a log line say what they hold, and so the wrong kind of id cannot be passed where another is expected.
 * </p>
 */
@JsonDeserialize(using = ManufacturerId.Reader.class)
public record ManufacturerId(Long value) implements Identifier, KeyPart, Comparable<ManufacturerId> {

    private static final String KIND = "manufacturer id";

    public ManufacturerId {
        LongIds.checked(value, KIND);
    }

    public static ManufacturerId of(long value) {
        return new ManufacturerId(value);
    }

    /** From a path or query parameter; Spring finds this factory when it binds a {@code String}. */
    public static ManufacturerId of(String text) {
        return new ManufacturerId(LongIds.parse(text, KIND));
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
    public int compareTo(ManufacturerId other) {
        return Long.compare(value, other.value);
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    /** Reads the number, or a digit string, back through the constructor; anything else is a format error. */
    static final class Reader extends StdDeserializer<ManufacturerId> {

        Reader() {
            super(ManufacturerId.class);
        }

        @Override
        public ManufacturerId deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            return LongIds.read(p, ctxt, ManufacturerId.class, ManufacturerId::new);
        }
    }
}
