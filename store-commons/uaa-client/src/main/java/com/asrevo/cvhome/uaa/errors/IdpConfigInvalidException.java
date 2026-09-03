package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/** A provider whose settings cannot produce a working client registration. Names the field. */
public class IdpConfigInvalidException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected IdpConfigInvalidException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static IdpConfigInvalidException of(String field, String detail) {
        return new ErrorBuilder<>(UaaErrors.IDP_CONFIG_INVALID, IdpConfigInvalidException::new)
                .detail(detail)
                .param("field", field)
                .fieldError(field, UaaErrors.IDP_CONFIG_INVALID, detail)
                .build();
    }

}
