package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/** The audit query cannot be answered as asked. */
public class AuditQueryInvalidException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected AuditQueryInvalidException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static AuditQueryInvalidException of(String field, String detail) {
        return new ErrorBuilder<>(UaaErrors.AUDIT_QUERY_INVALID, AuditQueryInvalidException::new)
                .detail(detail)
                .fieldError(field, UaaErrors.AUDIT_QUERY_INVALID, detail)
                .build();
    }

}
