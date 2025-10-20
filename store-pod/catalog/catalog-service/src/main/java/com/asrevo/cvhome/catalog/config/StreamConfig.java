package com.asrevo.cvhome.catalog.config;

import com.asrevo.cvhome.s2s.config.internal.LocalEventProcessorConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({LocalEventProcessorConfig.class})
public class StreamConfig {}
