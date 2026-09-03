package com.asrevo.cvhome.uaa.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.uaa.invitation.LinksProperties;

@Configuration
@EnableConfigurationProperties(LinksProperties.class)
public class LinksConfig {
}
