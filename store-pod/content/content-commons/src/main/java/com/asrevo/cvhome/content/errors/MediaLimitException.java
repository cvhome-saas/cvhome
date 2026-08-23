package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * An upload exceeds the per-file limit or would push the store over its storage quota. Renders as HTTP 413 with the
 * figures the console needs to prompt an upgrade.
 */
public class MediaLimitException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected MediaLimitException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static MediaLimitException tooLarge(String filename, long bytes, long maxBytes) {
        return new ErrorBuilder<>(ContentErrors.MEDIA_TOO_LARGE, MediaLimitException::new)
                .detail("%s is %s bytes; the limit is %s bytes per file.", filename, bytes, maxBytes)
                .param("filename", filename).param("bytes", bytes).param("maxBytes", maxBytes).build();
    }

    public static MediaLimitException quotaExceeded(long bytesUsed, long bytesQuota, long bytesRequested) {
        return new ErrorBuilder<>(ContentErrors.MEDIA_QUOTA_EXCEEDED, MediaLimitException::new)
                .detail("Uploading %s bytes would exceed the %s byte storage quota (%s used).", bytesRequested,
                        bytesQuota, bytesUsed)
                .param("bytesUsed", bytesUsed).param("bytesQuota", bytesQuota).param("bytesRequested", bytesRequested)
                .build();
    }

}
