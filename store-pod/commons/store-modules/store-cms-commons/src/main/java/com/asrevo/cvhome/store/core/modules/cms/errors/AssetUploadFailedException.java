package com.asrevo.cvhome.store.core.modules.cms.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

/**
 * Storing an object failed.
 *
 * <p>
 * A caller must treat the write as not having happened: the resize path uploads several derivatives per image, so a
 * failure part-way leaves some present and some missing, and only the caller knows whether to roll the rest back.
 * </p>
 */
public class AssetUploadFailedException extends StoreIOException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected AssetUploadFailedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static AssetUploadFailedException of(String key, Throwable cause) {
        return new ErrorBuilder<>(CmsErrors.ASSET_UPLOAD_FAILED, AssetUploadFailedException::new)
                .detail("Could not store the file under %s.", key)
                .param("key", key)
                .cause(cause)
                .build();
    }

}
