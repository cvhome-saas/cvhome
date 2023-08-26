package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.gateway.service.AcmService;
import com.asrevo.cvhome.gateway.service.DomainReferenceService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import static com.asrevo.cvhome.gateway.utils.WebClientsUtils.build;
import static org.springframework.http.HttpMethod.POST;

@Configuration
public class ClientsConfig {

    @Bean
    public AcmService acmService(RestTemplate template, DiscoveryClient discoveryClient) {
        ParameterizedTypeReference<byte[]> responseType = new ParameterizedTypeReference<>() {
        };
        return (domain, fileType) -> {
            // @TODO need to get any instance using discoveryClient
            ServiceInstance instance = discoveryClient.getInstances("certificate-manager").get(0);
            String url = instance.getUri() + "/api/v1/acm/domain-certificate-file?domain=" + domain + "&fileType=" + fileType.name();
            return template.exchange(url, POST, null, responseType);
        };
    }

    @Bean
    public DomainReferenceService domainReferenceService(@Qualifier("defaultMicroServiceBuilder") WebClient.Builder builder) {
        return build(builder, "lb://user", DomainReferenceService.class);
    }
}
