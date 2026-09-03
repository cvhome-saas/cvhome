package com.asrevo.cvhome.sso.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.sso.invitation.LinksProperties;

@Configuration
@EnableConfigurationProperties(LinksProperties.class)
public class LinksConfig {
}
