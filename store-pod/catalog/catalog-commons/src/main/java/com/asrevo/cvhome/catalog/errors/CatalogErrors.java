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
     * A submitted payload references a product that does not resolve in this store.
     *
     * <p>
     * Deliberately not {@link #PRODUCT_NOT_FOUND}: the endpoint's target exists, and it is a <em>field</em> inside the
     * body that names nothing. That is a 400 about the payload, not a 404 about the resource — and it is the only
     * shape a mapper can report, since {@code Mapper} declares {@code ConversionException}.
     * </p>
     */
    PRODUCT_REFERENCE_UNRESOLVABLE("CATALOG.PRODUCT.REFERENCE_UNRESOLVABLE", ErrorCategory.CONVERSION),

    /**
     * A product could not be converted between its persisted and its API form.
     */
    PRODUCT_NOT_CONVERTIBLE("CATALOG.PRODUCT.NOT_CONVERTIBLE", ErrorCategory.CONVERSION),

    /**
     * Persisting a product, or a change to one, failed.
     */
    PRODUCT_NOT_PERSISTED("CATALOG.PRODUCT.NOT_PERSISTED", ErrorCategory.STORAGE),

    /**
     * The caller is authenticated but the product belongs to another store.
     *
     * <p>
     * 403, not the 401 the legacy {@code UnauthorizedException} produced. The caller <em>is</em> authenticated; what
     * they lack is a claim on this store, and asking them to log in again would send them round a loop that cannot
     * terminate.
     * </p>
     */
    PRODUCT_FOREIGN_STORE("CATALOG.PRODUCT.FOREIGN_STORE", ErrorCategory.FORBIDDEN),

    /**
     * No product option with that id exists in this store.
     */
    PRODUCT_OPTION_NOT_FOUND("CATALOG.PRODUCT_OPTION.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * A submitted payload references a product option that does not resolve in this store.
     */
    PRODUCT_OPTION_REFERENCE_UNRESOLVABLE("CATALOG.PRODUCT_OPTION.REFERENCE_UNRESOLVABLE", ErrorCategory.CONVERSION),

    /**
     * A product option could not be converted.
     */
    PRODUCT_OPTION_NOT_CONVERTIBLE("CATALOG.PRODUCT_OPTION.NOT_CONVERTIBLE", ErrorCategory.CONVERSION),

    /**
     * No product option value with that id exists in this store.
     */
    PRODUCT_OPTION_VALUE_NOT_FOUND("CATALOG.PRODUCT_OPTION_VALUE.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * A submitted payload references a product option value that does not resolve in this store.
     */
    PRODUCT_OPTION_VALUE_REFERENCE_UNRESOLVABLE("CATALOG.PRODUCT_OPTION_VALUE.REFERENCE_UNRESOLVABLE",
            ErrorCategory.CONVERSION),

    /**
     * No product option set with that id exists in this store.
     */
    PRODUCT_OPTION_SET_NOT_FOUND("CATALOG.PRODUCT_OPTION_SET.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * No product attribute with that id exists on the product in this store.
     */
    PRODUCT_ATTRIBUTE_NOT_FOUND("CATALOG.PRODUCT_ATTRIBUTE.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * A product attribute could not be converted.
     */
    PRODUCT_ATTRIBUTE_NOT_CONVERTIBLE("CATALOG.PRODUCT_ATTRIBUTE.NOT_CONVERTIBLE", ErrorCategory.CONVERSION),

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

    /**
     * Writing or removing a product image failed.
     */
    PRODUCT_IMAGE_NOT_PERSISTED("CATALOG.PRODUCT_IMAGE.NOT_PERSISTED", ErrorCategory.STORAGE),

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
     * A category description was submitted with no language on it.
     */
    CATEGORY_DESCRIPTION_NO_LANGUAGE("CATALOG.CATEGORY_DESCRIPTION.NO_LANGUAGE", ErrorCategory.CONVERSION),

    /**
     * A category could not be converted.
     */
    CATEGORY_NOT_CONVERTIBLE("CATALOG.CATEGORY.NOT_CONVERTIBLE", ErrorCategory.CONVERSION),

    /**
     * No manufacturer with that id exists in this store.
     */
    MANUFACTURER_NOT_FOUND("CATALOG.MANUFACTURER.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * A submitted payload references a manufacturer that does not resolve in this store.
     */
    MANUFACTURER_REFERENCE_UNRESOLVABLE("CATALOG.MANUFACTURER.REFERENCE_UNRESOLVABLE", ErrorCategory.CONVERSION),

    /**
     * A manufacturer could not be converted.
     */
    MANUFACTURER_NOT_CONVERTIBLE("CATALOG.MANUFACTURER.NOT_CONVERTIBLE", ErrorCategory.CONVERSION),

    /**
     * A product type with that code already exists in this store.
     */
    PRODUCT_TYPE_DUPLICATE("CATALOG.PRODUCT_TYPE.DUPLICATE", ErrorCategory.CONFLICT),

    /**
     * A product option set with that code already exists in this store.
     */
    PRODUCT_OPTION_SET_DUPLICATE("CATALOG.PRODUCT_OPTION_SET.DUPLICATE", ErrorCategory.CONFLICT),

    /**
     * A category was addressed by a friendly URL that matches nothing in this store.
     */
    CATEGORY_FRIENDLY_URL_NOT_FOUND("CATALOG.CATEGORY.FRIENDLY_URL_NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * A category's id and code identify two different categories, so the reference is self-contradictory.
     */
    CATEGORY_IDENTIFIERS_INCONSISTENT("CATALOG.CATEGORY.IDENTIFIERS_INCONSISTENT", ErrorCategory.UNPROCESSABLE);

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
