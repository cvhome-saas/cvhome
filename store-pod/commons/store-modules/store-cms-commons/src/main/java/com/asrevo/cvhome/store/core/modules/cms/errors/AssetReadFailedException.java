package com.asrevo.cvhome.store.core.modules.cms.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

/**
 * The object exists but could not be fetched or streamed back — the bucket was unreachable, credentials were refused,
 * the stream broke part-way.
 *
 * <p>
 * Distinct from {@link AssetNotFoundException} because retrying this one can succeed and retrying that one never will.
 * </p>
 */
public class AssetReadFailedException extends StoreIOException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected AssetReadFailedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static AssetReadFailedException of(String key, Throwable cause) {
        return new ErrorBuilder<>(CmsErrors.ASSET_READ_FAILED, AssetReadFailedException::new)
                .detail("Could not read the stored file under %s.", key)
                .param("key", key)
                .cause(cause)
                .build();
    }

}
