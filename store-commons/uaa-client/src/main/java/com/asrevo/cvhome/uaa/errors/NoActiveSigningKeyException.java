package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/** No usable active signing key exists and none could be generated: nothing can be minted. */
public class NoActiveSigningKeyException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected NoActiveSigningKeyException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static NoActiveSigningKeyException create(Throwable cause) {
        return new ErrorBuilder<>(UaaErrors.NO_ACTIVE_SIGNING_KEY, NoActiveSigningKeyException::new)
                .detail("No active signing key is available.")
                .cause(cause)
                .build();
    }

}
