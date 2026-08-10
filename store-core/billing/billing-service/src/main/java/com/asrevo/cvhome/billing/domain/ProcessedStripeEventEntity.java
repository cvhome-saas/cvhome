package com.asrevo.cvhome.billing.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.billing.commons.ProcessedEventOutcome;
import com.asrevo.cvhome.billing.commons.StripeEventId;

import lombok.Getter;

/**
 * A Stripe event we have already accepted, keyed by Stripe's own event id.
 *
 * <p>
 * This table is the inbound idempotency guarantee, and the primary key is what provides it. Stripe redelivers on any
 * non-2xx and can deliver the same event twice regardless; inserting here first means a duplicate loses the insert,
 * its transaction rolls back, and its retry finds the row already present and stops. Without it, a redelivered
 * {@code invoice.payment_succeeded} would extend a billing period twice.
 * </p>
 */
@Getter
@Table(schema = "billing", name = "processed_stripe_event")
public class ProcessedStripeEventEntity {

    @Id
    @Column("event_id")
    private StripeEventId eventId;

    @Column("event_type")
    private String eventType;

    @Column("api_version")
    private String apiVersion;

    @Column("outcome")
    private ProcessedEventOutcome outcome;

    @Column("received_at")
    private Instant receivedAt;

    @Column("processed_at")
    private Instant processedAt;

    @Version
    private Integer version;

    public static ProcessedStripeEventEntity received(StripeEventId eventId, String eventType, String apiVersion,
                                                      ProcessedEventOutcome outcome) {
        ProcessedStripeEventEntity entity = new ProcessedStripeEventEntity();
        entity.eventId = eventId;
        entity.eventType = eventType;
        entity.apiVersion = apiVersion;
        entity.outcome = outcome;
        entity.receivedAt = Instant.now();
        entity.processedAt = outcome == ProcessedEventOutcome.SCHEDULED ? null : Instant.now();
        return entity;
    }

}
