package com.asrevo.cvhome.billing.repository;

import java.time.Instant;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.billing.commons.StripeEventId;
import com.asrevo.cvhome.billing.domain.ProcessedStripeEventEntity;

public interface ProcessedStripeEventRepository
        extends CrudRepository<ProcessedStripeEventEntity, StripeEventId> {

    /**
     * Claims a Stripe event for processing, atomically.
     *
     * <p>
     * {@code ON CONFLICT DO NOTHING} rather than {@code save} plus a caught constraint violation, for the same reason
     * as the trial grant: on Postgres a violation aborts the enclosing transaction, so the caller could not go on to
     * do anything useful afterwards. Returning 0 leaves the transaction clean and tells the caller this event has
     * already been seen.
     * </p>
     *
     * @return 1 when this call claimed the event, 0 when it was already recorded
     */
    @Modifying
    @Query("""
            insert into billing.processed_stripe_event
                (event_id, event_type, api_version, outcome, received_at, processed_at, version)
            values (:eventId, :eventType, :apiVersion, :outcome, :receivedAt, :processedAt, 0)
            on conflict (event_id) do nothing
            """)
    int claim(@Param("eventId") String eventId, @Param("eventType") String eventType,
              @Param("apiVersion") String apiVersion, @Param("outcome") String outcome,
              @Param("receivedAt") Instant receivedAt, @Param("processedAt") Instant processedAt);

    /**
     * Inbound events that failed, in a window.
     *
     * <p>
     * Nothing has ever read this table. Together with {@code StripeRequestRepository.countStalledSince} it is the
     * only "billing is broken right now" signal the platform has — a non-zero figure means webhooks are arriving and
     * not being applied, which is invisible from every other screen because the subscriptions simply stop moving.
     * </p>
     */
    @Query("""
            select count(*) from billing.processed_stripe_event
            where outcome = 'FAILED' and received_at >= :since""")
    long countFailedSince(Instant since);

}
