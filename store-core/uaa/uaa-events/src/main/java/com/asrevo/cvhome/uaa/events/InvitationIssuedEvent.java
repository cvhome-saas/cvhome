package com.asrevo.cvhome.uaa.events;

import java.time.Instant;

import io.namastack.outbox.annotation.OutboxEvent;

/** An invitation to set a first password was issued (or re-issued) for a pending account. */
@OutboxEvent(key = "#this.userId()")
public record InvitationIssuedEvent(String userId, String username, String recipientEmail, String recipientName,
                                    String link, Instant expiresAt, String locale) implements LinkIssuedEvent {

    @Override
    public String eventType() {
        return InvitationIssuedEvent.class.getSimpleName();
    }

}
