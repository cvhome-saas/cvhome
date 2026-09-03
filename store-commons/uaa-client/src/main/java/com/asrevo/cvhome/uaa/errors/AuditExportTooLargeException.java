package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/** The export would run past the cap. Narrowing the range is the answer; a half-written file is not. */
public class AuditExportTooLargeException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected AuditExportTooLargeException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static AuditExportTooLargeException of(long matched, int cap) {
        return new ErrorBuilder<>(UaaErrors.AUDIT_EXPORT_TOO_LARGE, AuditExportTooLargeException::new)
                .detail("That export would hold %d events; the cap is %d. Narrow the range or the filters.", matched, cap)
                .param("matched", matched)
                .param("cap", cap)
                .build();
    }

}
