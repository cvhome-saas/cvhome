package com.asrevo.cvhome.payment.services.payment;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.api.errors.PaymentApiUnavailableException;
import com.asrevo.cvhome.payment.api.errors.PaymentGatewayRejectedException;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;

/**
 * What a caller of the payment service should depend on.
 *
 * <p>
 * {@code ExternalPaymentGatewayService} is the wire contract, and it is implemented by the payment service's own
 * controller, so its {@code throws} clauses have to be the server's truth. This interface is the other half: the same
 * operations restated in the <em>caller's</em> vocabulary, where a failure means "payment-service refused us" rather
 * than "Stripe refused payment-service". Splitting them is what stops one signature from having to be honest about two
 * different things at once.
 * </p>
 *
 * <p>
 * It is also where a condition that only exists on this side lives: {@link PaymentApiUnavailableException} has no
 * server-side counterpart, because a service that cannot be reached never threw anything.
 * </p>
 */
@HttpExchange("/api/v1/private")
public interface ExternalPaymentGatewayService {

    /**
     * Starts a payment for an order.
     *
     * @throws PaymentGatewayRejectedException the payment was refused; it will not happen, and whatever was staged for
     *                                         this order should be unwound
     * @throws PaymentApiUnavailableException  the payment service could not be reached, so <em>nothing was decided</em>
     *                                         — the payment may or may not have started
     */
    @PostExchange("/payments/initiate")
    PaymentInitiateResult initiatePayment(StoreMerchantId store, @RequestBody PaymentRequest paymentRequest)
            throws PaymentGatewayRejectedException, PaymentApiUnavailableException;

    /**
     * Current state of a previously initiated payment.
     *
     * @throws PaymentApiUnavailableException the payment service could not be reached
     */
    @GetExchange("/payments/{ref}/status")
    PaymentResponse status(StoreMerchantId store, @PathVariable("ref") String ref) throws PaymentApiUnavailableException;

}
