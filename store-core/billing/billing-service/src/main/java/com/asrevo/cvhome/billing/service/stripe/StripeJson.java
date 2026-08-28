package com.asrevo.cvhome.billing.service.stripe;

import java.time.Instant;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Reads Stripe's raw event JSON.
 *
 * <p>
 * Deliberately reading the JSON rather than Stripe's deserialised model objects. The SDK's types are bound to the API
 * version the library was built against, while a webhook arrives in whatever version the account is pinned to, and
 * fields have moved between versions — {@code current_period_end} migrated from the subscription onto its items in
 * the 2025 versions. Reading the document lets a handler look in both places and keep working across an account's
 * version upgrade instead of throwing.
 * </p>
 *
 * <p>
 * Every accessor tolerates absence and returns {@code null}: a missing optional field is normal, and a handler that
 * exploded on one would fail an event Stripe will then redeliver forever.
 * </p>
 */
public final class StripeJson {

    private StripeJson() {
    }

    public static JsonObject parse(String raw) {
        return JsonParser.parseString(raw).getAsJsonObject();
    }

    public static String string(JsonObject object, String field) {
        JsonElement element = object == null ? null : object.get(field);
        return element == null || element.isJsonNull() ? null : element.getAsString();
    }

    public static Long number(JsonObject object, String field) {
        JsonElement element = object == null ? null : object.get(field);
        return element == null || element.isJsonNull() ? null : element.getAsLong();
    }

    public static boolean flag(JsonObject object, String field) {
        JsonElement element = object == null ? null : object.get(field);
        return element != null && !element.isJsonNull() && element.getAsBoolean();
    }

    public static JsonObject object(JsonObject object, String field) {
        JsonElement element = object == null ? null : object.get(field);
        return element == null || !element.isJsonObject() ? null : element.getAsJsonObject();
    }

    /**
     * The first element of a nested {@code data} array — the shape Stripe uses for a subscription's items and an
     * invoice's lines. A subscription carrying more than one item is not something this service creates.
     */
    public static JsonObject firstOfData(JsonObject object, String field) {
        JsonObject holder = object(object, field);
        if (holder == null) {
            return null;
        }
        JsonElement data = holder.get("data");
        if (data == null || !data.isJsonArray()) {
            return null;
        }
        JsonArray array = data.getAsJsonArray();
        return array.isEmpty() || !array.get(0).isJsonObject() ? null : array.get(0).getAsJsonObject();
    }

    /**
     * A Stripe epoch-seconds timestamp as an {@link Instant}, or {@code null} when unset.
     */
    public static Instant timestamp(JsonObject object, String field) {
        Long seconds = number(object, field);
        return seconds == null ? null : Instant.ofEpochSecond(seconds);
    }

}
