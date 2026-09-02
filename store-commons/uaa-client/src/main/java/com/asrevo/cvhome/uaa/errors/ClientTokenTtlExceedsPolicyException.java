package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;
import java.time.Duration;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/** The client asked for access tokens that outlive the realm's ceiling; the realm's setting wins. */
public class ClientTokenTtlExceedsPolicyException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String FIELD = "tokenSettings.accessTokenTimeToLive";

    protected ClientTokenTtlExceedsPolicyException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ClientTokenTtlExceedsPolicyException of(Duration requested, Duration max) {
        String detail = String.format("Access tokens may live at most %d seconds in this realm; %d was requested.",
                max.toSeconds(), requested.toSeconds());
        return new ErrorBuilder<>(UaaErrors.CLIENT_TOKEN_TTL_EXCEEDS_POLICY, ClientTokenTtlExceedsPolicyException::new)
                .detail(detail)
                .param("maxSeconds", max.toSeconds())
                .param("requestedSeconds", requested.toSeconds())
                .fieldError(FIELD, UaaErrors.CLIENT_TOKEN_TTL_EXCEEDS_POLICY, detail)
                .build();
    }

}
