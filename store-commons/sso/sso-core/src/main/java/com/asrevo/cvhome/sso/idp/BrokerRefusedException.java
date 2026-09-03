package com.asrevo.cvhome.sso.idp;

import java.io.Serial;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

/**
 * A brokered login uaa will not complete, with the code the sign-in page explains: {@code idp_rejected},
 * {@code idp_unknown_user}, {@code account_locked}, {@code account_disabled}, {@code idp_no_email}.
 * An {@link OAuth2AuthenticationException} so Spring's login filter routes it to the failure handler.
 */
public class BrokerRefusedException extends OAuth2AuthenticationException {

    public static final String REJECTED = "idp_rejected";

    public static final String UNKNOWN_USER = "idp_unknown_user";

    public static final String LOCKED = "account_locked";

    public static final String DISABLED = "account_disabled";

    public static final String NO_EMAIL = "idp_no_email";

    /** Not a refusal: the login continues once the person confirms with their password. */
    public static final String LINK_REQUIRED = "link_required";

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient PendingLink pending;

    public BrokerRefusedException(String code, String description) {
        super(new OAuth2Error(code, description, null));
        this.pending = null;
    }

    public BrokerRefusedException(PendingLink pending) {
        super(new OAuth2Error(LINK_REQUIRED, "Confirm with the account's password to link this login.", null));
        this.pending = pending;
    }

    public PendingLink pending() {
        return pending;
    }

    public String code() {
        return getError().getErrorCode();
    }

}
