package com.asrevo.cvhome.inventory.api.errors;

import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.inventory.errors.InventoryErrors;
import com.asrevo.cvhome.inventory.services.ExternalProductReservationService;
import com.asrevo.cvhome.inventory.services.IProductReservationService;

/**
 * The inventory API's error contract: which wire codes become which exception on a caller's side.
 *
 * <p>
 * Only the reservation API names failures so far, and it names the one that matters most to a caller — a refusal for
 * lack of stock, which is a <em>decision</em>, as against inventory being unreachable, which is not. Everything else
 * inventory can answer arrives as {@code UnmappedRemoteFailureException} carrying inventory's own code, which is the
 * signal that a code has earned an entry here.
 * </p>
 *
 * <p>
 * Both types are declared on {@link ExternalProductReservationService}, the interface the client proxy is generated
 * from — never on {@link IProductReservationService}, whose {@code throws} clauses name the server's exceptions for
 * the controller's sake. That placement is load-bearing: {@code S2sErrorHandler.declaredOrCarrier} narrows the
 * carrier only into types the invoked method declares.
 * </p>
 */
public final class InventoryApiErrors {

    /**
     * Codes are listed by their enum rather than as string literals, so renaming one in {@code InventoryErrors}
     * cannot silently orphan a mapping here.
     */
    public static final RemoteErrorCatalog INVENTORY = RemoteErrorCatalog.builder()
            // Inventory looked and said no. Definitive, so the caller may fail the order and name the sku.
            .map(InventoryErrors.RESERVATION_INSUFFICIENT_INVENTORY, ProductReservationRejectedException::from)
            // A malformed reservation is our bug, not a stock decision; it must not be mistaken for one, so it gets
            // the undecided type rather than the rejection.
            .map(InventoryErrors.RESERVATION_EMPTY, InventoryApiUnavailableException::from)
            // No server-side counterpart exists for a call that never arrived.
            .unreachable(InventoryApiUnavailableException::from)
            .build();

    private InventoryApiErrors() {
    }

}
