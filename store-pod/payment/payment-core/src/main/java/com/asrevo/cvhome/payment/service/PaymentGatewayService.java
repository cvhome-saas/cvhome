package com.asrevo.cvhome.payment.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.errors.InvalidWebhookSignatureException;
import com.asrevo.cvhome.payment.errors.PaymentInitiateRejectedException;
import com.asrevo.cvhome.payment.errors.PaymentProviderUnavailableException;
import com.asrevo.cvhome.payment.errors.UnexpectedWebhookObjectException;
import com.asrevo.cvhome.payment.errors.UnreadableWebhookPayloadException;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;
import com.asrevo.cvhome.payment.models.ReadablePaymentConfiguration;
import com.asrevo.cvhome.payment.service.processor.PaymentProcessor;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentGatewayService {

    private final PaymentConfigurationService paymentConfigurationService;
    private final TransactionService transactionService;
    private final List<PaymentProcessor> paymentProcessors;
    private final WebhookUseCaseHandler webhookUseCaseHandler;


    /**
     * Starts a payment.
     *
     * <p>
     * A refusal by the provider is <em>propagated</em>, not folded into a failed result. Swallowing it returned HTTP
     * 200 with {@code failed()} and no reason at all, so the caller could not tell a declined card from a
     * misconfigured store — the exception is the only thing that carries the provider's own code across the wire.
     * Conditions this service decides for itself (no configuration, no processor) stay as failed results, because they
     * are answers rather than failures.
     * </p>
     *
     * @throws PaymentInitiateRejectedException    the provider refused the payment
     * @throws PaymentProviderUnavailableException the provider could not be reached, so nothing was decided
     */
    @Transactional
    public PaymentInitiateResult initiatePayment(StoreMerchantId store, PaymentRequest request)
            throws PaymentInitiateRejectedException, PaymentProviderUnavailableException {
        log.info("Initiating payment for store {} and order {}", store, request.ref());

        PaymentInitiateResult existingRequest =
                transactionService.findExistingInitialResultByRequestRef(store, request.ref()).orElse(null);

        if (Objects.nonNull(existingRequest)) {
            return existingRequest;
        }

        ReadablePaymentConfiguration config = getPaymentConfiguration(store, request.paymentType());

        if (config == null) {
            log.warn("No enabled payment configuration found for store {} and type {}", store, request.paymentType());
            return PaymentInitiateResult.failed();
        }

        // Initialize transaction
        String transactionInternalRef = transactionService.createInitialTransaction(store, request);


            PaymentProcessor processor = getProcessor(request.paymentType()).orElse(null);
            if (processor == null) {
                log.warn("un supported payment type {}", request.paymentType());
                return PaymentInitiateResult.failed();
            }

            PaymentInitiateResult initiateResult = processor.initiate(transactionInternalRef, config, request);

            log.info("Initiating transaction {} with gateway for store {} and type {} to {}", transactionInternalRef, store,
                    request.paymentType(), initiateResult.externalId());

            transactionService.completeInitiateTransaction(store, transactionInternalRef, request, initiateResult);

            return PaymentInitiateResult.builder()
                    .status(initiateResult.status())
                    .gatewayRef(transactionInternalRef)
                    .redirectUrl(initiateResult.redirectUrl())
                    .build();
    }


    public PaymentResponse status(StoreMerchantId store, String requestRef) {
        return transactionService.status(store, requestRef);
    }

    public void handleWebhook(StoreMerchantId store, PaymentType paymentType, String payload, Map<String, String> headers) {
        ReadablePaymentConfiguration config = getPaymentConfiguration(store, paymentType);
        if (config == null) {
            log.warn("No enabled {} configuration found for store {}", paymentType, store);
            return;
        }

        PaymentProcessor processor = getProcessor(paymentType).orElse(null);
        if (processor == null) {
            log.warn("No enabled {} processor found for store {}", paymentType, store);
            return;
        }

        try {
            WebhookResult result = processor.parseWebhook(store, payload, headers, config);
            webhookUseCaseHandler.handleUseCase(store, result);
        } catch (InvalidWebhookSignatureException | UnreadableWebhookPayloadException
                 | UnexpectedWebhookObjectException e) {
            // Every failure parseWebhook declares is permanent for this payload: a redelivery of the same body fails
            // identically. So acknowledge and stop — propagating would make the outbox redeliver the bad event forever.
            // The multi-catch is deliberate: if a processor gains a retryable failure mode, this stops compiling and
            // the retry decision gets made explicitly instead of defaulting to "discard".
            log.warn("Discarding unprocessable {} webhook for store {} [{}]: {} {}", paymentType, store,
                    e.errorCode().code(), e.getMessage(), e.params());
        }
    }


    private ReadablePaymentConfiguration getPaymentConfiguration(StoreMerchantId store, PaymentType paymentType) {
        return paymentConfigurationService.getConfig(store, paymentType)
                .filter(ReadablePaymentConfiguration::isEnabled)
                .orElse(null);
    }

    private Optional<PaymentProcessor> getProcessor(PaymentType type) {
        return this.paymentProcessors.stream().filter(p -> p.type() == type).findFirst();
    }
}
