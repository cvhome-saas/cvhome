package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * An image was attached by an id that is not in this store's media library.
 *
 * <p>
 * Reported as a bad request rather than "not found": the product exists and the caller may write it, they just
 * named an asset that is not theirs. Content answers the lookup by omitting ids from other stores, so a seller
 * probing for another store's asset gets this and learns nothing about whether it exists.
 * </p>
 */
public class ProductImageAssetUnknownException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductImageAssetUnknownException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductImageAssetUnknownException of(Long assetId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_IMAGE_ASSET_UNKNOWN,
                ProductImageAssetUnknownException::new)
                .detail("No media asset %s in store %s.", assetId, store)
                .param("assetId", assetId)
                .param("store", store)
                .build();
    }

}
