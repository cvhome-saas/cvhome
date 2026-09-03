package com.asrevo.cvhome.uaa.delivery;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.asrevo.cvhome.uaa.events.InvitationIssuedEvent;
import com.asrevo.cvhome.uaa.invitation.LinksProperties;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import static org.assertj.core.api.Assertions.assertThat;

/** The link is a credential: it reaches the log only where configuration says the log is the delivery channel. */
class LoggingLinkDeliveryHandlerTest {

    private static final String LINK = "http://uaa.test/accept-invitation?token=secret-token";

    private static final String EMAIL = "someone@example.com";

    private static final InvitationIssuedEvent EVENT = new InvitationIssuedEvent("id", "someone", EMAIL,
            "Some One", LINK, Instant.parse("2026-09-09T12:00:00Z"), "en");

    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    private final Logger logger = (Logger) LoggerFactory.getLogger(LoggingLinkDeliveryHandler.class);

    @BeforeEach
    void setUp() {
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    void aDeploymentNeverLogsTheLink() {
        new LoggingLinkDeliveryHandler(properties(false)).onInvitation(EVENT);

        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.getFirst().getFormattedMessage()).contains(EMAIL).doesNotContain("secret-token");
    }

    @Test
    void locallyTheLogIsTheDeliveryChannel() {
        new LoggingLinkDeliveryHandler(properties(true)).onInvitation(EVENT);

        assertThat(appender.list).hasSize(2);
        assertThat(appender.list.getLast().getFormattedMessage()).contains(LINK);
    }

    private static LinksProperties properties(boolean logLinks) {
        return new LinksProperties(Duration.ofDays(7), Duration.ofHours(1), logLinks);
    }

}
