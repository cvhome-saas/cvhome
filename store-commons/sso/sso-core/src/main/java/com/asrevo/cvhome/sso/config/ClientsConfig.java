package com.asrevo.cvhome.sso.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.sso.client.ClientsProperties;

@Configuration
@EnableConfigurationProperties(ClientsProperties.class)
public class ClientsConfig {
}
