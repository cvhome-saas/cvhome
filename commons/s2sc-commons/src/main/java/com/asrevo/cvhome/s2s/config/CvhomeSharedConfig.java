package com.asrevo.cvhome.s2s.config;

import com.asrevo.cvhome.s2s.config.internal.CommandProcessorConfig;
import com.asrevo.cvhome.s2s.config.internal.EventProcessorConfig;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({EventProcessorConfig.class, CommandProcessorConfig.class})
@EnableConfigurationProperties({ServiceDomainProperties.class})
public class CvhomeSharedConfig {
}
