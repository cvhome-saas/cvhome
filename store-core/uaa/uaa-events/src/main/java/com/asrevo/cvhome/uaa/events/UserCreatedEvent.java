package com.asrevo.cvhome.uaa.events;

import io.namastack.outbox.annotation.OutboxEvent;

/** An account came into being — created by an administrator, invited, or provisioned by tenancy. */
@OutboxEvent(key = "#this.userId()")
public record UserCreatedEvent(String userId, String username, String email) implements UaaEvent {

    @Override
    public String eventType() {
        return UserCreatedEvent.class.getSimpleName();
    }

}
