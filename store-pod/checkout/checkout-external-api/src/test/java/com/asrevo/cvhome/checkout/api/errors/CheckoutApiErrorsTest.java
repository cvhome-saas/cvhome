package com.asrevo.cvhome.checkout.api.errors;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The only failure this API names for its callers is "no answer"; everything else is a decision answered with 200.
 */
class CheckoutApiErrorsTest {

    private static final String CHECKOUT = "checkout";

    private static final String CODE = "COMMON.INTERNAL_ERROR";

    private static final String DETAIL = "boom";

    private static final String KEY = "k";

    private static final String VALUE = "v";

    @Test
    void anUnreachableCheckoutBecomesTheUnavailableException() {
        RemoteErrorContext context = new RemoteErrorContext(null, null, Map.of(), List.of(), CHECKOUT, 0, null,
                new RuntimeException("refused"));

        Throwable built = CheckoutApiErrors.CATALOG.transportFailure().create(context);

        assertThat(built).isInstanceOf(CheckoutApiUnavailableException.class);
        CheckoutApiUnavailableException e = (CheckoutApiUnavailableException) built;
        assertThat(e.remoteService()).isEqualTo(CHECKOUT);
        assertThat(e.category()).isEqualTo(ErrorCategory.REMOTE_SERVICE);
        assertThat(e.getMessage()).contains("could not be reached");
        assertThat(CheckoutApiErrors.CATALOG.find("CHECKOUT.ORDER.NOT_FOUND")).isEmpty();
    }

    @Test
    void aDetailFromTheRemoteIsKept() {
        RemoteErrorContext context = new RemoteErrorContext(CODE, DETAIL, Map.of(KEY, VALUE), List.of(), CHECKOUT, 500,
                "trace", null);

        CheckoutApiUnavailableException e = CheckoutApiUnavailableException.from(context);

        assertThat(e.getMessage()).contains(DETAIL);
        assertThat(e.remoteStatus()).isEqualTo(500);
        assertThat(e.remoteCode()).isEqualTo(CODE);
        assertThat(e.params()).containsEntry(KEY, VALUE);
    }
}
