package com.asrevo.cvhome.store.core.modules.cms.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * The uploaded bytes are not an image any decoder on the classpath recognises, so no resize can be produced.
 *
 * <p>
 * The one failure in this package caused by the caller rather than by us, hence a 400: it was previously a bare
 * {@code new Exception("Cannot read image format ...")} that the surrounding {@code catch (Exception)} turned into the
 * same 500 as an S3 outage, telling a seller their working upload had broken the platform.
 * </p>
 */
public class ImageUnreadableException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ImageUnreadableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ImageUnreadableException of(String imageName) {
        return new ErrorBuilder<>(CmsErrors.IMAGE_UNREADABLE, ImageUnreadableException::new)
                .detail("%s is not in a readable image format.", imageName)
                .param("imageName", imageName)
                .build();
    }

}
