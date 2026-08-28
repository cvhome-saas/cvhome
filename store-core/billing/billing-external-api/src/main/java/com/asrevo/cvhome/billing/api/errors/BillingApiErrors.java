package com.asrevo.cvhome.billing.api.errors;

import com.asrevo.cvhome.billing.commons.errors.BillingErrors;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;

/**
 * The billing API's error contract: which wire codes become which exception on a caller's side.
 *
 * <p>
 * Passed explicitly where the client is built, in the caller's {@code ClientsConfig}, rather than discovered from the
 * classpath — a service that calls billing says so, instead of inheriting the contract because a jar happens to be
 * present.
 * </p>
 *
 * <p>
 * Codes are named by their enum rather than as string literals, so renaming one in {@link BillingErrors} cannot
 * silently orphan a mapping here.
 * </p>
 */
public final class BillingApiErrors {

    public static final RemoteErrorCatalog CATALOG = RemoteErrorCatalog.builder()
            // Billing said no. Definitive: the caller stops rather than retries.
            .map(BillingErrors.STORE_QUOTA_EXCEEDED, StoreQuotaRefusedException::from)
            // Billing's own provider never decided. Undecided from here too, so it reads as unavailable.
            .map(BillingErrors.PROVIDER_UNAVAILABLE, BillingApiUnavailableException::from)
            // No counterpart inside billing exists for a call that never arrived.
            .unreachable(BillingApiUnavailableException::from)
            .build();

    private BillingApiErrors() {
    }

}
