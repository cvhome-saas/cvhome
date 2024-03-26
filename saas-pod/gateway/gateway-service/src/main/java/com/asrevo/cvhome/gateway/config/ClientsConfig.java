package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.gateway.service.AcmService;
import com.asrevo.cvhome.s2s.clients.RouterAllocationService;
import com.asrevo.cvhome.s2s.config.internal.WebClientBuilder;
import com.asrevo.cvhome.s2s.model.ServiceDomain;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpMethod.POST;

@Configuration
public class ClientsConfig {

    @Bean
    public AcmService acmService(Environment environment, RestTemplate template, @Qualifier("microClientBuilder") RestClient.Builder microClientBuilder, ServiceDomainProperties serviceDomainProperties) {
        ParameterizedTypeReference<byte[]> responseType = new ParameterizedTypeReference<>() {
        };
        ServiceDomain gatewayService = serviceDomainProperties.services().get("core-gateway");
        String domainCertificateManagerUrl = gatewayService.getServiceHost("dcm");
        return (domain, fileType) -> {
            String url = domainCertificateManagerUrl + "/api/v1/acm/domain-certificate-file?domain=" + domain + "&fileType=" + fileType.name();
            return template.exchange(url, POST, null, responseType);
        };
//        return build(microClientBuilder, "lb://dcm", AcmService.class);
    }

    @Bean
    public RouterAllocationService routerAllocationService(WebClientBuilder webClientBuilder) {
        return webClientBuilder.buildClient("router", RouterAllocationService.class);
    }
}
