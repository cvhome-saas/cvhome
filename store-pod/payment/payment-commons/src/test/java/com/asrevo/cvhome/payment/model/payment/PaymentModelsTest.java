package com.asrevo.cvhome.payment.model.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.payment.model.payment.event.payment.PaymentCanceledEvent;
import com.asrevo.cvhome.payment.model.payment.event.payment.PaymentFailedEvent;
import com.asrevo.cvhome.payment.model.payment.event.payment.PaymentPaidEvent;
import com.asrevo.cvhome.payment.model.payment.event.webhook.WebhookEvent;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The value objects the payment API speaks: a request refuses to exist half-built, results know when they need a
 * redirect, and the outbox events expose the keys operators search by.
 */
class PaymentModelsTest {

    private static final String REF = "order-9";

    private static final String INTERNAL_REF = "tx-9";

    private static final String STORE = "store-9";

    private static final String URL = "https://pay.example/session";

    private static final String STORE_ID_KEY = "storeId";

    private static final String PAYMENT_TYPE_KEY = "paymentType";

    private static final String INTERNAL_REF_KEY = "internalRef";

    private static final String REQUEST_REF_KEY = "requestRef";

    private static PaymentRequest.PaymentRequestBuilder valid() {
        return PaymentRequest.builder()
                .ref(REF)
                .amount(BigDecimal.TEN)
                .currency(new CurrencyCode("USD"))
                .paymentType(PaymentType.COD)
                .expireAt(Instant.EPOCH)
                .successUrl(URL)
                .cancelUrl(URL);
    }

    @Test
    void completeRequestBuilds() {
        PaymentRequest request = valid().build();

        assertThat(request.ref()).isEqualTo(REF);
        assertThat(request.paymentType()).isEqualTo(PaymentType.COD);
    }

    @Test
    void everyMissingFieldIsRefusedByName() {
        assertThatThrownBy(() -> valid().ref(null).build()).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ref");
        assertThatThrownBy(() -> valid().amount(null).build()).hasMessageContaining("amount");
        assertThatThrownBy(() -> valid().currency(null).build()).hasMessageContaining("currency");
        assertThatThrownBy(() -> valid().paymentType(null).build()).hasMessageContaining(PAYMENT_TYPE_KEY);
        assertThatThrownBy(() -> valid().expireAt(null).build()).hasMessageContaining("expireAt");
        assertThatThrownBy(() -> valid().successUrl(null).build()).hasMessageContaining("successUrl");
        assertThatThrownBy(() -> valid().cancelUrl(null).build()).hasMessageContaining("cancelUrl");
    }

    @Test
    void initiateResultFactoriesAndRedirect() {
        assertThat(PaymentInitiateResult.failed().status()).isEqualTo(PaymentInitiateStatus.FAILED);
        assertThat(PaymentInitiateResult.failed(INTERNAL_REF).gatewayRef()).isEqualTo(INTERNAL_REF);
        assertThat(PaymentInitiateResult.pending().status()).isEqualTo(PaymentInitiateStatus.PENDING);
        assertThat(PaymentInitiateResult.pending().shouldRedirect()).isFalse();
        assertThat(PaymentInitiateResult.builder().redirectUrl(URL).build().shouldRedirect()).isTrue();
    }

    @Test
    void responseFactoriesAndRedirect() {
        assertThat(PaymentResponse.failed().status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(PaymentResponse.failed(INTERNAL_REF).gatewayRef()).isEqualTo(INTERNAL_REF);
        assertThat(PaymentResponse.pending().status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(PaymentResponse.pending().shouldRedirect()).isFalse();
        assertThat(PaymentResponse.builder().redirectUrl(URL).build().shouldRedirect()).isTrue();
    }

    @Test
    void noneUseCaseCarriesNoReference() {
        WebhookResult none = WebhookResult.noneUseCase();

        assertThat(none.paymentUseCase()).isEqualTo(PaymentUseCase.NONE);
        assertThat(none.internalReference()).isNull();
    }

    @Test
    void paymentEventsExposeTheirKeys() {
        PaymentPaidEvent paid = PaymentPaidEvent.from(INTERNAL_REF, REF, STORE);
        PaymentFailedEvent failed = PaymentFailedEvent.from(INTERNAL_REF, REF, STORE);
        PaymentCanceledEvent canceled = PaymentCanceledEvent.from(INTERNAL_REF, REF, STORE);

        assertThat(paid.eventType()).isEqualTo("PaymentPaidEvent");
        assertThat(failed.eventType()).isEqualTo("PaymentFailedEvent");
        assertThat(canceled.eventType()).isEqualTo("PaymentCanceledEvent");
        for (Map<String, String> data : new Map[] {paid.data(), failed.data(), canceled.data()}) {
            assertThat(data).containsEntry(INTERNAL_REF_KEY, INTERNAL_REF)
                    .containsEntry(REQUEST_REF_KEY, REF)
                    .containsEntry(STORE_ID_KEY, STORE);
        }
    }

    @Test
    void webhookEventExposesStoreAndType() {
        WebhookEvent event = new WebhookEvent(STORE, PaymentType.STRIPE, "{}", Map.of());

        assertThat(event.eventType()).isEqualTo("WebhookEvent");
        assertThat(event.data()).containsEntry(STORE_ID_KEY, STORE).containsEntry(PAYMENT_TYPE_KEY, "STRIPE");
    }

}
