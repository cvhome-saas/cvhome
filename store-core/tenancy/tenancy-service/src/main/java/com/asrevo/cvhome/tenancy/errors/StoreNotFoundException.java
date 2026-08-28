package com.asrevo.cvhome.tenancy.errors;

import java.io.Serial;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No store with the requested id is visible to the caller.
 *
 * <p>
 * <strong>A store belonging to another organization raises this same 404, not a 403.</strong> That is deliberate and
 * should not be "corrected" into a forbidden: a 403 confirms the id exists, which lets anyone enumerate the platform's
 * stores by probing ids and reading the status code. The caller cannot see the store, so as far as it is concerned the
 * store does not exist.
 * </p>
 *
 * <p>
 * It also replaces a bare {@code orElseThrow()}, which raised {@code NoSuchElementException} and surfaced as a 500 —
 * an ordinary missing store read as a server fault.
 * </p>
 */
public class StoreNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected StoreNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * Takes the id rather than an {@code Object} so the message carries the bare id. Handed the value object, the
     * builder renders its record {@code toString()} and the detail reads
     * {@code StoreMerchantId[id=65f0…]}, which is noise to whoever is reading the error.
     */
    public static StoreNotFoundException of(StoreMerchantId storeId) {
        String id = storeId == null || storeId.getId() == null ? "unknown" : storeId.getId().toString();
        return new ErrorBuilder<>(TenancyErrors.STORE_NOT_FOUND, StoreNotFoundException::new)
                .detail("No store is visible with id %s.", id)
                .param("storeId", id)
                .build();
    }

}
