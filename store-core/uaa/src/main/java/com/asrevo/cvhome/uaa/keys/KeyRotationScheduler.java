package com.asrevo.cvhome.uaa.keys;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Hourly: retire what is past its window, then rotate if the active key is older than the realm's interval. */
@Component
@RequiredArgsConstructor
@Slf4j
public class KeyRotationScheduler {

    private final KeyRotationService keys;

    @Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT5M")
    public void tick() {
        int retired = keys.retireDue();
        boolean rotated = keys.rotateIfDue();
        if (retired > 0 || rotated) {
            log.info("Signing keys: {} retired, rotated={}", retired, rotated);
        }
    }

}
