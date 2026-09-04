package com.asrevo.cvhome.sso.events;

import io.namastack.outbox.annotation.OutboxEvent;

/** An administrator switched the account off; its sessions and tokens are already gone. */
@OutboxEvent(key = "#this.userId()")
public record UserDisabledEvent(String userId, String username) implements SsoEvent {

    @Override
    public String eventType() {
        return UserDisabledEvent.class.getSimpleName();
    }

}
