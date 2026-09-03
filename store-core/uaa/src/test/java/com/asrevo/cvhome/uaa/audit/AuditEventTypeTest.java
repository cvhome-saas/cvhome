package com.asrevo.cvhome.uaa.audit;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The wire names are the API: they are what a filter, an export and a saved query all hold. */
class AuditEventTypeTest {

    private static final String UNKNOWN = "nope.nope";

    @Test
    void everyWireNameIsUnique() {
        Set<String> seen = new HashSet<>();
        for (AuditEventType type : AuditEventType.values()) {
            assertThat(seen.add(type.wire())).as(type.wire()).isTrue();
        }
    }

    @Test
    void roundTripsThroughTheWireName() {
        for (AuditEventType type : AuditEventType.values()) {
            assertThat(AuditEventType.fromWire(type.wire())).isEqualTo(type);
            assertThat(type.category()).isNotNull();
        }
    }

    @Test
    void refusesAnUnknownName() {
        assertThatThrownBy(() -> AuditEventType.fromWire(UNKNOWN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(UNKNOWN);
    }

}
