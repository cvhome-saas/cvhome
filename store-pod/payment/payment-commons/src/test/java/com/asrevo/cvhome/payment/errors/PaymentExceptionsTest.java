package com.asrevo.cvhome.payment.errors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ExternalProviderException;
import com.asrevo.cvhome.errors.ResourceNotFoundException;
import com.asrevo.cvhome.errors.ValidationException;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The typed failures of the payment context. Each factory is the whole contract a caller can act on — code, category,
 * params and provider metadata — so each is pinned here rather than left to the wire.
 */
class PaymentExceptionsTest {

    private static final String STRIPE = "stripe";

    private static final String ORDER_REF = "order-1";

    private static final String INTERNAL_REF = "tx-1";

    private static final String PROVIDER = "provider";

    private static final String SIGNATURE_PRESENT = "signaturePresent";

    private static final String STRING_TYPE = "String";

    private static final String EVENT_ID = "evt_1";

    private static final String EVENT_TYPE = "checkout.session.completed";

    private static final String CARD_DECLINED = "card_declined";

    private static final int PAYMENT_REQUIRED = 402;

    private static final RuntimeException CAUSE = new RuntimeException("boom");

    @ParameterizedTest
    @EnumSource(PaymentErrors.class)
    void everyCodeIsNamespacedUnderPayment(PaymentErrors error) {
        assertThat(error.code()).startsWith("PAYMENT.");
        assertThat(error.category()).isNotNull();
    }

    @Test
    void rejectionIsAnUnprocessableAnswerCarryingTheProvidersOwnCode() {
        PaymentInitiateRejectedException e = PaymentInitiateRejectedException.of(STRIPE, ORDER_REF, INTERNAL_REF,
                CARD_DECLINED, PAYMENT_REQUIRED, CAUSE);

        assertThat(e).isInstanceOf(ExternalProviderException.class);
        assertThat(e.errorCode()).isEqualTo(PaymentErrors.INITIATE_REJECTED);
        assertThat(e.category()).isEqualTo(ErrorCategory.UNPROCESSABLE);
        assertThat(e.provider()).isEqualTo(STRIPE);
        assertThat(e.providerCode()).isEqualTo(CARD_DECLINED);
        assertThat(e.providerStatus()).isEqualTo(PAYMENT_REQUIRED);
        assertThat(e.params()).containsEntry(PROVIDER, STRIPE)
                .containsEntry("orderRef", ORDER_REF)
                .containsEntry("internalReference", INTERNAL_REF);
        assertThat(e.payload().detail()).contains(ORDER_REF);
        assertThat(e.getCause()).isSameAs(CAUSE);
    }

    @Test
    void unavailabilityIsARemoteServiceFailureWithNoStatusWhenNothingAnswered() {
        PaymentProviderUnavailableException e = PaymentProviderUnavailableException.of(STRIPE, ORDER_REF,
                INTERNAL_REF, null, 0, CAUSE);

        assertThat(e.errorCode()).isEqualTo(PaymentErrors.INITIATE_FAILED);
        assertThat(e.category()).isEqualTo(ErrorCategory.REMOTE_SERVICE);
        assertThat(e.provider()).isEqualTo(STRIPE);
        assertThat(e.providerCode()).isNull();
        assertThat(e.providerStatus()).isZero();
        assertThat(e.params()).containsEntry(PROVIDER, STRIPE);
    }

    @Test
    void configurationNotFoundIsA404NamingTypeAndStore() {
        PaymentConfigurationNotFoundException e = PaymentConfigurationNotFoundException.of(PaymentType.STRIPE, ORDER_REF);

        assertThat(e).isInstanceOf(ResourceNotFoundException.class);
        assertThat(e.errorCode()).isEqualTo(PaymentErrors.CONFIGURATION_NOT_FOUND);
        assertThat(e.category().httpStatus()).isEqualTo(ErrorCategory.NOT_FOUND.httpStatus());
        assertThat(e.params()).containsEntry("paymentType", PaymentType.STRIPE).containsEntry("store", ORDER_REF);
    }

    @Test
    void invalidSignatureRecordsWhetherAnyHeaderWasSent() {
        InvalidWebhookSignatureException unsigned = InvalidWebhookSignatureException.verificationFailed(STRIPE, false,
                CAUSE);
        InvalidWebhookSignatureException wrongKey = InvalidWebhookSignatureException.verificationFailed(STRIPE, true,
                CAUSE);

        assertThat(unsigned).isInstanceOf(ValidationException.class);
        assertThat(unsigned.errorCode()).isEqualTo(PaymentErrors.WEBHOOK_SIGNATURE_INVALID);
        assertThat(unsigned.params()).containsEntry(SIGNATURE_PRESENT, false);
        assertThat(wrongKey.params()).containsEntry(SIGNATURE_PRESENT, true);
        assertThat(wrongKey.getCause()).isSameAs(CAUSE);
    }

    @Test
    void unreadablePayloadNamesTheEvent() {
        UnreadableWebhookPayloadException e = UnreadableWebhookPayloadException.of(STRIPE, EVENT_ID, EVENT_TYPE, CAUSE);

        assertThat(e.errorCode()).isEqualTo(PaymentErrors.WEBHOOK_PAYLOAD_UNREADABLE);
        assertThat(e.params()).containsEntry("eventId", EVENT_ID).containsEntry("eventType", EVENT_TYPE);
        assertThat(e.payload().detail()).contains(STRIPE);
    }

    @Test
    void unexpectedObjectNamesTheTypeTheRouterExpected() {
        UnexpectedWebhookObjectException e = UnexpectedWebhookObjectException.of(STRIPE, EVENT_ID, EVENT_TYPE,
                String.class, CAUSE);

        assertThat(e.errorCode()).isEqualTo(PaymentErrors.WEBHOOK_UNEXPECTED_OBJECT);
        assertThat(e.params()).containsEntry("expectedType", STRING_TYPE).containsEntry(PROVIDER, STRIPE);
        assertThat(e.payload().detail()).contains(STRING_TYPE);
    }

}
