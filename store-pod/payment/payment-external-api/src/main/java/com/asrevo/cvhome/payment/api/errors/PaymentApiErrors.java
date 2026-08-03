package com.asrevo.cvhome.payment.api.errors;

import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.payment.errors.PaymentErrors;
import com.asrevo.cvhome.payment.services.payment.ExternalPaymentGatewayService;
import com.asrevo.cvhome.payment.services.payment.IPaymentGatewayService;

/**
 * The payment API's error contract: which wire codes become which exception on a caller's side.
 *
 * <p>
 * The mappings rebuild <em>caller-side</em> types. The payment service's own exceptions describe a conversation with
 * Stripe — {@code PaymentInitiateRejectedException} is an {@link com.asrevo.cvhome.errors.ExternalProviderException}
 * whose {@code provider()} is {@code stripe}. Handing one of those to a caller of this SDK would tell it that
 * <em>it</em> called Stripe, which it did not: it called payment. So a rejection becomes
 * {@link PaymentGatewayRejectedException} and an undecided failure becomes {@link PaymentApiUnavailableException},
 * both naming payment as the remote and carrying the provider's details through in their params.
 * </p>
 *
 * <p>
 * Both types are declared on {@link ExternalPaymentGatewayService}, the interface the client proxy is generated from —
 * never on {@link IPaymentGatewayService}, whose {@code throws} clauses name the server's exceptions for the
 * controller's sake. That placement is load-bearing: {@code S2sErrorHandler.declaredOrCarrier} narrows the carrier
 * only into types the invoked method declares, so it is what delivers a rebuilt exception to the caller as itself
 * rather than wrapped.
 * </p>
 *
 * <p>
 * Passed explicitly when the client is built, in the caller's {@code ClientsConfig}. It is not discovered from the
 * classpath: a service that calls payment states so, rather than inheriting the contract by virtue of a jar being
 * present.
 * </p>
 */
public final class PaymentApiErrors {

    /**
     * Codes are listed by their enum rather than as string literals, so renaming one in {@code PaymentErrors} cannot
     * silently orphan a mapping here.
     */
    public static final RemoteErrorCatalog CATALOG = RemoteErrorCatalog.builder()
            // The provider said no. Definitive, so the caller may unwind the order.
            .map(PaymentErrors.INITIATE_REJECTED, PaymentGatewayRejectedException::from)
            // The provider never decided. Indeterminate, so the caller must hold and reconcile — the same instruction
            // as an unreachable payment service, hence the same type.
            .map(PaymentErrors.INITIATE_FAILED, PaymentApiUnavailableException::from)
            // No server-side counterpart exists for a call that never arrived, so this one is a client type by
            // necessity.
            .unreachable(PaymentApiUnavailableException::from)
            .build();

    private PaymentApiErrors() {
    }

}
