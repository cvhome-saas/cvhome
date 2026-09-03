package com.asrevo.cvhome.sso.idp;

import com.asrevo.cvhome.sso.domain.User;

/**
 * How a brokered login resolved: the account to sign in as, or a link waiting for confirmation.
 *
 * @param user    the local account, when the login may proceed
 * @param pending the link to confirm, when it may not yet
 */
public record BrokerOutcome(User user, PendingLink pending) {

    public static BrokerOutcome signedIn(User user) {
        return new BrokerOutcome(user, null);
    }

    public static BrokerOutcome confirm(PendingLink pending) {
        return new BrokerOutcome(null, pending);
    }

    public boolean needsConfirmation() {
        return pending != null;
    }

}
