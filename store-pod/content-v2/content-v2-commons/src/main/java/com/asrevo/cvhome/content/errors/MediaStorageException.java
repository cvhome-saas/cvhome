package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

public class MediaStorageException extends StoreIOException {
    @Serial
    private static final long serialVersionUID = 1L;

    protected MediaStorageException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static MediaStorageException causedBy(Throwable cause) {
        return new ErrorBuilder<>(ContentErrors.MEDIA_STORAGE_FAILED, MediaStorageException::new)
                .detail("Media could not be stored.")
                .cause(cause)
                .build();
    }
}
