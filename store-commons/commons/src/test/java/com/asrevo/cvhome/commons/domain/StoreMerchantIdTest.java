package com.asrevo.cvhome.commons.domain;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The wire contract of a store id.
 *
 * <p>
 * {@code StoreMerchantId.Reader} exists so that outbox rows and stored event payloads written before the two
 * store-id types were merged still deserialize: they hold {@code {"id":"…"}} (the old store-core
 * {@code ManagerStoreId}) or {@code {"storeMerchantId":"…"}} (this record's default shape), while everything
 * written since is a bare string. An outbox entry that cannot be deserialized is an event that is never handled,
 * so all three shapes are pinned here rather than left to be rediscovered in production.
 * </p>
 */
class StoreMerchantIdTest {

    private static final String HEX = "65f023632bc46470c104b76f";
    private static final String BARE = "\"%s\"".formatted(HEX);
    private static final String RECORD_SHAPE = "{\"storeMerchantId\":\"%s\"}".formatted(HEX);
    private static final String LEGACY_SHAPE = "{\"id\":\"%s\"}".formatted(HEX);
    private static final String BOTH_SHAPES = "{\"id\":\"older\",\"storeMerchantId\":\"%s\"}".formatted(HEX);

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void aBareStringIsTheCurrentWireShape() {
        assertThat(mapper.readValue(BARE, StoreMerchantId.class)).isEqualTo(new StoreMerchantId(HEX));
    }

    @Test
    void theRecordShapeStillReadsBecauseOutboxRowsPredateTheMerge() {
        assertThat(mapper.readValue(RECORD_SHAPE, StoreMerchantId.class)).isEqualTo(new StoreMerchantId(HEX));
    }

    @Test
    void theLegacyManagerStoreIdShapeStillReads() {
        assertThat(mapper.readValue(LEGACY_SHAPE, StoreMerchantId.class)).isEqualTo(new StoreMerchantId(HEX));
    }

    @Test
    void storeMerchantIdWinsWhenAPayloadCarriesBothKeys() {
        assertThat(mapper.readValue(BOTH_SHAPES, StoreMerchantId.class)).isEqualTo(new StoreMerchantId(HEX));
    }

    @Test
    void anObjectCarryingNeitherKeyReadsAsNullRatherThanThrowing() {
        assertThat(mapper.readValue("{\"unrelated\":1}", StoreMerchantId.class)).isNull();
    }

    @Test
    void anExplicitJsonNullValueReadsAsNull() {
        assertThat(mapper.readValue("{\"storeMerchantId\":null}", StoreMerchantId.class)).isNull();
    }

    @Test
    void aScalarThatIsNotAStringReadsAsNullRatherThanThrowing() {
        // The reader falls through to the object branch, finds neither key on a number node and yields null.
        // Rejecting a malformed value is the HTTP edge's job -- see ServletStoreMerchantIdArgumentResolver.
        assertThat(mapper.readValue("12", StoreMerchantId.class)).isNull();
    }

    @Test
    void itSerializesBackToTheBareStringItNowReads() {
        assertThat(mapper.writeValueAsString(new StoreMerchantId(HEX))).isEqualTo(BARE);
    }

    @Test
    void theWildcardSentinelSurvivesTheRoundTrip() {
        // The security layer uses "*" for a principal with access to every store, so the type must not validate it away.
        StoreMerchantId wildcard = new StoreMerchantId("*");
        assertThat(mapper.readValue(mapper.writeValueAsString(wildcard), StoreMerchantId.class)).isEqualTo(wildcard);
    }

    @Test
    void newIdMintsDistinctObjectIdHex() {
        assertThat(StoreMerchantId.newId().getId()).hasSize(24).isNotEqualTo(StoreMerchantId.newId().getId());
    }

    @Test
    void idsSortByTheirHexValue() {
        assertThat(new StoreMerchantId("aaa").compareTo(new StoreMerchantId("bbb"))).isNegative();
        assertThat(new StoreMerchantId(HEX).compareTo(new StoreMerchantId(HEX))).isZero();
    }
}
