package com.asrevo.cvhome.sso.keys;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.realm.RealmContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Hourly: retire what is past its window, then rotate if the active key is older than the platform's interval.
 *
 * <p>
 * In the platform realm, not in none. The keys themselves are deployment-wide and carry no realm, but the work
 * audits itself, and an audit row is a realm's row — written from a thread with no realm it would have landed
 * under the sentinel realm, which is to say nowhere anyone reads.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KeyRotationScheduler {

    private final KeyRotationService keys;

    @Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT5M")
    public void tick() {
        RealmContext.runIn(RealmId.PLATFORM, () -> {
            int retired = keys.retireDue();
            boolean rotated = keys.rotateIfDue();
            if (retired > 0 || rotated) {
                log.info("Signing keys: {} retired, rotated={}", retired, rotated);
            }
        });
    }

}
