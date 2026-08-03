/**
 *
 */
package com.asrevo.cvhome.store.core.exception;

import java.io.Serial;
import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.store.errors.LegacyErrors;

/**
 * Deprecated conversion failure, kept so existing {@code throws ConversionException} signatures compile while modules
 * are migrated. Renders as a 400 with a code instead of falling through to a 500.
 *
 * <p>
 * It extends the shared {@link com.asrevo.cvhome.errors.ConversionException} category base rather than
 * {@code BaseException} directly, which is what lets a migrated populator throw a condition-named type through the
 * same {@link com.asrevo.cvhome.store.core.populator.DataPopulator} signature that un-migrated populators still
 * satisfy with this one. Without that, the 27 files still on this type would all have had to move at once.
 * </p>
 *
 * @author Umesh A
 * @deprecated use {@link com.asrevo.cvhome.errors.ConversionException} with a context-specific error code.
 */
@Deprecated(since = "error-handling-refactor")
public class ConversionException extends com.asrevo.cvhome.errors.ConversionException {

    @Serial
    private static final long serialVersionUID = 687400310032876603L;

    public ConversionException(final String msg, final Throwable cause) {
        super(payloadOf(msg), cause);
    }

    public ConversionException(final String msg) {
        super(payloadOf(msg), null);
    }

    public ConversionException(Throwable t) {
        super(payloadOf(t.getMessage()), t);
    }

    private static ErrorPayload payloadOf(String message) {
        return new ErrorPayload(LegacyErrors.CONVERSION, message, Map.of(), List.of());
    }

}
