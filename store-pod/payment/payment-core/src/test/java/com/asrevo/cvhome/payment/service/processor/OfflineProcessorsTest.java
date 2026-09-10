package com.asrevo.cvhome.payment.service.processor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.errors.InvalidWebhookSignatureException;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateStatus;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentUseCase;
import com.asrevo.cvhome.payment.models.ReadablePaymentConfiguration;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Cash on delivery and manual transfer have no provider: they open a pending payment a seller settles by hand, and
 * they have no webhook to speak of.
 */
class OfflineProcessorsTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-1");

    private static final String URL = "https://shop.example";

    private static final String EMPTY_BODY = "{}";

    static Stream<Arguments> processors() {
        return Stream.of(Arguments.of(new CODProcessor(), PaymentType.COD),
                Arguments.of(new ManualTransferredProcessor(), PaymentType.MANUAL_TRANSFER));
    }

    @ParameterizedTest
    @MethodSource("processors")
    void opensAPendingPaymentWithoutRedirectAndIgnoresWebhooks(PaymentProcessor processor, PaymentType type)
            throws Exception {
        PaymentRequest request = PaymentRequest.builder().ref("order-1").amount(BigDecimal.ONE)
                .currency(new CurrencyCode("USD")).paymentType(type).expireAt(Instant.EPOCH).successUrl(URL)
                .cancelUrl(URL).build();
        ReadablePaymentConfiguration secret = new ReadablePaymentConfiguration();

        PaymentInitiateResult result = processor.initiate("tx-1", secret, request);

        assertThat(processor.type()).isEqualTo(type);
        assertThat(result.status()).isEqualTo(PaymentInitiateStatus.PENDING);
        assertThat(result.shouldRedirect()).isFalse();
        assertThat(processor.parseWebhook(STORE, EMPTY_BODY, Map.of(), secret).paymentUseCase())
                .isEqualTo(PaymentUseCase.NONE);
        // No provider, no signing secret, no authentic delivery — every store has these types enabled, so accepting
        // one would reopen the public webhook as a way to write outbox rows for any store.
        assertThatThrownBy(() -> processor.authenticateWebhook(STORE, EMPTY_BODY, Map.of(), secret))
                .isInstanceOf(InvalidWebhookSignatureException.class);
    }

}
