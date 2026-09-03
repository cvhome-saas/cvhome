package com.asrevo.cvhome.sso.events;

import java.util.Map;

import com.asrevo.cvhome.commons.event.Event;

/**
 * Every event the SSO server publishes through its outbox.
 *
 * <p>
 * Keyed by the account the event is about, so everything that happens to one account is delivered in the order it
 * happened — an invitation is never handled after the account it belongs to was deleted.
 * </p>
 */
public interface SsoEvent extends Event {

    /** The account id, as a string because it is the outbox partition key. */
    String userId();

    String username();

    @Override
    default Map<String, String> data() {
        return Map.of("userId", userId(), "username", username());
    }

}
