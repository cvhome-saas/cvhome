package com.asrevo.cvhome.sso.events;

import java.time.Instant;

/**
 * A one-time link that has to reach a person.
 *
 * <p>
 * uaa has no mail sender. Issuing an invitation or a password-reset link stores only the token's hash, returns the
 * link once to the administrator who asked, and publishes one of these. Whatever delivers it — today a log line,
 * later a service that sends SMS, WhatsApp or email — subscribes here rather than being called, so uaa never waits
 * on a transport and a transport that is down retries from the outbox instead of failing the administrator's request.
 * </p>
 *
 * <p>
 * The link is a bearer credential. A consumer must treat it as one: hand it to the recipient and nobody else, and
 * never write it to a log a third party can read.
 * </p>
 */
public interface LinkIssuedEvent extends SsoEvent {

    String recipientEmail();

    String recipientName();

    String link();

    Instant expiresAt();

    /** The realm's default locale as a BCP 47 tag, for the message the consumer composes. */
    String locale();

}
