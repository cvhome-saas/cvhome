package com.asrevo.cvhome.billing.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.billing.commons.StripeRequestOperation;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

import lombok.Getter;

/**
 * The intent to make one mutating Stripe call, keyed by the idempotency key it was made under.
 *
 * <p>
 * Records intent, not outcome. A crash between writing this row and Stripe answering leaves {@code completed_at}
 * null — and that is exactly what makes a retry safe, because replaying under the same key gets Stripe's stored
 * answer instead of charging twice. Do not "clean up" rows that never completed; they are the record that a call may
 * have happened.
 * </p>
 */
@Getter
@Table(schema = "billing", name = "stripe_request")
public class StripeRequestEntity {

    @Id
    @Column("idempotency_key")
    private String idempotencyKey;

    @Column("store_id")
    private ManagerStoreId storeId;

    @Column("operation")
    private StripeRequestOperation operation;

    @Column("stripe_object_id")
    private String stripeObjectId;

    @Column("created_at")
    private Instant createdAt;

    @Column("completed_at")
    private Instant completedAt;

    @Version
    private Integer version;

    public static StripeRequestEntity intent(String idempotencyKey, ManagerStoreId store,
                                             StripeRequestOperation operation) {
        StripeRequestEntity entity = new StripeRequestEntity();
        entity.idempotencyKey = idempotencyKey;
        entity.storeId = store;
        entity.operation = operation;
        entity.createdAt = Instant.now();
        return entity;
    }

    public StripeRequestEntity completed(String objectId) {
        this.stripeObjectId = objectId;
        this.completedAt = Instant.now();
        return this;
    }

}
