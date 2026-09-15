package com.asrevo.cvhome.cache.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The transport a service has until it needs one: every event is applied on the task that drained it and written
 * to the log at DEBUG, so the flow can be watched end to end on a stack before any fan-out exists. The other tasks
 * and the other services stay on their regions' time-to-live, as they were before the events.
 */
public final class LoggingCacheEventTransport implements CacheEventTransport {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingCacheEventTransport.class);

    @Override
    public String name() {
        return "logging";
    }

    @Override
    public void publish(CacheEvent event) {
        LOG.debug("cache event {} of store {} applied locally, no transport configured: {}", event.eventType(),
                event.store().getId(), event.data());
    }
}
