package com.asrevo.cvhome.uaa.events;

import java.util.Map;

import com.asrevo.cvhome.commons.event.Event;

/**
 * Every event uaa publishes through its outbox.
 *
 * <p>
 * Keyed by the account the event is about, so everything that happens to one account is delivered in the order it
 * happened — an invitation is never handled after the account it belongs to was deleted.
 * </p>
 */
public interface UaaEvent extends Event {

    /** The uaa account id, as a string because it is the outbox partition key. */
    String userId();

    String username();

    @Override
    default Map<String, String> data() {
        return Map.of("userId", userId(), "username", username());
    }

}
