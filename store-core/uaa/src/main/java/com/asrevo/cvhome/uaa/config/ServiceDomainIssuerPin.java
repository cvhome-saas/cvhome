package com.asrevo.cvhome.uaa.config;

import java.util.Objects;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.commons.domain.ServiceDomain;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import com.asrevo.cvhome.s2s.utils.UrlNormalize;
import com.asrevo.cvhome.sso.config.IssuerPin;

/**
 * uaa's issuer: its own entry in the service registry, and nothing derived from the request.
 *
 * <p>
 * This is one of the two seams that make uaa and cua the same server. cua pins one issuer per pod instead,
 * because its browser traffic arrives on merchant-owned hosts that no resource server's trust list could
 * enumerate. Both refuse to start unpinned, for the same reason: a token whose {@code iss} depends on the route
 * the request took is a token that verifies from one entry point and is rejected from another.
 * </p>
 */
@Configuration
public class ServiceDomainIssuerPin {

    private static final String UAA = "uaa";

    private static final String UNPINNED = """
            uaa's issuer is not configured: com.asrevo.cvhome.services.uaa must carry schema, domain and port \
            so every token names the same issuer whatever host the request arrived on.""";

    @Bean
    IssuerPin issuerPin(ServiceDomainProperties services) {
        return () -> {
            ServiceDomain uaa = services.getService(UAA);
            if (Objects.isNull(uaa) || Objects.isNull(uaa.schema()) || Objects.isNull(uaa.domain())
                    || Objects.isNull(uaa.port())) {
                throw new IllegalStateException(UNPINNED);
            }
            return UrlNormalize.normalizeUri(uaa.getServiceHost());
        };
    }

}
