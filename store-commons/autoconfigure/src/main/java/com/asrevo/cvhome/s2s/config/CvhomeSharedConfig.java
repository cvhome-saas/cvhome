package com.asrevo.cvhome.s2s.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.asrevo.cvhome.s2s.config.internal.CustomPermissionEvaluator;
import com.asrevo.cvhome.s2s.config.internal.EcsInfoConfig;
import com.asrevo.cvhome.s2s.config.internal.IssuerUriSetJwtDecoderConfiguration;
import com.asrevo.cvhome.s2s.config.internal.IssuerUriSetReactiveJwtDecoderConfiguration;
import com.asrevo.cvhome.s2s.config.internal.ReactiveGatewayConfig;
import com.asrevo.cvhome.s2s.config.internal.ServletPermissionConfig;
import com.asrevo.cvhome.s2s.config.internal.SwaggerConfig;
import com.asrevo.cvhome.s2s.config.internal.WebClientServicesConfig;
import com.asrevo.cvhome.s2s.config.internal.WebConfig;
import com.asrevo.cvhome.s2s.jwt.IssuerUriSetConfigrationProperties;
import com.asrevo.cvhome.s2s.model.AdminUserProperties;
import com.asrevo.cvhome.s2s.model.AppProperties;
import com.asrevo.cvhome.s2s.model.CdnProperties;
import com.asrevo.cvhome.s2s.model.CdnStorageProperties;
import com.asrevo.cvhome.s2s.model.PodInfoProperties;
import com.asrevo.cvhome.s2s.model.PodProperties;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import com.asrevo.cvhome.s2s.model.StoreProductImageProperties;
import com.asrevo.cvhome.s2s.model.StripeProperties;
import com.asrevo.cvhome.s2s.model.TestStoreProperties;

@Configuration
@Import({EcsInfoConfig.class, SwaggerConfig.class, WebConfig.class, WebClientServicesConfig.class,
        ReactiveGatewayConfig.class, IssuerUriSetJwtDecoderConfiguration.class,
        IssuerUriSetReactiveJwtDecoderConfiguration.class, CustomPermissionEvaluator.class,
        ServletPermissionConfig.class})
@EnableConfigurationProperties({PodInfoProperties.class, ServiceDomainProperties.class, AppProperties.class,
        PodProperties.class, CdnProperties.class, StripeProperties.class, StoreProductImageProperties.class,
        CdnStorageProperties.class, IssuerUriSetConfigrationProperties.class, TestStoreProperties.class,
        AdminUserProperties.class
})
public class CvhomeSharedConfig {

}
