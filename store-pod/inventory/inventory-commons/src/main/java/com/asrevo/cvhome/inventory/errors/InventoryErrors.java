package com.asrevo.cvhome.inventory.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes owned by the inventory context.
 *
 * <p>
 * Carried over from {@code CatalogErrors} when availability, pricing and reservations were split out of catalog; the
 * codes were re-rooted under {@code INVENTORY.} because the service that answers them changed. Grouped by resource,
 * with the {@link ErrorCategory} on each deciding the HTTP status.
 * </p>
 */
public enum InventoryErrors implements ErrorCode {

    /**
     * The stock on hand does not cover what was asked for, or the sku has no availability record at all.
     *
     * <p>
     * A decision about the data, not a malformed request, hence 422.
     * </p>
     */
    RESERVATION_INSUFFICIENT_INVENTORY("INVENTORY.RESERVATION.INSUFFICIENT_INVENTORY", ErrorCategory.UNPROCESSABLE),

    /**
     * A reservation was requested with no lines on it — the caller's bug, so 400.
     */
    RESERVATION_EMPTY("INVENTORY.RESERVATION.EMPTY", ErrorCategory.VALIDATION),

    /**
     * No inventory (product availability) with that id exists in this store.
     */
    INVENTORY_NOT_FOUND("INVENTORY.INVENTORY.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * A submitted payload references an inventory record that does not resolve in this store.
     */
    INVENTORY_REFERENCE_UNRESOLVABLE("INVENTORY.INVENTORY.REFERENCE_UNRESOLVABLE", ErrorCategory.CONVERSION),

    /**
     * An inventory record could not be converted.
     */
    INVENTORY_NOT_CONVERTIBLE("INVENTORY.INVENTORY.NOT_CONVERTIBLE", ErrorCategory.CONVERSION),

    /**
     * A submitted payload names a sku that has no inventory record in this store.
     */
    SKU_REFERENCE_UNRESOLVABLE("INVENTORY.SKU.REFERENCE_UNRESOLVABLE", ErrorCategory.CONVERSION),

    /**
     * No price with that id exists for the sku in this store.
     */
    PRODUCT_PRICE_NOT_FOUND("INVENTORY.PRODUCT_PRICE.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * A price could not be converted, or its final amount could not be calculated.
     */
    PRODUCT_PRICE_NOT_CONVERTIBLE("INVENTORY.PRODUCT_PRICE.NOT_CONVERTIBLE", ErrorCategory.CONVERSION),

    /**
     * The sku has no inventory a price can be calculated from.
     */
    PRICING_NO_APPLICABLE_INVENTORY("INVENTORY.PRICING.NO_APPLICABLE_INVENTORY", ErrorCategory.UNPROCESSABLE),

    /**
     * Persisting an inventory record or a price failed.
     */
    INVENTORY_WRITE_FAILED("INVENTORY.WRITE.FAILED", ErrorCategory.STORAGE),

    /**
     * Reading inventory data back out of the database failed.
     */
    INVENTORY_READ_FAILED("INVENTORY.READ.FAILED", ErrorCategory.STORAGE);

    private final String code;

    private final ErrorCategory category;

    InventoryErrors(String code, ErrorCategory category) {
        this.code = code;
        this.category = category;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public ErrorCategory category() {
        return category;
    }

}
