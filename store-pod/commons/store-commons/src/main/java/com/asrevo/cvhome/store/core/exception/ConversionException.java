/**
 *
 */
package com.asrevo.cvhome.store.core.exception;

import java.io.Serial;
import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.store.errors.LegacyErrors;

/**
 * Deprecated conversion failure, kept so existing {@code throws ConversionException} signatures compile while modules
 * are migrated. Now a {@link BaseException}, so its remaining throw sites render as a 400 with a code instead of
 * falling through to a 500.
 *
 * @author Umesh A
 * @deprecated use {@link com.asrevo.cvhome.errors.ConversionException} with a context-specific error code.
 */
@Deprecated(since = "error-handling-refactor")
public class ConversionException extends BaseException {

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
