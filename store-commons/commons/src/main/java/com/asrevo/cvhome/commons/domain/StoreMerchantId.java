package com.asrevo.cvhome.commons.domain;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonValue;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * A store's identifier, everywhere — store-core and the pods alike.
 *
 * <p>
 * Carried as a {@code String} rather than an {@link ObjectId} because the pods persist it as a plain column
 * ({@code STORE_MERCHANT_ID}) on some twenty JPA entities, and because nothing outside {@link #newId()} has any use
 * for the structure inside an ObjectId. The value is still ObjectId hex: tenancy mints every store id here, and
 * store-core stores it as {@code varchar(24)} while the pods use {@code varchar(50)}.
 * </p>
 *
 * <p>
 * <b>Deliberately unvalidated.</b> The security layer uses the non-hex sentinel {@code "*"} for a principal with
 * access to every store, and cua derives ids from OAuth2 client ids. Rejecting a malformed value is therefore the
 * HTTP edge's job — see {@code ServletStoreMerchantIdArgumentResolver} — not this constructor's.
 * </p>
 */
@JsonDeserialize(using = StoreMerchantId.Reader.class)
public record StoreMerchantId(String storeMerchantId) implements Identifier, Comparable<StoreMerchantId> {

    /**
     * Mints a new store id. Only tenancy has any business calling this — a store id is created once, when the store is.
     */
    public static StoreMerchantId newId() {
        return new StoreMerchantId(new ObjectId().toHexString());
    }

    @JsonValue
    @Override
    public String getId() {
        return this.storeMerchantId;
    }

    @Override
    public int compareTo(StoreMerchantId o) {
        return this.storeMerchantId.compareTo(o.storeMerchantId);
    }

    /**
     * Reads a store id from a bare string, and still from the two object shapes this type used to serialize as.
     *
     * <p>
     * The legacy shapes are not politeness: outbox rows and stored event payloads written before the two store-id
     * types were merged hold {@code {"id":"…"}} (the old store-core {@code ManagerStoreId}) or
     * {@code {"storeMerchantId":"…"}} (this record's default record shape). Those rows outlive the deploy that
     * changed the format, and an outbox entry that cannot be deserialized is an event that is never handled.
     * </p>
     */
    static final class Reader extends StdDeserializer<StoreMerchantId> {

        Reader() {
            super(StoreMerchantId.class);
        }

        @Override
        public StoreMerchantId deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            if (p.currentToken() == JsonToken.VALUE_STRING) {
                return new StoreMerchantId(p.getString());
            }
            JsonNode node = p.readValueAsTree();
            JsonNode value = node.get("storeMerchantId");
            if (value == null) {
                value = node.get("id");
            }
            return value == null || value.isNull() ? null : new StoreMerchantId(value.asString());
        }

    }

}
