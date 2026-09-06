package com.asrevo.cvhome.gateway.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The store is not one the target acts in, or it is not operable.
 *
 * <p>
 * uaa checks a store-scoped target against its own metadata, but an org admin has none — which stores their
 * organization owns is tenancy's knowledge. So the gateway asks tenancy <em>as the impersonated principal</em>, and
 * a refusal there (another organization's store, a suspended one) is this.
 * </p>
 */
public class ImpersonationStoreNotTargetsException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ImpersonationStoreNotTargetsException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ImpersonationStoreNotTargetsException of(String storeId, int tenancyStatus) {
        return new ErrorBuilder<>(GatewayErrors.IMPERSONATION_STORE_NOT_TARGETS, ImpersonationStoreNotTargetsException::new)
                .detail("The account does not act in store %s, or the store is not operable.", storeId)
                .param("store", storeId)
                .param("tenancyStatus", tenancyStatus)
                .build();
    }

}
