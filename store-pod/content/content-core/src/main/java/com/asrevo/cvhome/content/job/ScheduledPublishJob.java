package com.asrevo.cvhome.content.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.content.service.PublishingService;

import lombok.RequiredArgsConstructor;

/**
 * Promotes due {@code SCHEDULED} items and archives expired {@code PUBLISHED} ones, once a minute. Safe to run on
 * several instances: the predicates exclude anything already moved, and a lost race is a no-op update.
 */
@Component
@RequiredArgsConstructor
public class ScheduledPublishJob {

    private final PublishingService publishing;

    @Scheduled(fixedDelayString = "${com.asrevo.cvhome.content.scheduler.delay:PT60S}",
            initialDelayString = "${com.asrevo.cvhome.content.scheduler.initial-delay:PT30S}")
    public void run() {
        publishing.tick();
    }

}
