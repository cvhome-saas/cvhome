package com.asrevo.cvhome.cache.event;

/**
 * Where a cache event goes after this task has applied it: the port the cross-task and cross-service refresh is
 * built behind.
 *
 * <p>
 * The outbox delivers an event to one task of the service that raised it. The other tasks of that service, and
 * the services that hold a copy of the same data through a client, learn of it through whatever bean implements
 * this: the logging transport by default, later an HTTP fan-out to every replica of the consuming services, a
 * broker (RabbitMQ, Redis pub-sub), or nothing at all once a region lives on a shared provider, where the applier
 * updates the one copy and there is nobody left to tell. Choosing is a bean, never a switch: a service declares
 * its own {@code CacheEventTransport} and the default steps aside.
 * </p>
 */
public interface CacheEventTransport {

    /** A short name for the start-up log and the metrics. */
    String name();

    /** Tells the transport's audience about {@code event}; must not throw for a transient fault it can retry. */
    void publish(CacheEvent event);
}
