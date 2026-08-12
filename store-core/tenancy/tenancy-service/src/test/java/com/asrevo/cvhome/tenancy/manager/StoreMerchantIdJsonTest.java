package com.asrevo.cvhome.tenancy.manager;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.tenancy.events.store.StoreCreatedEvent;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The wire format of a store id, and its tolerance for the two shapes it used to have.
 *
 * <p>
 * Lives here rather than beside {@link StoreMerchantId} because {@code store-commons:commons} is a library module
 * with no test harness, and tenancy is the service that both mints store ids and publishes the events whose stored
 * payloads these guarantees are about.
 * </p>
 *
 * <p>
 * The legacy cases are the point. Before store-core's {@code ManagerStoreId} and the pods' {@code StoreMerchantId}
 * were merged, the same id serialized two different ways, and outbox rows written by the previous release still hold
 * those shapes. An event that cannot be deserialized is an event that is never handled, so reading them is not
 * optional.
 * </p>
 */
@Tag("unit-test")
class StoreMerchantIdJsonTest {

    private static final String HEX = "65f023632bc46470c104b76f";

    private static final String BARE = "\"65f023632bc46470c104b76f\"";

    /** How store-core's {@code ManagerStoreId} used to serialize. */
    private static final String LEGACY_OBJECT_ID = "{\"id\":\"65f023632bc46470c104b76f\"}";

    /** How this record serialized before it gained {@code @JsonValue}. */
    private static final String LEGACY_RECORD = "{\"storeMerchantId\":\"65f023632bc46470c104b76f\"}";

    private static final String LEGACY_EVENT = "{\"store\":{\"id\":\"65f023632bc46470c104b76f\"},\"data\":{}}";

    private static final String STORE_AS_BARE_STRING = "\"store\":\"65f023632bc46470c104b76f\"";

    private static final StoreMerchantId STORE = new StoreMerchantId(HEX);

    /**
     * Unknown properties are tolerated because the event carries a derived {@code eventType()} that is written but is
     * not a record component; that is orthogonal to what these tests are about.
     */
    private final ObjectMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @Test
    void serializesAsABareString() {
        assertThat(mapper.writeValueAsString(STORE)).isEqualTo(BARE);
    }

    @Test
    void readsABareString() {
        assertThat(mapper.readValue(BARE, StoreMerchantId.class)).isEqualTo(STORE);
    }

    @Test
    void readsTheLegacyManagerStoreIdShape() {
        assertThat(mapper.readValue(LEGACY_OBJECT_ID, StoreMerchantId.class)).isEqualTo(STORE);
    }

    @Test
    void readsTheLegacyRecordShape() {
        assertThat(mapper.readValue(LEGACY_RECORD, StoreMerchantId.class)).isEqualTo(STORE);
    }

    @Test
    void readsAnExplicitNull() {
        assertThat(mapper.readValue("{\"id\":null}", StoreMerchantId.class)).isNull();
    }

    @Test
    void roundTripsAnEventPayload() {
        StoreCreatedEvent event = new StoreCreatedEvent(STORE, null, null, Map.of(), null);
        String json = mapper.writeValueAsString(event);

        assertThat(json).contains(STORE_AS_BARE_STRING);
        assertThat(mapper.readValue(json, StoreCreatedEvent.class).store()).isEqualTo(STORE);
    }

    /**
     * An outbox row written by the release before the merge, read by the release after it.
     */
    @Test
    void readsAnEventPayloadStoredInTheLegacyShape() {
        assertThat(mapper.readValue(LEGACY_EVENT, StoreCreatedEvent.class).store()).isEqualTo(STORE);
    }

}
