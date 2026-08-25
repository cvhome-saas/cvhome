package com.asrevo.cvhome.payment.api.errors;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.payment.errors.PaymentErrors;
import com.asrevo.cvhome.payment.errors.PaymentInitiateRejectedException;
import com.asrevo.cvhome.payment.errors.PaymentProviderUnavailableException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The caller-side view of a payment failure: whatever the payment service said about Stripe, the exception a caller
 * holds names payment as the remote and keeps the provider's details as params.
 */
class PaymentApiErrorsTest {

    private static final String PAYMENT = "payment";

    private static final String STRIPE = "stripe";

    private static final String PROVIDER = "provider";

    private static final String ORDER_REF = "order-1";

    private static final String INTERNAL_REF = "tx-1";

    private static final String DETAIL = "stripe rejected the payment for order order-1.";

    private static final int UNPROCESSABLE = 422;

    private static final int BAD_GATEWAY = 502;

    private static RemoteErrorContext context(String code, int status, String detail) {
        return new RemoteErrorContext(code, detail, Map.of(PROVIDER, STRIPE), List.of(), PAYMENT, status, null, null);
    }

    @Test
    void catalogMapsRejectedFailedAndUnreachable() {
        assertThat(PaymentApiErrors.CATALOG).isNotNull();
    }

    @Test
    void rejectionRebuiltFromTheWireNamesPaymentAsTheRemote() {
        PaymentGatewayRejectedException e = PaymentGatewayRejectedException.from(
                context(PaymentErrors.INITIATE_REJECTED.code(), UNPROCESSABLE, DETAIL));

        assertThat(e.errorCode()).isEqualTo(PaymentErrors.INITIATE_REJECTED);
        assertThat(e.remoteService()).isEqualTo(PAYMENT);
        assertThat(e.remoteCode()).isEqualTo(PaymentErrors.INITIATE_REJECTED.code());
        assertThat(e.remoteStatus()).isEqualTo(UNPROCESSABLE);
        assertThat(e.params()).containsEntry(PROVIDER, STRIPE);
        assertThat(e.payload().detail()).isEqualTo(DETAIL);
    }

    @Test
    void rejectionWrappedLocallyAnswersWithPaymentsStatusNotStripes() {
        PaymentInitiateRejectedException cause = PaymentInitiateRejectedException.of(STRIPE, ORDER_REF, INTERNAL_REF,
                "card_declined", 402, null);

        PaymentGatewayRejectedException e = PaymentGatewayRejectedException.wrapping(cause);

        assertThat(e.getCause()).isSameAs(cause);
        assertThat(e.remoteService()).isEqualTo(PAYMENT);
        assertThat(e.remoteCode()).isEqualTo(PaymentErrors.INITIATE_REJECTED.code());
        assertThat(e.remoteStatus()).isEqualTo(UNPROCESSABLE);
        assertThat(e.params()).containsEntry(PROVIDER, STRIPE).containsEntry("orderRef", ORDER_REF);
    }

    @Test
    void unavailableFromTheWireDefaultsTheDetailWhenNoneWasSent() {
        PaymentApiUnavailableException withDetail = PaymentApiUnavailableException.from(
                context(PaymentErrors.INITIATE_FAILED.code(), BAD_GATEWAY, DETAIL));
        PaymentApiUnavailableException unreachable = PaymentApiUnavailableException.from(
                context(null, 0, null));

        assertThat(withDetail.errorCode()).isEqualTo(CommonErrors.REMOTE_UNAVAILABLE);
        assertThat(withDetail.payload().detail()).isEqualTo(DETAIL);
        assertThat(withDetail.remoteCode()).isEqualTo(PaymentErrors.INITIATE_FAILED.code());
        assertThat(withDetail.remoteStatus()).isEqualTo(BAD_GATEWAY);
        assertThat(unreachable.payload().detail()).contains("could not be reached");
        assertThat(unreachable.remoteStatus()).isZero();
        assertThat(unreachable.remoteService()).isEqualTo(PAYMENT);
    }

    @Test
    void wrappingAProviderFailureReportsPaymentsStatusAndKeepsTheCode() {
        PaymentProviderUnavailableException cause = PaymentProviderUnavailableException.of(STRIPE, ORDER_REF,
                INTERNAL_REF, "api_connection_error", 0, null);

        PaymentApiUnavailableException e = PaymentApiUnavailableException.wrapping(cause);

        assertThat(e.getCause()).isSameAs(cause);
        assertThat(e.remoteCode()).isEqualTo(PaymentErrors.INITIATE_FAILED.code());
        assertThat(e.remoteStatus()).isEqualTo(BAD_GATEWAY);
        assertThat(e.params()).containsEntry(PROVIDER, STRIPE);
    }

    @Test
    void wrappingARemoteFailureKeepsItsStatusAndAPlainThrowableKeepsNothing() {
        PaymentGatewayRejectedException remote = PaymentGatewayRejectedException.from(
                context(PaymentErrors.INITIATE_REJECTED.code(), UNPROCESSABLE, DETAIL));
        RuntimeException plain = new RuntimeException("boom");

        PaymentApiUnavailableException fromRemote = PaymentApiUnavailableException.wrapping(remote);
        PaymentApiUnavailableException fromPlain = PaymentApiUnavailableException.wrapping(plain);

        assertThat(fromRemote.remoteStatus()).isEqualTo(UNPROCESSABLE);
        assertThat(fromRemote.remoteCode()).isEqualTo(PaymentErrors.INITIATE_REJECTED.code());
        assertThat(fromPlain.remoteCode()).isNull();
        assertThat(fromPlain.remoteStatus()).isZero();
        assertThat(fromPlain.getCause()).isSameAs(plain);
        assertThat(fromPlain.remoteService()).isEqualTo(PAYMENT);
    }

}
