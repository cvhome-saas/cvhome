package com.asrevo.cvhome.errors;

import java.io.Serial;

/**
 * The caller is authenticated but not permitted to reach this resource — most often a tenant-isolation guard catching a
 * request for another store's data. Renders as HTTP 403.
 *
 * <p>
 * Named with a {@code Store} prefix to avoid colliding with {@code org.springframework.security.access.AccessDeniedException},
 * which callers frequently import in the same file.
 * </p>
 */
public abstract class AccessDeniedStoreException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected AccessDeniedStoreException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }


}
