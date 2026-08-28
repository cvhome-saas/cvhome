package com.asrevo.cvhome.billing.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables the trial-expiry, suspension and deferred-plan-change jobs.
 *
 * <p>
 * There is no distributed lock behind them. Each job only queries and then writes a command to the outbox, which
 * partitions by store id — so several instances may notice the same due row, but only one ends up doing the work.
 * </p>
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

}
