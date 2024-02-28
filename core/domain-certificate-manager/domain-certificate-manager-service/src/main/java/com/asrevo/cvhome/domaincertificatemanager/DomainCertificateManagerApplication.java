package com.asrevo.cvhome.domaincertificatemanager;

import com.asrevo.cvhome.domaincertificatemanager.config.AutoOrderDomainsProperties;
import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties;
import com.asrevo.cvhome.domaincertificatemanager.config.ServiceDomainProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        AutoOrderDomainsProperties.class,
        ServiceDomainProperties.class,
        DcmChallengesConfigProperties.class})
public class DomainCertificateManagerApplication {

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(DomainCertificateManagerApplication.class, args);
    }

}
