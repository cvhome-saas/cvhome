package com.asrevo.cvhome.sso.realm;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorCode;

/**
 * A request that cannot be placed in a realm.
 *
 * <p>
 * Unchecked, and thrown from {@link RealmFilter} rather than from a controller, because the realm has to be known
 * before anything else runs — authentication included. That is also why it carries its own {@link ErrorCode}: a
 * filter throws outside the reach of the {@code @ControllerAdvice}, so {@link RealmFilter} renders the problem
 * body itself and needs to know which code to render.
 * </p>
 */
public class RealmResolutionException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient ErrorCode code;

    public RealmResolutionException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }

}
