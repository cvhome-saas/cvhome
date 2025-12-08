package com.asrevo.cvhome.checkout.config;

import com.asrevo.cvhome.s2s.config.internal.LocalEventProcessorConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ LocalEventProcessorConfig.class })
public class StreamConfig {

}
