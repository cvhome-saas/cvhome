package com.asrevo.cvhome.payment.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.errors.InvalidWebhookSignatureException;
import com.asrevo.cvhome.payment.errors.PaymentInitiateRejectedException;
import com.asrevo.cvhome.payment.errors.PaymentProviderUnavailableException;
import com.asrevo.cvhome.payment.errors.UnexpectedWebhookObjectException;
import com.asrevo.cvhome.payment.errors.UnreadableWebhookPayloadException;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateStatus;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.model.payment.PaymentUseCase;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;
import com.asrevo.cvhome.payment.models.ReadablePaymentConfiguration;
import com.asrevo.cvhome.payment.service.processor.PaymentProcessor;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The orchestration between configuration, transaction bookkeeping and the provider processors. What this service
 * decides for itself (no configuration, no processor) is an answer; what the provider decides propagates.
 */
@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-1");

    private static final String REQUEST_REF = "order-1";

    private static final String INTERNAL_REF = "tx-1";

    private static final String EXTERNAL_ID = "cs_1";

    private static final String REDIRECT = "https://stripe.example/cs_1";

    private static final String PAYLOAD = "{}";

    private static final String EVENT_ID = "evt";

    private static final String EVENT_TYPE = "t";

    private static final String STRIPE = "stripe";

    private static final Map<String, String> HEADERS = Map.of();

    @Mock
    private PaymentConfigurationService configurations;

    @Mock
    private TransactionService transactions;

    @Mock
    private PaymentProcessor stripe;

    @Mock
    private WebhookUseCaseHandler useCases;

    private PaymentGatewayService service;

    private static PaymentRequest request(PaymentType type) {
        return PaymentRequest.builder().ref(REQUEST_REF).amount(BigDecimal.TEN).currency(new CurrencyCode("USD"))
                .paymentType(type).expireAt(Instant.EPOCH).successUrl(REDIRECT).cancelUrl(REDIRECT).build();
    }

    private static ReadablePaymentConfiguration config(boolean enabled) {
        return ReadablePaymentConfiguration.builder().storeMerchantId(STORE).paymentType(PaymentType.STRIPE)
                .enabled(enabled).build();
    }

    @BeforeEach
    void setUp() {
        service = new PaymentGatewayService(configurations, transactions, List.of(stripe), useCases);
    }

    @Test
    void aRequestAlreadyInitiatedAnswersTheExistingResultWithoutTouchingTheProvider() throws Exception {
        PaymentInitiateResult existing = PaymentInitiateResult.builder().status(PaymentInitiateStatus.PENDING)
                .gatewayRef(INTERNAL_REF).build();
        when(transactions.findExistingInitialResultByRequestRef(STORE, REQUEST_REF)).thenReturn(Optional.of(existing));

        assertThat(service.initiatePayment(STORE, request(PaymentType.STRIPE))).isSameAs(existing);

        verifyNoInteractions(configurations, stripe);
        verify(transactions, never()).createInitialTransaction(any(), any());
    }

    @Test
    void missingOrDisabledConfigurationIsAFailedAnswer() throws Exception {
        when(transactions.findExistingInitialResultByRequestRef(STORE, REQUEST_REF)).thenReturn(Optional.empty());
        when(configurations.getConfig(STORE, PaymentType.STRIPE)).thenReturn(Optional.empty())
                .thenReturn(Optional.of(config(false)));

        assertThat(service.initiatePayment(STORE, request(PaymentType.STRIPE)).status())
                .isEqualTo(PaymentInitiateStatus.FAILED);
        assertThat(service.initiatePayment(STORE, request(PaymentType.STRIPE)).status())
                .isEqualTo(PaymentInitiateStatus.FAILED);
        verify(transactions, never()).createInitialTransaction(any(), any());
    }

    @Test
    void unsupportedTypeIsAFailedAnswerAfterTheTransactionIsOpened() throws Exception {
        when(transactions.findExistingInitialResultByRequestRef(STORE, REQUEST_REF)).thenReturn(Optional.empty());
        when(configurations.getConfig(STORE, PaymentType.PAYPAL)).thenReturn(Optional.of(config(true)));
        when(transactions.createInitialTransaction(eq(STORE), any())).thenReturn(INTERNAL_REF);
        when(stripe.type()).thenReturn(PaymentType.STRIPE);

        PaymentInitiateResult result = service.initiatePayment(STORE, request(PaymentType.PAYPAL));

        assertThat(result.status()).isEqualTo(PaymentInitiateStatus.FAILED);
        verify(transactions, never()).completeInitiateTransaction(any(), any(), any(), any());
    }

    @Test
    void aSupportedTypeRunsTheProcessorAndRecordsItsAnswerUnderTheInternalRef() throws Exception {
        ReadablePaymentConfiguration config = config(true);
        PaymentRequest request = request(PaymentType.STRIPE);
        PaymentInitiateResult fromProvider = PaymentInitiateResult.builder().status(PaymentInitiateStatus.PENDING)
                .externalId(EXTERNAL_ID).redirectUrl(REDIRECT).build();
        when(transactions.findExistingInitialResultByRequestRef(STORE, REQUEST_REF)).thenReturn(Optional.empty());
        when(configurations.getConfig(STORE, PaymentType.STRIPE)).thenReturn(Optional.of(config));
        when(transactions.createInitialTransaction(STORE, request)).thenReturn(INTERNAL_REF);
        when(stripe.type()).thenReturn(PaymentType.STRIPE);
        when(stripe.initiate(INTERNAL_REF, config, request)).thenReturn(fromProvider);

        PaymentInitiateResult result = service.initiatePayment(STORE, request);

        verify(transactions).completeInitiateTransaction(STORE, INTERNAL_REF, request, fromProvider);
        assertThat(result.status()).isEqualTo(PaymentInitiateStatus.PENDING);
        assertThat(result.gatewayRef()).isEqualTo(INTERNAL_REF);
        assertThat(result.redirectUrl()).isEqualTo(REDIRECT);
        assertThat(result.externalId()).isNull();
    }

    @Test
    void providerRefusalsAndOutagesPropagateAsThemselves() throws Exception {
        PaymentRequest request = request(PaymentType.STRIPE);
        when(transactions.findExistingInitialResultByRequestRef(STORE, REQUEST_REF)).thenReturn(Optional.empty());
        when(configurations.getConfig(STORE, PaymentType.STRIPE)).thenReturn(Optional.of(config(true)));
        when(transactions.createInitialTransaction(eq(STORE), any())).thenReturn(INTERNAL_REF);
        when(stripe.type()).thenReturn(PaymentType.STRIPE);
        when(stripe.initiate(eq(INTERNAL_REF), any(), any()))
                .thenThrow(PaymentInitiateRejectedException.of(STRIPE, REQUEST_REF, INTERNAL_REF, "card_declined", 402,
                        null))
                .thenThrow(PaymentProviderUnavailableException.of(STRIPE, REQUEST_REF, INTERNAL_REF, null, 0, null));

        assertThatThrownBy(() -> service.initiatePayment(STORE, request))
                .isInstanceOf(PaymentInitiateRejectedException.class);
        assertThatThrownBy(() -> service.initiatePayment(STORE, request))
                .isInstanceOf(PaymentProviderUnavailableException.class);
        verify(transactions, never()).completeInitiateTransaction(any(), any(), any(), any());
    }

    @Test
    void statusIsDelegated() {
        PaymentResponse response = PaymentResponse.pending();
        when(transactions.status(STORE, REQUEST_REF)).thenReturn(response);

        assertThat(service.status(STORE, REQUEST_REF)).isSameAs(response);
    }

    @Test
    void webhookWithoutConfigurationOrProcessorIsIgnored() throws Exception {
        when(configurations.getConfig(STORE, PaymentType.STRIPE)).thenReturn(Optional.empty());
        when(configurations.getConfig(STORE, PaymentType.PAYPAL)).thenReturn(Optional.of(config(true)));
        when(stripe.type()).thenReturn(PaymentType.STRIPE);

        service.handleWebhook(STORE, PaymentType.STRIPE, PAYLOAD, HEADERS);
        service.handleWebhook(STORE, PaymentType.PAYPAL, PAYLOAD, HEADERS);

        verify(stripe, never()).parseWebhook(any(), anyString(), any(), any());
        verifyNoInteractions(useCases);
    }

    @Test
    void anAuthenticWebhookIsParsedAndHandled() throws Exception {
        ReadablePaymentConfiguration config = config(true);
        WebhookResult result = WebhookResult.builder().internalReference(INTERNAL_REF)
                .paymentUseCase(PaymentUseCase.PAYMENT_SUCCEEDED).build();
        when(configurations.getConfig(STORE, PaymentType.STRIPE)).thenReturn(Optional.of(config));
        when(stripe.type()).thenReturn(PaymentType.STRIPE);
        when(stripe.parseWebhook(STORE, PAYLOAD, HEADERS, config)).thenReturn(result);

        service.handleWebhook(STORE, PaymentType.STRIPE, PAYLOAD, HEADERS);

        verify(useCases).handleUseCase(STORE, result);
    }

    @Test
    void everyPermanentWebhookFailureIsAcknowledgedRatherThanRetried() throws Exception {
        when(configurations.getConfig(STORE, PaymentType.STRIPE)).thenReturn(Optional.of(config(true)));
        when(stripe.type()).thenReturn(PaymentType.STRIPE);
        when(stripe.parseWebhook(eq(STORE), anyString(), any(), any()))
                .thenThrow(InvalidWebhookSignatureException.verificationFailed(STRIPE, false, null))
                .thenThrow(UnreadableWebhookPayloadException.of(STRIPE, EVENT_ID, EVENT_TYPE, null))
                .thenThrow(UnexpectedWebhookObjectException.of(STRIPE, EVENT_ID, EVENT_TYPE, String.class, null));

        service.handleWebhook(STORE, PaymentType.STRIPE, PAYLOAD, HEADERS);
        service.handleWebhook(STORE, PaymentType.STRIPE, PAYLOAD, HEADERS);
        service.handleWebhook(STORE, PaymentType.STRIPE, PAYLOAD, HEADERS);

        verifyNoInteractions(useCases);
    }

}
