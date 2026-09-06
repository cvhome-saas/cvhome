package com.asrevo.cvhome.checkout.api.errors;

import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;

/**
 * The order-signal API's error contract for its callers. The API decides nothing a caller must branch on — a signal
 * it cannot use is a 200 {@code IGNORED} — so the only mapping is "never answered", and a 404 for an unknown ref
 * stays an unmapped failure that the caller's outbox records as {@code FAILED} with the problem detail.
 */
public final class CheckoutApiErrors {

    public static final RemoteErrorCatalog CATALOG = RemoteErrorCatalog.builder()
            .unreachable(CheckoutApiUnavailableException::from)
            .build();

    private CheckoutApiErrors() {
    }

}
