package com.asrevo.cvhome.uaa.audit;

import java.time.Clock;
import java.time.Duration;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.uaa.settings.SettingsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Trims the audit log to {@code settings.auditRetentionDays}, nightly. */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditRetentionJob {

    private final AuditEventRepository repository;

    private final SettingsService settings;

    private final Clock clock;

    @Scheduled(cron = "0 17 3 * * *")
    @Transactional
    public void trim() {
        int days = settings.current().auditRetentionDays();
        int deleted = repository.deleteOlderThan(clock.instant().minus(Duration.ofDays(days)));
        if (deleted > 0) {
            log.info("Audit retention removed {} events older than {} days", deleted, days);
        }
    }

}
