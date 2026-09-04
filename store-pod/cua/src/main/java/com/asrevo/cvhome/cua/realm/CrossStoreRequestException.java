package com.asrevo.cvhome.cua.realm;

import java.io.Serial;

import com.asrevo.cvhome.sso.realm.RealmResolutionException;
import com.asrevo.cvhome.uaa.errors.UaaErrors;

/**
 * A request whose host says one store and whose parameters say another.
 *
 * <p>
 * The edge resolved the storefront host to a store before this request landed, so a form or query parameter
 * naming a different one is either a misconfigured theme or an attempt to act in another merchant's realm.
 * Refused rather than resolved to either store — quietly picking a winner is what would make the second one work.
 * </p>
 */
public class CrossStoreRequestException extends RealmResolutionException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE = """
            This request arrived for store %s but names store %s. A request may only act in the store whose page \
            it came from.""";

    public CrossStoreRequestException(String fromHost, String claimed) {
        super(UaaErrors.CROSS_STORE_REQUEST, MESSAGE.formatted(fromHost, claimed));
    }

}
