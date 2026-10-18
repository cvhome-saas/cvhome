package com.asrevo.cvhome.commons.domain;

import java.util.function.LongFunction;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;

/**
 * What the four numeric catalog ids share: a positive {@code long}, read from a JSON number or a bare digit string.
 */
final class LongIds {

    private LongIds() {
    }

    static long checked(Long value, String kind) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(String.format("not a %s: %s (expected a positive number)", kind, value));
        }
        return value;
    }

    static long parse(String text, String kind) {
        try {
            return checked(Long.parseLong(text.trim()), kind);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("not a %s: '%s' (expected a positive number)", kind, text), e);
        }
    }

    static <T> T read(JsonParser p, DeserializationContext ctxt, Class<T> type, LongFunction<T> constructor)
            throws JacksonException {
        JsonToken token = p.currentToken();
        try {
            if (token == JsonToken.VALUE_NUMBER_INT) {
                return constructor.apply(checked(p.getLongValue(), type.getSimpleName()));
            }
            if (token == JsonToken.VALUE_STRING) {
                return constructor.apply(parse(p.getString(), type.getSimpleName()));
            }
        } catch (IllegalArgumentException e) {
            throw ctxt.weirdStringException(p.getString(), type, e.getMessage());
        }
        return type.cast(ctxt.handleUnexpectedToken(type, p));
    }
}
