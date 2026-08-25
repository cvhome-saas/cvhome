package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

/**
 * Object storage refused or failed an upload, read or delete. Renders as HTTP 500.
 */
public class MediaStorageException extends StoreIOException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected MediaStorageException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static MediaStorageException of(String operation, String key, Throwable cause) {
        return new ErrorBuilder<>(ContentErrors.MEDIA_STORAGE_FAILED, MediaStorageException::new)
                .detail("Storage %s failed for %s.", operation, key)
                .param("operation", operation).param("key", key).cause(cause).build();
    }

}
