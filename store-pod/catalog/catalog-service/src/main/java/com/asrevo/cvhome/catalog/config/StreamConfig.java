package com.asrevo.cvhome.catalog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.asrevo.cvhome.s2s.config.internal.LocalEventProcessorConfig;

@Configuration
@Import({LocalEventProcessorConfig.class})
public class StreamConfig {

}
