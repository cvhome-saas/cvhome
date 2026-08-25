package com.asrevo.cvhome.payment.entity.payment;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.model.payment.event.payment.PaymentCanceledEvent;
import com.asrevo.cvhome.payment.model.payment.event.payment.PaymentFailedEvent;
import com.asrevo.cvhome.payment.model.payment.event.payment.PaymentPaidEvent;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every settlement of a transaction registers the outbox event that tells checkout about it. The events are what
 * the order pipeline runs on, so a status change without one is a silent loss.
 */
class TransactionTest {

    private static final String INTERNAL_REF = "tx-1";

    private static final String REQUEST_REF = "order-1";

    private static final String STORE = "store-1";

    private static final String TRANSACTION_NO = "TXN-1";

    private static Transaction transaction() {
        Transaction tx = new Transaction();
        tx.setInternalRef(INTERNAL_REF);
        tx.setRequestRef(REQUEST_REF);
        tx.setStoreMerchantId(new StoreMerchantId(STORE));
        tx.setStatus(PaymentStatus.PENDING);
        return tx;
    }

    private static Collection<?> events(Transaction tx) {
        return ReflectionTestUtils.invokeMethod(tx, "domainEvents");
    }

    @Test
    void successIsPaidAndAnnounced() {
        Transaction tx = transaction();

        assertThat(tx.success()).isSameAs(tx);

        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(tx.getTransactionNo()).isNull();
        assertThat(events(tx)).singleElement().isInstanceOfSatisfying(PaymentPaidEvent.class, e -> {
            assertThat(e.internalRef()).isEqualTo(INTERNAL_REF);
            assertThat(e.requestRef()).isEqualTo(REQUEST_REF);
            assertThat(e.storeId()).isEqualTo(STORE);
        });
    }

    @Test
    void approvedSuccessKeepsTheTransactionNumber() {
        Transaction tx = transaction().success(TRANSACTION_NO);

        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(tx.getTransactionNo()).isEqualTo(TRANSACTION_NO);
        assertThat(events(tx)).singleElement().isInstanceOf(PaymentPaidEvent.class);
    }

    @Test
    void failureIsFailedAndAnnounced() {
        Transaction tx = transaction().failed();

        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(events(tx)).singleElement().isInstanceOf(PaymentFailedEvent.class);
    }

    @Test
    void cancellationIsCancelledAndAnnounced() {
        Transaction tx = transaction().canceled();

        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(events(tx)).singleElement().isInstanceOf(PaymentCanceledEvent.class);
    }

}
