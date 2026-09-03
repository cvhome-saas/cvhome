package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * A redirect URI the authorization server must not accept.
 *
 * <p>
 * A wildcard or a fragment would let an attacker choose where a code lands; plain HTTP off a local host would send
 * it in clear. Rejected at registration rather than at authorization time, where the failure would land on a
 * shopper.
 * </p>
 */
public class InvalidRedirectUriException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String FIELD = "redirectUris";

    protected InvalidRedirectUriException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param reason one of {@code NOT_ABSOLUTE}, {@code FRAGMENT}, {@code WILDCARD}, {@code PLAIN_HTTP}
     */
    public static InvalidRedirectUriException of(String uri, String reason) {
        String detail = String.format("Redirect URI %s is not allowed: %s.", uri, reason);
        return new ErrorBuilder<>(UaaErrors.INVALID_REDIRECT_URI, InvalidRedirectUriException::new)
                .detail(detail)
                .param("uri", uri)
                .param("reason", reason)
                .fieldError(FIELD, UaaErrors.INVALID_REDIRECT_URI, detail)
                .build();
    }

}
