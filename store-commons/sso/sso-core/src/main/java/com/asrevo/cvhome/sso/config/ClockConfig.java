package com.asrevo.cvhome.sso.config;

import java.time.Clock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * One injectable clock so lockouts, token windows, key rotation and the tests agree on "now".
 *
 * <p>
 * {@code @ConditionalOnMissingBean} is what lets test-support's {@code TestClockConfiguration} substitute a
 * {@code MutableClock} without an override flag.
 * </p>
 */
@Configuration
@EnableScheduling
public class ClockConfig {

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock clock() {
        return Clock.systemUTC();
    }

}
