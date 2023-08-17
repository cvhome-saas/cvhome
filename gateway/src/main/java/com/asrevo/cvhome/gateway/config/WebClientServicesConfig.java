package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.gateway.config.ssl.SslProperties;
import com.asrevo.cvhome.gateway.service.AcmService;
import com.asrevo.cvhome.gateway.service.DomainReferenceService;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.Http2SslContextSpec;

import java.security.cert.CertificateFactory;
import java.util.Optional;
import java.util.function.Supplier;

import static org.springframework.http.HttpMethod.POST;

@Configuration
@Slf4j
public class WebClientServicesConfig {
    public static <T> T build(WebClient.Builder builder, String url, Class<T> tClass) {
        WebClient client = builder
                .baseUrl(url)
                .build();

        WebClientAdapter clientAdapter = WebClientAdapter.forClient(client);
        HttpServiceProxyFactory proxy =
                HttpServiceProxyFactory.builder(clientAdapter)

                        .build();
        return proxy.createClient(tClass);
    }

    private static Supplier<SslContext> getSslContextSupplier(String domain) {
        return () -> {
            try {
                log.warn("will create self signed certificates for domain {} ", domain);
                SelfSignedCertificate certificate = new SelfSignedCertificate(domain);
                Http2SslContextSpec sslContextSpec = Http2SslContextSpec.forServer(certificate.certificate(), certificate.privateKey());
                return sslContextSpec.sslContext();
            } catch (Exception e) {
                log.error("did not manged to create createSelfSsl for " + domain);
                return null;
            }
        };
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder builder() {
        return WebClient.builder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    DomainReferenceService domainReferenceService(WebClient.Builder builder) {
        return build(builder, "lb://certificate-manager", DomainReferenceService.class);
    }

    @Bean
    public Supplier<SslContext> defaultSslContextSupplier(SslProperties sslProperties, CertificateFactory cf) {
        return () -> {
            String domain = sslProperties.getDefaultDomain();
            RestTemplate template = new RestTemplate();
            ParameterizedTypeReference<byte[]> responseType = new ParameterizedTypeReference<>() {
            };
            AcmService acmService = (domain1, fileType) -> {
                String url = "http://localhost:8082/api/v1/acm/domain-certificate-file?domain=" + domain1 + "&fileType=" + fileType.name();
                return template.exchange(url, POST, null, responseType);
            };
            SslContext sslContext = acmService.getSslContext(cf, domain);
            return Optional.ofNullable(sslContext).orElseGet(getSslContextSupplier(domain));
        };
    }

    @Bean
    public AcmService acmService(RestTemplate template, DiscoveryClient discoveryClient) {

        ParameterizedTypeReference<byte[]> responseType = new ParameterizedTypeReference<>() {
        };
        return (domain, fileType) -> {
            ServiceInstance instance = discoveryClient.getInstances("certificate-manager").get(0);
            String url = instance.getUri() + "/api/v1/acm/domain-certificate-file?domain=" + domain + "&fileType=" + fileType.name();
            return template.exchange(url, POST, null, responseType);
        };
    }

    @SneakyThrows
    @Bean
    public CertificateFactory cf() {
        return CertificateFactory.getInstance("X.509");
    }
}
