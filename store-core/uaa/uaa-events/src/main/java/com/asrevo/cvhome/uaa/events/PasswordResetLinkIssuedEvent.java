package com.asrevo.cvhome.uaa.events;

import java.time.Instant;

import io.namastack.outbox.annotation.OutboxEvent;

/** An administrator issued a link that lets the account choose a new password. */
@OutboxEvent(key = "#this.userId()")
public record PasswordResetLinkIssuedEvent(String userId, String username, String recipientEmail, String recipientName,
                                           String link, Instant expiresAt, String locale) implements LinkIssuedEvent {

    @Override
    public String eventType() {
        return PasswordResetLinkIssuedEvent.class.getSimpleName();
    }

}
