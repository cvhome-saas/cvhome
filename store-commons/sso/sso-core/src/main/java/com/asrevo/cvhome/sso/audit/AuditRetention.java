package com.asrevo.cvhome.sso.audit;

import java.time.Clock;
import java.time.Duration;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.settings.SettingsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** One realm's trim, in one transaction — {@link AuditRetentionJob} is what decides which realms and when. */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditRetention {

    private final AuditEventRepository repository;

    private final SettingsService settings;

    private final Clock clock;

    @Transactional
    public void trim(RealmId realm) {
        int days = settings.current().auditRetentionDays();
        int deleted = repository.deleteOlderThan(clock.instant().minus(Duration.ofDays(days)));
        if (deleted > 0) {
            log.info("Audit retention removed {} events older than {} days from realm {}", deleted, days, realm);
        }
    }

}
