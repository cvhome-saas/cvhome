package com.asrevo.cvhome.merchant.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

/**
 * An uploaded file's bytes could not be read off the request — a truncated multipart body, a client that hung up
 * mid-upload.
 *
 * <p>
 * Distinct from a failed store: nothing was written, and the remedy is to upload again rather than to look at the
 * object store.
 * </p>
 */
public class ContentFileUnreadableException extends StoreIOException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ContentFileUnreadableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ContentFileUnreadableException of(String fileName, Throwable cause) {
        return new ErrorBuilder<>(ContentErrors.FILE_UNREADABLE, ContentFileUnreadableException::new)
                .detail("Could not read the uploaded file %s.", fileName)
                .param("fileName", fileName)
                .cause(cause)
                .build();
    }

}
