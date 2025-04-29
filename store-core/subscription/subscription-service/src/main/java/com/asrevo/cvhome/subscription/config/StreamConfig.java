package com.asrevo.cvhome.subscription.config;

import com.asrevo.cvhome.s2s.config.internal.MessageConverterConfig;
import com.asrevo.cvhome.s2s.config.internal.StreamEventProcessorConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({StreamEventProcessorConfig.class, MessageConverterConfig.class})
public class StreamConfig {
}
