package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.gateway.service.DomainReferenceService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import static com.asrevo.cvhome.s2s.utils.WebClientsUtils.build;

@Configuration
public class ClientsConfig {
    @Bean
    public DomainReferenceService domainReferenceService(@Qualifier("defaultMicroServiceBuilder") WebClient.Builder builder) {
        return build(builder, "lb://dcm", DomainReferenceService.class);
    }
}
