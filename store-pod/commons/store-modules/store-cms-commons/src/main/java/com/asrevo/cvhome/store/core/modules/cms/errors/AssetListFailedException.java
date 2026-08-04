package com.asrevo.cvhome.store.core.modules.cms.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

/**
 * Enumerating the objects under a prefix failed.
 *
 * <p>
 * Separate from a read failure because a caller must not fall back to "then there are no files": the listing methods
 * return an empty or {@code null} collection for a genuinely empty folder, so a swallowed failure here reads as
 * "this store has no images" and silently hides a seller's catalogue.
 * </p>
 */
public class AssetListFailedException extends StoreIOException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected AssetListFailedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static AssetListFailedException of(String prefix, Throwable cause) {
        return new ErrorBuilder<>(CmsErrors.ASSET_LIST_FAILED, AssetListFailedException::new)
                .detail("Could not list the stored files under %s.", prefix)
                .param("prefix", prefix)
                .cause(cause)
                .build();
    }

}
