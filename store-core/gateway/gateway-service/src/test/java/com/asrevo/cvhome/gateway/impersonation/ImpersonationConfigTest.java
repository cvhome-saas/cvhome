package com.asrevo.cvhome.gateway.impersonation;

import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The beans are built as declared: a UTC clock and a plain client for uaa. */
class ImpersonationConfigTest {

    private final ImpersonationConfig config = new ImpersonationConfig();

    @Test
    void theClockIsUtc() {
        assertThat(config.impersonationClock().getZone()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    void theUaaClientIsBuilt() {
        assertThat(config.impersonationUaaClient()).isNotNull();
    }

}
