package com.asrevo.cvhome.store.core.modules.cms.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

/**
 * Removing an object or a store's folder failed, so the data may still be there.
 *
 * <p>
 * Worth its own type rather than sharing one with a failed upload: the remedies are opposite. A failed upload leaves
 * nothing to clean up; a failed delete leaves an orphan that a later reconciliation has to find.
 * </p>
 */
public class AssetDeleteFailedException extends StoreIOException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected AssetDeleteFailedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static AssetDeleteFailedException of(String key, Throwable cause) {
        return new ErrorBuilder<>(CmsErrors.ASSET_DELETE_FAILED, AssetDeleteFailedException::new)
                .detail("Could not remove the stored file under %s.", key)
                .param("key", key)
                .cause(cause)
                .build();
    }

}
