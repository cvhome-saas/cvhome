package com.asrevo.cvhome.s2s.config;

import com.asrevo.cvhome.s2s.config.internal.*;
import com.asrevo.cvhome.s2s.jwt.IssuerUriSetConfigrationProperties;
import com.asrevo.cvhome.s2s.model.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EcsInfoConfig.class, SwaggerConfig.class, WebConfig.class, WebClientServicesConfig.class,
		ReactiveGatewayConfig.class, IssuerUriSetJwtDecoderConfiguration.class,
		IssuerUriSetReactiveJwtDecoderConfiguration.class })
@EnableConfigurationProperties({ PodInfoProperties.class, ServiceDomainProperties.class, AppProperties.class,
		CdnProperties.class, StripeProperties.class, StoreProductImageProperties.class, CdnStorageProperties.class,
		KeycloakCredentialsProperties.class, IssuerUriSetConfigrationProperties.class })
public class CvhomeSharedConfig {

}
