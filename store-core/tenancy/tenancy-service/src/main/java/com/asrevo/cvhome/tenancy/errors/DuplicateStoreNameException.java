package com.asrevo.cvhome.tenancy.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A store already exists with that name.
 *
 * <p>
 * There has always been a {@code checkNameExists} call for the create form, but it is a read-then-write: two
 * concurrent creates both pass it and both proceed. The unique constraint added alongside this class is what
 * actually decides, and this is how the loser hears about it — as a 409 naming the collision rather than the raw
 * {@code DuplicateKeyException} a constraint violation would otherwise surface as.
 * </p>
 */
public class DuplicateStoreNameException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected DuplicateStoreNameException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * Deliberately does <strong>not</strong> chain the underlying {@code DuplicateKeyException}.
     *
     * <p>
     * Spring's exception resolver walks the cause chain when looking for a handler, and
     * {@code DataIntegrityErrorHandler} claims {@code DataIntegrityViolationException} — so attaching the cause
     * made the advice answer with the generic {@code COMMON.DATA_INTEGRITY_VIOLATION} instead of this type's own
     * code, silently discarding the specific error. The constraint violation is logged at the call site instead,
     * where it is still available for diagnosis.
     * </p>
     */
    public static DuplicateStoreNameException of(String name) {
        return new ErrorBuilder<>(TenancyErrors.STORE_NAME_TAKEN, DuplicateStoreNameException::new)
                .detail("A store named %s already exists.", name)
                .param("name", name)
                .build();
    }

}
