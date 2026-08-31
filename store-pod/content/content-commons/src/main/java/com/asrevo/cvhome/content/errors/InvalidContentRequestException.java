package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * The request body is semantically wrong in a way bean validation cannot express. Renders as HTTP 400.
 */
public class InvalidContentRequestException extends ValidationException {

    private static final String FILENAME = "filename";

    @Serial
    private static final long serialVersionUID = 1L;

    protected InvalidContentRequestException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static InvalidContentRequestException scheduleInvalid(String reason) {
        return new ErrorBuilder<>(ContentErrors.SCHEDULE_INVALID, InvalidContentRequestException::new)
                .detail("Invalid schedule: %s", reason)
                .fieldError("publishAt", ContentErrors.SCHEDULE_INVALID, reason).build();
    }

    public static InvalidContentRequestException menuTargetInvalid(String reason) {
        return new ErrorBuilder<>(ContentErrors.MENU_TARGET_INVALID, InvalidContentRequestException::new)
                .detail("Invalid menu target: %s", reason).build();
    }

    public static InvalidContentRequestException bulkTooLarge(int size, int max) {
        return new ErrorBuilder<>(ContentErrors.BULK_TOO_LARGE, InvalidContentRequestException::new)
                .detail("Bulk request carries %s ids; the maximum is %s.", size, max)
                .param("size", size).param("max", max).build();
    }

    public static InvalidContentRequestException layoutInvalid(String reason) {
        return new ErrorBuilder<>(ContentErrors.LAYOUT_INVALID, InvalidContentRequestException::new)
                .detail("Invalid layout: %s", reason).build();
    }

    public static InvalidContentRequestException mediaTypeNotAllowed(String filename, String mimeType) {
        return new ErrorBuilder<>(ContentErrors.MEDIA_TYPE_NOT_ALLOWED, InvalidContentRequestException::new)
                .detail("%s (%s) is not an accepted file type.", filename, mimeType)
                .param(FILENAME, filename).param("mimeType", mimeType).build();
    }

    public static InvalidContentRequestException mediaUnreadable(String filename, Throwable cause) {
        return new ErrorBuilder<>(ContentErrors.MEDIA_UNREADABLE, InvalidContentRequestException::new)
                .detail("%s could not be read.", filename)
                .param(FILENAME, filename).cause(cause).build();
    }

}
