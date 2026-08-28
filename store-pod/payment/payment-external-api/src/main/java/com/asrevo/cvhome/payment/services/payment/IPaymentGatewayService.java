package com.asrevo.cvhome.payment.services.payment;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.errors.PaymentInitiateRejectedException;
import com.asrevo.cvhome.payment.errors.PaymentProviderUnavailableException;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;

/**
 * The payment service's HTTP contract, in the payment service's own vocabulary.
 *
 * <p>
 * This interface has two implementations, and that is why its {@code throws} clauses name <em>server-side</em>
 * exceptions: {@code ExternalPaymentGatewayApi} implements it as the controller, and {@code WebClientsUtils} generates
 * a client proxy from it. Listing a caller-side type here would put an exception the controller can never throw into
 * the controller's own signature, and — because Java forbids widening — would block the server from ever declaring a
 * new failure of its own.
 * </p>
 *
 * <p>
 * Callers should depend on {@link ExternalPaymentGatewayService} instead. It wraps this
 * proxy and restates the failures in the caller's vocabulary, which is where "payment-service refused us" is expressed
 * rather than "Stripe refused payment-service". {@code PaymentApiErrors.CATALOG} decodes the wire into those
 * caller-side types, which is why they are deliberately absent from the {@code throws} clauses below.
 * </p>
 */
public interface IPaymentGatewayService {

    /**
     * Starts a payment for an order.
     *
     * <p>
     * A store with no enabled configuration, or a payment type with no processor, comes back as a failed
     * {@code PaymentInitiateResult} rather than an exception: those are decisions this service reached, not failures of
     * the call.
     * </p>
     *
     * @throws PaymentInitiateRejectedException   the payment provider refused the payment
     * @throws PaymentProviderUnavailableException the payment provider could not be reached
     */
    PaymentInitiateResult initiatePayment(StoreMerchantId store, @RequestBody PaymentRequest paymentRequest)
            throws PaymentInitiateRejectedException, PaymentProviderUnavailableException;

    /**
     * Current state of a previously initiated payment.
     */
    PaymentResponse status(StoreMerchantId store, @PathVariable("ref") String ref);

}