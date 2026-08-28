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
 * {@link IPaymentGatewayService} is the other half: implemented by the payment service's own controller, so its
 * {@code throws} clauses have to be the server's truth. This interface restates the same operations in the
 * <em>caller's</em> vocabulary, where a failure means "payment-service refused us" rather than "Stripe refused
 * payment-service". Splitting them is what stops one signature from having to be honest about two different things at
 * once.
 * </p>
 *
 * <p>
 * Nothing implements this interface: {@code RestClientBuilder.buildClient(...)} generates the proxy from it, and
 * because {@code S2sErrorHandler.declaredOrCarrier} treats the invoked method's declared exception types as the
 * authority, naming the caller-side types here is exactly what makes them arrive narrowed instead of wrapped in
 * {@code UncheckedBaseException}. That is the whole job the deleted {@code RestPaymentGatewayClient} wrapper used to
 * do by hand.
 * </p>
 *
 * <p>
 * The paths below are not checked against {@code ExternalPaymentGatewayApi}'s {@code @PostMapping}/{@code @GetMapping}
 * by any compiler. Keep them in step by eye when adding a method.
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
