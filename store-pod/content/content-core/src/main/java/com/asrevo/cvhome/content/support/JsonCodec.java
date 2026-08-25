package com.asrevo.cvhome.content.support;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * One lenient mapper for the JSON columns ({@code meta}, revision snapshots). Unknown properties are ignored so a
 * snapshot written by an older build still restores.
 */
public final class JsonCodec {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private JsonCodec() {
    }

    public static String write(Object value) {
        return value == null ? null : MAPPER.writeValueAsString(value);
    }

    public static <T> T read(String json, Class<T> type) {
        return json == null || json.isBlank() ? null : MAPPER.readValue(json, type);
    }

}
