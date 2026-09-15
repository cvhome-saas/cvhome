package com.asrevo.cvhome.aot;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.aot.AotServices;

import io.namastack.outbox.OutboxProcessingScheduler;
import io.namastack.outbox.annotation.OutboxEvent;
import io.namastack.outbox.annotation.OutboxHandler;
import io.namastack.outbox.instance.OutboxInstanceRegistry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The outbox can run its own schedule and deliver to our handlers in a native image. Each gap failed a running
 * service, not a build.
 */
class OutboxRuntimeHintsTest {

    private static RuntimeHints registered() {
        RuntimeHints hints = new RuntimeHints();
        new OutboxRuntimeHints().registerHints(hints, OutboxRuntimeHintsTest.class.getClassLoader());
        return hints;
    }

    @Test
    void theLibrarysHeartbeatAndPollerMayBeInvoked() throws NoSuchMethodException {
        RuntimeHints hints = registered();

        // The exact method the native billing service failed to invoke, and the poller scheduled the same way.
        assertThat(RuntimeHintsPredicates.reflection()
                .onMethodInvocation(OutboxInstanceRegistry.class.getMethod("performHeartbeatAndCleanup"))).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
                .onMethodInvocation(OutboxProcessingScheduler.class.getMethod("process"))).accepts(hints);
    }

    @Test
    void aHandlerMayBeInvokedAndItsPayloadIsBindable() throws NoSuchMethodException {
        RuntimeHints hints = registered();

        assertThat(RuntimeHintsPredicates.reflection()
                .onMethodInvocation(StubHandler.class.getMethod("on", StubPayload.class))).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection().onType(TypeReference.of(StubPayload.class))).accepts(hints);
    }

    @Test
    void anEventsKeyAccessorMayBeCalledBySpel() throws NoSuchMethodException {
        // uaa's UserCreatedEvent carries @OutboxEvent(key = "#this.userId()"); SpEL calls the accessor reflectively.
        assertThat(RuntimeHintsPredicates.reflection()
                .onMethodInvocation(StubEvent.class.getMethod("storeId"))).accepts(registered());
    }

    @Test
    void theRegistrarIsDiscoveredByAotProcessingWithoutABean() {
        assertThat(AotServices.factories().load(RuntimeHintsRegistrar.class))
                .anyMatch(OutboxRuntimeHints.class::isInstance);
    }

    /** Stands in for billing's and tenancy's outbox handlers. */
    public static class StubHandler {

        @OutboxHandler
        public void on(StubPayload payload) {
            // Delivery is the library's; only the signature matters here.
        }

    }

    public record StubPayload(String storeId, String type) {
    }

    /** Stands in for UserCreatedEvent and the billing and tenancy events. */
    @OutboxEvent(key = "#this.storeId()")
    public record StubEvent(String storeId) {
    }

}
