package com.asrevo.cvhome.billing.repository;

import java.time.Instant;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import com.asrevo.cvhome.billing.domain.StripeRequestEntity;

public interface StripeRequestRepository extends CrudRepository<StripeRequestEntity, String> {

    /**
     * Mutating Stripe calls that were recorded and never completed.
     *
     * <p>
     * {@code stripe_request} records the <em>intent</em> of a call before it is made, so a row whose
     * {@code completed_at} is still null well after it was written is a call that did not come back — a crash
     * between the insert and the response, or a provider timeout. That is safe to replay under the same idempotency
     * key, which is the whole point of the table; it is also worth an operator knowing about, and nothing has ever
     * read it.
     * </p>
     *
     * <p>
     * The cut-off is a parameter rather than a constant because "recent" here means "younger than the longest call
     * we expect", and a request made a second ago is in flight rather than stalled.
     * </p>
     */
    @Query("""
            select count(*) from billing.stripe_request
            where completed_at is null and created_at < :before""")
    long countStalledBefore(Instant before);

}
