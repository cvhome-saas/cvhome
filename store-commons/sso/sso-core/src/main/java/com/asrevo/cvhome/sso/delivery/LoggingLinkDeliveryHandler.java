package com.asrevo.cvhome.sso.delivery;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.sso.events.InvitationIssuedEvent;
import com.asrevo.cvhome.sso.events.LinkIssuedEvent;
import com.asrevo.cvhome.sso.events.PasswordResetLinkIssuedEvent;
import com.asrevo.cvhome.sso.invitation.LinksProperties;

import io.namastack.outbox.annotation.OutboxHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The default consumer of one-time links: the log.
 *
 * <p>
 * uaa has no mail sender, and the platform's delivery service — SMS, WhatsApp, email through whichever API — does
 * not exist yet. Until it does, this handler is the whole delivery channel: it records that a link was issued, for
 * whom and until when, and locally ({@code links.log-links=true}) the link itself so a tester can open it. A
 * deployment never logs the link; the administrator who issued it saw it once in the response and that is the copy
 * that reaches the person.
 * </p>
 *
 * <p>
 * The future service subscribes to the same {@link InvitationIssuedEvent} and {@link PasswordResetLinkIssuedEvent}
 * from {@code uaa-events}; nothing in uaa changes when it arrives. Idempotent in the way an outbox handler must be:
 * a redelivered event produces a second log line and nothing else.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoggingLinkDeliveryHandler {

    private final LinksProperties properties;

    @OutboxHandler
    public void onInvitation(InvitationIssuedEvent event) {
        deliver("invitation", event);
    }

    @OutboxHandler
    public void onPasswordResetLink(PasswordResetLinkIssuedEvent event) {
        deliver("password reset", event);
    }

    void deliver(String kind, LinkIssuedEvent event) {
        log.info("One-time {} link issued for {} <{}> (account {}), valid until {}", kind, event.recipientName(),
                event.recipientEmail(), event.username(), event.expiresAt());
        if (properties.logLinks()) {
            log.info("One-time {} link for {}: {}", kind, event.username(), event.link());
        }
    }

}
