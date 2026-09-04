package com.asrevo.cvhome.billing.domain;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.asrevo.cvhome.billing.commons.ProcessedEventOutcome;
import com.asrevo.cvhome.billing.commons.StripeEventId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The idempotency row that stops a redelivered Stripe event being applied twice.
 *
 * <p>
 * The one decision it makes is when {@code processedAt} is stamped: a scheduled event has not been applied yet, so
 * it stays null until something applies it, while any other outcome is finished on arrival. Stamping it eagerly
 * would make a crashed handler look like a completed one, and the redelivery Stripe sends would be skipped.
 * </p>
 */
class ProcessedStripeEventEntityTest {

    private static final StripeEventId EVENT = new StripeEventId("evt_1");
    private static final String TYPE = "invoice.paid";
    private static final String API_VERSION = "2024-06-20";

    @Test
    void aScheduledEventIsReceivedButNotYetProcessed() {
        Instant before = Instant.now();

        ProcessedStripeEventEntity row = ProcessedStripeEventEntity.received(EVENT, TYPE, API_VERSION,
                ProcessedEventOutcome.SCHEDULED);

        assertThat(row.getEventId()).isEqualTo(EVENT);
        assertThat(row.getEventType()).isEqualTo(TYPE);
        assertThat(row.getApiVersion()).isEqualTo(API_VERSION);
        assertThat(row.getReceivedAt()).isBetween(before, Instant.now());
        assertThat(row.getProcessedAt()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = ProcessedEventOutcome.class, mode = EnumSource.Mode.EXCLUDE, names = "SCHEDULED")
    void everyOtherOutcomeIsFinishedOnArrival(ProcessedEventOutcome outcome) {
        ProcessedStripeEventEntity row =
                ProcessedStripeEventEntity.received(EVENT, TYPE, API_VERSION, outcome);

        assertThat(row.getOutcome()).isEqualTo(outcome);
        assertThat(row.getProcessedAt()).isNotNull();
    }
}
