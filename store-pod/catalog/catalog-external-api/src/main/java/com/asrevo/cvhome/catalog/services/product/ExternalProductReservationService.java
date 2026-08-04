package com.asrevo.cvhome.catalog.services.product;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.asrevo.cvhome.catalog.api.errors.CatalogApiUnavailableException;
import com.asrevo.cvhome.catalog.api.errors.ProductReservationRejectedException;
import com.asrevo.cvhome.catalog.model.product.ProductReservationCommitResult;
import com.asrevo.cvhome.catalog.model.product.ProductReservationReleaseResult;
import com.asrevo.cvhome.catalog.model.product.ProductReservationReserveResult;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;

/**
 * What a caller of the catalog reservation API should depend on.
 *
 * <p>
 * {@link IProductReservationService} is the other half: implemented by catalog's own controller, so its {@code throws}
 * clauses have to be the server's truth. This interface restates the same operations in the <em>caller's</em>
 * vocabulary, where a failure means "catalog refused us" rather than "the stock was short".
 * </p>
 *
 * <p>
 * Nothing implements this interface: {@code RestClientBuilder.buildClient(...)} generates the proxy from it, and
 * because {@code S2sErrorHandler.declaredOrCarrier} treats the invoked method's declared exception types as the
 * authority, naming the caller-side types here is exactly what makes them arrive narrowed instead of wrapped in
 * {@code UncheckedBaseException}.
 * </p>
 *
 * <p>
 * The paths below are not checked against {@code ExternalProductReservationApi}'s {@code @PostMapping} by any
 * compiler. Keep them in step by eye when adding a method.
 * </p>
 */
@HttpExchange("/api/v1/private")
public interface ExternalProductReservationService {

    /**
     * Takes stock for an order.
     *
     * @throws ProductReservationRejectedException the stock is not there; <em>nothing was reserved</em>, so the order
     *                                             can be failed and the shopper told which sku blocked it
     * @throws CatalogApiUnavailableException      catalog could not be reached, so <em>nothing was decided</em> — the
     *                                             stock may or may not be held, and the order must stay recoverable
     */
    @PostExchange("/reserve/{ref}")
    ProductReservationReserveResult reserve(StoreMerchantId store, @PathVariable("ref") String ref,
                                            @RequestBody ProductReservationList productReservation)
            throws ProductReservationRejectedException, CatalogApiUnavailableException;

    /**
     * Turns a temporary reservation into a permanent one.
     *
     * @throws CatalogApiUnavailableException catalog could not be reached; the reservation is still held, so the order
     *                                        should be left for retry rather than marked failed
     */
    @PostExchange("/commit/{ref}")
    ProductReservationCommitResult commit(StoreMerchantId store, @PathVariable("ref") String ref)
            throws CatalogApiUnavailableException;

    /**
     * Returns reserved stock to availability.
     *
     * @throws CatalogApiUnavailableException catalog could not be reached; the stock may still be held and will be
     *                                        freed by catalog's expiry job if this is never retried
     */
    @PostExchange("/release/{ref}")
    ProductReservationReleaseResult release(StoreMerchantId store, @PathVariable("ref") String ref)
            throws CatalogApiUnavailableException;

}
