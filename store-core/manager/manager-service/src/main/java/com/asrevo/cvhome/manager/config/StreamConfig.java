package com.asrevo.cvhome.manager.config;

import com.asrevo.cvhome.s2s.config.internal.LocalEventProcessorConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({LocalEventProcessorConfig.class /*, MessageConverterConfig.class*/})
public class StreamConfig {}
