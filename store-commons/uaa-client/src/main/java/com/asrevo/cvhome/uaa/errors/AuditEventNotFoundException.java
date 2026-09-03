package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/** No audit event with that id — or it is older than the realm's retention and has been trimmed. */
public class AuditEventNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected AuditEventNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static AuditEventNotFoundException of(long id) {
        return new ErrorBuilder<>(UaaErrors.AUDIT_EVENT_NOT_FOUND, AuditEventNotFoundException::new)
                .detail("No audit event with id %d. It may be older than the realm's retention.", id)
                .param("id", id)
                .build();
    }

}
