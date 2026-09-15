package com.asrevo.cvhome.cache.event;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.cache.Stores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class LoggingCacheEventTransportTest {

    @Test
    void theDefaultTransportOnlyLogs() {
        LoggingCacheEventTransport transport = new LoggingCacheEventTransport();

        assertThat(transport.name()).isEqualTo("logging");
        assertThatCode(() -> transport.publish(new StoreChanged(Stores.A))).doesNotThrowAnyException();
    }
}
