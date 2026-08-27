package com.asrevo.cvhome.s2s.config.internal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.s2s.jwt.IssuerRealmProperties;
import com.asrevo.cvhome.s2s.jwt.IssuerRegistry;

/**
 * The realm registry, shared by the servlet decoder, the reactive decoder and the authorities converter. It sits
 * outside all three because it is a plain reading of configuration, and because who-signed-this-token is one
 * question that must be answered the same way whichever of them asks it.
 */
@Configuration
public class IssuerRegistryConfiguration {

    @Bean
    @Conditional(IssuerRealmsCondition.class)
    IssuerRegistry issuerRegistry(IssuerRealmProperties properties) {
        return properties.toRegistry();
    }

}
