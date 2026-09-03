package com.asrevo.cvhome.cua.realm;

import java.io.Serial;

/**
 * A request whose host says one store and whose parameters say another.
 *
 * <p>
 * Unchecked because there is no caller that could sensibly recover: by the time this is thrown the request has
 * already failed to identify one store, and the only correct answer is to stop. It is not the shopper's mistake —
 * the storefront builds these forms — so it reads as a server-side fault, which is what a misconfigured theme or
 * a tampered form both are.
 * </p>
 */
public class CrossStoreRequestException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE = """
            This request arrived for store %s but names store %s. A request may only act in the store whose page \
            it came from.""";

    public CrossStoreRequestException(String fromHost, String claimed) {
        super(MESSAGE.formatted(fromHost, claimed));
    }

}
