package com.asrevo.cvhome.podregistry.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables the health probe sweep. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
