package com.asrevo.cvhome.s2s.config.internal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.s2s.jwt.IssuerRegistry;
import com.asrevo.cvhome.s2s.jwt.IssuerUriSetConfigrationProperties;

/**
 * The realm registry, shared by the servlet decoder, the reactive decoder and the authorities converter. It sits
 * outside all three because it is a plain reading of configuration, and because who-signed-this-token is one
 * question that must be answered the same way whichever of them asks it.
 */
@Configuration
public class IssuerRegistryConfiguration {

    @Bean
    @Conditional(IssuerUriSetCondition.class)
    IssuerRegistry issuerRegistry(IssuerUriSetConfigrationProperties properties) {
        return properties.toRegistry();
    }

}
