package com.asrevo.cvhome.s2s.config;

import com.asrevo.cvhome.s2s.config.internal.*;
import com.asrevo.cvhome.s2s.model.SaasProperties;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({EventProcessorConfig.class, CommandProcessorConfig.class, EcsInfoConfig.class, JacksonConfig.class, SwaggerConfig.class, WebConfig.class, WebClientServicesConfig.class, ReactiveGatewayConfig.class})
@EnableConfigurationProperties({ServiceDomainProperties.class, SaasProperties.class})
public class CvhomeSharedConfig {
}
