package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.TooManyRequestsException;

/**
 * The {@code test} action, used more times in an hour than one realm is allowed.
 *
 * <p>
 * The limit is not about load. Testing a provider makes this server fetch a URL of the merchant's choosing, so an
 * unlimited test button is a port scanner that reports its findings through the difference between "answered" and
 * "no route" — and an outbound traffic generator pointed at whoever the merchant likes.
 * </p>
 */
public class IdpTestThrottledException extends TooManyRequestsException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected IdpTestThrottledException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static IdpTestThrottledException of(String alias) {
        return new ErrorBuilder<>(UaaErrors.IDP_TEST_THROTTLED, IdpTestThrottledException::new)
                .detail("This provider has been tested too many times recently. Try again later.")
                .param("alias", alias)
                .build();
    }

}
