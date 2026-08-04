package com.asrevo.cvhome.store.core.modules.cms.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

/**
 * The configured product image dimensions cannot be used — non-numeric, zero or negative.
 *
 * <p>
 * A deployment fault, not a request fault, so it stays a 500 and the caller's upload is never blamed. It sits under
 * {@link StoreIOException} with the rest of the image pipeline because that is where it is raised and what it stops;
 * the {@code CMS.IMAGE.SIZE_MISCONFIGURED} code is what tells an operator to go and fix
 * {@code PRODUCT_IMAGE_HEIGHT_SIZE} rather than to go looking at S3.
 * </p>
 *
 * <p>
 * Also covers the unparseable case, which used to escape the old {@code catch (Exception)} as a raw
 * {@code NumberFormatException} and reach the client as an unexplained 500.
 * </p>
 */
public class ImageSizeMisconfiguredException extends StoreIOException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ImageSizeMisconfiguredException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ImageSizeMisconfiguredException of(String width, String height) {
        return of(width, height, null);
    }

    public static ImageSizeMisconfiguredException of(String width, String height, Throwable cause) {
        return new ErrorBuilder<>(CmsErrors.IMAGE_SIZE_MISCONFIGURED, ImageSizeMisconfiguredException::new)
                .detail("Product image size is configured as %s x %s, which is not usable.", width, height)
                .param("width", width)
                .param("height", height)
                .cause(cause)
                .build();
    }

}
