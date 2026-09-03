package com.asrevo.cvhome.sso.settings;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** The platform's limits are configuration, so they need enabling; a realm's own policy is rows. */
@Configuration
@EnableConfigurationProperties(SsoPlatformCeilings.class)
public class SettingsConfig {
}
