package com.asrevo.cvhome.s2s.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.asrevo.cvhome.errors.web.ErrorHandlingAutoConfiguration;
import com.asrevo.cvhome.s2s.config.internal.CustomPermissionEvaluator;
import com.asrevo.cvhome.s2s.config.internal.EcsInfoConfig;
import com.asrevo.cvhome.s2s.config.internal.IssuerRegistryConfiguration;
import com.asrevo.cvhome.s2s.config.internal.JwtAuthenticationConverterConfiguration;
import com.asrevo.cvhome.s2s.config.internal.MultiIssuerJwtDecoderConfiguration;
import com.asrevo.cvhome.s2s.config.internal.MultiIssuerReactiveJwtDecoderConfiguration;
import com.asrevo.cvhome.s2s.config.internal.ReactiveGatewayConfig;
import com.asrevo.cvhome.s2s.config.internal.ServletPermissionConfig;
import com.asrevo.cvhome.s2s.config.internal.SwaggerConfig;
import com.asrevo.cvhome.s2s.config.internal.WebClientServicesConfig;
import com.asrevo.cvhome.s2s.config.internal.WebConfig;
import com.asrevo.cvhome.s2s.jwt.IssuerRealmProperties;
import com.asrevo.cvhome.s2s.model.AdminUserProperties;
import com.asrevo.cvhome.s2s.model.AppProperties;
import com.asrevo.cvhome.s2s.model.CdnProperties;
import com.asrevo.cvhome.s2s.model.CdnStorageProperties;
import com.asrevo.cvhome.s2s.model.OAuth2ClientProperties;
import com.asrevo.cvhome.s2s.model.PodInfoProperties;
import com.asrevo.cvhome.s2s.model.PodProperties;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import com.asrevo.cvhome.s2s.model.StoreProductImageProperties;
import com.asrevo.cvhome.s2s.model.StripeProperties;
import com.asrevo.cvhome.s2s.model.TestStoreProperties;

@Configuration
@Import({EcsInfoConfig.class, SwaggerConfig.class, WebConfig.class, WebClientServicesConfig.class,
        ReactiveGatewayConfig.class, IssuerRegistryConfiguration.class, MultiIssuerJwtDecoderConfiguration.class,
        JwtAuthenticationConverterConfiguration.class,
        MultiIssuerReactiveJwtDecoderConfiguration.class, CustomPermissionEvaluator.class,
        ServletPermissionConfig.class, ErrorHandlingAutoConfiguration.class})
@EnableConfigurationProperties({PodInfoProperties.class, ServiceDomainProperties.class, AppProperties.class,
        PodProperties.class, CdnProperties.class, StripeProperties.class, StoreProductImageProperties.class,
        CdnStorageProperties.class, IssuerRealmProperties.class, TestStoreProperties.class,
        AdminUserProperties.class, OAuth2ClientProperties.class
})
public class CvhomeSharedConfig {

}
