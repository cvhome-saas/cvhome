package com.asrevo.cvhome.catalog.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes owned by the catalog context.
 *
 * <p>
 * The two reservation codes came first, in Step 6, because checkout could not tell a refusal from an outage without
 * them. The rest arrived with the catalog migration.
 * </p>
 *
 * <p>
 * Grouped by resource rather than by category, because that is how they are looked up: everything a caller can be told
 * about a product option sits together, and the {@link ErrorCategory} on each says what the status will be.
 * </p>
 */
public enum CatalogErrors implements ErrorCode {


    /**
     * No product with that id or sku exists in this store.
     */
    PRODUCT_NOT_FOUND("CATALOG.PRODUCT.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * A product could not be converted between its persisted and its API form.
     */
    PRODUCT_NOT_CONVERTIBLE("CATALOG.PRODUCT.NOT_CONVERTIBLE", ErrorCategory.CONVERSION),

    /**
     * No product type with that id or code exists in this store.
     */
    PRODUCT_TYPE_NOT_FOUND("CATALOG.PRODUCT_TYPE.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * A submitted payload references a product type that does not resolve in this store.
     */
    PRODUCT_TYPE_REFERENCE_UNRESOLVABLE("CATALOG.PRODUCT_TYPE.REFERENCE_UNRESOLVABLE", ErrorCategory.CONVERSION),

    /**
     * No product group with that name exists in this store.
     */
    PRODUCT_GROUP_NOT_FOUND("CATALOG.PRODUCT_GROUP.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * No product image with that id exists for the product.
     */
    PRODUCT_IMAGE_NOT_FOUND("CATALOG.PRODUCT_IMAGE.NOT_FOUND", ErrorCategory.NOT_FOUND),

    PRODUCT_IMAGE_ASSET_UNKNOWN("CATALOG.PRODUCT_IMAGE.ASSET_UNKNOWN", ErrorCategory.VALIDATION),

    /**
     * The category is already attached to that product.
     */
    CATEGORY_ALREADY_ATTACHED("CATALOG.CATEGORY.ALREADY_ATTACHED", ErrorCategory.CONFLICT),

    /**
     * No category with that id or code exists in this store.
     */
    CATEGORY_NOT_FOUND("CATALOG.CATEGORY.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * A category reference carries neither an id nor a code, or names a category that does not resolve in this store.
     */
    CATEGORY_REFERENCE_UNRESOLVABLE("CATALOG.CATEGORY.REFERENCE_UNRESOLVABLE", ErrorCategory.CONVERSION),

    /**
     * No manufacturer with that id exists in this store.
     */
    MANUFACTURER_NOT_FOUND("CATALOG.MANUFACTURER.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * A submitted payload references a manufacturer that does not resolve in this store.
     */
    MANUFACTURER_REFERENCE_UNRESOLVABLE("CATALOG.MANUFACTURER.REFERENCE_UNRESOLVABLE", ErrorCategory.CONVERSION),

    /**
     * A product type with that code already exists in this store.
     */
    PRODUCT_TYPE_DUPLICATE("CATALOG.PRODUCT_TYPE.DUPLICATE", ErrorCategory.CONFLICT),

    /**
     * A category was addressed by a friendly URL that matches nothing in this store.
     */
    CATEGORY_FRIENDLY_URL_NOT_FOUND("CATALOG.CATEGORY.FRIENDLY_URL_NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * No product option with that id or code exists in this store.
     */
    PRODUCT_OPTION_NOT_FOUND("CATALOG.PRODUCT_OPTION.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * A product option with that code already exists in this store.
     */
    PRODUCT_OPTION_DUPLICATE("CATALOG.PRODUCT_OPTION.DUPLICATE", ErrorCategory.CONFLICT),

    /**
     * The option (or one of its values) is still referenced by a product's assignments or variants.
     */
    PRODUCT_OPTION_IN_USE("CATALOG.PRODUCT_OPTION.IN_USE", ErrorCategory.CONFLICT);

    private final String code;

    private final ErrorCategory category;

    CatalogErrors(String code, ErrorCategory category) {
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
