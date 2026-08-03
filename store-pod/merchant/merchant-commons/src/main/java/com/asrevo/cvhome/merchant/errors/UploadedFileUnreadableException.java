package com.asrevo.cvhome.merchant.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

/**
 * An uploaded image's bytes could not be read off the request.
 *
 * <p>
 * Replaces a {@code RestApiException}, which reported {@code LEGACY.BAD_REQUEST} and therefore a 400 — blaming the
 * caller for what is a broken transfer on our side.
 * </p>
 */
public class UploadedFileUnreadableException extends StoreIOException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected UploadedFileUnreadableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static UploadedFileUnreadableException of(String fileName, Throwable cause) {
        return new ErrorBuilder<>(MerchantErrors.UPLOADED_FILE_UNREADABLE, UploadedFileUnreadableException::new)
                .detail("Could not read the uploaded file %s.", fileName)
                .param("fileName", fileName)
                .cause(cause)
                .build();
    }

}
