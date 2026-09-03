package com.asrevo.cvhome.uaa.events;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * The account is gone. The contract tenancy's membership reconciliation subscribes to: a deleted uaa user should
 * not keep an {@code org_member} row.
 */
@OutboxEvent(key = "#this.userId()")
public record UserDeletedEvent(String userId, String username, String email) implements UaaEvent {

    @Override
    public String eventType() {
        return UserDeletedEvent.class.getSimpleName();
    }

}
