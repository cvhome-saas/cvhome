package com.asrevo.cvhome.store.core.modules.cms.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No object exists under the requested key.
 *
 * <p>
 * The only failure in this package that is not the platform's fault, and the reason the store's {@code catch (Exception
 * e)} had to be split: asking for a deleted image used to produce the same 500 as an unreachable bucket, so a caller
 * could neither show a sensible message nor decide whether retrying was worth anything.
 * </p>
 */
public class AssetNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected AssetNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static AssetNotFoundException of(String key) {
        return of(key, null);
    }

    public static AssetNotFoundException of(String key, Throwable cause) {
        return new ErrorBuilder<>(CmsErrors.ASSET_NOT_FOUND, AssetNotFoundException::new)
                .detail("No stored file under %s.", key)
                .param("key", key)
                .cause(cause)
                .build();
    }

}
